package com.open.thumbseekbar;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * ThumbTipState
 *
 * @Description: 提示框View对应的状态
 * @Author: xing.tang
 */
@IntDef({ThumbTipState.STATE_DEFAULT, ThumbTipState.STATE_SHOW, ThumbTipState.STATE_MOVE, ThumbTipState.STATE_HIDE})
@Retention(RetentionPolicy.SOURCE)
public @interface ThumbTipState {
    /**
     * 默认状态
     */
    int STATE_DEFAULT = 0;
    /**
     * 显示状态
     */
    int STATE_SHOW = 1;
    /**
     * 移动状态
     */
    int STATE_MOVE = 2;
    /**
     * 隐藏状态
     */
    int STATE_HIDE = 3;
}
