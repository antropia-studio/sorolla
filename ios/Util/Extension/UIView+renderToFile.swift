import UIKit

extension UIView {
  func renderViewToFile(cropRect: CGRect, fileName: String = UUID().uuidString) -> URL? {
    guard let snapshot = viewToImage() else { return nil }
    guard let image = cropping(image: snapshot, toRect: cropRect * snapshot.scale) else { return nil }

    guard let jpegData = image.jpegData(compressionQuality: 1.0) else { return nil }
    guard let documentsDirectory = FileManager.default.urls(for: .cachesDirectory, in: .userDomainMask).first else { return nil }

    let fileURL = documentsDirectory.appendingPathComponent("\(fileName).jpg")

    do {
      try jpegData.write(to: fileURL)
      return fileURL
    } catch {
      print("Error saving image: \(error.localizedDescription)")
      return nil
    }
  }

  /**
   * Transforms a UIView into a UIImage by rendering its contents. Be warned, this method
   * does not take into account the own's view transforms (I guess because transforms are
   * relative to a view's parent). If you pass a UIImage view with some transforms applied
   * to this method, those transforms are not applied. Always pass the parent view.
   */
  private func viewToImage() -> UIImage? {
    let format = UIGraphicsImageRendererFormat()
    format.opaque = false

    let renderer = UIGraphicsImageRenderer(size: frame.size, format: format)
    return renderer.image { _ in drawHierarchy(in: bounds, afterScreenUpdates: true) }
  }

  private func cropping(image: UIImage, toRect cropRect: CGRect) -> UIImage? {
    guard let cutImageRef: CGImage = image.cgImage?.cropping(to: cropRect) else { return nil }

    return UIImage(cgImage: cutImageRef)
  }
}
