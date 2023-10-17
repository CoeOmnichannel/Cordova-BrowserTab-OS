/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */

var exec = require('cordova/exec');

exports.isAvailable = function(success, error) {
  exec(success, error, 'BrowserTab', 'isAvailable', []);
};

exports.openUrl = function(url, opt_error, forSession, schemeUrl, androidForceChrome) {
  var doNothing = function() {};
  var error = (!opt_error) ? doNothing : opt_error;
  exec(doNothing, error, 'BrowserTab', 'openUrl', [url, forSession, schemeUrl, androidForceChrome]);
};

exports.isCustomTabsSupported = function(success, error) {
    exec(success, error, 'BrowserTab', 'isCustomTabsSupported', []);
}

exports.isBrowserAppAvailable = function(success, error, browserPackage) {

  if(cordova.platformId === "iOS") {
    success(true);
  } else {
    exec(success, error, 'BrowserTab', 'isBrowserAppAvailable', [browserPackage]);
  }
}

exports.openAppStore = function(success, error, browserPackage, extraParams) {

  if(cordova.platformId === "iOS") {
    success(true);
  } else {
    exec(success, error, 'BrowserTab', 'openAppStore', [browserPackage, extraParams ? extraParams : ""]);
  }
}

exports.close = function(opt_error, forSession, schemeUrl) {
  var doNothing = function() {};
  var error = (!opt_error) ? doNothing : opt_error;
  exec(doNothing, error, 'BrowserTab', 'close', [forSession, schemeUrl]);
};

exports.getVersion = function() {
  let md = cordova.require("cordova/plugin_list").metadata
  return md["cordova-plugin-browsertab"] || "-1";
  //return "14";
};
