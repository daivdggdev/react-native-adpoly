'use strict';

var RNAdPoly = require('react-native').NativeModules.RNAdPoly;
var IsAndroid = RNAdPoly.IsAndroid;

function init(appId, appKey) {
  RNAdPoly.init(appId, appKey);
}

function requestPermissionIfNecessary() {
  RNAdPoly.requestPermissionIfNecessary();
}

function showSplash(placementId) {
  RNAdPoly.showSplash(placementId);
}

function showFullScreenVideo(type, appKey, placementId) {
  RNAdPoly.showFullScreenVideo(type, appKey, placementId);
}

function loadRewardVideo(placementId) {
  RNAdPoly.loadRewardVideo(placementId);
}

function isRewardAdReady() {
  RNAdPoly.isRewardAdReady();
}

function showRewardVideo() {
  RNAdPoly.showRewardVideo();
}

module.exports = {
  init,
  requestPermissionIfNecessary,
  showSplash,
  showFullScreenVideo,
  loadRewardVideo,
  isRewardAdReady,
  showRewardVideo
};
