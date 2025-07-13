package com.dwwang.RNAdPoly;

import static com.facebook.react.bridge.UiThreadUtil.runOnUiThread;

import java.util.HashMap;
import java.util.Map;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.app.Activity;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdLoadType;
import com.bytedance.sdk.openadsdk.TTAdManager;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAppDownloadListener;
import com.bytedance.sdk.openadsdk.TTFullScreenVideoAd;

public class RNAdPolyModule extends ReactContextBaseJavaModule {

    ReactApplicationContext context;

    private static final String TAG = "RNAdPolyModule";
    private TTFullScreenVideoAd mttFullVideoAd;

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
    public void init(String type, final String appId, final Boolean requestPermission) {
        Log.i("AD_DEMO", "init type = " + type);
        if (type.equals("gdt")) {
            GDTAdManagerHolder.init(context, appId);
        } else if (type.equals("tt")) {
            runOnUiThread(() -> {
                TTAdManagerHolder.init(context, appId, requestPermission);
            });
        }
    }

    @ReactMethod
    public void requestPermissionIfNecessary() {
        Log.i("AD_DEMO", "requestPermissionIfNecessary");
        // 在合适的时机申请权限，如read_phone_state,防止获取不了imei时候，下载类广告没有填充的问题
        // 在开屏时候申请不太合适，因为该页面倒计时结束或者请求超时会跳转，在该页面申请权限，体验不好
        TTAdManagerHolder.get().requestPermissionIfNecessary(this.context);
    }

    @ReactMethod
    public void showSplash(String type, final String appKey, final String placementId) {
        Log.i("AD_DEMO", "type = " + type);
        if (type.equals("gdt")) {
            if (!GDTAdManagerHolder.isInitSuccess()) {
                GDTAdManagerHolder.init(context, appKey);
            }

            showGdtSplash(appKey, placementId);
        } else if (type.equals("tt")) {
            if (TTAdManagerHolder.isInitSuccess()) {
                showTTSplash(placementId);
            } else {
                runOnUiThread(() -> {
                    TTAdManagerHolder.init(context, appKey, true);
                });
            }
        }
    }

    @ReactMethod
    public void loadFullScreenVideo(String type, String appKey, String placementId) {
        Log.i("AD_DEMO", "loadFullScreenVideo type = " + type);
        // if (type.equals("tt")) {
        // showTTFullScreenVideo(placementId);
        // }
    }

    @ReactMethod
    public void showFullScreenVideo(String type, String appKey, String placementId) {
        Log.i("AD_DEMO", "showFullScreenVideo type = " + type);
        if (type.equals("tt")) {
            showTTFullScreenVideo(placementId);
        } else if (type.equals("gdt")) {
            showGdtFullScreenVideo(placementId);
        }
    }

    @ReactMethod
    public void loadRewardVideo(String type, String appKey, String placementId, String rewardName, int rewardAmount) {
        Log.i("AD_DEMO", "loadRewardVideo type = " + type);
        // if (type.equals("tt")) {
        // showTTFullScreenVideo(placementId);
        // }
    }

