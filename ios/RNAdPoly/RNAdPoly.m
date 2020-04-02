//
//  RNAdPoly.m
//  RNAdPoly
//
//  Created by dwwang on 2017/8/24.
//  Copyright © 2017年 dwwang. All rights reserved.
//

#import "RNAdPoly.h"
#import "GDTSplashAd.h"
#import <Masonry/Masonry.h>
//#import <SDWebImage/UIImageView+WebCache.h>
//@import InMobiSDK;

static RNAdPoly *_instance = nil;

typedef NS_ENUM(NSInteger, AdSplashType)
{
    AdSplashType_GDT = 0,
    AdSplashType_BAIDU,
};

@interface RNAdPoly ()<GDTSplashAdDelegate/*, IMNativeDelegate*/>
@property (nonatomic, strong) GDTSplashAd *gdtSplash;
@property (nonatomic, strong) UIView *customSplashView;
@property (nonatomic, strong) UIView *bottomView;

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

- (void)showGdtSplash:(NSString*)appKey withPlacementId:(NSString*)placementId
{
    UIWindow *window = [UIApplication sharedApplication].keyWindow;
    
    //开屏广告初始化并展示代码
    self.gdtSplash = [[GDTSplashAd alloc] initWithAppId:appKey placementId:placementId];
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
    //[splashAd loadAdAndShowInWindow:self.window]; //设置开屏底部自定义LogoView，展示半屏开屏广告
    
    [self.gdtSplash loadAdAndShowInWindow:window withBottomView:self.bottomView];
}

#if 0
- (void)showInmobiSplash
{
    self.nativeAd = [[IMNative alloc] initWithPlacementId:1498527731848];
    self.nativeAd.delegate = self;
    [self.nativeAd load];
    
    UIWindow *window = [UIApplication sharedApplication].keyWindow;
    UIView *customSplashView = [[UIView alloc] initWithFrame:window.frame];
    customSplashView.backgroundColor = [UIColor whiteColor];
    [window addSubview:customSplashView];
    
    CGFloat screenWidth = window.frame.size.width;
    CGFloat screenHeight = window.frame.size.height;
    
    CGFloat bottomHeight = screenHeight / 6;
    NSURL *url = [NSURL URLWithString:@"http://i.l.inmobicdn.net/assets/e6448d567652405fbcfe7a25db184580.jpeg"];
    UIImageView *screenshotsView = [[UIImageView alloc] initWithFrame:CGRectMake(0, 0, screenWidth, screenHeight - bottomHeight)];
    [screenshotsView sd_setImageWithURL:url];
    //screenshotsView.image = [UIImage imageWithData:[NSData dataWithContentsOfURL:url]];
    [customSplashView addSubview:screenshotsView];
    
    self.skipButton = [[UIButton alloc] init];
    self.skipButton.titleLabel.font = [UIFont systemFontOfSize:14.0];
    [self.skipButton setTitle:@"跳过" forState:UIControlStateNormal];
    self.skipButton.layer.cornerRadius = 14;
    self.skipButton.layer.borderColor = [UIColor redColor].CGColor;
    self.skipButton.layer.borderWidth = 1.0f;
    self.skipButton.contentEdgeInsets = UIEdgeInsetsMake(6, 12, 6, 12);
    
    [customSplashView addSubview:self.skipButton];
    [self.skipButton mas_makeConstraints:^(MASConstraintMaker *make) {
        make.top.equalTo(screenshotsView.mas_top).with.offset(30);
        make.right.equalTo(screenshotsView.mas_right).with.offset(-10);
    }];
    
    
    
    UIView *bottomView = [[UIView alloc] initWithFrame:CGRectMake(0, screenHeight - bottomHeight, screenWidth, bottomHeight)];
    bottomView.backgroundColor = [UIColor whiteColor];
    CALayer *upperBorder = [CALayer layer];
    upperBorder.backgroundColor = [[UIColor colorWithRed:200.f/255.f
                                                   green:199.f/255.f
                                                    blue:204.f/255.f
                                                   alpha:1] CGColor];
    upperBorder.frame = CGRectMake(0, 0, screenWidth, 0.5f);
    [bottomView.layer addSublayer:upperBorder];
    
    UIImageView *imageView = [[UIImageView alloc] init];
    NSURL *url2 = [NSURL URLWithString:@"http://i.l.inmobicdn.net/assets/f5b9fec4b51b4160a80ca72bf283484b.png"];
    imageView.image = [UIImage imageWithData:[NSData dataWithContentsOfURL:url2]];
    [bottomView addSubview:imageView];
    
    UILabel *appNameLabel = [[UILabel alloc] init];
    appNameLabel.text = @"Sample TPCT";
    appNameLabel.font = [UIFont systemFontOfSize:22];
    appNameLabel.textAlignment = NSTextAlignmentLeft;
    [bottomView addSubview:appNameLabel];
    
    UILabel *sloganLabel = [[UILabel alloc] init];
    sloganLabel.text = @"This is a sample ad to demonstrate the use of conversion tracker";
    sloganLabel.font = [UIFont systemFontOfSize:14];
    sloganLabel.textColor = [UIColor colorWithRed:85.f/255.f
                                            green:87.f/255.f
                                             blue:85.f/255.f
                                            alpha:1];
    sloganLabel.textAlignment = NSTextAlignmentLeft;
    [bottomView addSubview:sloganLabel];
    
    [imageView mas_makeConstraints:^(MASConstraintMaker *make) {
        make.height.equalTo(@50);
        make.width.equalTo(@50);
        make.centerY.equalTo(bottomView.mas_centerY);
        make.centerX.equalTo(bottomView.mas_centerX).with.offset(-60);
    }];
    
    [appNameLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.top.equalTo(imageView.mas_top).with.offset(4);
        make.left.equalTo(imageView.mas_right).with.offset(10);
    }];
    
    [sloganLabel mas_makeConstraints:^(MASConstraintMaker *make) {
        make.top.equalTo(appNameLabel.mas_bottom).with.offset(-1);
        make.left.equalTo(appNameLabel.mas_left);
    }];
    
    [customSplashView addSubview:bottomView];
    
    [self startCountdown];
}

