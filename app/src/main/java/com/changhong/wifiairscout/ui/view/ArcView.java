package com.changhong.wifiairscout.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import com.changhong.wifiairscout.R;
import com.changhong.wifiairscout.utils.UnitUtils;

/**
 * Created by Hugo on 16/8/8.
 */
public class ArcView extends View {


    private Paint mBGPaint;
    private Paint mProgressPaint;
    private Paint mTextPaint;
    private Paint mSmallTextPaint;

    private RectF mRectF;

    private final static float ANGLE_SWEEP = 270;

    private float mCenter;
    private int mProgressWidth;

    private float mCurrentProgress = 0f;

    private String mTitle = "Title";
    private String mMinValue = "minValue";
    private String mMaxValue = "maxValue";
    private String mUnit = "unit";
    private String mCurValue = "curvalue";

    public void setProgress(int progress) {
        if (progress == mCurrentProgress) return;
        mCurrentProgress = Math.min(100, Math.max(0, mCurrentProgress));
//        mCurrentProgress = currentProgress;
        clearAnimation();
        startAnimation(new ArcProgressAnimation(progress));
    }

    public ArcView(Context context) {
        super(context);
        init(context);
    }

    public ArcView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ArcView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {

        mProgressWidth = UnitUtils.dip2px(context, 12);

        mBGPaint = new Paint();
        mBGPaint.setAntiAlias(true); //消除锯齿
        mBGPaint.setStyle(Paint.Style.STROKE); //绘制空心圆
        mBGPaint.setStrokeWidth(mProgressWidth >> 1);
        mBGPaint.setStrokeJoin(Paint.Join.ROUND);
        mBGPaint.setStrokeCap(Paint.Cap.ROUND); //设置圆角
        mBGPaint.setColor(Color.GRAY);


        mProgressPaint = new Paint();
        mProgressPaint.setAntiAlias(true); //消除锯齿
        mProgressPaint.setStyle(Paint.Style.STROKE); //绘制空心圆
        mProgressPaint.setStrokeWidth(mProgressWidth); //设置进度条宽度
        mProgressPaint.setStrokeJoin(Paint.Join.ROUND);
        mProgressPaint.setStrokeCap(Paint.Cap.ROUND); //设置圆角

        mTextPaint = new Paint();
        mTextPaint.setColor(context.getResources().getColor(R.color.textColorPrimary));
        mTextPaint.setTextSize(UnitUtils.dip2px(context, 45));

        mSmallTextPaint = new Paint(mTextPaint);
        mSmallTextPaint.setTextSize(UnitUtils.dip2px(context, 16));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mCurrentProgress = Math.min(100, Math.max(0, mCurrentProgress));
        canvas.drawArc(mRectF, 135, ANGLE_SWEEP, false, mBGPaint);

        if (mCurrentProgress != 0) {
            canvas.save();
            canvas.rotate(90, mRectF.centerX(), mRectF.centerY());
            canvas.drawArc(mRectF,
                    45, ANGLE_SWEEP * mCurrentProgress / 100f, false, mProgressPaint);
            canvas.restore();
        }
        onDrawText(canvas);
    }

