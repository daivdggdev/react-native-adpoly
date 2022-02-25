package com.dwwang.RNAdPoly;

import android.content.Context;

import com.qq.e.comm.managers.GDTAdSdk;

/**
 * 可以用一个单例来保存TTAdManager实例，在需要初始化sdk的时候调用
 */
public class GDTAdManagerHolder {

    private static boolean sInit = false;

    public static void init(Context context, String appId) {
        if (!sInit) {
            GDTAdSdk.init(context, appId);
            sInit = true;
        }
    }

    public static boolean isInitSuccess() {
        return sInit;
    }
}
