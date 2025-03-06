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

  func isWithin(_ rect: CGRect) -> Bool {
    if minX < rect.minX { return false }
    if minY < rect.minY { return false }
    if maxX > rect.maxX { return false }
    if maxY > rect.maxY { return false }

    return true
  }

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

  func moved(
    anchor: Anchor,
    translation: CGVector,
    minSize: CGSize = CGSize(width: 100, height: 100)
  ) -> CGRect {
    switch anchor {
    case .topLeft:
      return movedLeft(dx: translation.dx, minWidth: minSize.width)
        .movedTop(dy: translation.dy, minHeight: minSize.height)
    case .topRight:
      return movedRight(dx: translation.dx, minWidth: minSize.width)
        .movedTop(dy: translation.dy, minHeight: minSize.height)
    case .bottomLeft:
      return movedLeft(dx: translation.dx, minWidth: minSize.width)
        .movedBottom(dy: translation.dy, minHeight: minSize.height)
    case .bottomRight:
      return movedRight(dx: translation.dx, minWidth: minSize.width)
        .movedBottom(dy: translation.dy, minHeight: minSize.height)
    case .left:
      return movedLeft(dx: translation.dx, minWidth: minSize.width)
    case .top:
      return movedTop(dy: translation.dy, minHeight: minSize.height)
    case .right:
      return movedRight(dx: translation.dx, minWidth: minSize.width)
    case .bottom:
      return movedBottom(dy: translation.dy, minHeight: minSize.height)
    }
  }

  private func getLeadingAxisToFit(in rect: CGRect) -> Axis {
    let horizontalZoomRatio = width / rect.width

    if rect.height * horizontalZoomRatio < height {
      return .vertical
    } else {
      return .horizontal
    }
  }

  private func movedLeft(dx: CGFloat, minWidth: CGFloat) -> CGRect {
    guard size.width - dx > minWidth else { return self }

    return CGRect(x: minX + dx, y: minY, width: width - dx, height: height)
  }

  private func movedTop(dy: CGFloat, minHeight: CGFloat) -> CGRect {
    guard size.height - dy > minHeight else { return self }

    return CGRect(x: minX, y: minY + dy, width: width, height: height - dy)
  }

  private func movedRight(dx: CGFloat, minWidth: CGFloat) -> CGRect {
    guard size.width + dx > minWidth else { return self }

    return CGRect(x: minX, y: minY, width: width + dx, height: height)
  }

  private func movedBottom(dy: CGFloat, minHeight: CGFloat) -> CGRect {
    guard size.height + dy > minHeight else { return self }

    return CGRect(x: minX, y: minY, width: width, height: height + dy)
  }

}
