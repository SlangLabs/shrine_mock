package in.slanglabs.shrinemock.slang;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.util.Log;

import in.slanglabs.shrinemock.FeedbackActivity;
import in.slanglabs.shrinemock.OrderEntryFragment;
import in.slanglabs.shrinemock.OrderListActivity;
import com.example.mockapp.R;
import in.slanglabs.shrinemock.network.OrderEntry;
import in.slanglabs.shrinemock.network.OrderList;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import in.slanglabs.platform.SlangBuddy;
import in.slanglabs.platform.SlangBuddyOptions;
import in.slanglabs.platform.SlangEntity;
import in.slanglabs.platform.SlangIntent;
import in.slanglabs.platform.SlangLocale;
import in.slanglabs.platform.SlangSession;
import in.slanglabs.platform.action.SlangAction;
import in.slanglabs.platform.action.SlangMultiStepIntentAction;

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

    public static void init(final Application appContext, String buddyId, String authKey) {
        VoiceInterface.appContext = appContext;
        orderList = OrderList.initOrderList(appContext.getResources());
        orders = new ArrayList<>();

        SharedPreferences sharedPreferences = appContext
                .getSharedPreferences(ActivityDetector.PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        try {
            SlangBuddyOptions options = new SlangBuddyOptions.Builder()
                    .setContext(appContext)
                    .setBuddyId(buddyId)
                    .setAPIKey(authKey)
                    .setListener(new BuddyListener())
                    .setIntentAction(new V1Action())
                    .setRequestedLocales(SlangLocale.getSupportedLocales())
                    .setDefaultLocale(SlangLocale.LOCALE_ENGLISH_IN)
                    .build();
            SlangBuddy.initialize(options);
        } catch (SlangBuddyOptions.InvalidOptionException e) {
            e.printStackTrace();
        } catch (SlangBuddy.InsufficientPrivilegeException e) {
            e.printStackTrace();
        }
    }

    private static class V1Action implements SlangMultiStepIntentAction {

        @Override
        public void onIntentResolutionBegin(SlangIntent slangIntent, SlangSession slangSession) {
            switch(slangIntent.getName()) {
                case ActivityDetector.INTENT_TRACK_PRODUCT:
                    orders.clear();
                    flag = true;
                    break;
                case ActivityDetector.INTENT_TRACK_REFUND_PRODUCT:
                    flag = true;
                    break;
                case ActivityDetector.INTENT_RETURN_PRODUCT:
                    orders.clear();
                    flag = true;
                    current = false;
                    break;
                case ActivityDetector.INTENT_CANCEL_PRODUCT:
                    orders.clear();
                    flag = true;
                    current = false;
                    break;
            }
        }

        @Override
        public Status onEntityUnresolved(SlangEntity slangEntity, SlangSession slangSession) {
            return Status.SUCCESS;
        }

        @Override
        public Status onEntityResolved(SlangEntity slangEntity, SlangSession slangSession) {
            switch (slangEntity.getIntent().getName()) {
                case ActivityDetector.INTENT_TRACK_PRODUCT:
                    switch (slangEntity.getName()) {
                        case ActivityDetector.ENTITY_PRODUCT:
                        case ActivityDetector.ENTITY_COLOR:
                        case ActivityDetector.ENTITY_BRAND:
                        case ActivityDetector.ENTITY_CARDINAL:
                            Log.d(TAG, "Entity resolved");
                            if (slangEntity.getName().equals(ActivityDetector.ENTITY_CARDINAL))
                                Log.d(TAG, "Cardinal value is " + slangEntity.getValue());
                            trackProduct(slangEntity.getIntent(), slangSession, true);
                    }
                    return Status.SUCCESS;
                case ActivityDetector.INTENT_TRACK_REFUND_PRODUCT:
                    switch (slangEntity.getName()) {
                        case ActivityDetector.ENTITY_PRODUCT:
                        case ActivityDetector.ENTITY_COLOR:
                        case ActivityDetector.ENTITY_BRAND:
                            Log.d(TAG, "Entity Resolve for Refund Intent");
                            refundProduct(slangEntity.getIntent(), slangSession, true);
                            return Status.SUCCESS;
                    }
                case ActivityDetector.INTENT_RETURN_PRODUCT:
                    switch (slangEntity.getName()) {
                        case ActivityDetector.ENTITY_PRODUCT:
                        case ActivityDetector.ENTITY_COLOR:
                        case ActivityDetector.ENTITY_BRAND:
                        case ActivityDetector.ENTITY_DATE:
                            Log.d(TAG, "Entity Product for Return Intent");
                            returnProduct(slangEntity.getIntent(), slangSession, true);
                            return Status.SUCCESS;
                    }
                case ActivityDetector.INTENT_CANCEL_PRODUCT:
                    Log.d(TAG, "Entity Resolved for Cancel Intent");
                    switch (slangEntity.getName()) {
                        case ActivityDetector.ENTITY_PRODUCT:
                        case ActivityDetector.ENTITY_COLOR:
                        case ActivityDetector.ENTITY_BRAND:
                            cancelOrder(slangEntity.getIntent(), slangSession, true);
                            return Status.SUCCESS;
                    }
            }
            return null;
        }

        @Override
        public void onIntentResolutionEnd(SlangIntent slangIntent, SlangSession slangSession) {
        }

        @Override
        public Status action(SlangIntent slangIntent, SlangSession slangSession) {
            switch (slangIntent.getName()) {
                case ActivityDetector.INTENT_TRACK_DEFAULT:
                    Log.d(TAG, "Slang Triggering Default Track Intent");
                    return trackDefault(ActivityDetector.MODE_TRACK_DEFAULT);
                case ActivityDetector.INTENT_TRACK_PRODUCT:
                    Log.d(TAG, "Slang Triggering Product Track Intent");
                    flag = true;
                    trackProduct(slangIntent, slangSession, false);
                    if (num > 0) {
                        slangIntent.getCompletionStatement()
                                .overrideAffirmative(getCompletionPrompt(
                                        slangSession.getCurrentLocale(),
                                        ActivityDetector.MODE_TRACK_PRODUCT));
                        return Status.SUCCESS;
                    } else {
                        return Status.FAILURE;
                    }
                case ActivityDetector.INTENT_TRACK_ALL:
                    Log.d(TAG, "Slang Triggering All Track Intent");
                    return trackDefault(ActivityDetector.MODE_TRACK_ALL);
                case ActivityDetector.INTENT_TRACK_REFUND_DEFAULT:
                    Log.d(TAG, "Slang Triggering Default Refund Intent");
                    return refundDefault(slangIntent, slangSession,
                            ActivityDetector.MODE_REFUND_DEFAULT);
                case ActivityDetector.INTENT_TRACK_REFUND_PRODUCT:
                    Log.d(TAG, "Slang Triggering Product Refund Intent");
                    flag = true;
                    refundProduct(slangIntent, slangSession, false);
                    if (num > 0) {
                        slangIntent.getCompletionStatement()
                                .overrideAffirmative(getCompletionPrompt(
                                        slangSession.getCurrentLocale(),
                                        ActivityDetector.MODE_REFUND_PRODUCT)
                                );
                        return Status.SUCCESS;
                    }
                    else {
                        return Status.FAILURE;
                    }
                case ActivityDetector.INTENT_TRACK_RETURN:
                    trackReturn(slangIntent, slangSession, true);
                    trackReturn(slangIntent, slangSession, false);
                    Log.d(TAG, "Slang Triggering Track Return Intent");
                    if (num > 0) {
                        slangIntent.getCompletionStatement()
                                .overrideAffirmative(
                                        getCompletionPrompt(
                                                slangSession.getCurrentLocale(),
                                                ActivityDetector.MODE_TRACK_RETURN
                                        )
                                );
                        return Status.SUCCESS;
                    }
                    else {
                        return Status.FAILURE;
                    }
                case ActivityDetector.INTENT_RETURN_DEFAULT:
                    Log.d(TAG, "Slang Triggering Default Return Intent");
                    return returnDefault(ActivityDetector.MODE_RETURN_DEFAULT);
                case ActivityDetector.INTENT_RETURN_PRODUCT:
                    if(flag) {
                        resolveCurrent(slangIntent, slangSession,
                                ActivityDetector.MODE_RETURN_PRODUCT);
                        if (!current)
                            returnProduct(slangIntent, slangSession, true);
                    }
                    flag = true;
                    if (!current) {
                        returnProduct(slangIntent, slangSession, false);
                    }
                    if (num > 0) {
                        slangIntent.getCompletionStatement()
                                .overrideAffirmative(
                                        getCompletionPrompt(
                                                slangSession.getCurrentLocale(),
                                                ActivityDetector.MODE_RETURN_PRODUCT
                                        )
                                );
                        return Status.SUCCESS;
                    }
                    else {
                        return Status.FAILURE;
                    }
                case ActivityDetector.INTENT_CANCEL_DEFAULT:
                    Log.d(TAG, "Slang Triggering Default Cancel Intent");
                    return cancelDefault(ActivityDetector.MODE_RETURN_DEFAULT);
                case ActivityDetector.INTENT_CANCEL_PRODUCT:
                    if(flag) {
                        resolveCurrent(slangIntent, slangSession,
                                ActivityDetector.MODE_CANCEL_PRODUCT);
                        Log.d(TAG, "Inside if flag Current is " + current);
                        if (!current)
                            cancelOrder(slangIntent, slangSession, true);
                    }
                    flag = true;
                    Log.d(TAG, "Outside if flag Current is " + current);
                    if (!current) {
                        cancelOrder(slangIntent, slangSession, false);
                    }
                    if (num > 0) {
                        slangIntent.getCompletionStatement()
                                .overrideAffirmative(
                                        getCompletionPrompt(
                                                slangSession.getCurrentLocale(),
                                                ActivityDetector.MODE_CANCEL_PRODUCT
                                        )
                                );
                        return Status.SUCCESS;
                    }
                    else {
                        return Status.FAILURE;
                    }
                case ActivityDetector.INTENT_CONTACT_SUPPORT:
                    appContext.startActivity(new Intent(appContext, FeedbackActivity.class));
                    return Status.SUCCESS;
                case ActivityDetector.INTENT_NO_VOICE:
                    return Status.SUCCESS;
            }
            return null;
        }
    }

    private static void trackReturn(SlangIntent slangResolvedIntent, SlangSession slangSession, boolean process) {
        if(process) {
            orders.clear();
            num = 0;
            String productName = String.valueOf(slangResolvedIntent
                    .getEntity(ActivityDetector.ENTITY_PRODUCT).getValue());
            String productColor = String.valueOf(slangResolvedIntent
                    .getEntity(ActivityDetector.ENTITY_COLOR).getValue());
            String productBrand = String.valueOf(slangResolvedIntent
                    .getEntity(ActivityDetector.ENTITY_BRAND).getValue());
            String dateString = slangResolvedIntent
                    .getEntity(ActivityDetector.ENTITY_DATE).getValue();
            boolean colorBool = false;
            boolean brandBool = false;
            boolean dateBool = false;
            colorList = new ArrayList<>();
            colorNum = 0;
            brandList = new ArrayList<>();
            brandNum = 0;
            List<OrderEntry> list;
            if (!productColor.isEmpty())
                colorBool = true;
            if (!productBrand.isEmpty())
                brandBool = true;
            if (!dateString.isEmpty())
                dateBool = true;
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

                        if(dateBool) {
                            if (!dateString.equalsIgnoreCase(entry.return_date))
                                break;
                        }

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
                switch (slangSession.getCurrentLocale().getLanguage()) {
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
                Intent intent = new Intent(appContext, OrderListActivity.class);
                intent.putExtra(ActivityDetector.ACTIVITY_MODE, ActivityDetector.MODE_TRACK_RETURN);
                Log.d(TAG, "Order size is " + orders.size());
                intent.putParcelableArrayListExtra(ActivityDetector.ORDER_LIST,
                        (ArrayList<OrderList>) orders);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                appContext.startActivity(intent);
            }
        }
    }

    private static SlangAction.Status trackDefault(String mode) {
        Intent i = new Intent(appContext, OrderListActivity.class);
        i.putExtra(ActivityDetector.ACTIVITY_MODE, mode);
        i.putParcelableArrayListExtra(ActivityDetector.ORDER_LIST, (ArrayList<OrderList>) orderList);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        appContext.startActivity(i);
        return SlangAction.Status.SUCCESS;
    }

    private static void trackProduct(final SlangIntent slangResolvedIntent,
                                     SlangSession slangSession,
                                     boolean process) {
        if (flag) {
            if (process) {
                flag = false;
                num = 0;
                boolean colorBool = false;
                boolean brandBool = false;
                boolean dateBool = false;
                //boolean cardinalBool = false;
                String productName = String.valueOf(slangResolvedIntent
                        .getEntity(ActivityDetector.ENTITY_PRODUCT).getValue());
                Log.d(TAG, "Name is " + productName);
                String productColor = String.valueOf(slangResolvedIntent
                        .getEntity(ActivityDetector.ENTITY_COLOR).getValue());
                Log.d(TAG, "Color is " + productColor);
                String productBrand = String.valueOf(slangResolvedIntent
                        .getEntity(ActivityDetector.ENTITY_BRAND).getValue());
                Log.d(TAG, "Brand is " + productBrand);
                String numberString = slangResolvedIntent
                        .getEntity(ActivityDetector.ENTITY_CARDINAL).getValue();
                Log.d(TAG, "Cardinal is " + numberString);
                String dateString = slangResolvedIntent
                        .getEntity(ActivityDetector.ENTITY_DATE).getValue();
                Log.d(TAG, "Date is " + dateString);
                int numberValues = -1;
                if (!numberString.isEmpty()) {
                    numberValues = Integer.valueOf(numberString);
                    cardinal = true;
                }
                colorList = new ArrayList<>();
                colorNum = 0;
                brandList = new ArrayList<>();
                brandNum = 0;
                List<OrderEntry> list;
                //int storeCounter = 0;

                if (cardinal) {
                    Log.d(TAG, "Cardinal is true");
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
                    Log.d(TAG, "Cardinal is false");
                    if (!productColor.isEmpty())
                        colorBool = true;
                    if (!productBrand.isEmpty())
                        brandBool = true;
                    if (!dateString.isEmpty())
                        dateBool = true;
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

                            if(dateBool) {
                                Log.d(TAG, "datebool is true");
                                if (!dateString.equalsIgnoreCase(orderList.get(i).order_date))
                                    break;
                            }

                            if (name.toLowerCase().contains(productName.toLowerCase())) {
                                Log.d(TAG, "Name matches");
                                if (colorBool) {
                                    if (productColor.equalsIgnoreCase(color)) {
                                        Log.d(TAG, "Color matches");
                                        if (brandBool) {
                                            if (productBrand.equalsIgnoreCase(brand)) {
                                                Log.d(TAG, "Brand matches");
                                                num++;
                                                //storeCounter++;
                                                store = true;
                                                storeOut = true;
                                            }
                                        } else {
                                            // brand  not given by user
                                            Log.d(TAG, "Brand not given");
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
                                    Log.d(TAG, "Color not given");
                                    if (brandBool) {
                                        if (productBrand.equalsIgnoreCase(brand)) {
                                            //color not given by user
                                            Log.d(TAG, "Brand matches");
                                            if (colorList.size() > 0 && colorList.contains(color))
                                                colorNum++;
                                            num++;
                                            //storeCounter++;
                                            store = true;
                                            storeOut = true;
                                            colorList.add(color);
                                        }
                                    } else {
                                        Log.d(TAG, "Only name given");
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
                            .overrideNegative(getNegativePrompt(slangSession.getCurrentLocale()));
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

    private static SlangAction.Status returnDefault(String mode) {
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
        return SlangAction.Status.SUCCESS;
    }

    private static void returnProduct(SlangIntent slangResolvedIntent,
                                      SlangSession slangSession,
                                      boolean process) {
        if (flag) {
            //Activity activity = SlangScreenContext.getInstance().getCurrentActivity();
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
                String dateString = slangResolvedIntent
                        .getEntity(ActivityDetector.ENTITY_DATE).getValue();
                boolean colorBool = false;
                boolean brandBool = false;
                boolean dateBool = false;
                colorList = new ArrayList<>();
                colorNum = 0;
                brandList = new ArrayList<>();
                brandNum = 0;
                List<OrderEntry> list;
                if (!productColor.isEmpty())
                    colorBool = true;
                if (!productBrand.isEmpty())
                    brandBool = true;
                if (!dateString.isEmpty())
                    dateBool = true;
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
                        if(dateBool) {
                            if (!dateString.equalsIgnoreCase(orderList.get(i).order_date))
                                break;
                        }
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
                            .overrideNegative(getNegativePrompt(slangSession.getCurrentLocale()));
                } else {
                    Intent intent = new Intent(appContext, OrderListActivity.class);
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

    private static SlangAction.Status refundDefault(SlangIntent slangResolvedIntent,
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
                    .overrideAffirmative(getCompletionPrompt(slangSession.getCurrentLocale(), mode));
            return SlangAction.Status.SUCCESS;
        }
        else {
            slangResolvedIntent.getCompletionStatement()
                    .overrideNegative(getNegativePrompt(slangSession.getCurrentLocale()));
            return SlangAction.Status.FAILURE;
        }
    }

    private static void refundProduct(SlangIntent slangResolvedIntent, SlangSession slangSession, boolean process) {
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
                String dateString = slangResolvedIntent
                        .getEntity(ActivityDetector.ENTITY_DATE).getValue();
                boolean colorBool = false;
                boolean brandBool = false;
                boolean dateBool = false;
                colorList = new ArrayList<>();
                colorNum = 0;
                brandList = new ArrayList<>();
                brandNum = 0;
                List<OrderEntry> list;
                if (!productColor.isEmpty())
                    colorBool = true;
                if (!productBrand.isEmpty())
                    brandBool = true;
                if (!dateString.isEmpty())
                    dateBool = true;
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

                            if(dateBool) {
                                if (!(dateString.equalsIgnoreCase(entry.return_date)))
                                    break;
                            }

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
                    switch (slangSession.getCurrentLocale().getLanguage()) {
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

    private static SlangAction.Status cancelDefault(String mode) {
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
        return SlangAction.Status.SUCCESS;
    }

    private static void cancelOrder(SlangIntent slangResolvedIntent, SlangSession slangSession, boolean process) {
        if (flag) {
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
                String dateString = slangResolvedIntent
                        .getEntity(ActivityDetector.ENTITY_DATE).getValue();
                boolean colorBool = false;
                boolean brandBool = false;
                boolean dateBool = false;
                colorList = new ArrayList<>();
                colorNum = 0;
                brandList = new ArrayList<>();
                brandNum = 0;
                List<OrderEntry> list;
                if (!productColor.isEmpty())
                    colorBool = true;
                if (!productBrand.isEmpty())
                    brandBool = true;
                if (!dateString.isEmpty())
                    dateBool = true;
                for (int i = 0; i < orderList.size(); i++) {
                    list = orderList.get(i).items;
                    List<OrderEntry> orderEntries = new ArrayList<>();
                    boolean storeOut = false;
                    for (int j = 0; j < list.size(); j++) {
                        OrderEntry entry = list.get(j);
                        String name = entry.title;
                        String color = entry.color;
                        String brand = entry.brand;
                        if(dateBool) {
                            if (!dateString.equalsIgnoreCase(orderList.get(i).order_date))
                                break;
                        }
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
                    switch (slangSession.getCurrentLocale().getLanguage()) {
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
                    Intent intent = new Intent(appContext, OrderListActivity.class);
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

    private static void resolveCurrent(SlangIntent slangResolvedIntent, SlangSession slangSession, String mode) {
        final Activity activity = slangSession.getCurrentActivity();
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
                        switch (slangSession.getCurrentLocale().getLanguage()) {
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
                        switch (slangSession.getCurrentLocale().getLanguage()) {
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
            else if (!currentFragment.getTag().equals(ActivityDetector.TAG_ORDER_ENTRY)) { ;
                Log.d(TAG, "CANNOT BE RESOLVED");
                current = false;
            }
        }
    }

    private static String getCompletionPrompt(Locale locale, String mode) {
        String language = locale.getLanguage();
        switch (mode) {
            case ActivityDetector.MODE_TRACK_PRODUCT:
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
                break;
            case ActivityDetector.MODE_REFUND_DEFAULT:
                switch (language) {
                    case "en":
                        return "The refund status is " + orders.get(0).items.get(0).status + ".";
                    case "hi":
                        return "आपका रिफंड स्टेटस यह है " + orders.get(0).items.get(0).status + ".";
                }
                break;
            case ActivityDetector.MODE_REFUND_PRODUCT:
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
                break;
            case ActivityDetector.MODE_TRACK_RETURN:
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
                break;
            case ActivityDetector.MODE_RETURN_PRODUCT:
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
                break;
            case ActivityDetector.MODE_CANCEL_PRODUCT:
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
                break;
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

    private static class BuddyListener implements SlangBuddy.Listener {
        @Override
        public void onInitialized() {}

        @Override
        public void onInitializationFailed(SlangBuddy.InitializationError e) {}

        @Override
        public void onLocaleChanged(Locale locale) {

        }

        @Override
        public void onLocaleChangeFailed(Locale locale, SlangBuddy.LocaleChangeError localeChangeError) {

        }
    }
}