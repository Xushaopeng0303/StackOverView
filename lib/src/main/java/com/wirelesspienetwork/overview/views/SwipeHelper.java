package com.wirelesspienetwork.overview.views;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.wirelesspienetwork.overview.misc.Configuration;

/**
 * This class facilitates swipe to dismiss. It defines an interface to be implemented by the
 * by the class hosting the views that need to swiped, and, using this interface, handles touch
 * events and translates / fades / animates the view as it is dismissed.
 */
class SwipeHelper {
    private static final boolean ALLOW_SWIPE_TOWARDS_START = true;
    private static final boolean ALLOW_SWIPE_TOWARDS_END = true;


    static final int X = 0;
    private static final int Y = 1;

    private static LinearInterpolator sLinearInterpolator = new LinearInterpolator();

    private static final float SWIPE_ESCAPE_VELOCITY = 500f; // dp/sec
    private static final int DEFAULT_ESCAPE_ANIMATION_DURATION = 75; // ms
    private static final int MAX_ESCAPE_ANIMATION_DURATION = 150; // ms
    private static final int MAX_DISMISS_VELOCITY = 2000; // dp/sec
    private static final int SWIPE_BACK_ANIM_DURATION = 200; // ms

    // 左右滑动消失时起始Alpha值和终止Alpha值
    private static final float ALPHA_FADE_START = 0.15f;
    private static final float ALPHA_FADE_END = 0.85f;

    private float mPagingTouchSlop;
    private Callback mCallback;
    private int mSwipeDirection;
    private VelocityTracker mVelocityTracker;

    private float mInitialTouchPos;
    private boolean mDragging;

    private View mCurrView;
    private float mDensityScale;

    private Configuration mConfig;

    SwipeHelper(int swipeDirection, Callback callback, float densityScale, float pagingTouchSlop, Configuration config) {
        mCallback = callback;
        mSwipeDirection = swipeDirection;
        mVelocityTracker = VelocityTracker.obtain();
        mDensityScale = densityScale;
        mPagingTouchSlop = pagingTouchSlop;
        mConfig = config;
    }

    private float getPos(MotionEvent ev) {
        return mSwipeDirection == X ? ev.getX() : ev.getY();
    }

    private float getTranslation(View v) {
        return mSwipeDirection == X ? v.getTranslationX() : v.getTranslationY();
    }

    private float getVelocity(VelocityTracker vt) {
        return mSwipeDirection == X ? vt.getXVelocity() :
                vt.getYVelocity();
    }

    private ObjectAnimator createTranslationAnimation(View v, float newPos) {
        return ObjectAnimator.ofFloat(v, mSwipeDirection == X ? View.TRANSLATION_X : View.TRANSLATION_Y, newPos);
    }

    private float getPerpendicularVelocity(VelocityTracker vt) {
        return mSwipeDirection == X ? vt.getYVelocity() : vt.getXVelocity();
    }

    private void setTranslation(View v, float translate) {
        if (mSwipeDirection == X) {
            v.setTranslationX(translate);
        } else {
            v.setTranslationY(translate);
        }
    }

    private float getSize(View v) {
        final DisplayMetrics dm = v.getContext().getResources().getDisplayMetrics();
        return mSwipeDirection == X ? dm.widthPixels : dm.heightPixels;
    }

    private float getAlphaForOffset(View view) {
        float viewSize = getSize(view);
        final float fadeSize = ALPHA_FADE_END * viewSize;
        float result = 1.0f;
        float pos = getTranslation(view);
        if (pos >= viewSize * ALPHA_FADE_START) {
            result = 1.0f - (pos - viewSize * ALPHA_FADE_START) / fadeSize;
        } else if (pos < viewSize * -ALPHA_FADE_START) {
            result = 1.0f + (viewSize * ALPHA_FADE_START + pos) / fadeSize;
        }
        result = Math.min(result, 1.0f);
        result = Math.max(result, 0f);
        return Math.max(0f, result);
    }

    boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();

