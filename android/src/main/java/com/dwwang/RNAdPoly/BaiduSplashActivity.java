package com.dwwang.RNAdPoly;

import com.baidu.mobads.sdk.api.AdSettings;
import com.baidu.mobads.sdk.api.BiddingListener;
import com.baidu.mobads.sdk.api.RequestParameters;
import com.baidu.mobads.sdk.api.SplashAd;
import com.baidu.mobads.sdk.api.SplashInteractionListener;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * 实时开屏，广告实时请求并且立即展现。实时开屏接入请看该类
 */
public class BaiduSplashActivity extends Activity {
    private static final String TAG = "BaiduSplashActivity";

    // 推荐使用全局变量，以便统一释放资源
    private SplashAd splashAd;
    private boolean canJumpImmediately = false;

    private String placementId;
    private boolean mIsHalfSize = false;// 是否是半全屏开屏
    private ViewGroup container;
    private FrameLayout mSplashContainer;
    private LinearLayout mSplashHalfSizeLayout;
    private FrameLayout mSplashSplashContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /**
         * 【注意】开屏点睛需要开屏和主页的窗口具有特性 {@linkplain Window.FEATURE_ACTIVITY_TRANSITIONS}
         * Tips: 一般具有Material Design风格的App主题，系统会默认开启该特性。
         * 若没有该特性，可以在{@link #setContentView(int)}之前
         * 调用 {@link #requestWindowFeature(int)} 即可开启特性，如下：
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            requestWindowFeature(Window.FEATURE_ACTIVITY_TRANSITIONS);
        }
        /* 设置开屏全屏显示&透明状态栏 */
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setStatusBarColor(Color.TRANSPARENT);
        // 把requestWindowFeature放在super前面，要不然部分机型会上报异常
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash_tt);
        mSplashContainer = (FrameLayout) findViewById(R.id.splash_container);
        mSplashHalfSizeLayout = (LinearLayout) findViewById(R.id.splash_half_size_layout);
        mSplashSplashContainer = (FrameLayout) findViewById(R.id.splash_container_half_size);

        placementId = getIntent().getStringExtra("placementId");
        mIsHalfSize = getIntent().getBooleanExtra("is_half_size", false);

        if (mIsHalfSize) {
            container = mSplashSplashContainer;
            mSplashHalfSizeLayout.setVisibility(View.VISIBLE);
            mSplashContainer.setVisibility(View.GONE);
        } else {
            container = mSplashContainer;
            mSplashHalfSizeLayout.setVisibility(View.GONE);
        }

        fetchSplashAD();
    }

    /**
     * 请求和展现广告
     *
     * @return void
     */
    private void fetchSplashAD() {
        SplashInteractionListener listener = new SplashInteractionListener() {
            @Override
            public void onLpClosed() {
                Log.i(TAG, "lp页面关闭");
                jump();
            }

            @Override
            public void onAdDismissed() {
                Log.i(TAG, "onAdDismissed");
                jumpWhenCanClick(); // 跳转至您的应用主界面
            }

            @Override
            public void onAdSkip() {
                Log.i(TAG, "onAdSkip");
            }

            @Override
            public void onADLoaded() {
                Log.i(TAG, "onADLoaded");
            }

            @Override
            public void onAdExposed() {
                Log.i(TAG, "onAdExposed");
            }

            @Override
            public void onAdFailed(String arg0) {
                Log.i(TAG, "" + arg0);
                jump();
            }

            @Override
            public void onAdPresent() {
                Log.i(TAG, "onAdPresent");
            }

            @Override
            public void onAdClick() {
                Log.i(TAG, "onAdClick");
                // 设置开屏可接受点击时，该回调可用
            }

            @Override
            public void onAdCacheSuccess() {
                Log.i(TAG, "onAdCacheSuccess");
            }

            @Override
            public void onAdCacheFailed() {
                Log.i(TAG, "onAdCacheFailed");
                jump();
            }
        };

        // splashAd = new SplashAd(this, adPlaceId, listener);
        // splashAd.loadAndShow(adsParent);

        // 如果开屏需要load广告和show广告分开，请参考类RSplashManagerActivity的写法
        // 如果需要修改开屏超时时间、隐藏工信部下载整改展示，请设置下面代码;
        final RequestParameters.Builder parameters = new RequestParameters.Builder();
        // sdk内部默认超时时间为4200，单位：毫秒
        parameters.addExtra(SplashAd.KEY_TIMEOUT, "4200");
        // sdk内部默认值为true
        parameters.addExtra(SplashAd.KEY_DISPLAY_DOWNLOADINFO, "true");
        // 用户点击开屏下载类广告时，是否弹出Dialog
        // 此选项设置为true的情况下，会覆盖掉 {SplashAd.KEY_DISPLAY_DOWNLOADINFO} 的设置
        parameters.addExtra(SplashAd.KEY_POPDIALOG_DOWNLOAD, "true");
        // 设置开屏图片宽高(单位dp)，建议按照app情况传入正确的宽高比例值
        // parameters.setWidth(1080);
        // parameters.setHeight(1920);
        final int width = 1080;
        final int height = mIsHalfSize ? 1920 : 2310;

        splashAd = new SplashAd(this, placementId, parameters.build(), listener);
        // 【可选】【Bidding】设置广告的底价，单位：分
        // splashAd.setBidFloor(100);
        // 请求并展示广告
        splashAd.loadAndShow(container);
    }

    private void jumpWhenCanClick() {
        if (canJumpImmediately) {
            // if (splashAd != null) {
            // Intent intent = new Intent(RSplashActivity.this, BaiduSDKDemo.class);
            // /**
            // * 1. 结束当前Activity并启动主页，建议在该方法的回调中执行开屏Activity的结束操作。
            // * 2. 若仅传入Intent而不设置回调，则会在启动主页后自动结束当前开屏Activity，
            // * 例如： {@link SplashAd#finishAndJump(Intent)}
            // * 3. 建议配合主页onCreate阶段中的 {@link SplashAd#registerEnterTransition(
            // * Activity, SplashAd.SplashFocusAdListener)} 使用
            // */
            // splashAd.finishAndJump(intent, new SplashAd.OnFinishListener() {
            // @Override
            // public void onFinishActivity() {
            // Log.i(TAG, "onFinishActivity");
            // finish();
            // }
            // });
            // splashAd.destroy();
            // }
            finish();
        } else {
            canJumpImmediately = true;
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        canJumpImmediately = false;
    }

    /**
     * 不可点击的开屏，使用该jump方法，而不是用jumpWhenCanClick
     */
    private void jump() {
        if (canJumpImmediately) {
            // if (splashAd != null) {
            // Intent intent = new Intent(RSplashActivity.this, BaiduSDKDemo.class);
            // /**
            // * 1. 结束当前Activity并启动主页，建议在该方法的回调中执行开屏Activity的结束操作。
            // * 2. 若仅传入Intent而不设置回调，则会在启动主页后自动结束当前开屏Activity，
            // * 例如： {@link SplashAd#finishAndJump(Intent)}
            // * 3. 建议配合主页onCreate阶段中的 {@link SplashAd#registerEnterTransition(
            // * Activity, SplashAd.SplashFocusAdListener)} 使用
            // */
            // splashAd.finishAndJump(intent, new SplashAd.OnFinishListener() {
            // @Override
            // public void onFinishActivity() {
            // Log.i(TAG, "onFinishActivity");
            // finish();
            // }
            // });
            // splashAd.destroy();
            // }
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (canJumpImmediately) {
            jumpWhenCanClick();
        }
        canJumpImmediately = true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Activity销毁时，销毁广告对象释放资源，避免潜在的内存泄露
        if (splashAd != null) {
            splashAd.destroy();
            splashAd = null;
        }
        this.finish();
    }
}
