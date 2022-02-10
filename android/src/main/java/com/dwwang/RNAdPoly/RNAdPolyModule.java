package com.dwwang.RNAdPoly;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;

import java.util.HashMap;
import java.util.Map;
import java.lang.Runnable;
import android.util.Log;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.content.Intent;

import com.anythink.core.api.ATSDK;
import com.anythink.china.api.ATChinaSDKHandler;
import com.anythink.china.api.ATAppDownloadListener;
import com.anythink.core.api.ATAdConst;
import com.anythink.core.api.ATAdInfo;
import com.anythink.core.api.ATAdStatusInfo;
import com.anythink.core.api.AdError;
import com.anythink.rewardvideo.api.ATRewardVideoAd;
import com.anythink.rewardvideo.api.ATRewardVideoExListener;

public class RNAdPolyModule extends ReactContextBaseJavaModule {
    private static final String TAG = "AD_DEMO";

    ReactApplicationContext context;
    ATRewardVideoAd mRewardVideoAd;

    public RNAdPolyModule(ReactApplicationContext context) {
        super(context);
        this.context = context;
        AdHelper.reactContext = context;
    }

    @Override
    public String getName() {
        return "RNAdPoly";
    }

    @ReactMethod
    public void init(String appId, String appKey) {
        Log.i("AD_DEMO", "init appId = " + appId);
        // if (type.equals("gdt")) {
        //     GDTADManager.getInstance().initWith(this.context, appId);
        // } else if (type.equals("tt")) {
        //     Handler mainHandler = new Handler(this.context.getMainLooper());
        //     Runnable myRunnable = new Runnable() {
        //         @Override
        //         public void run() {
        //             TTAdManagerHolder.init(context, appId);
        //         }
        //     };

        //     mainHandler.post(myRunnable);
        // }

        ATSDK.setNetworkLogDebug(true);//SDK日志功能，集成测试阶段建议开启，上线前必须关闭

        Log.i("AD_DEMO", "TopOn SDK version: " + ATSDK.getSDKVersionName());//SDK版本

        ATSDK.integrationChecking(context);//检查广告平台的集成状态
        //(v5.7.77新增) 打印当前设备的设备信息(IMEI、OAID、GAID、AndroidID等)
        // ATSDK.testModeDeviceInfo(context, new DeviceInfoCallback() {
        //     @Override
        //     public void deviceInfo(String deviceInfo) {
        //         Log.i("AD_DEMO", "deviceInfo: " + deviceInfo);
        //     }
        // });


        ATSDK.init(context, appId, appKey);//初始化SDK
    }

    @ReactMethod
    public void requestPermissionIfNecessary() {
        Log.i("AD_DEMO", "requestPermissionIfNecessary");
        //在合适的时机申请权限，如read_phone_state,防止获取不了imei时候，下载类广告没有填充的问题
        //在开屏时候申请不太合适，因为该页面倒计时结束或者请求超时会跳转，在该页面申请权限，体验不好
        // TTAdManagerHolder.get().requestPermissionIfNecessary(this.context);

        ATChinaSDKHandler atChinaSDKHandler = new ATChinaSDKHandler();
        atChinaSDKHandler.requestPermissionIfNecessary(context);
    }

    @ReactMethod
    public void showSplash(String placementId) {
        Log.i("AD_DEMO", "showSplash placementId = " + placementId);

        ReactApplicationContext context = getReactApplicationContext();
        Intent intent = new Intent(context, ATSplashActivity.class);
        intent.putExtra("placementId", placementId);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);

        // if (type.equals("gdt")) {
        //     if (!GDTAdManagerHolder.isInitSuccess()) {
        //         GDTAdManagerHolder.init(context, appKey);
        //     }
            
