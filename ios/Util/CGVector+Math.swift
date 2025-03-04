import Foundation

extension CGVector {
  static func *(lhs: CGVector, factor: CGFloat) -> CGVector {
    return CGVector(dx: lhs.dx * factor, dy: lhs.dy * factor)
  }

  static func /(lhs: CGVector, factor: CGFloat) -> CGVector {
    return CGVector(dx: lhs.dx / factor, dy: lhs.dy / factor)
  }

  static func /(lhs: CGVector, rhs: CGPoint) -> CGVector {
    return CGVector(dx: lhs.dx / rhs.x, dy: lhs.dy / rhs.y)
  }

  static func +(lhs: CGVector, rhs: CGVector) -> CGVector {
    return CGVector(dx: lhs.dx + rhs.dx, dy: lhs.dy + rhs.dy)
  }

  static func -(lhs: CGVector, rhs: CGVector) -> CGVector {
    return CGVector(dx: lhs.dx - rhs.dx, dy: lhs.dy - rhs.dy)
  }
}
