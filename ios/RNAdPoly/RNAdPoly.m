//
//  RNAdPoly.m
//  RNAdPoly
//
//  Created by dwwang on 2017/8/24.
//  Copyright © 2017年 dwwang. All rights reserved.
//

#import "RNAdPoly.h"
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
    self.buSplash.tolerateTimeout = 3;
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
        [GDTSDKConfig registerAppId:appKey];
        [GDTSDKConfig enableGPS:YES];
    }
    else if ([type isEqual:@"tt"])
    {
        [BUAdSDKManager setAppID:appKey];
#if DEBUG
        [BUAdSDKManager setLoglevel:BUAdSDKLogLevelDebug];
#endif
        [BUAdSDKManager setIsPaidApp:NO];
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
        
        if ([type isEqual:@"gdt"])
        {
            [manager showGdtSplash:placementId];
        }
        else if ([type isEqual:@"tt"])
        {
            [manager showBuSplash:placementId];
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
    [self removeSplash];
}

/**
 *  展示结束or展示失败后, 手动移除splash和delegate
 */
- (void) removeSplash
{
    if (self.gdtSplash)
    {
        self.gdtSplash.delegate = nil;
        self.gdtSplash = nil;
    }
}

#pragma mark delegate

- (void)splashAdDidClose:(BUSplashAdView *)splashAd
{
    [splashAd removeFromSuperview];
    NSLog(@"%s",__FUNCTION__);
}

- (void)splashAd:(BUSplashAdView *)splashAd didFailWithError:(NSError *)error
{
    [splashAd removeFromSuperview];
    NSLog(@"%s%@",__FUNCTION__,error);
    [self sendEventWithName:@"ShowSplashFailed" body:nil];
}

- (void)splashAdWillVisible:(BUSplashAdView *)splashAd
{
    NSLog(@"%s",__FUNCTION__);
}

@end
