package com.zhttty.mylibrary;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

/**
 * author     zhangHeng
 * date :     2017/4/5 13:40.
 * describe:
 */

public class FitView extends View {
    private Paint mPaint;
    private Path mPath;
    private RectF mRectF;
    private float progress;
    private int mStrokeWidth;
    private int mStrokeWidthFit;

    public FitView(Context context) {
        super(context);
        init();
    }

    public FitView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
        mStrokeWidth = DipPxUtil.dip2px(getContext(), 1.45f);
        mStrokeWidthFit = DipPxUtil.dip2px(getContext(), 2f);
        mPath = new Path();
        mRectF = new RectF();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mRectF.top = mStrokeWidth / 2;
        mRectF.left = mStrokeWidth / 2;
        mRectF.right = getMeasuredWidth() - mStrokeWidth / 2;
        mRectF.bottom = getMeasuredHeight() - mStrokeWidth / 2;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPaint.setStrokeWidth(mStrokeWidth);
        if (progress < 0.5f && progress > 0) {
            mPaint.setColor(getResources().getColor(R.color.colorPrimary));
            mPaint.setStyle(Paint.Style.FILL);
            progress *= 2;
            canvas.drawArc(mRectF, -15, 360 * progress, true, mPaint);
        } else if (progress > 0) {
            mPaint.setColor(getResources().getColor(R.color.colorPrimary));
            mPaint.setStyle(Paint.Style.FILL);
            canvas.drawArc(mRectF, -15, 360, true, mPaint);
            mPaint.setStrokeWidth(mStrokeWidthFit);
            mPath.reset();
            mPath.moveTo(getWidth() * 0.95f, getHeight() * 1.21f / 4);
            if (progress < 0.8f) {
                progress = progress - 0.5f;
                progress *= 10 / 3f;
                mPath.rLineTo(-getWidth() / 7 * 3.4f * progress, getHeight() / 4 * 1.85f * progress);
            } else {
                mPath.rLineTo(-getWidth() / 7 * 3.4f, getHeight() / 4 * 1.85f);
                progress = progress - 0.8f;
                progress *= 5;
                mPath.rLineTo(-getWidth() / 7 * 1.7f * progress, -getHeight() / 4 * 1.01f * progress);

            }
            mPaint.setColor(0xFFFFFFFF);
            mPaint.setStyle(Paint.Style.STROKE);
            canvas.drawPath(mPath, mPaint);
        }
    }

    public void startAnimation(int duration) {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
        valueAnimator.setDuration(duration);
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                progress = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        valueAnimator.start();
    }

    @Override
    public int getSolidColor() {
        return 0xFFFF0000;
    }
}
