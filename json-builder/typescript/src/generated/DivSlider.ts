// Generated code. Do not modify.

import { Exact, IntBoolean, NonEmptyArray } from '../helper';
import { TemplateBlock, Type } from '../template';
import { DivExpression } from '../expression';

import {
    DivAlignmentHorizontal,
    DivAlignmentVertical,
    DivAppearanceTransition,
    DivBackground,
    DivChangeTransition,
    DivDrawable,
    DivFontWeight,
    DivSize,
    DivSizeUnit,
    DivTransitionTrigger,
    DivVisibility,
    IDivAccessibility,
    IDivAction,
    IDivBorder,
    IDivEdgeInsets,
    IDivExtension,
    IDivFocus,
    IDivPoint,
    IDivTooltip,
    IDivTransform,
    IDivVisibilityAction,
} from './';

/**
 * Slider for selecting a value in the range.
 */
export class DivSlider<T extends DivSliderProps = DivSliderProps> {
    readonly _props?: Exact<DivSliderProps, T>;

    readonly type = 'slider';
    /**
     * Accessibility for disabled people.
     */
    accessibility?: Type<IDivAccessibility>;
    /**
     * Horizontal alignment of an element inside the parent element.
     */
    alignment_horizontal?: Type<DivAlignmentHorizontal> | DivExpression;
    /**
     * Vertical alignment of an element inside the parent element.
     */
    alignment_vertical?: Type<DivAlignmentVertical> | DivExpression;
    /**
     * Sets transparency of the entire element: `0` — completely transparent, `1` — opaque.
     */
    alpha?: Type<number> | DivExpression;
    /**
     * Element background. It can contain multiple layers.
     */
    background?: Type<NonEmptyArray<DivBackground>>;
    /**
     * Element stroke.
     */
    border?: Type<IDivBorder>;
    /**
     * Merges cells in a column of the [grid](div-grid.md) element.
     */
    column_span?: Type<number> | DivExpression;
    /**
     * Extensions for additional processing of an element. The list of extensions is given in 
     * [DivExtension](../../extensions.dita).
     */
    extensions?: Type<NonEmptyArray<IDivExtension>>;
    /**
     * Parameters when focusing on an element or losing focus.
     */
    focus?: Type<IDivFocus>;
    /**
     * Element height. For Android: if there is text in this or in a child element, specify height in
     * `sp` to scale the element together with the text. To learn more about units of size
     * measurement, see [Layout inside the card](../../layout.dita).
     */
    height?: Type<DivSize>;
    /**
     * Element ID. It must be unique within the root element. It is used as `accessibilityIdentifier`
     * on iOS.
     */
    id?: Type<string>;
    /**
     * External margins from the element stroke.
     */
    margins?: Type<IDivEdgeInsets>;
    /**
     * Maximum value. It must be greater than the minimum value.
     */
    max_value?: Type<number> | DivExpression;
    /**
     * Minimum value.
     */
    min_value?: Type<number> | DivExpression;
    /**
     * Internal margins from the element stroke.
     */
    paddings?: Type<IDivEdgeInsets>;
    /**
     * Merges cells in a string of the [grid](div-grid.dita) element.
     */
    row_span?: Type<number> | DivExpression;
    /**
     * Accessibility for the secondary thumb.
     */
    secondary_value_accessibility?: Type<IDivAccessibility>;
    /**
     * List of [actions](div-action.md) to be executed when selecting an element in
     * [pager](div-pager.md).
     */
    selected_actions?: Type<NonEmptyArray<IDivAction>>;
    /**
     * Style of the second pointer.
     */
    thumb_secondary_style?: Type<DivDrawable>;
    /**
     * Text style in the second pointer.
     */
    thumb_secondary_text_style?: Type<IDivSliderTextStyle>;
    /**
     * Name of the variable to store the current value of the secondary thumb.
     */
    thumb_secondary_value_variable?: Type<string>;
    /**
     * Style of the first pointer.
     */
    thumb_style: Type<DivDrawable>;
    /**
     * Text style in the first pointer.
     */
    thumb_text_style?: Type<IDivSliderTextStyle>;
    /**
     * Name of the variable to store the current thumb value.
     */
    thumb_value_variable?: Type<string>;
    /**
     * Style of active serifs.
     */
    tick_mark_active_style?: Type<DivDrawable>;
    /**
     * Style of inactive serifs.
     */
    tick_mark_inactive_style?: Type<DivDrawable>;
    /**
     * Tooltips linked to an element. A tooltip can be shown by `div-action://show_tooltip?id=`,
     * hidden by `div-action://hide_tooltip?id=` where `id` — tooltip id.
     */
    tooltips?: Type<NonEmptyArray<IDivTooltip>>;
    /**
     * Style of the active part of a scale.
     */
    track_active_style: Type<DivDrawable>;
    /**
     * Style of the inactive part of a scale.
     */
    track_inactive_style: Type<DivDrawable>;
    /**
     * Transformation of the element. Applies the passed transform to the element. The content that
     * does not fit into the original view will be cut off.
     */
    transform?: Type<IDivTransform>;
    /**
     * Change animation. It is played when the position or size of an element changes in the new
     * layout.
     */
    transition_change?: Type<DivChangeTransition>;
    /**
     * Appearance animation. It is played when an element with a new ID appears. To learn more about
     * the concept of transitions, see [Animated
     * transitions](../../interaction.dita#animation/transition-animation).
     */
    transition_in?: Type<DivAppearanceTransition>;
    /**
     * Disappearance animation. It is played when an element disappears in the new layout.
     */
    transition_out?: Type<DivAppearanceTransition>;
    /**
     * Animation starting triggers. Default value: `[state_change, visibility_change]`.
     */
    transition_triggers?: Type<NonEmptyArray<DivTransitionTrigger>>;
    /**
     * Element visibility.
     */
    visibility?: Type<DivVisibility> | DivExpression;
    /**
     * Tracking visibility of a single element. Not used if the `visibility_actions` parameter is
     * set.
     */
    visibility_action?: Type<IDivVisibilityAction>;
    /**
     * Actions when an element appears on the screen.
     */
    visibility_actions?: Type<NonEmptyArray<IDivVisibilityAction>>;
    /**
     * Element width.
     */
    width?: Type<DivSize>;

