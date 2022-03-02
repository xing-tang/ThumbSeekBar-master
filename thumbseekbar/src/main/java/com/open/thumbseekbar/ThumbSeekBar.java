package com.open.thumbseekbar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.appcompat.widget.AppCompatSeekBar;
import androidx.core.content.ContextCompat;

/**
 * ThumbSeekBar
 *
 * @Description:
 * @Author: xing.tang
 */
public class ThumbSeekBar extends AppCompatSeekBar {

    // WindowManager
    private WindowManager windowManager = null;
    // WindowManager对应的LayoutParams
    private WindowManager.LayoutParams layoutParams = null;
    // 提示框对应的View
    private IThumbTipView thumbTipView = null;
    // 提示框对应的View的状态
    @ThumbTipState
    private int thumbTipState = ThumbTipState.STATE_DEFAULT;
    // 拇指画笔
    private Paint thumbPaint = null;
    // 当前SeekBar是否按下状态【true:按下，false:未按下】
    private boolean isPressed = false;
    // 拇指的宽度
    private int thumbWidth = 0;
    // 当前拇指的半径
    private float curThumRadius = 0;
    // 拇指对应的动画
    private ValueAnimator thumbValueAnimator = null;
    // 矫正的最大进度值
    private final int correctedMax = 1000;
    // 矫正的最大进度值被除数
    private int correctedDividend = 10;
    // 矫正偏移值，单位为：dp
    private final int correctedOffset = ThumbUtils.dp2px(6);
    // 默认最小进度值，thumbMin < thumbMax, 默认：0
    private final int defalutThumbMin = 0;
    // 最小进度值，thumbMin < thumbMax, 默认：0
    private int thumbMin = defalutThumbMin;
    // 默认最大进度值，thumbMin < thumbMax, 默认：100
    private final int defalutThumbMax = 500;
    // 最大进度值，thumbMin < thumbMax, 默认：100
    private int thumbMax = defalutThumbMax;
    // 默认当前进度值，默认：等于最小进度值
    private final int defaultThumbProgress = thumbMin;
    // 当前进度值，默认：等于最小进度值
    private int thumbProgress = defaultThumbProgress;
    // 默认进度条模式，默认：R.drawable.layer_seekbar
    private final Drawable defaultThumbProgressDrawable = ContextCompat.getDrawable(getContext(), R.drawable.layer_default_seekbar);
    // 进度条模式
    private Drawable thumbProgressDrawable = defaultThumbProgressDrawable;
    // 默认最小进度条高度，默认为：2dp
    private final int defaultThumbProgressMinHeight = ThumbUtils.dp2px(2);
    // 最小进度条高度，单位为：dp
    private int thumbProgressMinHeight = defaultThumbProgressMinHeight;
    // 默认最大进度条高度，默认为：2dp
    private final int defaultThumbProgressMaxHeight = ThumbUtils.dp2px(2);
    // 最大进度条高度，单位为：dp
    private int thumbProgressMaxHeight = defaultThumbProgressMaxHeight;
    // 默认拇指的颜色，默认：android.R.color.white
    private final int defaultThumbColor = ContextCompat.getColor(getContext(), android.R.color.white);
    // 拇指的颜色
    private int thumbColor = defaultThumbColor;
    // 默认非拖动时拇指的半径，默认：12dp
    private final int defaultThumbRadius = ThumbUtils.dp2px(12);
    // 非拖动时拇指的半径，单位为：dp
    private int thumbRadius = defaultThumbRadius;
    // 拖动时拇指的半径缩放比例，默认为：2.0f倍
    private final float defaultThumbRadiusZoomOnDragging = 2.0f;
    // 拖动时拇指的半径缩放比例，取值范围为：1.0f~2.0f倍
    private float thumbRadiusZoomOnDragging = defaultThumbRadiusZoomOnDragging;
    // 默认是否显示提示框
    private final boolean isDefaultThumbShowTip = false;
    // 是否显示提示框【true:显示，false:不显示】，默认为：false
    private boolean isThumbShowTip = isDefaultThumbShowTip;
    // 默认是否一直保持显示提示框
    private final boolean defaultThumbShowTipAlways = false;
    // 是否一直保持显示提示框【true:显示，false:不显示】，默认为：false
    private boolean isThumbShowTipAlways = defaultThumbShowTipAlways;
    // 默认拖动后拇指缩小的动画时长，默认：200毫秒
    private final int defaultThumbAnimDuration = 200;
    // 拖动后拇指缩小的动画时长，单位为：毫秒
    private int thumbAnimDuration = defaultThumbAnimDuration;
    // 默认SeekBar是否拦截点击切换进度，默认为：true
    private final boolean defaultThumbInterceptClick = true;
    // SeekBar是否拦截点击切换进度【true:拦截，false:不拦截】
    private boolean isThumbInterceptClick = defaultThumbInterceptClick;

