/** 布局与会话 UI 使用的用户快照形状。 */
export interface SessionUser {
  name?: string;
  username?: string;
}

/** 导航菜单形状（来自 sitemap）。 */
export interface NavMenu {
  id?: string;
  key?: string;
  label?: string;
  type?: string;
  pageId?: string;
  path?: string;
  title?: string;
  icon?: string;
  hidden?: boolean;
  children?: NavMenu[];
}
