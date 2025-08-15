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
#import <GDTMobSDK/GDTSplashAd.h>
#import <GDTMobSDK/GDTSDKConfig.h>
#import <GDTMobSDK/GDTUnifiedInterstitialAd.h>
#import <GDTMobSDK/GDTRewardVideoAd.h>

#import <Masonry/Masonry.h>
#import <BUAdSDK/BUAdSDK.h>
#import <BUAdSDK/BUAdSDKManager.h>
#import <BUAdSDK/BUNativeExpressFullscreenVideoAd.h>
#import <BUAdSDK/BUNativeExpressRewardedVideoAd.h>
#import <BUAdSDK/BURewardedVideoModel.h>

#import <KSAdSDK/KSAdSDK.h>

#import <WindSDK/WindSDK.h>

#import <BaiduMobAdSDK/BaiduMobAdExpressInterstitial.h>
#import <BaiduMobAdSDK/BaiduMobAdExpressIntDelegate.h>
#import <BaiduMobAdSDK/BaiduMobAdRewardVideo.h>
#import <BaiduMobAdSDK/BaiduMobAdSetting.h>
#import <BaiduMobAdSDK/BaiduMobAdSplash.h>
#import <BaiduMobAdSDK/BaiduMobAdManager.h>

static RNAdPoly *_instance = nil;

typedef NS_ENUM(NSInteger, AdSplashType)
{
    AdSplashType_GDT = 0,
    AdSplashType_BAIDU,
};

@interface RNAdPoly ()<GDTSplashAdDelegate, BUSplashAdDelegate, GDTUnifiedInterstitialAdDelegate, GDTRewardedVideoAdDelegate, BUNativeExpressFullscreenVideoAdDelegate, BUNativeExpressRewardedVideoAdDelegate, KSInterstitialAdDelegate, KSRewardedVideoAdDelegate, WindNewIntersititialAdDelegate, WindRewardVideoAdDelegate, BaiduMobAdExpressIntDelegate, BaiduMobAdRewardVideoDelegate>
@property (nonatomic, strong) GDTSplashAd *gdtSplash;
//@property (nonatomic, strong) UIView *customSplashView;
@property (nonatomic, strong) UIView *bottomView;
@property (nonatomic, strong) BUSplashAd *buSplash;
@property (nonatomic, strong) BUNativeExpressFullscreenVideoAd *fullscreenAd;
@property (nonatomic, strong) BUNativeExpressRewardedVideoAd *rewardedAd;
@property (nonatomic, assign) BOOL sInitGDT;
@property (nonatomic, assign) BOOL sInitBU;
@property (nonatomic, assign) BOOL sInitKS;
@property (nonatomic, assign) BOOL sInitSigmob;
@property (nonatomic, assign) BOOL sInitBaidu;
@property (nonatomic, assign) BOOL isRewardSuccess;
@property (nonatomic, assign) BOOL isLoadAnShowReward;

@property (nonatomic, strong) GDTUnifiedInterstitialAd *gdtInterstitial;
@property (nonatomic, strong) GDTRewardVideoAd *gdtRewardVideoAd;

@property (nonatomic, strong) KSInterstitialAd *ksInterstitial;
@property (nonatomic, strong) KSRewardedVideoAd *ksRewardVideoAd;

@property (nonatomic, strong) WindNewIntersititialAd *sigmobIntersititialAd;
@property (nonatomic, strong) WindRewardVideoAd *sigmobRewardVideoAd;

@property (nonatomic, strong) BaiduMobAdExpressInterstitial *baiduInterstitialAd;
@property (nonatomic, strong) BaiduMobAdRewardVideo *baiduRewardVideoAd;

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
    return @[@"AdInitSuccess",
             @"ShowSplashFailed",
             @"RewardDidLoad",
             @"RewardDidSucceed",
             @"RewardDidClose",
             @"FullVideoAdDidSucceed",
             @"FullVideoAdDidFailed",
             @"FullVideoAdDidClose"];
}

- (UIWindow*)getKeyWindow
{
    UIWindow *keyWindow = nil;
    for (UIWindowScene *scene in [UIApplication sharedApplication].connectedScenes) {
        if (scene.activationState == UISceneActivationStateForegroundActive) {
            for (UIWindow *window in scene.windows) {
                if (window.isKeyWindow) {
                    keyWindow = window;
                    break;
                }
            }
        }
        if (keyWindow) break;
    }
    return keyWindow;
}

