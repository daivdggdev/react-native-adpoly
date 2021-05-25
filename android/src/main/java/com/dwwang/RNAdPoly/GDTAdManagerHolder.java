package com.dwwang.RNAdPoly;

import android.content.Context;

import com.qq.e.comm.managers.GDTADManager;

/**
 * 可以用一个单例来保存TTAdManager实例，在需要初始化sdk的时候调用
 */
public class GDTAdManagerHolder {

    private static boolean sInit = false;


    public static GDTADManager get() {
        if (!sInit) {
            throw new RuntimeException("GDTAdSdk is not init, please check.");
        }
        return GDTADManager.getInstance();
    }

    public static void init(Context context, String appId) {
        if (!sInit) {
            GDTADManager.getInstance().initWith(context, appId);
            sInit = true;
        }
    }

    public static boolean isInitSuccess() {
        return sInit;
    }
}
