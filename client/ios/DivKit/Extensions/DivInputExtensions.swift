import CoreFoundation
import CoreGraphics
import Foundation

import BaseUIPublic
import CommonCorePublic
import LayoutKit

extension DivInput: DivBlockModeling {
  public func makeBlock(context: DivBlockModelingContext) throws -> Block {
    let textBinding = context.makeBinding(variableName: textVariable, defaultValue: "")
    return try applyBaseProperties(
      to: { try makeBaseBlock(context: context, textBinding: textBinding) },
      context: context,
      actionsHolder: nil,
      customAccessibilityParams: CustomAccessibilityParams { [unowned self] in
        accessibility?.resolveDescription(context.expressionResolver) ?? textBinding.value
      }
    )
  }

  private func makeBaseBlock(
    context: DivBlockModelingContext,
    textBinding: Binding<String>
  ) throws -> Block {
    let expressionResolver = context.expressionResolver

    let font = context.fontProvider.font(
      family: resolveFontFamily(expressionResolver) ?? "",
      weight: resolveFontWeight(expressionResolver),
      size: resolveFontSizeUnit(expressionResolver)
        .makeScaledValue(resolveFontSize(expressionResolver))
    )
    var typo = Typo(font: font).allowHeightOverrun

    let kern = CGFloat(resolveLetterSpacing(expressionResolver))
    if !kern.isApproximatelyEqualTo(0) {
      typo = typo.kerned(kern)
    }

    if let lineHeightInt = resolveLineHeight(expressionResolver) {
      let lineHeight = CGFloat(lineHeightInt)
      typo = typo.with(height: lineHeight)
      if lineHeight > font.defaultLineHeight() {
        typo = typo.with(baseline: (lineHeight - font.defaultLineHeight()) / 2)
      }
    }

    let hintValue = resolveHintText(expressionResolver) ?? ""
    let keyboardType = resolveKeyboardType(expressionResolver)

    let onFocusActions = focus?.onFocus?.uiActions(context: context) ?? []
    let onBlurActions = focus?.onBlur?.uiActions(context: context) ?? []

    let inputPath = context.parentPath + (id ?? DivInput.type)
    let isFocused = context.blockStateStorage.isFocused(path: inputPath)

    return TextInputBlock(
      widthTrait: resolveContentWidthTrait(context),
      heightTrait: resolveContentHeightTrait(context),
      hint: hintValue.with(typo: typo.with(color: resolveHintColor(expressionResolver))),
      textValue: textBinding,
      rawTextValue: mask?.makeRawVariable(context),
      textTypo: typo.with(color: resolveTextColor(expressionResolver)),
      multiLineMode: keyboardType == .multiLineText,
      inputType: keyboardType.system,
      highlightColor: resolveHighlightColor(expressionResolver),
      maxVisibleLines: resolveMaxVisibleLines(expressionResolver),
      selectAllOnFocus: resolveSelectAllOnFocus(expressionResolver),
      maskValidator: mask?.makeMaskValidator(expressionResolver),
      path: inputPath,
      isFocused: isFocused,
      onFocusActions: onFocusActions,
      onBlurActions: onBlurActions,
      parentScrollView: context.parentScrollView,
      validators: makeValidators(context),
      layoutDirection: context.layoutDirection,
      textAlignmentHorizontal: resolveTextAlignmentHorizontal(expressionResolver).textAlignment,
      textAlignmentVertical: resolveTextAlignmentVertical(expressionResolver).textAlignment,
      isEnabled: resolveIsEnabled(expressionResolver)
    )
  }
}

extension DivInput {
  fileprivate func makeValidators(_ context: DivBlockModelingContext) -> [TextInputValidator]? {
    let expressionResolver = context.expressionResolver
    return validators?.compactMap { validator -> TextInputValidator? in
      switch validator {
      case let .divInputValidatorRegex(regexValidator):
        let pattern = regexValidator.resolvePattern(expressionResolver) ?? ""
        guard let regex = try? NSRegularExpression(pattern: pattern) else {
          DivKitLogger.error("Invalid regex pattern '\(pattern)'")
          return nil
        }
        return TextInputValidator(
          isValid: context.makeBinding(variableName: regexValidator.variable, defaultValue: false),
          allowEmpty: regexValidator.resolveAllowEmpty(expressionResolver),
          validator: { $0.fullMatchesRegex(regex) },
          message: makeMessage(
            from: regexValidator.resolveLabelId(expressionResolver),
            storage: context.blockStateStorage,
            cardId: context.cardId
          )
        )
      case let .divInputValidatorExpression(expressionValidator):
        return TextInputValidator(
          isValid: context.makeBinding(
            variableName: expressionValidator.variable,
            defaultValue: false
          ),
          allowEmpty: expressionValidator.resolveAllowEmpty(expressionResolver),
          validator: { _ in expressionValidator.resolveCondition(expressionResolver) ?? true },
          message: makeMessage(
            from: expressionValidator.resolveLabelId(expressionResolver),
            storage: context.blockStateStorage,
            cardId: context.cardId
          )
        )
      }
    }
  }

