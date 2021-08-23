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

function showFullScreenVideo(type, appKey, placementId) {
  RNAdPoly.showFullScreenVideo(type, appKey, placementId);
}

module.exports = {
  init,
  requestPermissionIfNecessary,
  showSplash,
  showFullScreenVideo,
};
