package com.dwwang.RNAdPoly;

import static com.facebook.react.bridge.UiThreadUtil.runOnUiThread;

import java.util.HashMap;
import java.util.Map;
import java.util.Locale;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;

import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.app.Activity;
import android.os.Bundle;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdLoadType;
import com.bytedance.sdk.openadsdk.TTAdManager;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAppDownloadListener;
import com.bytedance.sdk.openadsdk.TTFullScreenVideoAd;
import com.bytedance.sdk.openadsdk.TTRewardVideoAd;

import com.qq.e.ads.cfg.VideoOption;
import com.qq.e.ads.interstitial2.UnifiedInterstitialAD;
import com.qq.e.ads.interstitial2.UnifiedInterstitialADListener;
import com.qq.e.ads.interstitial2.UnifiedInterstitialMediaListener;
import com.qq.e.ads.rewardvideo.RewardVideoAD;
import com.qq.e.ads.rewardvideo.RewardVideoADListener;
import com.qq.e.ads.rewardvideo.ServerSideVerificationOptions;
import com.qq.e.comm.listeners.NegativeFeedbackListener;
import com.qq.e.comm.util.AdError;

public class RNAdPolyModule extends ReactContextBaseJavaModule {

    ReactApplicationContext context;

    private static final String TAG = "RNAdPolyModule";

    // 插屏广告
    private TTFullScreenVideoAd mttFullVideoAd;
    private UnifiedInterstitialAD gdtInterstitialAd;

    // 激励广告
    private RewardVideoAD mRewardVideoAD;
    private TTRewardVideoAd mttRewardVideoAd;
    private boolean isRewardSuccess = false;

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
    public void addListener(String eventName) {
        // Required for RN EventEmitter support
        Log.i(TAG, "addListener eventName = " + eventName);
    }

    @ReactMethod
    public void removeListeners(Integer count) {
        // Required for RN EventEmitter support
        Log.i(TAG, "removeListeners count = " + count);
    }

    @ReactMethod
    public void init(String type, final String appId, final Boolean requestPermission) {
        Log.i(TAG, "init type = " + type);
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
        Log.i(TAG, "requestPermissionIfNecessary");
        // 在合适的时机申请权限，如read_phone_state,防止获取不了imei时候，下载类广告没有填充的问题
        // 在开屏时候申请不太合适，因为该页面倒计时结束或者请求超时会跳转，在该页面申请权限，体验不好
        TTAdManagerHolder.get().requestPermissionIfNecessary(this.context);
    }

    @ReactMethod
    public void showSplash(String type, final String appKey, final String placementId) {
        Log.i(TAG, "type = " + type);
        if (type.equals("gdt")) {
            if (GDTAdManagerHolder.isInitSuccess()) {
                showGdtSplash(appKey, placementId);
            }
        } else if (type.equals("tt")) {
            if (TTAdManagerHolder.isInitSuccess()) {
                showTTSplash(placementId);
            }
        }
    }

    @ReactMethod
    public void loadFullScreenVideo(String type, String appKey, String placementId) {
        Log.i(TAG, "loadFullScreenVideo type = " + type);
        runOnUiThread(() -> {
            if (type.equals("tt")) {
                loadTTFullScreenVideo(placementId);
            } else if (type.equals("gdt")) {
                loadGdtFullScreenVideo(placementId);
            }
        });
    }

    @ReactMethod
    public void showFullScreenVideo(String type, String appKey, String placementId) {
        Log.i(TAG, "showFullScreenVideo type = " + type);
        runOnUiThread(() -> {
            if (type.equals("tt")) {
                showTTFullScreenVideo(placementId);
            } else if (type.equals("gdt")) {
                showGdtFullScreenVideo(placementId);
            }
        });
    }

    @ReactMethod
    public void loadRewardVideo(String type, String appKey, String placementId, String rewardName, int rewardAmount) {
        Log.i(TAG, "loadRewardVideo type = " + type);
        this.isRewardSuccess = false;
        runOnUiThread(() -> {
            if (type.equals("tt")) {
                loadTTRewardVideo(placementId, rewardName, rewardAmount);
            } else if (type.equals("gdt")) {
                loadGdtRewardVideo(placementId, rewardName, rewardAmount);
            }
        });
    }

