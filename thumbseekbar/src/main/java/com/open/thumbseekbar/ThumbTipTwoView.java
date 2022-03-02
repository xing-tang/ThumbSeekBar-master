package com.open.thumbseekbar;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

/**
 * ThumbTipTwoView
 *
 * @Description:
 * @Author: xing.tang
 */
public class ThumbTipTwoView extends ConstraintLayout implements IThumbTipView {

    private final static String TAG = "ThumbTipTwoView";
    private Context mContext = null;
    private View mRootView = null;
    private TextView tvSeekbarTipText;
    private String mProgressText = "";

    public ThumbTipTwoView(Context context) {
        this(context, null);
    }

    public ThumbTipTwoView(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.seekBarStyle);
    }

    public ThumbTipTwoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        // 设置可点击
        setClickable(true);
        // 设置子类限制在父类控件大小之内
        setClipChildren(false);
        mContext = context;
        init(attrs, defStyleAttr);
    }

    private void init(AttributeSet attrs, int defStyleAttr) {
        mRootView = LayoutInflater.from(getContext()).inflate(R.layout.seekbar_beauty_tip, this);
        tvSeekbarTipText = (TextView) mRootView.findViewById(R.id.tv_seekbar_tip_text);
    }

    @Override
    public void setProgressText(String progressText) {
        if (tvSeekbarTipText != null && TextUtils.isEmpty(progressText)
                && !mProgressText.equals(progressText)) {
            mProgressText = progressText;
            tvSeekbarTipText.setText(progressText);
            invalidate();
        }
    }
}