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
#import <BUAdSDK/BUNativeExpressFullscreenVideoAd.h>
#import <BUAdSDK/BUNativeExpressRewardedVideoAd.h>
#import <BUAdSDK/BURewardedVideoModel.h>

static RNAdPoly *_instance = nil;

typedef NS_ENUM(NSInteger, AdSplashType)
{
    AdSplashType_GDT = 0,
    AdSplashType_BAIDU,
};

@interface RNAdPoly ()<GDTSplashAdDelegate, BUSplashAdDelegate, BUNativeExpressFullscreenVideoAdDelegate, BUNativeExpressRewardedVideoAdDelegate>
@property (nonatomic, strong) GDTSplashAd *gdtSplash;
//@property (nonatomic, strong) UIView *customSplashView;
@property (nonatomic, strong) UIView *bottomView;
@property (nonatomic, strong) BUSplashAdView *buSplash;
@property (nonatomic, strong) BUNativeExpressFullscreenVideoAd *fullscreenAd;
@property (nonatomic, strong) BUNativeExpressRewardedVideoAd *rewardedAd;
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
    return @[@"ShowSplashFailed", @"RewardDidSucceed", @"RewardDidClose", @"FullVideoAdDidClose"];
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
//    NSLog(@"setupGDTAdSDK sInitGDT: %@", self.sInitGDT);
    if (self.sInitGDT) {
        return;
    }

    [GDTSDKConfig registerAppId:appKey];
    self.sInitGDT = YES;
}

- (void)setupBUAdSDK:(NSString*)appKey handler:(BUCompletionHandler)completionHandler
{
//    NSLog(@"setupBUAdSDK sInitBU: %@", self.sInitBU);
    if (self.sInitBU && completionHandler != nil) {
        completionHandler(YES, nil);
        return;
    }
    
    [BUAdSDKManager setAppID:appKey];
    [BUAdSDKManager setLoglevel:BUAdSDKLogLevelVerbose];
    [BUAdSDKManager setCoppa:0];
    [BUAdSDKManager setGDPR:0];
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

- (void)loadBUFullscreenVideoAd:(NSString*)placementId
{
    self.fullscreenAd = [[BUNativeExpressFullscreenVideoAd alloc] initWithSlotID:placementId];
    self.fullscreenAd.delegate = self;
    [self.fullscreenAd loadAdData];
}

- (void)showBUFullscreenVideoAd
{
    if (self.fullscreenAd) {
        UIViewController *rootViewController = [UIApplication sharedApplication].keyWindow.rootViewController;
        [self.fullscreenAd showAdFromRootViewController:rootViewController];
    }
}

- (void)loadBURewardVideoAd:(NSString*)placementId 
                 rewardName:(NSString*)rewardName
               rewardAmount:(NSInteger)rewardAmount
{
    BURewardedVideoModel *model = [[BURewardedVideoModel alloc] init];
    model.rewardName = rewardName;
    model.rewardAmount = rewardAmount;
    self.rewardedAd = [[BUNativeExpressRewardedVideoAd alloc] initWithSlotID:placementId rewardedVideoModel:model];
    self.rewardedAd.delegate = self;
    [self.rewardedAd loadAdData];
}

- (void)showBURewardVideoAd
{
    if (self.rewardedAd) {
        UIViewController *rootViewController = [UIApplication sharedApplication].keyWindow.rootViewController;
        [self.rewardedAd showAdFromRootViewController:rootViewController];
    }
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
    RNAdPoly *manager = [RNAdPoly sharedInstance];
    if ([type isEqual:@"gdt"])
    {
        [manager setupGDTAdSDK:appKey];
    }
    else if ([type isEqual:@"tt"])
    {
        [manager setupBUAdSDK:appKey handler:nil];
    }
}


- (void)showSplashImpl:(NSString*)type
                appKey:(NSString*)appKey
           placementId:(NSString*)placementId
{
    RNAdPoly *manager = [RNAdPoly sharedInstance];
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
        }];
        
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
        
        if (@available(iOS 14, *)) {
          [ATTrackingManager requestTrackingAuthorizationWithCompletionHandler:^(ATTrackingManagerAuthorizationStatus status) {
              [self showSplashImpl:type appKey:appKey placementId:placementId];
          }];
        } else {
            [self showSplashImpl:type appKey:appKey placementId:placementId];
        }
    });
}