    @ReactMethod
    public void showRewardVideo(String type, String appKey, String placementId, String rewardName, int rewardAmount) {
        Log.i("AD_DEMO", "showFullScreenVideo type = " + type);
        if (type.equals("tt")) {
            showTTRewardVideo(placementId, rewardName, rewardAmount);
        } else if (type.equals("gdt")) {
            showGdtRewardVideo(placementId, rewardName, rewardAmount);
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

    private void loadTTFullScreenVideo(String placementId) {
        Log.i("AD_DEMO", "loadTTFullScreenVideo placementId = " + placementId);
        TTAdManager ttAdManager = TTAdManagerHolder.get();
        // step2:(可选，强烈建议在合适的时机调用):申请部分权限，如read_phone_state,防止获取不了imei时候，下载类广告没有填充的问题。
        TTAdManagerHolder.get().requestPermissionIfNecessary(this.context);
        // step3:创建TTAdNative对象,用于调用广告请求接口
        TTAdNative mTTAdNative = ttAdManager.createAdNative(this.context);
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(placementId)
                .setSupportDeepLink(true)
                .setOrientation(TTAdConstant.VERTICAL)// 必填参数，期望视频的播放方向：TTAdConstant.HORIZONTAL 或 TTAdConstant.VERTICAL
                .build();
        // step5:请求广告
        mTTAdNative.loadFullScreenVideoAd(adSlot, new TTAdNative.FullScreenVideoAdListener() {
            @Override
            public void onError(int code, String message) {
                Log.i(TAG, "Callback --> onError: " + code + ", " + String.valueOf(message));
                AdHelper.sendEvent("FullVideoAdDidFailed", null);
            }

            @Override
            public void onFullScreenVideoAdLoad(TTFullScreenVideoAd ad) {
                Log.i(TAG, "FullVideoAd loaded  广告类型：" + ad.getFullVideoAdType());

                mttFullVideoAd = ad;
                mttFullVideoAd.setFullScreenVideoAdInteractionListener(
                        new TTFullScreenVideoAd.FullScreenVideoAdInteractionListener() {

                            @Override
                            public void onAdShow() {
                                Log.i(TAG, "Callback --> FullVideoAd show");
                            }

                            @Override
                            public void onAdVideoBarClick() {
                                Log.i(TAG, "Callback --> FullVideoAd bar click");
                            }

                            @Override
                            public void onAdClose() {
                                Log.i(TAG, "Callback --> FullVideoAd close");
                                AdHelper.sendEvent("FullVideoAdDidClose", null);
                            }

                            @Override
                            public void onVideoComplete() {
                                Log.i(TAG, "Callback --> FullVideoAd complete");
                            }

                            @Override
                            public void onSkippedVideo() {
                                Log.i(TAG, "Callback --> FullVideoAd skipped");
                            }

                        });
            }

            @Override
            public void onFullScreenVideoCached() {
            }

            @Override
            public void onFullScreenVideoCached(TTFullScreenVideoAd ad) {
                Log.i(TAG, "Callback --> onFullScreenVideoCached");
                AdHelper.sendEvent("FullVideoAdDidSucceed", null);
            }
        });
    }

    private void showTTFullScreenVideo(String placementId) {
        Log.i("AD_DEMO", "showTTFullScreenVideo placementId = " + placementId);
        // ReactApplicationContext context = getReactApplicationContext();
        // Intent intent = new Intent(context, TTFullScreenVideoActivity.class);
        // intent.putExtra("placementId", placementId);
        // intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // context.startActivity(intent);
        Activity activity = getCurrentActivity();
        if (mttFullVideoAd != null && activity != null) {
            mttFullVideoAd.showFullScreenVideoAd(activity);
            mttFullVideoAd = null;
        }
    }

    private void loadGdtFullScreenVideo(String placementId) {
        Log.i("AD_DEMO", "showGdtFullScreenVideo placementId = " + placementId);
        ReactApplicationContext context = getReactApplicationContext();
        Intent intent = new Intent(context, GDTInterstitialADActivity.class);
        intent.putExtra("placementId", placementId);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private void showGdtFullScreenVideo(String placementId) {
        Log.i("AD_DEMO", "showGdtFullScreenVideo placementId = " + placementId);
        ReactApplicationContext context = getReactApplicationContext();
        Intent intent = new Intent(context, GDTInterstitialADActivity.class);
        intent.putExtra("placementId", placementId);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private void showTTRewardVideo(String placementId, String rewardName, int rewardAmount) {
        Log.i("AD_DEMO", "showTTRewardVideo placementId = " + placementId);
        ReactApplicationContext context = getReactApplicationContext();
        Intent intent = new Intent(context, TTRewardVideoActivity.class);
        intent.putExtra("placementId", placementId);
        intent.putExtra("rewardName", rewardName);
        intent.putExtra("rewardAmount", rewardAmount);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private void showGdtRewardVideo(String placementId, String rewardName, int rewardAmount) {
        Log.i("AD_DEMO", "showGdtRewardVideo placementId = " + placementId);
        ReactApplicationContext context = getReactApplicationContext();
        Intent intent = new Intent(context, GDTRewardVideoActivity.class);
        intent.putExtra("placementId", placementId);
        intent.putExtra("rewardName", rewardName);
        intent.putExtra("rewardAmount", rewardAmount);
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
