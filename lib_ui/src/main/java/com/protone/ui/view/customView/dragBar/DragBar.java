package com.protone.ui.view.customView.dragBar;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.protone.ui.R;

public class DragBar extends ViewGroup {

    private Path mBarPath;
    private Paint mBarPaint;
    private LinearGradient linearGradient;


    private Bitmap mBackgroundBitmap;
    private Bitmap mForegroundBitmap;

    private float rectR;

    private int mMovedLength;

    private DragButton child;

    private int cacheL;
    private int cacheR;

    private int mL;
    private int mT;
    private int mR;
    private int mB;

    private float oldX;

    private float my;
    private float mx;

    private int movePositionR;
    private int movePositionL;

    private int[] colors = new int[]{Color.parseColor("#FFBB86FC"),
            Color.parseColor("#448AFF")};
    private int scroll;
    private int speed;
    private int steep;
    private boolean isRound;
    private float blurRadius;
    private int mBarHeight;
    private int mPaddingTop;
    private int mPaddingBottom;
    private float mStrokeWidth;
    private boolean barAnimator;
    private int barColor = Color.parseColor("#F6F6F6");
    private float shadowRadius;
    private int mElevation;
    public Long duration;
    public boolean isProgressBar = true;

    private Handler handler;
    private Runnable colorRunnable;
    public progress progressListener;

    private boolean onTouch = false;

    private final String TAG = "DragBar";

    public DragBar(Context context) {
        super(context);
    }

