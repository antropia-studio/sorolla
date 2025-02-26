import SnapKit
import UIKit

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
    
  }
}
