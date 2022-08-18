// Generated code. Do not modify.

@testable import DivKit

import CommonCore
import Foundation
import Serialization
import TemplatesSupport

public final class EntityWithOptionalStringEnumPropertyTemplate: TemplateValue, TemplateDeserializable {
  public typealias Property = EntityWithOptionalStringEnumProperty.Property

  public static let type: String = "entity_with_optional_string_enum_property"
  public let parent: String? // at least 1 char
  public let property: Field<Expression<Property>>?

  static let parentValidator: AnyValueValidator<String> =
    makeStringValidator(minLength: 1)

  public convenience init(dictionary: [String: Any], templateToType: TemplateToType) throws {
    self.init(
      parent: try dictionary.getOptionalField("type", validator: Self.parentValidator),
      property: try dictionary.getOptionalExpressionField("property")
    )
  }

  init(
    parent: String?,
    property: Field<Expression<Property>>? = nil
  ) {
    self.parent = parent
    self.property = property
  }

  private static func resolveOnlyLinks(context: Context, parent: EntityWithOptionalStringEnumPropertyTemplate?) -> DeserializationResult<EntityWithOptionalStringEnumProperty> {
    let propertyValue = parent?.property?.resolveOptionalValue(context: context, validator: ResolvedValue.propertyValidator) ?? .noValue
    let errors = mergeErrors(
      propertyValue.errorsOrWarnings?.map { .right($0.asError(deserializing: "property", level: .warning)) }
    )
    let result = EntityWithOptionalStringEnumProperty(
      property: propertyValue.value
    )
    return errors.isEmpty ? .success(result) : .partialSuccess(result, warnings: NonEmptyArray(errors)!)
  }

  public static func resolveValue(context: Context, parent: EntityWithOptionalStringEnumPropertyTemplate?, useOnlyLinks: Bool) -> DeserializationResult<EntityWithOptionalStringEnumProperty> {
    if useOnlyLinks {
      return resolveOnlyLinks(context: context, parent: parent)
    }
    var propertyValue: DeserializationResult<Expression<EntityWithOptionalStringEnumProperty.Property>> = parent?.property?.value() ?? .noValue
    context.templateData.forEach { key, __dictValue in
      switch key {
      case "property":
        propertyValue = deserialize(__dictValue, validator: ResolvedValue.propertyValidator).merged(with: propertyValue)
      case parent?.property?.link:
        propertyValue = propertyValue.merged(with: deserialize(__dictValue, validator: ResolvedValue.propertyValidator))
      default: break
      }
    }
    let errors = mergeErrors(
      propertyValue.errorsOrWarnings?.map { Either.right($0.asError(deserializing: "property", level: .warning)) }
    )
    let result = EntityWithOptionalStringEnumProperty(
      property: propertyValue.value
    )
    return errors.isEmpty ? .success(result) : .partialSuccess(result, warnings: NonEmptyArray(errors)!)
  }

  private func mergedWithParent(templates: Templates) throws -> EntityWithOptionalStringEnumPropertyTemplate {
    guard let parent = parent, parent != Self.type else { return self }
    guard let parentTemplate = templates[parent] as? EntityWithOptionalStringEnumPropertyTemplate else {
      throw DeserializationError.unknownType(type: parent)
    }
    let mergedParent = try parentTemplate.mergedWithParent(templates: templates)

    return EntityWithOptionalStringEnumPropertyTemplate(
      parent: nil,
      property: property ?? mergedParent.property
    )
  }

  public func resolveParent(templates: Templates) throws -> EntityWithOptionalStringEnumPropertyTemplate {
    return try mergedWithParent(templates: templates)
  }
}
