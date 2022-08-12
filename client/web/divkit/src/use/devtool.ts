import type { RootCtxValue } from '../context/root';
import type { DivBase, TemplateContext } from '../../typings/common';
import type { DivBaseData } from '../types/base';

function devtoolReal(node: HTMLElement, {
    json,
    origJson,
    templateContext,
    rootCtx
}: {
    json: Partial<DivBaseData>;
    origJson?: DivBase | undefined;
    templateContext: TemplateContext;
    rootCtx: RootCtxValue;
}) {
    rootCtx.registerComponent?.({
        node,
        json,
        origJson,
        templateContext
    });

    return {
        destroy() {
            rootCtx.unregisterComponent?.({ node });
        }
    };
}

export const devtool = process.env.DEVTOOL ? devtoolReal : undefined;
