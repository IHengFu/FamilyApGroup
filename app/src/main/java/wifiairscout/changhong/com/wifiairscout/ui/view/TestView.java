package wifiairscout.changhong.com.wifiairscout.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by fuheng on 2017/12/25.
 */

public class TestView extends View {
    private Path mPath;
    private Paint mPaint;

    public TestView(Context context) {
        super(context);
    }

    public TestView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public TestView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public TestView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onFinishInflate() {
        mPath = new Path();
        mPath.moveTo(50, 50);
//        mPath.lineTo(100, 100);
//        mPath.arcTo(0, 0, 100, 100, 45, 180, true);
        mPath.quadTo(50,100,100,100);
        mPath.quadTo(200,100,200,50);
//        mPath.quadTo(100,50,50,50);
        mPath.close();

        mPaint = new Paint();
        mPaint.setColor(Color.BLUE);
        super.onFinishInflate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
//        canvas.save();
//        canvas.clipPath(mPath);
        canvas.drawPath(mPath, mPaint);
//        canvas.restore();
    }
}
