import UIKit

extension CroppingOverlayView {
  func animateCropRect(from sourceRect: CGRect, to targetRect: CGRect, duration: TimeInterval = 0.5) {
    cropRect = sourceRect // To avoid flickering while the animation starts
    
    displayLink = CADisplayLink(target: self, selector: #selector(updateAnimation))

    animationStartRect = sourceRect
    animationTargetRect = targetRect
    animationDuration = duration
    animationStart = CACurrentMediaTime()

    displayLink?.add(to: .main, forMode: .common)
  }

  @objc private func updateAnimation() {
    let now = CACurrentMediaTime()

    animationProgress = (now - animationStart!) / animationDuration!

    var interpolationProgress = applyTimingFunction(to: animationProgress)

    if animationProgress >= 1.0 {
      animationProgress = 1.0
      interpolationProgress = 1.0
      displayLink?.invalidate()
      displayLink = nil
    }

    if let start = animationStartRect, let target = animationTargetRect {
      cropRect = CGRect(
        x: start.origin.x + (target.origin.x - start.origin.x) * interpolationProgress,
        y: start.origin.y + (target.origin.y - start.origin.y) * interpolationProgress,
        width: start.width + (target.width - start.width) * interpolationProgress,
        height: start.height + (target.height - start.height) * interpolationProgress
      )
    }

    setNeedsDisplay()
  }

  private func applyTimingFunction(to t: CGFloat) -> CGFloat {
    return t < 0.5 ? 2 * t * t : -1 + (4 - 2 * t) * t
  }
}
