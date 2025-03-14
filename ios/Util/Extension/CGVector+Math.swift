import Foundation

extension CGVector {
  static var one: CGVector { return CGVector(dx: 1, dy: 1) }

  static func *(lhs: CGVector, factor: CGFloat) -> CGVector {
    return CGVector(dx: lhs.dx * factor, dy: lhs.dy * factor)
  }

  static func *(lhs: CGVector, rhs: CGVector) -> CGVector {
    return CGVector(dx: lhs.dx * rhs.dx, dy: lhs.dy * rhs.dy)
  }

  static func *=(lhs: inout CGVector, factor: CGFloat) {
    lhs = lhs * factor
  }

  static func *=(lhs: inout CGVector, rhs: CGVector) {
    lhs = lhs * rhs
  }

  static func /(lhs: CGVector, factor: CGFloat) -> CGVector {
    return CGVector(dx: lhs.dx / factor, dy: lhs.dy / factor)
  }

  static func /(lhs: CGVector, rhs: CGPoint) -> CGVector {
    return CGVector(dx: lhs.dx / rhs.x, dy: lhs.dy / rhs.y)
  }

  static func /(lhs: CGVector, rhs: CGVector) -> CGVector {
    return CGVector(dx: lhs.dx / rhs.dx, dy: lhs.dy / rhs.dy)
  }

  static func /=(lhs: inout CGVector, factor: CGFloat) {
    lhs = lhs / factor
  }

  static func +(lhs: CGVector, rhs: CGVector) -> CGVector {
    return CGVector(dx: lhs.dx + rhs.dx, dy: lhs.dy + rhs.dy)
  }

  static func +=(lhs: inout CGVector, rhs: CGVector) {
    lhs = lhs + rhs
  }

  static func -(lhs: CGVector, rhs: CGVector) -> CGVector {
    return CGVector(dx: lhs.dx - rhs.dx, dy: lhs.dy - rhs.dy)
  }

  /**
   * The sign property returns either +1 or -1 and it's useful to determine
   * if a rotation must account for mirroring effects.
   * It returns -1 if one of the vector components is negative and +1 if none or
   * both of them are negative.
   */
  var sign: CGFloat {
    if dx < 0 && dy > 0 { return -1 }
    if dx > 0 && dy < 0 { return -1 }
    return 1
  }

  func rotate(degrees: CGFloat) -> CGVector {
    let radians = degrees * .pi / 180.0

    let cosAngle = cos(radians)
    let sinAngle = sin(radians)

    return CGVector(
      dx: dx * cosAngle - dy * sinAngle,
      dy: dx * sinAngle + dy * cosAngle
    )
  }

  func projected(to axis: Axis) -> CGVector {
    switch (axis) {
    case .horizontal:
      return CGVector(dx: dx, dy: 0)
    case .vertical:
      return CGVector(dx: 0, dy: dy)
    }
  }

  static func mirrorVector(for axis: Axis) -> CGVector {
    switch (axis) {
    case .horizontal:
      return CGVector(dx: -1, dy: 1)
    case .vertical:
      return CGVector(dx: 1, dy: -1)
    }
  }
}