    //draw TITLE
    private void onDrawText(Canvas canvas) {
        Rect rect = new Rect();

        if (!TextUtils.isEmpty(mTitle)) {//标题不为空
            mSmallTextPaint.getTextBounds(mTitle, 0, mTitle.length(), rect);
            if (TextUtils.isEmpty(mCurValue)) {//当前值为空，不绘制当前值和单位
                canvas.drawText(mTitle, mRectF.centerX() - rect.centerX(), mRectF.centerY() - rect.centerY(), mSmallTextPaint);
            } else if (TextUtils.isEmpty(mUnit)) {//当前值不为空，单位为空
                canvas.drawText(mTitle, mRectF.centerX() - rect.centerX(), mRectF.centerY() - mTextPaint.getTextSize() / 2, mSmallTextPaint);
                mTextPaint.getTextBounds(mCurValue, 0, mCurValue.length(), rect);
                canvas.drawText(mCurValue, mRectF.centerX() - rect.centerX(), mRectF.centerY() + mSmallTextPaint.getTextSize() / 2, mTextPaint);
            } else {//当前值、单位都不为空
                canvas.drawText(mTitle, mRectF.centerX() - rect.centerX(), mRectF.centerY() - rect.height() - mTextPaint.getTextSize() / 2, mSmallTextPaint);
                mTextPaint.getTextBounds(mCurValue, 0, mCurValue.length(), rect);
                canvas.drawText(mCurValue, mRectF.centerX() - rect.centerX(), mRectF.centerY() + mSmallTextPaint.getTextSize() / 2, mTextPaint);
                mSmallTextPaint.getTextBounds(mUnit, 0, mUnit.length(), rect);
                canvas.drawText(mUnit, mRectF.centerX() - rect.centerX(), mRectF.centerY() + rect.height() + mTextPaint.getTextSize() / 2, mSmallTextPaint);
            }
        } else if (TextUtils.isEmpty(mUnit)) {//单位为空，标题为空
            if (!TextUtils.isEmpty(mCurValue)) {
                mTextPaint.getTextBounds(mCurValue, 0, mCurValue.length(), rect);
                canvas.drawText(mCurValue, mRectF.centerX() - rect.centerX(), mRectF.centerY() - rect.centerY(), mTextPaint);
            }
        } else {//标题为空 单位不为空
            if (!TextUtils.isEmpty(mCurValue)) {
                mTextPaint.getTextBounds(mCurValue, 0, mCurValue.length(), rect);
                canvas.drawText(mCurValue, mRectF.centerX() - rect.centerX(), mRectF.centerY() - rect.centerY() + mSmallTextPaint.getTextSize() / 2, mTextPaint);
                mSmallTextPaint.getTextBounds(mUnit, 0, mUnit.length(), rect);
                canvas.drawText(mUnit, mRectF.centerX() - rect.centerX(), mRectF.centerY() - rect.height() + mTextPaint.getTextSize() / 2, mSmallTextPaint);
            }
        }


        if (!TextUtils.isEmpty(mMinValue)) {
            canvas.drawText(mMinValue, getPaddingLeft(), getHeight() - getPaddingBottom(), mSmallTextPaint);
        }
        if (!TextUtils.isEmpty(mMaxValue)) {
            mSmallTextPaint.getTextBounds(mMaxValue, 0, mMaxValue.length(), rect);
            canvas.drawText(mMaxValue, getWidth() - getPaddingRight() - rect.width(), getHeight() - getPaddingBottom(), mSmallTextPaint);
        }
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        int widthtMode = MeasureSpec.getMode(widthMeasureSpec);
        int wdithSize = MeasureSpec.getSize(widthMeasureSpec);


        setMeasuredDimension(wdithSize, heightSize);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        float width = right - left - getPaddingBottom() - getPaddingLeft();
        float height = bottom - top - getPaddingBottom() - getPaddingTop() - mSmallTextPaint.getTextSize();

        float tL = Math.min(width, height) - mProgressWidth;
        mRectF = new RectF(getPaddingLeft() + (width - tL) / 2,
                getPaddingTop() + (height - tL) / 2,
                right - left - getPaddingRight() - (width - tL) / 2,
                bottom - top - getPaddingBottom() - mSmallTextPaint.getTextSize() - (height - tL) / 2);
        mCenter = mRectF.centerX();
        float[] positions = {0, .5f, 1f};
        int[] colors = {Color.RED, Color.YELLOW, Color.RED
        };
        Shader progressShader = new SweepGradient(mRectF.centerX(), mRectF.centerY(), colors, positions);
        mProgressPaint.setShader(progressShader);
        super.onLayout(changed, left, top, right, bottom);
    }

    public class ArcProgressAnimation extends Animation {

        private float oldProgress;
        private float newProgress;

        public ArcProgressAnimation(float newProgress) {
            this.oldProgress = mCurrentProgress;
            this.newProgress = newProgress;
//            setDuration((long) (Math.abs(newProgress - oldProgress) * 20));
            setDuration(1000);
            setInterpolator(new AccelerateDecelerateInterpolator());
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation transformation) {
            mCurrentProgress = oldProgress + ((newProgress - oldProgress) * interpolatedTime);
            invalidate();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        clearAnimation();
    }

    public void setDisplayString(String title, String currentValue, String minValue, String maxValue, String unit) {
        this.mTitle = title;
        this.mCurValue = currentValue;
        this.mMinValue = minValue;
        this.mMaxValue = maxValue;
        this.mUnit = unit;
    }
}
