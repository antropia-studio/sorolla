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
  private var padding: CGFloat = 0.0

  // animation
  var displayLink: CADisplayLink?
  var animationStartRect: CGRect?
  var animationTargetRect: CGRect?
  var animationProgress: CGFloat = 0
  var animationStart: CFTimeInterval?
  var animationDuration: TimeInterval?

  convenience init(padding: CGFloat) {
    self.init(frame: .zero)
    self.padding = padding
  }

  override init(frame: CGRect) {
    super.init(frame: frame)

    backgroundColor = .clear
    layer.addSublayer(rectangleLayer)
  }

  required init?(coder aDecoder: NSCoder) {
    super.init(coder: aDecoder)
  }
  
  func setImageRect(rect: CGRect) {
    self.imageRect = CGRect(rect: rect)
    self.cropRect = CGRect(rect: rect)

    setNeedsDisplay()
  }
  
  override public func draw(_ rect: CGRect) {
    guard let cropRect = self.panCropRect ?? self.cropRect else { return }
    guard let context = UIGraphicsGetCurrentContext() else { return }

    self.drawBackground(context: context, rect: cropRect)
    self.drawVerticalLines(context: context, rect: cropRect)
    self.drawHorizontalLines(context: context, rect: cropRect)
    self.drawAnchors(context: context, rect: cropRect)
  }

  public override func removeFromSuperview() {
    super.removeFromSuperview()
    displayLink?.invalidate()
    displayLink = nil
  }

  func onPanGestureStart(on location: CGPoint) -> PanAction? {
    guard let cropRect = cropRect else { return nil }

    panAnchor = Anchor.allCases.first { anchor in
      return cropRect
        .getAnchorPoint(anchor)
        .isInside(point: location, radius: PAN_RADIUS)
    }

    panCropRect = CGRect(rect: cropRect)

    return panAnchor != nil ? .crop : .move
  }

  func onPanGestureMove(translation: CGVector) {
    guard let panAnchor = panAnchor else { return }

    panCropRect?.move(anchor: panAnchor, translation: translation)

    setNeedsDisplay()
  }

  func onPanGestureEnded() -> PanGestureEndResult {
    guard let panAnchor = panAnchor else { return .zero }
    guard let fromRect = panCropRect else { return .zero }

    let workingRect = CGRect(
      x: frame.minX + self.padding,
      y: frame.minY + self.padding,
      width: frame.maxX - self.padding * 2,
      height: frame.maxY - self.padding * 2
    )
    let aspectRatio = fromRect.aspectRatio
    let leadingAxis = getLeadingAxisForZoom(rect: fromRect)

    let center = frame.center

    let zoomedWidth = workingRect.height * aspectRatio
    let zoomedHeight = workingRect.width / aspectRatio

    let zoomedLeft = center.x - zoomedWidth / 2
    let zoomedTop = center.y - zoomedHeight / 2

    var toRect: CGRect
    switch (leadingAxis) {
    case .horizontal:
      toRect = CGRect(x: workingRect.minX, y: zoomedTop, width: workingRect.width, height: zoomedHeight)
    case .vertical:
      toRect = CGRect(x: zoomedLeft, y: workingRect.minY, width: zoomedWidth, height: workingRect.height)
    }

    let scale = min(toRect.width / fromRect.width, toRect.height / fromRect.height)

    animateCropRect(from: fromRect, to: toRect, duration: 0.5)
    panCropRect = nil
    self.panAnchor = nil

    return PanGestureEndResult(
      scale: scale,
      anchor: panAnchor.opposite,
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
