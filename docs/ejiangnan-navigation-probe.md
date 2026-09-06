# e江南 独立玻璃导航运行时记录

目标包：`com.wisedu.cpdaily.jiangnan`（模块包名 `org.orynnx.liquidejnu`）

## 已确认的运行时层级

在 N667J 的 e江南 3.0.7 主页面 UI 树中确认：

```text
view_content
└── tab_content (FrameLayout)
└── rl_bottom (RelativeLayout)
    └── tl_nav (FrameLayout / CpHomeBottomTabLayout)
        └── common_tab_layout
            ├── root -> ll_tap -> iv_tab_icon / tv_tab_title  × 5
```

原生 `root` 五项仍保持 `VISIBLE`, `clickable=true`，仅原生图标和标题透明；
它们继续持有 e江南自己的点击监听。模块行作为 `LiquidGlassHostLayout` 的子项
插入 `tl_nav`，不创建悬浮窗口。

## 当前模块映射

| 模块索引 | 标题 | 原生代理 |
| ---: | --- | --- |
| 0 | 首页 | 第 0 个 `root.performClick()` |
| 1 | 应用 | 第 1 个 `root.performClick()` |
| 2 | 消息 | 第 2 个 `root.performClick()` |
| 3 | 新闻 | 第 3 个 `root.performClick()` |
| 4 | 我的 | 第 4 个 `root.performClick()` |

模块按钮使用本地 Canvas 绘制的 Material Symbols 风格图标；液滴绑定到模块
行自身。液滴坐标会扣除 `LiquidGlassHostLayout` 的 shadow padding，避免
相对原生行出现一个 padding 的水平/垂直偏移。

## 设备证据（N667J）

- `rl_bottom`：`[0,2133][1080,2400]`
- `tl_nav`：`[0,2133][1080,2400]`
- 模块按钮：`首页 [39,2152][239,2303]` … `我的 [840,2152][1041,2303]`
- 选中液滴：`[39,2163][239,2292]`（与首页按钮同中心，内缩约 11px）
- `tab_content`：`[0,0][1080,2400]`
- 原生五个 `root` 仍存在且可点击；原生视觉节点不再绘制。

## 探针与限制

模块输出 `probe:ejiangnan-module-injected`、ClassLoader 和原生导航定位信息；
部分壳版本在 `onPackageLoaded` 阶段尚未把 `HomeActivity` 放入默认
ClassLoader，因此模块不依赖对该业务类的静态反射。模块同时挂接
`Instrumentation.callActivityOnResume`、`Activity.onResume` 和
`Activity.onAttachedToWindow`，并通过 DecorView 延迟扫描，在真实窗口出现后
再安装导航。安装失败会恢复原生视觉节点，不移除业务对象。
