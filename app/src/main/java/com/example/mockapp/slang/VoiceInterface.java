package com.example.mockapp.slang;

import android.app.Application;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;


import com.example.mockapp.OrderListActivity;

import in.slanglabs.platform.application.ISlangApplicationStateListener;
import in.slanglabs.platform.application.SlangApplication;
import in.slanglabs.platform.application.SlangApplicationUninitializedException;
import in.slanglabs.platform.application.actions.DefaultResolvedIntentAction;
import in.slanglabs.platform.session.SlangResolvedIntent;
import in.slanglabs.platform.session.SlangSession;

public class VoiceInterface {

    //static private Handler handler;
    static Application appContext;
    private static final String TAG = VoiceInterface.class.getSimpleName();

    public static void init(final Application appContext, String appId, String authKey, final boolean shouldHide) {
        VoiceInterface.appContext = appContext;
        SlangApplication.initialize(appContext, appId, authKey, new ISlangApplicationStateListener() {
            @Override
            public void onInitialized() {
                try {
                    registerActionsNew();
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

        //handler = new Handler();
    }

    private static void registerActionsNew() throws SlangApplicationUninitializedException {
        SlangApplication.getIntentDescriptor(ActivityDetector.INTENT_TRACK_DEFAULT)
                .setResolutionAction(new DefaultResolvedIntentAction() {
                    @Override
                    public SlangSession.Status action(SlangResolvedIntent slangResolvedIntent, SlangSession slangSession) {
                        Intent i = new Intent(appContext, OrderListActivity.class);
                        Log.d(TAG, "******* Slang Triggering Default Track Intent");
                        i.putExtra(ActivityDetector.ACTIVITY_MODE, ActivityDetector.MODE_TRACK_DEFAULT);
                        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        appContext.startActivity(i);
                        return slangSession.success();
                    }
                });
        SlangApplication.getIntentDescriptor(ActivityDetector.INTENT_TRACK_PRODUCT)
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
                            Log.d(TAG, "Slang Triggering Product Track Intent");
                            Log.d(TAG,"The name of product is " + productName);
                            if (!productColor.isEmpty()) {
                                //Put color details in intent
                                Log.d(TAG,"The color of product is " + productColor);
                                i.putExtra(ActivityDetector.ENTITY_COLOR, productColor);
                            }
                            if (!productBrand.isEmpty()) {
                                Log.d(TAG, "The brand of the product is " + productBrand);
                                i.putExtra(ActivityDetector.ENTITY_BRAND, productBrand);
                            }
                            i.putExtra(ActivityDetector.ACTIVITY_MODE, ActivityDetector.MODE_TRACK_PRODUCT);
                            i.putExtra(ActivityDetector.ENTITY_PRODUCT, productName);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            appContext.startActivity(i);
                            return slangSession.success();
                        }
                        return slangSession.failure();
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
                        if(slangResolvedIntent.getEntity(ActivityDetector.ENTITY_PRODUCT).isResolved()) {
                            String productName = String.valueOf(slangResolvedIntent
                                    .getEntity(ActivityDetector.ENTITY_PRODUCT).getValue());
                            String productColor = String.valueOf(slangResolvedIntent
                                    .getEntity(ActivityDetector.ENTITY_COLOR).getValue());
                            String productBrand = String.valueOf(slangResolvedIntent
                                    .getEntity(ActivityDetector.ENTITY_BRAND).getValue());
                            Intent i = new Intent(appContext, OrderListActivity.class);
                            Log.d(TAG, "Slang Triggering Refund Track Intent");
                            Log.d(TAG,"The name of product is " + productName);
                            if (!productColor.isEmpty()) {
                                //Put color details in intent
                                Log.d(TAG,"The color of product is " + productColor);
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
                        // TODO show confirmation to return and stamp it
                        Intent i = new Intent(appContext, OrderListActivity.class);
                        Log.d(TAG, "******* Slang Triggering Default Return Intent");
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
                        // TODO show list of options, if multiple ask which one and then stamp it (Multi-modal should work even if
                        // user does not say and stamps instead)
                        //TODO if product not delivered yet, use SlangContext class to launch cancel intent
                        // OR JUST SHOW CANCELLED AND STAMP
                        if(slangResolvedIntent.getEntity(ActivityDetector.ENTITY_PRODUCT).isResolved()) {
                            String productName = String.valueOf(slangResolvedIntent
                                    .getEntity(ActivityDetector.ENTITY_PRODUCT).getValue());
                            String productColor = String.valueOf(slangResolvedIntent
                                    .getEntity(ActivityDetector.ENTITY_COLOR).getValue());
                            String productBrand = String.valueOf(slangResolvedIntent
                                    .getEntity(ActivityDetector.ENTITY_BRAND).getValue());
                            Intent i = new Intent(appContext, OrderListActivity.class);
                            Log.d(TAG, "Slang Triggering Refund Track Intent");
                            Log.d(TAG,"The name of product is " + productName);
                            if (!productColor.isEmpty()) {
                                //Put color details in intent
                                Log.d(TAG,"The color of product is " + productColor);
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
                        // TODO show list of options, if multiple ask which one and then stamp it (Multi-modal should work even if
                        // user does not say and clicks on the prompt instead)
                        // TODO if product already delivered, use SlangContext class to launch return intent
                        // OR JUST SHOW RETURN INITIATED AND STAMP
                        return null;
                    }
                });
    }
}
