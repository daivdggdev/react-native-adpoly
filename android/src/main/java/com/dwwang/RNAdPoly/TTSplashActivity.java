package com.dwwang.RNAdPoly;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.MainThread;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAppDownloadListener;
import com.bytedance.sdk.openadsdk.TTSplashAd;

/**
 * 开屏广告Activity示例
 */
public class TTSplashActivity extends Activity {
    private static final String TAG = "TTSplashActivity";
    private TTAdNative mTTAdNative;
    private FrameLayout mSplashContainer;
    //是否强制跳转到主页面
    private boolean mForceGoMain;

    /**
     * 为防止无广告时造成视觉上类似于"闪退"的情况，设定无广告时页面跳转根据需要延迟一定时间，demo
     * 给出的延时逻辑是从拉取广告开始算开屏最少持续多久，仅供参考，开发者可自定义延时逻辑，如果开发者采用demo
     * 中给出的延时逻辑，也建议开发者考虑自定义minSplashTimeWhenNoAD的值（单位ms）
     **/
    private int minSplashTimeWhenNoAD = 2000;
    /**
     * 记录拉取广告的时间
     */
    private long fetchSplashADTime = 0;
    private Handler handler = new Handler(Looper.getMainLooper());

    //开屏广告加载超时时间,建议大于3500,这里为了冷启动第一次加载到广告并且展示,示例设置了3500ms
    private static final int AD_TIME_OUT = 3500;
    private String mCodeId = "";
    private boolean mIsExpress = false; //是否请求模板广告

    @SuppressWarnings("RedundantCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);
        mSplashContainer = (FrameLayout) findViewById(R.id.splash_container);
        //step2:创建TTAdNative对象
        mTTAdNative = TTAdManagerHolder.get().createAdNative(this);
        getExtraInfo();
        //在合适的时机申请权限，如read_phone_state,防止获取不了imei时候，下载类广告没有填充的问题
        //在开屏时候申请不太合适，因为该页面倒计时结束或者请求超时会跳转，在该页面申请权限，体验不好
        TTAdManagerHolder.get().requestPermissionIfNecessary(this);
        //加载开屏广告
        loadSplashAd();
    }

    private void getExtraInfo() {
        Intent intent = getIntent();
        if(intent == null) {
            return;
        }
        String codeId = intent.getStringExtra("placementId");
        if (!TextUtils.isEmpty(codeId)){
         mCodeId = codeId;
        }
    }

    @Override
    protected void onResume() {
        //判断是否该跳转到主页面
        if (mForceGoMain) {
            goToMainActivity();
        }
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mForceGoMain = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }

