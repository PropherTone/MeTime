package com.protone.ui.view.customView.dragBar;

import android.content.Context;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class DragButton extends View {

    private Paint paint;
    private float mStrokeWidth = 30;
    private float elevation = 5;
    private boolean isRound;

    public DragButton(Context context) {
        super(context);
        Init();
    }

    public DragButton(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        Init();
    }

    public DragButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Init();
    }

    private void Init() {
        setBackgroundColor(Color.TRANSPARENT);
        paint = new Paint();
        paint.setPathEffect(new CornerPathEffect(mStrokeWidth));
        paint.setColor(Color.parseColor("#FFFDFDFD"));
        paint.setShadowLayer(elevation,0,0,Color.GRAY);
    }

    public void onClk(){
        if (paint != null) {
            paint.setMaskFilter(new BlurMaskFilter(elevation,BlurMaskFilter.Blur.SOLID));
            invalidate();
        }
    }
    public void lostClk(){
        if (paint != null) {
            paint.setMaskFilter(null);
            invalidate();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getMeasuredWidth(),getMeasuredWidth());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (isRound){
            int x = (int) (getWidth() / 2);
            int y = (int) (getHeight() / 2);
            canvas.drawCircle(x,y,x-elevation*4,paint);
        }else {
            canvas.drawRect(elevation*4,
                    elevation*4,
                    getWidth()-elevation*4,
                    getHeight()-elevation*4,paint);
        }
    }

    public void setRound(boolean round) {
        this.isRound = round;
    }

    public void setStrokeWidth(float strokeWidth) {
        this.mStrokeWidth = strokeWidth;
    }

    public float getMElevation() {
        return elevation;
    }

    public void setMElevation(float elevation) {
        this.elevation = elevation;
        invalidate();
    }
}