- (void)drawBottomView
{
    UIWindow *window = [self getKeyWindow];
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

- (void)setupGDTAdSDK:(NSString*)appId
               appKey:(NSString*)appKey
{
//    NSLog(@"setupGDTAdSDK sInitGDT: %@", self.sInitGDT);
    if (self.sInitGDT) {
        return;
    }

    BOOL result = [GDTSDKConfig initWithAppId:appId];
    if (result) {
        NSLog(@"初始化成功");
    }
    [GDTSDKConfig startWithCompletionHandler:^(BOOL success, NSError *error) {
        self.sInitGDT = success;
        [self sendEventWithName:@"AdInitSuccess" body:@{@"type": @"gdt"}];
    }];
}

- (void)setupBUAdSDK:(NSString*)appId
              appKey:(NSString*)appKey
             handler:(BUCompletionHandler)completionHandler
{
//    NSLog(@"setupBUAdSDK sInitBU: %@", self.sInitBU);
    if (self.sInitBU && completionHandler != nil) {
        completionHandler(YES, nil);
        return;
    }
    
    BUAdSDKConfiguration *configuration = [BUAdSDKConfiguration configuration];
    configuration.appID = appId;
//    configuration.privacyProvider = [[BUDPrivacyProvider alloc] init];
    configuration.appLogoImage = [UIImage imageNamed:@"AppIcon"];
    [BUAdSDKManager startWithAsyncCompletionHandler:^(BOOL success, NSError *error) {
        self.sInitBU = success;
        [self sendEventWithName:@"AdInitSuccess" body:@{@"type": @"tt"}];
    }];    
}

- (void)setupKSAdSDK:(NSString*)appId
              appKey:(NSString*)appKey
{
//    NSLog(@"setupGDTAdSDK sInitGDT: %@", self.sInitGDT);
    if (self.sInitKS) {
        return;
    }

    KSAdSDKConfiguration *configuration = [KSAdSDKConfiguration configuration];
    configuration.appId = appId;

    // 启动SDK：SDK启动成功后，才可以继续进行后续的广告请求操作（异步）
    [KSAdSDKManager startWithCompletionHandler:^(BOOL success, NSError *error) {
        self.sInitKS = success;
        [self sendEventWithName:@"AdInitSuccess" body:@{@"type": @"ks"}];
    }];
}

- (void)setupSigmobAdSDK:(NSString*)appId
                  appKey:(NSString*)appKey
{
//    NSLog(@"setupGDTAdSDK sInitGDT: %@", self.sInitGDT);
    if (self.sInitSigmob) {
        return;
    }

    WindAdOptions *option = [[WindAdOptions alloc] initWithAppId:appId appKey:appKey];
    [WindAds startWithOptions:option];
    self.sInitSigmob = YES;
    [self sendEventWithName:@"AdInitSuccess" body:@{@"type": @"sigmob"}];
}

- (void)setupBaiduAdSDK:(NSString*)appId
                 appKey:(NSString*)appKey
{
//    NSLog(@"setupGDTAdSDK sInitGDT: %@", self.sInitGDT);
    if (self.sInitBaidu) {
        return;
    }

    [BaiduMobAdManager setAppsid:appId];
    [BaiduMobAdManager startWithCompletionHandler:^(BOOL success, NSError * _Nullable error) {
        // 初始化成功
        if (success) {
            self.sInitBaidu = YES;
            [self sendEventWithName:@"AdInitSuccess" body:@{@"type": @"baidu"}];
        }
    }];
}

- (void)loadGDTFullscreenVideoAd:(NSString*)placementId
{
    if (self.gdtInterstitial) {
        self.gdtInterstitial.delegate = nil;
    }
    self.gdtInterstitial = [[GDTUnifiedInterstitialAd alloc] initWithPlacementId:placementId];
    self.gdtInterstitial.delegate = self;
    [self.gdtInterstitial loadAd]; // 加载插屏半屏广告
}

- (void)showGDTFullscreenVideoAd
{
    if ([self.gdtInterstitial isAdValid]) {
        UIViewController *rootViewController = [self getKeyWindow].rootViewController;
        [self.gdtInterstitial presentAdFromRootViewController:rootViewController];
    }
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
        UIViewController *rootViewController = [self getKeyWindow].rootViewController;
        [self.fullscreenAd showAdFromRootViewController:rootViewController];
    }
}

- (void)loadKSFullscreenVideoAd:(NSString*)placementId
{
    if (self.ksInterstitial) {
        self.ksInterstitial.delegate = nil;
        self.ksInterstitial = nil;
    }
    self.ksInterstitial = [[KSInterstitialAd alloc] initWithPosId:placementId];
    self.ksInterstitial.delegate = self;
    [self.ksInterstitial loadAdData]; // 加载插屏半屏广告
}

- (void)showKSFullscreenVideoAd
{
    if (self.ksInterstitial) {
        UIViewController *rootViewController = [self getKeyWindow].rootViewController;
        [self.ksInterstitial showFromViewController:rootViewController];
    }
}

- (void)loadSigmobFullscreenVideoAd:(NSString*)placementId
{
    WindAdRequest *request = [WindAdRequest request];
//    request.userId = @"your user id";
    request.placementId = placementId;
//    request.options = @{@"test_key":@"test_value"};
    if (!self.sigmobIntersititialAd) {
        self.sigmobIntersititialAd = [[WindNewIntersititialAd alloc] initWithRequest:request];
    }
    self.sigmobIntersititialAd.delegate = self;
    [self.sigmobIntersititialAd loadAdData];
}

- (void)showSigmobFullscreenVideoAd
{
    if (!self.sigmobIntersititialAd.isAdReady) {
        return;
    }
    //当多场景使用同一个广告位是，可以通过WindMillAdSceneId来区分某个场景的广告播放数据
    //不需要统计可以设置为options=nil
    UIViewController *rootViewController = [self getKeyWindow].rootViewController;
    [self.sigmobIntersititialAd showAdFromRootViewController:rootViewController options:nil];
}

- (void)loadBaiduFullscreenVideoAd:(NSString*)placementId
{
    BaiduMobAdFeedRequestParameters *parameters = [[BaiduMobAdFeedRequestParameters alloc] init];
//    [parameters addCustExtParametersKey:@"baidu1" value:@"bd1234"];
//    NSTimeInterval date = [[NSDate date] timeIntervalSince1970];
//    NSString *md5String = [NSString stringWithFormat:@"cust_Value_这是时间戳 %f",date];
//    [parameters addCustExtParametersKey:@"cust_Key_这是key" value:md5String];
    self.baiduInterstitialAd.requestParameters = parameters;
    
    [self.baiduInterstitialAd load];
}

- (void)showBaiduFullscreenVideoAd
{
    if (![self.baiduInterstitialAd isReady]) {
        return;
    }
    //当多场景使用同一个广告位是，可以通过WindMillAdSceneId来区分某个场景的广告播放数据
    //不需要统计可以设置为options=nil
    UIViewController *rootViewController = [self getKeyWindow].rootViewController;
    [self.baiduInterstitialAd showFromViewController:rootViewController];
}

- (void)loadGDTRewardVideoAd:(NSString*)placementId 
                  rewardName:(NSString*)rewardName
                rewardAmount:(NSInteger)rewardAmount
{
    self.gdtRewardVideoAd = [[GDTRewardVideoAd alloc] initWithPlacementId:placementId];
    self.gdtRewardVideoAd.delegate = self;
    [self.gdtRewardVideoAd loadAd];
}

- (void)showGDTRewardVideoAd
{
    if (self.gdtRewardVideoAd.isAdValid) {
        UIViewController *rootViewController = [self getKeyWindow].rootViewController;
        [self.gdtRewardVideoAd showAdFromRootViewController:rootViewController];
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
        UIViewController *rootViewController = [self getKeyWindow].rootViewController;
        [self.rewardedAd showAdFromRootViewController:rootViewController];
    }
}

