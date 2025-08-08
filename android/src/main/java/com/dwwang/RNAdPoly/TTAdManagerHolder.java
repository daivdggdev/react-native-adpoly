package com.dwwang.RNAdPoly;

import android.content.Context;

import com.bytedance.sdk.openadsdk.LocationProvider;
import com.bytedance.sdk.openadsdk.TTAdConfig;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdManager;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.bytedance.sdk.openadsdk.TTCustomController;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;

import android.util.Log;

/**
 * 可以用一个单例来保存TTAdManager实例，在需要初始化sdk的时候调用
 */
public class TTAdManagerHolder {

    private static boolean sInit;

    public static TTAdManager get() {
        if (!sInit) {
            throw new RuntimeException("TTAdSdk is not init, please check.");
        }
        return TTAdSdk.getAdManager();
    }

    public static void init(Context context, String appId, Boolean requestPermission) {
        if (!sInit) {
            TTAdConfig config = new TTAdConfig.Builder()
                    .appId(appId)
                    .appName("口袋五线谱")
                    // .useTextureView(true) //
                    // 使用TextureView控件播放视频,默认为SurfaceView,当有SurfaceView冲突的场景，可以使用TextureView
                    .allowShowNotify(true) // 是否允许sdk展示通知栏提示
                    // .debug(BuildConfig.DEBUG) //测试阶段打开，可以通过日志排查问题，上线时去除该调用
                    .debug(BuildConfig.DEBUG) // 测试阶段打开，可以通过日志排查问题，上线时去除该调用
                    .directDownloadNetworkType(TTAdConstant.NETWORK_STATE_WIFI, TTAdConstant.NETWORK_STATE_4G,
                            TTAdConstant.NETWORK_STATE_5G) // 允许直接下载的网络状态集合
                    .supportMultiProcess(false)// 是否支持多进程
                    // .needClearTaskReset()
                    .customController(new TTCustomController() {
                        @Override
                        public boolean isCanUseLocation() {
                            return requestPermission;
                        }

                        @Override
                        public LocationProvider getTTLocation() {
                            return super.getTTLocation();
                        }

                        @Override
                        public boolean alist() {
                            return requestPermission;
                        }

                        @Override
                        public boolean isCanUsePhoneState() {
                            return requestPermission;
                        }

                        @Override
                        public String getDevImei() {
                            return super.getDevImei();
                        }

                        @Override
                        public boolean isCanUseWifiState() {
                            return requestPermission;
                        }

                        @Override
                        public String getMacAddress() {
                            return super.getMacAddress();
                        }

                        @Override
                        public boolean isCanUseWriteExternal() {
                            return requestPermission;
                        }

                        @Override
                        public String getDevOaid() {
                            return super.getDevOaid();
                        }
                    })
                    .build();

            TTAdSdk.init(context, config);

            TTAdSdk.start(new TTAdSdk.Callback() {
                /**
                 * 初始化成功回调
                 * 注意：开发者需要在success回调之后再去请求广告
                 */
                @Override
                public void success() {
                    sInit = true;
                    Log.d("RNAdPolyModule", "tt init success");

                    WritableMap params = Arguments.createMap();
                    params.putString("type", "tt");
                    AdHelper.sendEvent("AdInitSuccess", params);

                    // 初始化之后申请下权限，开发者如果不想申请可以将此处删除
                    if (requestPermission) {
                        TTAdSdk.getAdManager().requestPermissionIfNecessary(context);
                    }
                }

                /**
                 * @param code 初始化失败回调错误码
                 * @param msg  初始化失败回调信息
                 */
                @Override
                public void fail(int code, String msg) {
                    Log.d("RNAdPolyModule", "tt init fail, code = " + code + "s = " + msg);
                }
            });
        }
    }

    public static boolean isInitSuccess() {
        return TTAdSdk.isSdkReady();
    }
}
