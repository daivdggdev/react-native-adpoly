'use strict';

var RNAdPoly = require('react-native').NativeModules.RNAdPoly;
var IsAndroid = RNAdPoly.IsAndroid;

function showSplash(type, appKey, placementId) {
  RNAdPoly.showSplash(type, appKey, placementId);
}

module.exports = {
  showSplash: showSplash
};
