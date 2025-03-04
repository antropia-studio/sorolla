import Foundation

extension CGAffineTransform {
  func translatedBy(vector: CGVector) -> CGAffineTransform {
    return translatedBy(x: vector.dx, y: vector.dy)
  }

  func rotatedBy(degrees: CGFloat) -> CGAffineTransform {
    let radians = degrees * .pi / 180.0

    return rotated(by: radians)
  }

  func scaledBy(factor: CGFloat) -> CGAffineTransform {
    return scaledBy(x: factor, y: factor)
  }

  func scaledBy(vector: CGVector) -> CGAffineTransform {
    return scaledBy(x: vector.dx, y: vector.dy)
  }
}
