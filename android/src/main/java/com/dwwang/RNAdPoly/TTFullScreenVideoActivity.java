package com.dwwang.RNAdPoly;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdManager;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAppDownloadListener;
import com.bytedance.sdk.openadsdk.TTFullScreenVideoAd;

/**
 * Created by bytedance on 2018/2/1.
 */

public class TTFullScreenVideoActivity extends Activity {
    private static final String TAG = "TTFullScreenVideoActivity";
    private Button mLoadAd;
    private Button mLoadAdVertical;
    private Button mShowAd;
    private TTAdNative mTTAdNative;
    private TTFullScreenVideoAd mttFullVideoAd;
    private String mCodeId = "";
    private boolean mIsExpress = false; //是否请求模板广告
    private boolean mIsLoaded = false; //视频是否加载完成

    @SuppressWarnings("RedundantCast")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //setContentView(R.layout.activity_full_screen_video);
        //mLoadAd = (Button) findViewById(R.id.btn_reward_load);
        //mLoadAdVertical = (Button) findViewById(R.id.btn_reward_load_vertical);
        //mShowAd = (Button) findViewById(R.id.btn_reward_show);
        //step1:初始化sdk
        TTAdManager ttAdManager = TTAdManagerHolder.get();
        //step2:(可选，强烈建议在合适的时机调用):申请部分权限，如read_phone_state,防止获取不了imei时候，下载类广告没有填充的问题。
        TTAdManagerHolder.get().requestPermissionIfNecessary(this);
        //step3:创建TTAdNative对象,用于调用广告请求接口
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
        if (!TextUtils.isEmpty(codeId)){
            mCodeId = codeId;
        }
    }

    private boolean mHasShowDownloadActive = false;

    @SuppressWarnings("SameParameterValue")
    private void loadAd(String codeId, int orientation) {
        //step4:创建广告请求参数AdSlot,具体参数含义参考文档
        AdSlot adSlot;
        if (mIsExpress) {
            adSlot = new AdSlot.Builder()
                    .setCodeId(codeId)
                    //模板广告需要设置期望个性化模板广告的大小,单位dp,全屏视频场景，只要设置的值大于0即可
                    .setExpressViewAcceptedSize(500,500)
                    .build();

        } else {
            adSlot = new AdSlot.Builder()
                    .setCodeId(codeId)
                    .build();
        }
        //step5:请求广告
        mTTAdNative.loadFullScreenVideoAd(adSlot, new TTAdNative.FullScreenVideoAdListener() {
            @Override
            public void onError(int code, String message) {
                Log.i(TAG, "Callback --> onError: " + code + ", " + String.valueOf(message));
            }

            @Override
            public void onFullScreenVideoAdLoad(TTFullScreenVideoAd ad) {
                Log.i(TAG, "FullVideoAd loaded  广告类型：" + getAdType(ad.getFullVideoAdType()));

                mttFullVideoAd = ad;
                mIsLoaded = false;
                mttFullVideoAd.setFullScreenVideoAdInteractionListener(new TTFullScreenVideoAd.FullScreenVideoAdInteractionListener() {

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


                //ad.setDownloadListener(new TTAppDownloadListener() {
                    //@Override
                    //public void onIdle() {
                        //mHasShowDownloadActive = false;
                    //}

                    //@Override
                    //public void onDownloadActive(long totalBytes, long currBytes, String fileName, String appName) {
                        //Log.d("DML", "onDownloadActive==totalBytes=" + totalBytes + ",currBytes=" + currBytes + ",fileName=" + fileName + ",appName=" + appName);

                        //if (!mHasShowDownloadActive) {
                            //mHasShowDownloadActive = true;
                            //TToast.show(FullScreenVideoActivity.this, "下载中，点击下载区域暂停", Toast.LENGTH_LONG);
                        //}
                    //}

                    //@Override
                    //public void onDownloadPaused(long totalBytes, long currBytes, String fileName, String appName) {
                        //Log.d("DML", "onDownloadPaused===totalBytes=" + totalBytes + ",currBytes=" + currBytes + ",fileName=" + fileName + ",appName=" + appName);
                        //TToast.show(FullScreenVideoActivity.this, "下载暂停，点击下载区域继续", Toast.LENGTH_LONG);
                    //}

                    //@Override
                    //public void onDownloadFailed(long totalBytes, long currBytes, String fileName, String appName) {
                        //Log.d("DML", "onDownloadFailed==totalBytes=" + totalBytes + ",currBytes=" + currBytes + ",fileName=" + fileName + ",appName=" + appName);
                        //TToast.show(FullScreenVideoActivity.this, "下载失败，点击下载区域重新下载", Toast.LENGTH_LONG);
                    //}

                    //@Override
                    //public void onDownloadFinished(long totalBytes, String fileName, String appName) {
                        //Log.d("DML", "onDownloadFinished==totalBytes=" + totalBytes + ",fileName=" + fileName + ",appName=" + appName);
                        //TToast.show(FullScreenVideoActivity.this, "下载完成，点击下载区域重新下载", Toast.LENGTH_LONG);
                    //}

                    //@Override
                    //public void onInstalled(String fileName, String appName) {
                        //Log.d("DML", "onInstalled==" + ",fileName=" + fileName + ",appName=" + appName);
                        //TToast.show(FullScreenVideoActivity.this, "安装完成，点击下载区域打开", Toast.LENGTH_LONG);
                    //}
                //});
            }

            @Override
            public void onFullScreenVideoCached() {
                Log.i(TAG, "Callback --> onFullScreenVideoCached");
                if (mttFullVideoAd != null) {
                    //step6:在获取到广告后展示
                    //展示广告，并传入广告展示的场景
                    // mttFullVideoAd.showFullScreenVideoAd(FullScreenVideoActivity.this, TTAdConstant.RitScenes.GAME_GIFT_BONUS, null);
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
        //Intent intent = new Intent(SplashActivity.this, MainActivity.class);
        //startActivity(intent);
        this.finish();
    }
}
