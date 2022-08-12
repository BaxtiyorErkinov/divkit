import type {
    DivJson,
    StatCallback,
    ErrorCallback,
    DivkitInstance,
    Platform,
    CustomActionCallback,
    Theme
} from './common';
import type { GlobalVariablesController } from './variables';

export function render(opts: {
    target: HTMLElement;
    json: DivJson;
    id: string;
    hydrate?: boolean;
    globalVariablesController?: GlobalVariablesController;
    onStat?: StatCallback;
    onCustomAction?: CustomActionCallback;
    onError?: ErrorCallback;
    platform?: Platform;
    /** EXPERIMENTAL SUPPORT */
    theme?: Theme;
}): DivkitInstance;

export { createVariable, createGlobalVariablesController } from './variables';