    public DragBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray array = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.DragBar,
                0, 0);
        mBarHeight = array.getInteger(R.styleable.DragBar_barHeight, 100);
        mPaddingBottom = array.getInteger(R.styleable.DragBar_paddingBottom, 20);
        mPaddingTop = array.getInteger(R.styleable.DragBar_paddingTop, 20);
        steep = array.getInteger(R.styleable.DragBar_steep, 10);
        speed = array.getInteger(R.styleable.DragBar_scrollingSpeed, 100);
        isRound = array.getBoolean(R.styleable.DragBar_roundStyle, true);
        blurRadius = array.getInteger(R.styleable.DragBar_blurRadius, 10);
        mStrokeWidth = array.getInteger(R.styleable.DragBar_barStrokeWidth, 30);
        barAnimator = array.getBoolean(R.styleable.DragBar_barAnimator, true);
        barColor = array.getColor(R.styleable.DragBar_barColor, barColor);
        shadowRadius = array.getFloat(R.styleable.DragBar_shadowRadius, 8f);
        mElevation = array.getInteger(R.styleable.DragBar_buttonElevation, 5);
        array.recycle();
        Init();
    }

    public DragBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private void Init() {
        setClickable(true);
        setBackgroundColor(Color.TRANSPARENT);
        mBarPaint = new Paint();

        mBarPaint.setStyle(Paint.Style.STROKE);
        mBarPaint.setAntiAlias(true);
        mBarPaint.setDither(true);
        mBarPaint.setStrokeJoin(Paint.Join.ROUND);
        mBarPaint.setStrokeCap(Paint.Cap.ROUND);
        mBarPaint.setColor(Color.WHITE);
        mBarPaint.setMaskFilter(
                new BlurMaskFilter(blurRadius, BlurMaskFilter.Blur.SOLID));

        if (barAnimator) {
            handler = new Handler();
            colorRunnable = new colorRunnable();
            handler.post(colorRunnable);
        }
        mBarPath = new Path();
        addView(new DragButton(getContext()));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        onTouch = true;
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
//                oldX = event.getX();
//                my = event.getY();
//                mx = event.getX();
            case MotionEvent.ACTION_MOVE:
                getParent().requestDisallowInterceptTouchEvent(true);
                mMovedLength += (int) (event.getX() - oldX);
                Log.d(TAG, "onTouchEvent: "+mMovedLength);
                movePositionL = mL + mMovedLength;
                movePositionR = mR + mMovedLength;
                float len = rectR + mStrokeWidth;
                if (child != null) {
                    if (movePositionL < cacheL) {
                        movePositionL = cacheL;
                        movePositionR = cacheR;
                    } else if (movePositionR > len) {
                        movePositionR = (int) len;
                        movePositionL = movePositionR - (cacheR - cacheL);
                    }
                    child.onClk();
                    Log.d(TAG, "onTouchEvent: "+movePositionL);
                    Log.d(TAG, "onTouchEvent: "+mT);
                    Log.d(TAG, "onTouchEvent: "+movePositionR);
                    Log.d(TAG, "onTouchEvent: "+mB);
                    child.layout(movePositionL, mT, movePositionR, mB);
                }
                if (progressListener != null) {
                    float total = len - cacheR;
                    float p = movePositionR - cacheR;
                    progressListener.getProgress((long) getProgress(p, total));
                }
                oldX = event.getX();
                invalidate();
                break;
            case MotionEvent.ACTION_UP:
                mL = movePositionL;
                mR = movePositionR;
                mMovedLength = 0;
                child.lostClk();
                performClick();
                onTouch = false;
                break;
        }

        return super.onTouchEvent(event);
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int childCount = getChildCount();
        if (childCount > 0) {
            child = (DragButton) getChildAt(childCount - 1);
            child.setClickable(false);
            child.setFocusable(false);
            child.setRound(isRound);
            child.setMElevation(mElevation);
            cacheL = mL = getLeft();
            movePositionR = cacheR = mR;
            child.layout(mL, mT, mR, mB);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(widthMeasureSpec,
                mBarHeight + mPaddingTop + mPaddingBottom);
        mBarPaint.setStrokeWidth(mBarHeight);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        float rectT = mPaddingTop;
        float rectB = mBarHeight + mPaddingBottom;
        float rectL = getLeft() + mStrokeWidth;
        rectR = getRight() - mStrokeWidth;

        mT = (int) (rectT - mStrokeWidth / 2) - mElevation * 2;
        mB = (int) (rectB + mStrokeWidth / 2) + mElevation * 2;
        mR = mB - mT;

        mForegroundBitmap = Bitmap.createBitmap(w,
                h, Bitmap.Config.ARGB_8888);
        Paint mForegroundPaint = new Paint();
        PaintSet(mForegroundPaint);
        DrawBitmap(rectL, rectT, rectR, rectB,
                new Canvas(mForegroundBitmap), mForegroundPaint, barColor);

        if (!isProgressBar) {
            mBackgroundBitmap = Bitmap.createBitmap(w,
                    h, Bitmap.Config.ARGB_8888);
            Paint mBackgroundPaint = new Paint();
            PaintSet(mBackgroundPaint);
            mBackgroundPaint.setMaskFilter(new BlurMaskFilter(shadowRadius, BlurMaskFilter.Blur.NORMAL));
            DrawBitmap(rectL, rectT, rectR, rectB,
                    new Canvas(mBackgroundBitmap), mBackgroundPaint, Color.GRAY);
        }
    }

    private void PaintSet(Paint paint) {
        paint.setStyle(Paint.Style.FILL);
        if (!isRound) {
            paint.setPathEffect(new CornerPathEffect(45));
        }
        paint.setAntiAlias(true);
        paint.setDither(true);
        if (!isProgressBar) paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_OUT));
    }

    private void DrawBitmap(float L, float T, float R, float B, Canvas canvas, Paint paint, int color) {
        int radius = mR / 2;
        if (!isProgressBar) {
            canvas.drawColor(Color.BLUE, PorterDuff.Mode.MULTIPLY);
            canvas.drawColor(color);
        } else {
            paint.setColor(color);
        }
        if (isRound) {
            canvas.drawRoundRect(L, T, R, B, radius, radius, paint);
        } else {
            canvas.drawRect(L, T, R, B, paint);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mBarPath.reset();
        int h2 = getMeasuredHeight() / 2;
        int half = cacheR / 2;
        int mX = movePositionR - half;
        mBarPath.moveTo(half, h2);
        mBarPath.lineTo(mX, h2);
        if (barAnimator) {
            mBarPaint.setShader(linearGradient);
        } else {
            mBarPaint.setShader(null);
        }

        if (!isProgressBar) canvas.drawBitmap(mBackgroundBitmap, 0, 0, null);
        canvas.drawBitmap(mForegroundBitmap, 0, 0, null);
        canvas.drawPath(mBarPath, mBarPaint);
    }

    private class colorRunnable implements Runnable {

        @Override
        public void run() {
            scroll += steep;

            if (scroll >= (cacheR << 4)) {
                scroll = cacheL;
            }
            linearGradient =
                    new LinearGradient(scroll, 100,
                            (cacheR << 2) + scroll, 200,
                            colors, null, Shader.TileMode.MIRROR);

            postInvalidateDelayed(10);

            handler.postDelayed(colorRunnable, speed);
        }
    }

    public void setProgress(long duration) {
        if (!onTouch){
            float len = rectR + mStrokeWidth;
            mMovedLength = (int) (((float) duration / (float) this.duration) * (len - cacheR));
            movePositionL = cacheL + mMovedLength;
            movePositionR = cacheR + mMovedLength;
            if (child != null) {
                if (movePositionL < cacheL) {
                    movePositionL = cacheL;
                    movePositionR = cacheR;
                } else if (movePositionR > len) {
                    movePositionR = (int) len;
                    movePositionL = movePositionR - (cacheR - cacheL);
                }
                child.layout(movePositionL, mT, movePositionR, mB);
            }
            invalidate();
            mMovedLength = 0;
            mL = movePositionL;
            mR = movePositionR;
        }
    }

    private int getProgress(float p, float total) {
        return (int) ((p / total) * 100);
    }

    public void setStrokeWidth(float strokeWidth) {
        this.mStrokeWidth = strokeWidth;
    }

    public void setColors(int[] colors) {
        this.colors = colors;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public void setSteep(int steep) {
        this.steep = steep;
    }

    public int getSpeed() {
        return speed;
    }

    public int getSteep() {
        return steep;
    }

    public boolean isRound() {
        return isRound;
    }

    public void setRound(boolean round) {
        isRound = round;
    }

    public float getBlurRadius() {
        return blurRadius;
    }

    public void setBlurRadius(float blurRadius) {
        this.blurRadius = blurRadius;
    }

    public int getMPaddingTop() {
        return mPaddingTop;
    }

    public void setMPaddingTop(int mPaddingTop) {
        this.mPaddingTop = mPaddingTop;
    }

    public int getMPaddingBottom() {
        return mPaddingBottom;
    }

    public void setMPaddingBottom(int mPaddingBottom) {
        this.mPaddingBottom = mPaddingBottom;
    }

    public float getMStrokeWidth() {
        return mStrokeWidth;
    }

    public void setMStrokeWidth(float mStrokeWidth) {
        this.mStrokeWidth = mStrokeWidth;
    }

    public boolean isBarAnimator() {
        return barAnimator;
    }

    public void setBarAnimator(boolean barAnimator) {
        this.barAnimator = barAnimator;
    }

    public int getBarColor() {
        return barColor;
    }

    public void setBarColor(int barColor) {
        this.barColor = barColor;
    }

    public float getShadowRadius() {
        return shadowRadius;
    }

    public void setShadowRadius(float shadowRadius) {
        this.shadowRadius = shadowRadius;
    }

    public int getMElevation() {
        return mElevation;
    }

    public void setMElevation(int mElevation) {
        this.mElevation = mElevation;
    }

    public int getMBarHeight() {
        return mBarHeight;
    }

    public void setMBarHeight(int barHeight) {
        this.mBarHeight = barHeight;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public boolean isProgressBar() {
        return isProgressBar;
    }

    public void setProgressBar(boolean progressBar) {
        isProgressBar = progressBar;
    }

    public interface progress {
        void getProgress(Long p);
    }

    public void progressListener(progress progress) {
        this.progressListener = progress;
    }

    public void stopAnimator() {
        if (barAnimator) {
            handler.removeCallbacks(colorRunnable);
        }
    }
}
