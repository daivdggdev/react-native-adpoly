package com.dwwang.RNAdPoly;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;

import com.qq.e.ads.cfg.VideoOption;
import com.qq.e.ads.interstitial2.UnifiedInterstitialAD;
import com.qq.e.ads.interstitial2.UnifiedInterstitialADListener;
import com.qq.e.ads.interstitial2.UnifiedInterstitialMediaListener;
import com.qq.e.comm.listeners.NegativeFeedbackListener;
import com.qq.e.comm.util.AdError;

import java.util.Locale;

public class GDTInterstitialADActivity implements UnifiedInterstitialADListener, UnifiedInterstitialMediaListener {

  private static final String TAG = GDTInterstitialADActivity.class.getSimpleName();
  private UnifiedInterstitialAD iad;
  private String mCodeId;

  private boolean isRenderFail;
  private boolean mLoadSuccess;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    getExtraInfo();
    loadAd();
  }

  private void getExtraInfo() {
    Intent intent = getIntent();
    if (intent == null) {
        return;
    }
    mCodeId = intent.getStringExtra("placementId");
  }

  private void loadAd() {
    mLoadSuccess = false;
    iad = getIAD();
    iad.loadAD();
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    if (iad != null) {
      iad.destroy();
    }
  }

  private UnifiedInterstitialAD getIAD() {
    if (this.iad != null) {
      iad.close();
      iad.destroy();
    }
    isRenderFail = false;
    iad = new UnifiedInterstitialAD(this, mCodeId, this);
    iad.setNegativeFeedbackListener(new NegativeFeedbackListener() {
      @Override
      public void onComplainSuccess() {
        Log.i(TAG, "onComplainSuccess");
      }
    });
    iad.setMediaListener(this);
    return iad;
  }

  private void close() {
    if (iad != null) {
      iad.close();
    }
  }

  @Override
  public void onADReceive() {
    mLoadSuccess = true;
    // onADReceive之后才可调用getECPM()
    Log.d(TAG, "onADReceive eCPMLevel = " + iad.getECPMLevel()+ ", ECPM: " + iad.getECPM()
        + ", videoduration=" + iad.getVideoDuration()
        + ", testExtraInfo:" + iad.getExtraInfo().get("mp")
        + ", request_id:" + iad.getExtraInfo().get("request_id"));
  }

  // /**
  //  * 上报给优量汇服务端在开发者客户端竞价中优量汇的竞价结果，以便于优量汇服务端调整策略提供给开发者更合理的报价
  //  *
  //  * 优量汇竞价失败调用 sendLossNotification，并填入优量汇竞败原因（必填）、竞胜ADN ID（选填）、竞胜ADN报价（选填）
  //  * 优量汇竞价胜出调用 sendWinNotification，并填入开发者期望扣费价格（单位分）
  //  * 请开发者如实上报相关参数，以保证优量汇服务端能根据相关参数调整策略，使开发者收益最大化
  //  */
  // private void reportBiddingResult(UnifiedInterstitialAD interstitialAD) {
  //   DemoBiddingC2SUtils.reportBiddingWinLoss(interstitialAD);
  //   if (DemoUtil.isNeedSetBidECPM()) {
  //     interstitialAD.setBidECPM(300);
  //   }
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
  }

  @Override
  public void onRenderSuccess() {
    Log.i(TAG, "onRenderSuccess，建议在此回调后再调用展示方法");
    if (iad.isValid()){
      iad.show();
    }
  }

  @Override
  public void onRenderFail() {
    Log.i(TAG, "onRenderFail");
  }

  @Override
  public void onVideoInit() {
    Log.i(TAG, "onVideoInit");
  }

  @Override
  public void onVideoLoading() {
    Log.i(TAG, "onVideoLoading");
  }

  @Override
  public void onVideoReady(long videoDuration) {
    Log.i(TAG, "onVideoReady, duration = " + videoDuration);
  }

  @Override
  public void onVideoStart() {
    Log.i(TAG, "onVideoStart");
  }

  @Override
  public void onVideoPause() {
    Log.i(TAG, "onVideoPause");
  }

  @Override
  public void onVideoComplete() {
    Log.i(TAG, "onVideoComplete");
  }

  @Override
  public void onVideoError(AdError error) {
    Log.i(TAG, "onVideoError, code = " + error.getErrorCode() + ", msg = " + error.getErrorMsg());
  }

  @Override
  public void onVideoPageOpen() {
    Log.i(TAG, "onVideoPageOpen");
  }

  @Override
  public void onVideoPageClose() {
    Log.i(TAG, "onVideoPageClose");
  }
}
