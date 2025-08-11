'use strict';

var RNAdPoly = require('react-native').NativeModules.RNAdPoly;
var IsAndroid = RNAdPoly.IsAndroid;

function init(type, appId, appKey, requestPermission) {
  RNAdPoly.init(type, appId, appKey, requestPermission);
}

function requestPermissionIfNecessary() {
  RNAdPoly.requestPermissionIfNecessary();
}

function showSplash(type, placementId) {
  RNAdPoly.showSplash(type, placementId);
}

function loadInterAd(type, placementId) {
  RNAdPoly.loadInterAd(type, placementId);
}

function showInterAd(type) {
  RNAdPoly.showInterAd(type);
}

function loadRewardVideo(type, placementId, rewardName, rewardAmount) {
  RNAdPoly.loadRewardVideo(type, placementId, rewardName, rewardAmount);
}

function showRewardVideo(type, rewardName, rewardAmount) {
  RNAdPoly.showRewardVideo(type, rewardName, rewardAmount);
}

function loadAndShowRewardVideo(type, placementId, rewardName, rewardAmount) {
  RNAdPoly.loadAndShowRewardVideo(type, placementId, rewardName, rewardAmount);
}

module.exports = {
  init,
  requestPermissionIfNecessary,
  showSplash,
  loadInterAd,
  showInterAd,
  loadRewardVideo,
  showRewardVideo,
  loadAndShowRewardVideo
};
