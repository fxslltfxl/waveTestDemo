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
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class SurfaceViewL extends SurfaceView implements SurfaceHolder.Callback {
    private SurfaceHolder mSurfaceHolder;

    private ScheduledExecutorService executors;
    private static final Object mSurfaceLock = new Object();
    private RenderThread renderThread;
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
    private final int SAMPLINGPOINT = 128;

    private float[] Y;
    private float[] transitionAfterX;
    private float[] transitionBeforeX;
    private float interval;
    private float shake;

    private class RenderThread extends Thread {
        private static final long SLEEP = 16;

        private SurfaceHolder mSurfaceHolder;

        private boolean isDrawing = true;

        public RenderThread(SurfaceHolder mSurfaceHolder) {
            super("RenderThread");
            this.mSurfaceHolder = mSurfaceHolder;
        }

        @Override
        public void run() {
            while (true) {
                startTime = System.currentTimeMillis();
                synchronized (mSurfaceHolder) {
                    while (true) {
                        if (!isDrawing) return;
                        Canvas mCanvas = mSurfaceHolder.lockCanvas();
                        if (mCanvas != null) {
                            onRender(mCanvas, startTime);
                            mSurfaceHolder.unlockCanvasAndPost(mCanvas);
                        }
                        try {
                            Thread.sleep(SLEEP);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        public void setDrawing(boolean isDrawing) {
            this.isDrawing = isDrawing;
        }
    }

    public SurfaceViewL(Context context) {
        super(context);
        init();
    }

    private void init() {
        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);
        executors = new ScheduledThreadPoolExecutor(3);
        Y = new float[129];
        transitionAfterX = new float[129];
        transitionBeforeX = new float[129];

        mXfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);
        rectF = new RectF();
        mPaint = new Paint();
        topPath = new Path();
        bottomPath = new Path();
        centerPath = new Path();
//        setFocusable(true);
//        setFocusableInTouchMode(true);
//        //保持屏幕长亮
//        this.setKeepScreenOn(true);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        renderThread = new RenderThread(holder);
        renderThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        synchronized (mSurfaceLock) {
            renderThread.setDrawing(false);
        }
    }

    protected void onRender(Canvas mCanvas, long off) {
        mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        float offset = (System.currentTimeMillis() - off) / 500f;
        mHeight = mCanvas.getHeight();
        mWidth = mCanvas.getWidth();
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
        int saveCount = mCanvas.saveLayer(0, 0, mWidth, mHeight, null, Canvas.ALL_SAVE_FLAG);
        //填充上下两条
        mPaint.setColor(Color.WHITE);
        mPaint.setStrokeWidth(3);
        mPaint.setStyle(Paint.Style.FILL);
        mCanvas.drawPath(topPath, mPaint);
        mCanvas.drawPath(bottomPath, mPaint);

        //绘制渐变矩形
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.BLUE);
        mPaint.setXfermode(mXfermode);
        mPaint.setShader(new LinearGradient(0, centerHeight - shake, mWidth, centerHeight + shake, Color.BLUE, Color.GREEN, Shader.TileMode.CLAMP));
        rectF.set(0, centerHeight - shake, mWidth, centerHeight + shake);
        mCanvas.drawRect(rectF, mPaint);
        //清理
        mPaint.setShader(null);
        mPaint.setXfermode(null);
        //
        mCanvas.restoreToCount(saveCount);

        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(3);
        mPaint.setColor(Color.GRAY);
        mCanvas.drawPath(topPath, mPaint);
        mCanvas.drawPath(bottomPath, mPaint);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(3);
        mPaint.setColor(Color.RED);
        mCanvas.drawPath(centerPath, mPaint);
    }

    private float calculateY(float x, float offset) {
        offset %= 2;
        float sin = (float) Math.sin(0.75 * Math.PI * x + offset * Math.PI);
        float coefficient = (float) (4 / (4 + Math.pow(x, 4)));
        return sin * coefficient;
    }
}
