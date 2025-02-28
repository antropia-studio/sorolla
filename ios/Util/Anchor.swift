import Foundation

enum Anchor: CaseIterable {
  case topLeft, topRight, bottomLeft, bottomRight
  case left, top, right, bottom

  var opposite: Anchor {
    switch (self) {
    case .topLeft: return .bottomRight
    case .topRight: return .bottomLeft
    case .bottomLeft: return .topRight
    case .bottomRight: return .topLeft
    case .left: return .left
    case .top: return .bottom
    case .right: return .left
    case .bottom: return .top
    }
  }
}
