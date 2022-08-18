// Generated code. Do not modify.

@testable import DivKit

import CommonCore
import Foundation
import Serialization
import TemplatesSupport

public final class EntityWithComplexPropertyTemplate: TemplateValue, TemplateDeserializable {
  public final class PropertyTemplate: TemplateValue, TemplateDeserializable {
    public let value: Field<Expression<URL>>?

    public convenience init(dictionary: [String: Any], templateToType: TemplateToType) throws {
      do {
        self.init(
          value: try dictionary.getOptionalExpressionField("value", transform: URL.init(string:))
        )
      } catch let DeserializationError.invalidFieldRepresentation(field: field, representation: representation) {
        throw DeserializationError.invalidFieldRepresentation(field: "property_template." + field, representation: representation)
      }
    }

    init(
      value: Field<Expression<URL>>? = nil
    ) {
      self.value = value
    }

    private static func resolveOnlyLinks(context: Context, parent: PropertyTemplate?) -> DeserializationResult<EntityWithComplexProperty.Property> {
      let valueValue = parent?.value?.resolveValue(context: context, transform: URL.init(string:)) ?? .noValue
      var errors = mergeErrors(
        valueValue.errorsOrWarnings?.map { .right($0.asError(deserializing: "value", level: .error)) }
      )
      if case .noValue = valueValue {
        errors.append(.right(FieldError(fieldName: "value", level: .error, error: .requiredFieldIsMissing)))
      }
      guard
        let valueNonNil = valueValue.value
      else {
        return .failure(NonEmptyArray(errors)!)
      }
      let result = EntityWithComplexProperty.Property(
        value: valueNonNil
      )
      return errors.isEmpty ? .success(result) : .partialSuccess(result, warnings: NonEmptyArray(errors)!)
    }

    public static func resolveValue(context: Context, parent: PropertyTemplate?, useOnlyLinks: Bool) -> DeserializationResult<EntityWithComplexProperty.Property> {
      if useOnlyLinks {
        return resolveOnlyLinks(context: context, parent: parent)
      }
      var valueValue: DeserializationResult<Expression<URL>> = parent?.value?.value() ?? .noValue
      context.templateData.forEach { key, __dictValue in
        switch key {
        case "value":
          valueValue = deserialize(__dictValue, transform: URL.init(string:)).merged(with: valueValue)
        case parent?.value?.link:
          valueValue = valueValue.merged(with: deserialize(__dictValue, transform: URL.init(string:)))
        default: break
        }
      }
      var errors = mergeErrors(
        valueValue.errorsOrWarnings?.map { Either.right($0.asError(deserializing: "value", level: .error)) }
      )
      if case .noValue = valueValue {
        errors.append(.right(FieldError(fieldName: "value", level: .error, error: .requiredFieldIsMissing)))
      }
      guard
        let valueNonNil = valueValue.value
      else {
        return .failure(NonEmptyArray(errors)!)
      }
      let result = EntityWithComplexProperty.Property(
        value: valueNonNil
      )
      return errors.isEmpty ? .success(result) : .partialSuccess(result, warnings: NonEmptyArray(errors)!)
    }

    private func mergedWithParent(templates: Templates) throws -> PropertyTemplate {
      return self
    }

    public func resolveParent(templates: Templates) throws -> PropertyTemplate {
      return try mergedWithParent(templates: templates)
    }
  }

  public static let type: String = "entity_with_complex_property"
  public let parent: String? // at least 1 char
  public let property: Field<PropertyTemplate>?

  static let parentValidator: AnyValueValidator<String> =
    makeStringValidator(minLength: 1)

  public convenience init(dictionary: [String: Any], templateToType: TemplateToType) throws {
    do {
      self.init(
        parent: try dictionary.getOptionalField("type", validator: Self.parentValidator),
        property: try dictionary.getOptionalField("property", templateToType: templateToType)
      )
    } catch let DeserializationError.invalidFieldRepresentation(field: field, representation: representation) {
      throw DeserializationError.invalidFieldRepresentation(field: "entity_with_complex_property_template." + field, representation: representation)
    }
  }

  init(
    parent: String?,
    property: Field<PropertyTemplate>? = nil
  ) {
    self.parent = parent
    self.property = property
  }

  private static func resolveOnlyLinks(context: Context, parent: EntityWithComplexPropertyTemplate?) -> DeserializationResult<EntityWithComplexProperty> {
    let propertyValue = parent?.property?.resolveValue(context: context, useOnlyLinks: true) ?? .noValue
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
    let result = EntityWithComplexProperty(
      property: propertyNonNil
    )
    return errors.isEmpty ? .success(result) : .partialSuccess(result, warnings: NonEmptyArray(errors)!)
  }

  public static func resolveValue(context: Context, parent: EntityWithComplexPropertyTemplate?, useOnlyLinks: Bool) -> DeserializationResult<EntityWithComplexProperty> {
    if useOnlyLinks {
      return resolveOnlyLinks(context: context, parent: parent)
    }
    var propertyValue: DeserializationResult<EntityWithComplexProperty.Property> = .noValue
    context.templateData.forEach { key, __dictValue in
      switch key {
      case "property":
        propertyValue = deserialize(__dictValue, templates: context.templates, templateToType: context.templateToType, type: EntityWithComplexPropertyTemplate.PropertyTemplate.self).merged(with: propertyValue)
      case parent?.property?.link:
        propertyValue = propertyValue.merged(with: deserialize(__dictValue, templates: context.templates, templateToType: context.templateToType, type: EntityWithComplexPropertyTemplate.PropertyTemplate.self))
      default: break
      }
    }
    if let parent = parent {
      propertyValue = propertyValue.merged(with: parent.property?.resolveValue(context: context, useOnlyLinks: true))
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
    let result = EntityWithComplexProperty(
      property: propertyNonNil
    )
    return errors.isEmpty ? .success(result) : .partialSuccess(result, warnings: NonEmptyArray(errors)!)
  }

  private func mergedWithParent(templates: Templates) throws -> EntityWithComplexPropertyTemplate {
    guard let parent = parent, parent != Self.type else { return self }
    guard let parentTemplate = templates[parent] as? EntityWithComplexPropertyTemplate else {
      throw DeserializationError.unknownType(type: parent)
    }
    let mergedParent = try parentTemplate.mergedWithParent(templates: templates)

    return EntityWithComplexPropertyTemplate(
      parent: nil,
      property: property ?? mergedParent.property
    )
  }

  public func resolveParent(templates: Templates) throws -> EntityWithComplexPropertyTemplate {
    let merged = try mergedWithParent(templates: templates)

    return EntityWithComplexPropertyTemplate(
      parent: nil,
      property: try merged.property?.resolveParent(templates: templates)
    )
  }
}
