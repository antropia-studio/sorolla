import UIKit

protocol CroppingOverlayViewAnimatorDelegate: AnyObject {
  func didProgressAnimation(interpolatedRect: CGRect) -> Void
}

class CroppingOverlayViewAnimator {
  private var displayLink: CADisplayLink?
  private var startRect: CGRect?
  private var targetRect: CGRect?
  private var progress: CGFloat = 0
  private var start: CFTimeInterval?
  private var duration: TimeInterval?
  private weak var delegate: CroppingOverlayViewAnimatorDelegate?

  func animateCropRect(from sourceRect: CGRect, to targetRect: CGRect, delegate: CroppingOverlayViewAnimatorDelegate, duration: TimeInterval = 0.5) {
    self.displayLink = CADisplayLink(target: self, selector: #selector(updateAnimation))

    self.startRect = sourceRect
    self.targetRect = targetRect
    self.delegate = delegate
    self.duration = duration
    self.start = CACurrentMediaTime()

    displayLink?.add(to: .main, forMode: .common)
  }

  @objc private func updateAnimation() {
    let now = CACurrentMediaTime()

    progress = (now - start!) / duration!

    if progress >= 1.0 {
      progress = 1.0
      invalidate()
    }

    var interpolationProgress = applyTimingFunction(to: progress)

    if let start = self.startRect, let target = self.targetRect {
      let interpolatedRect = CGRect(
        x: start.origin.x + (target.origin.x - start.origin.x) * interpolationProgress,
        y: start.origin.y + (target.origin.y - start.origin.y) * interpolationProgress,
        width: start.width + (target.width - start.width) * interpolationProgress,
        height: start.height + (target.height - start.height) * interpolationProgress
      )

      self.delegate?.didProgressAnimation(interpolatedRect: interpolatedRect)
    }
  }

  func invalidate() {
    displayLink?.invalidate()
    displayLink = nil
  }

  private func applyTimingFunction(to t: CGFloat) -> CGFloat {
    return t < 0.5 ? 2 * t * t : -1 + (4 - 2 * t) * t
  }
}
