/*! @file CBTBrowserTab.m
    @brief Browser tab plugin for Cordova
    @copyright
        Copyright 2016 Google Inc. All Rights Reserved.
    @copydetails
        Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at
        http://www.apache.org/licenses/LICENSE-2.0
        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License.
 */

#import "CBTBrowserTab.h"

API_AVAILABLE(ios(12.0))
ASWebAuthenticationSession *_asAuthenticationVC;

@implementation CBTBrowserTab {
  SFSafariViewController *_safariViewController;
}

- (void)isAvailable:(CDVInvokedUrlCommand *)command {
  BOOL available = ([SFSafariViewController class] != nil);
  CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                                messageAsBool:available];
  [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}

-(void)appIsActive {
    if (@available(iOS 12.0, *)) {
        [_asAuthenticationVC start];
    }
}

- (void)openUrl:(CDVInvokedUrlCommand *)command {
  NSString *urlString = command.arguments[0];
  if (urlString == nil) {
    CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR
                                                messageAsString:@"url can't be empty"];
    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
    return;
  }

  if ([SFSafariViewController class] != nil) {
    NSString *errorMessage = @"in app browser tab not available";
    CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR
                                                messageAsString:errorMessage];
    [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
  }

  /*_safariViewController = [[SFSafariViewController alloc] initWithURL:url];
  [self.viewController presentViewController:_safariViewController animated:YES completion:nil];
    */
    

    if (@available(iOS 12.0, *)) {
          NSString* redirectScheme =  @"exampleauth";
           NSURL* requestURL = [NSURL URLWithString:[command.arguments objectAtIndex:0]];

           ASWebAuthenticationSession* authenticationVC =
           [[ASWebAuthenticationSession alloc] initWithURL:requestURL
                                      callbackURLScheme: [[NSURL URLWithString: redirectScheme] scheme]
                                      completionHandler:^(NSURL * _Nullable callbackURL,
                                                          NSError * _Nullable error) {
                                          CDVPluginResult *result;
                                          if (callbackURL) {
                                              result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString: callbackURL.absoluteString];

                                          } else {
                                              result = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR messageAsString:@"error"];
                                          }

                                          [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
                                           _asAuthenticationVC = nil;
                                      }];

           // Need to keep a strong reference for < iOS 13.0 until the authentication session is complete
           _asAuthenticationVC = authenticationVC;
           if (@available(iOS 13.0, *)) {
               _asAuthenticationVC.presentationContextProvider = self;
           }

           UIApplicationState state = [UIApplication sharedApplication].applicationState;
           if (state != UIApplicationStateActive) {
               [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(appIsActive) name:UIApplicationDidBecomeActiveNotification object:nil];
           } else {
               [self appIsActive];
           }
       }
    
/*
    // Initialize the session.
    let session = ASWebAuthenticationSession(url: authURL, callbackURLScheme: scheme)
    { callbackURL, error in
        // Handle the callback.
    }
*/
//  CDVPluginResult *result = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK];
//  [self.commandDelegate sendPluginResult:result callbackId:command.callbackId];
}

- (nonnull ASPresentationAnchor)presentationAnchorForWebAuthenticationSession:(nonnull ASWebAuthenticationSession *)session API_AVAILABLE(ios(13.0)){
    return [[[UIApplication sharedApplication] windows] firstObject];
}


- (void)close:(CDVInvokedUrlCommand *)command {
  if (!_safariViewController) {
    return;
  }
  [_safariViewController dismissViewControllerAnimated:YES completion:nil];
  _safariViewController = nil;
}

@end
