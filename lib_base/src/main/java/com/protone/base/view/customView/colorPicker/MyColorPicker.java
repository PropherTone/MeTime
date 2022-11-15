package com.protone.base.view.customView.colorPicker;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.protone.base.R;

public class MyColorPicker extends ViewGroup {

    private Path mBarPath;
    private Paint mBarPaint;
    private LinearGradient linearGradient;
    private Bitmap colorBarBitmap;

    private float rectR;

    private int mMovedLength;

    private PickButton child;

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

    private final int[] colors = new int[]{
            Color.WHITE,
            Color.WHITE,
            Color.BLACK,
            Color.RED,
            Color.YELLOW,
            Color.GREEN,
            Color.parseColor("#18FFFF"),
            Color.BLUE,
            Color.parseColor("#E040FB"),
            Color.RED};

    private boolean isRound;
    private float blurRadius;
    private int mBarHeight;
    private int mPaddingTop;
    private int mPaddingBottom;
    private float mStrokeWidth;
    private float mElevation;

    private colorListener onColorChangeListener;

    private Boolean isLaidOut = false;
    private Boolean isBarDrawn = false;


    public MyColorPicker(Context context) {
        super(context);
    }

    public MyColorPicker(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        TypedArray array = context.getTheme().obtainStyledAttributes(attrs,
                R.styleable.DragBar,
                0, 0);
        mBarHeight = array.getDimensionPixelSize(R.styleable.DragBar_barHeight, 100);
        mPaddingBottom = array.getInteger(R.styleable.DragBar_paddingBottom, 20);
        mPaddingTop = array.getInteger(R.styleable.DragBar_paddingTop, 20);
        isRound = array.getBoolean(R.styleable.DragBar_roundStyle, true);
        blurRadius = array.getFloat(R.styleable.DragBar_blurRadius, 20);
        mStrokeWidth = array.getInteger(R.styleable.DragBar_barStrokeWidth, 30);
        mElevation = array.getFloat(R.styleable.DragBar_buttonElevation, 5);
        array.recycle();
        Init();
    }

    public MyColorPicker(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
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
        mBarPaint.setMaskFilter(
                new BlurMaskFilter(blurRadius, BlurMaskFilter.Blur.SOLID));
        mBarPath = new Path();
        addView(new PickButton(getContext()));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                oldX = event.getX();
                my = event.getY();
                mx = event.getX();
            case MotionEvent.ACTION_MOVE:
                getParent().requestDisallowInterceptTouchEvent(true);
                if (!(my > mT && my < mB && mx > mL && mx < mR)) {
                    break;
                }

                mMovedLength += event.getX() - oldX;
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
                    child.layout(movePositionL, mT, movePositionR, mB);
                }

                if (colorBarBitmap != null && onColorChangeListener != null) {
                    int x = (movePositionL + movePositionR) / 2;
                    int y = (mT + mB) / 2;
                    int color = getColor(colorBarBitmap.getPixel(x, y));
                    onColorChangeListener.onColorChange(color);
                    child.setButtonColor(color);
                }

                oldX = event.getX();

                break;
            case MotionEvent.ACTION_UP:
                mL = movePositionL;
                mR = movePositionR;
                mMovedLength = 0;
                child.lostClk();
                performClick();
                break;
        }

        return super.onTouchEvent(event);
    }

    private int getColor(int pixel) {
        return Color.argb(
                Color.alpha(pixel),
                Color.red(pixel),
                Color.green(pixel),
                Color.blue(pixel));
    }

    @Override
    public boolean performClick() {
        return super.performClick();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (!isLaidOut) {
            int childCount = getChildCount();
            if (childCount > 0) {
                child = (PickButton) getChildAt(childCount - 1);
                child.setClickable(false);
                child.setFocusable(false);
                child.setRound(isRound);
                child.setMElevation(mElevation);
                cacheL = mL = getLeft();
                movePositionR = cacheR = mR;
                child.layout(mL, mT, mR, mB);
            }
            isLaidOut = true;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(widthMeasureSpec,
                mBarHeight + mPaddingTop + mPaddingBottom);
        int barWidth = mBarHeight / 2;
        mBarPaint.setStrokeWidth(barWidth);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        float rectT = mPaddingTop;
        float rectB = mBarHeight + mPaddingBottom;
        rectR = getRight() - mStrokeWidth;

        mT = (int) ((rectT - mStrokeWidth / 2) - mElevation);
        mB = (int) ((rectB + mStrokeWidth / 2) + mElevation);
        mR = mB - mT;

        linearGradient = new LinearGradient(0, 0,
                getMeasuredWidth(), 0,
                colors, null, Shader.TileMode.MIRROR);
        colorBarBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!isBarDrawn) {
            Canvas barCanvas = new Canvas(colorBarBitmap);
            mBarPath.reset();
            int h2 = getMeasuredHeight() / 2;
            int half = cacheR / 2;
            int mX = getMeasuredWidth() - half;
            mBarPath.moveTo(half, h2);
            mBarPath.lineTo(mX, h2);
            mBarPaint.setShader(linearGradient);
            barCanvas.drawPath(mBarPath, mBarPaint);
            isBarDrawn = true;
        }
        canvas.drawBitmap(colorBarBitmap, 0, 0, null);
    }

    public interface colorListener {
        void onColorChange(int color);
    }

    @SuppressWarnings("unused")
    public void onColorChangeListener(colorListener colorListener) {
        this.onColorChangeListener = colorListener;
    }
}
