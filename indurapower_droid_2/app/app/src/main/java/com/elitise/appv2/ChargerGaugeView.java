package com.elitise.appv2;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import com.github.florent37.viewanimator.AnimationListener;
import com.github.florent37.viewanimator.ViewAnimator;

import java.math.BigDecimal;

/**
 * TODO: document your custom view class.
 */
public class ChargerGaugeView extends View {

    private String [] ticks = {"0","10","20","30","40","50","60","70","80","90","100"};
    private float mSOC = 50.0f;
    private float mSOF = 0f;
    private String mCurrentVauleDisplay = "50.0%";
    private ValueAnimator animator = null;
    private float maxTick = 100f;
    private float minTick = 0.0f;
    private int startColor = Color.RED;
    private int endColor = Color.GREEN;
    private boolean zoomIn = false;
    private boolean isChargerGauge = true;
    private GestureDetector mDetector;
    private int needleColor = Color.GRAY;
    private boolean oneNeedle = false;
    private boolean isDevice = false;


    private ChargerGaugeView.ChargeGaugeListener local;
    public ChargerGaugeView.ChargeGaugeListener getChargeListener() {
        return local;
    }

    public void setChargeListener(ChargerGaugeView.ChargeGaugeListener local) {
        this.local = local;
    }

    public void reset(){
        for (int i=0 ;i<11;i++){
            ticks[i] = Integer.toString(i*10);
        }
        mSOC = 50.0f;
        mCurrentVauleDisplay = "50.0%";
        isChargerGauge = true;
        startColor = Color.RED;
        endColor = Color.GREEN;
        zoomIn = false;
        invalidate();
    }

    public void setDevice(boolean t){
        isDevice = t;
    }

    public String[] getTicks() {
        return ticks;
    }

    public void setTicks(String[] ticks) {
        this.ticks = ticks;
    }

    public float getmSOC() {
        return mSOC;
    }

