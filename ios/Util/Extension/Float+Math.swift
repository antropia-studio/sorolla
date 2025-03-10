import Foundation

extension Float {
  /**
   * Given a value (self) and the full range of possible values (min, max),
   * this function returns the normalized 0-1 value where the value falls
   * inside the range. If value equals to min, it returns 0, if it equals to max,
   * it returns 1.
   */
  func normalize(min: Float, max: Float) -> Float {
    let size = max - min;

    return (self - min) / size
  }
}
