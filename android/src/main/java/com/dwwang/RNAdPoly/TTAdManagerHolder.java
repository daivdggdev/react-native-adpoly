package com.dwwang.RNAdPoly;

import android.content.Context;
import android.util.Log;

import com.bytedance.sdk.openadsdk.TTAdConfig;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdManager;
import com.bytedance.sdk.openadsdk.TTAdSdk;

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

    public static void init(Context context, String appId) {
        synchronized (TTAdManagerHolder.class) {
            if (!sInit) {
                TTAdConfig config = new TTAdConfig.Builder()
                        .appId(appId)
                        .appName("口袋五线谱")
                        .useTextureView(true) // 使用TextureView控件播放视频,默认为SurfaceView,当有SurfaceView冲突的场景，可以使用TextureView
                        .allowShowNotify(true) // 是否允许sdk展示通知栏提示
                        // .debug(BuildConfig.DEBUG) //测试阶段打开，可以通过日志排查问题，上线时去除该调用
                        .debug(true) // 测试阶段打开，可以通过日志排查问题，上线时去除该调用
                        .directDownloadNetworkType(TTAdConstant.NETWORK_STATE_WIFI, TTAdConstant.NETWORK_STATE_4G) // 允许直接下载的网络状态集合
                        .supportMultiProcess(false)// 是否支持多进程
                        .needClearTaskReset()
                        // .httpStack(new MyOkStack3())//自定义网络库，demo中给出了okhttp3版本的样例，其余请自行开发或者咨询工作人员。
                        .build();

                TTAdSdk.init(context, config, new TTAdSdk.InitCallback() {
                    /**
                     * 初始化成功回调
                     * 注意：开发者需要在success回调之后再去请求广告
                     */
                    @Override
                    public void success() {
                        sInit = true;
                        Log.d("TTAdSdk", "init success");
                        // 初始化之后申请下权限，开发者如果不想申请可以将此处删除
                        // TTAdSdk.getAdManager().requestPermissionIfNecessary(context);
                    }

                    /**
                     * @param code 初始化失败回调错误码
                     * @param msg  初始化失败回调信息
                     */
                    @Override
                    public void fail(int code, String msg) {
                        Log.d("TTAdSdk", "init fail, code = " + code + "s = " + msg);
                    }
                });
            }
        }
    }

    public static boolean isInitSuccess() {
        return TTAdSdk.isInitSuccess();
    }
}
