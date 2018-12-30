package com.example.mockapp.slang;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;


import com.example.mockapp.FeedbackActivity;
import com.example.mockapp.OrderEntryFragment;
import com.example.mockapp.OrderListActivity;
import com.example.mockapp.R;
import com.example.mockapp.network.OrderEntry;
import com.example.mockapp.network.OrderList;
import com.slanglabs.slang.internal.util.SlangUserConfig;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import in.slanglabs.platform.application.ISlangApplicationStateListener;
import in.slanglabs.platform.application.SlangApplication;
import in.slanglabs.platform.application.SlangApplicationUninitializedException;
import in.slanglabs.platform.application.SlangLocaleException;
import in.slanglabs.platform.application.actions.DefaultResolvedIntentAction;
import in.slanglabs.platform.session.SlangEntity;
import in.slanglabs.platform.session.SlangResolvedIntent;
import in.slanglabs.platform.session.SlangSession;
import in.slanglabs.platform.ui.SlangScreenContext;
import in.slanglabs.platform.ui.SlangUI;

public class VoiceInterface {

    private static Application appContext;
    private static final String TAG = VoiceInterface.class.getSimpleName();
    private static List<OrderList> orderList;
    private static List<OrderList> orders;
    private static List<String> colorList, brandList;
    private static int num, brandNum, colorNum;
    private static boolean flag = true;
    private static boolean current = false;
    private static boolean cardinal = false;

