import SwiftUI

import CommonCore

struct ScannerView: UIViewControllerRepresentable {
  private let disposePool = AutodisposePool()

  @Binding
  var result: String

  func makeUIViewController(context _: Context) -> UIViewController {
    let controller = ScannerViewController()
    controller.result.currentAndNewValues
      .addObserver {
        if result != $0 {
          result = $0
        }
      }
      .dispose(in: disposePool)
    return controller
  }
  
  func updateUIViewController(_: UIViewController, context _: Context) {}
}
