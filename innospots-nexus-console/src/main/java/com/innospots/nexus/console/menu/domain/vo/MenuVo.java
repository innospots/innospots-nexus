package com.innospots.nexus.console.menu.domain.vo;

import java.time.LocalDateTime;
import java.util.List;

import com.innospots.nexus.base.domain.enums.BasicStatus;
import com.innospots.nexus.console.menu.domain.enums.MenuOpenMode;
import com.innospots.nexus.console.menu.domain.enums.MenuType;

/**
 * 管理控制台菜单详情及嵌套子节点。
 *
 * @author Smars
 * @date 2026/09/13
 * @param menuId       菜单标识符
 * @param parentId     可选 parent 菜单标识符
 * @param menuKey      项目内唯一的稳定菜单键
 * @param menuName     显示名称
 * @param menuType     菜单节点类型
 * @param routePath    内部导航路径
 * @param componentKey 逻辑前端组件标识符
 * @param redirectPath 可选 redirect path
 * @param externalUrl  外部目标地址
 * @param icon         可选 icon 标识符
 * @param openMode     浏览器打开模式
 * @param visible      节点是否在导航中可见
 * @param status       生命周期状态
 * @param sortOrder    sibling 显示顺序
 * @param builtIn      节点是否由系统管理
 * @param createdAt    创建时间
 * @param updatedAt    最后更新时间
 * @param children     嵌套子菜单
 */
public record MenuVo(
        String menuId,
        String parentId,
        String menuKey,
        String menuName,
        MenuType menuType,
        String routePath,
        String componentKey,
        String redirectPath,
        String externalUrl,
        String icon,
        MenuOpenMode openMode,
        Boolean visible,
        BasicStatus status,
        Integer sortOrder,
        Boolean builtIn,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        List<MenuVo> children
) {

    public MenuVo {
        children = children == null ? List.of() : List.copyOf(children);
    }
}
