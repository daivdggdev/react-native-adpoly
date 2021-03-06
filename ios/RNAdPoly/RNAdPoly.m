//
//  RNAdPoly.m
//  RNAdPoly
//
//  Created by dwwang on 2017/8/24.
//  Copyright © 2017年 dwwang. All rights reserved.
//

#import "RNAdPoly.h"
#import <AppTrackingTransparency/AppTrackingTransparency.h>
#import <AdSupport/AdSupport.h>
#import <GDTSplashAd.h>
#import <GDTSDKConfig.h>
#import <Masonry/Masonry.h>
#import <BUAdSDK/BUAdSDKManager.h>
#import <BUAdSDK/BUSplashAdView.h>

static RNAdPoly *_instance = nil;

typedef NS_ENUM(NSInteger, AdSplashType)
{
    AdSplashType_GDT = 0,
    AdSplashType_BAIDU,
};

@interface RNAdPoly ()<GDTSplashAdDelegate, BUSplashAdDelegate>
@property (nonatomic, strong) GDTSplashAd *gdtSplash;
//@property (nonatomic, strong) UIView *customSplashView;
@property (nonatomic, strong) UIView *bottomView;
@property (nonatomic, strong) BUSplashAdView *buSplash;
@property (nonatomic, assign) BOOL sInitGDT;
@property (nonatomic, assign) BOOL sInitBU;

//@property (nonatomic, strong) IMNative* nativeAd;
//@property (nonatomic, strong) NSString* nativeContent;
//@property (nonatomic, strong) UIButton* skipButton;

@end

@implementation RNAdPoly

RCT_EXPORT_MODULE();

+ (instancetype)sharedInstance
{
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        if(_instance == nil) {
            _instance = [[self alloc] init];
            _instance.sInitGDT = NO;
            _instance.sInitBU = NO;
        }
    });
    return _instance;
}

+ (instancetype)allocWithZone:(struct _NSZone *)zone
{
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        if(_instance == nil) {
            _instance = [super allocWithZone:zone];
        }
    });
    return _instance;
}

+ (dispatch_queue_t)sharedMethodQueue
{
    static dispatch_queue_t methodQueue;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        methodQueue = dispatch_queue_create("react-native-ad-manager", DISPATCH_QUEUE_SERIAL);
    });
    return methodQueue;
}

- (dispatch_queue_t)methodQueue
{
    return [RNAdPoly sharedMethodQueue];
}

- (NSArray<NSString *> *)supportedEvents
{
    return @[@"ShowSplashFailed"];
}

- (void)drawBottomView
{
    UIWindow *window = [UIApplication sharedApplication].keyWindow;
    CGFloat screenWidth = window.frame.size.width;
    CGFloat screenHeight = window.frame.size.height;
    
    self.bottomView = [[UIView alloc] initWithFrame:CGRectMake(0, screenHeight - 120, screenWidth, 120)];
    self.bottomView.backgroundColor = [UIColor whiteColor];
    CALayer *upperBorder = [CALayer layer];
    upperBorder.backgroundColor = [[UIColor colorWithRed:200.f/255.f
                                                   green:199.f/255.f
                                                    blue:204.f/255.f
                                                   alpha:1] CGColor];
    upperBorder.frame = CGRectMake(0, 0, screenWidth, 0.5f);
    [self.bottomView.layer addSublayer:upperBorder];
    
    UIImageView *imageView = [[UIImageView alloc] init];
    imageView.image = [UIImage imageNamed:@"icon-50"];
    [self.bottomView addSubview:imageView];
    
    UILabel *appNameLabel = [[UILabel alloc] init];
    appNameLabel.text = @"口袋五线谱";
    appNameLabel.font = [UIFont systemFontOfSize:22];
    appNameLabel.textAlignment = NSTextAlignmentLeft;
    [self.bottomView addSubview:appNameLabel];
    
    UILabel *sloganLabel = [[UILabel alloc] init];
    sloganLabel.text = @"离弹钢琴更进一步";
    sloganLabel.font = [UIFont systemFontOfSize:14];
    sloganLabel.textColor = [UIColor colorWithRed:85.f/255.f
                                            green:87.f/255.f
                                             blue:85.f/255.f
                                            alpha:1];
    sloganLabel.textAlignment = NSTextAlignmentLeft;
    [self.bottomView addSubview:sloganLabel];
    
    [imageView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.centerY.equalTo(self.bottomView.mas_centerY);
        make.centerX.equalTo(self.bottomView.mas_centerX).with.offset(-60);
    }];
    
    [appNameLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.top.equalTo(imageView.mas_top).with.offset(4);
        make.left.equalTo(imageView.mas_right).with.offset(10);
    }];
    
    [sloganLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.top.equalTo(appNameLabel.mas_bottom).with.offset(-1);
        make.left.equalTo(appNameLabel.mas_left);
    }];
}

