package com.ashokvarma.bottomnavigation;

import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorCompat;
import android.support.v4.view.ViewPropertyAnimatorListener;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.lang.ref.WeakReference;

/**
 * Class description : Holds and manages data for badges
 * (i.e data structure which holds all data to paint a badge and updates badges when changes are made)
 *
 * @author ashokvarma
 * @version 1.0
 * @since 21 Apr 2016
 */
abstract class BadgeItem<T extends BadgeItem<T>> {

    private int mGravity = Gravity.TOP | Gravity.END;
    private boolean mHideOnSelect;

    private WeakReference<TextView> mTextViewRef;

    private boolean mIsHidden = false;

    private int mAnimationDuration = 200;

    ///////////////////////////////////////////////////////////////////////////
    // Public setter methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * @param gravity gravity of badge (TOP|LEFT ..etc)
     * @return this, to allow builder pattern
     */
    public T setGravity(int gravity) {
        this.mGravity = gravity;
        if (isWeakReferenceValid()) {
            TextView textView = mTextViewRef.get();
            FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) textView.getLayoutParams();
            layoutParams.gravity = gravity;
            textView.setLayoutParams(layoutParams);
        }
        return (T) this;
    }

    /**
     * @param hideOnSelect if true hides badge on tab selection
     * @return this, to allow builder pattern
     */
    public T setHideOnSelect(boolean hideOnSelect) {
        this.mHideOnSelect = hideOnSelect;
        return (T) this;
    }

    /**
     * @param animationDuration hide and show animation time
     * @return this, to allow builder pattern
     */
    public T setAnimationDuration(int animationDuration) {
        this.mAnimationDuration = animationDuration;
        return (T) this;
    }


    ///////////////////////////////////////////////////////////////////////////
    // Library only access method
    ///////////////////////////////////////////////////////////////////////////

    void bindToBottomTab(BadgeItem badgeItem, BottomNavigationTab bottomNavigationTab) {
        // set initial bindings
        bottomNavigationTab.setBadgeItem(badgeItem);
        badgeItem.setTextView(bottomNavigationTab.badgeView);

        // allow sub class to modify the things
        bindToBottomTabInternal((T) badgeItem, bottomNavigationTab);

        // make view visible because gone by default
        bottomNavigationTab.badgeView.setVisibility(View.VISIBLE);

        // set layout params based on gravity
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) bottomNavigationTab.badgeView.getLayoutParams();
        layoutParams.gravity = badgeItem.getGravity();
        bottomNavigationTab.badgeView.setLayoutParams(layoutParams);

        // if hidden hide
        if (badgeItem.isHidden()) {
            // if hide is called before the initialisation of bottom-bar this will handle that
            // by hiding it.
            badgeItem.hide();
        }
    }

    abstract void bindToBottomTabInternal(T badgeItem, BottomNavigationTab bottomNavigationTab);

    /**
     * Internal method used to update view when ever changes are made
     *
     * @param mTextView badge textView
     * @return this, to allow builder pattern
     */
    T setTextView(TextView mTextView) {
        this.mTextViewRef = new WeakReference<>(mTextView);
        return (T) this;
    }

    /**
     * @return gravity of badge
     */
    int getGravity() {
        return mGravity;
    }

    /**
     * @return should hide on selection ?
     */
    boolean isHideOnSelect() {
        return mHideOnSelect;
    }

    /**
     * @return reference to text-view
     */
    WeakReference<TextView> getTextView() {
        return mTextViewRef;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Internal Methods
    ///////////////////////////////////////////////////////////////////////////

    boolean isWeakReferenceValid() {
        return mTextViewRef != null && mTextViewRef.get() != null;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Internal call back methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * callback from bottom navigation tab when it is selected
     */
    void select() {
        if (mHideOnSelect) {
            hide(true);
        }
    }

    /**
     * callback from bottom navigation tab when it is un-selected
     */
    void unSelect() {
        if (mHideOnSelect) {
            show(true);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Public functionality methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * @return this, to allow builder pattern
     */
    public T toggle() {
        return toggle(true);
    }

    /**
     * @param animate whether to animate the change
     * @return this, to allow builder pattern
     */
    public T toggle(boolean animate) {
        if (mIsHidden) {
            return show(animate);
        } else {
            return hide(animate);
        }
    }

    /**
     * @return this, to allow builder pattern
     */
    public T show() {
        return show(true);
    }

    /**
     * @param animate whether to animate the change
     * @return this, to allow builder pattern
     */
    public T show(boolean animate) {
        mIsHidden = false;
        if (isWeakReferenceValid()) {
            TextView textView = mTextViewRef.get();
            if (animate) {
                textView.setScaleX(0);
                textView.setScaleY(0);
                textView.setVisibility(View.VISIBLE);
                ViewPropertyAnimatorCompat animatorCompat = ViewCompat.animate(textView);
                animatorCompat.cancel();
                animatorCompat.setDuration(mAnimationDuration);
                animatorCompat.scaleX(1).scaleY(1);
                animatorCompat.setListener(null);
                animatorCompat.start();
            } else {
                textView.setScaleX(1);
                textView.setScaleY(1);
                textView.setVisibility(View.VISIBLE);
            }
        }
        return (T) this;
    }

    /**
     * @return this, to allow builder pattern
     */
    public T hide() {
        return hide(true);
    }

    /**
     * @param animate whether to animate the change
     * @return this, to allow builder pattern
     */
    public T hide(boolean animate) {
        mIsHidden = true;
        if (isWeakReferenceValid()) {
            TextView textView = mTextViewRef.get();
            if (animate) {
                ViewPropertyAnimatorCompat animatorCompat = ViewCompat.animate(textView);
                animatorCompat.cancel();
                animatorCompat.setDuration(mAnimationDuration);
                animatorCompat.scaleX(0).scaleY(0);
                animatorCompat.setListener(new ViewPropertyAnimatorListener() {
                    @Override
                    public void onAnimationStart(View view) {
                        // Empty body
                    }

                    @Override
                    public void onAnimationEnd(View view) {
                        view.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationCancel(View view) {
                        view.setVisibility(View.GONE);
                    }
                });
                animatorCompat.start();
            } else {
                textView.setVisibility(View.GONE);
            }
        }
        return (T) this;
    }

    /**
     * @return if the badge is hidden
     */
    public boolean isHidden() {
        return mIsHidden;
    }
}
