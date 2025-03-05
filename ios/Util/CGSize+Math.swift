import Foundation

extension CGSize {
  func rotated90Degrees(times: Int) -> CGSize {
    return CGSize(width: times % 2 == 0 ? width : height, height: times % 2 == 0 ? height : width)
  }
}