- (void)startCountdown
{
    __block NSInteger timeout = 5;
    
    dispatch_queue_t queue = dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0);
    dispatch_source_t _timer = dispatch_source_create(DISPATCH_SOURCE_TYPE_TIMER, 0, 0, queue);
    dispatch_source_set_timer(_timer, dispatch_walltime(NULL, 0), 1.0 * NSEC_PER_SEC, 0);
    dispatch_source_set_event_handler(_timer, ^{
        if(timeout <= 0 )
        {
            dispatch_source_cancel(_timer);
            dispatch_async(dispatch_get_main_queue(), ^{
                
            });
        }
        else
        {
            NSString *strTime = [NSString stringWithFormat:@"%.2ld | 跳过", (long)timeout];
            
            dispatch_async(dispatch_get_main_queue(), ^{
                [self.skipButton setTitle:strTime forState:UIControlStateNormal];
                self.skipButton.enabled = NO;
            });
            
            timeout--;
        }
    });
    
    dispatch_resume(_timer);
}
#endif

RCT_EXPORT_METHOD(showSplash:(NSString*)type
                  appKey:(NSString*)appKey
                  placementId:(NSString*)placementId)
{
    dispatch_async(dispatch_get_main_queue(), ^{
        RNAdPoly *manager = [RNAdPoly sharedInstance];
        
        if (!self.bottomView)
        {
            [manager drawBottomView];
        }
        
        if ([type isEqual:@"gdt"])
        {
            [manager showGdtSplash:appKey withPlacementId:placementId];
        }
        else if ([type isEqual:@"baidu"])
        {
            //[manager showBaiduSplash:appKey withPublisherId:placementId];
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

@end
