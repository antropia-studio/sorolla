import UIKit
import AVKit

class TransformableImageView: UIImageView {
  private var imageScale: CGPoint = CGPoint(x: 1.0, y: 1.0)

  convenience init() {
    self.init(frame: CGRect.zero)
  }

  override init(frame: CGRect) {
    super.init(frame: frame)

    self.contentMode = .scaleAspectFit
  }

  required init?(coder aDecoder: NSCoder) {
    super.init(coder: aDecoder)
  }

  func mirror(on axis: Axis, rect: CGRect) {
    /**
     * Because all transforms are applied based on the center of the image we need to correct the
     * translation to move the anchor to the center of the working/crop rect.
     * To do that we calculate the distance between the centers of the crop rect and the inner image.
     * We know we have to move the image double that (to virtually move the crop rect to the other side
     * of the reflection).
     */
    let imageCenterToCropCenter = rect.center - contentClippingRect.center
    let translation = (imageCenterToCropCenter * 2) / imageScale

    switch (axis) {
    case .horizontal:
      self.transform = self.transform
        .translatedBy(x: translation.dx, y: 0)
        .scaledBy(x: -1, y: 1)

      self.imageScale *= CGPoint(x: -1, y: 1)
    case .vertical:
      self.transform = self.transform
        .translatedBy(x: 0, y: translation.dy)
        .scaledBy(x: 1, y: -1)

      self.imageScale *= CGPoint(x: 1, y: -1)
    }

    layoutIfNeeded()
  }

  func rotate90DegCcw(scale: CGFloat, rect: CGRect) {
    UIView.animate(withDuration: 0.5) {
      self.transform = self.transform
        .translatedBy(x: 0, y: 0)
        .rotated(by: -CGFloat.pi / 2)
        .scaledBy(x: scale, y: scale)
    } completion: { finished in
    
    }

    imageScale *= scale
    layoutIfNeeded()
  }

  func reset(animated: Bool = false) {
    if (animated) {
      UIView.animate(withDuration: 0.5) {
        self.transform = CGAffineTransform.identity
      }
    } else {
      self.transform = CGAffineTransform.identity
    }

    self.imageScale = CGPoint(x: 1.0, y: 1.0)
  }

  func move(_ translation: CGVector) {
    let distance = translation / imageScale

    self.transform = self.transform.translatedBy(x: distance.dx, y: distance.dy)
    layoutIfNeeded()
  }

  func moveWithinBounds(_ bounds: CGRect) {
    var translation = CGVector.zero

    let imageFrame = contentClippingRect

    if imageFrame.minX > bounds.minX {
      translation.dx = bounds.minX - imageFrame.minX
    }

    if imageFrame.maxX < bounds.maxX {
      translation.dx = bounds.maxX - imageFrame.maxX
    }

    if imageFrame.minY > bounds.minY {
      translation.dy = bounds.minY - imageFrame.minY
    }

    if imageFrame.maxY < bounds.maxY {
      translation.dy = bounds.maxY - imageFrame.maxY
    }

    translation = translation / imageScale

    UIView.animate(withDuration: 0.4) {
      self.transform = self.transform
        .translatedBy(x: translation.dx, y: translation.dy)
      self.layoutIfNeeded()
    }
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

  var contentClippingRect: CGRect {
    guard let image = image else { return bounds }

    return AVMakeRect(aspectRatio: image.size, insideRect: frame);
  }
}
