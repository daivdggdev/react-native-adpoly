'use strict';

var RNAdPoly = require('react-native').NativeModules.RNAdPoly;
var IsAndroid = RNAdPoly.IsAndroid;

function init(type, appKey) {
  RNAdPoly.init(type, appKey);
}

function requestPermissionIfNecessary() {
  RNAdPoly.requestPermissionIfNecessary();
}

function showSplash(type, appKey, placementId) {
  RNAdPoly.showSplash(type, appKey, placementId);
}

function loadFullScreenVideo(type, appKey, placementId) {
  RNAdPoly.loadFullScreenVideo(type, appKey, placementId);
}

function showFullScreenVideo(type, appKey, placementId) {
  RNAdPoly.showFullScreenVideo(type, appKey, placementId);
}

function loadRewardVideo(type, appKey, placementId, rewardName, rewardAmount) {
  RNAdPoly.loadRewardVideo(type, appKey, placementId, rewardName, rewardAmount);
}

function showRewardVideo(type, appKey, placementId, rewardName, rewardAmount) {
  RNAdPoly.showRewardVideo(type, appKey, placementId, rewardName, rewardAmount);
}

module.exports = {
  init,
  requestPermissionIfNecessary,
  showSplash,
  loadFullScreenVideo,
  showFullScreenVideo,
  loadRewardVideo,
  showRewardVideo
};
