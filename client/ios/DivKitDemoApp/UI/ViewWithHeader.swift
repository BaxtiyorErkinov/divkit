import SwiftUI

struct ViewWithHeader<Content>: View where Content: View {
  private let title: String
  private let background: Color
  private let presentationMode: Binding<PresentationMode>
  private let content: () -> Content
  
  init(
    _ title: String,
    background: Color,
    presentationMode: Binding<PresentationMode>,
    @ViewBuilder content: @escaping () -> Content
  ) {
    self.title = title
    self.background = background
    self.presentationMode = presentationMode
    self.content = content
  }
  
  var body: some View {
    VStack(spacing: 0) {
      Text(title)
        .font(ThemeFont.make(size: 28, weight: .medium))
        .foregroundColor(.white)
        .frame(maxWidth: .infinity, minHeight: ThemeSize.header)
        .background(background)
        .overlay(backButton, alignment: .leading)
      content()
      Spacer()
    }
    .modifier(StatusBarModifier(color: background))
    .navigationBarHidden(true)
  }
  
  private var backButton: some View {
    Button(action: { presentationMode.wrappedValue.dismiss() }) {
      Image(systemName: "chevron.backward")
        .applyHeaderButtonStyle()
    }
  }
}

private struct StatusBarModifier: ViewModifier {
  let color: Color

  func body(content: Content) -> some View {
    ZStack {
      VStack {
        GeometryReader { geometry in
          color
            .frame(height: geometry.safeAreaInsets.top)
            .edgesIgnoringSafeArea(.top)
          Spacer()
        }
      }
      content
    }
  }
}
