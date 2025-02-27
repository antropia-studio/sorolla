import Foundation

extension CGRect {
  init(rect: CGRect) {
    self.init(origin: rect.origin, size: rect.size)
  }

  var topLeft: CGPoint { return CGPoint(x: minX, y: minY) }
  var topRight: CGPoint { return CGPoint(x: maxX, y: minY) }
  var bottomLeft: CGPoint { return CGPoint(x: minX, y: maxY) }
  var bottomRight: CGPoint { return CGPoint(x: maxX, y: maxY) }
  var left: CGPoint { return CGPoint(x: minX, y: midY) }
  var top: CGPoint { return CGPoint(x: midX, y: minY) }
  var right: CGPoint { return CGPoint(x: maxX, y: midY) }
  var bottom: CGPoint { return CGPoint(x: midX, y: maxY) }
  var center: CGPoint { return CGPoint(x: midX, y: midY) }

  var aspectRatio: CGFloat { return width / height }

  func getAnchorPoint(_ anchor: Anchor) -> CGPoint {
    switch anchor {
    case .topLeft: return topLeft
    case .topRight: return topRight
    case .bottomLeft: return bottomLeft
    case .bottomRight: return bottomRight
    case .left: return left
    case .top: return top
    case .right: return right
    case .bottom: return bottom
    }
  }

  mutating func move(
    anchor: Anchor,
    translation: CGPoint,
    minSize: CGSize = CGSize(width: 100, height: 100)
  ) {
    switch anchor {
    case .topLeft:
      moveLeft(dx: translation.x, minWidth: minSize.width)
      moveTop(dy: translation.y, minHeight: minSize.height)
    case .topRight:
      moveRight(dx: translation.x, minWidth: minSize.width)
      moveTop(dy: translation.y, minHeight: minSize.height)
    case .bottomLeft:
      moveLeft(dx: translation.x, minWidth: minSize.width)
      moveBottom(dy: translation.y, minHeight: minSize.height)
    case .bottomRight:
      moveRight(dx: translation.x, minWidth: minSize.width)
      moveBottom(dy: translation.y, minHeight: minSize.height)
    case .left:
      moveLeft(dx: translation.x, minWidth: minSize.width)
    case .top:
      moveTop(dy: translation.y, minHeight: minSize.height)
    case .right:
      moveRight(dx: translation.x, minWidth: minSize.width)
    case .bottom:
      moveBottom(dy: translation.y, minHeight: minSize.height)
    }
  }

  private mutating func moveLeft(dx: CGFloat, minWidth: CGFloat) {
    guard size.width - dx > minWidth else { return }

    origin.x += dx
    size.width -= dx
  }

  private mutating func moveTop(dy: CGFloat, minHeight: CGFloat) {
    guard size.height - dy > minHeight else { return }

    origin.y += dy
    size.height -= dy
  }

  private mutating func moveRight(dx: CGFloat, minWidth: CGFloat) {
    guard size.width + dx > minWidth else { return }

    size.width += dx
  }

  private mutating func moveBottom(dy: CGFloat, minHeight: CGFloat) {
    guard size.height + dy > minHeight else { return }

    size.height += dy
  }

}