        switch (action) {
            case MotionEvent.ACTION_DOWN:
                mDragging = false;
                mCurrView = mCallback.getChildAtPosition(ev);
                mVelocityTracker.clear();
                if (mCurrView != null) {
                    mVelocityTracker.addMovement(ev);
                    mInitialTouchPos = getPos(ev);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mCurrView != null) {
                    mVelocityTracker.addMovement(ev);
                    float pos = getPos(ev);
                    float delta = pos - mInitialTouchPos;
                    if (Math.abs(delta) > mPagingTouchSlop) {
                        mCallback.onBeginDrag(mCurrView);
                        mDragging = true;
                        mInitialTouchPos = pos - getTranslation(mCurrView);
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                mDragging = false;
                mCurrView = null;
                break;
        }
        return mDragging;
    }

    /**
     * @param view The view to be dismissed
     * @param velocity The desired pixels/second speed at which the view should move
     */
    private void dismissChild(final View view, float velocity) {
        final boolean canAnimViewBeDismissed = mCallback.canChildBeDismissed(view);
        float newPos;
        if (velocity < 0
                || (velocity == 0 && getTranslation(view) < 0)
                // if we use the Menu to dismiss an item in landscape, animate up
                || (velocity == 0 && getTranslation(view) == 0 && mSwipeDirection == Y)) {
            newPos = -getSize(view);
        } else {
            newPos = getSize(view);
        }
        int duration = MAX_ESCAPE_ANIMATION_DURATION;
        if (velocity != 0) {
            duration = Math.min(duration, (int) (Math.abs(newPos - getTranslation(view)) * 1000f / Math.abs(velocity)));
        } else {
            duration = DEFAULT_ESCAPE_ANIMATION_DURATION;
        }

        ValueAnimator anim = createTranslationAnimation(view, newPos);
        anim.setInterpolator(sLinearInterpolator);
        anim.setDuration(duration);
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mCallback.onChildDismissed(view);
                if (canAnimViewBeDismissed) {
                    view.setAlpha(1.f);
                }
            }
        });
        anim.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if ( canAnimViewBeDismissed) {
                    view.setAlpha(getAlphaForOffset(view));
                }
            }
        });
        anim.start();
    }

    private void snapChild(final View view) {
        final boolean canAnimViewBeDismissed = mCallback.canChildBeDismissed(view);
        ValueAnimator anim = createTranslationAnimation(view, 0);
        int duration = SWIPE_BACK_ANIM_DURATION;
        anim.setDuration(duration);
        anim.setInterpolator(mConfig.linearOutSlowInInterpolator);
        anim.addUpdateListener(new AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if (canAnimViewBeDismissed) {
                    view.setAlpha(getAlphaForOffset(view));
                }
                mCallback.onSwipeChanged(mCurrView, view.getTranslationX());
            }
        });
        anim.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (canAnimViewBeDismissed) {
                    view.setAlpha(1.0f);
                }
                mCallback.onSnapBackCompleted(view);
            }
        });
        anim.start();
    }

    boolean onTouchEvent(MotionEvent ev) {
        if (!mDragging) {
            if (!onInterceptTouchEvent(ev)) {
                return mCurrView != null;
            }
        }

        mVelocityTracker.addMovement(ev);
        final int action = ev.getAction();
        switch (action) {
            case MotionEvent.ACTION_OUTSIDE:
            case MotionEvent.ACTION_MOVE:
                if (mCurrView != null) {
                    float delta = getPos(ev) - mInitialTouchPos;
                    setSwipeAmount(delta);
                    mCallback.onSwipeChanged(mCurrView, delta);
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (mCurrView != null) {
                    endSwipe(mVelocityTracker);
                }
                break;
        }
        return true;
    }

    private void setSwipeAmount(float amount) {
        // don't let items that can't be dismissed be dragged more than
        // maxScrollDistance
        if (!isValidSwipeDirection(amount)) {
            float size = getSize(mCurrView);
            float maxScrollDistance = 0.15f * size;
            if (Math.abs(amount) >= size) {
                amount = amount > 0 ? maxScrollDistance : -maxScrollDistance;
            } else {
                amount = maxScrollDistance * (float) Math.sin((amount/size)*(Math.PI/2));
            }
        }
        setTranslation(mCurrView, amount);
        float alpha = getAlphaForOffset(mCurrView);
        mCurrView.setAlpha(alpha);
    }

    private boolean isValidSwipeDirection(float amount) {
        if (mSwipeDirection == X) {
            return (amount <= 0) ? ALLOW_SWIPE_TOWARDS_START : ALLOW_SWIPE_TOWARDS_END;
        }

        // Vertical swipes are always valid.
        return true;
    }

    private void endSwipe(VelocityTracker velocityTracker) {
        float maxVelocity = MAX_DISMISS_VELOCITY * mDensityScale;
        velocityTracker.computeCurrentVelocity(1000 /* px/sec */, maxVelocity);
        float velocity = getVelocity(velocityTracker);
        float perpendicularVelocity = getPerpendicularVelocity(velocityTracker);
        float escapeVelocity = SWIPE_ESCAPE_VELOCITY * mDensityScale;
        float translation = getTranslation(mCurrView);
        // Decide whether to dismiss the current view
        boolean childSwipedFarEnough = Math.abs(translation) > 0.6 * getSize(mCurrView);
        boolean childSwipedFastEnough = (Math.abs(velocity) > escapeVelocity) &&
                (Math.abs(velocity) > Math.abs(perpendicularVelocity)) &&
                (velocity > 0) == (translation > 0);

        boolean dismissChild = mCallback.canChildBeDismissed(mCurrView)
                && isValidSwipeDirection(translation)
                && (childSwipedFastEnough || childSwipedFarEnough);

        if (dismissChild) {
            dismissChild(mCurrView, childSwipedFastEnough ? velocity : 0f);
        } else {
            mCallback.onDragCancelled(mCurrView);
            snapChild(mCurrView);
        }
    }

    interface Callback {
        View getChildAtPosition(MotionEvent ev);

        boolean canChildBeDismissed(View v);

        void onBeginDrag(View v);

        void onSwipeChanged(View v, float delta);

        void onChildDismissed(View v);

        void onSnapBackCompleted(View v);

        void onDragCancelled(View v);
    }
}