RCT_EXPORT_METHOD(loadFullScreenVideo:(NSString*)type
                  appKey:(NSString*)appKey
                  placementId:(NSString*)placementId)
{
    NSLog(@"loadFullScreenVideo type: %@, placementId: %@", type, placementId);
    RNAdPoly *manager = [RNAdPoly sharedInstance];
    if ([type isEqual:@"gdt"])
    {
        // [manager setupGDTAdSDK:appKey];
    }
    else if ([type isEqual:@"tt"])
    {
        [manager loadBUFullscreenVideoAd:placementId];
    }
}

RCT_EXPORT_METHOD(showFullScreenVideo:(NSString*)type
                  appKey:(NSString*)appKey
                  placementId:(NSString*)placementId)
{
    NSLog(@"showFullScreenVideo type: %@", type);
    RNAdPoly *manager = [RNAdPoly sharedInstance];
    if ([type isEqual:@"gdt"])
    {
        // [manager setupGDTAdSDK:appKey];
    }
    else if ([type isEqual:@"tt"])
    {
        dispatch_async(dispatch_get_main_queue(), ^{
            [manager showBUFullscreenVideoAd];
        });
    }
}

RCT_EXPORT_METHOD(loadRewardVideo:(NSString*)type
                  appKey:(NSString*)appKey
                  placementId:(NSString*)placementId
                  rewardName:(NSString*)rewardName
               rewardAmount:(NSInteger)rewardAmount)
{
    NSLog(@"loadRewardVideo type: %@, placementId: %@", type, placementId);
    RNAdPoly *manager = [RNAdPoly sharedInstance];
    if ([type isEqual:@"gdt"])
    {
        // [manager setupGDTAdSDK:appKey];
    }
    else if ([type isEqual:@"tt"])
    {
        [manager loadBURewardVideoAd:placementId 
                          rewardName:rewardName
                        rewardAmount:rewardAmount];
    }
}

