package com.protone.base.view.customView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class ScaleImageView extends androidx.appcompat.widget.AppCompatImageView {

    private float Finger1DownX;
    private float Finger1DownY;
    private float Finger2DownX;
    private float Finger2DownY;
    private double oldDistance;

    private static final float SCALE_MAX = 5.0f;
    private static final float SCALE_MID = 2.5f;
    private static final float SCALE_MIN = 1.0f;

    private float clkX;
    private float clkY;

    private final long[] clk = new long[2];

    private int zoomIn = 0;

    public ScaleImageView(@NonNull Context context) {
        super(context);
    }

    public ScaleImageView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public ScaleImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void load(Uri uri){

    }

    public void openDoubleClick(){
        setClickable(true);
        setFocusable(true);
        OnClickListener listener = v -> {
            System.arraycopy(clk, 1, clk, 0, clk.length - 1);
            clk[clk.length - 1] = SystemClock.uptimeMillis();
            if (clk[clk.length - 1] - clk[0] < 300) {
                performZoom();
            }
        };
        setOnClickListener(listener);
    }

    public void performZoom(){
        switch (zoomIn) {
            case 0:
                setPivotXYNoCalculate(clkX, clkY);
                animateScale(SCALE_MID, SCALE_MID);
                zoomIn++;
                break;
            case 1:
                setPivotXYNoCalculate(clkX, clkY);
                animateScale(SCALE_MAX, SCALE_MAX);
                zoomIn++;
                break;
            case 2:
                animateScale(SCALE_MIN, SCALE_MIN);
                zoomIn = 0;
                break;
        }
    }

    private void setPivotXYNoCalculate(float x, float y) {
        setPivotX(x);
        setPivotY(y);
    }

    private void animateScale(float x, float y) {
        animate().scaleX(x).scaleY(y).setDuration(100).start();
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        int fingerCounts = ev.getPointerCount();
        switch (ev.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_UP:
                if (fingerCounts == 2) {
                    Finger1DownX = 0;
                    Finger1DownY = 0;
                    Finger2DownX = 0;
                    Finger2DownY = 0;
                    return true;
                }
            case MotionEvent.ACTION_MOVE:
                if (fingerCounts == 2) {
                    float moveX1 = ev.getX(0);
                    float moveY1 = ev.getY(0);
                    float moveX2 = ev.getX(1);
                    float moveY2 = ev.getY(1);

                    double changeX1 = moveX1 - Finger1DownX;
                    double changeY1 = moveY1 - Finger1DownY;
                    double changeX2 = moveX2 - Finger2DownX;
                    double changeY2 = moveY2 - Finger2DownY;
                    if (getScaleX() > 1) { //滑动
                        float lessX = (float) ((changeX1) / 2 + (changeX2) / 2);
                        float lessY = (float) ((changeY1) / 2 + (changeY2) / 2);
                        setPivot(-lessX, -lessY);
                    }
                    double newDistance = spacing(ev);
                    double space = newDistance - oldDistance;
                    float scale = (float) (getScaleX() + space / getWidth());
                    setScale(Math.min(scale, SCALE_MAX));
                    return true;
                }
            case MotionEvent.ACTION_POINTER_DOWN:
                clkX = ev.getX();
                clkY = ev.getY();
                if (fingerCounts == 2) {
                    Finger1DownX = ev.getX(0);
                    Finger1DownY = ev.getY(0);
                    Finger2DownX = ev.getX(1);
                    Finger2DownY = ev.getY(1);
                    oldDistance = spacing(ev);
                    return true;
                }
            default:
                break;
        }
        return super.onTouchEvent(ev);
    }

    public void setPivot(float x, float y) {
        float PivotX;
        float PivotY;
        PivotX = getPivotX() + x;
        PivotY = getPivotY() + y;
        if (PivotX < 0 && PivotY < 0) {
            PivotX = 0;
            PivotY = 0;
        } else if (PivotX > 0 && PivotY < 0) {
            PivotY = 0;
            if (PivotX > getWidth()) {
                PivotX = getWidth();
            }
        } else if (PivotX < 0 && PivotY > 0) {
            PivotX = 0;
            if (PivotY > getHeight()) {
                PivotY = getHeight();
            }
        } else {
            if (PivotX > getWidth()) {
                PivotX = getWidth();
            }
            if (PivotY > getHeight()) {
                PivotY = getHeight();
            }
        }
        setPivotX(PivotX);
        setPivotY(PivotY);

    }

    private void setScale(float scale) {
        setScaleX(scale);
        setScaleY(scale);
    }

    private double spacing(MotionEvent event) {
        if (event.getPointerCount() == 2) {
            float x = event.getX(0) - event.getX(1);
            float y = event.getY(0) - event.getY(1);
            return Math.sqrt(x * x + y * y);
        } else {
            return 0;
        }
    }
}

