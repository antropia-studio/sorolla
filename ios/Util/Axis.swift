import Foundation

enum Axis {
  case horizontal
  case vertical;

  var inverted: Axis {
    switch self {
    case .horizontal:
      return .vertical
    case .vertical:
      return .horizontal
    }
  }

  func rotated90Degrees(times: Int) -> Axis {
    if times % 2 == 0 {
      return self
    } else {
      return inverted
    }
  }
}
