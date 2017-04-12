package com.elitise.firmwareupdate;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Andy.Xiang on 2/14/2017.
 */
public class ConnectLEDView extends View {
    int mColor = Color.BLACK;
    private Timer timer = null;
    private boolean on = false;
    private boolean isBlinking  = false;
    public ConnectLEDView(Context context) {
        super(context);
        init(null, 0);
        //f = new RectF(0,0,getWidth(),getHeight());
    }

    public ConnectLEDView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
        //f = new RectF(0,0,getWidth(),getHeight());
    }

    public ConnectLEDView(Context context, AttributeSet attrs, int defStyle) {
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


    public void blinkLED(final int C,boolean repeat){

        timer = new Timer();

        if(repeat) {
            isBlinking = true;
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    on = !on;
                    if (on) {
                        mColor = C;
                    } else {
                        mColor = Color.BLACK;
                    }
                    postInvalidate();
                    //invalidate();
                }
            }, 0, 250);

        }else {
            mColor = C;
            invalidate();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    mColor = Color.BLACK;
                    postInvalidate();
                    //invalidate();
                }
            }, 250);

        }
    }
    public void stopBlink(){
        mColor = Color.BLACK;
        if(timer!=null) {
            timer.cancel();
        }
        postInvalidate();
    }
    public boolean isBlinking(){
        return isBlinking;
    }

}