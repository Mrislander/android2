package com.elitise.appv2;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

/**
 * TODO: document your custom view class.
 */
public class DeviceBatteryAnimationView extends View {

    private boolean[] ledOnOff = {true,true,true,true,true,true,true};
    private int mLedColor;
    private Bitmap phoneImage;
    private Bitmap tabletImage;
    private float mAmpsVal;
    private String mAmpsText;
    private boolean notCharging;
    private boolean isPhone;
    private boolean isTablet;

    private int ledIndex = 0;
    private ValueAnimator va;
    private GestureDetector mDetector;

    private DeviceBatteryAnimationView.exitListener local;

    public DeviceBatteryAnimationView.exitListener getChargeListener() {
        return local;
    }

    public void setExitListener(DeviceBatteryAnimationView.exitListener local) {
        this.local = local;
    }


    public DeviceBatteryAnimationView(Context context) {
        super(context);
        mDetector = new GestureDetector(DeviceBatteryAnimationView.this.getContext(), new DeviceBatteryAnimationView.GestureListener());
        init(null, 0);
    }

    public DeviceBatteryAnimationView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mDetector = new GestureDetector(DeviceBatteryAnimationView.this.getContext(), new DeviceBatteryAnimationView.GestureListener());
        init(attrs, 0);
    }

    public DeviceBatteryAnimationView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mDetector = new GestureDetector(DeviceBatteryAnimationView.this.getContext(), new DeviceBatteryAnimationView.GestureListener());
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.DeviceBatteryAnimationView, defStyle, 0);
        phoneImage = BitmapFactory.decodeResource(getResources(),R.drawable.generic_phone);
        tabletImage = BitmapFactory.decodeResource(getResources(),R.drawable.generic_tablet);

        a.recycle();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        DeviceBatteryAnimation.drawDeviceBatteryAnimation(canvas,new RectF(0,0,getWidth(),getHeight()), DeviceBatteryAnimation.ResizingBehavior.AspectFit,
                mLedColor,phoneImage,tabletImage,mAmpsText,mAmpsVal,ledOnOff[0],ledOnOff[1],ledOnOff[2],ledOnOff[3],ledOnOff[4],ledOnOff[5],ledOnOff[6], notCharging,isPhone,isTablet);

    }

    public void init(boolean t){
        notCharging = false;
        isPhone = !t;
        isTablet = t;
        mLedColor = Color.GREEN;
        mAmpsText = "0.0 Amp";
        mAmpsVal = 0f;
    }
    public void setMamps(float a){
        mAmpsVal = a/1000f;
        mAmpsText = String.format("%.1f",mAmpsVal)+" Amp";

        if(a>=0){
            notCharging = false;
            mLedColor = Color.GREEN;
        }else {
            notCharging = true;
            mLedColor = Color.RED;
        }

    }

    public void startAnimation(){

            va = ValueAnimator.ofInt(0,6);
            va.setDuration(2000);
            va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    ledDisplay(3, ledIndex);
                    if(mAmpsVal<=0) {
                        ledIndex++;
                        if(ledIndex == 7)
                            ledIndex = 0;
                    }
                    else {
                        ledIndex--;
                        if(ledIndex == -1)
                            ledIndex = 6;
                    }
                 DeviceBatteryAnimationView.this.invalidate();

                }
            });

            va.setRepeatCount(ValueAnimator.INFINITE);
            va.setRepeatMode(ValueAnimator.RESTART);
            va.start();
    }

    public void ledDisplay(int ledNum ,int idx){
        for(int i = 0 ;i<7 ;i++){
            ledOnOff[i] = false;
        }
        for(int i = 0;i<ledNum;i++){
            int index = (i+idx)%7;
            ledOnOff[index] = true;
        }
    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mDetector.onTouchEvent(event);
    }
    private class GestureListener extends GestureDetector.SimpleOnGestureListener{
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }


        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            getChargeListener().onGaugeSwipe();
            return true;
        }


    }

    public interface exitListener{
        void onGaugeSwipe();
    }



}