    @ReactMethod
    public void showRewardVideo(String type, String appKey, String placementId, String rewardName, int rewardAmount) {
        Log.i(TAG, "showFullScreenVideo type = " + type);
        runOnUiThread(() -> {
            if (type.equals("tt")) {
                showTTRewardVideo(placementId, rewardName, rewardAmount);
            } else if (type.equals("gdt")) {
                showGdtRewardVideo(placementId, rewardName, rewardAmount);
            }
        });
    }

    private void showGdtSplash(String appKey, String placementId) {
        Log.i(TAG, "appKey = " + appKey);
        if (appKey == null || placementId == null) {
            return;
        }

        Log.i(TAG, "showGdtSplash");
        ReactApplicationContext context = getReactApplicationContext();

        Intent intent = new Intent(context, SplashActivity.class);
        intent.putExtra("appKey", appKey);
        intent.putExtra("placementId", placementId);
        intent.putExtra("is_half_size", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private void showTTSplash(String placementId) {
        Log.i(TAG, "showTTSplash placementId = " + placementId);
        if (TextUtils.isEmpty(placementId)) {
            return;
        }

        ReactApplicationContext context = getReactApplicationContext();
        Intent intent = new Intent(context, TTSplashActivity.class);
        intent.putExtra("placementId", placementId);
        intent.putExtra("is_half_size", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private void loadTTFullScreenVideo(String placementId) {
        Log.i(TAG, "loadTTFullScreenVideo placementId = " + placementId);
        TTAdManager ttAdManager = TTAdManagerHolder.get();
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
        Log.i(TAG, "showTTFullScreenVideo placementId = " + placementId);
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
        Log.i(TAG, "showGdtFullScreenVideo placementId = " + placementId);
        // ReactApplicationContext context = getReactApplicationContext();
        // Intent intent = new Intent(context, GDTInterstitialADActivity.class);
        // intent.putExtra("placementId", placementId);
        // intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // context.startActivity(intent);

        Activity currentActivity = getCurrentActivity();
        if (currentActivity == null) {
            return;
        }

        if (gdtInterstitialAd != null) {
            gdtInterstitialAd.close();
            gdtInterstitialAd.destroy();
        }

        gdtInterstitialAd = new UnifiedInterstitialAD(currentActivity, placementId,
                new UnifiedInterstitialADListener() {
                    @Override
                    public void onADReceive() {
                        // iad.setMediaListener(this);
                        // 如果支持奖励，设置ADRewardListener接收onReward回调；图文广告暂不支持奖励
                        // iad.setRewardListener(this);
                        // onADReceive之后才可调用getECPM()
                        Log.d(TAG,
                                "onADReceive eCPMLevel = " + gdtInterstitialAd.getECPMLevel() + ", ECPM: "
                                        + gdtInterstitialAd.getECPM()
                                        + ", videoduration=" + gdtInterstitialAd.getVideoDuration()
                                        + ", testExtraInfo:" + gdtInterstitialAd.getExtraInfo().get("mp")
                                        + ", request_id:" + gdtInterstitialAd.getExtraInfo().get("request_id"));

                        AdHelper.sendEvent("FullVideoAdDidSucceed", null);
                    }

                    // /**
                    // * 上报给优量汇服务端在开发者客户端竞价中优量汇的竞价结果，以便于优量汇服务端调整策略提供给开发者更合理的报价
                    // *
                    // * 优量汇竞价失败调用 sendLossNotification，并填入优量汇竞败原因（必填）、竞胜ADN ID（选填）、竞胜ADN报价（选填）
                    // * 优量汇竞价胜出调用 sendWinNotification，并填入开发者期望扣费价格（单位分）
                    // * 请开发者如实上报相关参数，以保证优量汇服务端能根据相关参数调整策略，使开发者收益最大化
                    // */
                    // private void reportBiddingResult(UnifiedInterstitialAD interstitialAD) {
                    // DemoBiddingC2SUtils.reportBiddingWinLoss(interstitialAD);
                    // if (DemoUtil.isNeedSetBidECPM()) {
                    // interstitialAD.setBidECPM(300);
                    // }
                    // }

                    @Override
                    public void onVideoCached() {
                        // 视频素材加载完成，在此时调用iad.show()或iad.showAsPopupWindow()视频广告不会有进度条。
                        Log.i(TAG, "onVideoCached");
                    }

                    @Override
                    public void onNoAD(AdError error) {
                        String msg = String.format(Locale.getDefault(), "onNoAD, error code: %d, error msg: %s",
                                error.getErrorCode(), error.getErrorMsg());
                        Log.i(TAG, "onNoAD: " + msg);
                        AdHelper.sendEvent("FullVideoAdDidFailed", null);
                    }

                    @Override
                    public void onADOpened() {
                        Log.i(TAG, "onADOpened");
                    }

                    @Override
                    public void onADExposure() {
                        Log.i(TAG, "onADExposure");
                    }

                    @Override
                    public void onADClicked() {
                        Log.i(TAG, "onADClicked");
                    }

                    @Override
                    public void onADLeftApplication() {
                        Log.i(TAG, "onADLeftApplication");
                    }

                    @Override
                    public void onADClosed() {
                        Log.i(TAG, "onADClosed");
                        AdHelper.sendEvent("FullVideoAdDidClose", null);
                    }

                    @Override
                    public void onRenderSuccess() {
                        Log.i(TAG, "onRenderSuccess，建议在此回调后再调用展示方法");
                    }

                    @Override
                    public void onRenderFail() {
                        Log.i(TAG, "onRenderFail");
                    }
                });
        gdtInterstitialAd.setNegativeFeedbackListener(new NegativeFeedbackListener() {
            @Override
            public void onComplainSuccess() {
                Log.i(TAG, "onComplainSuccess");
            }
        });
        gdtInterstitialAd.loadAD();
    }

    private void showGdtFullScreenVideo(String placementId) {
        Log.i(TAG, "showGdtFullScreenVideo placementId = " + placementId);
        if (gdtInterstitialAd != null && gdtInterstitialAd.isValid()) {
            gdtInterstitialAd.show();
        }
    }

    private void loadTTRewardVideo(String placementId, String rewardName, int rewardAmount) {
        Log.i(TAG, "loadTTRewardVideo placementId = " + placementId);

        // 模板广告需要设置期望个性化模板广告的大小,单位dp,代码位是否属于个性化模板广告，请在穿山甲平台查看
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(placementId)
                .setAdLoadType(TTAdLoadType.LOAD)
                // .setRewardName(mRewardName)
                // .setRewardAmount(mRewardAmount)
                .setOrientation(TTAdConstant.VERTICAL)
                .build();
        // step5:请求广告

        TTAdManager ttAdManager = TTAdManagerHolder.get();
        TTAdNative mTTAdNative = ttAdManager.createAdNative(this.context);
        mTTAdNative.loadRewardVideoAd(adSlot, new TTAdNative.RewardVideoAdListener() {
            @Override
            public void onError(int code, String message) {
                Log.e(TAG, "Callback --> onError: " + code + ", " + String.valueOf(message));
                AdHelper.sendEvent("RewardDidFailed", null);
            }

            // 视频广告加载后，视频资源缓存到本地的回调，在此回调后，播放本地视频，流畅不阻塞。
            @Override
            public void onRewardVideoCached() {
                Log.e(TAG, "Callback --> onRewardVideoCached");
            }

            @Override
            public void onRewardVideoCached(TTRewardVideoAd ad) {
                Log.e(TAG, "Callback --> onRewardVideoCached");
            }

            // 视频广告的素材加载完毕，比如视频url等，在此回调后，可以播放在线视频，网络不好可能出现加载缓冲，影响体验。
            @Override
            public void onRewardVideoAdLoad(TTRewardVideoAd ad) {
                Log.e(TAG, "Callback --> onRewardVideoAdLoad");

                mttRewardVideoAd = ad;
                mttRewardVideoAd.setRewardAdInteractionListener(new TTRewardVideoAd.RewardAdInteractionListener() {

                    @Override
                    public void onAdShow() {
                        Log.i(TAG, "Callback --> rewardVideoAd show");
                        // TToast.show(RewardVideoActivity.this, "rewardVideoAd show");
                    }

                    @Override
                    public void onAdVideoBarClick() {
                        Log.i(TAG, "Callback --> rewardVideoAd bar click");
                        // TToast.show(RewardVideoActivity.this, "rewardVideoAd bar click");
                    }

                    @Override
                    public void onAdClose() {
                        Log.i(TAG, "Callback --> rewardVideoAd close");
                        // TToast.show(RewardVideoActivity.this, "rewardVideoAd close");

                        WritableMap params = Arguments.createMap();
                        params.putBoolean("isEnded", isRewardSuccess);
                        AdHelper.sendEvent("RewardDidClose", params);
                    }

                    // 视频播放完成回调
                    @Override
                    public void onVideoComplete() {
                        Log.i(TAG, "Callback --> rewardVideoAd complete");
                        // TToast.show(RewardVideoActivity.this, "rewardVideoAd complete");
                    }

                    @Override
                    public void onVideoError() {
                        Log.i(TAG, "Callback --> rewardVideoAd error");
                        // TToast.show(RewardVideoActivity.this, "rewardVideoAd error");
                    }

                    // 视频播放完成后，奖励验证回调，rewardVerify：是否有效，rewardAmount：奖励梳理，rewardName：奖励名称
                    @Override
                    public void onRewardVerify(boolean rewardVerify, int rewardAmount, String rewardName, int errorCode,
                            String errorMsg) {
                        String logString = "verify:" + rewardVerify + " amount:" + rewardAmount +
                                " name:" + rewardName + " errorCode:" + errorCode + " errorMsg:" + errorMsg;
                        Log.i(TAG, "Callback --> " + logString);
                        // TToast.show(RewardVideoActivity.this, logString);
                        isRewardSuccess = rewardVerify;
                    }

                    @Override
                    public void onRewardArrived(boolean isRewardValid, int rewardType, Bundle extraInfo) {
                        // 奖励发放
                        Log.i(TAG, "Callback --> onRewardArrived");
                        isRewardSuccess = isRewardValid;
                    }

                    @Override
                    public void onSkippedVideo() {
                        Log.i(TAG, "Callback --> rewardVideoAd has onSkippedVideo");
                        // TToast.show(RewardVideoActivity.this, "rewardVideoAd has onSkippedVideo");
                    }
                });
            }
        });
    }

    private void showTTRewardVideo(String placementId, String rewardName, int rewardAmount) {
        Log.i(TAG, "showTTRewardVideo placementId = " + placementId);
        Activity activity = getCurrentActivity();
        if (mttRewardVideoAd != null && activity != null) {
            mttRewardVideoAd.showRewardVideoAd(activity);
        }
    }

    private void loadGdtRewardVideo(String placementId, String rewardName, int rewardAmount) {
        ReactApplicationContext context = getReactApplicationContext();
        mRewardVideoAD = new RewardVideoAD(context, placementId, new RewardVideoADListener() {
            /**
             * 广告加载成功，可在此回调后进行广告展示
             **/
            @Override
            public void onADLoad() {
                Log.i(TAG, "GDT RewardVideo onADLoad");
                if (mRewardVideoAD.getRewardAdType() == RewardVideoAD.REWARD_TYPE_VIDEO) {
                    Log.d(TAG, "eCPMLevel = " + mRewardVideoAD.getECPMLevel() + ", ECPM: " + mRewardVideoAD.getECPM()
                            + " ,video duration = " + mRewardVideoAD.getVideoDuration()
                            + ", testExtraInfo:" + mRewardVideoAD.getExtraInfo().get("mp")
                            + ", request_id:" + mRewardVideoAD.getExtraInfo().get("request_id"));
                } else if (mRewardVideoAD.getRewardAdType() == RewardVideoAD.REWARD_TYPE_PAGE) {
                    Log.d(TAG, "eCPMLevel = " + mRewardVideoAD.getECPMLevel()
                            + ", ECPM: " + mRewardVideoAD.getECPM()
                            + ", testExtraInfo:" + mRewardVideoAD.getExtraInfo().get("mp")
                            + ", request_id:" + mRewardVideoAD.getExtraInfo().get("request_id"));
                }
                // reportBiddingResult(mRewardVideoAD);
            }

            /**
             * 上报给优量汇服务端在开发者客户端竞价中优量汇的竞价结果，以便于优量汇服务端调整策略提供给开发者更合理的报价
             *
             * 优量汇竞价失败调用 sendLossNotification，并填入优量汇竞败原因（必填）、竞胜ADN ID（选填）、竞胜ADN报价（选填）
             * 优量汇竞价胜出调用 sendWinNotification，并填入开发者期望扣费价格（单位分）
             * 请开发者如实上报相关参数，以保证优量汇服务端能根据相关参数调整策略，使开发者收益最大化
             */
            // private void reportBiddingResult(RewardVideoAD rewardVideoAD) {
            // DemoBiddingC2SUtils.reportBiddingWinLoss(rewardVideoAD);
            // if (DemoUtil.isNeedSetBidECPM()) {
            // rewardVideoAD.setBidECPM(300);
            // }
            // }

            /**
             * 视频素材缓存成功，可在此回调后进行广告展示
             */
            @Override
            public void onVideoCached() {
                Log.i(TAG, "onVideoCached");
            }

            /**
             * 激励视频广告页面展示
             */
            @Override
            public void onADShow() {
                Log.i(TAG, "onADShow");
            }

            /**
             * 激励视频广告曝光
             */
            @Override
            public void onADExpose() {
                Log.i(TAG, "onADExpose");
            }

            /**
             * 激励视频触发激励（观看视频大于一定时长或者视频播放完毕）
             *
             * @param map 若选择了服务端验证，可以通过 ServerSideVerificationOptions#TRANS_ID 键从 map
             *            中获取此次交易的 id；若未选择服务端验证，则不需关注 map 参数。
             */
            @Override
            public void onReward(Map<String, Object> map) {
                // Log.i(TAG, "onReward " + map.get(ServerSideVerificationOptions.TRANS_ID)); //
                // 获取服务端验证的唯一 ID
                Log.i(TAG, "onReward "); // 获取服务端验证的唯一 ID
                // AdHelper.sendEvent("RewardDidSucceed", null);
                isRewardSuccess = true;
            }

            /**
             * 激励视频广告被点击
             */
            @Override
            public void onADClick() {
                Log.i(TAG, "onADClick");
            }

            /**
             * 激励视频播放完毕
             */
            @Override
            public void onVideoComplete() {
                Log.i(TAG, "onVideoComplete");
            }

            /**
             * 激励视频广告被关闭
             */
            @Override
            public void onADClose() {
                Log.i(TAG, "onADClose");

                WritableMap params = Arguments.createMap();
                params.putBoolean("isEnded", isRewardSuccess);
                AdHelper.sendEvent("RewardDidClose", params);
            }

            /**
             * 广告流程出错
             */
            @Override
            public void onError(AdError adError) {
                String msg = String.format(Locale.getDefault(), "onError, error code: %d, error msg: %s",
                        adError.getErrorCode(), adError.getErrorMsg());
                Log.i(TAG, "onError, adError=" + msg);
                AdHelper.sendEvent("RewardDidFailed", null);
            }
        }, false);
        mRewardVideoAD.setNegativeFeedbackListener(new NegativeFeedbackListener() {
            @Override
            public void onComplainSuccess() {
                Log.i(TAG, "onComplainSuccess");
            }
        });

        // 2. 加载激励视频广告
        mRewardVideoAD.loadAD();
    }

    private void showGdtRewardVideo(String placementId, String rewardName, int rewardAmount) {
        Log.i(TAG, "showGdtRewardVideo placementId = " + placementId);
        if (mRewardVideoAD != null && !mRewardVideoAD.hasShown() && mRewardVideoAD.isValid()) {
            // 广告展示检查2：当前广告数据还没有展示过
            // 广告展示检查3：展示广告前判断广告数据未过期
            mRewardVideoAD.showAD();
        }
    }

    @Override
    public Map<String, Object> getConstants() {
        final Map<String, Object> constants = new HashMap<>();
        constants.put("IsAndroid", true);
        return constants;
    }
}
