# e江南底栏沉浸 · LiquideJNU

这是一个基于 libxposed API 102 的 LSPosed 模块，当前版本只作用于 e江南
（`com.wisedu.cpdaily.jiangnan`）。它不修改 e江南 APK 本体，也不接入液态玻璃
渲染；当前阶段只完成底部导航区域移除和 edge-to-edge 布局适配。

## 当前行为

- 作用域和运行时 allow-list 都只有 `com.wisedu.cpdaily.jiangnan`。
- 在 `com.wisorg.wisedu.home.ui.HomeActivity` 恢复后定位
  `com.wisorg.wisedu.widget.tablayout.CpHomeBottomTabLayout`。
- 将其所在的 `rl_bottom` 容器设为 `GONE`，同时隐藏
  `tab_line_nontransparent` 分隔线；不删除业务对象，便于 Activity 重建时恢复。
- 将 `tab_content` 的宽高固定为 `MATCH_PARENT`、清除底部 margin/padding，并在后续
  layout/pre-draw 中持续保持，避免原导航区域留下空白。
- 设置透明导航栏、关闭导航栏对比度强制、启用 `LAYOUT_HIDE_NAVIGATION`，并剥离
  root 收到的 navigation-bar inset，使系统手势小白条沉浸在页面内容上方。
- 安插 Logcat 探针：`probe:ejiangnan-module-injected`、
  `probe:ejiangnan-package-loaded`、`probe:ejiangnan-nav-hidden`。

本阶段有意不创建 `LiquidGlassHostLayout`、`LiquidGlassPanel` 或任何动画层。

## 布局依据

供应的 `base.apk`（e江南 3.0.7）中，主页面资源 `activity_home.xml` 的相关结构为：

```text
ConstraintLayout
├── tab_content (FrameLayout)
└── rl_bottom (RelativeLayout)
    ├── tl_nav (CpHomeBottomTabLayout)
    └── tab_line_nontransparent
```

完整方案图见 [docs/ejiangnan-layout-proposal.svg](docs/ejiangnan-layout-proposal.svg)。

## 构建

无需 Gradle / Android Studio：

```bash
./setup-tools.sh
./build.sh
```

产物：`LiquideJNU-vX.Y.Z.apk`。默认使用临时 debug key；正式发布时通过环境变量
传入 keystore：

```bash
KEYSTORE=/path/to/release.jks \
KEYSTORE_PASS='...' KEY_ALIAS='...' KEY_PASS='...' ./build.sh
```

## 验证与限制

构建脚本会运行 `javac`、`d8`、`aapt2`、`zipalign` 和 `apksigner verify`。设备验证需要
在线的 Android/HyperOS ADB 设备；若 `adb devices` 无设备，真实设备上的模块注入、页面
布局和手势返回必须标记为 `UNTESTED`，不能以静态构建结果代替硬件证明。

## License

本项目按 [MIT License](LICENSE) 分发。上游 WeChat-LiquidGlass 代码的原版权声明保留，
本 Fork 的 e江南适配代码也在相同许可下发布。
