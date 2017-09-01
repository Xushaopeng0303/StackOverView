package com.wirelesspienetwork.overview.misc;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;

import com.wirelesspienetwork.overview.R;

public class Configuration {

    /**
     * Interpolator
     */
    public Interpolator fastOutSlowInInterpolator;
    public Interpolator linearOutSlowInInterpolator;
    public Interpolator quintOutInterpolator;

    /**
     * Insets
     */
    private Rect displayRect = new Rect();

    /**
     * Task stack
     */
    public int taskStackScrollDuration;
    public int taskStackTopPaddingPx;
    public float taskStackWidthPaddingPct;
    public float taskStackOverScrollPct;

    /**
     * Task view animation and styles
     */
    public int taskViewEnterFromHomeDelay;
    public int taskViewEnterFromHomeDuration;
    public int taskViewEnterFromHomeStaggerDelay;
    public int taskViewTranslationZMinPx;
    public int taskViewTranslationZMaxPx;

    public Configuration(Context context) {
        fastOutSlowInInterpolator = AnimationUtils.loadInterpolator(context,
                android.R.interpolator.accelerate_decelerate);
        linearOutSlowInInterpolator = AnimationUtils.loadInterpolator(context,
                android.R.interpolator.accelerate_decelerate);
        quintOutInterpolator = AnimationUtils.loadInterpolator(context,
                android.R.interpolator.decelerate_quint);
        update(context);
    }

    /**
     * Updates the state, given the specified context
     */
    private void update(Context context) {
        Resources res = context.getResources();
        DisplayMetrics dm = res.getDisplayMetrics();

        // Insets
        displayRect.set(0, 0, dm.widthPixels, dm.heightPixels);

        // Task stack
        taskStackScrollDuration =
                res.getInteger(R.integer.recent_animate_task_stack_scroll_duration);

        // 获取dimen资源值
        TypedValue widthPaddingPctValue = new TypedValue();
        res.getValue(R.dimen.recent_stack_width_padding_percentage, widthPaddingPctValue, true);
        taskStackWidthPaddingPct = widthPaddingPctValue.getFloat();

        // 获取dimen资源值
        TypedValue stackOverScrollPctValue = new TypedValue();
        res.getValue(R.dimen.recent_stack_over_scroll_percentage, stackOverScrollPctValue, true);
        taskStackOverScrollPct = stackOverScrollPctValue.getFloat();

        taskStackTopPaddingPx = res.getDimensionPixelSize(R.dimen.recent_stack_top_padding);

        // Task view animation and styles
        taskViewEnterFromHomeDelay =
                res.getInteger(R.integer.recent_animate_task_enter_from_home_delay);
        taskViewEnterFromHomeDuration =
                res.getInteger(R.integer.recent_animate_task_enter_from_home_duration);
        taskViewEnterFromHomeStaggerDelay =
                res.getInteger(R.integer.recent_animate_task_enter_from_home_stagger_delay);
        taskViewTranslationZMinPx = res.getDimensionPixelSize(R.dimen.recent_task_view_z_min);
        taskViewTranslationZMaxPx = res.getDimensionPixelSize(R.dimen.recent_task_view_z_max);
    }

    /**
     * Returns the task stack bounds in the current orientation. These bounds do not account for
     * the system insets.
     */
    public void getOverviewStackBounds(int windowWidth, int windowHeight,
                                       Rect taskStackBounds) {
        taskStackBounds.set(0, 0, windowWidth, windowHeight);
    }
}
