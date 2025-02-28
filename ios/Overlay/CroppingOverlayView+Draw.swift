import SnapKit
import UIKit

private let CORNER_STROKE_HALF_WIDTH = 2.0
private let ANCHOR_LENGTH = 25.0
private let MID_ANCHOR_LENGTH = ANCHOR_LENGTH / 2.0

@objc extension CroppingOverlayView {
  func drawHorizontalLines(context: CGContext, rect: CGRect) {
    UIColor.white.setStroke()

    let lines = UIBezierPath()

    let slotHeight = rect.height / 3
    for i in 0...3 {
      let y = rect.minY + slotHeight * CGFloat(i)
      lines.move(to: CGPoint(x: rect.minX, y: y))
      lines.addLine(to: CGPoint(x: rect.maxX, y: y))
      lines.lineWidth = i == 0 || i == 3 ? 1 : 0.5
    }

    lines.stroke()
  }

  func drawVerticalLines(context: CGContext, rect: CGRect) {
    UIColor.white.setStroke()

    let lines = UIBezierPath()

    let slotWidth = rect.width / 3
    for i in 0...3 {
      let x = rect.minX + slotWidth * CGFloat(i)
      lines.move(to: CGPoint(x: x, y: rect.minY))
      lines.addLine(to: CGPoint(x: x, y: rect.maxY))
      lines.lineWidth = i == 0 || i == 3 ? 1 : 0.5
    }

    lines.stroke()
  }

  func drawAnchors(context: CGContext, rect: CGRect) {
    UIColor.white.setStroke()

    let left = rect.minX - CORNER_STROKE_HALF_WIDTH
    let top = rect.minY - CORNER_STROKE_HALF_WIDTH
    let right = rect.maxX + CORNER_STROKE_HALF_WIDTH
    let bottom = rect.maxY + CORNER_STROKE_HALF_WIDTH

    let cornersPath = UIBezierPath()

    // Top-left corner: ⌜
    cornersPath.move(to: CGPoint(x: left, y: top + ANCHOR_LENGTH))
    cornersPath.addLine(to: CGPoint(x: left, y: top))
    cornersPath.addLine(to: CGPoint(x: left + ANCHOR_LENGTH, y: top))

    // Top-right corner: ⌝
    cornersPath.move(to: CGPoint(x: right, y: top + ANCHOR_LENGTH))
    cornersPath.addLine(to: CGPoint(x: right, y: top))
    cornersPath.addLine(to: CGPoint(x: right - ANCHOR_LENGTH, y: top))

    // Bottom-left corner: ⌞
    cornersPath.move(to: CGPoint(x: left, y: bottom - ANCHOR_LENGTH))
    cornersPath.addLine(to: CGPoint(x: left, y: bottom))
    cornersPath.addLine(to: CGPoint(x: left + ANCHOR_LENGTH, y: bottom))

    // Bottom-right corner: ⌟
    cornersPath.move(to: CGPoint(x: right, y: bottom - ANCHOR_LENGTH))
    cornersPath.addLine(to: CGPoint(x: right, y: bottom))
    cornersPath.addLine(to: CGPoint(x: right - ANCHOR_LENGTH, y: bottom))

    cornersPath.lineWidth = 4
    cornersPath.stroke()

    let edgesPath = UIBezierPath()

    // Left edge
    edgesPath.move(to: CGPoint(x: left, y: rect.midY - MID_ANCHOR_LENGTH))
    edgesPath.addLine(to: CGPoint(x: left, y: rect.midY + MID_ANCHOR_LENGTH))

    // Top edge
    edgesPath.move(to: CGPoint(x: rect.midX - MID_ANCHOR_LENGTH, y: top))
    edgesPath.addLine(to: CGPoint(x: rect.midX + MID_ANCHOR_LENGTH, y: top))

    // Right edge
    edgesPath.move(to: CGPoint(x: right, y: rect.midY - MID_ANCHOR_LENGTH))
    edgesPath.addLine(to: CGPoint(x: right, y: rect.midY + MID_ANCHOR_LENGTH))

    // Bottom edge
    edgesPath.move(to: CGPoint(x: rect.midX - MID_ANCHOR_LENGTH, y: bottom))
    edgesPath.addLine(to: CGPoint(x: rect.midX + MID_ANCHOR_LENGTH, y: bottom))

    edgesPath.lineWidth = 4
    edgesPath.stroke()
  }

  func drawBackground(context: CGContext, rect: CGRect) {
    UIColor.init(red: 0, green: 0, blue: 0, alpha: 0.5).setFill()

    context.addRect(CGRect(x: 0, y: 0, width: bounds.width, height: rect.minY))
    context.addRect(CGRect(x: 0, y: rect.minY, width: rect.minX, height: rect.height))
    context.addRect(CGRect(x: rect.maxX, y: rect.minY, width: bounds.maxX - rect.maxX, height: rect.height))
    context.addRect(CGRect(x: 0, y: rect.maxY, width: bounds.width, height: bounds.maxY - rect.maxY))

    context.fillPath()
  }
}
