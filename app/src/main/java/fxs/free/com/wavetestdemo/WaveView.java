package fxs.free.com.wavetestdemo;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Xfermode;
import android.os.Handler;
import android.os.Message;
import android.view.View;

public class WaveView extends View {

    /***画图相关***/
    private Path topPath;
    private Path bottomPath;
    private Path centerPath;

    private Paint mPaint;

    private Xfermode mXfermode;
    private RectF rectF;

    /***函数相关***/

    private int mHeight;
    private int mWidth;
    private int centerHeight;
    private long startTime;
    /**
     * 取样点间隔
     */
    private final int SAMPLINGPOINT;

    private float[] Y;
    private float[] transitionAfterX;
    private float[] transitionBeforeX;
    private float interval;
    private float shake;

    /*** 动画相关***/
    private MyHandler myHandler;

    public WaveView(Context context) {
        super(context);
        startTime = System.currentTimeMillis();
        SAMPLINGPOINT = 128;
        Y = new float[129];
        transitionAfterX = new float[129];
        transitionBeforeX = new float[129];
        mXfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);
        rectF = new RectF();
        myHandler = new MyHandler();
        init();
    }

    private void init() {
        mPaint = new Paint();
        topPath = new Path();
        bottomPath = new Path();
        centerPath = new Path();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        float offset = ((System.currentTimeMillis() - startTime) / 500f);
        mHeight = canvas.getHeight();
        mWidth = canvas.getWidth();
        centerHeight = mHeight >> 1;
        shake = mWidth >> 3;
        interval = mWidth / SAMPLINGPOINT;
        topPath.rewind();
        bottomPath.rewind();
        centerPath.rewind();
        topPath.moveTo(0, mHeight / 2);
        bottomPath.moveTo(0, mHeight / 2);
        centerPath.moveTo(0, mHeight / 2);
        float cur;
        for (int i = 0, length = transitionAfterX.length; i < length; i++) {
            transitionBeforeX[i] = interval * i;
            transitionAfterX[i] = transitionBeforeX[i] / mWidth * 4 - 2;
            Y[i] = calculateY(transitionAfterX[i], offset);
            Y[i] = calculateY(transitionAfterX[i], offset);
            cur = i < SAMPLINGPOINT ? Y[i] * shake : 0;
            // 坐标向下平移centerHeight。  topPath，bottomPath Y坐标对称（一正一负）
            topPath.lineTo(transitionBeforeX[i], cur + centerHeight);
            bottomPath.lineTo(transitionBeforeX[i], -cur + centerHeight);
            centerPath.lineTo(transitionBeforeX[i], cur / 5f + centerHeight);
        }
        //连接到终点
        topPath.lineTo(mWidth, centerHeight);
        bottomPath.lineTo(mWidth, centerHeight);
        centerPath.lineTo(mWidth, centerHeight);
        // 保存图层
        int saveCount = canvas.saveLayer(0, 0, mWidth, mHeight, null, Canvas.ALL_SAVE_FLAG);
        //填充上下两条
        mPaint.setColor(Color.WHITE);
        mPaint.setStrokeWidth(3);
        mPaint.setStyle(Paint.Style.FILL);
        canvas.drawPath(topPath, mPaint);
        canvas.drawPath(bottomPath, mPaint);

        //绘制渐变矩形
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.BLUE);
        mPaint.setXfermode(mXfermode);
        mPaint.setShader(new LinearGradient(0, centerHeight - shake, mWidth, centerHeight + shake, Color.BLUE, Color.GREEN, Shader.TileMode.CLAMP));
        rectF.set(0, centerHeight - shake, mWidth, centerHeight + shake);
        canvas.drawRect(rectF, mPaint);
        //清理
        mPaint.setShader(null);
        mPaint.setXfermode(null);
        //
        canvas.restoreToCount(saveCount);

        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(3);
        mPaint.setColor(Color.BLUE);
        canvas.drawPath(topPath, mPaint);
        canvas.drawPath(bottomPath, mPaint);
        canvas.drawPath(centerPath, mPaint);
        myHandler.sendEmptyMessageDelayed(0, 50);

    }

    private class MyHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            invalidate();
            myHandler.sendEmptyMessageDelayed(0, 50);
            super.handleMessage(msg);

        }
    }

    private float calculateY(float x, float offset) {
        offset %= 2;
        float sin = (float) Math.sin(0.75 * Math.PI * x + offset * Math.PI);
        float coefficient = (float) (4 / (4 + Math.pow(x, 4)));
        return sin * coefficient;
    }
}
