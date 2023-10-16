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

 package com.google.cordova.plugin.browsertab;

 import android.app.Activity;
 import android.app.role.RoleManager;
 import android.content.ActivityNotFoundException;
 import android.content.Intent;
 import android.content.pm.PackageManager;
 import android.content.pm.ResolveInfo;
 import android.net.Uri;
 import android.os.Build;
 import android.os.Bundle;
 import android.provider.Settings;
 import android.util.Log;
 import android.webkit.WebChromeClient;
 
 import androidx.browser.customtabs.CustomTabsClient;
 import androidx.browser.customtabs.CustomTabsIntent;
 
 import org.apache.cordova.CallbackContext;
 import org.apache.cordova.CordovaPlugin;
 import org.apache.cordova.LOG;
 import org.apache.cordova.PluginResult;
 import org.json.JSONArray;
 import org.json.JSONException;
 
 import java.util.Collections;
 import java.util.Iterator;
 
 /**
  * Cordova plugin which provides the ability to launch a URL in an
  * in-app browser tab. On Android, this means using the custom tabs support
  * library, if a supporting browser (e.g. Chrome) is available on the device.
  */
 public class BrowserTab extends CordovaPlugin {
 
   public static final int RC_OPEN_URL = 101;
 
   private static final String LOG_TAG = "BrowserTab";
 
   /**
    * The service we expect to find on a web browser that indicates it supports custom tabs.
    */
   private static final String ACTION_CUSTOM_TABS_CONNECTION =
           "android.support.customtabs.action.CustomTabsService";
 
   private static final String SETTINGS_SELECT_OPTION_KEY = ":settings:fragment_args_key";
   private static final String SETTINGS_SHOW_FRAGMENT_ARGS = ":settings:show_fragment_args";
   private static final String DEFAULT_BROWSER_APP_OPTION = "default_browser";
 
   @Override
   public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
     Log.d(LOG_TAG, "executing " + action);
     if ("isAvailable".equals(action)) {
       isAvailable(callbackContext);
     } else if ("openUrl".equals(action)) {
       openUrl(args, callbackContext);
     }else // close is a NOP on Android
       if ("isCustomTabsSupported".equals(action)) {
             isCustomTabsSupported(callbackContext);
     } else {
         return "close".equals(action);
     }
 
     return true;
   }
 
   private void isAvailable(CallbackContext callbackContext) {
     callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, true));
   }
 
   private void isCustomTabsSupported(CallbackContext callbackContext) {
     String packages = CustomTabsClient.getPackageName(
             cordova.getActivity(),
             Collections.emptyList()
     );
 
     Intent intent = new Intent(Settings.ACTION_MANAGE_DEFAULT_APPS_SETTINGS);
     intent.putExtra(
             SETTINGS_SELECT_OPTION_KEY,
             DEFAULT_BROWSER_APP_OPTION
     );
     Bundle bundle = new Bundle();
     bundle.putString(SETTINGS_SELECT_OPTION_KEY, DEFAULT_BROWSER_APP_OPTION);
     intent.putExtra(
             SETTINGS_SHOW_FRAGMENT_ARGS,
             bundle
     );
     intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
     cordova.getActivity().startActivity(intent);
 
     if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
    /*   RoleManager roleManager = (RoleManager) getSystemService(ROLE_SERVICE);
       Intent intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_BROWSER);
       resultLauncher.launch(intent);*/
     } else {
 
     }
 
 
     boolean customTabsSupported = (packages != null);
     callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, customTabsSupported));
   }
 
   private void openUrl(JSONArray args, CallbackContext callbackContext) {
     if (args.length() < 1) {
       Log.d(LOG_TAG, "openUrl: no url argument received");
       callbackContext.error("URL argument missing");
       return;
     }
 
     String urlStr;
     try {
       urlStr = args.getString(0);
     } catch (JSONException e) {
       Log.d(LOG_TAG, "openUrl: failed to parse url argument");
       callbackContext.error("URL argument is not a string");
       return;
     }
 
     Intent customTabsIntent = new CustomTabsIntent.Builder().build().intent;
     customTabsIntent.setData(Uri.parse(urlStr));
     //cordova.getActivity().startActivity(customTabsIntent);
 
     try {
       cordova.startActivityForResult(new CordovaPlugin() {
         @Override
         public void onActivityResult(int requestCode, int resultCode, Intent intent) {
           if (resultCode !=  Activity.RESULT_OK) {
             callbackContext.error("ERROR_CANCELED_BY_USER");
           } else {
             callbackContext.success();
           }
         }
       }, customTabsIntent, -20231016);
     } catch (ActivityNotFoundException e) {
       LOG.w("No activity found to handle file chooser intent.", e);
       callbackContext.error("No activity found to handle the intent.");
     }
   }
 
   private boolean isFullBrowser(ResolveInfo resolveInfo) {
     // The filter must match ACTION_VIEW, CATEGORY_BROWSEABLE, and at least one scheme,
     if (!resolveInfo.filter.hasAction(Intent.ACTION_VIEW)
             || !resolveInfo.filter.hasCategory(Intent.CATEGORY_BROWSABLE)
             || resolveInfo.filter.schemesIterator() == null) {
         return false;
     }
 
     // The filter must not be restricted to any particular set of authorities
     if (resolveInfo.filter.authoritiesIterator() != null) {
         return false;
     }
 
     // The filter must support both HTTP and HTTPS.
     boolean supportsHttp = false;
     boolean supportsHttps = false;
     Iterator<String> schemeIter = resolveInfo.filter.schemesIterator();
     while (schemeIter.hasNext()) {
         String scheme = schemeIter.next();
         supportsHttp |= "http".equals(scheme);
         supportsHttps |= "https".equals(scheme);
 
         if (supportsHttp && supportsHttps) {
             return true;
         }
     }
 
     // at least one of HTTP or HTTPS is not supported
     return false;
   }
 
   private boolean hasCustomTabWarmupService(PackageManager pm, String packageName) {
     Intent serviceIntent = new Intent();
     serviceIntent.setAction(ACTION_CUSTOM_TABS_CONNECTION);
     serviceIntent.setPackage(packageName);
     return (pm.resolveService(serviceIntent, 0) != null);
   }
 }
 