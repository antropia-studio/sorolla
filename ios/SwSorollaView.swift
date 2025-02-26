import SnapKit
import UIKit

@objc public class SwSorollaView: UIView {
  lazy var imageView = UIImageView()
  lazy var croppingOverlayView = CroppingOverlayView()

  override init(frame: CGRect) {
    super.init(frame: frame)
    
    self.createViews()
  }

  required init(coder aDecoder: NSCoder) {
    fatalError("init(coder:) has not been implemented")
  }

  @objc public func setUri(_ uri: String) {
    DispatchQueue.global(qos: .userInitiated).async {
      if let url = URL(string: uri),
        let data = try? Data(contentsOf: url),
        let image = UIImage(data: data)
      {
        DispatchQueue.main.async {
          let imageViewSize = self.imageView.frame.size
          self.imageView.image = image
          let scale = min(
            imageViewSize.width / image.size.width,
            imageViewSize.height / image.size.height
          )
          let imageSize = CGSize(
            width: image.size.width * scale,
            height: image.size.height * scale
          )
          let origin = CGPoint(
            x: (imageViewSize.width - imageSize.width) / 2,
            y: (imageViewSize.height - imageSize.height) / 2
          )
          let rect = CGRect(origin: origin, size: imageSize)
          
          self.croppingOverlayView.setImageRect(rect: rect)
        }
      }
    }
  }

  private func createViews() {
    self.addSubview(imageView)

    imageView.contentMode = .scaleAspectFit
    imageView.snp.makeConstraints { (make) -> Void in
      make.edges
        .equalTo(snp.edges)
        .inset(UIEdgeInsets(top: 10, left: 10, bottom: 10, right: 10))
    }
    
    self.addSubview(croppingOverlayView)
    croppingOverlayView.snp.makeConstraints { (make) -> Void in
      make.edges
        .equalTo(snp.edges)
        .inset(UIEdgeInsets(top: 10, left: 10, bottom: 10, right: 10))
    }
  }
}
