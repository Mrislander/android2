package com.elitise.appv2;

import android.animation.Animator;
import android.animation.FloatArrayEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.github.florent37.viewanimator.AnimationListener;
import com.github.florent37.viewanimator.ViewAnimator;
import com.mikepenz.materialize.color.Material;

import java.math.BigDecimal;

/**
 * TODO: document your custom view class.
 */
public class TempGaugeView extends View {

    private String [] ticks = {"-40","-25","-10","5","20","35","50","65","80","95","110","125"};
    private float mCurrentVaule = 42.5f;
    private String mCurrentVauleDisplay = "test";
    private ValueAnimator animator = null;
    private float maxTick = 125f;
    private float minTick = -40f;
    private int gColor1 = Color.BLUE;
    private int gColor2 = Color.GREEN;
    private int gColor3 = Color.YELLOW;
    private int gColor4 = Color.RED;
    private boolean isZoomIn = false;
    private GestureDetector mDetector;

    private int redRangeWidth = 22;
    private int blueMin = -40;
    private int blueMax = 9;
    private int greenMin = 10;
    private int greenMax = 50;
    private int yellowMin = 51;
    private int yellowMax= 77;
    private int orangeMin = 78;
    private int orangeMax = 89;
    private int redMin = 90;
    private int redMax = 125;

    private boolean isCelsius = true;
    public boolean updating = false;


    private TempGaugeView.TempGaugeListener local;
    public TempGaugeView.TempGaugeListener getTempListener() {
        return local;
    }

    public void setTempListener(TempGaugeView.TempGaugeListener local) {
        this.local = local;
    }
    public void initGauge(boolean celsius){
        isCelsius = celsius;
        if(isCelsius) {
            maxTick = 125f;
            mCurrentVaule = 42.5f;
            minTick = -40f;
            for(int i = 0; i<ticks.length;i++){
                ticks[i] = Integer.toString((int)(minTick+i*15));
            }
        }else{
            maxTick = 257f;
            mCurrentVaule = 108.5f;
            for(int i = 0; i<ticks.length;i++){
                ticks[i] = Integer.toString((int)(minTick+i*27));
            }
        }
    }



    public TempGaugeView(Context context) {
        super(context);
        //TempGauge.context = context;
        mDetector = new GestureDetector(TempGaugeView.this.getContext(), new TempGaugeView.GestureListener());
        init(null, 0);
    }

