// Generated code. Do not modify.

import CommonCore
import Foundation
import Serialization

public final class EntityWithArrayTemplate: TemplateValue, TemplateDeserializable {
  public static let type: String = "entity_with_array"
  public let parent: String? // at least 1 char
  public let array: Field<[EntityTemplate]>? // at least 1 elements

  static let parentValidator: AnyValueValidator<String> =
    makeStringValidator(minLength: 1)

  public convenience init(dictionary: [String: Any], templateToType: TemplateToType) throws {
    do {
      self.init(
        parent: try dictionary.getOptionalField("type", validator: Self.parentValidator),
        array: try dictionary.getOptionalArray("array", templateToType: templateToType)
      )
    } catch let DeserializationError.invalidFieldRepresentation(field: field, representation: representation) {
      throw DeserializationError.invalidFieldRepresentation(field: "entity_with_array_template." + field, representation: representation)
    }
  }

  init(
    parent: String?,
    array: Field<[EntityTemplate]>? = nil
  ) {
    self.parent = parent
    self.array = array
  }

  private static func resolveOnlyLinks(context: Context, parent: EntityWithArrayTemplate?) -> DeserializationResult<EntityWithArray> {
    let arrayValue = parent?.array?.resolveValue(context: context, validator: ResolvedValue.arrayValidator, useOnlyLinks: true) ?? .noValue
    var errors = mergeErrors(
      arrayValue.errorsOrWarnings?.map { .right($0.asError(deserializing: "array", level: .error)) }
    )
    if case .noValue = arrayValue {
      errors.append(.right(FieldError(fieldName: "array", level: .error, error: .requiredFieldIsMissing)))
    }
    guard
      let arrayNonNil = arrayValue.value
    else {
      return .failure(NonEmptyArray(errors)!)
    }
    let result = EntityWithArray(
      array: arrayNonNil
    )
    return errors.isEmpty ? .success(result) : .partialSuccess(result, warnings: NonEmptyArray(errors)!)
  }

  public static func resolveValue(context: Context, parent: EntityWithArrayTemplate?, useOnlyLinks: Bool) -> DeserializationResult<EntityWithArray> {
    if useOnlyLinks {
      return resolveOnlyLinks(context: context, parent: parent)
    }
    var arrayValue: DeserializationResult<[Entity]> = .noValue
    context.templateData.forEach { key, __dictValue in
      switch key {
      case "array":
        arrayValue = deserialize(__dictValue, templates: context.templates, templateToType: context.templateToType, validator: ResolvedValue.arrayValidator, type: EntityTemplate.self).merged(with: arrayValue)
      case parent?.array?.link:
        arrayValue = arrayValue.merged(with: deserialize(__dictValue, templates: context.templates, templateToType: context.templateToType, validator: ResolvedValue.arrayValidator, type: EntityTemplate.self))
      default: break
      }
    }
    if let parent = parent {
      arrayValue = arrayValue.merged(with: parent.array?.resolveValue(context: context, validator: ResolvedValue.arrayValidator, useOnlyLinks: true))
    }
    var errors = mergeErrors(
      arrayValue.errorsOrWarnings?.map { Either.right($0.asError(deserializing: "array", level: .error)) }
    )
    if case .noValue = arrayValue {
      errors.append(.right(FieldError(fieldName: "array", level: .error, error: .requiredFieldIsMissing)))
    }
    guard
      let arrayNonNil = arrayValue.value
    else {
      return .failure(NonEmptyArray(errors)!)
    }
    let result = EntityWithArray(
      array: arrayNonNil
    )
    return errors.isEmpty ? .success(result) : .partialSuccess(result, warnings: NonEmptyArray(errors)!)
  }

  private func mergedWithParent(templates: Templates) throws -> EntityWithArrayTemplate {
    guard let parent = parent, parent != Self.type else { return self }
    guard let parentTemplate = templates[parent] as? EntityWithArrayTemplate else {
      throw DeserializationError.unknownType(type: parent)
    }
    let mergedParent = try parentTemplate.mergedWithParent(templates: templates)

    return EntityWithArrayTemplate(
      parent: nil,
      array: array ?? mergedParent.array
    )
  }

  public func resolveParent(templates: Templates) throws -> EntityWithArrayTemplate {
    let merged = try mergedWithParent(templates: templates)

    return EntityWithArrayTemplate(
      parent: nil,
      array: try merged.array?.resolveParent(templates: templates)
    )
  }
}