    /**
     * 加载开屏广告
     */
    private void loadSplashAd() {
        //step3:创建开屏广告请求参数AdSlot,具体参数含义参考文档
        AdSlot adSlot = null;
        if (mIsExpress) {
            //个性化模板广告需要传入期望广告view的宽、高，单位dp，请传入实际需要的大小，
            //比如：广告下方拼接logo、适配刘海屏等，需要考虑实际广告大小
            //float expressViewWidth = UIUtils.getScreenWidthDp(this);
            //float expressViewHeight = UIUtils.getHeight(this);
            adSlot = new AdSlot.Builder()
                    .setCodeId(mCodeId)
                    //模板广告需要设置期望个性化模板广告的大小,单位dp,代码位是否属于个性化模板广告，请在穿山甲平台查看
                    //view宽高等于图片的宽高
                    .setExpressViewAcceptedSize(1080,1920)
                    .build();
        } else {
            adSlot = new AdSlot.Builder()
                    .setCodeId(mCodeId)
                    .setImageAcceptedSize(1080, 1920)
                    .build();
        }

        //step4:请求广告，调用开屏广告异步请求接口，对请求回调的广告作渲染处理
        fetchSplashADTime = System.currentTimeMillis();
        mTTAdNative.loadSplashAd(adSlot, new TTAdNative.SplashAdListener() {
            @Override
            @MainThread
            public void onError(int code, String message) {
                Log.i(TAG, "onError" + String.valueOf(message));
                AdHelper.sendEventWithSplashFailed();
                finishDelay();
            }

            @Override
            @MainThread
            public void onTimeout() {
                Log.i(TAG, "onTimeout");
                goToMainActivity();
            }

            @Override
            @MainThread
            public void onSplashAdLoad(TTSplashAd ad) {
                Log.i(TAG, "开屏广告请求成功");
                if (ad == null) {
                    return;
                }
                //获取SplashView
                View view = ad.getSplashView();
                if (view != null && mSplashContainer != null && !TTSplashActivity.this.isFinishing()) {
                    mSplashContainer.removeAllViews();
                    //把SplashView 添加到ViewGroup中,注意开屏广告view：width >=70%屏幕宽；height >=50%屏幕高
                    mSplashContainer.addView(view);
                    //设置不开启开屏广告倒计时功能以及不显示跳过按钮,如果这么设置，您需要自定义倒计时逻辑
                    //ad.setNotAllowSdkCountdown();
                }else {
                    goToMainActivity();
                }

                //设置SplashView的交互监听器
                ad.setSplashInteractionListener(new TTSplashAd.AdInteractionListener() {
                    @Override
                    public void onAdClicked(View view, int type) {
                        Log.i(TAG, "onAdClicked");
                    }

                    @Override
                    public void onAdShow(View view, int type) {
                        Log.i(TAG, "onAdShow");
                    }

                    @Override
                    public void onAdSkip() {
                        Log.i(TAG, "onAdSkip");
                        goToMainActivity();

                    }

                    @Override
                    public void onAdTimeOver() {
                        Log.i(TAG, "onAdTimeOver");
                        goToMainActivity();
                    }
                });
                if(ad.getInteractionType() == TTAdConstant.INTERACTION_TYPE_DOWNLOAD) {
                    ad.setDownloadListener(new TTAppDownloadListener() {
                        boolean hasShow = false;

                        @Override
                        public void onIdle() {
                        }

                        @Override
                        public void onDownloadActive(long totalBytes, long currBytes, String fileName, String appName) {
                            if (!hasShow) {
                                hasShow = true;
                            }
                        }

                        @Override
                        public void onDownloadPaused(long totalBytes, long currBytes, String fileName, String appName) {

                        }

                        @Override
                        public void onDownloadFailed(long totalBytes, long currBytes, String fileName, String appName) {

                        }

                        @Override
                        public void onDownloadFinished(long totalBytes, String fileName, String appName) {

                        }

                        @Override
                        public void onInstalled(String fileName, String appName) {

                        }
                    });
                }
            }
        }, AD_TIME_OUT);

    }

    private void finishDelay() {
        /**
         * 为防止无广告时造成视觉上类似于"闪退"的情况，设定无广告时页面跳转根据需要延迟一定时间，demo
         * 给出的延时逻辑是从拉取广告开始算开屏最少持续多久，仅供参考，开发者可自定义延时逻辑，如果开发者采用demo
         * 中给出的延时逻辑，也建议开发者考虑自定义minSplashTimeWhenNoAD的值
         **/
        long alreadyDelayMills = System.currentTimeMillis() - fetchSplashADTime;//从拉广告开始到onNoAD已经消耗了多少时间
        long shouldDelayMills = alreadyDelayMills > minSplashTimeWhenNoAD ? 0 : minSplashTimeWhenNoAD
            - alreadyDelayMills;//为防止加载广告失败后立刻跳离开屏可能造成的视觉上类似于"闪退"的情况，根据设置的minSplashTimeWhenNoAD
        // 计算出还需要延时多久
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                TTSplashActivity.this.finish();
            }
        }, shouldDelayMills);
    }

    /**
     * 跳转到主页面
     */
    private void goToMainActivity() {
        //Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        //startActivity(intent);
        mSplashContainer.removeAllViews();
        this.finish();
    }
}