    public TempGaugeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        //TempGauge.context = context;
        mDetector = new GestureDetector(TempGaugeView.this.getContext(), new TempGaugeView.GestureListener());
        init(attrs, 0);
    }

    public TempGaugeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        //TempGauge.context = context;
        mDetector = new GestureDetector(TempGaugeView.this.getContext(), new TempGaugeView.GestureListener());
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.TempGaugeView, defStyle, 0);
        //this.setLayerType(View.LAYER_TYPE_HARDWARE,null);
        a.recycle();

    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //zoomInOut();
        TempGauge.drawTempGauge(canvas,new RectF(0,0,getWidth(),getHeight()), TempGauge.ResizingBehavior.AspectFit,gColor1,gColor2,gColor3,gColor4,mCurrentVauleDisplay,minTick,maxTick,mCurrentVaule,
                ticks[0],ticks[1],ticks[2],ticks[3],ticks[4],ticks[5],ticks[6],ticks[7],ticks[8],ticks[9],ticks[10],ticks[11],redRangeWidth);
    }

    public void celsiusAndFahrenheit(boolean celsius){
        if(celsius){
           if(!isCelsius){
               isCelsius = true;
               mCurrentVaule = (mCurrentVaule-32) *5.0f / 9.0f;
               mCurrentVauleDisplay = Float.toString(round(mCurrentVaule,1))+" °C";

           }
        }else{
            if(isCelsius){
                isCelsius = false;
                mCurrentVaule = mCurrentVaule*9.0f/5.0f+32.0f;
                mCurrentVauleDisplay = Float.toString(round(mCurrentVaule,1))+" °F";

            }
        }
        zoomInOut();
    }


    public void zoomInOut(){

        if(!isZoomIn){
                if(isCelsius){
                    minTick = -40f;
                    maxTick = 125f;
                    for(int i = 0; i< ticks.length;i++){
                        ticks[i] = Integer.toString((int)(minTick+i*15));
                    }
                }
                else {
                    minTick = -40f;
                    maxTick = 257f;
                    for(int i = 0; i< ticks.length;i++){
                        ticks[i] = Integer.toString((int)(minTick+i*27));
                    }
            }
            gColor1 = Color.BLUE;
            gColor2 = Color.GREEN;
            gColor3 = Color.YELLOW;
            gColor4 = Color.RED;
            redRangeWidth = 23;

        }else{
                int tempC = 0;
                if(isCelsius){
                    int temp = (int) mCurrentVaule;
                    minTick =  (float) (((temp - 5) < -40 ? -40:(temp-5)));
                    maxTick =  (float) (((temp + 6) > 125 ? 125:(temp+6)));
                    for(int i = 0; i< ticks.length;i++){
                        ticks[i] = Integer.toString((int)(minTick+i));
                    }

                }
                else {
                    int temp = (int) mCurrentVaule;
                    minTick =  (float) (((temp - 15) < -40 ? -40:(temp -15)));
                    maxTick =  (float) (((temp + 18) > 257 ? 257 :(temp+18)));
                    for(int i = 0; i< ticks.length;i++){
                        ticks[i] = Integer.toString((int)(minTick+i*3));
                    }
                }
                if(!isCelsius) {
                    tempC = (int) ((mCurrentVaule - 32) * 5.0f / 9.0f);

                }else{
                    tempC = (int) mCurrentVaule;
                }

                if( tempC < blueMax && tempC > blueMin){

                    gColor1 = Color.BLUE;
                    gColor2 = Color.BLUE;
                    gColor3 = Color.BLUE;
                    gColor4 = Color.BLUE;
                    redRangeWidth = 0;

                }else if( tempC > greenMin && tempC  < greenMax){

                    gColor1 = Color.GREEN;
                    gColor2 = Color.GREEN;
                    gColor3 = Color.GREEN;
                    gColor4 = Color.GREEN;
                    redRangeWidth = 0;
                }
                else if(tempC > yellowMin && tempC  < yellowMax){

                    gColor1 = Color.YELLOW;
                    gColor2 = Color.YELLOW;
                    gColor3 = Color.YELLOW;
                    gColor4 = Color.YELLOW;
                    redRangeWidth = 0;
                }else if(tempC > orangeMin && tempC < orangeMax){
                    gColor1 = Color.parseColor("#FFA500");
                    gColor2 = Color.parseColor("#FFA500");
                    gColor3 = Color.parseColor("#FFA500");
                    gColor4 = Color.parseColor("#FFA500");
                    redRangeWidth = 0;
                }
                else if(tempC > redMin && tempC  < redMax){

                    gColor1 = Color.RED;
                    gColor2 = Color.RED;
                    gColor3 = Color.RED;
                    gColor4 = Color.RED;
                    redRangeWidth = 22;
                }
                else{
                    gColor1 = Color.BLUE;
                    gColor2 = Color.GREEN;
                    gColor3 = Color.YELLOW;
                    gColor4 = Color.RED;
                    redRangeWidth = 22;
                }
         }
        invalidate();
    }

    public void setmCurrentVaule(float newValue) {
        float previousValue = mCurrentVaule;
        if(isCelsius) {
            if (newValue < -40f) {
                mCurrentVaule = -40f;
                mCurrentVauleDisplay = "-40 °C";
            } else if (newValue > 125f) {
                mCurrentVaule = 125f;
                mCurrentVauleDisplay = "125 °C";
            } else {
                mCurrentVaule = newValue;
                mCurrentVauleDisplay = Float.toString(round(mCurrentVaule, 1)) + " °C";
            }
        }else{
            if (newValue < -40f) {
                mCurrentVaule = -40f;
                mCurrentVauleDisplay = "-40 °F";
            } else if (newValue > 125f) {
                mCurrentVaule = 125f;
                mCurrentVauleDisplay = "257 °F";
            } else {
                mCurrentVaule = newValue*9.0f/5.0f+32.0f;
                mCurrentVauleDisplay = Float.toString(round(mCurrentVaule, 1)) + " °F";
            }

        }
        if(mCurrentVaule >= maxTick|| mCurrentVaule<=minTick){
            zoomInOut();
        }
//        if (animator != null) {
//            animator.cancel();
 //       }
        animator = ValueAnimator.ofFloat(previousValue,mCurrentVaule);
        animator.setDuration(800);
        animator.setStartDelay(50);
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                updating = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                updating = false;
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                mCurrentVaule = value;
                invalidate();
            }
        });
        animator.start();
    }

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

    private void startAnimation(){
        ViewAnimator
                .animate(this)
                .alpha(1,0,1)
                .duration(1000)
                .onStop(new AnimationListener.Stop() {
                    @Override
                    public void onStop() {
                        zoomInOut();
                    }
                })
                .start();
    }

    private class GestureListener extends GestureDetector.SimpleOnGestureListener{
        @Override
        public boolean onDown(MotionEvent e) {
            return true;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            //Toast.makeText(getContext(),"doudle tap",Toast.LENGTH_SHORT).show();
            if(!updating) {
                isZoomIn = !isZoomIn;
                startAnimation();
            }
            return true;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

            //Toast.makeText(getContext(),"Sweep",Toast.LENGTH_SHORT).show();
            if(!updating) {
                celsiusAndFahrenheit(!isCelsius);
                getTempListener().onGaugeSwipe(isCelsius);
            }

            return true;
        }


    }
    public void reset(){
        initGauge(isCelsius);
    }

    public interface TempGaugeListener{
        void onGaugeSwipe(boolean type);
    }
}