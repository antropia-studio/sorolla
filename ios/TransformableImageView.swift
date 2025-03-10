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
 * We keep the transforms applied in the form of translations, rotations and scales because
 * it's way simpler to operate on the identity matrix over and over again than compounding
 * matrices.
 */
class TransformableImageView: UIImageView {
  private var originalCIImage: CIImage?
  private var translation: CGVector = .zero
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
    let toRectCenterVector = rect.center - contentClippingRect.center
    let translationInAxis = toRectCenterVector.projected(to: axis) * 2

    self.translation += translationInAxis
    self.imageScale *= CGVector.mirrorVector(for: axis)

    applyTransform(animated: false)
  }

  func rotate90DegCcw(scale: CGFloat, rect: CGRect, toRect: CGRect) {
    let rotation: CGFloat = -90
    let toRectCenterVector = rect.center - contentClippingRect.center
    let newCenterVector = toRectCenterVector.rotate(degrees: rotation) * scale
    let newCenter = contentClippingRect.center + newCenterVector

    self.translation += rect.center - newCenter
    self.imageScale *= scale
    self.rotationInDegrees += rotation * imageScale.sign

    applyTransform(animated: true)
  }

  func setSettings(brightness: Float, saturation: Float, contrast: Float) {
    guard let image = image else { return }

    if (originalCIImage == nil) {
      originalCIImage = CIImage(
        image: image,
        options: [
          .applyOrientationProperty: true,
            .properties: [kCGImagePropertyOrientation: CGImagePropertyOrientation(image.imageOrientation).rawValue]
        ]
      )
    }

    guard let ciImage = originalCIImage else { return }

    let filter = CIFilter(name: "CIColorControls")!
    print(brightness, saturation, contrast)
    filter.setValue(ciImage, forKey: kCIInputImageKey)
    filter.setValue(brightness, forKey: kCIInputBrightnessKey)
    filter.setValue(2 * contrast.normalize(min: -1, max: 1), forKey: kCIInputContrastKey)
    filter.setValue(2 * saturation.normalize(min: -1, max: 1), forKey: kCIInputSaturationKey)
    guard let outputImage = filter.outputImage else { return }

    self.image = UIImage(ciImage: outputImage)
  }

  func reset(animated: Bool = false) {
    self.originalCIImage = nil
    self.translation = .zero
    self.imageScale = CGVector(dx: 1.0, dy: 1.0)
    self.rotationInDegrees = 0

    applyTransform(animated: animated)
  }

  func move(_ translation: CGVector) {
    self.translation += translation

    applyTransform(animated: false)
  }

  func moveWithinBounds(_ bounds: CGRect) {
    var translation = CGVector.zero

    if contentClippingRect.minX > bounds.minX {
      translation.dx = bounds.minX - contentClippingRect.minX
    }

    if contentClippingRect.maxX < bounds.maxX {
      translation.dx = bounds.maxX - contentClippingRect.maxX
    }

    if contentClippingRect.minY > bounds.minY {
      translation.dy = bounds.minY - contentClippingRect.minY
    }

    if contentClippingRect.maxY < bounds.maxY {
      translation.dy = bounds.maxY - contentClippingRect.maxY
    }

    self.translation += translation

    applyTransform(animated: true)
  }

  func refit(
    scale: CGFloat,
    anchor: Anchor,
    fromRect: CGRect,
    toRect: CGRect
  ) {
    let fromVector = fromRect.center - contentClippingRect.center
    let toVector = toRect.center - contentClippingRect.center

    self.translation += toVector - (fromVector * scale)
    self.imageScale *= scale

    applyTransform(animated: true)
  }

  var contentClippingRect: CGRect {
    guard let image = image else { return bounds }

    return AVMakeRect(aspectRatio: image.size.rotated90Degrees(times: Int(rotationInDegrees / 90)), insideRect: frame);
  }

  private func applyTransform(animated: Bool) {
    UIView.animate(withDuration: animated ? 0.5 : 0) {
      // Reminder: Operations are applied from bottom to top
      self.transform = .identity
        .translatedBy(vector: self.translation)
        .scaledBy(vector: self.imageScale)
        .rotatedBy(degrees: self.rotationInDegrees)

      self.setNeedsLayout()
    }
  }
}
