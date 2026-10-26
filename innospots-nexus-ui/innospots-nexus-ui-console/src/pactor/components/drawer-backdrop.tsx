"use client";

import { useUiText } from "@/i18n/ui";

export interface DrawerBackdropProps {
    open?: boolean;
    onClose?: () => void;
}

/**
 * 受控 Drawer 在 @pactor-app/ui 中 modal=false，遮罩不拦截点击；
 * 在列表等页面上叠一层可点空白关闭的半透明幕布。
 */
export function DrawerBackdrop({ open, onClose }: DrawerBackdropProps) {
    const t = useUiText();
    if (open !== true) {
        return null;
    }
    return (
        <button
            type="button"
            aria-label={t("关闭")}
            className="fixed inset-0 z-[45] cursor-default bg-black/40"
            onClick={() => onClose?.()}
        />
    );
}