- (void)loadKSRewardVideoAd:(NSString*)placementId 
                 rewardName:(NSString*)rewardName
               rewardAmount:(NSInteger)rewardAmount
{
    if (self.ksRewardVideoAd) {
        self.ksRewardVideoAd.delegate = nil;
        self.ksRewardVideoAd.innerDelegate = nil;
        self.ksRewardVideoAd = nil;
    }
    // 激励视频广告
    KSRewardedVideoModel *model = [KSRewardedVideoModel new];
    self.ksRewardVideoAd = [[KSRewardedVideoAd alloc] initWithPosId:placementId rewardedVideoModel:model];
    self.ksRewardVideoAd.delegate = self;
    [self.ksRewardVideoAd loadAdData];
}

- (void)showKSRewardVideoAd
{
    if (self.ksRewardVideoAd.isValid) {
        UIViewController *rootViewController = [self getKeyWindow].rootViewController;
        [self.ksRewardVideoAd showAdFromRootViewController:rootViewController];
    }
}

- (void)loadSigmobRewardVideoAd:(NSString*)placementId
                  rewardName:(NSString*)rewardName
                rewardAmount:(NSInteger)rewardAmount
{
    WindAdRequest *request = [WindAdRequest request];
//    request.userId = @"your user id";
    request.placementId = placementId;
//    request.options = @{@"test_key":@"test_value"};
    if (!self.sigmobRewardVideoAd) {
        self.sigmobRewardVideoAd = [[WindRewardVideoAd alloc] initWithRequest:request];
    }
    self.sigmobRewardVideoAd.delegate = self;
    [self.sigmobRewardVideoAd loadAdData];
}

- (void)showSigmobRewardVideoAd
{
    if (!self.sigmobRewardVideoAd.isAdReady) {
        return;
    }
    //当多场景使用同一个广告位是，可以通过WindAdSceneId来区分某个场景的广告播放数据
    //不需要统计可以设置为options=nil
    UIViewController *rootViewController = [self getKeyWindow].rootViewController;
    [self.sigmobRewardVideoAd showAdFromRootViewController:rootViewController options:nil];
}

- (void)loadBaiduRewardVideoAd:(NSString*)placementId
                    rewardName:(NSString*)rewardName
                  rewardAmount:(NSInteger)rewardAmount
{
    BaiduMobAdFeedRequestParameters *parameters = [[BaiduMobAdFeedRequestParameters alloc] init];
//    [parameters addCustExtParametersKey:@"baidu1" value:@"bd1234"];
//    NSTimeInterval date = [[NSDate date] timeIntervalSince1970];
//    NSString *md5String = [NSString stringWithFormat:@"cust_Value_这是时间戳 %f",date];
//    [parameters addCustExtParametersKey:@"cust_Key_这是key" value:md5String];
    self.baiduRewardVideoAd.requestParameters = parameters;
    [self.baiduRewardVideoAd load];
}

- (void)showBaiduRewardVideoAd
{
    if (self.baiduRewardVideoAd.isReady) {
        UIViewController *rootViewController = [self getKeyWindow].rootViewController;
        [self.baiduRewardVideoAd showFromViewController:rootViewController];
    }
}

- (void)showGdtSplash:(NSString*)placementId
{
    UIWindow *window = [self getKeyWindow];
    
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
    CGRect splashFrame = CGRectMake(0, 0, frame.size.width, frame.size.height - 120);
      
    self.buSplash = [[BUSplashAd alloc] initWithSlotID:placementId adSize:splashFrame.size];
      // 不支持中途更改代理，中途更改代理会导致接收不到广告相关回调，如若存在中途更改代理场景，需自行处理相关逻辑，确保广告相关回调正常执行。
    self.buSplash.delegate = self;
//    self.buSplash.cardDelegate = self;
    self.buSplash.supportCardView = YES;
    self.buSplash.tolerateTimeout = 3;
    [self.buSplash loadAdData];
}

RCT_EXPORT_METHOD(init:(NSString*)type
                  appId:(NSString*)appId
                  appKey:(NSString*)appKey
                  requestPermission:(BOOL)requestPermission)
{
    dispatch_async(dispatch_get_main_queue(), ^{
        NSLog(@"init type: %@, appId: %@, appKey: %@", type, appId, appKey);
        RNAdPoly *manager = [RNAdPoly sharedInstance];
        if ([type isEqual:@"gdt"])
        {
            [manager setupGDTAdSDK:appId
                            appKey:appKey];
        }
        else if ([type isEqual:@"tt"])
        {
            [manager setupBUAdSDK:appId
                           appKey:appKey
                          handler:nil];
        }
        else if ([type isEqual:@"ks"])
        {
            [manager setupKSAdSDK:appId
                           appKey:appKey];
        }
        else if ([type isEqual:@"sigmob"])
        {
            [manager setupSigmobAdSDK:appId
                               appKey:appKey];
        }
        else if ([type isEqual:@"baidu"])
        {
            [manager setupBaiduAdSDK:appId
                              appKey:appKey];
        }
    });
}


- (void)showSplashImpl:(NSString*)type
           placementId:(NSString*)placementId
{
    RNAdPoly *manager = [RNAdPoly sharedInstance];
    if ([type isEqual:@"gdt"])
    {
        [manager showGdtSplash:placementId];
    }
    else if ([type isEqual:@"tt"])
    {
        [manager showBuSplash:placementId];
    }
}

RCT_EXPORT_METHOD(showSplash:(NSString*)type
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
              [self showSplashImpl:type placementId:placementId];
          }];
        } else {
            [self showSplashImpl:type placementId:placementId];
        }
    });
}

RCT_EXPORT_METHOD(loadInterAd:(NSString*)type
                  placementId:(NSString*)placementId)
{
    NSLog(@"loadInterAd type: %@, placementId: %@", type, placementId);
    dispatch_async(dispatch_get_main_queue(), ^{
        RNAdPoly *manager = [RNAdPoly sharedInstance];
        if ([type isEqual:@"gdt"])
        {
            [manager loadGDTFullscreenVideoAd:placementId];
        }
        else if ([type isEqual:@"tt"])
        {
            [manager loadBUFullscreenVideoAd:placementId];
        }
        else if ([type isEqual:@"ks"])
        {
            [manager loadKSFullscreenVideoAd:placementId];
        }
        else if ([type isEqual:@"sigmob"])
        {
            [manager loadSigmobFullscreenVideoAd:placementId];
        }
        else if ([type isEqual:@"baidu"])
        {
            [manager loadBaiduFullscreenVideoAd:placementId];
        }
    });
}

