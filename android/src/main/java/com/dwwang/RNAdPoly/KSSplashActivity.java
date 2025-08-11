package com.dwwang.RNAdPoly;

import java.util.Random;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.kwad.sdk.api.KsAdSDK;
import com.kwad.sdk.api.KsLoadManager;
import com.kwad.sdk.api.KsScene;
import com.kwad.sdk.api.KsSplashScreenAd;
import com.kwad.sdk.api.model.AdClickAction;
import com.kwad.sdk.api.model.AdExposureFailedReason;
import com.kwad.sdk.api.model.AdExposureFailureCode;
import com.kwad.sdk.api.model.AdShowAction;
import com.kwad.sdk.api.model.AdnName;
import com.kwad.sdk.api.model.AdnType;
import com.kwad.sdk.api.model.SplashAdExtraData;

public class KSSplashActivity extends Activity {
  private static final String TAG = "splash_test";

  private ViewGroup mEmptyView;
  private ViewGroup mSplashAdContainer;
  private boolean mIsPaused;
  private boolean mGotoMainActivity;
  private boolean mIsResumeGoToMain;
  private String mBidResponse;
  private String mBidResponseV2;
  private long mPosId;
  private Context mContext;

  private String placementId;
  private boolean mIsHalfSize = false;// 是否是半全屏开屏
  private ViewGroup container;
  private FrameLayout mSplashContainer;
  private LinearLayout mSplashHalfSizeLayout;
  private FrameLayout mSplashSplashContainer;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mContext = this;

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

    requestSplashScreenAd();
  }

  // 1.请求开屏广告，获取广告对象，KsFullScreenVideoAd
  public void requestSplashScreenAd() {
    KsScene.Builder builder = null;
    try {
      builder = new KsScene.Builder(Long.valueOf(placementId).longValue());
    } catch (Throwable e) {
      e.printStackTrace();
    }

    if (builder == null) {
      // 创建失败，直接进入首页
      gotoMainActivity();
      return;
    }

    SplashAdExtraData extraData = new SplashAdExtraData();
    extraData.setDisableShakeStatus(true);
    builder.setSplashExtraData(extraData);

    KsScene scene = builder.build();
    KsAdSDK.getLoadManager().loadSplashScreenAd(scene, new KsLoadManager.SplashScreenAdListener() {
      @Override
      public void onError(int code, String msg) {
        mSplashAdContainer.setVisibility(View.GONE);
        mEmptyView.setVisibility(View.VISIBLE);
        Log.i(TAG, "开屏广告请求失败" + code + msg);
        gotoMainActivity();
      }

      @Override
      public void onRequestResult(int adNumber) {
      }

      @Override
      public void onSplashScreenAdLoad(KsSplashScreenAd splashScreenAd) {
        Log.i(TAG, "开屏广告请求成功");
        addView(splashScreenAd);
      }
    });
  }

  private void addView(final KsSplashScreenAd splashScreenAd) {
    if (isFinishing()) {
      return;
    }
    View view = splashScreenAd.getView(mContext, new KsSplashScreenAd.SplashScreenAdInteractionListener() {
      @Override
      public void onAdClicked() {
        Log.i(TAG, "开屏广告点击");
        /**
         * 开屏广告点击会吊起h5或应用商店，并回调onAdClick(), mGotoMainActivity控制由h5或应用商店返回后是否直接进入主界面
         */
        mGotoMainActivity = true;
      }

      @Override
      public void onAdShowError(int code, String extra) {
        Log.i(TAG, "开屏广告显示错误 " + code + " extra " + extra);
        // 出错不触发显示miniWindow
        gotoMainActivity();
      }

      @Override
      public void onAdShowEnd() {
        Log.i(TAG, "开屏广告显示结束");
        gotoMainActivity();
      }

      @Override
      public void onAdShowStart() {
        Log.i(TAG, "开屏广告显示开始");
      }

      @Override
      public void onSkippedAd() {
        Log.i(TAG, "用户跳过开屏广告");
        gotoMainActivity();
      }

      @Override
      public void onDownloadTipsDialogShow() {

      }

      @Override
      public void onDownloadTipsDialogDismiss() {

      }

      @Override
      public void onDownloadTipsDialogCancel() {

      }
    });
    if (view == null) {
      gotoMainActivity();
    } else {
      container.addView(view);
    }
  }

  @Override
  protected void onPause() {
    super.onPause();
    mIsPaused = true;
  }

  @Override
  protected void onResume() {
    super.onResume();
    mIsPaused = false;
    if (mGotoMainActivity) {
      gotoMainActivity();
    }
  }

  @Override
  public void finish() {
    super.finish();
  }

  private void gotoMainActivity() {
    if (mIsPaused) {
      mGotoMainActivity = true;
    } else {
      finish();
    }
  }
}
