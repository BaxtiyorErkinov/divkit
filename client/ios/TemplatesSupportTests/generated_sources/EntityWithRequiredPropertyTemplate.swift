// Generated code. Do not modify.

@testable import DivKit

import CommonCore
import Foundation
import Serialization
import TemplatesSupport

public final class EntityWithRequiredPropertyTemplate: TemplateValue, TemplateDeserializable {
  public static let type: String = "entity_with_required_property"
  public let parent: String? // at least 1 char
  public let property: Field<Expression<String>>? // at least 1 char

  static let parentValidator: AnyValueValidator<String> =
    makeStringValidator(minLength: 1)

  public convenience init(dictionary: [String: Any], templateToType: TemplateToType) throws {
    do {
      self.init(
        parent: try dictionary.getOptionalField("type", validator: Self.parentValidator),
        property: try dictionary.getOptionalExpressionField("property")
      )
    } catch let DeserializationError.invalidFieldRepresentation(field: field, representation: representation) {
      throw DeserializationError.invalidFieldRepresentation(field: "entity_with_required_property_template." + field, representation: representation)
    }
  }

  init(
    parent: String?,
    property: Field<Expression<String>>? = nil
  ) {
    self.parent = parent
    self.property = property
  }

  private static func resolveOnlyLinks(context: Context, parent: EntityWithRequiredPropertyTemplate?) -> DeserializationResult<EntityWithRequiredProperty> {
    let propertyValue = parent?.property?.resolveValue(context: context, validator: ResolvedValue.propertyValidator) ?? .noValue
    var errors = mergeErrors(
      propertyValue.errorsOrWarnings?.map { .right($0.asError(deserializing: "property", level: .error)) }
    )
    if case .noValue = propertyValue {
      errors.append(.right(FieldError(fieldName: "property", level: .error, error: .requiredFieldIsMissing)))
    }
    guard
      let propertyNonNil = propertyValue.value
    else {
      return .failure(NonEmptyArray(errors)!)
    }
    let result = EntityWithRequiredProperty(
      property: propertyNonNil
    )
    return errors.isEmpty ? .success(result) : .partialSuccess(result, warnings: NonEmptyArray(errors)!)
  }

  public static func resolveValue(context: Context, parent: EntityWithRequiredPropertyTemplate?, useOnlyLinks: Bool) -> DeserializationResult<EntityWithRequiredProperty> {
    if useOnlyLinks {
      return resolveOnlyLinks(context: context, parent: parent)
    }
    var propertyValue: DeserializationResult<Expression<String>> = parent?.property?.value() ?? .noValue
    context.templateData.forEach { key, __dictValue in
      switch key {
      case "property":
        propertyValue = deserialize(__dictValue, validator: ResolvedValue.propertyValidator).merged(with: propertyValue)
      case parent?.property?.link:
        propertyValue = propertyValue.merged(with: deserialize(__dictValue, validator: ResolvedValue.propertyValidator))
      default: break
      }
    }
    var errors = mergeErrors(
      propertyValue.errorsOrWarnings?.map { Either.right($0.asError(deserializing: "property", level: .error)) }
    )
    if case .noValue = propertyValue {
      errors.append(.right(FieldError(fieldName: "property", level: .error, error: .requiredFieldIsMissing)))
    }
    guard
      let propertyNonNil = propertyValue.value
    else {
      return .failure(NonEmptyArray(errors)!)
    }
    let result = EntityWithRequiredProperty(
      property: propertyNonNil
    )
    return errors.isEmpty ? .success(result) : .partialSuccess(result, warnings: NonEmptyArray(errors)!)
  }

  private func mergedWithParent(templates: Templates) throws -> EntityWithRequiredPropertyTemplate {
    guard let parent = parent, parent != Self.type else { return self }
    guard let parentTemplate = templates[parent] as? EntityWithRequiredPropertyTemplate else {
      throw DeserializationError.unknownType(type: parent)
    }
    let mergedParent = try parentTemplate.mergedWithParent(templates: templates)

    return EntityWithRequiredPropertyTemplate(
      parent: nil,
      property: property ?? mergedParent.property
    )
  }

  public func resolveParent(templates: Templates) throws -> EntityWithRequiredPropertyTemplate {
    return try mergedWithParent(templates: templates)
  }
}
