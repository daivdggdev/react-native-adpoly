package com.dwwang.RNAdPoly;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.qq.e.ads.rewardvideo.RewardVideoAD;
import com.qq.e.ads.rewardvideo.RewardVideoADListener;
import com.qq.e.ads.rewardvideo.ServerSideVerificationOptions;
import com.qq.e.comm.listeners.NegativeFeedbackListener;
import com.qq.e.comm.util.AdError;

import java.util.Locale;
import java.util.Map;

import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;
import static android.content.res.Configuration.ORIENTATION_PORTRAIT;

/**
 * 激励视频广告基本接入示例，演示了基本的激励视频广告功能（1.初始化激励视频广告;2.加载激励视频广告;3.展示激励视频广告）。
 * <p>
 * Created by chaotao on 2018/10/8.
 */

public class GDTRewardVideoActivity extends Activity implements RewardVideoADListener {

  private static final String TAG = GDTRewardVideoActivity.class.getSimpleName();
  private RewardVideoAD mRewardVideoAD;
  private String mCodeId;
  private String mRewardName;
  private int mRewardAmount;
  private boolean mIsLoadSuccess;

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
    mRewardName = intent.getStringExtra("rewardName");
    mRewardAmount = intent.getIntExtra("rewardAmount", 0);
  }

  private void loadAd(){
    // 1. 初始化激励视频广告
    mRewardVideoAD = new RewardVideoAD(getApplicationContext(), mCodeId, this, false);
    mRewardVideoAD.setNegativeFeedbackListener(new NegativeFeedbackListener() {
      @Override
      public void onComplainSuccess() {
        Log.i(TAG, "onComplainSuccess");
      }
    });

    mIsLoadSuccess = false;
    // 2. 加载激励视频广告
    mRewardVideoAD.loadAD();
  }

  /**
   * 广告加载成功，可在此回调后进行广告展示
   **/
  @Override
  public void onADLoad() {
    Log.i(TAG, "onADLoad");
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

    mIsLoadSuccess = true;
    if (!mRewardVideoAD.hasShown() && mRewardVideoAD.isValid()) {
      //广告展示检查2：当前广告数据还没有展示过
      //广告展示检查3：展示广告前判断广告数据未过期
      mRewardVideoAD.showAD();
    }
  }

  /**
   * 上报给优量汇服务端在开发者客户端竞价中优量汇的竞价结果，以便于优量汇服务端调整策略提供给开发者更合理的报价
   *
   * 优量汇竞价失败调用 sendLossNotification，并填入优量汇竞败原因（必填）、竞胜ADN ID（选填）、竞胜ADN报价（选填）
   * 优量汇竞价胜出调用 sendWinNotification，并填入开发者期望扣费价格（单位分）
   * 请开发者如实上报相关参数，以保证优量汇服务端能根据相关参数调整策略，使开发者收益最大化
   */
  // private void reportBiddingResult(RewardVideoAD rewardVideoAD) {
  //   DemoBiddingC2SUtils.reportBiddingWinLoss(rewardVideoAD);
  //   if (DemoUtil.isNeedSetBidECPM()) {
  //     rewardVideoAD.setBidECPM(300);
  //   }
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
   * @param map 若选择了服务端验证，可以通过 ServerSideVerificationOptions#TRANS_ID 键从 map 中获取此次交易的 id；若未选择服务端验证，则不需关注 map 参数。
   */
  @Override
  public void onReward(Map<String, Object> map) {
    // Log.i(TAG, "onReward " + map.get(ServerSideVerificationOptions.TRANS_ID));  // 获取服务端验证的唯一 ID
    Log.i(TAG, "onReward ");  // 获取服务端验证的唯一 ID
    AdHelper.sendEvent("RewardDidSucceed", null);
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
    AdHelper.sendEvent("RewardDidClose", null);
    goToMainActivity();
  }

  /**
   * 广告流程出错
   */
  @Override
  public void onError(AdError adError) {
    String msg = String.format(Locale.getDefault(), "onError, error code: %d, error msg: %s",
        adError.getErrorCode(), adError.getErrorMsg());
    Log.i(TAG, "onError, adError=" + msg);
    goToMainActivity();
  }

  /**
     * 跳转到主页面
     */
  private void goToMainActivity() {
    // Intent intent = new Intent(SplashActivity.this, MainActivity.class);
    // startActivity(intent);
    this.finish();
  }
}