- (void)setupGDTAdSDK:(NSString*)appKey
{
    NSLog(@"setupGDTAdSDK sInitGDT: %@", self.sInitGDT);
    if (self.sInitGDT) {
        return;
    }

    [GDTSDKConfig registerAppId:appKey];
    [GDTSDKConfig enableGPS:YES];
    self.sInitGDT = YES;
}

- (void)setupBUAdSDK:(NSString*)appKey handler:(BUCompletionHandler)completionHandler
{
    NSLog(@"setupBUAdSDK sInitBU: %@", self.sInitBU);
    if (self.sInitBU) {
        completionHandler(YES, nil);
        return;
    }
    
    NSInteger territory = [[NSUserDefaults standardUserDefaults]integerForKey:@"territory"];
    BOOL isNoCN = (territory > 0 && territory != BUAdSDKTerritory_CN);
    
    BUAdSDKConfiguration *configuration = [BUAdSDKConfiguration configuration];
    configuration.territory = isNoCN ? BUAdSDKTerritory_NO_CN : BUAdSDKTerritory_CN;
    configuration.GDPR = @(0);
    configuration.coppa = @(0);
    configuration.CCPA = @(1);
    configuration.logLevel = BUAdSDKLogLevelVerbose;
    configuration.appID = appKey;
    [BUAdSDKManager startWithAsyncCompletionHandler:^(BOOL success, NSError *error) {
        self.sInitBU = success;
    }];
    
//    ///optional
//    ///CN china, NO_CN is not china
//    ///you must set Territory first,  if you need to set them
//    [BUAdSDKManager setTerritory:isNoCN?BUAdSDKTerritory_NO_CN:BUAdSDKTerritory_CN];
//    //optional
//    //GDPR 0 close privacy protection, 1 open privacy protection
//    [BUAdSDKManager setGDPR:0];
//    //optional
//    //Coppa 0 adult, 1 child
//    [BUAdSDKManager setCoppa:0];
//    // you can set idfa by yourself, it is optional and maybe will never be used.
//    [BUAdSDKManager setCustomIDFA:@"12345678-1234-1234-1234-123456789012"];
//#if DEBUG
//    // Whether to open log. default is none.
//    [BUAdSDKManager setLoglevel:BUAdSDKLogLevelDebug];
//#endif
//    //BUAdSDK requires iOS 9 and up
//    [BUAdSDKManager setAppID:[BUDAdManager appKey]];
//
//    [BUAdSDKManager setIsPaidApp:NO];
    
    
}

- (void)showGdtSplash:(NSString*)placementId
{
    UIWindow *window = [UIApplication sharedApplication].keyWindow;
    
    //开屏广告初始化并展示代码
    self.gdtSplash = [[GDTSplashAd alloc] initWithPlacementId:placementId];
    self.gdtSplash.delegate = self;
    
    if ([[UIScreen mainScreen] bounds].size.height >= 568.0f)
    {
        self.gdtSplash.backgroundColor = [UIColor colorWithPatternImage:[UIImage imageNamed:@"LaunchImage-568h"]];
    }
    else
    {
        self.gdtSplash.backgroundColor = [UIColor colorWithPatternImage:[UIImage imageNamed:@"LaunchImage"]];
    }
    self.gdtSplash.fetchDelay = 3; //开发者可以设置开屏拉取时间，超时则放弃展示 //[可选]拉取并展示全屏开屏广告
    
    [self.gdtSplash loadAdAndShowInWindow:window withBottomView:self.bottomView];
}