RCT_EXPORT_METHOD(showInterAd:(NSString*)type)
{
    NSLog(@"showFullScreenVideo type: %@", type);
    dispatch_async(dispatch_get_main_queue(), ^{
        RNAdPoly *manager = [RNAdPoly sharedInstance];
        if ([type isEqual:@"gdt"])
        {
            [manager showGDTFullscreenVideoAd];
        }
        else if ([type isEqual:@"tt"])
        {
            [manager showBUFullscreenVideoAd];
        }
        else if ([type isEqual:@"ks"])
        {
            [manager showKSFullscreenVideoAd];
        }
        else if ([type isEqual:@"sigmob"])
        {
            [manager showSigmobFullscreenVideoAd];
        }
        else if ([type isEqual:@"baidu"])
        {
            [manager showBaiduFullscreenVideoAd];
        }
    });
}

RCT_EXPORT_METHOD(loadRewardVideo:(NSString*)type
                  placementId:(NSString*)placementId
                  rewardName:(NSString*)rewardName
               rewardAmount:(NSInteger)rewardAmount)
{
    NSLog(@"loadRewardVideo type: %@, placementId: %@", type, placementId);
    dispatch_async(dispatch_get_main_queue(), ^{
        self.isRewardSuccess = false;
        self.isLoadAnShowReward = false;
      
        RNAdPoly *manager = [RNAdPoly sharedInstance];
        if ([type isEqual:@"gdt"])
        {
            [manager loadGDTRewardVideoAd:placementId
                               rewardName:rewardName
                             rewardAmount:rewardAmount];
        }
        else if ([type isEqual:@"tt"])
        {
            [manager loadBURewardVideoAd:placementId
                              rewardName:rewardName
                            rewardAmount:rewardAmount];
        }
        else if ([type isEqual:@"ks"])
        {
            [manager loadKSRewardVideoAd:placementId
                              rewardName:rewardName
                            rewardAmount:rewardAmount];
        }
        else if ([type isEqual:@"sigmob"])
        {
            [manager loadSigmobRewardVideoAd:placementId
                                  rewardName:rewardName
                                rewardAmount:rewardAmount];
        }
        else if ([type isEqual:@"baidu"])
        {
            [manager loadBaiduRewardVideoAd:placementId
                                 rewardName:rewardName
                               rewardAmount:rewardAmount];
        }
    });
}

