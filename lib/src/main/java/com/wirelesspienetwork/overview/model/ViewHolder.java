package com.wirelesspienetwork.overview.model;

import android.view.View;

import com.wirelesspienetwork.overview.views.StackViewCard;

/**
 * 任务卡片 ViewHolder
 * @param <V>   卡片View
 * @param <Model>   卡片信息Model
 */
public class ViewHolder<V extends View, Model> {
    public final V itemView;
    public Model model;

    private StackViewCard mContainer;

    private int mCurrentPosition = -1;
    private int mLastPosition = -1;

    public ViewHolder(V view) {
        this.itemView = view;
    }

    public void setPosition(int position) {
        mLastPosition = mCurrentPosition;
        mCurrentPosition = position;
    }

    public int getPosition() {
        return mCurrentPosition;
    }

    public int getLastPosition() {
        return mLastPosition;
    }

    public StackViewCard getContainer() {
        return mContainer;
    }

    void setContainer(StackViewCard container) {
        if (mContainer != null) {
            mContainer.setContent(null);
        }
        mContainer = container;
        if (mContainer != null && itemView != null) {
            mContainer.setContent(itemView);
        }
    }
}
