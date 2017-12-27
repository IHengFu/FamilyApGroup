package wifiairscout.changhong.com.wifiairscout.ui.view;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;

import wifiairscout.changhong.com.wifiairscout.utils.UnitUtils;


/**
 * Created by fuheng on 2017/12/12.
 */

public class SignalView extends View {

    private Paint mBigTextPaint;
    private Paint mSmallTextPaint;
    private Paint mSolidPaint;
    private Paint mStrokePaint;

    private String mStringCenter;
    private String mMinValue;
    private String mMaxValue;


    public SignalView(Context context) {
        super(context);
        init(context);
    }

    public SignalView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public SignalView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }


    private void init(Context context) {
        mBigTextPaint = new Paint();
        mBigTextPaint.setAntiAlias(true);
        mBigTextPaint.setColor(Color.BLACK);
        mBigTextPaint.setTextSize(UnitUtils.dip2px(context, 45));


        mSmallTextPaint = new Paint(mBigTextPaint);
        mSmallTextPaint.setTextSize(UnitUtils.dip2px(context, 12));


        mSolidPaint = new Paint();
        mSolidPaint.setColor(Color.parseColor("#3307c800"));

        mStrokePaint = new Paint();
        mStrokePaint.setColor(Color.parseColor("#07c800"));
        mStrokePaint.setStyle(Paint.Style.STROKE);
        mStrokePaint.setStrokeWidth(2);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int x0 = getWidth() / 2;
        int y0 = getHeight() / 2;
        int maxRadius = Math.min(x0, y0);

        if (!TextUtils.isEmpty(mStringCenter)) {
            Rect rect = new Rect();
            mSmallTextPaint.getTextBounds(mStringCenter, 0, mStringCenter.length(), rect);
            canvas.drawText(mStringCenter,
                    x0 - rect.width() / 2,
                    y0 - rect.height() / 2,
                    mBigTextPaint);
        }
        if (!TextUtils.isEmpty(mMaxValue)) {
            Rect rect = new Rect();
            mSmallTextPaint.getTextBounds(mMaxValue, 0, mMaxValue.length(), rect);
            canvas.drawText(mMaxValue,
                    getWidth() - rect.width() - getPaddingRight(),
                    getHeight() - getPaddingBottom(),
                    mSmallTextPaint);
        }

        if (!TextUtils.isEmpty(mMinValue))
            canvas.drawText(mMinValue,
                    getPaddingLeft(),
                    getHeight() - getPaddingBottom(),
                    mSmallTextPaint);

        canvas.drawCircle(x0, y0, maxRadius * scaleSolid, mSolidPaint);

        canvas.drawCircle(x0, y0, maxRadius * scaleStroke - 2, mStrokePaint);
    }

    private AnimatorSet mAnimatorSetSolid;
    private AnimatorSet mAnimatorSetStroke;

    private float scaleSolid = .5f;
    private float scaleStroke = .5f;

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == View.VISIBLE) {
            startAnim();
        } else {
            stopAnim();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        startAnim();
    }

    private void stopAnim() {
        scaleSolid = .5f;
        if (mAnimatorSetSolid != null) {
            mAnimatorSetSolid.cancel();
            mAnimatorSetSolid = null;
        }

        if (mAnimatorSetStroke != null) {
            mAnimatorSetStroke.cancel();
            mAnimatorSetStroke = null;
        }
    }


    private final static int SCALE_TIME = 600;
    private final static int PAUSE_TIME1 = 300;

    @Override
    protected void onDetachedFromWindow() {
        stopAnim();
        super.onDetachedFromWindow();
    }

    private void startAnim() {
        stopAnim();
        mAnimatorSetSolid = new AnimatorSet();
        mAnimatorSetStroke = new AnimatorSet();


        ValueAnimator pauseAnim = ValueAnimator.ofFloat(0, 0);
        pauseAnim.setRepeatCount(Animation.INFINITE);
        pauseAnim.setRepeatMode(ValueAnimator.REVERSE);
        pauseAnim.setDuration(PAUSE_TIME1);

        ValueAnimator scaleUp = ValueAnimator.ofFloat(0, 100);
        scaleUp.setRepeatCount(Animation.INFINITE);
        scaleUp.setRepeatMode(ValueAnimator.REVERSE);
        scaleUp.setDuration(SCALE_TIME);
        scaleUp.setInterpolator(new DecelerateInterpolator());
        scaleUp.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float percent = (float) animation.getAnimatedValue();
                scaleSolid = .5f + .5f * percent / 100f;
                invalidate();
            }
        });

        mAnimatorSetSolid.playSequentially(scaleUp, pauseAnim);
        mAnimatorSetSolid.start();


        ValueAnimator pauseAnim2 = ValueAnimator.ofFloat(0, 0);
        pauseAnim2.setRepeatCount(Animation.INFINITE);
        pauseAnim2.setRepeatMode(ValueAnimator.REVERSE);
        pauseAnim2.setDuration(PAUSE_TIME1);

        ValueAnimator scaleUp2 = ValueAnimator.ofFloat(0, 100);
        scaleUp2.setRepeatCount(Animation.INFINITE);
        scaleUp2.setRepeatMode(ValueAnimator.REVERSE);
        scaleUp2.setDuration(SCALE_TIME);
        scaleUp2.setInterpolator(new DecelerateInterpolator());
        scaleUp2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float percent = (float) animation.getAnimatedValue();
                scaleStroke = .5f + .5f * percent / 100f;
                invalidate();
            }
        });

        mAnimatorSetStroke.setStartDelay(200);
        mAnimatorSetStroke.playSequentially(scaleUp2, pauseAnim2);
        mAnimatorSetStroke.start();
    }

    public void setDisplayString(String center, String minValue, String maxValue) {
        this.mStringCenter = center;
        this.mMinValue = minValue;
        this.mMaxValue = maxValue;
    }

    final int[] colors = new int[3];
    final float[] points = {0, .7f, .9f};

    public void setColor(int color) {

//        if (getWidth() > 0 && getHeight() > 0) {
//            colors[0] = color & 0xffffff | 0x22000000;
//            colors[1] = color & 0xffffff | 0x44000000;
//            colors[2] = color & 0xffffff | 0x66000000;
//
//            Shader shader = new RadialGradient(getWidth() / 2, getHeight() / 2, Math.max(getWidth(), getHeight()) / 2, colors, points, Shader.TileMode.MIRROR);
//            mSolidPaint.setShader(shader);
//        } else
        mSolidPaint.setColor((mSolidPaint.getColor() & 0xff000000) | (color & 0xffffff));
        mStrokePaint.setColor((mStrokePaint.getColor() & 0xff000000) | (color & 0xffffff));
    }
}
