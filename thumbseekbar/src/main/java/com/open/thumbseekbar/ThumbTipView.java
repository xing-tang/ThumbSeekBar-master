package com.open.thumbseekbar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.content.ContextCompat;

/**
 * ThumbTipView
 *
 * @Description:
 * @Author: xing.tang
 */
public class ThumbTipView extends View implements IThumbTipView {

    private final static String TAG = "BaseThumbTipView";
    private Context mContext = null;
    private Paint mBubblePaint;
    private Path mBubblePath;
    private RectF mBubbleRectF;
    private Rect mRect;
    private String mProgressText = "";
    private int mBubbleRadius;

    ThumbTipView(Context context) {
        this(context, null);
    }

    ThumbTipView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    ThumbTipView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // 设置可点击
        setClickable(true);
        mContext = context;
        init(attrs, defStyleAttr);
    }

    protected void init(AttributeSet attrs, int defStyleAttr) {
        mBubblePaint = new Paint();
        mBubblePaint.setAntiAlias(true);
        mBubblePaint.setTextAlign(Paint.Align.CENTER);
        mBubblePath = new Path();
        mBubbleRectF = new RectF();
        mRect = new Rect();
        mBubbleRadius = ThumbUtils.dp2px(14); // default 14dp
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(3 * mBubbleRadius, 3 * mBubbleRadius);
        mBubbleRectF.set(getMeasuredWidth() / 2f - mBubbleRadius, 0,
                getMeasuredWidth() / 2f + mBubbleRadius, 2 * mBubbleRadius);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mBubblePath.reset();
        float x0 = getMeasuredWidth() / 2f;
        float y0 = getMeasuredHeight() - mBubbleRadius / 3f;
        mBubblePath.moveTo(x0, y0);
        float x1 = (float) (getMeasuredWidth() / 2f - Math.sqrt(3) / 2f * mBubbleRadius);
        float y1 = 3 / 2f * mBubbleRadius;
        mBubblePath.quadTo(
                x1 - ThumbUtils.dp2px(2), y1 - ThumbUtils.dp2px(2),
                x1, y1
        );
        mBubblePath.arcTo(mBubbleRectF, 150, 240);

        float x2 = (float) (getMeasuredWidth() / 2f + Math.sqrt(3) / 2f * mBubbleRadius);
        mBubblePath.quadTo(
                x2 + ThumbUtils.dp2px(2), y1 - ThumbUtils.dp2px(2),
                x0, y0
        );
        mBubblePath.close();
        mBubblePaint.setColor(ContextCompat.getColor(getContext(), R.color.white));
        canvas.drawPath(mBubblePath, mBubblePaint);
        mBubblePaint.setTextSize(ThumbUtils.sp2px(12));
        mBubblePaint.setColor(ContextCompat.getColor(getContext(), R.color.black));
        mBubblePaint.getTextBounds(mProgressText, 0, mProgressText.length(), mRect);
        Paint.FontMetrics fm = mBubblePaint.getFontMetrics();
        float baseline = mBubbleRadius + (fm.descent - fm.ascent) / 2f - fm.descent;
        canvas.drawText(mProgressText, getMeasuredWidth() / 2f, baseline, mBubblePaint);
    }

    @Override
    public void setProgressText(String progressText) {
        if (progressText != null && !mProgressText.equals(progressText)) {
            mProgressText = progressText;
            invalidate();
        }
    }
}
