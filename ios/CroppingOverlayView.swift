import SnapKit
import UIKit

let CORNER_STROKE_HALF_WIDTH = 2.0
let ANCHOR_LENGTH = 25.0
let MID_ANCHOR_LENGTH = ANCHOR_LENGTH / 2.0

@objc public class CroppingOverlayView: UIView {
  lazy var imageView = UIImageView()
  var imageRect: CGRect?

  override init(frame: CGRect) {
    super.init(frame: frame)
    
    backgroundColor = .clear
  }

  required init(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }
  
  func setImageRect(rect: CGRect) {
    self.imageRect = rect
    setNeedsDisplay()
  }
  
  override public func draw(_ rect: CGRect) {
    guard let imageRect = self.imageRect else { return }
    guard let context = UIGraphicsGetCurrentContext() else { return }
    
    self.drawVerticalLines(context: context, rect: imageRect)
    self.drawHorizontalLines(context: context, rect: imageRect)
    self.drawAnchors(context: context, rect: imageRect)
  }
  
  private func drawHorizontalLines(context: CGContext, rect: CGRect) {
    UIColor.white.setStroke()
    
    let slotHeight = rect.height / 3
    for i in 0...3 {
      let y = rect.minY + slotHeight * CGFloat(i)
      let line = UIBezierPath()
      line.move(to: CGPoint(x: rect.minX, y: y))
      line.addLine(to: CGPoint(x: rect.maxX, y: y))
      line.lineWidth = i == 0 || i == 3 ? 1 : 0.5
      line.stroke()
    }
  }
  
  private func drawVerticalLines(context: CGContext, rect: CGRect) {
    UIColor.white.setStroke()
    
    let slotWidth = rect.width / 3
    for i in 0...3 {
      let x = rect.minX + slotWidth * CGFloat(i)
      let line = UIBezierPath()
      line.move(to: CGPoint(x: x, y: rect.minY))
      line.addLine(to: CGPoint(x: x, y: rect.maxY))
      line.lineWidth = i == 0 || i == 3 ? 1 : 0.5
      line.stroke()
    }
  }
  
  private func drawAnchors(context: CGContext, rect: CGRect) {
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
}
