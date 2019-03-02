package com.mediapocket.android.view;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.ColorFilter;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.animation.AccelerateDecelerateInterpolator;

import java.util.LinkedList;


/**
 * @author Vlad Namashko
 */
public class AnimatedImageView extends AppCompatImageView {

    private static final int ZOOM_DURATION = 12000;

    private static final float INITIAL_CROP_COEF = .2f;
    private static final float FINAL_CROP_COEF = 1.4f;

    private int mFrameWidth;
    private int mFrameHeight;

    private ValueAnimator mCurrentAnimator;
    private float mImageScaleX;
    private float mImageScaleY;

    private boolean mIsRunning;
    private long mCurrentPlayTime;

    private LinkedList<AnimationMeta> mAnimations = new LinkedList<>();

    public AnimatedImageView(Context context) {
        super(context);
        setScaleType(ScaleType.MATRIX);
        setColorFilter(grayScaleColorFilter());
    }

    public AnimatedImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setScaleType(ScaleType.MATRIX);
        setColorFilter(grayScaleColorFilter());
    }

    public AnimatedImageView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setScaleType(ScaleType.MATRIX);
        setColorFilter(grayScaleColorFilter());
    }

    private static ColorFilter grayScaleColorFilter() {

        float[] colorMatrix = new float[]{0, 0, 0, 0, 0,
                0, 0, 0, 0, 0,
                0, 0, 1, 0, 0,
                0, 0, 0, 1, 0};

        ColorMatrix matrix = new ColorMatrix(colorMatrix);
        matrix.setSaturation(0);

        return new ColorMatrixColorFilter(matrix);

    }

    @Override
    protected boolean setFrame(int l, int t, int r, int b) {
        Drawable drawable = getDrawable();
        mFrameWidth = r - l;
        mFrameHeight = b - t;
        if (canTransform()) {

            calculateZoom();

            Matrix matrix = new Matrix();
            matrix.preScale(mImageScaleX + INITIAL_CROP_COEF, mImageScaleY + INITIAL_CROP_COEF);
            float x = drawable.getIntrinsicWidth() * INITIAL_CROP_COEF;
            float y = drawable.getIntrinsicHeight() * INITIAL_CROP_COEF;
            matrix.postTranslate(-x / 2.0f, -y / 2.0f);
            setImageMatrix(matrix);
        }
        return super.setFrame(l, t, r, b);
    }

    private void calculateZoom() {
        Drawable drawable = getDrawable();
        mImageScaleX = mFrameWidth * 1.0f / drawable.getIntrinsicWidth();
        mImageScaleY = mImageScaleX;
        if ((drawable.getIntrinsicHeight() * mImageScaleY) < mFrameHeight) {
            mImageScaleY = mFrameHeight * 1.0f / drawable.getIntrinsicHeight();
        }
    }

    private boolean canTransform() {
        Drawable drawable = getDrawable();
        return drawable != null && drawable.getIntrinsicWidth() > 0 && mFrameWidth > 0 && mFrameHeight > 0;
    }

    public void init(Matrix imageMatrix) {
        float[] matrixValues = new float[9];
        imageMatrix.getValues(matrixValues);

        if (canTransform()) {
            Matrix matrix = getMatrix();
            matrix.preScale(matrixValues[Matrix.MSCALE_X], matrixValues[Matrix.MSCALE_Y]);
            matrix.postTranslate(matrixValues[Matrix.MTRANS_X], matrixValues[Matrix.MTRANS_Y]);
            setImageMatrix(matrix);
        }
        invalidate();
    }

    @Override
    public void onLayout(boolean changed,
                         int left,
                         int top,
                         int right,
                         int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (mIsRunning) {
            mIsRunning = false;
            mCurrentAnimator.cancel();
            calculateZoom();
            initAnimation();
            startAnimation();
        }
    }

    public void startAnimation() {
        if (mIsRunning) {
            return;
        }

        mIsRunning = true;

        initAnimation();

        animateTo(getNextAnimation());
    }

    protected void initAnimation() {
        float[] matrixValues = new float[9];
        getImageMatrix().getValues(matrixValues);

        float width = getDrawable().getIntrinsicWidth();
        float height = getDrawable().getIntrinsicHeight();

        float finalScaleX = mImageScaleX + FINAL_CROP_COEF;
        float finalScaleY = mImageScaleY + FINAL_CROP_COEF;

        mAnimations.clear();
        mAnimations.add(new AnimationMeta(0, 0, finalScaleX, finalScaleY));
        mAnimations.add(new AnimationMeta(width * FINAL_CROP_COEF, 0, finalScaleX, finalScaleY));
        mAnimations.add(new AnimationMeta(0, height * FINAL_CROP_COEF, finalScaleX, finalScaleY, 14000));
        mAnimations.add(new AnimationMeta(width * FINAL_CROP_COEF, height * FINAL_CROP_COEF, finalScaleX, finalScaleY));
        mAnimations.add(new AnimationMeta(width * INITIAL_CROP_COEF, height * INITIAL_CROP_COEF, matrixValues[Matrix.MSCALE_X], matrixValues[Matrix.MSCALE_Y]));
    }

    private AnimationMeta getNextAnimation () {
        AnimationMeta m = mAnimations.pollFirst();
        mAnimations.add(m);
        return m;
    }
    
    private void animateTo(AnimationMeta animationMeta) {
        ValueAnimator animator = ValueAnimator.ofFloat(0, 1f)
                .setDuration(animationMeta.mDuration != 0 ? animationMeta.mDuration : ZOOM_DURATION);

        float[] matrixValues = new float[9];
        getImageMatrix().getValues(matrixValues);
        float scaleX = matrixValues[Matrix.MSCALE_X];
        float scaleY = matrixValues[Matrix.MSCALE_Y];
        float x = matrixValues[Matrix.MTRANS_X];
        float y = matrixValues[Matrix.MTRANS_Y];

        float dScaleX = animationMeta.mScaleX - scaleX;
        float dScaleY = animationMeta.mScaleY - scaleY;
        float dX = animationMeta.mX - Math.abs(x);
        float dY = animationMeta.mY - Math.abs(y);

        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(valueAnimator -> {
            float t = (float) valueAnimator.getAnimatedValue();
            if (canTransform()) {
                Matrix theMatrix = getMatrix();
                theMatrix.preScale(scaleX + dScaleX * t, scaleY + dScaleY * t);
                theMatrix.postTranslate(x - dX * t, y - dY * t);
                setImageMatrix(theMatrix);
            }

            invalidate();
        });
        mCurrentAnimator = animator;

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animator) {
                super.onAnimationEnd(animator);
                if (mIsRunning) {
                    animateTo(getNextAnimation());
                }
            }
        });

        animator.start();
    }

    public void resumeAnimation() {
        if (mCurrentAnimator != null) {
            mIsRunning = true;
            mCurrentAnimator.start();
            mCurrentAnimator.setCurrentPlayTime(mCurrentPlayTime);
        }
    }

    public void pauseAnimation() {
        if (mIsRunning && mCurrentAnimator != null) {
            mCurrentPlayTime = mCurrentAnimator.getCurrentPlayTime();
            mIsRunning = false;
            mCurrentAnimator.cancel();
        }
    }

    private class AnimationMeta {
        private float mX;
        private float mY;
        private float mScaleX;
        private float mScaleY;
        private int mDuration;

        private AnimationMeta(float x, float y, float scaleX, float scaleY, int duration) {
            mX = x;
            mY = y;
            mScaleX = scaleX;
            mScaleY = scaleY;
            mDuration = duration;
        }

        private AnimationMeta(float x, float y, float scaleX, float scaleY) {
            this(x, y, scaleX, scaleY, 0);
        }
    }
}