RCT_EXPORT_METHOD(showRewardVideo:(NSString*)type
                  appKey:(NSString*)appKey
                  placementId:(NSString*)placementId
                  rewardName:(NSString*)rewardName
               rewardAmount:(NSInteger)rewardAmount)
{
    NSLog(@"showRewardVideo type: %@", type);
    RNAdPoly *manager = [RNAdPoly sharedInstance];
    if ([type isEqual:@"gdt"])
    {
        // [manager setupGDTAdSDK:appKey];
    }
    else if ([type isEqual:@"tt"])
    {
        dispatch_async(dispatch_get_main_queue(), ^{
            [manager showBURewardVideoAd];
        });
    }
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

#pragma mark - BUNativeExpressFullscreenVideoAdDelegate
- (void)nativeExpressFullscreenVideoAdDidLoad:(BUNativeExpressFullscreenVideoAd *)fullscreenVideoAd {
    [self pbud_logWithSEL:_cmd msg:@""];
}

- (void)nativeExpressFullscreenVideoAd:(BUNativeExpressFullscreenVideoAd *)fullscreenVideoAd didFailWithError:(NSError *_Nullable)error {
    [self pbud_logWithSEL:_cmd msg:[NSString stringWithFormat:@"%@", error]];
}

- (void)nativeExpressFullscreenVideoAdViewRenderSuccess:(BUNativeExpressFullscreenVideoAd *)rewardedVideoAd {
    [self pbud_logWithSEL:_cmd msg:@""];
}

- (void)nativeExpressFullscreenVideoAdViewRenderFail:(BUNativeExpressFullscreenVideoAd *)rewardedVideoAd error:(NSError *_Nullable)error {
    [self pbud_logWithSEL:_cmd msg:[NSString stringWithFormat:@"%@", error]];
}

- (void)nativeExpressFullscreenVideoAdDidDownLoadVideo:(BUNativeExpressFullscreenVideoAd *)fullscreenVideoAd {
    [self pbud_logWithSEL:_cmd msg:@""];
}

- (void)nativeExpressFullscreenVideoAdWillVisible:(BUNativeExpressFullscreenVideoAd *)fullscreenVideoAd {
    [self pbud_logWithSEL:_cmd msg:@""];
}

- (void)nativeExpressFullscreenVideoAdDidVisible:(BUNativeExpressFullscreenVideoAd *)fullscreenVideoAd {
    [self pbud_logWithSEL:_cmd msg:@""];
}

- (void)nativeExpressFullscreenVideoAdDidClick:(BUNativeExpressFullscreenVideoAd *)fullscreenVideoAd {
    [self pbud_logWithSEL:_cmd msg:@""];
}

- (void)nativeExpressFullscreenVideoAdDidClickSkip:(BUNativeExpressFullscreenVideoAd *)fullscreenVideoAd {
    [self pbud_logWithSEL:_cmd msg:@""];
}

- (void)nativeExpressFullscreenVideoAdWillClose:(BUNativeExpressFullscreenVideoAd *)fullscreenVideoAd {
    [self pbud_logWithSEL:_cmd msg:@""];
}

- (void)nativeExpressFullscreenVideoAdDidClose:(BUNativeExpressFullscreenVideoAd *)fullscreenVideoAd {
    [self pbud_logWithSEL:_cmd msg:@""];
    [self sendEventWithName:@"FullVideoAdDidClose" body:nil];
}

- (void)nativeExpressFullscreenVideoAdDidPlayFinish:(BUNativeExpressFullscreenVideoAd *)fullscreenVideoAd didFailWithError:(NSError *_Nullable)error {
    [self pbud_logWithSEL:_cmd msg:@""];
}

- (void)nativeExpressFullscreenVideoAdCallback:(BUNativeExpressFullscreenVideoAd *)fullscreenVideoAd withType:(BUNativeExpressFullScreenAdType) nativeExpressVideoAdType{
    [self pbud_logWithSEL:_cmd msg:@""];
}

- (void)nativeExpressFullscreenVideoAdDidCloseOtherController:(BUNativeExpressFullscreenVideoAd *)fullscreenVideoAd interactionType:(BUInteractionType)interactionType {
    NSString *str;
    if (interactionType == BUInteractionTypePage) {
        str = @"ladingpage";
    } else if (interactionType == BUInteractionTypeVideoAdDetail) {
        str = @"videoDetail";
    } else {
        str = @"appstoreInApp";
    }
    [self pbud_logWithSEL:_cmd msg:str];
}

#pragma mark - Log
- (void)pbud_logWithSEL:(SEL)sel msg:(NSString *)msg {
    NSLog(@"SDKDemoDelegate BUNativeExpressFullscreenVideoAd In VC (%@) extraMsg:%@", NSStringFromSelector(sel), msg);
}

#pragma mark - BUNativeExpressRewardedVideoAdDelegate
- (void)nativeExpressRewardedVideoAdDidLoad:(BUNativeExpressRewardedVideoAd *)rewardedVideoAd {
    [self pbud_logWithSEL:_cmd msg:@""];
}

- (void)nativeExpressRewardedVideoAd:(BUNativeExpressRewardedVideoAd *)rewardedVideoAd didFailWithError:(NSError *_Nullable)error {
    [self pbud_logWithSEL:_cmd msg:[NSString stringWithFormat:@"%@", error]];
}

- (void)nativeExpressRewardedVideoAdCallback:(BUNativeExpressRewardedVideoAd *)rewardedVideoAd withType:(BUNativeExpressRewardedVideoAdType)nativeExpressVideoType{
    [self pbud_logWithSEL:_cmd msg:@""];
}

- (void)nativeExpressRewardedVideoAdDidDownLoadVideo:(BUNativeExpressRewardedVideoAd *)rewardedVideoAd {
    [self pbud_logWithSEL:_cmd msg:@""];
}

- (void)nativeExpressRewardedVideoAdViewRenderSuccess:(BUNativeExpressRewardedVideoAd *)rewardedVideoAd {
    [self pbud_logWithSEL:_cmd msg:@""];
}

- (void)nativeExpressRewardedVideoAdViewRenderFail:(BUNativeExpressRewardedVideoAd *)rewardedVideoAd error:(NSError *_Nullable)error {
    [self pbud_logWithSEL:_cmd msg:[NSString stringWithFormat:@"%@", error]];
}

- (void)nativeExpressRewardedVideoAdWillVisible:(BUNativeExpressRewardedVideoAd *)rewardedVideoAd {
    [self pbud_logWithSEL:_cmd msg:@""];
}

- (void)nativeExpressRewardedVideoAdDidVisible:(BUNativeExpressRewardedVideoAd *)rewardedVideoAd {
    [self pbud_logWithSEL:_cmd msg:@""];
}

- (void)nativeExpressRewardedVideoAdWillClose:(BUNativeExpressRewardedVideoAd *)rewardedVideoAd {
    [self pbud_logWithSEL:_cmd msg:@""];
}

- (void)nativeExpressRewardedVideoAdDidClose:(BUNativeExpressRewardedVideoAd *)rewardedVideoAd {
    [self pbud_logWithSEL:_cmd msg:@""];
    self.rewardedAd = nil;
    [self sendEventWithName:@"RewardDidClose" body:nil];
}

- (void)nativeExpressRewardedVideoAdDidClick:(BUNativeExpressRewardedVideoAd *)rewardedVideoAd {
    [self pbud_logWithSEL:_cmd msg:@""];
}

- (void)nativeExpressRewardedVideoAdDidClickSkip:(BUNativeExpressRewardedVideoAd *)rewardedVideoAd {
    [self pbud_logWithSEL:_cmd msg:@""];
}

- (void)nativeExpressRewardedVideoAdDidPlayFinish:(BUNativeExpressRewardedVideoAd *)rewardedVideoAd didFailWithError:(NSError *_Nullable)error {
    [self pbud_logWithSEL:_cmd msg:[NSString stringWithFormat:@"%@", error]];
}

- (void)nativeExpressRewardedVideoAdServerRewardDidSucceed:(BUNativeExpressRewardedVideoAd *)rewardedVideoAd verify:(BOOL)verify {
    [self pbud_logWithSEL:_cmd msg:[NSString stringWithFormat:@"verify:%@ rewardName:%@ rewardMount:%ld",verify?@"true":@"false",rewardedVideoAd.rewardedVideoModel.rewardName,(long)rewardedVideoAd.rewardedVideoModel.rewardAmount]];
    
    [self sendEventWithName:@"RewardDidSucceed" body:nil];
}

- (void)nativeExpressRewardedVideoAdServerRewardDidFail:(BUNativeExpressRewardedVideoAd *)rewardedVideoAd error:(NSError * _Nullable)error {
    [self pbud_logWithSEL:_cmd msg:[NSString stringWithFormat:@"rewardName:%@ rewardMount:%ld error:%@",rewardedVideoAd.rewardedVideoModel.rewardName,(long)rewardedVideoAd.rewardedVideoModel.rewardAmount,error]];
}

- (void)nativeExpressRewardedVideoAdDidCloseOtherController:(BUNativeExpressRewardedVideoAd *)rewardedVideoAd interactionType:(BUInteractionType)interactionType {
    NSString *str;
    if (interactionType == BUInteractionTypePage) {
        str = @"ladingpage";
    } else if (interactionType == BUInteractionTypeVideoAdDetail) {
        str = @"videoDetail";
    } else {
        str = @"appstoreInApp";
    }
    [self pbud_logWithSEL:_cmd msg:str];
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
//        [[BUDAnimationTool sharedInstance] transitionFromView:splashAd toView:splashAd.zoomOutView splashCompletion:^{
//            [splashAd removeFromSuperview];
//        }];
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
//        [[BUDAnimationTool sharedInstance] transitionFromView:splashAd toView:splashAd.zoomOutView splashCompletion:^{
//            [self removeSplashAdView];
//        }];
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
