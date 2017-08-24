package com.dwwang.RNAdPoly;

import android.net.Uri;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;

import java.util.HashMap;
import java.util.Map;

import com.qq.e.ads.splash.SplashAD;
import com.qq.e.ads.splash.SplashADListener;

public class RNAdPolyModule extends ReactContextBaseJavaModule {

    private SplashAD mSplashAD;
    ReactApplicationContext context;

    public RNSoundModule(ReactApplicationContext context) {
        super(context);
        this.context = context;
    }

    @Override
    public String getName() {
        return "RNAdPoly";
    }

    @ReactMethod
    public void showSplash(String type, String appKey, String placementId) {
        if (type == "gdt") {
            showGdtSplash(appKey, placementId);
        }
    }

    private void showGdtSplash(String appKey, String placementId) {
        if (appKey == null || placementId == null) {
            return;
        }
        
        ViewGroup viewGroup = (ViewGroup) getWindow().getDecorView();
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
    }

    @Override
    public Map<String, Object> getConstants() {
        final Map<String, Object> constants = new HashMap<>();
        constants.put("IsAndroid", true);
        return constants;
    }
}
