package com.example.mockapp.slang;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;


import com.example.mockapp.OrderListActivity;
import com.example.mockapp.network.OrderEntry;
import com.example.mockapp.network.OrderList;

import java.util.ArrayList;
import java.util.List;

import in.slanglabs.platform.application.ISlangApplicationStateListener;
import in.slanglabs.platform.application.SlangApplication;
import in.slanglabs.platform.application.SlangApplicationUninitializedException;
import in.slanglabs.platform.application.actions.DefaultResolvedIntentAction;
import in.slanglabs.platform.session.SlangEntity;
import in.slanglabs.platform.session.SlangResolvedIntent;
import in.slanglabs.platform.session.SlangSession;
import in.slanglabs.platform.ui.SlangScreenContext;

public class VoiceInterface {

    private static Application appContext;
    private static final String TAG = VoiceInterface.class.getSimpleName();
    private static List<OrderList> orderList;
    private static List<OrderList> orders;
    private static List<String> colorList, brandList;
    private static int num, brandNum, colorNum;

    //TODO DO A GIT PUSH FOR NEW BRANCH

    public static void init(final Application appContext, String appId, String authKey) {
        VoiceInterface.appContext = appContext;
        orderList = OrderList.initOrderList(appContext.getResources());
        orders = new ArrayList<>();
        SlangApplication.initialize(appContext, appId, authKey, new ISlangApplicationStateListener() {
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
                Toast.makeText(appContext, "Not able to initialize", Toast.LENGTH_LONG).show();
                Log.d(TAG, "Failure Reason: " + failureReason);
            }
        });

    }

    private static void registerActions() throws SlangApplicationUninitializedException {
        SlangApplication.getIntentDescriptor(ActivityDetector.INTENT_TRACK_DEFAULT)
                .setResolutionAction(new DefaultResolvedIntentAction() {
                    @Override
                    public SlangSession.Status action(SlangResolvedIntent slangResolvedIntent, SlangSession slangSession) {
                        Log.d(TAG, "Slang Triggering Default Track Intent");
                        return trackDefault(slangSession, ActivityDetector.MODE_TRACK_DEFAULT);
                    }
                });
        SlangApplication.getIntentDescriptor(ActivityDetector.INTENT_TRACK_PRODUCT)
                .setResolutionAction(new DefaultResolvedIntentAction() {
                    @Override
                    public SlangSession.Status onIntentResolutionBegin(SlangResolvedIntent intent, SlangSession session) {
                        orders.clear();
                        return super.onIntentResolutionBegin(intent, session);
                    }

                    @Override
                    public SlangSession.Status onEntityResolved(SlangEntity entity, SlangSession session) {
                        Log.d(TAG, "Calling onEntityResolved");
                        switch (entity.getName()) {
                            case ActivityDetector.ENTITY_PRODUCT:
                            case ActivityDetector.ENTITY_COLOR:
                            case ActivityDetector.ENTITY_BRAND:
                                Log.d(TAG, "Entity Product");
                                trackProduct(entity.getParent(), true);
                                return session.success();
                            default:
                                return super.onEntityResolved(entity, session);
                        }
                    }

                    @Override
                    public SlangSession.Status action(SlangResolvedIntent slangResolvedIntent, SlangSession slangSession) {
                        Log.d(TAG, "Slang Triggering Product Track Intent");
                        trackProduct(slangResolvedIntent, false);
                        if (num > 1) {
                            slangResolvedIntent.getCompletionStatement()
                                    .overrideAffirmative("Here are the products you asked for.");
                            return slangSession.success();
                        }
                        else if (num == 1) {
                            slangResolvedIntent.getCompletionStatement()
                                    .overrideAffirmative("Here is the order you asked for.");
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
                    public SlangSession.Status action(SlangResolvedIntent slangResolvedIntent, SlangSession slangSession) {
                        Log.d(TAG, "Slang Triggering All Track Intent");
                        return trackDefault(slangSession, ActivityDetector.MODE_TRACK_ALL);
                    }
                });

        SlangApplication.getIntentDescriptor(ActivityDetector.INTENT_TRACK_REFUND_DEFAULT)
                .setResolutionAction(new DefaultResolvedIntentAction() {
                    @Override
                    public SlangSession.Status action(SlangResolvedIntent slangResolvedIntent, SlangSession slangSession) {
                        Log.d(TAG, "Slang Triggering Default Refund Intent");
                        return refundDefault(slangResolvedIntent, slangSession, ActivityDetector.MODE_REFUND_DEFAULT);
                    }
                });
        SlangApplication.getIntentDescriptor(ActivityDetector.INTENT_TRACK_REFUND_PRODUCT)
                .setResolutionAction(new DefaultResolvedIntentAction() {
                    @Override
                    public SlangSession.Status action(SlangResolvedIntent slangResolvedIntent, SlangSession slangSession) {
                        refundProduct(slangResolvedIntent, false);
                        if (num > 1) {
                            slangResolvedIntent.getCompletionStatement()
                                    .overrideAffirmative("Please choose the product you want to " +
                                            "check the refund status for.");
                            return slangSession.success();
                        }
                        else if (num == 1){
                            slangResolvedIntent.getCompletionStatement()
                                    .overrideAffirmative("The refund status is "
                                            + orders.get(0).items.get(0).status);
                            return slangSession.success();
                        }
                        else {
                            return slangSession.failure();
                        }
                    }

                    @Override
                    public SlangSession.Status onEntityResolved(SlangEntity entity, SlangSession session) {
                        switch (entity.getName()) {
                            case ActivityDetector.ENTITY_PRODUCT:
                            case ActivityDetector.ENTITY_COLOR:
                            case ActivityDetector.ENTITY_BRAND:
                                Log.d(TAG, "Entity Product for Refund Intent");
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
                    public SlangSession.Status action(SlangResolvedIntent slangResolvedIntent, SlangSession slangSession) {
                        trackReturn(slangResolvedIntent, true);
                        trackReturn(slangResolvedIntent, false);
                        Log.d(TAG, "Slang Triggering Track Return Intent");
                        if (num > 1) {
                            slangResolvedIntent.getCompletionStatement()
                                    .overrideAffirmative("Choose among the returns you wish to track.");
                            return slangSession.success();
                        }
                        else if (num == 1) {
                            slangResolvedIntent.getCompletionStatement()
                                    .overrideAffirmative("Your order will be pickup up on the specified pick-up date.");
                            return slangSession.success();
                        }
                        else {
                            return slangSession.failure();
                        }
                    }

                    /*@Override
                    public SlangSession.Status onEntityResolved(SlangEntity entity, SlangSession session) {
                        switch (entity.getName()) {
                            case ActivityDetector.ENTITY_PRODUCT:
                            case ActivityDetector.ENTITY_COLOR:
                            case ActivityDetector.ENTITY_BRAND:
                                Log.d(TAG, "Entity Product for Refund Intent");
                                refundProduct(entity.getParent(), true);
                                return session.success();
                            default:
                                return super.onEntityResolved(entity, session);
                        }
                    }*/
                });
        SlangApplication.getIntentDescriptor(ActivityDetector.INTENT_RETURN_DEFAULT)
                .setResolutionAction(new DefaultResolvedIntentAction() {
                    @Override
                    public SlangSession.Status action(SlangResolvedIntent slangResolvedIntent, SlangSession slangSession) {
                        Log.d(TAG, "Slang Triggering Default Return Intent");
                        return returnDefault(slangSession, ActivityDetector.MODE_RETURN_DEFAULT);
                    }
                });
        SlangApplication.getIntentDescriptor(ActivityDetector.INTENT_RETURN_PRODUCT)
                .setResolutionAction(new DefaultResolvedIntentAction() {
                    @Override
                    public SlangSession.Status action(SlangResolvedIntent slangResolvedIntent, SlangSession slangSession) {
                        returnProduct(slangResolvedIntent, false);
                        if (num > 1) {
                            slangResolvedIntent.getCompletionStatement()
                                    .overrideAffirmative("Choose among the products you wish to return.");
                            return slangSession.success();
                        }
                        else if (num == 1) {
                            slangResolvedIntent.getCompletionStatement()
                                    .overrideAffirmative("Please confirm if you wish to return this product.");
                            return slangSession.success();
                        }
                        else {
                            return slangSession.failure();
                        }
                    }

                    @Override
                    public SlangSession.Status onEntityResolved(SlangEntity entity, SlangSession session) {
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
                    public SlangSession.Status action(SlangResolvedIntent slangResolvedIntent, SlangSession slangSession) {
                        Log.d(TAG, "Slang Triggering Default Cancel Intent");
                        return cancelDefault(slangSession, ActivityDetector.MODE_RETURN_DEFAULT);
                    }
                });
        SlangApplication.getIntentDescriptor(ActivityDetector.INTENT_CANCEL_PRODUCT)
                .setResolutionAction(new DefaultResolvedIntentAction() {
                    @Override
                    public SlangSession.Status action(SlangResolvedIntent slangResolvedIntent, SlangSession slangSession) {
                        cancelOrder(slangResolvedIntent, false);
                        if (num > 1) {
                            slangResolvedIntent.getCompletionStatement()
                                    .overrideAffirmative("Choose among the products you wish to cancel.");
                            return slangSession.success();
                        }
                        else if (num == 1){
                            slangResolvedIntent.getCompletionStatement()
                                    .overrideAffirmative("Please confirm if you wish to cancel this product.");
                            return slangSession.success();
                        }
                        else {
                            return slangSession.failure();
                        }
                    }
                    @Override
                    public SlangSession.Status onEntityResolved(SlangEntity entity, SlangSession session) {
                        switch (entity.getName()) {
                            case ActivityDetector.ENTITY_PRODUCT:
                            case ActivityDetector.ENTITY_COLOR:
                            case ActivityDetector.ENTITY_BRAND:
                                Log.d(TAG, "Entity Product for Cancel Intent");
                                cancelOrder(entity.getParent(), true);
                                return session.success();
                            default:
                                return super.onEntityResolved(entity, session);
                        }
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
                    if (entry.returned && entry.delivered && !entry.pickup) {
                        /*Log.d(TAG, "For product name " + name);
                        Log.d(TAG, "Returned is " + String.valueOf(entry.returned) +
                                " Delivered is " + String.valueOf(entry.delivered) + " Pickup is " + String.valueOf(entry.pickup));*/
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
                        .overrideNegative("Sorry, no matching products found. Try cancelling an order instead.");
            }
            else {
                Intent intent = new Intent(activity, OrderListActivity.class);
                intent.putExtra(ActivityDetector.ACTIVITY_MODE, ActivityDetector.MODE_TRACK_RETURN);
                Log.d(TAG, "Order size is " + orders.size());
                intent.putParcelableArrayListExtra(ActivityDetector.ORDER_LIST, (ArrayList<OrderList>) orders);
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

    private static void trackProduct(final SlangResolvedIntent slangResolvedIntent, boolean process) {

        if (process) {
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
                    boolean store = false;

                    if (name.toLowerCase().contains(productName.toLowerCase())) {
                        if (colorBool) {
                            if (productColor.equalsIgnoreCase(color)) {
                                if (brandBool) {
                                    if (productColor.equalsIgnoreCase(brand)) {
                                        num++;
                                        store = true;
                                        storeOut = true;
                                    }
                                } else {
                                    // brand  not given by user
                                    if (brandList.size() > 0 && brandList.contains(brand))
                                        brandNum++;
                                    num++;
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
            Log.d(TAG, "value of num is " + num);
        } else {
            if (num == 0) {
                slangResolvedIntent.getCompletionStatement()
                        .overrideNegative("Sorry, no matching products found");
            }
            else {
                Intent intent = new Intent(appContext, OrderListActivity.class);
                intent.putExtra(ActivityDetector.ACTIVITY_MODE, ActivityDetector.MODE_TRACK_PRODUCT);
                intent.putParcelableArrayListExtra(ActivityDetector.ORDER_LIST, (ArrayList<OrderList>) orders);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                appContext.startActivity(intent);
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
        Activity activity = SlangScreenContext.getInstance().getCurrentActivity();
        if(process) {
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
                        .overrideNegative("Sorry, no matching products found. Try cancelling an order instead.");
            }
            else {
                Intent intent = new Intent(activity, OrderListActivity.class);
                intent.putExtra(ActivityDetector.ACTIVITY_MODE, ActivityDetector.MODE_RETURN_PRODUCT);
                Log.d(TAG, "Order size is " + orders.size());
                intent.putParcelableArrayListExtra(ActivityDetector.ORDER_LIST, (ArrayList<OrderList>) orders);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                appContext.startActivity(intent);
            }
        }
    }

    private static SlangSession.Status refundDefault(SlangResolvedIntent slangResolvedIntent, SlangSession slangSession, String mode) {
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
        slangResolvedIntent.getCompletionStatement()
                .overrideAffirmative("The refund status is "
                        + orders.get(0).items.get(0).status);
        return slangSession.success();
    }

    private static void refundProduct(SlangResolvedIntent slangResolvedIntent, boolean process) {
        if(process) {
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
                slangResolvedIntent.getCompletionStatement()
                        .overrideNegative("Sorry, no pending refunds found.");
            }
            else {
                Intent intent = new Intent(appContext, OrderListActivity.class);
                intent.putExtra(ActivityDetector.ACTIVITY_MODE, ActivityDetector.MODE_REFUND_PRODUCT);
                intent.putParcelableArrayListExtra(ActivityDetector.ORDER_LIST, (ArrayList<OrderList>) orders);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                appContext.startActivity(intent);
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
        Activity activity = SlangScreenContext.getInstance().getCurrentActivity();
        if (process) {
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
                slangResolvedIntent.getCompletionStatement()
                        .overrideNegative("Sorry, no products eligible for cancellation found");
            }
            else {
                Intent intent = new Intent(activity, OrderListActivity.class);
                intent.putExtra(ActivityDetector.ACTIVITY_MODE, ActivityDetector.MODE_CANCEL_PRODUCT);
                intent.putParcelableArrayListExtra(ActivityDetector.ORDER_LIST, (ArrayList<OrderList>) orders);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                appContext.startActivity(intent);
            }
        }
    }

}