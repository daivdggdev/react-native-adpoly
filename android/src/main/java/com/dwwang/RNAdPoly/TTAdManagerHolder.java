package com.dwwang.RNAdPoly;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.bytedance.sdk.openadsdk.TTAdConfig;
import com.bytedance.sdk.openadsdk.TTAdManager;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.bytedance.sdk.openadsdk.TTCustomController;
import com.bytedance.sdk.openadsdk.mediation.init.MediationConfigUserInfoForSegment;
import com.bytedance.sdk.openadsdk.mediation.init.MediationPrivacyConfig;

import java.util.HashMap;
import java.util.Map;


/**
 * 可以用一个单例来保存TTAdManager实例，在需要初始化sdk的时候调用
 */
public class TTAdManagerHolder {

    private static final String TAG = "TTAdManagerHolder";

    private static boolean sInit;
    private static boolean sStart;

    public static TTAdManager get() {
        return TTAdSdk.getAdManager();
    }

    public static void init(final Context context) {
        //初始化穿山甲SDK
        doInit(context);
    }

    //step1:接入网盟广告sdk的初始化操作，详情见接入文档和穿山甲平台说明
    private static void doInit(Context context) {
        if (sInit) {
            Toast.makeText(context, "您已经初始化过了", Toast.LENGTH_LONG).show();
            return;
        }
        //TTAdSdk.init(context, buildConfig(context));
        //setp1.1：初始化SDK

        TTAdSdk.init(context, buildConfig(context));
        sInit = true;
        Toast.makeText(context, "初始化成功", Toast.LENGTH_LONG).show();
    }

    public static void start(Context context) {
        if (!sInit) {
            Toast.makeText(context, "还没初始化SDK，请先进行初始化", Toast.LENGTH_LONG).show();
            return;
        }
        if (sStart) {
            return;
        }
        //setp1.2：启动SDK

        TTAdSdk.start(new TTAdSdk.Callback() {
            @Override
            public void success() {
                Log.i(TAG, "success: " + TTAdSdk.isSdkReady());
            }

            @Override
            public void fail(int code, String msg) {
                sStart = false;
                Log.i(TAG, "fail:  code = " + code + " msg = " + msg);
            }
        });
        sStart = true;
    }

    private static TTAdConfig buildConfig(Context context) {

        return new TTAdConfig.Builder()
                /**
                 * 注：需要替换成在媒体平台申请的appID ，切勿直接复制
                 */
                .appId("5001121")
                .appName("APP测试媒体")
                /**
                 * 上线前需要关闭debug开关，否则会影响性能
                 */
                .debug(true)
                /**
                 * 使用聚合功能此开关必须设置为true，默认为false，不会初始化聚合模板，聚合功能会吟唱
                 */
                .useMediation(true)
//                .customController(getTTCustomController()) //如果您需要设置隐私策略请参考该api
//                .setMediationConfig(new MediationConfig.Builder() //可设置聚合特有参数详细设置请参考该api
//                        .setMediationConfigUserInfoForSegment(getUserInfoForSegment())//如果您需要配置流量分组信息请参考该api
//                        .build())
                .build();
    }

    private static MediationConfigUserInfoForSegment getUserInfoForSegment(){
        MediationConfigUserInfoForSegment userInfo = new MediationConfigUserInfoForSegment();
        userInfo.setUserId("msdk-demo");
        userInfo.setGender(MediationConfigUserInfoForSegment.GENDER_MALE);
        userInfo.setChannel("msdk-channel");
        userInfo.setSubChannel("msdk-sub-channel");
        userInfo.setAge(999);
        userInfo.setUserValueGroup("msdk-demo-user-value-group");

        Map<String, String> customInfos = new HashMap<>();
        customInfos.put("aaaa", "test111");
        customInfos.put("bbbb", "test222");
        userInfo.setCustomInfos(customInfos);
        return userInfo;
    }

    private static TTCustomController getTTCustomController(){
        return new TTCustomController() {

            @Override
            public boolean isCanUseWifiState() {
                return super.isCanUseWifiState();
            }

            @Override
            public String getMacAddress() {
                return super.getMacAddress();
            }

            @Override
            public boolean isCanUseWriteExternal() {
                return super.isCanUseWriteExternal();
            }

            @Override
            public String getDevOaid() {
                return super.getDevOaid();
            }

            @Override
            public boolean isCanUseAndroidId() {
                return super.isCanUseAndroidId();
            }

            @Override
            public String getAndroidId() {
                return super.getAndroidId();
            }

            @Override
            public MediationPrivacyConfig getMediationPrivacyConfig() {
                return new MediationPrivacyConfig() {

                    @Override
                    public boolean isLimitPersonalAds() {
                        return super.isLimitPersonalAds();
                    }

                    @Override
                    public boolean isProgrammaticRecommend() {
                        return super.isProgrammaticRecommend();
                    }
                };
            }

            @Override
            public boolean isCanUsePermissionRecordAudio() {
                return super.isCanUsePermissionRecordAudio();
            }
        };
    }
}