- (void)showBuSplash:(NSString*)placementId;
{
    CGRect frame = [UIScreen mainScreen].bounds;
    self.buSplash = [[BUSplashAdView alloc] initWithSlotID:placementId frame:frame];
    self.buSplash.tolerateTimeout = 3.5;
    self.buSplash.delegate = self;
    
    UIWindow *window = [UIApplication sharedApplication].keyWindow;
    [window.rootViewController.view addSubview:self.buSplash];
    self.buSplash.rootViewController = window.rootViewController;
    [self.buSplash loadAdData];
}

RCT_EXPORT_METHOD(init:(NSString*)type
                  appKey:(NSString*)appKey)
{
    NSLog(@"init type: %@, appKey: %@", type, appKey);
    if ([type isEqual:@"gdt"])
    {
        [manager setupGDTAdSDK:appKey];
    }
    else if ([type isEqual:@"tt"])
    {
        [manager setupBUAdSDK:appKey handler:nil];
    }
}

RCT_EXPORT_METHOD(showSplash:(NSString*)type
                  appKey:(NSString*)appKey
                  placementId:(NSString*)placementId)
{
    dispatch_async(dispatch_get_main_queue(), ^{
        RNAdPoly *manager = [RNAdPoly sharedInstance];
        
        NSLog(@"showSplash type: %@, placementId: %@", type, placementId);
        if (!self.bottomView)
        {
            [manager drawBottomView];
        }
        
        [ATTrackingManager requestTrackingAuthorizationWithCompletionHandler:^(ATTrackingManagerAuthorizationStatus status) {
            // Tracking authorization completed. Start loading ads here.
            // [self loadAd];

            if ([type isEqual:@"gdt"])
            {
                [manager setupGDTAdSDK:appKey];
                [manager showGdtSplash:placementId];
            }
            else if ([type isEqual:@"tt"])
            {
                [manager setupBUAdSDK:appKey handler:^(BOOL success, NSError *error) {
                    if (!success) {
                        NSLog(@"setupBUAdSDK error: %@", error);
                        return;
                    }

                    dispatch_async(dispatch_get_main_queue(), ^{
                        [manager showBuSplash:placementId];
                    });
                }]
                
            }
        }
    });
}

#pragma mark Gdt Splash Delegate

- (void)splashAdSuccessPresentScreen:(GDTSplashAd *)splashAd
{
    NSLog(@"%s",__FUNCTION__);
}

- (void)splashAdFailToPresent:(GDTSplashAd *)splashAd withError:(NSError *)error
{
    NSLog(@"%s%@",__FUNCTION__,error);
    [self sendEventWithName:@"ShowSplashFailed" body:nil];
}

- (void)splashAdClicked:(GDTSplashAd *)splashAd
{
    NSLog(@"%s",__FUNCTION__);
}

- (void)splashAdApplicationWillEnterBackground:(GDTSplashAd *)splashAd
{
    NSLog(@"%s",__FUNCTION__);
}

- (void)splashAdWillClosed:(GDTSplashAd *)splashAd
{
    NSLog(@"%s",__FUNCTION__);
}

- (void)splashAdClosed:(GDTSplashAd *)splashAd
{
    NSLog(@"%s",__FUNCTION__);
    [self removeGDTSplash];
}

/**
 *  展示结束or展示失败后, 手动移除splash和delegate
 */
- (void) removeGDTSplash
{
    if (self.gdtSplash)
    {
        self.gdtSplash.delegate = nil;
        self.gdtSplash = nil;
    }
}

#pragma mark delegate

- (void)splashAdDidLoad:(BUSplashAdView *)splashAd {
    if (splashAd.zoomOutView) {
        UIViewController *parentVC = [UIApplication sharedApplication].keyWindow.rootViewController;
        //Add this view to your container
        [parentVC.view insertSubview:splashAd.zoomOutView belowSubview:splashAd];
        splashAd.zoomOutView.rootViewController = parentVC;
        splashAd.zoomOutView.delegate = self;
    }
}

- (void)splashAdDidClose:(BUSplashAdView *)splashAd {
    if (splashAd.zoomOutView) {
        [[BUDAnimationTool sharedInstance] transitionFromView:splashAd toView:splashAd.zoomOutView splashCompletion:^{
            [splashAd removeFromSuperview];
        }];
    } else{
        // Be careful not to say 'self.splashadview = nil' here.
        // Subsequent agent callbacks will not be triggered after the 'splashAdView' is released early.
        [splashAd removeFromSuperview];
    }
}

- (void)splashAdDidClick:(BUSplashAdView *)splashAd {
    if (splashAd.zoomOutView) {
        [splashAd.zoomOutView removeFromSuperview];
    }
    // Be careful not to say 'self.splashadview = nil' here.
    // Subsequent agent callbacks will not be triggered after the 'splashAdView' is released early.
    [splashAd removeFromSuperview];
}

- (void)splashAdDidClickSkip:(BUSplashAdView *)splashAd {
    if (splashAd.zoomOutView) {
        [[BUDAnimationTool sharedInstance] transitionFromView:splashAd toView:splashAd.zoomOutView splashCompletion:^{
            [self removeSplashAdView];
        }];
    } else{
        // Click Skip, there is no subsequent operation, completely remove 'splashAdView', avoid memory leak
        [self removeSplashAdView];
    }
}

- (void)splashAd:(BUSplashAdView *)splashAd didFailWithError:(NSError *)error {
    [self removeSplashAdView];
    NSLog(@"%s%@",__FUNCTION__,error);
    [self sendEventWithName:@"ShowSplashFailed" body:nil];
}

- (void)splashAdWillVisible:(BUSplashAdView *)splashAd {
}

- (void)splashAdWillClose:(BUSplashAdView *)splashAd {
}

- (void)splashAdDidCloseOtherController:(BUSplashAdView *)splashAd interactionType:(BUInteractionType)interactionType {
    // No further action after closing the other Controllers, completely remove the 'splashAdView' and avoid memory leaks
    [self removeSplashAdView];
}

- (void)splashAdCountdownToZero:(BUSplashAdView *)splashAd {
    // When the countdown is over, it is equivalent to clicking Skip to completely remove 'splashAdView' and avoid memory leak
    if (!splashAd.zoomOutView) {    
        [self removeSplashAdView];
    }
}

#pragma mark - BUSplashZoomOutViewDelegate
- (void)splashZoomOutViewAdDidClick:(BUSplashZoomOutView *)splashAd {
}

- (void)splashZoomOutViewAdDidClose:(BUSplashZoomOutView *)splashAd {
    // Click close, completely remove 'splashAdView', avoid memory leak
    [self removeSplashAdView];
}

- (void)splashZoomOutViewAdDidAutoDimiss:(BUSplashZoomOutView *)splashAd {
    // Back down at the end of the countdown to completely remove the 'splashAdView' to avoid memory leaks
    [self removeSplashAdView];
}

- (void)splashZoomOutViewAdDidCloseOtherController:(BUSplashZoomOutView *)splashAd interactionType:(BUInteractionType)interactionType {
    // No further action after closing the other Controllers, completely remove the 'splashAdView' and avoid memory leaks
    [self removeSplashAdView];
}



- (void)removeSplashAdView {
    if (self.buSplash) {
        [self.buSplash removeFromSuperview];
        self.buSplash = nil;
    }
}

@end