  private func makeMessage(
    from labelId: String?,
    storage: DivBlockStateStorage,
    cardId: DivCardID
  ) -> () -> String? {
    {
      guard let labelId else {
        return nil
      }
      guard let state: TextBlockViewState = storage.getState(labelId, cardId: cardId) else {
        DivKitLogger.error("Can't find text with id '\(labelId)'")
        return nil
      }
      return state.text
    }
  }
}

extension DivAlignmentHorizontal {
  fileprivate var textAlignment: TextInputBlock.TextAlignmentHorizontal {
    switch self {
    case .left:
      .left
    case .center:
      .center
    case .right:
      .right
    case .start:
      .start
    case .end:
      .end
    }
  }
}

extension DivAlignmentVertical {
  fileprivate var textAlignment: TextInputBlock.TextAlignmentVertical {
    switch self {
    case .top:
      return .top
    case .center:
      return .center
    case .bottom:
      return .bottom
    case .baseline:
      DivKitLogger.warning("Baseline alignment is not supported.")
      return .center
    }
  }
}

extension DivInput.KeyboardType {
  fileprivate var system: TextInputBlock.InputType {
    switch self {
    case .singleLineText, .multiLineText:
      .default
    case .phone:
      .keyboard(.phonePad)
    case .number:
      .keyboard(.decimalPad)
    case .email:
      .keyboard(.emailAddress)
    case .uri:
      .keyboard(.URL)
    }
  }
}

extension DivInputMask {
  fileprivate func makeMaskValidator(_ resolver: ExpressionResolver) -> MaskValidator? {
    switch self {
    case let .divFixedLengthInputMask(divFixedLengthInputMask):
      MaskValidator(formatter: FixedLengthMaskFormatter(
        pattern: divFixedLengthInputMask.resolvePattern(resolver) ?? "",
        alwaysVisible: divFixedLengthInputMask.resolveAlwaysVisible(resolver),
        patternElements: divFixedLengthInputMask.patternElements
          .map { $0.makePatternElement(resolver) }
      ))
    case .divCurrencyInputMask:
      nil
    case .divPhoneInputMask:
      MaskValidator(formatter: PhoneMaskFormatter(
        masksByCountryCode: PhoneMasks().value.typedJSON(),
        extraSymbols: PhoneMasks.extraNumbers
      ))
    }
  }

  fileprivate func makeRawVariable(_ context: DivBlockModelingContext) -> Binding<String>? {
    switch self {
    case let .divFixedLengthInputMask(divFixedLengthInputMask):
      context.makeBinding(
        variableName: divFixedLengthInputMask.rawTextVariable,
        defaultValue: ""
      )
    case .divCurrencyInputMask:
      nil
    case let .divPhoneInputMask(divPhoneInputMask):
      context.makeBinding(
        variableName: divPhoneInputMask.rawTextVariable,
        defaultValue: ""
      )
    }
  }
}

extension DivFixedLengthInputMask.PatternElement {
  fileprivate func makePatternElement(_ resolver: ExpressionResolver) -> PatternElement {
    PatternElement(
      key: (resolveKey(resolver) ?? "").first ?? " ".first!,
      regex: try! NSRegularExpression(pattern: resolveRegex(resolver) ?? "a"),
      placeholder: resolvePlaceholder(resolver).first ?? " ".first!
    )
  }
}

extension String {
  fileprivate func fullMatchesRegex(_ regex: NSRegularExpression) -> Bool {
    let range = stringRangeAsNSRange(wholeStringRange, inString: self)
    return regex.matches(in: self, options: [], range: range)
      .contains { $0.range == range }
  }
}