    constructor(props: Exact<DivSliderProps, T>) {
        this.accessibility = props.accessibility;
        this.alignment_horizontal = props.alignment_horizontal;
        this.alignment_vertical = props.alignment_vertical;
        this.alpha = props.alpha;
        this.background = props.background;
        this.border = props.border;
        this.column_span = props.column_span;
        this.extensions = props.extensions;
        this.focus = props.focus;
        this.height = props.height;
        this.id = props.id;
        this.margins = props.margins;
        this.max_value = props.max_value;
        this.min_value = props.min_value;
        this.paddings = props.paddings;
        this.row_span = props.row_span;
        this.secondary_value_accessibility = props.secondary_value_accessibility;
        this.selected_actions = props.selected_actions;
        this.thumb_secondary_style = props.thumb_secondary_style;
        this.thumb_secondary_text_style = props.thumb_secondary_text_style;
        this.thumb_secondary_value_variable = props.thumb_secondary_value_variable;
        this.thumb_style = props.thumb_style;
        this.thumb_text_style = props.thumb_text_style;
        this.thumb_value_variable = props.thumb_value_variable;
        this.tick_mark_active_style = props.tick_mark_active_style;
        this.tick_mark_inactive_style = props.tick_mark_inactive_style;
        this.tooltips = props.tooltips;
        this.track_active_style = props.track_active_style;
        this.track_inactive_style = props.track_inactive_style;
        this.transform = props.transform;
        this.transition_change = props.transition_change;
        this.transition_in = props.transition_in;
        this.transition_out = props.transition_out;
        this.transition_triggers = props.transition_triggers;
        this.visibility = props.visibility;
        this.visibility_action = props.visibility_action;
        this.visibility_actions = props.visibility_actions;
        this.width = props.width;
    }
}

