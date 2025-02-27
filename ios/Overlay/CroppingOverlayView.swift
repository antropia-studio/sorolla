import SnapKit
import UIKit

private let PAN_RADIUS = 40.0

@objc public class CroppingOverlayView: UIView {
  private lazy var imageView = UIImageView()
  private var imageRect: CGRect?
  private var cropRect: CGRect?
  private var panAnchor: Anchor?

  override init(frame: CGRect) {
    super.init(frame: frame)
    
    backgroundColor = .clear
  }

  required init(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  func setImageRect(rect: CGRect) {
    self.imageRect = CGRect.init(origin: rect.origin, size: rect.size)
    self.cropRect = CGRect.init(origin: rect.origin, size: rect.size)

    setNeedsDisplay()
  }
  
  override public func draw(_ rect: CGRect) {
    guard let cropRect = self.cropRect else { return }
    guard let context = UIGraphicsGetCurrentContext() else { return }
    
    self.drawVerticalLines(context: context, rect: cropRect)
    self.drawHorizontalLines(context: context, rect: cropRect)
    self.drawAnchors(context: context, rect: cropRect)
  }

  func onPanGestureStart(on location: CGPoint) {
    guard let cropRect = cropRect else { return }

    panAnchor = Anchor.allCases.first { anchor in
      return isInTouchArea(
        point: location,
        anchorPosition: cropRect.getAnchorPoint(anchor),
        touchRadius: PAN_RADIUS
      )
    } ?? Anchor.center
  }

  func onPanGestureEnded() {
    panAnchor = nil
  }

  func onPanGestureMove(translation: CGPoint) {
    guard let panAnchor = panAnchor else { return }

    cropRect?.move(anchor: panAnchor, translation: translation)
    setNeedsDisplay()
  }

  private func isInTouchArea(
    point: CGPoint,
    anchorPosition: CGPoint,
    touchRadius: CGFloat
  ) -> Bool {
    return
      abs(point.x - anchorPosition.x) <= touchRadius &&
      abs(point.y - anchorPosition.y) <= touchRadius
  }
}
