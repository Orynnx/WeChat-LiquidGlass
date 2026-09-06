package io.github.liuran001.mmliquidglass;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * The visual navigation row installed inside e江南's existing tl_nav.
 *
 * <p>The five real e江南 roots are deliberately not replaced. They remain in
 * the native hierarchy and keep their listeners; this row only supplies the
 * pixels and forwards taps to those roots through {@link Proxy}.</p>
 */
final class EjiangnanGlassNavigation extends LinearLayout {

    interface Proxy {
        boolean click(int index);
    }

    private static final String[] LABELS = {"首页", "应用", "消息", "新闻", "我的"};
    private static final int ACCENT = Color.rgb(22, 126, 240);
    private static final int INACTIVE = Color.rgb(95, 101, 112);
    private final TabButton[] mButtons = new TabButton[LABELS.length];
    private final View[] mNativeRoots;
    private Proxy mProxy;
    private int mSelected = 0;

    EjiangnanGlassNavigation(Context context, View[] nativeRoots) {
        super(context);
        mNativeRoots = nativeRoots == null ? new View[0] : nativeRoots;
        setOrientation(HORIZONTAL);
        setGravity(android.view.Gravity.CENTER_VERTICAL);
        setClipChildren(false);
        setClipToPadding(false);
        setWillNotDraw(true);
        for (int i = 0; i < LABELS.length; i++) {
            final int index = i;
            TabButton button = new TabButton(context, i, LABELS[i]);
            button.setOnClickListener(v -> {
                boolean forwarded = mProxy != null && mProxy.click(index);
                if (forwarded) {
                    // The app may update isSelected a frame later. The visual
                    // state is updated immediately, then reconciled by the
                    // pre-draw watcher from the real native roots.
                    setSelectedIndex(index);
                    postDelayed(this::syncFromNative, 90L);
                    LiquidGlassModule.log(android.util.Log.INFO,
                            "ejiangnan glass tab proxy click index=" + index);
                } else {
                    LiquidGlassModule.log(android.util.Log.WARN,
                            "ejiangnan glass tab proxy failed index=" + index);
                }
            });
            addView(button, new LinearLayout.LayoutParams(0,
                    ViewGroup.LayoutParams.MATCH_PARENT, 1f));
            mButtons[i] = button;
        }
        setSelectedIndex(0);
    }

    void setProxy(Proxy proxy) {
        mProxy = proxy;
    }

    int selectedIndex() {
        return mSelected;
    }

    void setSelectedIndex(int index) {
        if (index < 0 || index >= mButtons.length) {
            return;
        }
        mSelected = index;
        for (int i = 0; i < mButtons.length; i++) {
            mButtons[i].setSelectedState(i == index);
        }
        invalidate();
    }

    /** Uses native selection/activation flags as the source of truth. */
    boolean syncFromNative() {
        int selected = -1;
        for (int i = 0; i < mNativeRoots.length; i++) {
            View root = mNativeRoots[i];
            if (root != null && (root.isSelected() || root.isActivated())) {
                selected = i;
                break;
            }
        }
        if (selected < 0) {
            return false;
        }
        if (selected != mSelected) {
            setSelectedIndex(selected);
            LiquidGlassModule.log(android.util.Log.INFO,
                    "ejiangnan glass selection confirmed index=" + selected);
        }
        return true;
    }

    private final class TabButton extends LinearLayout {
        private final MaterialIconView mIcon;
        private final TextView mTitle;
        private final int mIndex;

        TabButton(Context context, int index, String title) {
            super(context);
            mIndex = index;
            setOrientation(VERTICAL);
            setGravity(android.view.Gravity.CENTER);
            setClickable(true);
            setFocusable(true);
            setPadding(0, dp(4), 0, dp(4));
            mIcon = new MaterialIconView(context, index);
            addView(mIcon, new LinearLayout.LayoutParams(dp(27), dp(27)));
            mTitle = new TextView(context);
            mTitle.setText(title);
            mTitle.setTextSize(12f);
            mTitle.setTypeface(Typeface.create("sans", Typeface.NORMAL));
            mTitle.setGravity(android.view.Gravity.CENTER);
            mTitle.setIncludeFontPadding(false);
            mTitle.setSingleLine(true);
            mTitle.setTextColor(INACTIVE);
            addView(mTitle, new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT, dp(20)));
        }

        void setSelectedState(boolean selected) {
            // Keep the platform selection bit in sync with the visual state.
            // DropletPanel/TabBarBridge use View.isSelected() to locate the
            // tab whose contents are refracted; leaving it false makes the
            // droplet remain bound to the first tab after a page switch.
            setSelected(selected);
            mIcon.setSelectedState(selected);
            mTitle.setTextColor(selected ? ACCENT : INACTIVE);
            mTitle.setTypeface(Typeface.create("sans",
                    selected ? Typeface.BOLD : Typeface.NORMAL));
            setContentDescription(LABELS[mIndex]);
        }

        @Override
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
            super.onSizeChanged(w, h, oldw, oldh);
            // The endpoint droplet is inset by 1%; move only the endpoint
            // glyphs inward by the same screen-relative amount.
            float shift = getResources().getDisplayMetrics().widthPixels * 0.01f;
            float endpointShift = mIndex == 0 ? shift
                    : (mIndex == LABELS.length - 1 ? -shift : 0f);
            mIcon.setTranslationX(endpointShift);
            mTitle.setTranslationX(endpointShift);
        }
    }

    /** Official Google Material Symbols Rounded vector asset. */
    private static final class MaterialIconView extends ImageView {
        private final int mKind;
        private final int mDrawableId;

        MaterialIconView(Context context, int kind) {
            super(context);
            mKind = kind;
            String[] names = {"ms_home", "ms_apps", "ms_chat", "ms_article", "ms_person"};
            Context module = context;
            int id = 0;
            try {
                module = context.createPackageContext("org.orynnx.liquidejnu",
                        Context.CONTEXT_IGNORE_SECURITY);
                id = module.getResources().getIdentifier(names[kind], "drawable",
                        "org.orynnx.liquidejnu");
                if (id != 0) {
                    setImageDrawable(module.getDrawable(id));
                }
            } catch (Throwable t) {
                LiquidGlassModule.logErr("material symbol load failed", t);
            }
            mDrawableId = id;
            setScaleType(ScaleType.CENTER_INSIDE);
            setColorFilter(INACTIVE, android.graphics.PorterDuff.Mode.SRC_IN);
        }

        void setSelectedState(boolean selected) {
            setColorFilter(selected ? ACCENT : INACTIVE,
                    android.graphics.PorterDuff.Mode.SRC_IN);
        }

    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
