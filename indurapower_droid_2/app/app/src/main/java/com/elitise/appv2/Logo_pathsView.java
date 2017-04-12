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
import android.view.View;

/**
 * TODO: document your custom view class.
 */
public class Logo_pathsView extends View {

    private String mVersion = "1.0.0";

    public Logo_pathsView(Context context) {
        super(context);
        init(null, 0);
    }

    public Logo_pathsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public Logo_pathsView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.Logo_pathsView, defStyle, 0);

        a.recycle();


    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Logo_paths.drawCanvas1(canvas,new RectF(getWidth()*.2f,getHeight()*.2f,getWidth()*.8f,getHeight()*.8f), Logo_paths.ResizingBehavior.AspectFit,mVersion);
        //Logo_paths.drawCanvas1(canvas,mVersion);
    }

    public void setVersion(String version){
        mVersion = version;
        invalidate();
    }

}
