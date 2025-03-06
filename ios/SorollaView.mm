#import "SorollaView.h"

#import "generated/RNSorollaViewSpec/ComponentDescriptors.h"
#import "generated/RNSorollaViewSpec/EventEmitters.h"
#import "generated/RNSorollaViewSpec/Props.h"
#import "generated/RNSorollaViewSpec/RCTComponentViewHelpers.h"

#if __has_include("Sorolla/Sorolla-Swift.h")
#import "Sorolla/Sorolla-Swift.h"
#else
#import "Sorolla-Swift.h"
#endif

#import "RCTFabricComponentsPlugins.h"

using namespace facebook::react;

@interface SorollaView () <RCTSorollaViewViewProtocol>

@end

@implementation SorollaView {
  SwSorollaView* _view;
}

+ (ComponentDescriptorProvider)componentDescriptorProvider
{
    return concreteComponentDescriptorProvider<SorollaViewComponentDescriptor>();
}

- (instancetype)initWithFrame:(CGRect)frame
{
  if (self = [super initWithFrame:frame]) {
    static const auto defaultProps = std::make_shared<const SorollaViewProps>();
    _props = defaultProps;

    _view = [[SwSorollaView alloc] init];

    self.contentView = _view;
  }

  return self;
}

- (void)updateProps:(Props::Shared const &)props oldProps:(Props::Shared const &)oldProps
{
  const auto &oldViewProps = *std::static_pointer_cast<SorollaViewProps const>(_props);
  const auto &newViewProps = *std::static_pointer_cast<SorollaViewProps const>(props);

  if (oldViewProps.uri != newViewProps.uri) {
    NSString *uri = [[NSString alloc] initWithUTF8String: newViewProps.uri.c_str()];
    [_view setUri:uri];
  }

  if (oldViewProps.mode != newViewProps.mode) {
    NSString *mode = [[NSString alloc] initWithUTF8String: facebook::react::toString(newViewProps.mode).c_str()];
    [_view setMode:mode];
  }

  if (oldViewProps.backgroundColor != newViewProps.backgroundColor) {
    NSString *backgroundColor = [[NSString alloc] initWithUTF8String: newViewProps.backgroundColor.c_str()];
    [_view setBackgroundAndOverlayColor:[self hexStringToColor:backgroundColor]];
  }

  [super updateProps:props oldProps:oldProps];
}

- (void)handleCommand:(const NSString *)commandName args:(const NSArray *)args
{
  RCTSorollaViewHandleCommand(self, commandName, args); //
}

- (void)mirrorVertically {
  [_view mirrorVertically];
}

- (void)mirrorHorizontally {
  [_view mirrorHorizontally];
}

- (void)rotateCcw {
  [_view rotateCcw];
}


- (void)cancelTransform {
  [_view resetCurrentTransform];
}

Class<RCTComponentViewProtocol> SorollaViewCls(void)
{
    return SorollaView.class;
}

- hexStringToColor:(NSString *)stringToConvert
{
    NSString *noHashString = [stringToConvert stringByReplacingOccurrencesOfString:@"#" withString:@""];
    NSScanner *stringScanner = [NSScanner scannerWithString:noHashString];

    unsigned hex;
    if (![stringScanner scanHexInt:&hex]) return nil;
    int r = (hex >> 16) & 0xFF;
    int g = (hex >> 8) & 0xFF;
    int b = (hex) & 0xFF;

    return [UIColor colorWithRed:r / 255.0f green:g / 255.0f blue:b / 255.0f alpha:1.0f];
}

@end
