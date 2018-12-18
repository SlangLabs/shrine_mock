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
import in.slanglabs.platform.application.actions.ISlangResolvedIntentAction;
import in.slanglabs.platform.session.SlangEntity;
import in.slanglabs.platform.session.SlangIntent;
import in.slanglabs.platform.session.SlangResolvedIntent;
import in.slanglabs.platform.session.SlangSession;
import in.slanglabs.platform.ui.SlangScreenContext;

public class VoiceInterface {

    static Application appContext;
    private static final String TAG = VoiceInterface.class.getSimpleName();
    private static List<OrderList> orderList;
    private static List<OrderList> orders;
    private static List<String> colorList, brandList;
    private static int num, brandNum, colorNum;

    public static void init(final Application appContext, String appId, String authKey, final boolean shouldHide) {
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
                Log.d(TAG, "________________ " + failureReason);
            }
        });

    }

    private static void registerActions() throws SlangApplicationUninitializedException {
        SlangApplication.getIntentDescriptor(ActivityDetector.INTENT_TRACK_DEFAULT)
                .setResolutionAction(new DefaultResolvedIntentAction() {
                    @Override
                    public SlangSession.Status action(SlangResolvedIntent slangResolvedIntent, SlangSession slangSession) {
                        Log.d(TAG, "Slang Triggering Default Track Intent");
                        return trackDefault(slangResolvedIntent, slangSession, ActivityDetector.MODE_TRACK_DEFAULT);
                    }
                });
        SlangApplication.getIntentDescriptor(ActivityDetector.INTENT_TRACK_PRODUCT)
                .setResolutionAction(new DefaultResolvedIntentAction() {
                    /*@Override
                    public SlangSession.Status action(SlangResolvedIntent slangResolvedIntent, SlangSession slangSession) {
                        if (slangResolvedIntent.getEntity(ActivityDetector.ENTITY_PRODUCT).isResolved()) {
                            Log.d(TAG, "Slang Triggering Product Track Intent");
                            return trackProduct(slangResolvedIntent, slangSession);
                        }
                        return slangSession.failure();
                    }*/

                    @Override
                    public SlangSession.Status onIntentResolutionBegin(SlangResolvedIntent intent, SlangSession session) {
                        orders.clear();
                        return super.onIntentResolutionBegin(intent, session);
                    }

                    @Override
                    public SlangSession.Status onEntityUnresolved(SlangEntity entity, SlangSession session) {
                        Activity activity = SlangScreenContext.getInstance().getCurrentActivity();
                        switch (entity.getName()) {
                            case ActivityDetector.ENTITY_COLOR:
                                Log.d(TAG, "Calling Unresolved Color");
                                if ((colorNum + 1) == colorList.size()) {
                                    Log.d(TAG, "Resolving color, value is " + colorList.get(0));
                                    entity.resolve(colorList.get(0));
                                    return session.success();
                                }
                                if (num == 1) {
                                    Log.d(TAG, "Only 1 value in list, resolving color");
                                    entity.resolve(orders.get(0).items.get(0).color);
                                }
                                else {
                                    if (!(activity instanceof OrderListActivity)) {
                                        Intent intent = new Intent(appContext, OrderListActivity.class);
                                        intent.putExtra(ActivityDetector.ACTIVITY_MODE, ActivityDetector.MODE_TRACK_PRODUCT);
                                        intent.putParcelableArrayListExtra(ActivityDetector.ORDER_LIST, (ArrayList<OrderList>) orders);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        appContext.startActivity(intent);
                                    }
                                }
                                return session.success();
                            case ActivityDetector.ENTITY_BRAND:
                                Log.d(TAG, "Calling Unresolved Brand");
                                if ((brandNum + 1) == brandList.size()) {
                                    Log.d(TAG, "Resolving brand, value is " + brandList.get(0));
                                    entity.resolve(brandList.get(0));
                                    return session.success();
                                }
                                if (num == 1) {
                                    Log.d(TAG, "Only 1 value in list, resolving brand");
                                    entity.resolve(orders.get(0).items.get(0).brand);
                                }
                                else {
                                    if (!(activity instanceof OrderListActivity)) {
                                        Intent intent = new Intent(appContext, OrderListActivity.class);
                                        intent.putExtra(ActivityDetector.ACTIVITY_MODE, ActivityDetector.MODE_TRACK_PRODUCT);
                                        intent.putParcelableArrayListExtra(ActivityDetector.ORDER_LIST, (ArrayList<OrderList>) orders);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        appContext.startActivity(intent);
                                    }
                                }
                                return session.success();
                            default:
                                Log.d(TAG, "Calling Unresolved Product");
                                return super.onEntityUnresolved(entity, session);
                        }
                    }

                    @Override
                    public SlangSession.Status onEntityResolved(SlangEntity entity, SlangSession session) {
                        Log.d(TAG, "Calling onEntityResolved");
                        switch (entity.getName()) {
                            case ActivityDetector.ENTITY_COLOR:
                                String productColor = entity.getValue();
                                int sizeOut = orders.size();
                                List<OrderEntry> list;
                                for (int i = 0; i < sizeOut; i++) {
                                    list = orders.get(i).items;
                                    int sizeIn = list.size();
                                    boolean delete = false;
                                    for (int j = 0; j < sizeIn; j++) {
                                        OrderEntry entry = list.get(j);
                                        String color = entry.color;
                                        if (!productColor.equalsIgnoreCase(color)) {
                                            //this operation is call by reference
                                            list.remove(j);
                                            colorList.remove(j);
                                            sizeIn--;
                                            j--;
                                        }
                                        if (sizeIn == 0) {
                                            delete = true;
                                            break;
                                        }
                                    }
                                    //to remove the order number and date details if inner list is empty
                                    if (delete) {
                                        orders.remove(i);
                                        sizeOut--;
                                        i--;
                                    }
                                    if (sizeOut == 0)
                                        break;
                                }
                                num = orders.size();
                                return session.success();
                            case ActivityDetector.ENTITY_BRAND:
                                String productBrand = entity.getValue();
                                sizeOut = orders.size();
                                for (int i = 0; i < sizeOut; i++) {
                                    list = orders.get(i).items;
                                    int sizeIn = list.size();
                                    boolean delete = false;
                                    for (int j = 0; j < sizeIn; j++) {
                                        OrderEntry entry = list.get(j);
                                        String brand = entry.brand;
                                        if (!productBrand.equalsIgnoreCase(brand)) {
                                            //this operation is call by reference
                                            list.remove(j);
                                            sizeIn--;
                                            j--;
                                        }
                                        if (sizeIn == 0) {
                                            delete = true;
                                            break;
                                        }
                                    }
                                    //to remove the order number and date details if inner list is empty
                                    if (delete) {
                                        orders.remove(i);
                                        sizeOut--;
                                        i--;
                                    }
                                    if (sizeOut == 0)
                                        break;
                                }
                                num = orders.size();
                                return session.success();
                            case ActivityDetector.ENTITY_PRODUCT:
                                Log.d(TAG, "Entity Product");
                                trackProduct(entity.getParent(), true);
                                return session.success();
                            default:
                                return super.onEntityResolved(entity, session);
                        }
                    }

                    @Override
                    public SlangSession.Status action(SlangResolvedIntent slangResolvedIntent, SlangSession slangSession) {
                        trackProduct(slangResolvedIntent, false);
                        return slangSession.success();
                    }
                });
        SlangApplication.getIntentDescriptor(ActivityDetector.INTENT_TRACK_ALL)
                .setResolutionAction(new DefaultResolvedIntentAction() {
                    @Override
                    public SlangSession.Status action(SlangResolvedIntent slangResolvedIntent, SlangSession slangSession) {
                        Log.d(TAG, "Slang Triggering All Track Intent");
                        return trackDefault(slangResolvedIntent, slangSession, ActivityDetector.MODE_TRACK_ALL);
                    }
                });
        SlangApplication.getIntentDescriptor(ActivityDetector.INTENT_TRACK_REFUND_DEFAULT)
                .setResolutionAction(new DefaultResolvedIntentAction() {
                    @Override
                    public SlangSession.Status action(SlangResolvedIntent slangResolvedIntent, SlangSession slangSession) {
                        Intent i = new Intent(appContext, OrderListActivity.class);
                        Log.d(TAG, "******* Slang Triggering Default Refund Intent");
                        i.putExtra(ActivityDetector.ACTIVITY_MODE, ActivityDetector.MODE_REFUND_DEFAULT);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        appContext.startActivity(i);
                        return slangSession.success();
                    }
                });
        SlangApplication.getIntentDescriptor(ActivityDetector.INTENT_TRACK_REFUND_PRODUCT)
                .setResolutionAction(new DefaultResolvedIntentAction() {
                    @Override
                    public SlangSession.Status action(SlangResolvedIntent slangResolvedIntent, SlangSession slangSession) {
                        if (slangResolvedIntent.getEntity(ActivityDetector.ENTITY_PRODUCT).isResolved()) {
                            String productName = String.valueOf(slangResolvedIntent
                                    .getEntity(ActivityDetector.ENTITY_PRODUCT).getValue());
                            String productColor = String.valueOf(slangResolvedIntent
                                    .getEntity(ActivityDetector.ENTITY_COLOR).getValue());
                            String productBrand = String.valueOf(slangResolvedIntent
                                    .getEntity(ActivityDetector.ENTITY_BRAND).getValue());
                            Intent i = new Intent(appContext, OrderListActivity.class);
                            Log.d(TAG, "Slang Triggering Refund Track Intent");
                            Log.d(TAG, "The name of product is " + productName);
                            if (!productColor.isEmpty()) {
                                //Put color details in intent
                                Log.d(TAG, "The color of product is " + productColor);
                                i.putExtra(ActivityDetector.ENTITY_COLOR, productColor);
                            }
                            if (!productBrand.isEmpty()) {
                                Log.d(TAG, "The brand of the product is " + productBrand);
                                i.putExtra(ActivityDetector.ENTITY_BRAND, productBrand);
                            }
                            i.putExtra(ActivityDetector.ACTIVITY_MODE, ActivityDetector.MODE_REFUND_PRODUCT);
                            i.putExtra(ActivityDetector.ENTITY_PRODUCT, productName);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            appContext.startActivity(i);
                            return slangSession.success();
                        }
                        return slangSession.failure();
                    }
                });
        SlangApplication.getIntentDescriptor(ActivityDetector.INTENT_RETURN_DEFAULT)
                .setResolutionAction(new DefaultResolvedIntentAction() {
                    @Override
                    public SlangSession.Status action(SlangResolvedIntent slangResolvedIntent, SlangSession slangSession) {
                        Intent i = new Intent(appContext, OrderListActivity.class);
                        Log.d(TAG, "Slang Triggering Default Return Intent");
                        i.putExtra(ActivityDetector.ACTIVITY_MODE, ActivityDetector.MODE_RETURN_DEFAULT);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        appContext.startActivity(i);
                        return slangSession.success();
                    }
                });
        SlangApplication.getIntentDescriptor(ActivityDetector.INTENT_RETURN_PRODUCT)
                .setResolutionAction(new DefaultResolvedIntentAction() {
                    @Override
                    public SlangSession.Status action(SlangResolvedIntent slangResolvedIntent, SlangSession slangSession) {
                        if (slangResolvedIntent.getEntity(ActivityDetector.ENTITY_PRODUCT).isResolved()) {
                            String productName = String.valueOf(slangResolvedIntent
                                    .getEntity(ActivityDetector.ENTITY_PRODUCT).getValue());
                            String productColor = String.valueOf(slangResolvedIntent
                                    .getEntity(ActivityDetector.ENTITY_COLOR).getValue());
                            String productBrand = String.valueOf(slangResolvedIntent
                                    .getEntity(ActivityDetector.ENTITY_BRAND).getValue());
                            Intent i = new Intent(appContext, OrderListActivity.class);
                            Log.d(TAG, "Slang Triggering Return Track Intent");
                            Log.d(TAG, "The name of product is " + productName);
                            if (!productColor.isEmpty()) {
                                //Put color details in intent
                                Log.d(TAG, "The color of product is " + productColor);
                                i.putExtra(ActivityDetector.ENTITY_COLOR, productColor);
                            }
                            if (!productBrand.isEmpty()) {
                                Log.d(TAG, "The brand of the product is " + productBrand);
                                i.putExtra(ActivityDetector.ENTITY_BRAND, productBrand);
                            }
                            i.putExtra(ActivityDetector.ACTIVITY_MODE, ActivityDetector.MODE_RETURN_PRODUCT);
                            i.putExtra(ActivityDetector.ENTITY_PRODUCT, productName);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            appContext.startActivity(i);
                            return slangSession.success();
                        }
                        return slangSession.failure();
                    }
                });
        SlangApplication.getIntentDescriptor(ActivityDetector.INTENT_CANCEL_PRODUCT)
                .setResolutionAction(new DefaultResolvedIntentAction() {
                    @Override
                    public SlangSession.Status action(SlangResolvedIntent slangResolvedIntent, SlangSession slangSession) {
                        if (slangResolvedIntent.getEntity(ActivityDetector.ENTITY_PRODUCT).isResolved()) {
                            String productName = String.valueOf(slangResolvedIntent
                                    .getEntity(ActivityDetector.ENTITY_PRODUCT).getValue());
                            String productColor = String.valueOf(slangResolvedIntent
                                    .getEntity(ActivityDetector.ENTITY_COLOR).getValue());
                            String productBrand = String.valueOf(slangResolvedIntent
                                    .getEntity(ActivityDetector.ENTITY_BRAND).getValue());
                            Intent i = new Intent(appContext, OrderListActivity.class);
                            Log.d(TAG, "Slang Triggering Cancel Track Intent");
                            Log.d(TAG, "The name of product is " + productName);
                            if (!productColor.isEmpty()) {
                                //Put color details in intent
                                Log.d(TAG, "The color of product is " + productColor);
                                i.putExtra(ActivityDetector.ENTITY_COLOR, productColor);
                            }
                            if (!productBrand.isEmpty()) {
                                Log.d(TAG, "The brand of the product is " + productBrand);
                                i.putExtra(ActivityDetector.ENTITY_BRAND, productBrand);
                            }
                            i.putExtra(ActivityDetector.ACTIVITY_MODE, ActivityDetector.MODE_CANCEL);
                            i.putExtra(ActivityDetector.ENTITY_PRODUCT, productName);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            appContext.startActivity(i);
                            return slangSession.success();
                        }
                        return slangSession.failure();
                    }
                });
    }

    private static SlangSession.Status trackDefault(final SlangResolvedIntent slangResolvedIntent,
                                                    final SlangSession slangSession, String mode) {
        //final Activity activity = SlangScreenContext.getInstance().getCurrentActivity();
        List<OrderList> orderList = OrderList.initOrderList(appContext.getResources());
        Intent i = new Intent(appContext, OrderListActivity.class);
        i.putExtra(ActivityDetector.ACTIVITY_MODE, mode);
        i.putParcelableArrayListExtra(ActivityDetector.ORDER_LIST, (ArrayList<OrderList>) orderList);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        appContext.startActivity(i);
        return slangSession.success();
    }

    private static void trackProduct(final SlangResolvedIntent slangResolvedIntent, boolean process) {

        Activity activity = SlangScreenContext.getInstance().getCurrentActivity();

        //TODO integrate date
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
            Log.d(TAG, "______ value of num is " + num);
        } else {
            if (num == 0) {
                slangResolvedIntent.getCompletionStatement()
                        .overrideNegative("Sorry, no matching products found");
                //return slangSession.failure();
            } else if (num == 1) {
                if (!(activity instanceof OrderListActivity)) {
                    Intent intent = new Intent(appContext, OrderListActivity.class);
                    intent.putExtra(ActivityDetector.ACTIVITY_MODE, ActivityDetector.MODE_TRACK_PRODUCT);
                    intent.putParcelableArrayListExtra(ActivityDetector.ORDER_LIST, (ArrayList<OrderList>) orders);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    appContext.startActivity(intent);
                }
            }
        }
    }
}












        /*boolean again = true;
        while (again) {
            Log.d(TAG, "__________________Calling switch statement");
            switch (num) {
                case 0:
                    Log.d(TAG, "______ none");
                    slangResolvedIntent.getCompletionStatement()
                            .overrideNegative("Sorry, no matching products found");
                    return slangSession.failure();
                case 1:
                    Log.d(TAG, "______ one");
                    Intent intent = new Intent(appContext, OrderListActivity.class);
                    intent.putExtra(ActivityDetector.ACTIVITY_MODE, ActivityDetector.MODE_TRACK_PRODUCT);
                    intent.putParcelableArrayListExtra(ActivityDetector.ORDER_LIST, (ArrayList<OrderList>) orders);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    appContext.startActivity(intent);
                    return slangSession.success();
                default:
                    Log.d(TAG, "______ multiple");


                    //to check which entities were present, call isEmpty() or null comparison


                    //both entities were not provided by the user
                    if (colorList.size() > 0 && brandList.size() > 0) {
                        //color is the more common factor
                        if (colorNum >= brandNum) {
                            Log.d(TAG, "colorNum > brandNum");
                            boolean unresolved = true;
                            while (unresolved) {
                                slangResolvedIntent.getEntity(ActivityDetector.ENTITY_BRAND).getPrompt().getQuestion();
                                if (slangResolvedIntent.getEntity(ActivityDetector.ENTITY_BRAND).isResolved()) {
                                    unresolved = false;
                                    productColor = slangResolvedIntent.getEntity(ActivityDetector.ENTITY_BRAND).getValue();
                                    int sizeOut = orders.size();
                                    for (int i = 0; i < sizeOut; i++) {
                                        list = orders.get(i).items;
                                        int sizeIn = list.size();
                                        boolean delete = false;
                                        for (int j = 0; j < sizeIn; j++) {
                                            OrderEntry entry = list.get(j);
                                            String brand = entry.brand;
                                            if (!productColor.equalsIgnoreCase(brand)) {
                                                //this operation is call by reference
                                                list.remove(j);
                                                sizeIn--;
                                                j--;
                                            }
                                            if (sizeIn == 0) {
                                                delete = true;
                                                break;
                                            }
                                        }
                                        //to remove the order number and date details if inner list is empty
                                        if (delete) {
                                            orders.remove(i);
                                            sizeOut--;
                                            i--;
                                        }
                                        if (sizeOut == 0)
                                            break;
                                    }
                                    num = orders.size();
                                    brandNum = -1;

                                    //update value of num to size of orders, and use continue to go back to beginning of switch statement.
                                    // Do not launch another activity, just update the one presently on screen (RunOnUIThread?)
                                }
                            }
                            break;
                        } else {
                            Log.d(TAG, "colorNum < brandNum");
                            boolean unresolved = true;
                            while (unresolved) {
                                slangResolvedIntent.getEntity(ActivityDetector.ENTITY_COLOR).getPrompt().getQuestion();
                                if (slangResolvedIntent.getEntity(ActivityDetector.ENTITY_COLOR).isResolved()) {
                                    unresolved = false;
                                    productColor = slangResolvedIntent.getEntity(ActivityDetector.ENTITY_COLOR).getValue();
                                    int sizeOut = orders.size();
                                    for (int i = 0; i < sizeOut; i++) {
                                        list = orders.get(i).items;
                                        int sizeIn = list.size();
                                        boolean delete = false;
                                        for (int j = 0; j < sizeIn; j++) {
                                            OrderEntry entry = list.get(j);
                                            String color = entry.color;
                                            if (!productColor.equalsIgnoreCase(color)) {
                                                //this operation is call by reference
                                                list.remove(j);
                                                sizeIn--;
                                                j--;
                                            }
                                            if (sizeIn == 0) {
                                                delete = true;
                                                break;
                                            }
                                        }
                                        //to remove the order number and date details if inner list is empty
                                        if (delete) {
                                            orders.remove(i);
                                            sizeOut--;
                                            i--;
                                        }
                                        if (sizeOut == 0)
                                            break;
                                    }
                                    num = orders.size();
                                    colorNum = -1;
                                }
                            }

                            break;
                        }
                    }
                    // only color not provided by the user
                    else if (colorList.size() > 0) {
                        boolean unresolved = true;
                        while (unresolved) {
                            slangResolvedIntent.getEntity(ActivityDetector.ENTITY_COLOR).getPrompt().getQuestion();
                            if (slangResolvedIntent.getEntity(ActivityDetector.ENTITY_COLOR).isResolved()) {
                                unresolved = false;
                                productColor = slangResolvedIntent.getEntity(ActivityDetector.ENTITY_COLOR).getValue();
                                int sizeOut = orders.size();
                                for (int i = 0; i < sizeOut; i++) {
                                    list = orders.get(i).items;
                                    int sizeIn = list.size();
                                    boolean delete = false;
                                    for (int j = 0; j < sizeIn; j++) {
                                        OrderEntry entry = list.get(j);
                                        String color = entry.color;
                                        if (!productColor.equalsIgnoreCase(color)) {
                                            //this operation is call by reference
                                            list.remove(j);
                                            sizeIn--;
                                            j--;
                                        }
                                        if (sizeIn == 0) {
                                            delete = true;
                                            break;
                                        }
                                    }
                                    //to remove the order number and date details if inner list is empty
                                    if (delete) {
                                        orders.remove(i);
                                        sizeOut--;
                                        i--;
                                    }
                                    if (sizeOut == 0)
                                        break;
                                }
                                colorList.clear();
                                num = orders.size();
                            }
                        }
                        break;
                    }
                    // only brand not provided by the user
                    else if (brandList.size() > 0) {
                        boolean unresolved = true;
                        while (unresolved) {
                            slangResolvedIntent.getEntity(ActivityDetector.ENTITY_BRAND).getPrompt().getQuestion();
                            if (slangResolvedIntent.getEntity(ActivityDetector.ENTITY_BRAND).isResolved()) {
                                unresolved = false;
                                productColor = slangResolvedIntent.getEntity(ActivityDetector.ENTITY_BRAND).getValue();
                                int sizeOut = orders.size();
                                for (int i = 0; i < sizeOut; i++) {
                                    list = orders.get(i).items;
                                    int sizeIn = list.size();
                                    boolean delete = false;
                                    for (int j = 0; j < sizeIn; j++) {
                                        OrderEntry entry = list.get(j);
                                        String brand = entry.brand;
                                        if (!productColor.equalsIgnoreCase(brand)) {
                                            //this operation is call by reference
                                            list.remove(j);
                                            sizeIn--;
                                            j--;
                                        }
                                        if (sizeIn == 0) {
                                            delete = true;
                                            break;
                                        }
                                    }
                                    //to remove the order number and date details if inner list is empty
                                    if (delete) {
                                        orders.remove(i);
                                        sizeOut--;
                                        i--;
                                    }
                                    if (sizeOut == 0)
                                        break;
                                }
                                brandList.clear();
                                num = orders.size();
                            }
                        }
                        break;
                    }
                    else {
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(appContext, "Unhandled case, using date", Toast.LENGTH_LONG).show();
                            }
                        });
                        break;
                    }
            }
        }
        Log.d(TAG, "____________ Out of switch");
        return slangSession.suspend();*/
