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
import android.content.Context;
import android.content.pm.PackageInfo;
import android.location.Location;
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

import com.kwad.sdk.api.KsAdSDK;
import com.kwad.sdk.api.KsInnerAd;
import com.kwad.sdk.api.KsCustomController;
import com.kwad.sdk.api.KsFullScreenVideoAd;
import com.kwad.sdk.api.KsInitCallback;
import com.kwad.sdk.api.KsRewardVideoAd;
import com.kwad.sdk.api.KsInterstitialAd;
import com.kwad.sdk.api.KsLoadManager;
import com.kwad.sdk.api.KsScene;
import com.kwad.sdk.api.KsVideoPlayConfig;
import com.kwad.sdk.api.SdkConfig;
import com.kwad.sdk.api.model.RewardTaskType;

import com.sigmob.windad.OnInitializationListener;
import com.sigmob.windad.OnStartListener;
import com.sigmob.windad.WindAdError;
import com.sigmob.windad.WindAdOptions;
import com.sigmob.windad.WindAds;
import com.sigmob.windad.WindAgeRestrictedUserStatus;
import com.sigmob.windad.WindConsentStatus;
import com.sigmob.windad.WindCustomController;
import com.sigmob.windad.newInterstitial.WindNewInterstitialAd;
import com.sigmob.windad.newInterstitial.WindNewInterstitialAdListener;
import com.sigmob.windad.newInterstitial.WindNewInterstitialAdRequest;
import com.sigmob.windad.rewardVideo.WindRewardAdRequest;
import com.sigmob.windad.rewardVideo.WindRewardInfo;
import com.sigmob.windad.rewardVideo.WindRewardVideoAd;
import com.sigmob.windad.rewardVideo.WindRewardVideoAdListener;

import com.baidu.mobads.sdk.api.AdSettings;
import com.baidu.mobads.sdk.api.BDAdConfig;
import com.baidu.mobads.sdk.api.BDDialogParams;
import com.baidu.mobads.sdk.api.ExpressInterstitialAd;
import com.baidu.mobads.sdk.api.ExpressInterstitialListener;
import com.baidu.mobads.sdk.api.MobadsPermissionSettings;

import com.tencent.bugly.crashreport.CrashReport;

import java.util.ArrayList;
import java.util.List;

public class RNAdPolyModule extends ReactContextBaseJavaModule {

    ReactApplicationContext context;

    private static final String TAG = "RNAdPolyModule";

    // 插屏广告
    private TTFullScreenVideoAd mttFullVideoAd;
    private UnifiedInterstitialAD gdtInterstitialAd;
    private KsInterstitialAd mKsInterstitialAd;
    private WindNewInterstitialAd windNewInterstitialAd;
    private ExpressInterstitialAd baiduInterstitialAd;

    // 激励广告
    private RewardVideoAD mRewardVideoAD;
    private TTRewardVideoAd mttRewardVideoAd;
    private KsRewardVideoAd mKsRewardVideoAd;
    private WindRewardVideoAd windRewardedVideoAd;
    private boolean isRewardSuccess = false;

    public final String APP_NAME = "口袋五线谱";
    public final String BUGLY_APP_ID = "7c356cab77";
    public final String WEIXIN_OPEN_APP_ID = "wxa8bfec0590b522cd";

