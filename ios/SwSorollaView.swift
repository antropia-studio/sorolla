import SnapKit
import UIKit

private let PADDING = 10.0

@objc public class SwSorollaView: UIView {
  private lazy var imageView = TransformableImageView()
  private lazy var croppingOverlayView = CroppingOverlayView(padding: PADDING)
  private var mode: Mode = .none
  private var panGesture: UIPanGestureRecognizer!
  private var panAction: PanAction? = nil
  private var lastPanGestureLocation: CGPoint?
  @objc public var onEditFinish: ((String) -> Void)?

  override init(frame: CGRect) {
    super.init(frame: frame)

    self.clipsToBounds = true
    self.createViews()
    self.createGestureRecognizers()
  }

  required init?(coder aDecoder: NSCoder) {
    super.init(coder: aDecoder)
  }

  @objc public func setUri(_ uri: String) {
    DispatchQueue.global(qos: .userInitiated).async {
      if let url = URL(string: uri),
        let data = try? Data(contentsOf: url),
        let image = UIImage(data: data)
      {
        DispatchQueue.main.async {
          self.imageView.image = image
          self.imageView.reset()

          DispatchQueue.main.asyncAfter(deadline: .now() + 0.1) {
            self.croppingOverlayView.setImageRect(rect: self.imageView.contentClippingRect)
          }
        }
      }
    }
  }

  @objc public func setMode(_ strMode: String) {
    let mode = Mode(rawValue: strMode)!

    switch mode {
    case .none:
      isUserInteractionEnabled = false
      UIView.animate(withDuration: 0.2, animations: {
        self.croppingOverlayView.alpha = 0
      }, completion: { finished in
        self.croppingOverlayView.isHidden = true
      })
    case .transform:
      isUserInteractionEnabled = true
      self.croppingOverlayView.setNeedsDisplay()
      self.croppingOverlayView.isHidden = false
      UIView.animate(withDuration: 0.2, animations: {
        self.croppingOverlayView.alpha = 1
      })
    case .settings:
      break
    }
  }

  @objc public func setSettings(brightness: Float, saturation: Float, contrast: Float) {
    imageView.setSettings(brightness: brightness, saturation: saturation, contrast: contrast)
  }

  @objc public func setBackgroundAndOverlayColor(_ color: UIColor) {
    imageView.backgroundColor = color
    croppingOverlayView.setOverlayColor(color)
  }

  @objc public func mirrorHorizontally() {
    guard let cropRect = croppingOverlayView.cropRect else { return }

    imageView.mirror(on: .horizontal, rect: cropRect)
  }

  @objc public func mirrorVertically() {
    guard let cropRect = croppingOverlayView.cropRect else { return }

    imageView.mirror(on: .vertical, rect: cropRect)
  }

  @objc public func rotateCcw() {
    let result = croppingOverlayView.rotate90DegCcw()

    guard let result = result else { return }

    imageView.rotate90DegCcw(scale: result.scale, rect: result.fromRect, toRect: result.toRect)
  }

  @objc public func resetCurrentTransform() {
    self.imageView.reset(animated: true)
    self.croppingOverlayView.setImageRect(rect: self.imageView.contentClippingRect, update: false)
  }

  @objc public func acceptEdition() {
    /**
     * Unfortunately, if we send here the imageView itself to be rendered, all the transforms
     * are lost in the render process. That's why we need to send the parent (self) and to
     * do it cleanly, we hide the overlay.
     */
    let wasOverlayHidden = croppingOverlayView.isHidden
    croppingOverlayView.isHidden = true
    let url = renderViewToFile(cropRect: croppingOverlayView.cropRect!)
    croppingOverlayView.isHidden = wasOverlayHidden

    guard let url = url else { return }

    self.onEditFinish?(url.absoluteString)
  }

  private func createViews() {
    self.addSubview(imageView)
    imageView.snp.makeConstraints { (make) -> Void in
      make.edges
        .equalTo(snp.edges)
        .inset(UIEdgeInsets(top: PADDING, left: PADDING, bottom: PADDING, right: PADDING))
    }

    croppingOverlayView.isHidden = true
    croppingOverlayView.alpha = 0
    self.addSubview(croppingOverlayView)
    croppingOverlayView.snp.makeConstraints { (make) -> Void in
      make.edges
        .equalTo(snp.edges)
    }
  }

  private func createGestureRecognizers() {
    panGesture = UIPanGestureRecognizer()
    isUserInteractionEnabled = true
    addGestureRecognizer(panGesture)

    panGesture.addTarget(self, action: #selector(draggableFunction))
  }

  @objc func draggableFunction(_ sender: UIPanGestureRecognizer) {
    let location = sender.location(in: self.croppingOverlayView)

    switch (sender.state) {
    case .began:
      self.panAction = self.croppingOverlayView.onPanGestureStart(on: location)
      self.lastPanGestureLocation = location

    case .changed:
      let translation = CGVector(
        dx: location.x - self.lastPanGestureLocation!.x,
        dy: location.y - self.lastPanGestureLocation!.y
      )

      let canMove = self.croppingOverlayView.onPanGestureMove(
        translation: translation,
        withinRect: self.imageView.contentClippingRect
      )
      self.lastPanGestureLocation = location

      if canMove {
        switch (panAction) {
        case .move: self.imageView.move(translation)

        default: break
        }
      }

    case .ended:
      switch (panAction) {
      case .move:
        self.imageView.moveWithinBounds(self.croppingOverlayView.cropRect!)

      case .crop:
        let result = self.croppingOverlayView.onPanGestureEnded()

        self.imageView.refit(
          scale: result.scale,
          anchor: result.anchor,
          fromRect: result.fromRect,
          toRect: result.toRect
        )
      default: break
      }

      self.lastPanGestureLocation = nil
      self.panAction = nil
    default: break
    }
  }
}
