package com.elitise.appv2;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * TODO: document your custom view class.
 */
public class ConnectLedView extends View {
    int mColor = Color.RED;
    RectF  f;
    public ConnectLedView(Context context) {
        super(context);
        init(null, 0);
        //f = new RectF(0,0,getWidth(),getHeight());
    }

    public ConnectLedView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
        //f = new RectF(0,0,getWidth(),getHeight());
    }

    public ConnectLedView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
        //f = new RectF(0,0,getWidth(),getHeight());
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.ConnectLedView, defStyle, 0);
        this.setLayerType(View.LAYER_TYPE_HARDWARE,null);
        a.recycle();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //f = new RectF(0,0,getWidth(),getHeight());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //Log.v("SIZE","Width: "+f.width()+" Height: "+ f.height());

        ConnectLED.drawCanvas1(canvas, new RectF(0,0,getWidth(),getHeight()), ConnectLED.ResizingBehavior.AspectFill,mColor);

    }

    public void setLedColor(int color){
        mColor = color;
        invalidate();
    }

}
