import Foundation

extension CGPoint {
  var vector: CGVector { return CGVector(dx: x, dy: y) }

  func isInside(
    point: CGPoint,
    radius: CGFloat
  ) -> Bool {
    return
      abs(point.x - x) <= radius &&
      abs(point.y - y) <= radius
  }

  static func *(lhs: CGPoint, factor: CGFloat) -> CGPoint {
    return CGPoint(x: lhs.x * factor, y: lhs.y * factor)
  }

  static func *(lhs: CGPoint, rhs: CGPoint) -> CGPoint {
    return CGPoint(x: lhs.x * rhs.x, y: lhs.y * rhs.y)
  }

  static func *=(lhs: inout CGPoint, rhs: CGPoint) {
    lhs = lhs * rhs
  }

  static func *=(lhs: inout CGPoint, factor: CGFloat) {
    lhs = lhs * factor
  }

  static func -(end: CGPoint, start: CGPoint) -> CGVector {
    return CGVector(dx: end.x - start.x, dy: end.y - start.y)
  }

  static func /(lhs: CGPoint, rhs: CGVector) -> CGPoint {
    return CGPoint(x: lhs.x / rhs.dx, y: lhs.y / rhs.dy)
  }
}
