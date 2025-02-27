import Foundation

extension CGPoint {
  func isInside(
    point: CGPoint,
    radius: CGFloat
  ) -> Bool {
    return
      abs(point.x - x) <= radius &&
      abs(point.y - y) <= radius
  }
}
