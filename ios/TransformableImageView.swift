import UIKit
import AVKit

class TransformableImageView: UIImageView {
  private var rotationInDegrees: CGFloat = 0
  private var imageScale: CGVector = CGVector(dx: 1, dy: 1)

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

      self.imageScale *= CGVector(dx: -1, dy: 1)
    case .vertical:
      self.transform = self.transform
        .translatedBy(x: 0, y: translation.dy)
        .scaledBy(x: 1, y: -1)

      self.imageScale *= CGVector(dx: 1, dy: -1)
    }

    layoutIfNeeded()
  }

  func rotate90DegCcw(scale: CGFloat, rect: CGRect, toRect: CGRect) {
    /**
     * The idea here is to calculate the rotation using the center of the image
     * as the anchor point (because that's what CGAffineTransform does).
     * For that we calculate the vector to the center of the rect, we then apply the
     * transforms required to run the rotation (rotation + scaling) and then calculate
     * the vector from that calculated point back to the center of the rect.
     */
    let imageCenterToCropCenter = (rect.center - contentClippingRect.center).rotate(degrees: -rotationInDegrees)
    let rotatedVector = imageCenterToCropCenter.rotate(degrees: -90) * scale
    let newCenter = contentClippingRect.center + rotatedVector
    let translation = (rect.center - newCenter) / imageScale

    print("ROTATION")
    print("==================")
    print("scale", scale)
    print("imageScale", imageScale)
    print("rotation", rotationInDegrees)
    print("rect", rect)
    print("centers", rect.center, contentClippingRect.center)
    print("contentClippingRect", contentClippingRect)
    print("imageCenterToCropCenter", imageCenterToCropCenter)
    print("rotatedVector", rotatedVector)
    print("newCenter", newCenter)
    print("translation", translation)

    UIView.animate(withDuration: 0.5) {
      self.transform = self.transform
        .translatedBy(vector: translation)
        .rotatedBy(degrees: -90)
        .scaledBy(factor: scale)
    }

    imageScale /= scale
    rotationInDegrees -= 90
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

    self.imageScale = CGVector(dx: 1.0, dy: 1.0)
    rotationInDegrees = 0
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
        .translatedBy(vector: translation)
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
        .scaledBy(factor: scale)
        .translatedBy(vector: translation)
      self.layoutIfNeeded()
    }
  }

  var contentClippingRect: CGRect {
    guard let image = image else { return bounds }

    return AVMakeRect(aspectRatio: image.size.rotated90Degrees(times: Int(rotationInDegrees / 90)), insideRect: frame);
  }
}
