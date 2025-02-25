import Foundation

import CommonCorePublic

enum ToStringFunctions {
  static var all: [CalcExpression.Symbol: Function] = [
    .function("toString", arity: .exactly(1)): OverloadedFunction(
      functions: [
        boolFunction,
        intFunction,
        doubleFunction,
        colorFunction,
        urlFunction
      ]
    ),
    .method("toString", arity: .exactly(1)): OverloadedFunction(
      functions: [
        stringFunction,
        boolFunction,
        intFunction,
        doubleFunction,
        colorFunction,
        urlFunction
      ]
    )
  ]
}

private let stringFunction = FunctionUnary<String, String> { $0 }

private let boolFunction = FunctionUnary<Bool, String> { $0.description }

private let intFunction = FunctionUnary<Int, String> { $0.description }

private let doubleFunction = FunctionUnary<Double, String> {
  guard let string = $0.toString() else {
    throw CalcExpression.Error.message(
      "Failed to evaluate [toString()]. Unable to convert value to String."
    )
  }
  return string
}

private let colorFunction = FunctionUnary<Color, String> { $0.argbString }

private let urlFunction = FunctionUnary<URL, String> { $0.description }

extension Double {
  fileprivate func toString() -> String? {
    let formatter = NumberFormatter()
    formatter.minimumFractionDigits = 0
    formatter.maximumFractionDigits = 14
    formatter.decimalSeparator = "."
    return formatter.string(from: NSNumber(value: self))
  }
}