    public void setmSOC(float newValue) {

        float previousValue = mSOC;
        if(isChargerGauge) {
            if (newValue < 0f) {
                mSOC = 0.0f;
                mCurrentVauleDisplay = "0.0%";
            } else if (newValue > 100f) {
                mSOC = 100f;
                mCurrentVauleDisplay = "100.0%";
            } else {
                mSOC = newValue;
                mCurrentVauleDisplay = Float.toString(round(mSOC,1)) + "%";
            }
        }else{
            //current gauge
            if(!isDevice) {
                if (newValue < -100f) {
                    mSOC = -100f;
                    mCurrentVauleDisplay = "-100A";
                } else if (newValue > 200f) {
                    mSOC = 200f;
                    mCurrentVauleDisplay = "200A";
                } else {
                    mSOC = newValue;
                    mCurrentVauleDisplay = Float.toString(round(mSOC, 1)) + "A";
                }
            }else {
                if (newValue < -3f) {
                    mSOC = -3f;
                    mCurrentVauleDisplay = "-3A";
                } else if (newValue > 3f) {
                    mSOC = 3f;
                    mCurrentVauleDisplay = "3A";
                } else {
                    mSOC = newValue;
                    mCurrentVauleDisplay = Float.toString(round(mSOC, 1)) + "A";
                }
            }

        }
        if(mSOC >= maxTick || mSOC<=minTick){
            zoomInAndOut(zoomIn);
        }
//
        animator = ValueAnimator.ofFloat(previousValue, mSOC);
        animator.setDuration(800);
        animator.setStartDelay(50);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                    mSOC = value;
                    ChargerGaugeView.this.invalidate();
            }
        });
        animator.start();
    }

    public void setmSOF(float newValue) {
        float previousValue = mSOF;
            if (newValue < 0f) {
                mSOF = 0.0f;
                //mCurrentVauleDisplay = "0.0%";
            } else if (newValue > 100f) {
                mSOF = 100f;
                //mCurrentVauleDisplay = "100.0%";
            } else {
                mSOF = newValue;
               //mCurrentVauleDisplay = Float.toString(round(mSOC,1)) + "%";
            }

        if(mSOF >= 0 && mSOF<30){
            needleColor = Color.RED;
        }
        else if(mSOF >= 30 && mSOF<80){
            needleColor = Color.parseColor("#DC7633");
        }
        else {
            needleColor = Color.GRAY;
        }


        animator = ValueAnimator.ofFloat(previousValue, mSOF);
        animator.setDuration(800);
        animator.setStartDelay(50);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float value = (float) animation.getAnimatedValue();
                mSOF = value;
                ChargerGaugeView.this.invalidate();
            }
        });
        animator.start();
    }

    public String getmCurrentVauleDisplay() {
        return mCurrentVauleDisplay;
    }

    public void setmCurrentVauleDisplay(String mCurrentVauleDisplay) {
        this.mCurrentVauleDisplay = mCurrentVauleDisplay;
    }



    public ChargerGaugeView(Context context) {
        super(context);
        //ChargerGauge.context = context;
        mDetector = new GestureDetector(ChargerGaugeView.this.getContext(), new ChargerGaugeView.GestureListener());
        init(null, 0);
    }

    public ChargerGaugeView(Context context, AttributeSet attrs) {
        super(context, attrs);
        //ChargerGauge.context = context;
        mDetector = new GestureDetector(ChargerGaugeView.this.getContext(), new ChargerGaugeView.GestureListener());
        init(attrs, 0);
    }

    public ChargerGaugeView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        //ChargerGauge.context = context;
        mDetector = new GestureDetector(ChargerGaugeView.this.getContext(), new ChargerGaugeView.GestureListener());
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.ChargerGaugeView, defStyle, 0);
        //this.setLayerType(View.LAYER_TYPE_HARDWARE,null);
        a.recycle();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(!isDevice) {
            ChargerGauge.drawChargerGauge(canvas, new RectF(0, 0, getWidth(), getHeight()), ChargerGauge.ResizingBehavior.AspectFit, startColor, endColor, needleColor, mCurrentVauleDisplay,
                    minTick, maxTick, mSOC, ticks[0], ticks[1], ticks[2], ticks[3], ticks[4], ticks[5], ticks[6], ticks[7], ticks[8], ticks[9], ticks[10], mSOF, oneNeedle);
        }else{
            ChargerGauge.drawChargerGauge(canvas, new RectF(0, 0, getWidth(), getHeight()), ChargerGauge.ResizingBehavior.AspectFit, startColor, endColor, needleColor, mCurrentVauleDisplay,
                    minTick, maxTick, mSOC, ticks[0], ticks[1], ticks[2], ticks[3], ticks[4], ticks[5], ticks[6], ticks[7], ticks[8], ticks[9], ticks[10], mSOF, true);
        }

    }



    public  void zoomInAndOut(boolean isZoomIn){
        int max = 0;
        int min = 0;
        int mCurrent = (int) mSOC;
        if(isZoomIn){
            oneNeedle = true;
            if(isChargerGauge){
                if(mCurrent<=95&&mCurrent>=5) {
                    min = mCurrent - mCurrent % 10 - 5;
                    max = min + 10;
                }
                else if(mCurrent<5){
                    min = 0;
                    max = 10;
                }
                else{
                    min = 90;
                    max = 100;
                }
                for(int i = 0;i<=10;i++){
                    ticks[i] = Integer.toString(min+i);
                }
            }else{
                if(!isDevice) {
                    if (mCurrent <= 80 && mCurrent >= -80) {
                        min = mCurrent - mCurrent % 10 - 10;
                        max = min + 20;
                    } else if (mCurrent > 80) {
                        min = 80;
                        max = 100;
                    } else {
                        min = -100;
                        max = -80;
                    }
                    for (int i = 0; i <= 10; i++) {
                        ticks[i] = Integer.toString(min + i * 2);
                    }
                }else{
                    if (mCurrent <= 2 && mCurrent >= -2) {
                        min = mCurrent-1;
                        max = min + 2;
                    } else if (mCurrent > 2) {
                        min = 1;
                        max = 3;
                    } else {
                        min = -2;
                        max = 0;
                    }
                    for (int i = 0; i <= 10; i++) {
                        ticks[i] = String.format("%.1f",min + i * 0.2f);
                    }

                }
            }
            zoomIn = true;
            maxTick = (float) max;
            minTick = (float) min;

            if(isChargerGauge) {
                if (min >= 50) {
                    startColor = endColor = Color.GREEN;
                } else if (max<=50) {
                    startColor = endColor = Color.RED;
                }else{
                    startColor = Color.RED;
                    endColor = Color.GREEN;
                }
            }else{
                if (min >= 0) {
                    startColor = endColor = Color.GREEN;
                } else if (max<=0) {
                    startColor = endColor = Color.RED;
                }else{
                    startColor = Color.RED;
                    endColor = Color.GREEN;
                }

            }


        }else{
            if(isChargerGauge){
                oneNeedle = false;
                maxTick = 100f;
                minTick = 0f;
                for(int i = 0;i<=10;i++){
                    ticks[i] = Integer.toString(10*i);
                }
            }
            else{
                maxTick = 100f;
                minTick = -100f;
                for(int i = 0;i<=10;i++){
                    ticks[i] = Integer.toString((int)minTick+20*i);
                }
            }
            zoomIn = false;
            startColor = Color.RED;
            endColor = Color.GREEN;
        }
        invalidate();
    }

    public void setChargerGauge(boolean chargerGauge){
        mSOC = 0.0f;
        if(chargerGauge == true){
            oneNeedle = false;
            isChargerGauge = true;
            maxTick = 100f;
            minTick = 0f;
            for(int i = 0;i<=10;i++){
                ticks[i] = Integer.toString(10*i);
            }
            mCurrentVauleDisplay  =Float.toString(round(mSOC,1))+"%";
        }
        else{
            if(!isDevice) {
                isChargerGauge = false;
                oneNeedle = true;
                maxTick = 100f;
                minTick = -100f;
                for (int i = 0; i <= 10; i++) {
                    ticks[i] = Integer.toString((int) minTick + 20 * i);
                }
                mCurrentVauleDisplay = Float.toString(round(mSOC, 1)) + "A";
            }else {
                isChargerGauge = false;
                oneNeedle = true;
                maxTick = 3f;
                minTick = -3f;
                for (int i = 0; i <= 10; i++) {
                    ticks[i] = String.format("%.1f",minTick + 0.6f * i);
                }
                mCurrentVauleDisplay = Float.toString(round(mSOC, 1)) + "A";
            }
        }
        zoomIn = false;
        startColor = Color.RED;
        endColor = Color.GREEN;
        invalidate();
    }

    private void startSwipeAnimation(){
        ViewAnimator
                .animate(this)
                .translationX(0,-500)
                .alpha(1,0)
                .duration(1000)
                .onStop(new AnimationListener.Stop() {
                    @Override
                    public void onStop(){
                        isChargerGauge = !isChargerGauge;
                        getChargeListener().onGaugeSwipe(isChargerGauge);
                        setChargerGauge(isChargerGauge);

                    }
                })
                .thenAnimate(this)
                .translationX(-500,0)
                .alpha(0,1)
                .duration(1000)
                .start();
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
                      zoomInAndOut(zoomIn);
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
                zoomIn = !zoomIn;
                startAnimation();
            //setSpeed(speed,true);
            return true;
        }


        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            startSwipeAnimation();
            return true;
        }


    }

    public interface ChargeGaugeListener{
        void onGaugeSwipe(boolean type);
    }


    public static float round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        float f = bd.floatValue();
        return f;
    }
}