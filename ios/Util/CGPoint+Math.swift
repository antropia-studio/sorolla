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

  static func -(end: CGPoint, start: CGPoint) -> CGVector {
    return CGVector(dx: end.x - start.x, dy: end.y - start.y)
  }
}
