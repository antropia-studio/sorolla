import UIKit

class TransformableImageView: UIImageView {
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
  }

  func refit(
    scale: CGFloat,
    anchor: Anchor,
    fromRect: CGRect,
    toRect: CGRect
  ) {
    /**
     * We create a vector system using the center of the view the origin of all transformations.
     * This is like this, because CGAffineTransforms scales images from the center of the view,
     * and so it's much easier to calculate everything around this fixed point.
     */
    
    let center = fromRect.center
    let fromVector = fromRect.getAnchorPoint(anchor) - center
    let toVector = toRect.getAnchorPoint(anchor) - center
    let scaledImageAnchor = fromVector * scale
    let translation = toVector - scaledImageAnchor

    UIView.animate(withDuration: 1) {
      self.transform = self.transform
        .scaledBy(x: scale, y: scale)
        .translatedBy(x: translation.dx, y: translation.dy)
      self.layoutIfNeeded()
    }
  }
}
