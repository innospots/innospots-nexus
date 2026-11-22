"use client";

import { useI18n } from "@pactor-app/runtime";

export interface DrawerBackdropProps {
    open?: boolean;
    onClose?: () => void;
}

/**
 * 受控 Drawer 在 @pactor-app/ui 中 modal=false，遮罩不拦截点击；
 * 在列表等页面上叠一层可点空白关闭的半透明幕布。
 */
export function DrawerBackdrop({ open, onClose }: DrawerBackdropProps) {
    const { t } = useI18n();
    if (open !== true) {
        return null;
    }
    return (
        <button
            type="button"
            aria-label={t("common.ui.close")}
            className="fixed inset-0 z-[45] cursor-default bg-black/40"
            onClick={() => onClose?.()}
        />
    );
}