interface DivSliderProps {
    /**
     * Accessibility for disabled people.
     */
    accessibility?: Type<IDivAccessibility>;
    /**
     * Horizontal alignment of an element inside the parent element.
     */
    alignment_horizontal?: Type<DivAlignmentHorizontal> | DivExpression;
    /**
     * Vertical alignment of an element inside the parent element.
     */
    alignment_vertical?: Type<DivAlignmentVertical> | DivExpression;
    /**
     * Sets transparency of the entire element: `0` — completely transparent, `1` — opaque.
     */
    alpha?: Type<number> | DivExpression;
    /**
     * Element background. It can contain multiple layers.
     */
    background?: Type<NonEmptyArray<DivBackground>>;
    /**
     * Element stroke.
     */
    border?: Type<IDivBorder>;
    /**
     * Merges cells in a column of the [grid](div-grid.md) element.
     */
    column_span?: Type<number> | DivExpression;
    /**
     * Extensions for additional processing of an element. The list of extensions is given in 
     * [DivExtension](../../extensions.dita).
     */
    extensions?: Type<NonEmptyArray<IDivExtension>>;
    /**
     * Parameters when focusing on an element or losing focus.
     */
    focus?: Type<IDivFocus>;
    /**
     * Element height. For Android: if there is text in this or in a child element, specify height in
     * `sp` to scale the element together with the text. To learn more about units of size
     * measurement, see [Layout inside the card](../../layout.dita).
     */
    height?: Type<DivSize>;
    /**
     * Element ID. It must be unique within the root element. It is used as `accessibilityIdentifier`
     * on iOS.
     */
    id?: Type<string>;
    /**
     * External margins from the element stroke.
     */
    margins?: Type<IDivEdgeInsets>;
    /**
     * Maximum value. It must be greater than the minimum value.
     */
    max_value?: Type<number> | DivExpression;
    /**
     * Minimum value.
     */
    min_value?: Type<number> | DivExpression;
    /**
     * Internal margins from the element stroke.
     */
    paddings?: Type<IDivEdgeInsets>;
    /**
     * Merges cells in a string of the [grid](div-grid.dita) element.
     */
    row_span?: Type<number> | DivExpression;
    /**
     * Accessibility for the secondary thumb.
     */
    secondary_value_accessibility?: Type<IDivAccessibility>;
    /**
     * List of [actions](div-action.md) to be executed when selecting an element in
     * [pager](div-pager.md).
     */
    selected_actions?: Type<NonEmptyArray<IDivAction>>;
    /**
     * Style of the second pointer.
     */
    thumb_secondary_style?: Type<DivDrawable>;
    /**
     * Text style in the second pointer.
     */
    thumb_secondary_text_style?: Type<IDivSliderTextStyle>;
    /**
     * Name of the variable to store the current value of the secondary thumb.
     */
    thumb_secondary_value_variable?: Type<string>;
    /**
     * Style of the first pointer.
     */
    thumb_style: Type<DivDrawable>;
    /**
     * Text style in the first pointer.
     */
    thumb_text_style?: Type<IDivSliderTextStyle>;
    /**
     * Name of the variable to store the current thumb value.
     */
    thumb_value_variable?: Type<string>;
    /**
     * Style of active serifs.
     */
    tick_mark_active_style?: Type<DivDrawable>;
    /**
     * Style of inactive serifs.
     */
    tick_mark_inactive_style?: Type<DivDrawable>;
    /**
     * Tooltips linked to an element. A tooltip can be shown by `div-action://show_tooltip?id=`,
     * hidden by `div-action://hide_tooltip?id=` where `id` — tooltip id.
     */
    tooltips?: Type<NonEmptyArray<IDivTooltip>>;
    /**
     * Style of the active part of a scale.
     */
    track_active_style: Type<DivDrawable>;
    /**
     * Style of the inactive part of a scale.
     */
    track_inactive_style: Type<DivDrawable>;
    /**
     * Transformation of the element. Applies the passed transform to the element. The content that
     * does not fit into the original view will be cut off.
     */
    transform?: Type<IDivTransform>;
    /**
     * Change animation. It is played when the position or size of an element changes in the new
     * layout.
     */
    transition_change?: Type<DivChangeTransition>;
    /**
     * Appearance animation. It is played when an element with a new ID appears. To learn more about
     * the concept of transitions, see [Animated
     * transitions](../../interaction.dita#animation/transition-animation).
     */
    transition_in?: Type<DivAppearanceTransition>;
    /**
     * Disappearance animation. It is played when an element disappears in the new layout.
     */
    transition_out?: Type<DivAppearanceTransition>;
    /**
     * Animation starting triggers. Default value: `[state_change, visibility_change]`.
     */
    transition_triggers?: Type<NonEmptyArray<DivTransitionTrigger>>;
    /**
     * Element visibility.
     */
    visibility?: Type<DivVisibility> | DivExpression;
    /**
     * Tracking visibility of a single element. Not used if the `visibility_actions` parameter is
     * set.
     */
    visibility_action?: Type<IDivVisibilityAction>;
    /**
     * Actions when an element appears on the screen.
     */
    visibility_actions?: Type<NonEmptyArray<IDivVisibilityAction>>;
    /**
     * Element width.
     */
    width?: Type<DivSize>;
}

export interface IDivSliderTextStyle {
    /**
     * Font size.
     */
    font_size: Type<number> | DivExpression;
    font_size_unit?: Type<DivSizeUnit> | DivExpression;
    /**
     * Style.
     */
    font_weight?: Type<DivFontWeight> | DivExpression;
    /**
     * Shift relative to the center.
     */
    offset?: Type<IDivPoint>;
    /**
     * Text color.
     */
    text_color?: Type<string> | DivExpression;
}
