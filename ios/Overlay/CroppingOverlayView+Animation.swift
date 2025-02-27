import UIKit

private let FPS = 60.0

extension CroppingOverlayView {
  func animateCropRect(from sourceRect: CGRect, to targetRect: CGRect, duration: TimeInterval = 0.5) {
    displayLink = CADisplayLink(target: self, selector: #selector(updateAnimation))

    animationStartRect = sourceRect
    animationTargetRect = targetRect
    animationProgress = 0
    animationStep = 1 / (duration * FPS)

    displayLink?.add(to: .main, forMode: .common)
  }

  @objc private func updateAnimation() {
    animationProgress += animationStep

    let interpolationProgress = applyTimingFunction(to: animationProgress)

    if animationProgress >= 1.0 {
        animationProgress = 1.0
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
    // https://easings.net/#easeInOutSine
    return -(cos(CGFloat.pi * t) - 1) / 2;
  }
}
