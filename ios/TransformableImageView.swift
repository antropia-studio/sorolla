import UIKit
import AVKit


/**
 * This component allows consumers to apply transformations to the contained image.
 * Internally it uses CGAffineTransforms which is CoreGraphics implementation of popular
 * affine matrices.
 * In Core Graphics, all affine transforms are applied using the center of the image
 * as their anchor/pivot. That's why all the transforms inside this component take this
 * into account and all they do is to calculate vectors and distances to the center of the
 * image.
 */
class TransformableImageView: UIImageView {
  private var rotationInDegrees: CGFloat = 0
  private var imageScale: CGVector = .one

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
    let normalizedAxis = axis.rotated90Degrees(times: Int(rotationInDegrees / 90))
    let toRectCenterVector = rect.center - contentClippingRect.center
    let translationInAxis = toRectCenterVector
      .rotate(degrees: -self.rotationInDegrees)
      .projected(to: normalizedAxis) * 2
    let translation = translationInAxis / imageScale
    let scale = CGVector.mirrorVector(for: normalizedAxis)

    self.transform = self.transform
      .translatedBy(vector: translation)
      .scaledBy(vector: scale)

    self.imageScale *= scale

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

    // This sign operation accounts for mirroring operations and corrects the rotation value
    let rotation = -90 * imageScale.sign
    let toRectCenterVector = rect.center - contentClippingRect.center
    let newCenterVector = toRectCenterVector.rotate(degrees: -90) * scale
    let newCenter = contentClippingRect.center + newCenterVector
    let translation = (rect.center - newCenter) / imageScale

    UIView.animate(withDuration: 0.5) {
      self.transform = self.transform
        .translatedBy(vector: translation.rotate(degrees: -self.rotationInDegrees))
        .rotatedBy(degrees: rotation)
        .scaledBy(factor: scale)
    }

    imageScale *= scale
    rotationInDegrees += rotation
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

    self.transform = self.transform
      .translatedBy(vector: distance.rotate(degrees: -rotationInDegrees))

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
        .translatedBy(vector: translation.rotate(degrees: -self.rotationInDegrees))

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
