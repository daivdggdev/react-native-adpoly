package com.dwwang.RNAdPoly;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAdNative.FullScreenVideoAdListener;
import com.bytedance.sdk.openadsdk.TTFullScreenVideoAd;

/**
 * 融合demo，插全屏广告使用示例。更多功能参考接入文档。
 * <p>
 * 注意：每次加载的广告，只能展示一次
 * <p>
 * 接入步骤：
 * 1、创建AdSlot对象
 * 2、创建TTAdNative对象
 * 3、创建加载、展示监听器
 * 4、加载广告
 * 5、加载成功后，展示广告
 * 6、在onDestroy中销毁广告
 */
public final class MediationInterstitialFullActivity extends Activity {


    private TTFullScreenVideoAd mTTFullScreenVideoAd; // 插全屏广告对象
    private FullScreenVideoAdListener mFullScreenVideoListener; // 广告加载监听器
    private TTFullScreenVideoAd.FullScreenVideoAdInteractionListener mFullScreenVideoAdInteractionListener; // 广告展示监听器

    private static final String TAG = "TTFullScreen";
    private TTAdNative mTTAdNative;
    private TTFullScreenVideoAd mttFullVideoAd;
    private String mCodeId = "";
    private boolean mIsExpress = true; // 是否请求模板广告
    private boolean mIsLoaded = false; // 视频是否加载完成

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getExtraInfo();
    }

    private void getExtraInfo() {
        Intent intent = getIntent();
        if (intent == null) {
            return;
        }
        String codeId = intent.getStringExtra("placementId");
        mIsExpress = intent.getBooleanExtra("is_express", true);
        if (!TextUtils.isEmpty(codeId)) {
            mCodeId = codeId;
        }
    }

    private void loadInterstitialFullAd() {
        /** 1、创建AdSlot对象 */
        AdSlot adslot = new AdSlot.Builder()
                .setCodeId(this.mCodeId)
                .setOrientation(TTAdConstant.ORIENTATION_VERTICAL)
                .build();

        /** 2、创建TTAdNative对象 */
        TTAdNative adNativeLoader = TTAdManagerHolder.get().createAdNative(this);

        /** 3、创建加载、展示监听器 */
        initListeners();

        /** 4、加载广告 */
        adNativeLoader.loadFullScreenVideoAd(adslot, this.mFullScreenVideoListener);
    }

    // 在加载成功后展示广告
    private void showInterstitialFullAd() {
        if (this.mTTFullScreenVideoAd == null) {
            Log.d(TAG, "请先加载广告或等待广告加载完毕后再调用show方法");
            return;
        }

        /** 5、设置展示监听器，展示广告 */
        this.mTTFullScreenVideoAd.setFullScreenVideoAdInteractionListener(this.mFullScreenVideoAdInteractionListener);
        this.mTTFullScreenVideoAd.showFullScreenVideoAd(MediationInterstitialFullActivity.this);
    }

    private void initListeners() {
        // 广告加载监听器

        this.mFullScreenVideoListener = new FullScreenVideoAdListener() {
            public void onError(int code, String message) {
                Log.d(TAG, "InterstitialFull onError code = " + code + " msg = " + message);
            }


            public void onFullScreenVideoAdLoad(TTFullScreenVideoAd ad) {
                Log.d(TAG, "InterstitialFull onFullScreenVideoLoaded");
                mTTFullScreenVideoAd = ad;
            }


            public void onFullScreenVideoCached() {
                Log.d(TAG, "InterstitialFull onFullScreenVideoCached");
            }


            public void onFullScreenVideoCached(TTFullScreenVideoAd ad) {
                Log.d(TAG, "InterstitialFull onFullScreenVideoCached");
                mTTFullScreenVideoAd = ad;
            }
        };
        // 广告展示监听器

        this.mFullScreenVideoAdInteractionListener = new TTFullScreenVideoAd.FullScreenVideoAdInteractionListener() {

            public void onAdShow() {
                Log.d(TAG, "InterstitialFull onAdShow");
            }


            public void onAdVideoBarClick() {
                Log.d(TAG, "InterstitialFull onAdVideoBarClick");
            }


            public void onAdClose() {
                Log.d(TAG, "InterstitialFull onAdClose");
            }


            public void onVideoComplete() {
                Log.d(TAG, "InterstitialFull onVideoComplete");
            }


            public void onSkippedVideo() {
                Log.d(TAG, "InterstitialFull onSkippedVideo");
            }
        };
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /** 6、在onDestroy中销毁广告 */
        if (mTTFullScreenVideoAd != null && mTTFullScreenVideoAd.getMediationManager() != null) {
            mTTFullScreenVideoAd.getMediationManager().destroy();
        }
    }
}
