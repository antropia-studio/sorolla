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
    self.drawAnchors(context: context, rect: imageRect)
  }
}
