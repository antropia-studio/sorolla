import UIKit

class TransformableImageView: UIImageView {
  init() {
    super.init(frame: CGRect.zero)
  }

  override init(frame: CGRect) {
    super.init(frame: frame)
  }

  required init?(coder aDecoder: NSCoder) {
    super.init(coder: aDecoder)
  }

  func reset() {
    self.transform = CGAffineTransform.identity
  }

  func refit(
    scale: CGFloat,
    anchor: Anchor,
    fromRect: CGRect,
    toRect: CGRect
  ) {
    let fromPoint = fromRect.bottomRight
    let toPoint = toRect.bottomRight
    let imageScale = self.image!.size.width / self.image!.size.height

    let anchor = CGPoint(x: self.frame.width / 2, y: (self.frame.width / imageScale) / 2)

//    UIView.animate(withDuration: 2) {
//      self.transform = self.transform.translatedBy(x: anchor.x, y: anchor.y)
//      self.layoutIfNeeded()
//    } completion: { finished in
//      UIView.animate(withDuration: 2) {
//        self.transform = self.transform.scaledBy(x: scale, y: scale)
//      } completion: { finished in
//        UIView.animate(withDuration: 2) {
//          self.transform = self.transform.translatedBy(x: -anchor.x, y: -anchor.y)
//        } completion: { finished in
//
//        }
//      }
//    }
  }
}
