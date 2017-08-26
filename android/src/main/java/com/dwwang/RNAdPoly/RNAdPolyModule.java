package com.dwwang.RNAdPoly;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;

import java.util.HashMap;
import java.util.Map;
import android.util.Log;
import android.net.Uri;
import android.view.ViewGroup;
import android.content.Intent;

import com.qq.e.ads.splash.SplashAD;
import com.qq.e.ads.splash.SplashADListener;


public class RNAdPolyModule extends ReactContextBaseJavaModule {

    private SplashAD mSplashAD;
    ReactApplicationContext context;

    public RNAdPolyModule(ReactApplicationContext context) {
        super(context);
        this.context = context;
    }

    @Override
    public String getName() {
        return "RNAdPoly";
    }

    @ReactMethod
    public void showSplash(String type, String appKey, String placementId) {
        Log.i("AD_DEMO", "type = " + type);
            showGdtSplash(appKey, placementId);
        if (type == "gdt") {
        Log.i("AD_DEMO", "type22 = " + type);
            showGdtSplash(appKey, placementId);
        }
        Log.i("AD_DEMO", "type33 = " + type);
    }

    private void showGdtSplash(String appKey, String placementId) {
        Log.i("AD_DEMO", "appKey = " + appKey);
        if (appKey == null || placementId == null) {
            return;
        }

        
        Log.i("AD_DEMO", "showGdtSplash");
        ReactApplicationContext context = getReactApplicationContext();
        int rid = IdHelper.getLayout(context, "activity_splash");
        Log.i("AD_DEMO", "rid112 = " + R.layout.dev_loading_view);

        Log.i("AD_DEMO", "R.layout = " + context.getApplicationContext().getPackageName());

        Intent intent = new Intent(context, SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
        //this.context.startActivity(new Intent(this.context, SplashActivity.class));
        /*
        Activity activity = (Activity)this.context;
        ViewGroup viewGroup = (ViewGroup) activity.getWindow().getDecorView();
        mSplashAD = new SplashAD(this.context, viewGroup, appKey, placementId, new SplashADListener() {
            @Override
            public void onADDismissed() {
                Log.i("AD_DEMO", "onADDismissed");
            }

            @Override
            public void onNoAD(int i) {
                Log.i("AD_DEMO", "onNoAD");
            }

            @Override
            public void onADPresent() {
                Log.i("AD_DEMO", "onADPresent");
            }

            @Override
            public void onADClicked() {
                Log.i("AD_DEMO", "onADClicked");
            }

            @Override
            public void onADTick(long l) {
                Log.i("AD_DEMO", "onADTick");
            }
        });
        */
    }

    @Override
    public Map<String, Object> getConstants() {
        final Map<String, Object> constants = new HashMap<>();
        constants.put("IsAndroid", true);
        return constants;
    }
}
