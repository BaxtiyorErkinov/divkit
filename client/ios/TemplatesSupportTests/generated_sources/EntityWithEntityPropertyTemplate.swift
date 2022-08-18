// Generated code. Do not modify.

@testable import DivKit

import CommonCore
import Foundation
import Serialization
import TemplatesSupport

public final class EntityWithEntityPropertyTemplate: TemplateValue, TemplateDeserializable {
  public static let type: String = "entity_with_entity_property"
  public let parent: String? // at least 1 char
  public let entity: Field<EntityTemplate>? // default value: .entityWithStringEnumProperty(EntityWithStringEnumProperty(property: .value(.second)))

  static let parentValidator: AnyValueValidator<String> =
    makeStringValidator(minLength: 1)

  public convenience init(dictionary: [String: Any], templateToType: TemplateToType) throws {
    self.init(
      parent: try dictionary.getOptionalField("type", validator: Self.parentValidator),
      entity: try dictionary.getOptionalField("entity", templateToType: templateToType)
    )
  }

  init(
    parent: String?,
    entity: Field<EntityTemplate>? = nil
  ) {
    self.parent = parent
    self.entity = entity
  }

  private static func resolveOnlyLinks(context: Context, parent: EntityWithEntityPropertyTemplate?) -> DeserializationResult<EntityWithEntityProperty> {
    let entityValue = parent?.entity?.resolveOptionalValue(context: context, validator: ResolvedValue.entityValidator, useOnlyLinks: true) ?? .noValue
    let errors = mergeErrors(
      entityValue.errorsOrWarnings?.map { .right($0.asError(deserializing: "entity", level: .warning)) }
    )
    let result = EntityWithEntityProperty(
      entity: entityValue.value
    )
    return errors.isEmpty ? .success(result) : .partialSuccess(result, warnings: NonEmptyArray(errors)!)
  }

  public static func resolveValue(context: Context, parent: EntityWithEntityPropertyTemplate?, useOnlyLinks: Bool) -> DeserializationResult<EntityWithEntityProperty> {
    if useOnlyLinks {
      return resolveOnlyLinks(context: context, parent: parent)
    }
    var entityValue: DeserializationResult<Entity> = .noValue
    context.templateData.forEach { key, __dictValue in
      switch key {
      case "entity":
        entityValue = deserialize(__dictValue, templates: context.templates, templateToType: context.templateToType, validator: ResolvedValue.entityValidator, type: EntityTemplate.self).merged(with: entityValue)
      case parent?.entity?.link:
        entityValue = entityValue.merged(with: deserialize(__dictValue, templates: context.templates, templateToType: context.templateToType, validator: ResolvedValue.entityValidator, type: EntityTemplate.self))
      default: break
      }
    }
    if let parent = parent {
      entityValue = entityValue.merged(with: parent.entity?.resolveOptionalValue(context: context, validator: ResolvedValue.entityValidator, useOnlyLinks: true))
    }
    let errors = mergeErrors(
      entityValue.errorsOrWarnings?.map { Either.right($0.asError(deserializing: "entity", level: .warning)) }
    )
    let result = EntityWithEntityProperty(
      entity: entityValue.value
    )
    return errors.isEmpty ? .success(result) : .partialSuccess(result, warnings: NonEmptyArray(errors)!)
  }

  private func mergedWithParent(templates: Templates) throws -> EntityWithEntityPropertyTemplate {
    guard let parent = parent, parent != Self.type else { return self }
    guard let parentTemplate = templates[parent] as? EntityWithEntityPropertyTemplate else {
      throw DeserializationError.unknownType(type: parent)
    }
    let mergedParent = try parentTemplate.mergedWithParent(templates: templates)

    return EntityWithEntityPropertyTemplate(
      parent: nil,
      entity: entity ?? mergedParent.entity
    )
  }

  public func resolveParent(templates: Templates) throws -> EntityWithEntityPropertyTemplate {
    let merged = try mergedWithParent(templates: templates)

    return EntityWithEntityPropertyTemplate(
      parent: nil,
      entity: merged.entity?.tryResolveParent(templates: templates)
    )
  }
}
