#import <React/RCTViewManager.h>
#import <React/RCTUIManager.h>
#import "RCTBridge.h"

@interface SorollaViewManager : RCTViewManager
@end

@implementation SorollaViewManager

RCT_EXPORT_MODULE(SorollaView)

- (UIView *)view
{
  return [[UIView alloc] init];
}

RCT_EXPORT_VIEW_PROPERTY(uri, NSString)

@end
