package com.dwwang.RNAdPoly;

import android.content.Context;
import androidx.annotation.Nullable;

import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactContext;

public class AdHelper {
	public static ReactContext reactContext;

	public static void sendEventWithSplashFailed() {
		sendEvent("ShowSplashFailed", null);
  }

	public static void sendEventWithReward() {
		sendEvent("onReward", null);
  }

	public static void sendEvent(String eventName, @Nullable WritableMap params) {
		if (reactContext == null) {
			return;
		}

		reactContext
			.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
			.emit(eventName, params);
	}
}