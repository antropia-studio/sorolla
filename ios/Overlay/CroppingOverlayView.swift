import SnapKit
import UIKit

private let PAN_RADIUS = 40.0

struct Rotate90DegCcwResult {
  let scale: CGFloat
  let fromRect: CGRect
  let toRect: CGRect
}

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
  }

  required init?(coder aDecoder: NSCoder) {
    super.init(coder: aDecoder)
  }
  
  func setImageRect(rect: CGRect, update: Bool = true) {
    self.imageRect = CGRect(rect: rect)
    self.cropRect = CGRect(rect: rect)
    self.panCropRect = nil
    self.panAnchor = nil

    if (update) {
      setNeedsDisplay()
    }
  }

  func rotate90DegCcw() -> Rotate90DegCcwResult? {
    guard let cropRect = cropRect else { return nil }

    let targetRect = cropRect.swappedAxis.fitting(in: workingRect)
    let scale = targetRect.width / cropRect.height
    animateCropRect(from: cropRect, to: targetRect, duration: 0.5)

    return Rotate90DegCcwResult(
      scale: scale,
      fromRect: cropRect,
      toRect: targetRect
    )
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

    let toRect = fromRect.fitting(in: workingRect)

    animateCropRect(from: fromRect, to: toRect, duration: 0.5)
    panCropRect = nil
    self.panAnchor = nil

    return PanGestureEndResult(
      scale: min(toRect.width / fromRect.width, toRect.height / fromRect.height),
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

  private var workingRect: CGRect {
    return CGRect(
      x: frame.minX + self.padding,
      y: frame.minY + self.padding,
      width: frame.maxX - self.padding * 2,
      height: frame.maxY - self.padding * 2
    )
  }
}
