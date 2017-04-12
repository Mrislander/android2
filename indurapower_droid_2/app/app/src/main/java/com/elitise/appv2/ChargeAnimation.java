package com.elitise.appv2;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathDashPathEffect;
import android.graphics.PathEffect;
import android.graphics.RectF;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;



import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by andy on 6/14/16.
 */
public class ChargeAnimation extends View{
    private Paint mPaint;
    private Paint mTextPaint;
    private Paint maskPaint;
    private Timer mTimer;
    private TimerTask mTimerTask;

    private float phase = 1;
    private int engineFrameIdx = 1;


    private Bitmap mMask;
    private Bitmap alternator;

    private Bitmap engineFrame1;
    private Bitmap engineFrame2;
    private Bitmap engineFrame3;
    private Bitmap engineFrame4;
    private Bitmap engineFrame5;
    private Bitmap engineFrame6;
    private Bitmap engineFrame7;
    private Bitmap engineFrame8;
    private Bitmap engineFrame9;
    private Bitmap engineFrame10;

    private Bitmap motoEngine;

    private UserData mUser = UserData.getInstance();


    private BatteryData mBD = BatteryData.getInstance();


    private float mCurrent = 0.0f;


    public ChargeAnimation(Context context) {
        super(context);
        initPaint();

    }

    public ChargeAnimation(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPaint();
    }



    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);

        drawBattery(canvas);

        drawGauge(canvas);

