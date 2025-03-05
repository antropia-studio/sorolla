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

  func rotate(degrees: CGFloat) -> CGVector {
    let radians = degrees * .pi / 180.0

    let cosAngle = cos(radians)
    let sinAngle = sin(radians)

    return CGVector(
      dx: dx * cosAngle - dy * sinAngle,
      dy: dx * sinAngle + dy * cosAngle
    )
  }
}