RCT_EXPORT_METHOD(showRewardVideo:(NSString*)type
                  placementId:(NSString*)placementId
                  rewardName:(NSString*)rewardName
               rewardAmount:(NSInteger)rewardAmount)
{
    NSLog(@"showRewardVideo type: %@", type);
    dispatch_async(dispatch_get_main_queue(), ^{
        RNAdPoly *manager = [RNAdPoly sharedInstance];
        if ([type isEqual:@"gdt"])
        {
            [manager showGDTRewardVideoAd];
        }
        else if ([type isEqual:@"tt"])
        {
            [manager showBURewardVideoAd];
        }
        else if ([type isEqual:@"ks"])
        {
            [manager showKSRewardVideoAd];
        }
        else if ([type isEqual:@"sigmob"])
        {
            [manager showSigmobRewardVideoAd];
        }
        else if ([type isEqual:@"baidu"])
        {
            [manager showBaiduRewardVideoAd];
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

#pragma mark - GDTUnifiedInterstitialAdDelegate

/**
 *  插屏2.0广告预加载成功回调
 *  当接收服务器返回的广告数据成功后调用该函数
 */
- (void)unifiedInterstitialSuccessToLoadAd:(GDTUnifiedInterstitialAd *)unifiedInterstitial
{
    NSLog(@"%s",__FUNCTION__);
    NSLog(@"eCPM:%ld eCPMLevel:%@", [unifiedInterstitial eCPM], [unifiedInterstitial eCPMLevel]);
    NSLog(@"videoDuration:%lf isVideo: %@", unifiedInterstitial.videoDuration, @(unifiedInterstitial.isVideoAd));
    [self sendEventWithName:@"FullVideoAdDidSucceed" body:nil];
}

/**
 *  插屏2.0广告预加载失败回调
 *  当接收服务器返回的广告数据失败后调用该函数
 */
- (void)unifiedInterstitialFailToLoadAd:(GDTUnifiedInterstitialAd *)unifiedInterstitial error:(NSError *)error
{
    NSLog(@"%s ad load fail: %@",__FUNCTION__,error);
    [self sendEventWithName:@"FullVideoAdDidFailed" body:nil];
}

- (void)unifiedInterstitialDidDownloadVideo:(GDTUnifiedInterstitialAd *)unifiedInterstitial {
    NSLog(@"%s",__FUNCTION__);
}

- (void)unifiedInterstitialRenderSuccess:(GDTUnifiedInterstitialAd *)unifiedInterstitial {
    NSLog(@"%s",__FUNCTION__);
}

- (void)unifiedInterstitialRenderFail:(GDTUnifiedInterstitialAd *)unifiedInterstitial error:(NSError *)error {
    NSLog(@"%s",__FUNCTION__);
}

/**
 *  插屏2.0广告将要展示回调
 *  插屏2.0广告即将展示回调该函数
 */
- (void)unifiedInterstitialWillPresentScreen:(GDTUnifiedInterstitialAd *)unifiedInterstitial
{
    NSLog(@"%s",__FUNCTION__);
}

- (void)unifiedInterstitialFailToPresent:(GDTUnifiedInterstitialAd *)unifiedInterstitial error:(NSError *)error {
    NSLog(@"%s",__FUNCTION__);
}

/**
 *  插屏2.0广告视图展示成功回调
 *  插屏2.0广告展示成功回调该函数
 */
- (void)unifiedInterstitialDidPresentScreen:(GDTUnifiedInterstitialAd *)unifiedInterstitial
{
    NSLog(@"%s",__FUNCTION__);
}

/**
 *  插屏2.0广告展示结束回调
 *  插屏2.0广告展示结束回调该函数
 */
- (void)unifiedInterstitialDidDismissScreen:(GDTUnifiedInterstitialAd *)unifiedInterstitial
{
    NSLog(@"%s",__FUNCTION__);
}

/**
 *  当点击下载应用时会调用系统程序打开
 */
- (void)unifiedInterstitialWillLeaveApplication:(GDTUnifiedInterstitialAd *)unifiedInterstitial
{
    NSLog(@"%s",__FUNCTION__);
}

/**
 *  插屏2.0广告曝光回调
 */
- (void)unifiedInterstitialWillExposure:(GDTUnifiedInterstitialAd *)unifiedInterstitial
{
    NSLog(@"广告已曝光");
    NSLog(@"%s",__FUNCTION__);
}

/**
 *  插屏2.0广告点击回调
 */
- (void)unifiedInterstitialClicked:(GDTUnifiedInterstitialAd *)unifiedInterstitial
{
    NSLog(@"广告已点击");
    NSLog(@"%s",__FUNCTION__);
}

/**
 *  点击插屏2.0广告以后即将弹出全屏广告页
 */
- (void)unifiedInterstitialAdWillPresentFullScreenModal:(GDTUnifiedInterstitialAd *)unifiedInterstitial
{
    NSLog(@"%s",__FUNCTION__);
}

/**
 *  点击插屏2.0广告以后弹出全屏广告页
 */
- (void)unifiedInterstitialAdDidPresentFullScreenModal:(GDTUnifiedInterstitialAd *)unifiedInterstitial
{
    NSLog(@"%s",__FUNCTION__);
}

/**
 *  全屏广告页将要关闭
 */
- (void)unifiedInterstitialAdWillDismissFullScreenModal:(GDTUnifiedInterstitialAd *)unifiedInterstitial
{
    NSLog(@"%s",__FUNCTION__);
}

/**
 *  全屏广告页被关闭
 */
- (void)unifiedInterstitialAdDidDismissFullScreenModal:(GDTUnifiedInterstitialAd *)unifiedInterstitial
{
    NSLog(@"%s",__FUNCTION__);
    [self sendEventWithName:@"FullVideoAdDidClose" body:nil];
}


/**
 * 插屏2.0视频广告 player 播放状态更新回调
 */
- (void)unifiedInterstitialAd:(GDTUnifiedInterstitialAd *)unifiedInterstitial playerStatusChanged:(GDTMediaPlayerStatus)status
{
    NSLog(@"%s",__FUNCTION__);
}

/**
 *  投诉成功回调
 */
- (void)gdtAdComplainSuccess:(id)ad {
    NSLog(@"%s",__FUNCTION__);
    NSLog(@"广告投诉成功");
}

#pragma mark - GDTRewardVideoAdDelegate
- (void)gdt_rewardVideoAdDidLoad:(GDTRewardVideoAd *)rewardedVideoAd
{
    NSLog(@"%s",__FUNCTION__);
    NSLog(@"eCPM:%ld eCPMLevel:%@", [rewardedVideoAd eCPM], [rewardedVideoAd eCPMLevel]);
    NSLog(@"videoDuration :%lf rewardAdType:%ld", rewardedVideoAd.videoDuration, rewardedVideoAd.rewardAdType);
}


- (void)gdt_rewardVideoAdVideoDidLoad:(GDTRewardVideoAd *)rewardedVideoAd
{
    NSLog(@"%s",__FUNCTION__);
}


- (void)gdt_rewardVideoAdWillVisible:(GDTRewardVideoAd *)rewardedVideoAd
{
    NSLog(@"%s",__FUNCTION__);
    NSLog(@"视频播放页即将打开");
}

- (void)gdt_rewardVideoAdDidExposed:(GDTRewardVideoAd *)rewardedVideoAd
{
    NSLog(@"%s",__FUNCTION__);
    NSLog(@"广告已曝光");
}

- (void)gdt_rewardVideoAdDidClose:(GDTRewardVideoAd *)rewardedVideoAd
{
    NSLog(@"%s",__FUNCTION__);
    NSLog(@"广告已关闭");
    [self sendEventWithName:@"RewardDidClose" body:@{@"isEnded": @(self.isRewardSuccess)}];
}


- (void)gdt_rewardVideoAdDidClicked:(GDTRewardVideoAd *)rewardedVideoAd
{
    NSLog(@"%s",__FUNCTION__);
    NSLog(@"广告已点击");
}

- (void)gdt_rewardVideoAd:(GDTRewardVideoAd *)rewardedVideoAd didFailWithError:(NSError *)error
{
    NSLog(@"%s ad load fail: %@",__FUNCTION__,error);
    if (error.code == 4014) {
        NSLog(@"请拉取到广告后再调用展示接口");
    } else if (error.code == 4016) {
        NSLog(@"应用方向与广告位支持方向不一致");
    } else if (error.code == 5012) {
        NSLog(@"广告已过期");
    } else if (error.code == 4015) {
        NSLog(@"广告已经播放过，请重新拉取");
    } else if (error.code == 5002) {
        NSLog(@"视频下载失败");
    } else if (error.code == 5003) {
        NSLog(@"视频播放失败");
    } else if (error.code == 5004) {
        NSLog(@"没有合适的广告");
    } else if (error.code == 5013) {
        NSLog(@"请求太频繁，请稍后再试");
    } else if (error.code == 3002) {
        NSLog(@"网络连接超时");
    } else if (error.code == 5027){
        NSLog(@"页面加载失败");
    } else {
        NSLog(@"拉取广告失败");
    }
    NSLog(@"ERROR: %@", error);
}

- (void)gdt_rewardVideoAdDidRewardEffective:(GDTRewardVideoAd *)rewardedVideoAd info:(NSDictionary *)info {
    NSLog(@"%s",__FUNCTION__);
    NSLog(@"播放达到激励条件 transid:%@", [info objectForKey:@"GDT_TRANS_ID"]);
    self.isRewardSuccess = YES;
}

- (void)gdt_rewardVideoAdDidPlayFinish:(GDTRewardVideoAd *)rewardedVideoAd
{
    NSLog(@"%s",__FUNCTION__);
    NSLog(@"视频播放结束");
    
    // if (self.audioSessionSwitch.on) {
    //     [[AVAudioSession sharedInstance] setActive:NO withOptions:AVAudioSessionSetActiveOptionNotifyOthersOnDeactivation error:nil];
    // }
}

#pragma mark BUNative Splash delegate

- (void)splashAdLoadSuccess:(nonnull BUSplashAd *)splashAd {
    [self pbud_logWithSEL:_cmd msg:@""];
    // 使用应用keyWindow的rootViewController（接入简单，推荐）
    UIViewController *rootViewController = [self getKeyWindow].rootViewController;
    [splashAd showSplashViewInRootViewController:rootViewController];
    
}

- (void)splashAdLoadFail:(nonnull BUSplashAd *)splashAd error:(BUAdError * _Nullable)error {
    NSString *errorMsg = error ? [NSString stringWithFormat:@"code=%ld, message=%@", (long)error.code, error.localizedDescription] : @"error is nil";
    [self pbud_logWithSEL:_cmd msg:errorMsg];
    [self sendEventWithName:@"ShowSplashFailed" body:nil];
}

- (void)splashAdRenderSuccess:(nonnull BUSplashAd *)splashAd {
    [self pbud_logWithSEL:_cmd msg:@""];
    [splashAd.splashRootViewController.view addSubview:self.bottomView];
}

- (void)splashAdRenderFail:(nonnull BUSplashAd *)splashAd error:(BUAdError * _Nullable)error {
    [self pbud_logWithSEL:_cmd msg:@""];
}

- (void)splashAdWillShow:(nonnull BUSplashAd *)splashAd {
    [self pbud_logWithSEL:_cmd msg:@""];
}

- (void)splashAdDidShow:(nonnull BUSplashAd *)splashAd {
    [self pbud_logWithSEL:_cmd msg:@""];
}

- (void)splashAdDidClick:(nonnull BUSplashAd *)splashAd {
    [self pbud_logWithSEL:_cmd msg:@""];
}

- (void)splashAdDidClose:(nonnull BUSplashAd *)splashAd closeType:(BUSplashAdCloseType)closeType {
    [self pbud_logWithSEL:_cmd msg:@""];
    [self.bottomView removeFromSuperview];
}

- (void)splashAdViewControllerDidClose:(BUSplashAd *)splashAd {
    [self pbud_logWithSEL:_cmd msg:@""];
}

- (void)splashDidCloseOtherController:(nonnull BUSplashAd *)splashAd interactionType:(BUInteractionType)interactionType {
    [self pbud_logWithSEL:_cmd msg:@""];
}

- (void)splashVideoAdDidPlayFinish:(nonnull BUSplashAd *)splashAd didFailWithError:(nullable NSError *)error {
    [self pbud_logWithSEL:_cmd msg:@""];
}

#pragma mark - BUNativeExpressFullscreenVideoAdDelegate

- (void)nativeExpressFullscreenVideoAdDidLoad:(BUNativeExpressFullscreenVideoAd *)fullscreenVideoAd {
    [self pbud_logWithSEL:_cmd msg:@""];
    [self sendEventWithName:@"FullVideoAdDidSucceed" body:nil];
}

- (void)nativeExpressFullscreenVideoAd:(BUNativeExpressFullscreenVideoAd *)fullscreenVideoAd didFailWithError:(NSError *_Nullable)error {
    [self pbud_logWithSEL:_cmd msg:[NSString stringWithFormat:@"%@", error]];
    [self sendEventWithName:@"FullVideoAdDidFailed" body:nil];
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
    [self sendEventWithName:@"RewardDidClose" body:@{@"isEnded": @(self.isRewardSuccess)}];
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
    self.isRewardSuccess = YES;
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

#pragma mark - KSInterstitialAdDelegate

- (void)ksad_interstitialAdDidLoad:(KSInterstitialAd *)interstitialAd {
    NSLog(@"%s",__FUNCTION__);
    [self sendEventWithName:@"FullVideoAdDidSucceed" body:nil];
}

- (void)ksad_interstitialAdDidClick:(KSInterstitialAd *)interstitialAd {
    NSLog(@"%s",__FUNCTION__);
}

- (void)ksad_interstitialAdDidClickSkip:(KSInterstitialAd *)interstitialAd {
    NSLog(@"%s",__FUNCTION__);
}

- (void)ksad_interstitialAdWillClose:(KSInterstitialAd *)interstitialAd {
    NSLog(@"%s",__FUNCTION__);
}

- (void)ksad_interstitialAdDidClose:(KSInterstitialAd *)interstitialAd {
    NSLog(@"%s",__FUNCTION__);
    [self sendEventWithName:@"FullVideoAdDidClose" body:nil];
}

- (void)ksad_interstitialAdWillVisible:(KSInterstitialAd *)interstitialAd {
    NSLog(@"%s",__FUNCTION__);
}

- (void)ksad_interstitialAdDidVisible:(KSInterstitialAd *)interstitialAd {
    NSLog(@"%s",__FUNCTION__);
}

- (void)ksad_interstitialAdRenderSuccess:(KSInterstitialAd *)interstitialAd {
    NSLog(@"%s",__FUNCTION__);
}

- (void)ksad_interstitialAdRenderFail:(KSInterstitialAd *)interstitialAd error:(NSError *)error {
    NSLog(@"%s",__FUNCTION__);
    [self sendEventWithName:@"FullVideoAdDidFailed" body:nil];
}

- (void)ksad_interstitialAdDidCloseOtherController:(KSInterstitialAd *)interstitialAd interactionType:(KSAdInteractionType)interactionType {
    NSLog(@"%s",__FUNCTION__);
}

- (void)ksad_interstitialVideoAdStartPlay:(KSInterstitialAd *)interstitialAd {
    NSLog(@"%s",__FUNCTION__);
}

- (void)ksad_interstitialVideoAdDidPlayFinish:(KSInterstitialAd *)interstitialAd didFailWithError:(NSError *_Nullable)error {
    NSLog(@"%s",__FUNCTION__);
}


#pragma mark - KSRewardedVideoAdDelegate

- (void)rewardedVideoAdDidLoad:(KSRewardedVideoAd *)rewardedVideoAd {
    NSLog(@"%s",__FUNCTION__);
}

- (void)rewardedVideoAd:(KSRewardedVideoAd *)rewardedVideoAd didFailWithError:(NSError *_Nullable)error {
    NSLog(@"%s",__FUNCTION__);
}

- (void)rewardedVideoAdVideoDidLoad:(KSRewardedVideoAd *)rewardedVideoAd {
    NSLog(@"%s",__FUNCTION__);
}

- (void)rewardedVideoAdWillVisible:(KSRewardedVideoAd *)rewardedVideoAd {
    NSLog(@"%s",__FUNCTION__);
}

- (void)rewardedVideoAdDidVisible:(KSRewardedVideoAd *)rewardedVideoAd {
    NSLog(@"%s",__FUNCTION__);
}

- (void)rewardedVideoAdWillClose:(KSRewardedVideoAd *)rewardedVideoAd {
    NSLog(@"%s",__FUNCTION__);
}

- (void)rewardedVideoAdDidClose:(KSRewardedVideoAd *)rewardedVideoAd {
    NSLog(@"%s",__FUNCTION__);
    [self sendEventWithName:@"RewardDidClose" body:@{@"isEnded": @(self.isRewardSuccess)}];
}

- (void)rewardedVideoAdDidClick:(KSRewardedVideoAd *)rewardedVideoAd {
    NSLog(@"%s",__FUNCTION__);
}

- (void)rewardedVideoAdDidPlayFinish:(KSRewardedVideoAd *)rewardedVideoAd didFailWithError:(NSError *_Nullable)error {
    NSLog(@"%s",__FUNCTION__);
}

- (void)rewardedVideoAdDidClickSkip:(KSRewardedVideoAd *)rewardedVideoAd {
    NSLog(@"%s",__FUNCTION__);
}

- (void)rewardedVideoAdDidClickSkip:(KSRewardedVideoAd *)rewardedVideoAd currentTime:(NSTimeInterval)currentTime {
    NSLog(@"%s",__FUNCTION__);
}

- (void)rewardedVideoAdStartPlay:(KSRewardedVideoAd *)rewardedVideoAd {
    NSLog(@"%s",__FUNCTION__);
}

- (void)rewardedVideoAd:(KSRewardedVideoAd *)rewardedVideoAd hasReward:(BOOL)hasReward {
    NSLog(@"%s",__FUNCTION__);
    self.isRewardSuccess = hasReward;
}

- (void)rewardedVideoAd:(KSRewardedVideoAd *)rewardedVideoAd hasReward:(BOOL)hasReward taskType:(KSAdRewardTaskType)taskType currentTaskType:(KSAdRewardTaskType)currentTaskType {
    NSLog(@"%s",__FUNCTION__);
}

- (void)rewardedVideoAd:(KSRewardedVideoAd *)rewardedVideoAd extraRewardVerify:(KSAdExtraRewardType)extraRewardType {
    NSLog(@"%s",__FUNCTION__);
}

- (void)rewardedVideoAd:(KSRewardedVideoAd *)rewardedVideoAd hasReward:(BOOL)hasReward extraDict:(NSDictionary *)extraDict {
    NSLog(@"%s",__FUNCTION__);
}

#pragma mark - WindIntersititialAdDelegate

- (void)intersititialAdDidLoad:(WindNewIntersititialAd *)intersititialAd {
    NSLog(@"%@ -- %@", NSStringFromSelector(_cmd), intersititialAd.placementId);
    [self sendEventWithName:@"FullVideoAdDidSucceed" body:nil];
}

- (void)intersititialAdDidLoad:(WindNewIntersititialAd *)intersititialAd didFailWithError:(NSError *)error {
    NSLog(@"%@ -- %@", NSStringFromSelector(_cmd), intersititialAd.placementId);
    [self sendEventWithName:@"FullVideoAdDidFailed" body:nil];
}

- (void)intersititialAdWillVisible:(WindNewIntersititialAd *)intersititialAd {
    NSLog(@"%@ -- %@", NSStringFromSelector(_cmd), intersititialAd.placementId);
}

- (void)intersititialAdDidVisible:(WindNewIntersititialAd *)intersititialAd {
    NSLog(@"%@ -- %@", NSStringFromSelector(_cmd), intersititialAd.placementId);
}

- (void)intersititialAdDidClick:(WindNewIntersititialAd *)intersititialAd {
    NSLog(@"%@ -- %@", NSStringFromSelector(_cmd), intersititialAd.placementId);
}

- (void)intersititialAdDidClickSkip:(WindNewIntersititialAd *)intersititialAd {
    NSLog(@"%@ -- %@", NSStringFromSelector(_cmd), intersititialAd.placementId);
}

- (void)intersititialAdDidClose:(WindNewIntersititialAd *)intersititialAd {
    NSLog(@"%@ -- %@", NSStringFromSelector(_cmd), intersititialAd.placementId);
    [self sendEventWithName:@"FullVideoAdDidClose" body:nil];
}

- (void)intersititialAdServerResponse:(WindNewIntersititialAd *)intersititialAd isFillAd:(BOOL)isFillAd {
    NSLog(@"%@ -- %@", NSStringFromSelector(_cmd), intersititialAd.placementId);
}

#pragma mark - WindRewardVideoAdDelegate

- (void)rewardVideoAdDidLoad:(WindRewardVideoAd *)rewardVideoAd {
    NSLog(@"%@ -- %@", NSStringFromSelector(_cmd), rewardVideoAd.placementId);
}

- (void)rewardVideoAdDidLoad:(WindRewardVideoAd *)rewardVideoAd didFailWithError:(NSError *)error {
    NSLog(@"%@ -- %@", NSStringFromSelector(_cmd), rewardVideoAd.placementId);
}

- (void)rewardVideoAdWillVisible:(WindRewardVideoAd *)rewardVideoAd {
    NSLog(@"%@ -- %@", NSStringFromSelector(_cmd), rewardVideoAd.placementId);
}

- (void)rewardVideoAdDidVisible:(WindRewardVideoAd *)rewardVideoAd {
    NSLog(@"%@ -- %@", NSStringFromSelector(_cmd), rewardVideoAd.placementId);
}

- (void)rewardVideoAdDidClick:(WindRewardVideoAd *)rewardVideoAd {
    NSLog(@"%@ -- %@", NSStringFromSelector(_cmd), rewardVideoAd.placementId);
}

- (void)rewardVideoAdDidClickSkip:(WindRewardVideoAd *)rewardVideoAd {
    NSLog(@"%@ -- %@", NSStringFromSelector(_cmd), rewardVideoAd.placementId);
}

- (void)rewardVideoAd:(WindRewardVideoAd *)rewardVideoAd reward:(WindRewardInfo *)reward {
    NSLog(@"%@ -- %@", NSStringFromSelector(_cmd), rewardVideoAd.placementId);
    self.isRewardSuccess = YES;
}

- (void)rewardVideoAdDidClose:(WindRewardVideoAd *)rewardVideoAd {
    NSLog(@"%@ -- %@", NSStringFromSelector(_cmd), rewardVideoAd.placementId);
    [self sendEventWithName:@"RewardDidClose" body:@{@"isEnded": @(self.isRewardSuccess)}];
}

- (void)rewardVideoAdDidPlayFinish:(WindRewardVideoAd *)rewardVideoAd didFailWithError:(NSError *)error {
    NSLog(@"%@ -- %@", NSStringFromSelector(_cmd), rewardVideoAd.placementId);
}

/**
 This method is called when return ads from sigmob ad server.
 */
- (void)rewardVideoAdServerResponse:(WindRewardVideoAd *)rewardVideoAd isFillAd:(BOOL)isFillAd {
    NSLog(@"%@ -- %@", NSStringFromSelector(_cmd), rewardVideoAd.placementId);
}

#pragma mark - BaiduIntersititialAdDelegate

- (void)interstitialAdLoaded:(BaiduMobAdExpressInterstitial *)interstitial {
    NSLog(@"ExpressInterstitial loaded 请求成功");
    NSLog(@"插屏价格标签: %@", [interstitial getECPMLevel]);
    [self sendEventWithName:@"FullVideoAdDidSucceed" body:nil];
}

- (void)interstitialAdLoadFailed:(BaiduMobAdExpressInterstitial *)interstitial withError:(BaiduMobFailReason)reason {
    NSLog(@"ExpressInterstitial failed 请求失败");
    [self sendEventWithName:@"FullVideoAdDidFailed" body:nil];
}

- (void)interstitialAdDownloadSucceeded:(BaiduMobAdExpressInterstitial *)interstitial {
    NSLog(@"ExpressInterstitial downloadSucceeded 缓存成功");
}

- (void)interstitialAdDownLoadFailed:(BaiduMobAdExpressInterstitial *)interstitial {
    NSLog(@"ExpressInterstitial downloadFailed 缓存失败");
}

- (void)interstitialAdExposure:(BaiduMobAdExpressInterstitial *)interstitial {
    NSLog(@"ExpressInterstitial exposure 曝光成功");
}

- (void)interstitialAdExposureFail:(BaiduMobAdExpressInterstitial *)interstitial withError:(int)reason {
    NSLog(@"ExpressInterstitial exposure 展现失败");
}

- (void)interstitialAdDidClick:(BaiduMobAdExpressInterstitial *)interstitial {
    NSLog(@"ExpressInterstitial click 发生点击");
}

- (void)interstitialAdDidLPClose:(BaiduMobAdExpressInterstitial *)interstitial {
    NSLog(@"ExpressInterstitial lpClose 落地页关闭");
}

- (void)interstitialAdDidClose:(BaiduMobAdExpressInterstitial *)interstitial {
    NSLog(@"ExpressInterstitial close 点击关闭");
    [self sendEventWithName:@"FullVideoAdDidClose" body:nil];
}

#pragma mark - BaiduRewardVideoAdDelegate

- (void)rewardedVideoAdLoaded:(BaiduMobAdRewardVideo *)video {
    NSLog(@"激励视频缓存成功");
}

- (void)rewardedVideoAdLoadFailed:(BaiduMobAdRewardVideo *)video withError:(BaiduMobFailReason)reason {
    NSLog(@"激励视频缓存失败，failReason：%d", reason);
}

- (void)rewardedVideoAdShowFailed:(BaiduMobAdRewardVideo *)video withError:(BaiduMobFailReason)reason {
    NSLog(@"激励视频展现失败，failReason：%d", reason);
    //异常情况处理
}

- (void)rewardedVideoAdDidExposured:(BaiduMobAdRewardVideo *)video {
    NSLog(@"激励视频曝光成功");
}

- (void)rewardedVideoAdDidStarted:(BaiduMobAdRewardVideo *)video {
    NSLog(@"激励视频开始播放");
}

- (void)rewardedVideoAdDidPlayFinish:(BaiduMobAdRewardVideo *)video {
    
    NSLog(@"激励视频完成播放");
}

- (void)rewardedVideoAdDidClick:(BaiduMobAdRewardVideo *)video withPlayingProgress:(CGFloat)progress {
    NSLog(@"激励视频被点击，progress:%f", progress);
}

- (void)rewardedVideoAdDidClose:(BaiduMobAdRewardVideo *)video withPlayingProgress:(CGFloat)progress {
    NSLog(@"激励视频点击关闭,视图已销毁，progress:%f", progress);
    [self sendEventWithName:@"RewardDidClose" body:@{@"isEnded": @(self.isRewardSuccess)}];
}

- (void)rewardedVideoAdWillClose:(BaiduMobAdRewardVideo *)video withPlayingProgress:(CGFloat)progress {
    NSLog(@"激励视频点击关闭,视图即将销毁，progress:%f", progress);
}

- (void)rewardedVideoAdDidSkip:(BaiduMobAdRewardVideo *)video withPlayingProgress:(CGFloat)progress {
    NSLog(@"激励视频点击跳过, progress:%f", progress);
}

- (void)rewardedVideoAdRewardDidSuccess:(BaiduMobAdRewardVideo *)video verify:(BOOL)verify {
    NSLog(@"激励视频奖励成功");
    self.isRewardSuccess = verify;
}

- (void)rewardedAdLoadFailCode:(NSString *)errCode message:(NSString *)message rewardedAd:(BaiduMobAdRewardVideo *)video {
    NSLog(@"激励视频请求失败，errCode:%@ failReason：%@", errCode, message);
}

- (void)rewardedAdLoadSuccess:(BaiduMobAdRewardVideo *)video {
    NSLog(@"激励视频价格标签: %@", [video getECPMLevel]);
    NSLog(@"激励视频请求成功");
}

- (void)removeSplashAdView {
    if (self.buSplash) {
//        [self.buSplash removeFromSuperview];
        self.buSplash = nil;
    }
}

@end
