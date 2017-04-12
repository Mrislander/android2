package com.elitise.firmwareupdate;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;

/**
 * TODO: document your custom view class.
 */
public class ProgressBarView extends View {
    private  float mPercent = 0.5f;

    public ProgressBarView(Context context) {
        super(context);
        init(null, 0);
    }

    public ProgressBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public ProgressBarView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.ProgressBarView, defStyle, 0);


        a.recycle();


    }


    @Override
    protected void onDraw(Canvas canvas) {
        ProgressBar.drawCanvas1(canvas,new RectF(0,0,getWidth(),getHeight()), ProgressBar.ResizingBehavior.AspectFill,mPercent);
        super.onDraw(canvas);
    }

    public void setPercent(float percent){
        mPercent = percent;
        invalidate();
    }
}