//        if(mCurrent>0) {
//            if(mCurrent < 5)
//                phase+=1;
//            else{
//                phase+=mCurrent;
//            }
//        }
//        else if(mCurrent<0) {
//            if(Math.abs(mCurrent)<5)
//                phase-=1;
//            else{
//                phase+=mCurrent;
//            }
//        }
//        else {
//            if(mCurrent < 5)
//                phase+=10;
//            else{
//                phase+=mCurrent;
//            }
//        }
//
        if(Math.abs(mCurrent)>1){

            if(Math.abs(mCurrent)>40) {
                if(mCurrent>0) {
                    phase += 40;
                }
                else {
                    phase -= 40;
                }
            }
            else {
                phase+=mCurrent;
            }

        }
       // phase+=30;


        drawCable(canvas,phase);



    }



    private void drawBattery(Canvas canvas){
        final int canvasWidth = canvas.getWidth() - getPaddingLeft() - getPaddingRight();
        final int canvasHeight = canvas.getHeight() - getPaddingTop() - getPaddingBottom();

        float bodyTopX = (float) (canvasWidth*.25);
        float bodyTopY =(float) (canvasHeight*.7);
        float bodyBottomX = (float) (canvasWidth*.75);
        float bodyBottomY =(float) (canvasHeight*.9);

        float posTopX = (float) (canvasWidth*.3);
        float posTopY =(float) (canvasHeight*.67);
        float posBottomX = (float) (canvasWidth*.35);
        float posBottomY =(float) (canvasHeight*.7);


        float negTopX = (float) (canvasWidth*.65);
        float negTopY =(float) (canvasHeight*.67);
        float negBottomX = (float) (canvasWidth*.7);
        float negBottomY =(float) (canvasHeight*.7);

        Bitmap mask = Bitmap.createScaledBitmap(mMask, (int) (canvasWidth*0.3), (int) (canvasHeight*.05), true);
        canvas.drawBitmap(mask, (float)(canvasWidth*.35),(float)(canvasHeight*.8), maskPaint);

        RectF body = new RectF(bodyTopX,bodyTopY,bodyBottomX,bodyBottomY);
        RectF positive = new RectF(posTopX,posTopY,posBottomX,posBottomY);
        RectF negative = new RectF(negTopX,negTopY,negBottomX,negBottomY);

        canvas.drawRoundRect(body,dp2px(10),dp2px(10),mPaint);
        canvas.drawRoundRect(positive,dp2px(2),dp2px(2),mPaint);
        canvas.drawRoundRect(negative,dp2px(2),dp2px(2),mPaint);

        float textposX = (float)(canvasWidth*.325);
        float textposY = (float)(canvasHeight*.77);
        float textnegX = (float)(canvasWidth*.675);
        float textnegY = (float)(canvasHeight*.77);

        canvas.drawText("+",textposX,textposY,mTextPaint);
        canvas.drawText("—",textnegX,textnegY, mTextPaint);
        if(mBD.getM_I_CHGR()>0){
            mCurrent = mBD.getM_I_CHGR();
        }
        else {
            mCurrent =mBD.getM_C_MON_amps();
        }

        canvas.drawText(String.format("%.1f",mCurrent),(float)(canvasWidth*.5),textnegY, mTextPaint);

    }

    private void drawCable(Canvas canvas,float phase){
        final int canvasWidth = canvas.getWidth() - getPaddingLeft() - getPaddingRight();
        final int canvasHeight = canvas.getHeight() - getPaddingTop() - getPaddingBottom();
        Paint mCablePaint = new Paint();
        Paint mCirclePaint = new Paint();
        Path path = new Path();
        Path circle = new Path();




        float centerX = canvasWidth*.5f;
        float centerY = canvasHeight*.16f;
        float r=canvasHeight*.14f;
        float k = 0.5519f;




        path.moveTo(canvasWidth*.5f,canvasHeight*.65f);
        path.lineTo(canvasWidth*.5f,canvasHeight*.3f);

        circle.moveTo(centerX,centerY+r);
        circle.cubicTo(centerX-k*r,centerY+r,
                centerX-r,centerY+k*r,
                centerX-r,centerY);
        circle.cubicTo(centerX-r,centerY-r*k,
                centerX-r*k,canvasHeight*.02f,
                centerX,canvasHeight*.02f);
        circle.cubicTo(centerX+r*k,canvasHeight*.02f,
                centerX+r,centerY-r*k,
                centerX+r,centerY);
        circle.cubicTo(centerX+r,centerY+r*k,
                centerX+r*k,centerY+r,
                centerX,centerY+r);




        if(mCurrent>0) {
            float radius = Math.abs(mCurrent);
            if(radius<5)
                radius = 5;
            if(radius>40)
                radius = 40;
            PathEffect mPathEffect = new PathDashPathEffect(
                    makeCircle(dp2px((int)radius)),
                    dp2px(200),
                    phase,
                    PathDashPathEffect.Style.ROTATE);

            PathEffect mCircleEffect = new PathDashPathEffect(
                    makeConvexArrowReverse(dp2px(15), dp2px(12)),
                    dp2px(50),
                    phase,
                    PathDashPathEffect.Style.ROTATE);

            mCablePaint.setPathEffect(mPathEffect);
            mCablePaint.setColor(Color.GREEN);
            mCablePaint.setStrokeWidth(dp2px(30));
            mCablePaint.setShadowLayer(dp2px(8), 0, 0, Color.WHITE);
            mCablePaint.setStyle(Paint.Style.FILL_AND_STROKE);
            mCablePaint.setStrokeJoin(Paint.Join.ROUND);
            mCablePaint.setStrokeCap(Paint.Cap.ROUND);
            mCablePaint.setAntiAlias(true);
            mCablePaint.setDither(true);

            mCirclePaint.setPathEffect(mCircleEffect);
            mCirclePaint.setColor(Color.GREEN);
            mCirclePaint.setStrokeWidth(dp2px(15));
            mCirclePaint.setShadowLayer(dp2px(8), 0, 0, Color.WHITE);
            mCirclePaint.setStyle(Paint.Style.FILL_AND_STROKE);
            mCirclePaint.setStrokeJoin(Paint.Join.ROUND);
            mCirclePaint.setStrokeCap(Paint.Cap.ROUND);
            mCirclePaint.setAntiAlias(true);
            mCirclePaint.setDither(true);

        }
        else if(mCurrent<0){
            float radius = Math.abs(mCurrent);
            if(radius<5)
                radius = 5;
            if(radius>40)
                radius = 40;

            PathEffect mPathEffect = new PathDashPathEffect(
                    makeCircle(dp2px((int)radius)),
                    dp2px(200),
                    phase,
                    PathDashPathEffect.Style.ROTATE);
            PathEffect mCircleEffect = new PathDashPathEffect(
                    makeConvexArrow(dp2px(15), dp2px(12)),
                    dp2px(50),
                    phase,
                    PathDashPathEffect.Style.ROTATE);
            mCablePaint.setPathEffect(mPathEffect);
            mCablePaint.setColor(Color.parseColor("#ff6529"));
            mCablePaint.setStrokeWidth(dp2px(30));
            mCablePaint.setShadowLayer(dp2px(8), 0, 0, Color.WHITE);
            mCablePaint.setStyle(Paint.Style.FILL_AND_STROKE);
            mCablePaint.setStrokeJoin(Paint.Join.ROUND);
            mCablePaint.setStrokeCap(Paint.Cap.ROUND);
            mCablePaint.setAntiAlias(true);
            mCablePaint.setDither(true);


            mCirclePaint.setPathEffect(mCircleEffect);
            mCirclePaint.setColor(Color.parseColor("#ff6529"));
            mCirclePaint.setStrokeWidth(dp2px(15));
            mCirclePaint.setShadowLayer(dp2px(8), 0, 0, Color.WHITE);
            mCirclePaint.setStyle(Paint.Style.FILL_AND_STROKE);
            mCirclePaint.setStrokeJoin(Paint.Join.ROUND);
            mCirclePaint.setStrokeCap(Paint.Cap.ROUND);
            mCirclePaint.setAntiAlias(true);
            mCirclePaint.setDither(true);
        }
        else {

            PathEffect mPathEffect = new PathDashPathEffect(
                    makeCircle(dp2px(20)),
                    dp2px(200),
                    phase,
                    PathDashPathEffect.Style.ROTATE);
            PathEffect mCircleEffect = new PathDashPathEffect(
                    makeConvexArrowReverse(dp2px(15), dp2px(10)),
                    dp2px(50),
                    phase,
                    PathDashPathEffect.Style.ROTATE);

            mCablePaint.setPathEffect(mPathEffect);
            mCablePaint.setColor(Color.GREEN);
            mCablePaint.setStrokeWidth(dp2px(30));
            mCablePaint.setShadowLayer(dp2px(8), 0, 0, Color.WHITE);
            mCablePaint.setStyle(Paint.Style.FILL_AND_STROKE);
            mCablePaint.setStrokeJoin(Paint.Join.ROUND);
            mCablePaint.setStrokeCap(Paint.Cap.ROUND);
            mCablePaint.setAntiAlias(true);
            mCablePaint.setDither(true);

            mCirclePaint.setPathEffect(mCircleEffect);
            mCirclePaint.setColor(Color.GREEN);
            mCirclePaint.setStrokeWidth(dp2px(15));
            mCirclePaint.setShadowLayer(dp2px(8), 0, 0, Color.WHITE);
            mCirclePaint.setStyle(Paint.Style.FILL_AND_STROKE);
            mCirclePaint.setStrokeJoin(Paint.Join.ROUND);
            mCirclePaint.setStrokeCap(Paint.Cap.ROUND);
            mCirclePaint.setAntiAlias(true);
            mCirclePaint.setDither(true);
        }

        Path pathBG = new Path();
        Paint pathBGPaint = new Paint();
        pathBG.moveTo(canvasWidth*.5f,canvasHeight*.3f);
        pathBG.lineTo(canvasWidth*.5f,canvasHeight*.7f+mPaint.ascent());
        pathBGPaint.setStyle(Paint.Style.STROKE);
        pathBGPaint.setColor(Color.GRAY);
        pathBGPaint.setStrokeWidth(dp2px(15));

        canvas.drawPath(pathBG,pathBGPaint);
        canvas.drawPath(path,mCablePaint);
        canvas.drawPath(circle,mCirclePaint);

    }


    private Path makeConvexArrow(float length,float height){
        Path p = new Path();
        p.moveTo(0.0f,-height/2.0f);
        p.lineTo(length - height/4.0f, -height/2.0f);
        p.lineTo(length,0.0f);
        p.lineTo(length-height/4.0f,height/2.0f);
        p.lineTo(0.0f,height/2.0f);
        p.lineTo(0.0f+height/4.0f,0.0f);
        p.close();
        return p;
    }

    private Path makeConvexArrowReverse(float length,float height){
        Path p = new Path();
        p.moveTo(0.0f+height/4.0f,-height/2.0f);
        p.lineTo(length, -height/2.0f);
        p.lineTo(length-height/4.0f,0.0f);
        p.lineTo(length,height/2.0f);
        p.lineTo(0.0f+height/4.0f,height/2.0f);
        p.lineTo(0.0f,0.0f);
        p.close();
        return p;
    }
    private Path makeCircle(float r){
        float k = 0.5519f;
        Path p = new Path();
        p.moveTo(0.0f,0.0f);
        p.cubicTo(0.0f,-k*r,
                r-r*k,-r,
                r,-r);
        p.cubicTo(r+r*k,-r,
                2*r,-r*k,
                2*r,0f);
        p.cubicTo(2*r,r*k,
                r+r*k,r,
                r,r);
        p.cubicTo(r-r*k,r,
                0f,r*k,
                0f,0f);
        p.close();
        return p;
    }






    private void drawGauge(Canvas canvas) {
        final int canvasWidth = canvas.getWidth() - getPaddingLeft() - getPaddingRight();
        final int canvasHeight = canvas.getHeight() - getPaddingTop() - getPaddingBottom();

        //canvas.drawCircle(canvasWidth*.5f,canvasHeight*.15f,canvasHeight*.15f,backgroundPaint);
        //canvas.drawCircle(canvasWidth*.5f,canvasHeight*.15f,canvasHeight*.13f,backgroundInnerPaint);
        if(mCurrent>=0) {
            Bitmap mask = Bitmap.createScaledBitmap(alternator, (int) (canvasHeight * .20f), (int) (canvasHeight * .20f), true);
            canvas.drawBitmap(mask, canvasWidth * .5f - canvasHeight * .20f / 2, canvasHeight * .16f - canvasHeight * .20f / 2, maskPaint);
        }
        else{
            if(mUser.getCurrentIdx() != 3 && mUser.getCurrentIdx() != 7) {
                if (engineFrameIdx == 10) {
                    engineFrameIdx = 1;
                } else {
                    engineFrameIdx++;
                }
                Bitmap mask = Bitmap.createScaledBitmap(engineFrame(engineFrameIdx), (int) (canvasHeight * .20f), (int) (canvasHeight * .20f), true);
                canvas.drawBitmap(mask, canvasWidth * .5f - canvasHeight * .20f / 2, canvasHeight * .16f - canvasHeight * .20f / 2, maskPaint);
            }
            else {
                Bitmap mask = Bitmap.createScaledBitmap(motoEngine, (int) (canvasHeight * .20f), (int) (canvasHeight * .20f), true);
                canvas.drawBitmap(mask, canvasWidth * .5f - canvasHeight * .20f / 2, canvasHeight * .16f - canvasHeight * .20f / 2, maskPaint);
            }

        }


//        canvas.drawText(unitsText, oval.centerX(), oval.centerY()/0.65f, unitsPaint);
//        if(isChargeGauge) {
//            String range = String.valueOf((int) low) + " — " + String.valueOf((int) high);
//            canvas.drawText(range, oval.centerX(), oval.centerY() / 0.65f + unitsPaint.descent() - unitsPaint.ascent(), unitsPaint);
//        }
//        else{
//            if(!mZoom){
//                // String range = String.valueOf((int) low) + " — " + String.valueOf((int) high);
//                canvas.drawText("-100 — 100", oval.centerX(), oval.centerY() / 0.65f + unitsPaint.descent() - unitsPaint.ascent(), unitsPaint);
//            }
//            else{
//                String range = String.valueOf((int) (low*2-100)) + " — " + String.valueOf((int) (high*2-100));
//                canvas.drawText(range, oval.centerX(), oval.centerY() / 0.65f + unitsPaint.descent() - unitsPaint.ascent(), unitsPaint);
//            }
//
//        }



    }



    private void initPaint(){
        mPaint = new Paint();
        mPaint.setColor(Color.parseColor("#ff6529"));
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(dp2px(8));

        mTextPaint = new Paint();
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setStyle(Paint.Style.FILL);
        mTextPaint.setTextAlign(Paint.Align.CENTER);
        mTextPaint.setTextSize(dp2px(30));


        mMask = BitmapFactory.decodeResource(getResources(), R.drawable.logo);
        mMask = Bitmap.createBitmap(mMask, 0, 0, mMask.getWidth(), mMask.getHeight());

        engineFrame1 =  BitmapFactory.decodeResource(getResources(), R.drawable.engineframe1);
        engineFrame1 = Bitmap.createBitmap(engineFrame1,0,0,engineFrame1.getWidth(),engineFrame1.getHeight());

        engineFrame2 =  BitmapFactory.decodeResource(getResources(), R.drawable.engineframe2);
        engineFrame2 = Bitmap.createBitmap(engineFrame1,0,0,engineFrame2.getWidth(),engineFrame2.getHeight());

        engineFrame3 =  BitmapFactory.decodeResource(getResources(), R.drawable.engineframe3);
        engineFrame3 = Bitmap.createBitmap(engineFrame3,0,0,engineFrame3.getWidth(),engineFrame3.getHeight());

        engineFrame4 =  BitmapFactory.decodeResource(getResources(), R.drawable.engineframe4);
        engineFrame4 = Bitmap.createBitmap(engineFrame4,0,0,engineFrame4.getWidth(),engineFrame4.getHeight());

        engineFrame5 =  BitmapFactory.decodeResource(getResources(), R.drawable.engineframe5);
        engineFrame5 = Bitmap.createBitmap(engineFrame5,0,0,engineFrame5.getWidth(),engineFrame5.getHeight());

        engineFrame6 =  BitmapFactory.decodeResource(getResources(), R.drawable.engineframe6);
        engineFrame6 = Bitmap.createBitmap(engineFrame6,0,0,engineFrame6.getWidth(),engineFrame6.getHeight());

        engineFrame7 =  BitmapFactory.decodeResource(getResources(), R.drawable.engineframe1);
        engineFrame7 = Bitmap.createBitmap(engineFrame7,0,0,engineFrame7.getWidth(),engineFrame7.getHeight());

        engineFrame8 =  BitmapFactory.decodeResource(getResources(), R.drawable.engineframe8);
        engineFrame8 = Bitmap.createBitmap(engineFrame8,0,0,engineFrame8.getWidth(),engineFrame8.getHeight());

        engineFrame9 =  BitmapFactory.decodeResource(getResources(), R.drawable.engineframe9);
        engineFrame9 = Bitmap.createBitmap(engineFrame9,0,0,engineFrame9.getWidth(),engineFrame9.getHeight());

        engineFrame10 =  BitmapFactory.decodeResource(getResources(), R.drawable.engineframe10);
        engineFrame10 = Bitmap.createBitmap(engineFrame10,0,0,engineFrame10.getWidth(),engineFrame10.getHeight());


        motoEngine =  BitmapFactory.decodeResource(getResources(), R.drawable.motorcycleengine);
        motoEngine = Bitmap.createBitmap(motoEngine,0,0,motoEngine.getWidth(),motoEngine.getHeight());

        alternator = BitmapFactory.decodeResource(getResources(), R.drawable.alternator);
        alternator = Bitmap.createBitmap(alternator, 0, 0, alternator.getWidth(), alternator.getHeight());

        maskPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        maskPaint.setDither(true);

        mTimer = new Timer();
        mTimerTask = new TimerTask() {
            @Override
            public void run() {
                postInvalidate();
            }
        };
        mTimer.schedule(mTimerTask,1,100);

    }
    private  int dp2px(int dp){
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,dp,getResources().getDisplayMetrics());
    }

    private Bitmap engineFrame(int idx){
        switch (idx){

            case 1:
                return engineFrame1;
            case 2:
                return engineFrame2;
            case 3:
                return engineFrame3;
            case 4:
                return engineFrame4;
            case 5:
                return engineFrame5;
            case 6:
                return engineFrame6;
            case 7:
                return engineFrame7;
            case 8:
                return engineFrame8;
            case 9:
                return engineFrame9;
            case 10:
                return engineFrame10;
            default:
                break;
        }
        return  null;
    }

}
