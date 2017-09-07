package com.evilbeast.meizi.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.evilbeast.meizi.R;

/**
 * Author: sumary
 */
public class CircleImageView extends ImageView {

    // 自定义参数
    private int mBorderWidth;
    private int mBorderColor;

    // 默认
    private static final int DEFAULT_BORDER_WIDTH = 0;
    private static final int DEFAULT_BORDER_COLOR = Color.BLACK;

    // Bitmap config
    private Bitmap mBitmap;
    private BitmapShader mBitmapShader;
    private static final int COLORDRAWABLE_DIMENSION = 1;
    private static final Bitmap.Config BITMAP_CONFIG = Bitmap.Config.ARGB_8888;

    // Paint
    private final Paint mBitmapPaint = new Paint();
    private final Paint mBorderPaint = new Paint();
    private float mDrawableRadius;
    private float mBorderRadius;
    private final Matrix mShaderMatrix = new Matrix();


    public CircleImageView(Context context) {
        super(context);
    }

    public CircleImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CircleImageView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        this(context, attrs, defStyleAttr);
    }

    public CircleImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        // 获取用户设置的参数
        TypedArray attrArray = context.obtainStyledAttributes(attrs, R.styleable.CircleImageView, defStyleAttr, 0);
        mBorderWidth = attrArray.getDimensionPixelSize(R.styleable.CircleImageView_border_width, DEFAULT_BORDER_WIDTH);
        mBorderColor = attrArray.getColor(R.styleable.CircleImageView_border_color, DEFAULT_BORDER_COLOR);
        attrArray.recycle();

    }

    /**
     * 从drawable中获取bitmap
     */
    private Bitmap getBitmapFromDrawable() {
        Drawable drawable;
        drawable = getDrawable();
        if (drawable == null) {
            return null;
        }

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        Bitmap newBitmap;
        if (drawable instanceof ColorDrawable) {
            newBitmap = Bitmap.createBitmap(COLORDRAWABLE_DIMENSION,COLORDRAWABLE_DIMENSION, BITMAP_CONFIG);
        } else {
            newBitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), BITMAP_CONFIG);
        }

        Canvas canvas = new Canvas(newBitmap);
        drawable.setBounds(0, 0, canvas.getWidth(),canvas.getHeight());
        drawable.draw(canvas);
        return newBitmap;
    }

    private boolean setupShader() {
        mBitmap = getBitmapFromDrawable();
        if (mBitmap == null) {
            return false;
        }

        int bitmapWidth = mBitmap.getWidth();
        int bitmapHeight = mBitmap.getHeight();

        // init bitmapShader, 拉伸
        mBitmapShader = new BitmapShader(mBitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);

        // bitmap paint
        mBitmapPaint.setAntiAlias(true);
        mBitmapPaint.setShader(mBitmapShader);


        // border paint
        mBorderPaint.setAntiAlias(true);  // 抗锯齿
        mBorderPaint.setStyle(Paint.Style.STROKE); // 描边
        mBorderPaint.setColor(mBorderColor);
        mBorderPaint.setStrokeWidth(mBorderWidth);

        // radius
        mBorderRadius = Math.min((getWidth()- mBorderWidth)/2, (getHeight()-mBorderWidth)/2);
        mDrawableRadius = mBorderRadius - mBorderWidth / 2;

        updateShaderMatrix();

        return true;
    }

    private void updateShaderMatrix() {

        mShaderMatrix.set(null);
        int bSize = Math.min(mBitmap.getWidth(), mBitmap.getHeight());
        float scale = (getWidth() - mBorderWidth*2) * 1.0f / bSize;
        mShaderMatrix.setScale(scale,scale);
        mShaderMatrix.postTranslate(mBorderWidth, mBorderWidth);
        mBitmapShader.setLocalMatrix(mShaderMatrix);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if (!setupShader()) {
            return;
        }

        // draw bitmap;
        canvas.drawCircle(getWidth()/2, getHeight()/2, mDrawableRadius, mBitmapPaint);
        // draw border
        if (mBorderWidth > 0) {
            canvas.drawCircle(getWidth()/2, getHeight()/2, mBorderRadius, mBorderPaint);
        }
    }

    public void setBorderColor(int borderColor) {
       if (mBorderColor != borderColor) {
           mBorderColor = borderColor;
           invalidate();
       }
    }

    public int getBorderColor() {
        return mBorderColor;
    }

    public void setBorderWidth(int borderWidth) {
        if (mBorderWidth != borderWidth) {
            mBorderWidth = borderWidth;
            invalidate();
        }
    }
}