    public RNAdPolyModule(ReactApplicationContext context) {
        super(context);
        this.context = context;
        AdHelper.reactContext = context;

        CrashReport.initCrashReport(getReactApplicationContext(), BUGLY_APP_ID, BuildConfig.DEBUG);
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
    public void init(String type, final String appId, final String appKey, final Boolean requestPermission) {
        Log.i(TAG, "init type = " + type);
        runOnUiThread(() -> {
            switch (type) {
                case "gdt":
                    GDTAdManagerHolder.init(context, appId);
                    break;
                case "tt":
                    TTAdManagerHolder.init(context, appId, requestPermission);
                    break;
                case "ks":
                    initKsAdSDK(appId);
                    break;
                case "baidu":
                    initBaidu(appId);
                    break;
                case "sigmob":
                    initSigmobSDK(appId, appKey);
                    break;
                default:
                    break;
            }
        });
    }

    @ReactMethod
    public void requestPermissionIfNecessary() {
        Log.i(TAG, "requestPermissionIfNecessary");
        // 在合适的时机申请权限，如read_phone_state,防止获取不了imei时候，下载类广告没有填充的问题
        // 在开屏时候申请不太合适，因为该页面倒计时结束或者请求超时会跳转，在该页面申请权限，体验不好
        TTAdManagerHolder.get().requestPermissionIfNecessary(this.context);
    }

    @ReactMethod
    public void showSplash(String type, final String placementId) {
        Log.i(TAG, "showSplash type = " + type);
        switch (type) {
            case "gdt":
                showGdtSplash(placementId);
                break;
            case "tt":
                showTTSplash(placementId);
                break;
            case "ks":
                showKsSplash(placementId);
                break;
            default:
                break;
        }
    }

    @ReactMethod
    public void loadInterAd(String type, String placementId) {
        Log.i(TAG, "loadInterAd type = " + type);
        runOnUiThread(() -> {
            switch (type) {
                case "tt":
                    loadTTInterAd(placementId);
                    break;
                case "gdt":
                    loadGdtInterAd(placementId);
                    break;
                case "ks":
                    loadKSInterAd(placementId);
                    break;
                case "baidu":
                    loadBaiduInterAd(placementId);
                    break;
                case "sigmob":
                    loadSigmobInterAd(placementId);
                    break;
                default:
                    break;
            }
        });
    }

    @ReactMethod
    public void showInterAd(String type) {
        Log.i(TAG, "showInterAd type = " + type);
        runOnUiThread(() -> {
            switch (type) {
                case "tt":
                    showTTInterAd();
                    break;
                case "gdt":
                    showGdtInterAd();
                    break;
                case "ks":
                    showKSInterAd();
                    break;
                case "baidu":
                    showBaiduInterAd();
                    break;
                case "sigmob":
                    showSigmobInterAd();
                    break;
                default:
                    break;
            }
        });
    }

    @ReactMethod
    public void loadRewardVideo(String type, String placementId, String rewardName, int rewardAmount) {
        Log.i(TAG, "loadRewardVideo type = " + type);
        this.isRewardSuccess = false;
        runOnUiThread(() -> {
            switch (type) {
                case "tt":
                    loadTTRewardVideo(placementId, rewardName, rewardAmount);
                    break;
                case "gdt":
                    loadGdtRewardVideo(placementId, rewardName, rewardAmount);
                    break;
                case "ks":
                    loadKsRewardVideo(placementId, rewardName, rewardAmount);
                    break;
                case "sigmob":
                    loadSigmobRewardVideo(placementId, rewardName, rewardAmount);
                    break;
                default:
                    break;
            }
        });
    }

    @ReactMethod
    public void showRewardVideo(String type, String placementId, String rewardName, int rewardAmount) {
        Log.i(TAG, "showRewardVideo type = " + type);
        runOnUiThread(() -> {
            switch (type) {
                case "tt":
                    showTTRewardVideo(placementId, rewardName, rewardAmount);
                    break;
                case "gdt":
                    showGdtRewardVideo(placementId, rewardName, rewardAmount);
                    break;
                case "ks":
                    showKsRewardVideo(placementId, rewardName, rewardAmount);
                    break;
                case "sigmob":
                    showSigmobRewardVideo(placementId, rewardName, rewardAmount);
                    break;
                default:
                    break;
            }
        });
    }

    private void initKsAdSDK(String appId) {
        Activity currentActivity = getCurrentActivity();
        if (currentActivity == null) {
            return;
        }

        KsAdSDK.init(currentActivity, new SdkConfig.Builder()
                .appId(appId) // 测试aapId，请联系快手平台申请正式AppId，必填
                .appName(APP_NAME) // 测试appName，请填写您应用的名称，非必填
                .showNotification(true) // 是否展示下载通知栏，非必填
                .debug(BuildConfig.DEBUG)
                .setInitCallback(new KsInitCallback() {
                    @Override
                    public void onSuccess() {
                        Log.i(TAG, "KsAdSDK onSuccess");
                        // PermissionUtil.handlePermission(AppActivity.this, isFirstRequestPermission);
                        KsAdSDK.start();
                    }

                    @Override
                    public void onFail(int code, String msg) {
                        Log.i(TAG, "init fail code:" + code + "--msg:" + msg);
                    }
                }).setStartCallback(new KsInitCallback() {
                    @Override
                    public void onSuccess() {
                        Log.i(TAG, "ks start success");
                        WritableMap params = Arguments.createMap();
                        params.putString("type", "ks");
                        AdHelper.sendEvent("AdInitSuccess", params);
                    }

                    @Override
                    public void onFail(int code, String msg) {
                        Log.i(TAG, "start fail msg: " + msg);
                    }
                })
                .build());

    }

    private void initBaidu(String appId) {
        Activity currentActivity = getCurrentActivity();
        if (currentActivity == null) {
            return;
        }

        final BDAdConfig bdAdConfig = new BDAdConfig.Builder()
                // 1、设置app名称，可选
                .setAppName(APP_NAME)
                // 2、应用在mssp平台申请到的appsid，和包名一一对应，此处设置等同于在AndroidManifest.xml里面设置
                .setAppsid(appId)
                .setBDAdInitListener(new BDAdConfig.BDAdInitListener() {
                    @Override
                    public void success() {
                        Log.i(TAG, "baidu init success");
                        WritableMap params = Arguments.createMap();
                        params.putString("type", "baidu");
                        AdHelper.sendEvent("AdInitSuccess", params);
                    }

                    @Override
                    public void fail() {
                        Log.i(TAG, "MobadsApplication SDK初始化失败");
                    }
                })
                // 3、设置下载弹窗的类型和按钮动效样式，可选
                .setDialogParams(new BDDialogParams.Builder()
                        .setDlDialogType(BDDialogParams.TYPE_BOTTOM_POPUP)
                        .setDlDialogAnimStyle(BDDialogParams.ANIM_STYLE_NONE)
                        .build())
                // 4、设置微信openSDK 应用id
                .setWXAppid(WEIXIN_OPEN_APP_ID)
                // 5.媒体debug日志调试开关 调试阶段打开，上线前需关闭
                .setDebug(BuildConfig.DEBUG)
                .build(currentActivity);
        bdAdConfig.init();

        // 合规设置，设置APP的ICON资源，系统通知使用
        // AdSettings.setNotificationIcon(R.mipmap.ic_launcher);

        MobadsPermissionSettings.setPermissionReadDeviceID(true);
        MobadsPermissionSettings.setPermissionAppList(true);
        MobadsPermissionSettings.setPermissionLocation(true);
        MobadsPermissionSettings.setPermissionStorage(true);
    }

    private void initSigmobSDK(String appId, String appKey) {
        WindAds ads = WindAds.sharedAds();
        ads.setDebugEnable(BuildConfig.DEBUG);
        // WindAds.setOAIDCertPem(loadOaidCertPem(this));

        ads.setUserAge(18);
        /*
         * 是否成年
         * true-成年；false-未成年；默认值为 true
         */
        ads.setAdult(true);
        /*
         * 是否开启个性化推荐接口
         * true-开启；false-关闭；默认值为 true
         */
        ads.setPersonalizedAdvertisingOn(true);
        /*
         * 是否允许使用传感器
         * true-开启；false-关闭；默认值为 true
         */
        ads.setSensorStatus(true);
        // coppa，是否年龄限制
        ads.setIsAgeRestrictedUser(WindAgeRestrictedUserStatus.NO);
        // 是否接受 gdpr 协议
        ads.setUserGDPRConsentStatus(WindConsentStatus.ACCEPT);

        WindAdOptions windAdOptions = new WindAdOptions(appId, appKey);
        // 设置自定义设备信息，允许开发者自行传入设备及用户信息
        windAdOptions.setCustomController(new WindCustomController() {
            /**
             * 是否允许 SDK 主动获取地理位置信息
             *
             * @return true 可以获取，false 禁止获取。默认为 true
             */
            @Override
            public boolean isCanUseLocation() {
                return true;
            }

            /**
             * 当 isCanUseLocation=false 时，Sigmob 使用开发者传入的地理位置信息
             *
             * @return 地理位置参数或者 null
             */
            @Override
            public Location getLocation() {
                return null;
            }

            /**
             * 是否允许 SDK 主动获取 IMEI
             *
             * @return true 可以使用，false 禁止使用。默认为 true
             */
            @Override
            public boolean isCanUsePhoneState() {
                return true;
            }

            /**
             * 当 isCanUsePhoneState=false 时，Sigmob 使用开发者传入的 IMEI 信息
             *
             * @return IMEI 或者 null
             */
            @Override
            public String getDevImei() {
                return null;
            }

            /**
             * 是否允许 SDK 主动获取 OAID
             *
             * @return true 可以使用，false 禁止使用。默认为 true
             */
            @Override
            public boolean isCanUseOaid() {
                return true;
            }

            /**
             * 当 isCanUseOaid=false 时，Sigmob 使用开发者传入的 OAID 信息
             *
             * @return OAID 或者 null
             */
            @Override
            public String getDevOaid() {
                return null;
            }

            /**
             * 是否允许 SDK 主动获取 AndroidId
             *
             * @return true 可以使用，false 禁止使用。默认为 true
             */
            @Override
            public boolean isCanUseAndroidId() {
                return true;
            }

            /**
             * isCanUseAndroidId=false 时，Sigmob 使用开发者传入的 AndroidId 信息
             *
             * @return AndroidId 或者 null
             */
            @Override
            public String getAndroidId() {
                return null;
            }

            /**
             * 是否允许 SDK 查询已安装应用列表
             *
             * @return true 可以使用，false 禁止使用
             */
            @Override
            public boolean isCanUseAppList() {
                return true;
            }

            /**
             * isCanUseAppList=false 时，Sigmob 使用开发者传入的已安装应用列表信息
             *
             * @return 应用列表或者 null
             */
            @Override
            public List<PackageInfo> getInstallPackageInfoList() {
                // Context applicationContext = getReactApplicationContext();
                // List<PackageInfo> result =
                // MainActivity.this.getInstallPackageInfoList(applicationContext);
                // Log.d(TAG, "getInstallPackageInfoList: result = " + result);
                return new ArrayList<>();
            }

            /**
             * 是否允许 SDK 查询运营商编码（4.22.0 版本新增）
             *
             * @return true 可以使用，false 禁止使用
             */
            @Override
            public boolean isCanUseSimOperator() {
                return true;
            }

            /**
             * isCanUseSimOperator=false 时，Sigmob 使用开发者传入的运营商编码，例如：46000（4.22.0 版本新增）
             */
            @Override
            public String getDevSimOperatorCode() {
                return null;
            }

            /**
             * isCanUseSimOperator=false 时，Sigmob 使用开发者传入的运营商名称，例如：中国移动（4.22.0 版本新增）
             */
            @Override
            public String getDevSimOperatorName() {
                return null;
            }
        });

        // SDK 初始化
        ads.init(getReactApplicationContext(), windAdOptions, new OnInitializationListener() {
            @Override
            public void onInitializationSuccess() {
                Log.i(TAG, "Sigmob initSDK#onInitializationSuccess");
            }

            @Override
            public void onInitializationFail(String error) {
                Log.e(TAG, "Sigmob initSDK#onInitializationFail: error =" + error);
            }
        });

        // SDK 启动
        ads.start(new OnStartListener() {
            @Override
            public void onStartSuccess() {
                Log.i(TAG, "Sigmob initSDK#OnStartSuccess");
                WritableMap params = Arguments.createMap();
                params.putString("type", "sigmob");
                AdHelper.sendEvent("AdInitSuccess", params);
            }

            @Override
            public void onStartFail(String error) {
                Log.e(TAG, "Sigmob initSDK#OnStartSuccess: error =" + error);
            }
        });
    }

    private void showGdtSplash(String placementId) {
        Log.i(TAG, "showGdtSplash placementId = " + placementId);
        if (placementId == null) {
            return;
        }

        ReactApplicationContext context = getReactApplicationContext();

        Intent intent = new Intent(context, SplashActivity.class);
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

    private void showKsSplash(String placementId) {
        Log.i(TAG, "showKSplash placementId = " + placementId);
        if (TextUtils.isEmpty(placementId)) {
            return;
        }

        ReactApplicationContext context = getReactApplicationContext();
        Intent intent = new Intent(context, KSSplashActivity.class);
        intent.putExtra("placementId", placementId);
        intent.putExtra("is_half_size", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    private void loadTTInterAd(String placementId) {
        Log.i(TAG, "loadTTFullScreenVideo placementId = " + placementId);
        TTAdManager ttAdManager = TTAdManagerHolder.get();
        TTAdNative mTTAdNative = ttAdManager.createAdNative(this.context);
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(placementId)
                .setSupportDeepLink(true)
                .setOrientation(TTAdConstant.VERTICAL)// 必填参数，期望视频的播放方向：TTAdConstant.HORIZONTAL 或 TTAdConstant.VERTICAL
                .build();
        if (mTTAdNative == null) {
            return;
        }
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

    private void showTTInterAd() {
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

    private void loadGdtInterAd(String placementId) {
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

    private void showGdtInterAd() {
        if (gdtInterstitialAd != null && gdtInterstitialAd.isValid()) {
            gdtInterstitialAd.show();
        }
    }

    private void loadKSInterAd(String placementId) {
        KsScene.Builder builder = new KsScene.Builder(Long.valueOf(placementId).longValue());
        KsAdSDK.getLoadManager().loadInterstitialAd(builder.build(),
                new KsLoadManager.InterstitialAdListener() {
                    @Override
                    public void onError(int code, String msg) {
                        Log.i(TAG, "KSInterAd Callback --> onError: " + code + ", " + msg);
                        AdHelper.sendEvent("FullVideoAdDidFailed", null);
                    }

                    @Override
                    public void onRequestResult(int adNumber) {

                    }

                    @Override
                    public void onInterstitialAdLoad(List<KsInterstitialAd> adList) {
                        Log.i(TAG, "KSInterstitial onInterstitialAdLoad");
                        if (adList != null && adList.size() > 0) {
                            mKsInterstitialAd = adList.get(0);
                            AdHelper.sendEvent("FullVideoAdDidSucceed", null);
                        }
                    }
                });
    }

    private void showKSInterAd() {
        Activity currentActivity = getCurrentActivity();
        if (currentActivity == null) {
            return;
        }

        if (mKsInterstitialAd != null) {
            mKsInterstitialAd
                    .setAdInteractionListener(new KsInterstitialAd.AdInteractionListener() {
                        @Override
                        public void onAdClicked() {
                        }

                        @Override
                        public void onAdShow() {
                        }

                        @Override
                        public void onAdClosed() {
                        }

                        @Override
                        public void onPageDismiss() {
                        }

                        @Override
                        public void onVideoPlayError(int code, int extra) {
                        }

                        @Override
                        public void onVideoPlayEnd() {
                        }

                        @Override
                        public void onVideoPlayStart() {
                        }

                        @Override
                        public void onSkippedAd() {
                        }

                    });
            KsVideoPlayConfig videoPlayConfig = new KsVideoPlayConfig.Builder()
                    // .videoSoundEnable(!mVideoSoundSwitch.isChecked())
                    .build();
            mKsInterstitialAd.showInterstitialAd(currentActivity, videoPlayConfig);
        }
    }

    private void loadSigmobInterAd(String placementId) {
        if (windNewInterstitialAd == null) {
            Map<String, Object> options = new HashMap<>();
            windNewInterstitialAd = new WindNewInterstitialAd(
                    new WindNewInterstitialAdRequest(placementId, "", options));
            windNewInterstitialAd.setWindNewInterstitialAdListener(new WindNewInterstitialAdListener() {
                @Override
                public void onInterstitialAdLoadSuccess(final String placementId) {
                    Log.d(TAG, "------onInterstitialAdLoadSuccess------" + placementId);
                    // Toast.makeText(InterstitialActivity.this, "onInterstitialAdLoadSuccess",
                    // Toast.LENGTH_SHORT).show();
                    AdHelper.sendEvent("FullVideoAdDidSucceed", null);
                }

                @Override
                public void onInterstitialAdPreLoadSuccess(String s) {
                    Log.d(TAG, "------onInterstitialAdPreLoadSuccess------");
                    // Toast.makeText(InterstitialActivity.this, "onInterstitialAdLoadSuccess",
                    // Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onInterstitialAdPreLoadFail(String s) {
                    Log.d(TAG, "------onInterstitialAdPreLoadFail------");
                    // Toast.makeText(InterstitialActivity.this, "onInterstitialAdLoadSuccess",
                    // Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onInterstitialAdShow(final String placementId) {
                    Log.d(TAG, "------onInterstitialAdShow------" + placementId);
                }

                @Override
                public void onInterstitialAdClicked(final String placementId) {
                    Log.d(TAG, "------onInterstitialAdClicked------" + placementId);
                }

                @Override
                public void onInterstitialAdClosed(final String placementId) {
                    Log.d(TAG, "------onInterstitialAdClosed------" + placementId);
                }

                @Override
                public void onInterstitialAdLoadError(final WindAdError error, final String placementId) {
                    Log.d(TAG, "------onInterstitialAdLoadError------" + error.toString() + ":" + placementId);
                    AdHelper.sendEvent("FullVideoAdDidFailed", null);
                }

                @Override
                public void onInterstitialAdShowError(final WindAdError error, final String placementId) {
                    Log.d(TAG, "------onInterstitialAdShowError------" + error.toString() + ":" + placementId);
                }
            });
        }
        windNewInterstitialAd.loadAd();
    }

    private void showSigmobInterAd() {
        if (windNewInterstitialAd == null) {
            return;
        }
        HashMap<String, String> option = new HashMap<>();
        option.put(WindAds.AD_SCENE_ID, "1");
        option.put(WindAds.AD_SCENE_DESC, "游戏完成");
        if (windNewInterstitialAd != null && windNewInterstitialAd.isReady()) {
            windNewInterstitialAd.show(option);
        }
    }

    private void loadBaiduInterAd(String placementId) {
        baiduInterstitialAd = new ExpressInterstitialAd(getReactApplicationContext(), placementId);
        baiduInterstitialAd.setLoadListener(new ExpressInterstitialListener() {
            @Override
            public void onADLoaded() {
                Log.e(TAG, "Baidu onADLoaded");
                AdHelper.sendEvent("FullVideoAdDidSucceed", null);
            }

            @Override
            public void onAdClick() {
                Log.e(TAG, "Baidu onAdClick");
            }

            @Override
            public void onAdClose() {
                Log.e(TAG, "Baidu onAdClose");
            }

            @Override
            public void onAdFailed(int errorCode, String message) {
                Log.e(TAG, "Baidu onLoadFail reason:" + message + "errorCode:" + errorCode);
            }

            @Override
            public void onNoAd(int errorCode, String message) {
                Log.e(TAG, "Baidu onNoAd reason:" + message + "errorCode:" + errorCode);
                AdHelper.sendEvent("FullVideoAdDidFailed", null);
            }

            @Override
            public void onADExposed() {
                Log.e(TAG, "Baidu onADExposed");
            }

            @Override
            public void onADExposureFailed() {
                Log.e(TAG, "Baidu onADExposureFailed");
            }

            @Override
            public void onAdCacheSuccess() {
                Log.e(TAG, "Baidu onAdCacheSuccess");
            }

            @Override
            public void onAdCacheFailed() {
                Log.e(TAG, "Baidu onAdCacheFailed");
            }

            @Override
            public void onLpClosed() {
                Log.e(TAG, "Baidu onLpClosed");
            }
        });
        // baiduInterstitialAd.setDownloadListener(adDownloadListener);
        // 设置下载弹窗，默认为false
        baiduInterstitialAd.setDialogFrame(false);
        // 【非必要】设置传参
        // baiduInterstitialAd.setRequestParameters(requestParameters);
        baiduInterstitialAd.load();
    }

    private void showBaiduInterAd() {
        Activity currentActivity = getCurrentActivity();
        if (currentActivity == null) {
            return;
        }

        if (baiduInterstitialAd != null && baiduInterstitialAd.isReady()) {
            baiduInterstitialAd.show(currentActivity);
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

    public void loadKsRewardVideo(String placementId, String rewardName, int rewardAmount) {
        KsScene.Builder builder = null;
        try {
            builder = new KsScene.Builder(Long.valueOf(placementId).longValue());
        } catch (Throwable e) {
            e.printStackTrace();
        }

        if (builder == null) {
            return;
        }

        builder.screenOrientation(SdkConfig.SCREEN_ORIENTATION_PORTRAIT);
        KsScene scene = builder.build();

        final long startTime = System.currentTimeMillis();
        KsAdSDK.getLoadManager()
                .loadRewardVideoAd(scene, new KsLoadManager.RewardVideoAdListener() {
                    @Override
                    public void onError(int code, String msg) {
                        Log.i(TAG, "Callback --> onError: " + code + ", " + msg);
                        AdHelper.sendEvent("RewardDidFailed", null);
                    }

                    @Override
                    public void onRewardVideoResult(List<KsRewardVideoAd> adList) {
                        // 视频广告的数据加载完毕，在此回调后，可以播放在线视频，网络不好可能出现加载缓冲，影响体验。
                        Log.i(TAG, "Callback --> onRewardVideoResult time: "
                                + (System.currentTimeMillis() - startTime));
                        if (adList != null && adList.size() > 0) {
                            mKsRewardVideoAd = adList.get(0);
                        }
                    }

                    @Override
                    public void onRewardVideoAdLoad(List<KsRewardVideoAd> adList) {
                        // 视频广告的数据加载和资源缓存完毕，在此回调后，播放本地视频，流畅不阻塞。
                        Log.i(TAG, "Callback --> onRewardVideoAdLoad time: "
                                + (System.currentTimeMillis() - startTime));
                        if (adList != null && adList.size() > 0) {
                            mKsRewardVideoAd = adList.get(0);
                        }
                    }
                });
    }

    // 2.展示激励视频，可以竖屏展示也可以横屏展示，建议与当前屏幕方向一致
    private void showKsRewardVideo(String placementId, String rewardName, int rewardAmount) {
        Activity activity = getCurrentActivity();
        if (activity == null || mKsRewardVideoAd == null) {
            return;
        }

        mKsRewardVideoAd.setInnerAdInteractionListener(
                new KsInnerAd.KsInnerAdInteractionListener() {
                    @Override
                    public void onAdClicked(KsInnerAd ksInnerAd) {
                        Log.i(TAG, "激励视频内部广告点击：" + ksInnerAd.getType());
                    }

                    @Override
                    public void onAdShow(KsInnerAd ksInnerAd) {
                        Log.i(TAG, "激励视频内部广告曝光：" + ksInnerAd.getType());
                    }
                });

        mKsRewardVideoAd
                .setRewardAdInteractionListener(new KsRewardVideoAd.RewardAdInteractionListener() {
                    @Override
                    public void onAdClicked() {
                        Log.i(TAG, "激励视频广告点击");
                    }

                    @Override
                    public void onPageDismiss() {
                        Log.i(TAG, "激励视频广告关闭");

                        WritableMap params = Arguments.createMap();
                        params.putBoolean("isEnded", isRewardSuccess);
                        AdHelper.sendEvent("RewardDidClose", params);
                    }

                    @Override
                    public void onVideoPlayError(int code, int extra) {
                        Log.i(TAG, "激励视频广告播放出错");
                    }

                    @Override
                    public void onVideoPlayEnd() {
                        Log.i(TAG, "激励视频广告播放完成");
                    }

                    @Override
                    public void onVideoSkipToEnd(long playDuration) {
                        Log.i(TAG, "激励视频广告跳过播放完成");
                    }

                    @Override
                    public void onVideoPlayStart() {
                        Log.i(TAG, "激励视频广告播放开始");
                    }

                    /**
                     * 激励视频广告激励回调，只会回调一次
                     */
                    @Override
                    public void onRewardVerify() {
                        Log.i(TAG, "激励视频广告获取激励");
                        isRewardSuccess = true;
                    }

                    @Override
                    public void onRewardVerify(Map<String, Object> extraMap) {
                        Log.i(TAG, "激励视频广告获取激励");
                        isRewardSuccess = true;
                    }

                    /**
                     * 视频激励分阶段回调
                     * 
                     * @param taskType          当前激励视频所属任务类型
                     *                          RewardTaskType.LOOK_VIDEO 观看视频类型 属于浅度奖励类型
                     *                          RewardTaskType.LOOK_LANDING_PAGE 浏览落地页N秒类型 属于深度奖励类型
                     *                          RewardTaskType.USE_APP 下载使用App N秒类型 属于深度奖励类型
                     * @param currentTaskStatus 当前所完成任务类型，@RewardTaskType中之一
                     */
                    @Override
                    public void onRewardStepVerify(int taskType, int currentTaskStatus) {
                        Log.i(TAG, "激励视频广告分阶段获取激励");
                    }

                    @Override
                    public void onExtraRewardVerify(int extraRewardType) {
                        Log.i(TAG, "激励视频广告获取额外奖励：" + extraRewardType);
                    }
                });

        // 2.设置展示配置
        KsVideoPlayConfig videoPlayConfig = new KsVideoPlayConfig.Builder()
                .build();
        // 3.展示激励视频广告
        mKsRewardVideoAd.showRewardVideoAd(activity, videoPlayConfig);
    }

    public void loadSigmobRewardVideo(String placementId, String rewardName, int rewardAmount) {
        Map<String, Object> options = new HashMap<>();
        windRewardedVideoAd = new WindRewardVideoAd(new WindRewardAdRequest(placementId, "", options));
        windRewardedVideoAd.setWindRewardVideoAdListener(new WindRewardVideoAdListener() {
            @Override
            public void onRewardAdLoadSuccess(final String placementId) {
                Log.d(TAG, "------onRewardAdLoadSuccess------" + placementId);
            }

            @Override
            public void onRewardAdPreLoadSuccess(String s) {
                Log.d(TAG, "------onRewardAdPreLoadSuccess------" + placementId);
            }

            @Override
            public void onRewardAdPreLoadFail(String s) {
                Log.d(TAG, "------onRewardAdPreLoadFail------" + placementId);
            }

            @Override
            public void onRewardAdPlayEnd(final String placementId) {
                Log.d(TAG, "------onRewardAdPlayEnd------" + placementId);
            }

            @Override
            public void onRewardAdPlayStart(final String placementId) {
                Log.d(TAG, "------onRewardAdPlayStart------" + placementId);
            }

            @Override
            public void onRewardAdClicked(final String placementId) {
                Log.d(TAG, "------onRewardAdClicked------" + placementId);
            }

            @Override
            public void onRewardAdClosed(final String placementId) {
                Log.d(TAG, "------onRewardAdClosed------" + placementId);

                WritableMap params = Arguments.createMap();
                params.putBoolean("isEnded", isRewardSuccess);
                AdHelper.sendEvent("RewardDidClose", params);
            }

            @Override
            public void onRewardAdRewarded(WindRewardInfo rewardInfo, String placementId) {
                Log.d(TAG, "------onRewardAdRewarded------" + rewardInfo.toString() + ":" + placementId);
                if (rewardInfo.isReward()) {
                    isRewardSuccess = true;
                }
            }

            @Override
            public void onRewardAdLoadError(WindAdError error, String placementId) {
                Log.d(TAG, "------onRewardAdLoadError------" + error.toString() + ":" + placementId);
                AdHelper.sendEvent("RewardDidFailed", null);
            }

            @Override
            public void onRewardAdPlayError(WindAdError error, String placementId) {
                Log.d(TAG, "------onRewardAdPlayError------" + error.toString() + ":" + placementId);
            }
        });

        if (windRewardedVideoAd != null) {
            windRewardedVideoAd.loadAd();
        }
    }

    private void showSigmobRewardVideo(String placementId, String rewardName, int rewardAmount) {
        HashMap<String, String> option = new HashMap<>();
        option.put(WindAds.AD_SCENE_DESC, rewardName);
        if (windRewardedVideoAd != null && windRewardedVideoAd.isReady()) {
            windRewardedVideoAd.show(option);
        }
    }

    @Override
    public Map<String, Object> getConstants() {
        final Map<String, Object> constants = new HashMap<>();
        constants.put("IsAndroid", true);
        return constants;
    }
}
