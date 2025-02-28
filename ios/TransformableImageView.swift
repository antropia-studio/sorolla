import UIKit

class TransformableImageView: UIImageView {
  private var imageScale: CGFloat = 1.0

  init() {
    super.init(frame: CGRect.zero)
  }

  override init(frame: CGRect) {
    super.init(frame: frame)
  }

  required init?(coder aDecoder: NSCoder) {
    super.init(coder: aDecoder)
  }

  func reset() {
    self.transform = CGAffineTransform.identity
    self.imageScale = 1.0
  }

  func refit(
    scale: CGFloat,
    anchor: Anchor,
    fromRect: CGRect,
    toRect: CGRect
  ) {
    self.imageScale *= scale

    /**
     * We create a vector system using the center of the view as the origin of all transformations.
     * This is like this, because CGAffineTransforms scales images from the center of the view,
     * and so it's much easier to calculate everything around this fixed point.
     */
    let center = frame.center
    let fromVector = fromRect.getAnchorPoint(anchor) - center
    let toVector = toRect.getAnchorPoint(anchor) - center
    let referencePoint = fromVector * scale
    let translation = (toVector - referencePoint) / imageScale

    UIView.animate(withDuration: 0.5) {
      self.transform = self.transform
        .scaledBy(x: scale, y: scale)
        .translatedBy(x: translation.dx, y: translation.dy)
      self.layoutIfNeeded()
    }
  }
}
