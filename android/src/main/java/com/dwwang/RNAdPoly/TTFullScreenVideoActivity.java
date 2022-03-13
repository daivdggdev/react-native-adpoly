package com.dwwang.RNAdPoly;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.Toast;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdLoadType;
import com.bytedance.sdk.openadsdk.TTAdManager;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAppDownloadListener;
import com.bytedance.sdk.openadsdk.TTFullScreenVideoAd;

/**
 * Created by bytedance on 2018/2/1.
 */

public class TTFullScreenVideoActivity extends Activity {
    private static final String TAG = "TTFullScreen";
    private TTAdNative mTTAdNative;
    private TTFullScreenVideoAd mttFullVideoAd;
    private String mCodeId = "";
    private boolean mIsExpress = true; // 是否请求模板广告
    private boolean mIsLoaded = false; // 视频是否加载完成

    @SuppressWarnings("RedundantCast")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // setContentView(R.layout.activity_full_screen_video);
        // mLoadAd = (Button) findViewById(R.id.btn_reward_load);
        // mLoadAdVertical = (Button) findViewById(R.id.btn_reward_load_vertical);
        // mShowAd = (Button) findViewById(R.id.btn_reward_show);
        // step1:初始化sdk
        TTAdManager ttAdManager = TTAdManagerHolder.get();
        // step2:(可选，强烈建议在合适的时机调用):申请部分权限，如read_phone_state,防止获取不了imei时候，下载类广告没有填充的问题。
        TTAdManagerHolder.get().requestPermissionIfNecessary(this);
        // step3:创建TTAdNative对象,用于调用广告请求接口
        mTTAdNative = ttAdManager.createAdNative(this);
        getExtraInfo();
        loadAd(mCodeId, TTAdConstant.VERTICAL);
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

    private boolean mHasShowDownloadActive = false;

    @SuppressWarnings("SameParameterValue")
    private void loadAd(String codeId, int orientation) {
        // step4:创建广告请求参数AdSlot,具体参数含义参考文档
        AdSlot adSlot;
        if (mIsExpress) {
            adSlot = new AdSlot.Builder()
                    .setCodeId(codeId)
                    // 模板广告需要设置期望个性化模板广告的大小,单位dp,全屏视频场景，只要设置的值大于0即可
                    .setExpressViewAcceptedSize(500, 500)
                    .setSupportDeepLink(true)
                    .setOrientation(orientation)// 必填参数，期望视频的播放方向：TTAdConstant.HORIZONTAL 或 TTAdConstant.VERTICAL
                    .setAdLoadType(TTAdLoadType.LOAD)// 推荐使用，用于标注此次的广告请求用途为预加载（当做缓存）还是实时加载，方便后续为开发者优化相关策略
                    .build();

        } else {
            adSlot = new AdSlot.Builder()
                    .setCodeId(codeId)
                    .build();
        }
        // step5:请求广告
        mTTAdNative.loadFullScreenVideoAd(adSlot, new TTAdNative.FullScreenVideoAdListener() {
            @Override
            public void onError(int code, String message) {
                Log.i(TAG, "Callback --> onError: " + code + ", " + String.valueOf(message));
                goToMainActivity();
            }

            @Override
            public void onFullScreenVideoAdLoad(TTFullScreenVideoAd ad) {
                Log.i(TAG, "FullVideoAd loaded  广告类型：" + getAdType(ad.getFullVideoAdType()));

                mttFullVideoAd = ad;
                mIsLoaded = false;
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
                                goToMainActivity();
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
                if (mttFullVideoAd != null) {
                    // step6:在获取到广告后展示
                    // 展示广告，并传入广告展示的场景
                    // mttFullVideoAd.showFullScreenVideoAd(FullScreenVideoActivity.this,
                    // TTAdConstant.RitScenes.GAME_GIFT_BONUS, null);
                    mttFullVideoAd.showFullScreenVideoAd(TTFullScreenVideoActivity.this);
                    mttFullVideoAd = null;
                }
            }
        });

    }

    private String getAdType(int type) {
        switch (type) {
            case TTAdConstant.AD_TYPE_COMMON_VIDEO:
                return "普通全屏视频，type=" + type;
            case TTAdConstant.AD_TYPE_PLAYABLE_VIDEO:
                return "Playable全屏视频，type=" + type;
            case TTAdConstant.AD_TYPE_PLAYABLE:
                return "纯Playable，type=" + type;
        }

        return "未知类型+type=" + type;
    }

    /**
     * 跳转到主页面
     */
    private void goToMainActivity() {
        // Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        // startActivity(intent);
        this.finish();
    }

    /** 开屏页一定要禁止用户对返回按钮的控制，否则将可能导致用户手动退出了App而广告无法正常曝光和计费 */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
