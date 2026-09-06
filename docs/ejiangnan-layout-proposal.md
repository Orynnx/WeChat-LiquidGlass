# e江南 主页面 Layout 方案（待确认）

来源：`base.apk`（e江南 3.0.7）的反编译资源 `res/layout/activity_home.xml`。

## 现状

```text
Window / DecorView
└── view_content  LinearLayout (match_parent × match_parent, vertical)
    └── ConstraintLayout  (height=0dp, weight=1)
        ├── tab_content  FrameLayout (match_parent × match_parent)
        │   └── 当前页面内容 / Fragment 页面
        ├── cp_drag_view  CpHomeDragView (当前资源中 gone)
        └── rl_bottom  RelativeLayout (alignParentBottom, match_parent × wrap_content)
            ├── btnAudio  HomeAIRecordButton (通常 invisible)
            ├── tl_nav  CpHomeBottomTabLayout  ← 原生 e江南 底栏
            └── tab_line_nontransparent  View (约 0.5dp 顶部分隔线)
```

## 拟改造（只作用于 `com.wisedu.cpdaily.jiangnan`）

```text
Window / DecorView
└── view_content  LinearLayout
    └── ConstraintLayout
        ├── tab_content  FrameLayout  ← 页面内容扩展到手势区域后方
        │   └── 当前页面内容 / Fragment 页面（不删除）
        ├── cp_drag_view  CpHomeDragView（不改变）
        └── LiquidGlassHostLayout（替代 rl_bottom 的视觉承载）
            ├── LiquidGlassPanel（实时采样 tab_content，液态玻璃）
            ├── DropletPanel（选中项/拖拽反馈）
            └── tl_nav  CpHomeBottomTabLayout（保留点击、红点和页面切换）
                └── 原有 tab 子项（仅清除不透明背景与分隔线）

        删除/隐藏范围（需要你确认）
        ├── `rl_bottom` 原始全宽底栏容器的背景/占位区域
        └── `tab_line_nontransparent` 0.5dp 全宽分隔线

        保留范围
        ├── `tl_nav` 及其子项的交互和业务回调
        ├── `btnAudio`（仅在宿主实际显示时保留）
        └── `tab_content` 及所有页面内容
```

## 空白与系统导航适配

删除原始 `rl_bottom` 的全宽占位后，`tab_content` 会被重新拉伸到父容器底部；
液态玻璃浮层自身使用底部手势 inset + 12dp 视觉间距，系统导航栏设为透明并关闭
对比度强制，避免出现“内容区结束后的一条空白带”。系统小白条仍由系统绘制，模块
只调整其背景/对比度，不拦截系统导航手势。

## 需要你确认的一点

请确认是否按上图的“删除/隐藏范围”执行：

1. 移除 `rl_bottom` 的原全宽视觉承载及 `tab_line_nontransparent` 分隔线；
2. 保留 `tl_nav` 子项作为交互层，嵌入液态玻璃容器；
3. 将 `tab_content` 扩展到释放出的底部区域，并按系统 gesture inset 锚定浮层。

在你确认前，我不会提交这部分布局删除操作。
