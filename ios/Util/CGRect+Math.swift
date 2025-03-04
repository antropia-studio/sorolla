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

  /**
   * Interchanges vertex positions so that the rectangle changes its orientation (landscape <> portrait)
   */
  var swappedAxis: CGRect { return CGRect(x: minX, y: minY, width: height, height: width) }

  func fitting(in workingRect: CGRect) -> CGRect {
    let leadingAxis = getLeadingAxisToFit(in: workingRect)

    let center = workingRect.center

    let zoomedWidth = workingRect.height * aspectRatio
    let zoomedHeight = workingRect.width / aspectRatio

    let zoomedLeft = center.x - zoomedWidth / 2
    let zoomedTop = center.y - zoomedHeight / 2

    var toRect: CGRect
    switch (leadingAxis) {
    case .horizontal:
      toRect = CGRect(x: workingRect.minX, y: zoomedTop, width: workingRect.width, height: zoomedHeight)
    case .vertical:
      toRect = CGRect(x: zoomedLeft, y: workingRect.minY, width: zoomedWidth, height: workingRect.height)
    }

    return toRect
  }

  private func getLeadingAxisToFit(in rect: CGRect) -> Axis {
    let horizontalZoomRatio = width / rect.width

    if rect.height * horizontalZoomRatio < height {
      return .vertical
    } else {
      return .horizontal
    }
  }

  mutating func move(
    anchor: Anchor,
    translation: CGVector,
    minSize: CGSize = CGSize(width: 100, height: 100)
  ) {
    switch anchor {
    case .topLeft:
      moveLeft(dx: translation.dx, minWidth: minSize.width)
      moveTop(dy: translation.dy, minHeight: minSize.height)
    case .topRight:
      moveRight(dx: translation.dx, minWidth: minSize.width)
      moveTop(dy: translation.dy, minHeight: minSize.height)
    case .bottomLeft:
      moveLeft(dx: translation.dx, minWidth: minSize.width)
      moveBottom(dy: translation.dy, minHeight: minSize.height)
    case .bottomRight:
      moveRight(dx: translation.dx, minWidth: minSize.width)
      moveBottom(dy: translation.dy, minHeight: minSize.height)
    case .left:
      moveLeft(dx: translation.dx, minWidth: minSize.width)
    case .top:
      moveTop(dy: translation.dy, minHeight: minSize.height)
    case .right:
      moveRight(dx: translation.dx, minWidth: minSize.width)
    case .bottom:
      moveBottom(dy: translation.dy, minHeight: minSize.height)
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