        //     showGdtSplash(appKey, placementId);
        // } else if (type.equals("tt")) {
        //     if (TTAdManagerHolder.isInitSuccess()) {
        //         showTTSplash(placementId);
        //     } else {
        //         Handler mainHandler = new Handler(this.context.getMainLooper());
        //         Runnable myRunnable = new Runnable() {
        //             @Override
        //             public void run() {
        //                 TTAdManagerHolder.init(context, appKey);
        //                 showTTSplash(placementId);
        //             }
        //         };
        //         mainHandler.post(myRunnable);
        //     }
        // }
    }

    @ReactMethod
    public void loadRewardVideo(String placementId) {
        Log.i("AD_DEMO", "loadRewardVideo placementId = " + placementId);

        if (mRewardVideoAd == null) {
            mRewardVideoAd = new ATRewardVideoAd(context, placementId);
            mRewardVideoAd.setAdListener(new ATRewardVideoExListener() {

                @Override
                public void onDeeplinkCallback(ATAdInfo adInfo, boolean isSuccess) {
                    Log.i(TAG, "onDeeplinkCallback:" + adInfo.toString() + "--status:" + isSuccess);
                }
    
                @Override
                public void onRewardedVideoAdLoaded() {
                    Log.i(TAG, "onRewardedVideoAdLoaded");
                }
    
                @Override
                public void onRewardedVideoAdFailed(AdError errorCode) {
                    Log.i(TAG, "onRewardedVideoAdFailed error:" + errorCode.getFullErrorInfo());
                }
    
                @Override
                public void onRewardedVideoAdPlayStart(ATAdInfo entity) {
                    Log.i(TAG, "onRewardedVideoAdPlayStart:\n" + entity.toString());
                }
    
                @Override
                public void onRewardedVideoAdPlayEnd(ATAdInfo entity) {
                    Log.i(TAG, "onRewardedVideoAdPlayEnd:\n" + entity.toString());
                }
    
                @Override
                public void onRewardedVideoAdPlayFailed(AdError errorCode, ATAdInfo entity) {
                    Log.i(TAG, "onRewardedVideoAdPlayFailed error:" + errorCode.getFullErrorInfo());
                }
    
                @Override
                public void onRewardedVideoAdClosed(ATAdInfo entity) {
                    Log.i(TAG, "onRewardedVideoAdClosed:\n" + entity.toString());
                    mRewardVideoAd.load();
                }
    
                @Override
                public void onRewardedVideoAdPlayClicked(ATAdInfo entity) {
                    Log.i(TAG, "onRewardedVideoAdPlayClicked:\n" + entity.toString());
                }
    
                @Override
                public void onReward(ATAdInfo entity) {
                    Log.e(TAG, "onReward:\n" + entity.toString());
                    AdHelper.sendEventWithReward();
                }

                @Override
                public void onDownloadConfirm(Context context, ATAdInfo atAdInfo, ATNetworkConfirmInfo networkConfirmInfo) {
                    // if (networkConfirmInfo instanceof GDTDownloadFirmInfo) {
                    //     //二次弹窗处理，DownloadApkConfirmDialogWebView是Demo中的类文件，具体实现可查看Demo
                    //     new DownloadApkConfirmDialogWebView(context, ((GDTDownloadFirmInfo) networkConfirmInfo).appInfoUrl, ((GDTDownloadFirmInfo) networkConfirmInfo).confirmCallBack).show();
                    // }
                }
            });
    
            mRewardVideoAd.setAdDownloadListener(new ATAppDownloadListener() {
    
                @Override
                public void onDownloadStart(ATAdInfo adInfo, long totalBytes, long currBytes, String fileName, String appName) {
                    Log.i(TAG, "ATAdInfo:" + adInfo.toString() + "\n" + "onDownloadStart: totalBytes: " + totalBytes
                            + "\ncurrBytes:" + currBytes
                            + "\nfileName:" + fileName
                            + "\nappName:" + appName);
                }
    
                @Override
                public void onDownloadUpdate(ATAdInfo adInfo, long totalBytes, long currBytes, String fileName, String appName) {
                    Log.i(TAG, "ATAdInfo:" + adInfo.toString() + "\n" + "onDownloadUpdate: totalBytes: " + totalBytes
                            + "\ncurrBytes:" + currBytes
                            + "\nfileName:" + fileName
                            + "\nappName:" + appName);
                }
    
                @Override
                public void onDownloadPause(ATAdInfo adInfo, long totalBytes, long currBytes, String fileName, String appName) {
                    Log.i(TAG, "ATAdInfo:" + adInfo.toString() + "\n" + "onDownloadPause: totalBytes: " + totalBytes
                            + "\ncurrBytes:" + currBytes
                            + "\nfileName:" + fileName
                            + "\nappName:" + appName);
                }
    
                @Override
                public void onDownloadFinish(ATAdInfo adInfo, long totalBytes, String fileName, String appName) {
                    Log.i(TAG, "ATAdInfo:" + adInfo.toString() + "\n" + "onDownloadFinish: totalBytes: " + totalBytes
                            + "\nfileName:" + fileName
                            + "\nappName:" + appName);
                }
    
                @Override
                public void onDownloadFail(ATAdInfo adInfo, long totalBytes, long currBytes, String fileName, String appName) {
                    Log.i(TAG, "ATAdInfo:" + adInfo.toString() + "\n" + "onDownloadFail: totalBytes: " + totalBytes
                            + "\ncurrBytes:" + currBytes
                            + "\nfileName:" + fileName
                            + "\nappName:" + appName);
                }
    
                @Override
                public void onInstalled(ATAdInfo adInfo, String fileName, String appName) {
                    Log.i(TAG, "ATAdInfo:" + adInfo.toString() + "\n" + "onInstalled:"
                            + "\nfileName:" + fileName
                            + "\nappName:" + appName);
                }
            });
        }
        
        mRewardVideoAd.load();
    }

    @ReactMethod
    public boolean isRewardAdReady() {
        Log.i("AD_DEMO", "isRewardAdReady");

        return mRewardVideoAd.isAdReady();
    }

    @ReactMethod
    public void showRewardVideo() {
        Log.i("AD_DEMO", "showRewardVideo");

        if (mRewardVideoAd.isAdReady()){
            mRewardVideoAd.show((Activity)context);
        } else {
            mRewardVideoAd.load();
        }
    }

    @ReactMethod
    public void showFullScreenVideo(String type, String appKey, String placementId) {
        Log.i("AD_DEMO", "type = " + type);
        if (type.equals("tt")) {
            showTTFullScreenVideo(placementId);
        }
    }

    private void showGdtSplash(String appKey, String placementId) {
        Log.i("AD_DEMO", "appKey = " + appKey);
        if (appKey == null || placementId == null) {
            return;
        }

        Log.i("AD_DEMO", "showGdtSplash");
        ReactApplicationContext context = getReactApplicationContext();

        Intent intent = new Intent(context, SplashActivity.class);
        intent.putExtra("appKey", appKey);
        intent.putExtra("placementId", placementId);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private void showTTSplash(String placementId) {
        Log.i("AD_DEMO", "showTTSplash placementId = " + placementId);
        if (TextUtils.isEmpty(placementId)) {
            return;
        }

        ReactApplicationContext context = getReactApplicationContext();

        Intent intent = new Intent(context, TTSplashActivity.class);
        intent.putExtra("placementId", placementId);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private void showTTFullScreenVideo(String placementId) {
        Log.i("AD_DEMO", "showTTFullScreenVideo placementId = " + placementId);
        if (TextUtils.isEmpty(placementId)) {
            return;
        }

        ReactApplicationContext context = getReactApplicationContext();

        Intent intent = new Intent(context, TTFullScreenVideoActivity.class);
        intent.putExtra("placementId", placementId);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    @Override
    public Map<String, Object> getConstants() {
        final Map<String, Object> constants = new HashMap<>();
        constants.put("IsAndroid", true);
        return constants;
    }
}
