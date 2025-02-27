import SnapKit
import UIKit

private let PAN_RADIUS = 40.0

struct PanGestureEndResult {
  let scale: CGFloat
  let anchor: Anchor
  let fromRect: CGRect
  let toRect: CGRect

  static var zero: PanGestureEndResult {
    return PanGestureEndResult(scale: 1.0, anchor: Anchor.top, fromRect: CGRect.zero, toRect: CGRect.zero)
  }
}

@objc public class CroppingOverlayView: UIView {
  private lazy var imageView = UIImageView()
  private let rectangleLayer = CAShapeLayer()
  private var imageRect: CGRect?
  var cropRect: CGRect?
  private var panCropRect: CGRect?
  private var panAnchor: Anchor?

  // animation
  var displayLink: CADisplayLink?
  var animationStartRect: CGRect?
  var animationTargetRect: CGRect?
  var animationProgress: CGFloat = 0
  var animationStep: CGFloat = 0.05

  override init(frame: CGRect) {
    super.init(frame: frame)
    
    backgroundColor = .clear
    layer.addSublayer(rectangleLayer)
  }

  required init(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  func setImageRect(rect: CGRect) {
    self.imageRect = CGRect(rect: rect)
    self.cropRect = CGRect(rect: rect)

    setNeedsDisplay()
  }
  
  override public func draw(_ rect: CGRect) {
    guard let cropRect = self.panCropRect ?? self.cropRect else { return }
    guard let context = UIGraphicsGetCurrentContext() else { return }
    
    self.drawVerticalLines(context: context, rect: cropRect)
    self.drawHorizontalLines(context: context, rect: cropRect)
    self.drawAnchors(context: context, rect: cropRect)
  }

  public override func removeFromSuperview() {
    super.removeFromSuperview()
    displayLink?.invalidate()
    displayLink = nil
  }

  func onPanGestureStart(on location: CGPoint) {
    guard let cropRect = cropRect else { return }

    panAnchor = Anchor.allCases.first { anchor in
      return cropRect
        .getAnchorPoint(anchor)
        .isInside(point: location, radius: PAN_RADIUS)
    }

    panCropRect = CGRect(rect: cropRect)
  }

  func onPanGestureMove(translation: CGPoint) {
    guard let panAnchor = panAnchor else { return }

    panCropRect?.move(anchor: panAnchor, translation: translation)

    setNeedsDisplay()
  }

  func onPanGestureEnded() -> PanGestureEndResult {
    guard let anchor = panAnchor else { return .zero }
    guard let originalRect = cropRect else { return .zero }
    guard let fromRect = panCropRect else { return .zero }
    var toRect = CGRect.zero

    let scale = min(originalRect.width / fromRect.width, originalRect.height / fromRect.height)

    let aspectRatio = fromRect.aspectRatio
    let leadingAxis = getLeadingAxisForZoom(rect: fromRect)

    let center = frame.center

    let zoomedWidth = frame.height * aspectRatio
    let zoomedHeight = frame.width / aspectRatio

    let zoomedLeft = center.x - zoomedWidth / 2
    let zoomedTop = center.y - zoomedHeight / 2

    switch (leadingAxis) {
    case .horizontal:
      toRect = CGRect(x: frame.minX + 10.0, y: zoomedTop, width: frame.width - 20.0, height: zoomedHeight)
    case .vertical:
      toRect = CGRect(x: zoomedLeft, y: frame.minY + 10.0, width: zoomedWidth, height: frame.height - 20.0)
    }

    animateCropRect(from: fromRect, to: toRect, duration: 0.3)
    cropRect = fromRect
    panCropRect = nil
    panAnchor = nil

    return PanGestureEndResult(
      scale: scale,
      anchor: anchor,
      fromRect: fromRect,
      toRect: toRect
    )
  }

  private func getLeadingAxisForZoom(rect: CGRect) -> Axis {
    let horizontalZoomRatio = rect.width / frame.width

    if frame.height * horizontalZoomRatio < rect.height {
      return .vertical
    } else {
      return .horizontal
    }
  }
}
