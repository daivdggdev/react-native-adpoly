package com.dwwang.RNAdPoly;

import android.content.Context;
import android.util.Log;

import com.qq.e.comm.managers.GDTAdSdk;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;

/**
 * 可以用一个单例来保存TTAdManager实例，在需要初始化sdk的时候调用
 */
public class GDTAdManagerHolder {

    private static boolean sInit = false;

    public static void init(Context context, String appId) {
        if (!sInit) {
            GDTAdSdk.initWithoutStart(context, appId); // 调用此接口进行初始化，该接口不会采集用户信息
            // 调用initWithoutStart后请尽快调用start，否则可能影响广告填充，造成收入下降
            GDTAdSdk.start(new GDTAdSdk.OnStartListener() {
                @Override
                public void onStartSuccess() {
                    // 推荐开发者在onStartSuccess回调后开始拉广告
                    sInit = true;
                    Log.d("GDTAdSdk", "init success");

                    WritableMap params = Arguments.createMap();
                    params.putString("type", "gdt");
                    AdHelper.sendEvent("AdInitSuccess", params);
                }

                @Override
                public void onStartFailed(Exception e) {
                    Log.e("gdt onStartFailed:", e.toString());
                }
            });
        }
    }

    public static boolean isInitSuccess() {
        return sInit;
    }
}
