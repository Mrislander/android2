package com.elitise.appv2;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;

import java.math.BigDecimal;
import java.util.Timer;
import java.util.TimerTask;

/**
 * TODO: document your custom view class.
 */
public class CurrentChargeStylekitView extends View {
    private float mValue = 0.0f;
    private String mValueText = "";
    private float gearAngle = 0;

    private float ledYPos = 157;
    private Bitmap alternatorImage;
    private Bitmap batteryImage;
    private Bitmap engineImage;
    private Bitmap motoCycleImage;
    private float alterScale = 0.3f;
    private float engineScale = 0.5f ;
    private float motoScale = 0.15f;
    private int ledColor = Color.GREEN;
    private boolean[] ledOnOff = {true,true,true,true,true,true,true};
    private boolean isMoto = false;
    private int ledShowNum= 0;
    //private ValueAnimator va;
    //private Timer animationTimer = new Timer();
    private int ledIndex = 0;
    //private Activity mContext;
    private ValueAnimator va;

    private GestureDetector mDetector;

    private CurrentChargeStylekitView.exitListener local;

    public CurrentChargeStylekitView.exitListener getChargeListener() {
        return local;
    }

    public void setExitListener(CurrentChargeStylekitView.exitListener local) {
        this.local = local;
    }

    public CurrentChargeStylekitView(Context context) {
        super(context);
        mDetector = new GestureDetector(CurrentChargeStylekitView.this.getContext(), new CurrentChargeStylekitView.GestureListener());
        init(null, 0);
    }

    public CurrentChargeStylekitView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mDetector = new GestureDetector(CurrentChargeStylekitView.this.getContext(), new CurrentChargeStylekitView.GestureListener());
        init(attrs, 0);
    }

    public CurrentChargeStylekitView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mDetector = new GestureDetector(CurrentChargeStylekitView.this.getContext(), new CurrentChargeStylekitView.GestureListener());
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.CurrentChargeStylekitView, defStyle, 0);
        alternatorImage = BitmapFactory.decodeResource(getResources(),R.drawable.alternatorimage);
        batteryImage = BitmapFactory.decodeResource(getResources(),R.drawable.battery_withlabel);
        engineImage = BitmapFactory.decodeResource(getResources(),R.drawable.engineimage);
        motoCycleImage = BitmapFactory.decodeResource(getResources(),R.drawable.motorcycleengine);

        a.recycle();

    }



    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        CurrentChargeStyleKit.drawCurrentChargeAnimation(canvas,new RectF(0,0,getWidth(),getHeight() ), CurrentChargeStyleKit.ResizingBehavior.AspectFit,ledColor,alternatorImage,engineImage,motoCycleImage,batteryImage,gearAngle,mValueText,
                ledYPos,mValue,alterScale,engineScale,motoScale,ledOnOff[0],ledOnOff[1],ledOnOff[2],ledOnOff[3],ledOnOff[4],ledOnOff[5],ledOnOff[6]);

    }

    public void setImage(int id){
        switch (id){
            case 0:
                alterScale = 0.3f;
                engineScale = 0f;
                motoScale = 0f;
                break;
            case 1:
                alterScale = 0f;
                engineScale = 0.5f;
                motoScale = 0f;
                break;
            case 2:
                alterScale = 0f;
                engineScale = 0f;
                motoScale = 0.15f;
                break;
            default:
                break;

        }
    }
    public void setmValue(float value){
        mValue = value;
        mValueText = Float.toString(round(value,1)) + " A";
        if(value > 0){
            ledColor = Color.GREEN;
        }else{
            ledColor = Color.RED;
        }
         //startAnimation();
    }

    public void animation(){
        int angle = 0;
         ledShowNum = 0;
        if(mValue == 0)
            return;
        else if(Math.abs(mValue)<1){
            angle = 1;
            ledShowNum = 1;
        }
        else if(Math.abs(mValue)<10){
            angle = 5;
            ledShowNum = 3;
        }else{
            angle = 10;
            ledShowNum = 5;
        }
        if(mValue > 0){ // charging
            setImage(0);
            gearAngle -= angle;

        }else {   //discharging
            if(isMoto){
                setImage(2);
            }else{
                setImage(1);
            }
            gearAngle += angle;
        }
    }
    public void setIsMode(boolean moto){
        isMoto = moto;
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

    public void startAnimation(){
        //this.setLayerType(View.LAYER_TYPE_HARDWARE,null);
        va = ValueAnimator.ofInt(0,6);
        va.setDuration(2000);
        va.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {


                animation();
                ledDisplay(ledShowNum, ledIndex);
                if(mValue>=0) {
                    ledIndex++;
                    if(ledIndex == 7)
                        ledIndex = 0;
                }
                else {
                    ledIndex--;
                    if(ledIndex == -1)
                        ledIndex = 6;
                }
                CurrentChargeStylekitView.this.invalidate();
            }
        });

        va.setRepeatCount(ValueAnimator.INFINITE);
        va.setRepeatMode(ValueAnimator.RESTART);
        va.start();
//        if(ledIndex == 7)
//            ledIndex = 0;
//        animation();
//        ledDisplay(ledShowNum, ledIndex);
//        ledIndex++;
//        invalidate();

    }

//    public void stopAnimation(){
//        va.end();
//    }

          public static float round(float d, int decimalPlace) {
            BigDecimal bd = new BigDecimal(Float.toString(d));
            bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
            float f = bd.floatValue();
            return f;
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