    public static void init(final Application appContext, String appId, String authKey)
            throws SlangLocaleException {
        VoiceInterface.appContext = appContext;
        orderList = OrderList.initOrderList(appContext.getResources());
        orders = new ArrayList<>();

        SharedPreferences sharedPreferences = appContext
                .getSharedPreferences(ActivityDetector.PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        SlangApplication.initialize(appContext, appId, authKey,
                SlangApplication.getSupportedLocales(), SlangApplication.LOCALE_ENGLISH_IN,
                new ISlangApplicationStateListener() {
            @Override
            public void onInitialized() {
                try {
                    registerActions();
                } catch (SlangApplicationUninitializedException e) {
                    Toast.makeText(
                            appContext,
                            "Slang uninitialized - " + e.getLocalizedMessage(),
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onInitializationFailed(FailureReason failureReason) {
                Toast.makeText(appContext, "Slang not initialized", Toast.LENGTH_LONG).show();
                Log.d(TAG, "Failure Reason: " + failureReason);
            }
        });

    }

    private static void registerActions() throws SlangApplicationUninitializedException {

        //TODO figure out not starting new activities, just keep replacing fragments

        SlangUI.setTriggerImageResource(R.drawable.customer_support2);
        SlangApplication.getIntentDescriptor(ActivityDetector.INTENT_TRACK_DEFAULT)
                .setResolutionAction(new DefaultResolvedIntentAction() {
                    @Override
                    public SlangSession.Status action(SlangResolvedIntent slangResolvedIntent,
                                                      SlangSession slangSession) {
                        Log.d(TAG, "Slang Triggering Default Track Intent");
                        return trackDefault(slangSession, ActivityDetector.MODE_TRACK_DEFAULT);
                    }
                });

        SlangApplication.getIntentDescriptor(ActivityDetector.INTENT_TRACK_PRODUCT)
                .setResolutionAction(new DefaultResolvedIntentAction() {
                    @Override
                    public SlangSession.Status onIntentResolutionBegin(SlangResolvedIntent intent,
                                                                       SlangSession session) {
                        orders.clear();
                        flag = true;
                        return super.onIntentResolutionBegin(intent, session);
                    }

                    @Override
                    public SlangSession.Status onEntityResolved(SlangEntity entity,
                                                                SlangSession session) {
                        Log.d(TAG, "Calling onEntityResolved");
                        switch (entity.getName()) {
                            case ActivityDetector.ENTITY_PRODUCT:
                            case ActivityDetector.ENTITY_COLOR:
                            case ActivityDetector.ENTITY_BRAND:
                            case ActivityDetector.ENTITY_CARDINAL:
                                Log.d(TAG, "Entity resolved");
                                if (entity.getName().equals(ActivityDetector.ENTITY_CARDINAL))
                                    Log.d(TAG, "Cardinal value is " + entity.getValue());
                                trackProduct(entity.getParent(), true);
                                return session.success();
                            case ActivityDetector.ENTITY_DATE:
                                Log.d(TAG, "Date entity value is " + entity.getValue());
                                SimpleDateFormat dateFormat =
                                        new SimpleDateFormat("yyyy-MM-dd");
                                try {
                                    Date userDate = dateFormat.parse(entity.getValue());
                                    Date date = new Date();
                                    if (date.compareTo(userDate) > 0)
                                        Log.d(TAG, "Case true");
                                    else
                                        Log.d(TAG, "Case false");
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                return session.success();
                            default:
                                return super.onEntityResolved(entity, session);
                        }
                    }

                    @Override
                    public SlangSession.Status action(SlangResolvedIntent slangResolvedIntent,
                                                      SlangSession slangSession) {
                        Log.d(TAG, "Slang Triggering Product Track Intent");
                        flag = true;
                        trackProduct(slangResolvedIntent, false);
                        if (num > 0) {
                            slangResolvedIntent.getCompletionStatement()
                                    .overrideAffirmative(getCompletionPrompt(
                                                    SlangUserConfig.getLocale(),
                                                    ActivityDetector.MODE_TRACK_PRODUCT));
                            return slangSession.success();
                        }
                        else {
                            return slangSession.failure();
                        }
                    }
                });

        SlangApplication.getIntentDescriptor(ActivityDetector.INTENT_TRACK_ALL)
                .setResolutionAction(new DefaultResolvedIntentAction() {
                    @Override
                    public SlangSession.Status action(SlangResolvedIntent slangResolvedIntent,
                                                      SlangSession slangSession) {
                        Log.d(TAG, "Slang Triggering All Track Intent");
                        return trackDefault(slangSession, ActivityDetector.MODE_TRACK_ALL);
                    }
                });

        SlangApplication.getIntentDescriptor(ActivityDetector.INTENT_TRACK_REFUND_DEFAULT)
                .setResolutionAction(new DefaultResolvedIntentAction() {
                    @Override
                    public SlangSession.Status action(SlangResolvedIntent slangResolvedIntent,
                                                      SlangSession slangSession) {
                        Log.d(TAG, "Slang Triggering Default Refund Intent");
                        return refundDefault(slangResolvedIntent, slangSession,
                                ActivityDetector.MODE_REFUND_DEFAULT);
                    }
                });

        SlangApplication.getIntentDescriptor(ActivityDetector.INTENT_TRACK_REFUND_PRODUCT)
                .setResolutionAction(new DefaultResolvedIntentAction() {
                    @Override
                    public SlangSession.Status onIntentResolutionBegin(SlangResolvedIntent intent,
                                                                       SlangSession session) {
                        flag = true;
                        return super.onIntentResolutionBegin(intent, session);
                    }

                    @Override
                    public SlangSession.Status action(SlangResolvedIntent slangResolvedIntent,
                                                      SlangSession slangSession) {
                        Log.d(TAG, "Slang Triggering Product Refund Intent");
                        flag = true;
                        refundProduct(slangResolvedIntent, false);
                        if (num > 0) {
                            slangResolvedIntent.getCompletionStatement()
                                    .overrideAffirmative(getCompletionPrompt(
                                            SlangUserConfig.getLocale(),
                                            ActivityDetector.MODE_REFUND_PRODUCT)
                                    );
                            return slangSession.success();
                        }
                        else {
                            return slangSession.failure();
                        }
                    }

                    @Override
                    public SlangSession.Status onEntityResolved(SlangEntity entity,
                                                                SlangSession session) {
                        switch (entity.getName()) {
                            case ActivityDetector.ENTITY_PRODUCT:
                            case ActivityDetector.ENTITY_COLOR:
                            case ActivityDetector.ENTITY_BRAND:
                                Log.d(TAG, "Entity Resolve for Refund Intent");
                                refundProduct(entity.getParent(), true);
                                return session.success();
                            default:
                                return super.onEntityResolved(entity, session);
                        }
                    }
                });

        SlangApplication.getIntentDescriptor(ActivityDetector.INTENT_TRACK_RETURN)
                .setResolutionAction(new DefaultResolvedIntentAction() {
                    @Override
                    public SlangSession.Status action(SlangResolvedIntent slangResolvedIntent,
                                                      SlangSession slangSession) {
                        trackReturn(slangResolvedIntent, true);
                        trackReturn(slangResolvedIntent, false);
                        Log.d(TAG, "Slang Triggering Track Return Intent");
                        if (num > 0) {
                            slangResolvedIntent.getCompletionStatement()
                                    .overrideAffirmative(
                                            getCompletionPrompt(
                                                    SlangUserConfig.getLocale(),
                                                    ActivityDetector.MODE_TRACK_RETURN
                                            )
                                    );
                            return slangSession.success();
                        }
                        else {
                            return slangSession.failure();
                        }
                    }
                });

        SlangApplication.getIntentDescriptor(ActivityDetector.INTENT_RETURN_DEFAULT)
                .setResolutionAction(new DefaultResolvedIntentAction() {
                    @Override
                    public SlangSession.Status action(SlangResolvedIntent slangResolvedIntent,
                                                      SlangSession slangSession) {
                        Log.d(TAG, "Slang Triggering Default Return Intent");
                        return returnDefault(slangSession, ActivityDetector.MODE_RETURN_DEFAULT);
                    }
                });

        SlangApplication.getIntentDescriptor(ActivityDetector.INTENT_RETURN_PRODUCT)
                .setResolutionAction(new DefaultResolvedIntentAction() {

                    @Override
                    public SlangSession.Status onIntentResolutionBegin(SlangResolvedIntent intent,
                                                                       SlangSession session) {
                        orders.clear();
                        flag = true;
                        current = false;
                        return super.onIntentResolutionBegin(intent, session);
                    }

                    @Override
                    public SlangSession.Status action(SlangResolvedIntent slangResolvedIntent,
                                                      SlangSession slangSession) {
                        if(flag) {
                            resolveCurrent(slangResolvedIntent,
                                    ActivityDetector.MODE_RETURN_PRODUCT);
                            if (!current)
                                returnProduct(slangResolvedIntent, true);
                        }
                        flag = true;
                        if (!current) {
                            returnProduct(slangResolvedIntent, false);
                        }
                        if (num > 0) {
                            slangResolvedIntent.getCompletionStatement()
                                    .overrideAffirmative(
                                            getCompletionPrompt(
                                                    SlangUserConfig.getLocale(),
                                                    ActivityDetector.MODE_RETURN_PRODUCT
                                            )
                                    );
                            return slangSession.success();
                        }
                        else {
                            return slangSession.failure();
                        }
                    }

                    @Override
                    public SlangSession.Status onEntityResolved(SlangEntity entity,
                                                                SlangSession session) {
                        switch (entity.getName()) {
                            case ActivityDetector.ENTITY_PRODUCT:
                            case ActivityDetector.ENTITY_COLOR:
                            case ActivityDetector.ENTITY_BRAND:
                                Log.d(TAG, "Entity Product for Return Intent");
                                returnProduct(entity.getParent(), true);
                                return session.success();
                            default:
                                return super.onEntityResolved(entity, session);
                        }
                    }
                });

        SlangApplication.getIntentDescriptor(ActivityDetector.INTENT_CANCEL_DEFAULT)
                .setResolutionAction(new DefaultResolvedIntentAction() {
                    @Override
                    public SlangSession.Status action(SlangResolvedIntent slangResolvedIntent,
                                                      SlangSession slangSession) {
                        Log.d(TAG, "Slang Triggering Default Cancel Intent");
                        return cancelDefault(slangSession, ActivityDetector.MODE_RETURN_DEFAULT);
                    }
                });

        SlangApplication.getIntentDescriptor(ActivityDetector.INTENT_CANCEL_PRODUCT)
                .setResolutionAction(new DefaultResolvedIntentAction() {

                    @Override
                    public SlangSession.Status onIntentResolutionBegin(SlangResolvedIntent intent,
                                                                       SlangSession session) {
                        orders.clear();
                        flag = true;
                        current = false;
                        return super.onIntentResolutionBegin(intent, session);
                    }

                    @Override
                    public SlangSession.Status action(SlangResolvedIntent slangResolvedIntent,
                                                      SlangSession slangSession) {
                        if(flag) {
                            resolveCurrent(slangResolvedIntent,
                                    ActivityDetector.MODE_CANCEL_PRODUCT);
                            Log.d(TAG, "Inside if flag Current is " + current);
                            if (!current)
                                cancelOrder(slangResolvedIntent, true);
                        }
                        flag = true;
                        Log.d(TAG, "Outside if flag Current is " + current);
                        if (!current) {
                            cancelOrder(slangResolvedIntent, false);
                        }
                        if (num > 0) {
                            slangResolvedIntent.getCompletionStatement()
                                    .overrideAffirmative(
                                            getCompletionPrompt(
                                                    SlangUserConfig.getLocale(),
                                                    ActivityDetector.MODE_CANCEL_PRODUCT
                                            )
                                    );
                            return slangSession.success();
                        }
                        else {
                            return slangSession.failure();
                        }
                    }

                    @Override
                    public SlangSession.Status onEntityResolved(SlangEntity entity,
                                                                SlangSession session) {
                        Log.d(TAG, "Entity Resolved for Cancel Intent");
                        switch (entity.getName()) {
                            case ActivityDetector.ENTITY_PRODUCT:
                            case ActivityDetector.ENTITY_COLOR:
                            case ActivityDetector.ENTITY_BRAND:
                                cancelOrder(entity.getParent(), true);
                                return session.success();
                            default:
                                return super.onEntityResolved(entity, session);
                        }
                    }
                });

        SlangApplication.getIntentDescriptor(ActivityDetector.INTENT_CONTACT_SUPPORT)
                .setResolutionAction(new DefaultResolvedIntentAction() {
                    @Override
                    public SlangSession.Status action(SlangResolvedIntent slangResolvedIntent,
                                                      SlangSession slangSession) {
                        appContext.startActivity(new Intent(appContext, FeedbackActivity.class));
                        return slangSession.success();
                    }
                });

        SlangApplication.getIntentDescriptor(ActivityDetector.INTENT_NO_VOICE)
                .setResolutionAction(new DefaultResolvedIntentAction() {
                    @Override
                    public SlangSession.Status action(SlangResolvedIntent slangResolvedIntent,
                                                      SlangSession slangSession) {
                        return slangSession.success();
                    }
                });
    }

    private static void trackReturn(SlangResolvedIntent slangResolvedIntent, boolean process) {
        Activity activity = SlangScreenContext.getInstance().getCurrentActivity();
        if(process) {
            orders.clear();
            num = 0;
            String productName = String.valueOf(slangResolvedIntent
                    .getEntity(ActivityDetector.ENTITY_PRODUCT).getValue());
            //Log.d(TAG, "Name is " + productName);
            String productColor = String.valueOf(slangResolvedIntent
                    .getEntity(ActivityDetector.ENTITY_COLOR).getValue());
            String productBrand = String.valueOf(slangResolvedIntent
                    .getEntity(ActivityDetector.ENTITY_BRAND).getValue());
            boolean colorBool = false;
            boolean brandBool = false;
            colorList = new ArrayList<>();
            colorNum = 0;
            brandList = new ArrayList<>();
            brandNum = 0;
            List<OrderEntry> list;
            if (!productColor.isEmpty())
                colorBool = true;
            if (!productBrand.isEmpty())
                brandBool = true;
            for (int i = 0; i < orderList.size(); i++) {
                list = orderList.get(i).items;
                List<OrderEntry> orderEntries = new ArrayList<>();
                boolean storeOut = false;
                for (int j = 0; j < list.size(); j++) {
                    OrderEntry entry = list.get(j);
                    String name = entry.title;
                    Log.d(TAG, "Title is " + name);
                    String color = entry.color;
                    String brand = entry.brand;
                    if (entry.returned && entry.delivered && !entry.pickup) {
                        boolean store = false;

                        if (name.toLowerCase().contains(productName.toLowerCase())) {
                            if (colorBool) {
                                if (productColor.equalsIgnoreCase(color)) {
                                    //If color is present from user input we compare and
                                    // only validate if color and name match
                                    if (brandBool) {
                                        if (productBrand.equalsIgnoreCase(brand)) {
                                            num++;
                                            store = true;
                                            storeOut = true;
                                        }
                                    } else {
                                        num++;
                                        store = true;
                                        storeOut = true;
                                    }
                                }
                            } else {
                                if (brandBool) {
                                    if (productBrand.equalsIgnoreCase(brand)) {
                                        num++;
                                        store = true;
                                        storeOut = true;
                                    }
                                } else {
                                    num++;
                                    store = true;
                                    storeOut = true;
                                }
                            }
                        }
                        if (store) {
                            orderEntries.add(entry);
                            Log.d(TAG, "Adding orderEntry to list of OrderEntries");
                        }
                    }
                }
                if (storeOut) {
                    orders.add(new OrderList(orderList.get(i).order_number,
                            orderList.get(i).order_date, orderEntries));
                    Log.d(TAG, "Adding list of OrderEntries to list of orderList");
                }
            }
        } else {
            if (num == 0) {
                switch (SlangUserConfig.getLocale().getLanguage()) {
                    case "en":
                    slangResolvedIntent.getCompletionStatement()
                            .overrideNegative("Sorry, no matching products found. Try returning " +
                                    "an order first.");
                    break;
                    case "hi":
                        slangResolvedIntent.getCompletionStatement()
                                .overrideNegative("क्षमा करें कोई मिलता हुआ आर्डर प्राप्त नहीं हुआ. कृपया " +
                                        "पहले ऑर्डर लौटने का प्रयास करें");
                        break;
                        default:
                            slangResolvedIntent.getCompletionStatement()
                                    .overrideNegative("");
                            break;
                }
            }
            else {
                Intent intent = new Intent(activity, OrderListActivity.class);
                intent.putExtra(ActivityDetector.ACTIVITY_MODE, ActivityDetector.MODE_TRACK_RETURN);
                Log.d(TAG, "Order size is " + orders.size());
                intent.putParcelableArrayListExtra(ActivityDetector.ORDER_LIST,
                        (ArrayList<OrderList>) orders);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                appContext.startActivity(intent);
            }
        }
    }

    private static SlangSession.Status trackDefault(SlangSession slangSession, String mode) {
        //final Activity activity = SlangScreenContext.getInstance().getCurrentActivity();
        Intent i = new Intent(appContext, OrderListActivity.class);
        i.putExtra(ActivityDetector.ACTIVITY_MODE, mode);
        i.putParcelableArrayListExtra(ActivityDetector.ORDER_LIST, (ArrayList<OrderList>) orderList);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        appContext.startActivity(i);
        return slangSession.success();
    }

    private static void trackProduct(final SlangResolvedIntent slangResolvedIntent,
                                     boolean process) {
        if (flag) {
            if (process) {
                flag = false;
                num = 0;
                boolean colorBool = false;
                boolean brandBool = false;
                //boolean cardinalBool = false;
                String productName = String.valueOf(slangResolvedIntent
                        .getEntity(ActivityDetector.ENTITY_PRODUCT).getValue());
                Log.d(TAG, "Name is " + productName);
                String productColor = String.valueOf(slangResolvedIntent
                        .getEntity(ActivityDetector.ENTITY_COLOR).getValue());
                String productBrand = String.valueOf(slangResolvedIntent
                        .getEntity(ActivityDetector.ENTITY_BRAND).getValue());
                String numberString = slangResolvedIntent
                        .getEntity(ActivityDetector.ENTITY_CARDINAL).getValue();
                int numberValues = -1;
                if (numberString != null) {
                    numberValues = Integer.valueOf(numberString);
                    cardinal = true;
                }
                colorList = new ArrayList<>();
                colorNum = 0;
                brandList = new ArrayList<>();
                brandNum = 0;
                List<OrderEntry> list;
                //int storeCounter = 0;

                //TODO if cardinalBool is true add top n items to the list and done
                if (cardinal) {
                    if (numberValues > orderList.size()) {
                        orders = orderList;
                        num = orderList.size();
                    }
                    else {
                        orders = new ArrayList<>(orderList.subList(0, numberValues));
                        num = numberValues;
                    }
                }
                else {
                    if (!productColor.isEmpty())
                        colorBool = true;
                    if (!productBrand.isEmpty())
                        brandBool = true;
                    for (int i = 0; i < orderList.size(); i++) {
                        list = orderList.get(i).items;
                        List<OrderEntry> orderEntries = new ArrayList<>();
                        boolean storeOut = false;
                        for (int j = 0; j < list.size(); j++) {
                            OrderEntry entry = list.get(j);
                            String name = entry.title;
                            String color = entry.color;
                            String brand = entry.brand;
                            boolean store = false;

                            if (name.toLowerCase().contains(productName.toLowerCase())) {
                                if (colorBool) {
                                    if (productColor.equalsIgnoreCase(color)) {
                                        if (brandBool) {
                                            if (productColor.equalsIgnoreCase(brand)) {
                                                num++;
                                                //storeCounter++;
                                                store = true;
                                                storeOut = true;
                                            }
                                        } else {
                                            // brand  not given by user
                                            if (brandList.size() > 0 && brandList.contains(brand))
                                                brandNum++;
                                            num++;
                                            //storeCounter++;
                                            store = true;
                                            storeOut = true;
                                            brandList.add(brand);
                                        }
                                    }
                                } else {
                                    if (brandBool) {
                                        if (productColor.equalsIgnoreCase(brand)) {
                                            //color not given by user
                                            if (colorList.size() > 0 && colorList.contains(color))
                                                colorNum++;
                                            num++;
                                            //storeCounter++;
                                            store = true;
                                            storeOut = true;
                                            colorList.add(color);
                                        }
                                    } else {
                                        //only name given by user
                                        if (colorList.size() > 0) {
                                            if (colorList.contains(color))
                                                colorNum++;
                                            if (brandList.contains(brand))
                                                brandNum++;
                                        }
                                        num++;
                                        //storeCounter++;
                                        store = true;
                                        storeOut = true;
                                        colorList.add(color);
                                        brandList.add(brand);
                                    }
                                }
                            }
                            if (store) {
                                orderEntries.add(entry);
                                Log.d(TAG, "Adding orderEntry to list of OrderEntries");
                            /*if (cardinalBool) {
                                if (storeCounter <= numberValues) {
                                    orderEntries.add(entry);
                                    Log.d(TAG, "Adding orderEntry to list of OrderEntries");
                                }
                                else
                                    break;
                            }
                            else {
                                orderEntries.add(entry);
                                Log.d(TAG, "Adding orderEntry to list of OrderEntries");
                            }*/
                            }
                        }
                        if (storeOut) {
                            orders.add(new OrderList(orderList.get(i).order_number,
                                    orderList.get(i).order_date, orderEntries));
                            Log.d(TAG, "Adding list of OrderEntries to list of orderList");
                        }
                    }
                }
                Log.d(TAG, "value of num is " + num);
            } else {
                if (num == 0) {
                    slangResolvedIntent.getCompletionStatement()
                            .overrideNegative(getNegativePrompt(SlangUserConfig.getLocale()));
                } else {
                    Intent intent = new Intent(appContext, OrderListActivity.class);
                    intent.putExtra(ActivityDetector.ACTIVITY_MODE,
                            ActivityDetector.MODE_TRACK_PRODUCT);
                    intent.putParcelableArrayListExtra(ActivityDetector.ORDER_LIST,
                            (ArrayList<OrderList>) orders);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    appContext.startActivity(intent);
                }
            }
        }
    }

    private static SlangSession.Status returnDefault(SlangSession slangSession, String mode) {
        orders = new ArrayList<>();
        int index = -1;
        for (int i = 0; i < orderList.size(); i++) {
            List<OrderEntry> list = orderList.get(i).items;
            List<OrderEntry> orderEntries = new ArrayList<>();
            for (int j = 0; j < list.size(); j++) {
                OrderEntry entry = list.get(j);
                if (entry.delivered && !entry.returned && !entry.cancelled) {
                    index = i;
                    orderEntries.add(entry);
                    break;
                }
            }
            if (index > 0) {
                orders.add(new OrderList(orderList.get(i).order_number,
                        orderList.get(i).order_date, orderEntries));
                break;
            }
        }
        Intent i = new Intent(appContext, OrderListActivity.class);
        i.putExtra(ActivityDetector.ACTIVITY_MODE, mode);
        i.putParcelableArrayListExtra(ActivityDetector.ORDER_LIST, (ArrayList<OrderList>) orders);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        appContext.startActivity(i);
        return slangSession.success();
    }

    private static void returnProduct(SlangResolvedIntent slangResolvedIntent, boolean process) {
        if (flag) {
            Activity activity = SlangScreenContext.getInstance().getCurrentActivity();
            if (process) {
                flag = false;
                num = 0;
                String productName = String.valueOf(slangResolvedIntent
                        .getEntity(ActivityDetector.ENTITY_PRODUCT).getValue());
                Log.d(TAG, "Name is " + productName);
                String productColor = String.valueOf(slangResolvedIntent
                        .getEntity(ActivityDetector.ENTITY_COLOR).getValue());
                String productBrand = String.valueOf(slangResolvedIntent
                        .getEntity(ActivityDetector.ENTITY_BRAND).getValue());
                boolean colorBool = false;
                boolean brandBool = false;
                colorList = new ArrayList<>();
                colorNum = 0;
                brandList = new ArrayList<>();
                brandNum = 0;
                List<OrderEntry> list;
                if (!productColor.isEmpty())
                    colorBool = true;
                if (!productBrand.isEmpty())
                    brandBool = true;
                for (int i = 0; i < orderList.size(); i++) {
                    list = orderList.get(i).items;
                    List<OrderEntry> orderEntries = new ArrayList<>();
                    boolean storeOut = false;
                    for (int j = 0; j < list.size(); j++) {
                        OrderEntry entry = list.get(j);
                        String name = entry.title;
                        Log.d(TAG, "Title is " + name);
                        String color = entry.color;
                        String brand = entry.brand;
                        if (!entry.returned && entry.delivered && !entry.cancelled) {
                            boolean store = false;

                            if (name.toLowerCase().contains(productName.toLowerCase())) {
                                if (colorBool) {
                                    if (productColor.equalsIgnoreCase(color)) {
                                        //If color is present from user input we compare and
                                        // only validate if color and name match
                                        if (brandBool) {
                                            if (productBrand.equalsIgnoreCase(brand)) {
                                                num++;
                                                store = true;
                                                storeOut = true;
                                            }
                                        } else {
                                            num++;
                                            store = true;
                                            storeOut = true;
                                        }
                                    }
                                } else {
                                    if (brandBool) {
                                        if (productBrand.equalsIgnoreCase(brand)) {
                                            num++;
                                            store = true;
                                            storeOut = true;
                                        }
                                    } else {
                                        num++;
                                        store = true;
                                        storeOut = true;
                                    }
                                }
                            }
                            if (store) {
                                orderEntries.add(entry);
                                Log.d(TAG, "Adding orderEntry to list of OrderEntries");
                            }
                        }
                    }
                    if (storeOut) {
                        orders.add(new OrderList(orderList.get(i).order_number,
                                orderList.get(i).order_date, orderEntries));
                        Log.d(TAG, "Adding list of OrderEntries to list of orderList");
                    }
                }
            } else {
                if (num == 0) {
                    slangResolvedIntent.getCompletionStatement()
                            .overrideNegative(getNegativePrompt(SlangUserConfig.getLocale()));
                } else {
                    Intent intent = new Intent(activity, OrderListActivity.class);
                    intent.putExtra(ActivityDetector.ACTIVITY_MODE,
                            ActivityDetector.MODE_RETURN_PRODUCT);
                    Log.d(TAG, "Order size is " + orders.size());
                    intent.putParcelableArrayListExtra(ActivityDetector.ORDER_LIST,
                            (ArrayList<OrderList>) orders);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    appContext.startActivity(intent);
                }
            }
        }
    }

    private static SlangSession.Status refundDefault(SlangResolvedIntent slangResolvedIntent,
                                                     SlangSession slangSession, String mode) {
        orders = new ArrayList<>();
        int index = -1;
        for (int i = 0; i < orderList.size(); i++) {
            List<OrderEntry> list = orderList.get(i).items;
            List<OrderEntry> orderEntries = new ArrayList<>();
            for (int j = 0; j < list.size(); j++) {
                OrderEntry entry = list.get(j);
                if (entry.delivered && entry.returned && entry.pickup) {
                    index = i;
                    orderEntries.add(entry);
                    Log.d(TAG, "Adding to refund default list " + entry.title);
                    break;
                }
            }
            if (index > 0) {
                orders.add(new OrderList(orderList.get(i).order_number,
                        orderList.get(i).order_date, orderEntries));
                break;
            }
        }
        Intent i = new Intent(appContext, OrderListActivity.class);
        i.putExtra(ActivityDetector.ACTIVITY_MODE, mode);
        i.putParcelableArrayListExtra(ActivityDetector.ORDER_LIST, (ArrayList<OrderList>) orders);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        appContext.startActivity(i);
        if (index > 0) {
            slangResolvedIntent.getCompletionStatement()
                    .overrideAffirmative(getCompletionPrompt(SlangUserConfig.getLocale(), mode));
            return slangSession.success();
        }
        else {
            slangResolvedIntent.getCompletionStatement()
                    .overrideNegative(getNegativePrompt(SlangUserConfig.getLocale()));
            return slangSession.failure();
        }
    }

    private static void refundProduct(SlangResolvedIntent slangResolvedIntent, boolean process) {
        if(flag) {
            if (process) {
                flag = false;
                orders.clear();
                num = 0;
                String productName = String.valueOf(slangResolvedIntent
                        .getEntity(ActivityDetector.ENTITY_PRODUCT).getValue());
                Log.d(TAG, "Name is " + productName);
                String productColor = String.valueOf(slangResolvedIntent
                        .getEntity(ActivityDetector.ENTITY_COLOR).getValue());
                String productBrand = String.valueOf(slangResolvedIntent
                        .getEntity(ActivityDetector.ENTITY_BRAND).getValue());
                boolean colorBool = false;
                boolean brandBool = false;
                colorList = new ArrayList<>();
                colorNum = 0;
                brandList = new ArrayList<>();
                brandNum = 0;
                List<OrderEntry> list;
                if (!productColor.isEmpty())
                    colorBool = true;
                if (!productBrand.isEmpty())
                    brandBool = true;
                for (int i = 0; i < orderList.size(); i++) {
                    list = orderList.get(i).items;
                    List<OrderEntry> orderEntries = new ArrayList<>();
                    boolean storeOut = false;
                    for (int j = 0; j < list.size(); j++) {
                        OrderEntry entry = list.get(j);
                        String name = entry.title;
                        String color = entry.color;
                        String brand = entry.brand;
                        if (entry.delivered && entry.returned && entry.pickup) {
                            boolean store = false;

                            if (name.toLowerCase().contains(productName.toLowerCase())) {
                                if (colorBool) {
                                    if (productColor.equalsIgnoreCase(color)) {
                                        //If color is present from user input we compare and
                                        // only validate if color and name match
                                        if (brandBool) {
                                            if (productBrand.equalsIgnoreCase(brand)) {
                                                num++;
                                                store = true;
                                                storeOut = true;
                                            }
                                        } else {
                                            num++;
                                            store = true;
                                            storeOut = true;
                                        }
                                    }
                                } else {
                                    if (brandBool) {
                                        if (productBrand.equalsIgnoreCase(brand)) {
                                            num++;
                                            store = true;
                                            storeOut = true;
                                        }
                                    } else {
                                        num++;
                                        store = true;
                                        storeOut = true;
                                    }
                                }
                            }
                            if (store) {
                                orderEntries.add(entry);
                                Log.d(TAG, "Adding orderEntry to list of OrderEntries");
                            }
                        }
                    }
                    if (storeOut) {
                        orders.add(new OrderList(orderList.get(i).order_number,
                                orderList.get(i).order_date, orderEntries));
                        Log.d(TAG, "Adding list of OrderEntries to list of orderList");
                    }
                }
            } else {
                if (num == 0) {
                    switch (SlangUserConfig.getLocale().getLanguage()) {
                        case "en":
                            slangResolvedIntent.getCompletionStatement()
                                    .overrideNegative("Sorry, no pending refunds matching your " +
                                            "criteria were found.");
                            break;
                        case "hi":
                            slangResolvedIntent.getCompletionStatement()
                                    .overrideNegative("क्षमा करें आपके विनिर्देशों से मेल खाने वाली कोई लंबित " +
                                            "धनवापसी नहीं मिली");
                    }
                } else {
                    Intent intent = new Intent(appContext, OrderListActivity.class);
                    intent.putExtra(ActivityDetector.ACTIVITY_MODE,
                            ActivityDetector.MODE_REFUND_PRODUCT);
                    intent.putParcelableArrayListExtra(ActivityDetector.ORDER_LIST,
                            (ArrayList<OrderList>) orders);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    appContext.startActivity(intent);
                }
            }
        }
    }

    private static SlangSession.Status cancelDefault(SlangSession slangSession, String mode) {
        orders = new ArrayList<>();
        int index = -1;
        for (int i = 0; i < orderList.size(); i++) {
            List<OrderEntry> list = orderList.get(i).items;
            List<OrderEntry> orderEntries = new ArrayList<>();
            for (int j = 0; j < list.size(); j++) {
                OrderEntry entry = list.get(j);
                if (!entry.returned && !entry.delivered && !entry.cancelled) {
                    index = i;
                    orderEntries.add(entry);
                    Log.d(TAG, "Adding to refund default list " + entry.title);
                    break;
                }
            }
            if (index > 0) {
                orders.add(new OrderList(orderList.get(i).order_number,
                        orderList.get(i).order_date, orderEntries));
                break;
            }
        }
        Intent i = new Intent(appContext, OrderListActivity.class);
        i.putExtra(ActivityDetector.ACTIVITY_MODE, mode);
        i.putParcelableArrayListExtra(ActivityDetector.ORDER_LIST, (ArrayList<OrderList>) orders);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        appContext.startActivity(i);
        return slangSession.success();
    }

    private static void cancelOrder(SlangResolvedIntent slangResolvedIntent, boolean process) {
        if (flag) {
            final Activity activity = SlangScreenContext.getInstance().getCurrentActivity();
            if (process) {
                flag = false;
                num = 0;
                String productName = String.valueOf(slangResolvedIntent
                        .getEntity(ActivityDetector.ENTITY_PRODUCT).getValue());
                Log.d(TAG, "Name is " + productName);
                String productColor = String.valueOf(slangResolvedIntent
                        .getEntity(ActivityDetector.ENTITY_COLOR).getValue());
                String productBrand = String.valueOf(slangResolvedIntent
                        .getEntity(ActivityDetector.ENTITY_BRAND).getValue());
                boolean colorBool = false;
                boolean brandBool = false;
                colorList = new ArrayList<>();
                colorNum = 0;
                brandList = new ArrayList<>();
                brandNum = 0;
                List<OrderEntry> list;
                if (!productColor.isEmpty())
                    colorBool = true;
                if (!productBrand.isEmpty())
                    brandBool = true;
                for (int i = 0; i < orderList.size(); i++) {
                    list = orderList.get(i).items;
                    List<OrderEntry> orderEntries = new ArrayList<>();
                    boolean storeOut = false;
                    for (int j = 0; j < list.size(); j++) {
                        OrderEntry entry = list.get(j);
                        String name = entry.title;
                        String color = entry.color;
                        String brand = entry.brand;
                        if (!entry.returned && !entry.delivered && !entry.cancelled) {
                            boolean store = false;

                            if (name.toLowerCase().contains(productName.toLowerCase())) {
                                if (colorBool) {
                                    if (productColor.equalsIgnoreCase(color)) {
                                        //If color is present from user input we compare and
                                        // only validate if color and name match
                                        if (brandBool) {
                                            if (productBrand.equalsIgnoreCase(brand)) {
                                                num++;
                                                store = true;
                                                storeOut = true;
                                            }
                                        } else {
                                            num++;
                                            store = true;
                                            storeOut = true;
                                        }
                                    }
                                } else {
                                    if (brandBool) {
                                        if (productBrand.equalsIgnoreCase(brand)) {
                                            num++;
                                            store = true;
                                            storeOut = true;
                                        }
                                    } else {
                                        num++;
                                        store = true;
                                        storeOut = true;
                                    }
                                }
                            }
                            if (store) {
                                orderEntries.add(entry);
                                Log.d(TAG, "Adding orderEntry to list of OrderEntries");
                                Log.d(TAG, "name of product is " + name);
                            }
                        }
                    }
                    if (storeOut) {
                        orders.add(new OrderList(orderList.get(i).order_number,
                                orderList.get(i).order_date, orderEntries));
                        Log.d(TAG, "Adding list of OrderEntries to list of orderList");
                    }
                }
            } else {
                if (num == 0) {
                    switch (SlangUserConfig.getLocale().getLanguage()) {
                        case "en":
                            slangResolvedIntent.getCompletionStatement()
                                    .overrideNegative("Sorry, no products eligible for " +
                                            "cancellation found");
                            break;
                        case "hi":
                            slangResolvedIntent.getCompletionStatement()
                                    .overrideNegative("क्षमा करें, रद्दीकरण के लिए योग्य कोई आर्डर नहीं मिला");

                    }
                } else {
                    Intent intent = new Intent(activity, OrderListActivity.class);
                    intent.putExtra(ActivityDetector.ACTIVITY_MODE,
                            ActivityDetector.MODE_CANCEL_PRODUCT);
                    intent.putParcelableArrayListExtra(ActivityDetector.ORDER_LIST,
                            (ArrayList<OrderList>) orders);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    appContext.startActivity(intent);
                }
            }
        }
    }

    private static void resolveCurrent(SlangResolvedIntent slangResolvedIntent, String mode) {
        final Activity activity = SlangScreenContext.getInstance().getCurrentActivity();
        if (activity instanceof OrderListActivity) {
            final Fragment currentFragment = ((OrderListActivity) activity)
                    .getSupportFragmentManager()
                    .findFragmentById(R.id.orderListContainer);
            if (currentFragment.getTag().equals(ActivityDetector.TAG_ORDER_ENTRY)) {
                Log.d(TAG, "MAY BE RESOLVED");

                final OrderEntryFragment orderEntryFragment = (OrderEntryFragment) currentFragment;
                final OrderEntry entry = orderEntryFragment.getOrderEntry();
                final String date = String.valueOf(new java.sql.Date(System.currentTimeMillis()));
                current = true;

                SharedPreferences sharedPreferences = activity.getSharedPreferences(
                        ActivityDetector.PREFERENCES, Context.MODE_PRIVATE
                );

                boolean process = false;
                boolean present = sharedPreferences.getBoolean(entry.title +
                        ActivityDetector.PREF_KEY_BOOL, false);
                if (mode.equals(ActivityDetector.MODE_CANCEL_PRODUCT)) {
                    process = (!entry.returned && !entry.delivered && !entry.cancelled) && !present;
                }
                else if (mode.equals(ActivityDetector.MODE_RETURN_PRODUCT)) {
                    process = !entry.returned && entry.delivered && !entry.cancelled && !present;
                }

                if (process) {
                    Log.d(TAG, "Current is true");
                    num = 1;
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.d(TAG, "Running on UI thread");
                            orderEntryFragment.setOnClickListener(entry, date);
                        }
                    });
                }
                else {
                    num = 0;
                    if (mode.equals(ActivityDetector.MODE_CANCEL_PRODUCT)) {
                        switch (SlangUserConfig.getLocale().getLanguage()) {
                            case "en":
                                slangResolvedIntent.getCompletionStatement()
                                        .overrideNegative("Sorry, this order cannot be cancelled.");
                                break;
                            case "hi":
                                slangResolvedIntent.getCompletionStatement()
                                        .overrideNegative("क्षमा करें, यह आर्डर रद्द नहीं किया जा सकता है.");
                                break;
                        }
                    }
                    else if (mode.equals(ActivityDetector.MODE_RETURN_PRODUCT)) {
                        switch (SlangUserConfig.getLocale().getLanguage()) {
                            case "en":
                                slangResolvedIntent.getCompletionStatement()
                                        .overrideNegative("Sorry, this order cannot be returned.");
                                break;
                            case "hi":
                                slangResolvedIntent.getCompletionStatement()
                                        .overrideNegative("क्षमा करें, यह आर्डर वापस नहीं किया जा सकता है.");
                                break;
                        }
                    }
                }
            }
            else if (currentFragment.getTag().equals(ActivityDetector.TAG_ORDER_ENTRY)) { ;
                Log.d(TAG, "CANNOT BE RESOLVED");
                current = false;
            }
        }
    }

    private static String getCompletionPrompt(Locale locale, String mode) {
        String language = locale.getLanguage();
        if (mode.equals(ActivityDetector.MODE_TRACK_PRODUCT)) {
            switch (language) {
                case "en":
                    if (num > 1)
                        if (cardinal)
                            return "Here are the orders you asked for.";
                        else
                            return "We could not find the exact order you requested. Please " +
                                    "select one from the list of orders shown.";
                    else
                        return "Here is the order you asked for.";
                case "hi":
                    if (num > 1)
                        if (cardinal)
                            return "यह आपके द्वारा मांगे गए आर्डर हैं";
                        else
                            return "हमें आपके द्वारा अनुरोधित सटीक आर्डर प्राप्त नहीं हुआा, कृपया विकल्पों में से एक " +
                                    "का चयन करें.";
                    else
                        return "यह रहा आपका आर्डर.";
            }
        }
        else if (mode.equals(ActivityDetector.MODE_REFUND_DEFAULT)) {
            switch (language) {
                case "en":
                    return "The refund status is " + orders.get(0).items.get(0).status + ".";
                case "hi":
                    return "आपका रिफंड स्टेटस यह है " + orders.get(0).items.get(0).status + ".";
            }
        }
        else if (mode.equals(ActivityDetector.MODE_REFUND_PRODUCT)) {
            switch (language) {
                case "en":
                    if (num > 1)
                        return "We could not find the exact order you requested. " +
                                "Please choose the order you want to check the refund status for.";
                    else
                        return "The refund status is " + orders.get(0).items.get(0).status;
                case "hi":
                    if (num > 1)
                        return "हमें आपके द्वारा अनुरोधित सटीक आर्डर प्राप्त नहीं हुआा, कृपया विकल्पों में से एक का " +
                                "चयन करें किसके लिए आप धनवापसी की स्थिति जांचना चाहते हैं.";
                    else
                        return "आपका रिफंड स्टेटस यह है " + orders.get(0).items.get(0).status + ".";
            }
        }
        else if (mode.equals(ActivityDetector.MODE_TRACK_RETURN)) {
            switch (language) {
                case "en":
                    if (num > 1)
                        return "We could not find the exact order you requested. " +
                                "Choose among the returns you wish to track.";
                    else
                        return "Your order will be pickup up on the specified pick-up date.";
                case "hi":
                    if (num > 1)
                        return "हमें आपके द्वारा अनुरोधित सटीक आर्डर प्राप्त नहीं हुआा, कृपया विकल्पों में से एक का " +
                                "चयन करें किस आर्डर वापसी के लिए आप स्थिति जांचना चाहते हैं.";
                    else
                        return "आपका ऑर्डर निर्दिष्ट पिक-अप तिथि पर उठाया जाएगा.";
            }
        }
        else if (mode.equals(ActivityDetector.MODE_RETURN_PRODUCT)) {
            switch (language) {
                case "en":
                    if (num > 1)
                        return "We could not find the exact order you requested. Choose among " +
                                "the products you wish to return.";
                    else
                        return "Please confirm you wish to return this order.";
                case "hi":
                    if (num > 1)
                        return "हमें आपके द्वारा अनुरोधित सटीक आर्डर प्राप्त नहीं हुआा, कृपया विकल्पों में से एक का " +
                                "चयन करें.";
                    else
                        return "कृपया पुष्टि करें कि आप इस आर्डर को वापस करना चाहते हैं";
            }
        }
        else if (mode.equals(ActivityDetector.MODE_CANCEL_PRODUCT)) {
            switch (language) {
                case "en":
                    if (num > 1)
                        return "We could not find the exact order you requested. Choose among" +
                                " the products you wish to cancel.";
                    else
                        return "Please confirm you wish to cancel this order.";
                case "hi":
                    if (num > 1)
                        return "हमें आपके द्वारा अनुरोधित सटीक आर्डर प्राप्त नहीं हुआा, कृपया विकल्पों में से एक का " +
                                "चयन करें.";
                    else
                        return "कृपया पुष्टि करें कि आप इस आर्डर को रद्द करना चाहते हैं";
            }
        }
        return "";
    }

    private static String getNegativePrompt(Locale locale) {
        switch (locale.getLanguage()) {
            case "en":
                return "You do not have any orders currently matching the criteria.";
            case "hi":
                return "क्षमा करें कोई मिलता हुआ आर्डर प्राप्त नहीं हुआ.";
        }
        return "";
    }
}