    public ThumbSeekBar(Context context) {
        this(context, null);
    }

    public ThumbSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.seekBarStyle);
    }

    public ThumbSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private Bitmap drawableToBitmap(Drawable drawable) {
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                : Bitmap.Config.RGB_565;
        Bitmap bitmap = Bitmap.createBitmap(width, height, config);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);
        return bitmap;
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ThumbSeekBar, defStyleAttr, 0);
        thumbMin = typedArray.getInteger(R.styleable.ThumbSeekBar_thumb_min, defalutThumbMin);
        thumbMax = typedArray.getInteger(R.styleable.ThumbSeekBar_thumb_max, defalutThumbMax);
        thumbProgress = typedArray.getInteger(R.styleable.ThumbSeekBar_thumb_progress, defaultThumbProgress);
        if (thumbProgress > thumbMax) thumbProgress = thumbMax;
        if (thumbMax > 10) {
            correctedDividend = 10;
            thumbProgress = (thumbProgress / thumbMax) * correctedMax;
            thumbMax = correctedMax;
        } else {
            correctedDividend = 1;
        }
        thumbProgressDrawable = typedArray.getDrawable(R.styleable.ThumbSeekBar_thumb_progress_drawable);
        if (thumbProgressDrawable == null) thumbProgressDrawable = defaultThumbProgressDrawable;
        thumbProgressMinHeight = typedArray.getDimensionPixelSize(R.styleable.ThumbSeekBar_thumb_progress_min_height, defaultThumbProgressMinHeight);
        thumbProgressMaxHeight = typedArray.getDimensionPixelSize(R.styleable.ThumbSeekBar_thumb_progress_max_height, defaultThumbProgressMaxHeight);
        thumbColor = typedArray.getColor(R.styleable.ThumbSeekBar_thumb_color, defaultThumbColor);
        thumbRadius = typedArray.getDimensionPixelSize(R.styleable.ThumbSeekBar_thumb_radius, defaultThumbRadius);
        if (thumbRadius < ThumbUtils.dp2px(8) || thumbRadius > ThumbUtils.dp2px(16)) {
            thumbRadius = defaultThumbRadius;
        }
        thumbRadiusZoomOnDragging = typedArray.getFloat(R.styleable.ThumbSeekBar_thumb_radius_zoom_on_dragging, defaultThumbRadiusZoomOnDragging);
        if (thumbRadiusZoomOnDragging < 1.0f || thumbRadiusZoomOnDragging > 2.0f) {
            thumbRadiusZoomOnDragging = defaultThumbRadiusZoomOnDragging;
        }
        thumbAnimDuration = typedArray.getInteger(R.styleable.ThumbSeekBar_thumb_anim_duration, -1);
        thumbAnimDuration = thumbAnimDuration < 0 ? defaultThumbAnimDuration : thumbAnimDuration;
        isThumbShowTip = typedArray.getBoolean(R.styleable.ThumbSeekBar_thumb_show_tip, isDefaultThumbShowTip);
        isThumbShowTipAlways = typedArray.getBoolean(R.styleable.ThumbSeekBar_thumb_show_tip_always, defaultThumbShowTipAlways);
        isThumbInterceptClick = typedArray.getBoolean(R.styleable.ThumbSeekBar_thumb_intercept_click, defaultThumbInterceptClick);
        typedArray.recycle();
        // ---------------- 开始设置属性参数 ---------------
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) setMin(thumbMin);
        setMax(thumbMax);
        setProgress(thumbProgress);
        // 画拇指
        // 第一种实现方式：
        // Drawable thumbDrawable = ContextCompat.getDrawable(getContext(), R.drawable.shape_thumb);
        // thumbDrawable = zoomDrawable(thumbDrawable, ThumbUtils.dp2px(10), ThumbUtils.dp2px(10));
        // 第二种实现方式：
        GradientDrawable thumbDrawable = new GradientDrawable();
        thumbDrawable.setShape(GradientDrawable.OVAL);
        thumbDrawable.setColor(thumbColor);
        thumbDrawable.setSize(thumbRadius, thumbRadius);
        setThumb(thumbDrawable);
        thumbPaint = new Paint();
        thumbPaint.setColor(thumbColor);
        thumbWidth = getThumb().getIntrinsicWidth();
        // 画进度
        setBackground(null);
        setProgressDrawable(thumbProgressDrawable);
        setMinHeight(thumbProgressMinHeight);
        setMaxHeight(thumbProgressMaxHeight);
        // 画提示框视图
        if (isThumbShowTip) {
            windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            thumbTipView = new ThumbTipView(context);
            thumbTipView.setProgressText(String.valueOf(getProgress() / correctedDividend));
            layoutParams = new WindowManager.LayoutParams();
            layoutParams.gravity = Gravity.START | Gravity.TOP;
            layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT;
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;
            layoutParams.format = PixelFormat.TRANSLUCENT;
            layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                    | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
            // MIUI禁止了开发者使用TYPE_TOAST，Android 7.1.1 对TYPE_TOAST的使用更严格
            if (ThumbUtils.isMiUi() || Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
                layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION;
            } else {
                layoutParams.type = WindowManager.LayoutParams.TYPE_TOAST;
            }
        }
    }

    @Override
    protected synchronized void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (thumbTipView instanceof View) {
            View tempThumbTipView = (View) thumbTipView;
            tempThumbTipView.measure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isPressed) {
            // 系统SeekBar自带padding为16dp，所以在计算半径时候要将其考虑进去
            float cx = getThumbCenterX() + ThumbUtils.dp2px(16) - thumbWidth / 2;
            float cy = getThumbCenterY();
            float radius = curThumRadius;
            canvas.drawCircle(cx, cy, radius, thumbPaint);

            Log.d("TAG", "onDraw: "
                    + "cx=" + cx
                    + ",cy=" + cy
                    + ",radius=" + radius);

            Log.d("TAG", "onDraw: "
                    + "rect.centerX()=" + getThumbCenterX()
                    + ",rect.centerY()=" + getThumbCenterY()
                    + ",rect.width()=" + thumbWidth
                    + ",radius=" + radius);
        }
        if (isThumbShowTipAlways && thumbTipState == ThumbTipState.STATE_DEFAULT) {
            showThumbTip();
        }
    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                curThumRadius = (thumbWidth / 2) * thumbRadiusZoomOnDragging;
                isPressed = true;
                if (isThumbInterceptClick) return interceptAction(x, y);
                showThumbTip();
                break;
            case MotionEvent.ACTION_MOVE:
                curThumRadius = (thumbWidth / 2) * thumbRadiusZoomOnDragging;
                isPressed = true;
                moveThumbTip();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                thumbValueAnimator = ValueAnimator.ofFloat((thumbWidth / 2) * thumbRadiusZoomOnDragging, thumbWidth / 2);
                thumbValueAnimator.addUpdateListener(animation -> {
                    curThumRadius = (float) animation.getAnimatedValue();
                    if (curThumRadius == thumbWidth / 2) {
                        isPressed = false;
                        hideThumbTip();
                    }
                    invalidate();
                });
                thumbValueAnimator.setDuration(thumbAnimDuration);
                thumbValueAnimator.start();
                break;
            default:
                break;
        }
        return super.onTouchEvent(event);
    }

    /**
     * 显示拇指提示视图
     * 原理是利用WindowManager动态添加一个与Toast相同类型的BubbleView，消失时再移除
     * <p>
     * Showing the Tip View depends the way that the WindowManager adds a Toast type view to the Window.
     */
    private void showThumbTip() {
        try {
            if (!isThumbShowTip) return;
            if (thumbTipView instanceof View) {
                View tempThumbTipView = (View) thumbTipView;
                if (tempThumbTipView.getParent() != null || layoutParams == null
                        || windowManager == null) return;
                thumbTipState = ThumbTipState.STATE_SHOW;
                layoutParams.x = (int) (getThumbCenterX() + thumbWidth / 2);
                layoutParams.y = (int) getThumbCenterY();
                tempThumbTipView.setAlpha(0);
                tempThumbTipView.setVisibility(VISIBLE);
                tempThumbTipView.animate().alpha(1f).setDuration(thumbAnimDuration)
                        .setListener(new AnimatorListenerAdapter() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                                windowManager.addView(tempThumbTipView, layoutParams);
                            }
                        }).start();
                thumbTipView.setProgressText(String.valueOf(getProgress() / correctedDividend));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 移动拇指提示视图
     * <p>
     * The WindowManager update the x coordinate and y coordinate of the Tip View from the Window.
     */
    private void moveThumbTip() {
        try {
            if (!isThumbShowTip) return;
            if (thumbTipView instanceof View) {
                View tempThumbTipView = (View) thumbTipView;
                if (layoutParams == null || windowManager == null) return;
                thumbTipState = ThumbTipState.STATE_MOVE;
                layoutParams.x = (int) (getThumbCenterX() + thumbWidth / 2);
                layoutParams.y = (int) getThumbCenterY();
                windowManager.updateViewLayout(tempThumbTipView, layoutParams);
                thumbTipView.setProgressText(String.valueOf(getProgress() / correctedDividend));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 隐藏拇指提示视图
     * <p>
     * The WindowManager removes the Tip View from the Window.
     */
    private void hideThumbTip() {
        try {
            if (!isThumbShowTip) return;
            if (isThumbShowTipAlways) return;
            if (thumbTipView instanceof View) {
                View tempThumbTipView = (View) thumbTipView;
                if (windowManager == null) return;
                thumbTipState = ThumbTipState.STATE_HIDE;
                tempThumbTipView.setVisibility(GONE); // 防闪烁
                if (tempThumbTipView.getParent() != null) {
                    windowManager.removeViewImmediate(tempThumbTipView);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 主要逻辑都在这里，主要思路就是判断触摸点位置是否在进度条的小圆点里
     * <p>
     * 这里分别向左右扩大了50像素，目的是为了优化拖拽体验，因为小圆点非常小，
     * 拖拽时可能没那么准确的触摸到小圆点区域，导致很难进行拖拽
     *
     * @param x float
     * @param y float
     * @return boolean
     */
    private boolean interceptAction(float x, float y) {
        Drawable drawable = getThumb();
        if (drawable == null) return true;
        Rect rect = drawable.getBounds();
        if (rect == null) return true;
        Rect tempRect = new Rect(rect.left - 50, rect.top, rect.right + 50, rect.bottom);
        return tempRect.contains((int) (x), (int) (y));
    }

    /**
     * 获取拇指的中心X轴坐标
     * <p>
     * Get the center X coordinate of the thumb.
     *
     * @return float
     */
    private float getThumbCenterX() {
        Drawable drawable = getThumb();
        Rect rect = drawable.getBounds();
        return rect.centerX();
    }

    /**
     * 获取拇指的中心Y轴坐标
     * <p>
     * Get the center Y coordinate of the thumb.
     *
     * @return float
     */
    private float getThumbCenterY() {
        Drawable drawable = getThumb();
        Rect rect = drawable.getBounds();
        return rect.centerY();
    }

    // -------------------------------- 待删除 ------------------------------------------------
    public static StateListDrawable genOnoffButtonSelector(int color, Context context) {
        StateListDrawable res = new StateListDrawable();
        res.addState(new int[]{android.R.attr.state_checked}, genCheckedDrawable(color));
        //res.addState(new int[]{}, context.getResources().getDrawable(R.drawable.atom_flight_toggle_btn_unchecked));
        return res;
    }

    public static Drawable genCheckedDrawable(int color) {
        GradientDrawable roundRect = new GradientDrawable();
        roundRect.setShape(GradientDrawable.RECTANGLE);
        roundRect.setSize(ThumbUtils.dp2px(52), ThumbUtils.dp2px(30));
        roundRect.setCornerRadius(ThumbUtils.dp2px(16));
        roundRect.setColor(color);
        GradientDrawable circle = new GradientDrawable();
        circle.setShape(GradientDrawable.OVAL);
        circle.setSize(ThumbUtils.dp2px(24), ThumbUtils.dp2px(24));
        circle.setColor(Color.parseColor("#ffffff"));
        InsetDrawable insetLayer2 = new InsetDrawable(circle, ThumbUtils.dp2px(23), 4, 3, 4);
        return new LayerDrawable(new Drawable[]{roundRect, insetLayer2});
    }

    private Drawable zoomDrawable(Drawable drawable, int w, int h) {
        int width = drawable.getIntrinsicWidth();
        int height = drawable.getIntrinsicHeight();
        Bitmap oldbmp = drawableToBitmap(drawable);
        Matrix matrix = new Matrix();
        float scaleWidth = ((float) w / width);
        float scaleHeight = ((float) h / height);
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap newbmp = Bitmap.createBitmap(oldbmp, 0, 0, width, height,
                matrix, true);
        return new BitmapDrawable(null, newbmp);
    }
}