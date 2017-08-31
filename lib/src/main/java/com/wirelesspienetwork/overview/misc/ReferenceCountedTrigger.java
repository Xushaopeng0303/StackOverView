package com.wirelesspienetwork.overview.misc;

import java.util.ArrayList;

/**
 * A ref counted trigger that does some logic when the count is first incremented, or last
 * decremented.  Not thread safe as it's not currently needed.
 */
public class ReferenceCountedTrigger {

    private int mCount;
    private ArrayList<Runnable> mFirstIncRunnableList = new ArrayList<>();
    private ArrayList<Runnable> mLastDecRunnableList = new ArrayList<>();
    private Runnable mErrorRunnable;

    public ReferenceCountedTrigger(Runnable firstIncRunnable,
                                   Runnable lastDecRunnable, Runnable errorRunnable) {
        if (firstIncRunnable != null) mFirstIncRunnableList.add(firstIncRunnable);
        if (lastDecRunnable != null) mLastDecRunnableList.add(lastDecRunnable);
        mErrorRunnable = errorRunnable;
    }

    /**
     * Increments the ref count
     */
    public void increment() {
        if (mCount == 0 && !mFirstIncRunnableList.isEmpty()) {
            int runnableNum = mFirstIncRunnableList.size();
            for (int i = 0; i < runnableNum; i++) {
                mFirstIncRunnableList.get(i).run();
            }
        }
        mCount++;
    }

    /**
     * Adds a runnable to the last-decrement runnable list
     */
    public void addLastDecrementRunnable(Runnable r) {
        boolean ensureLastDecrement = (mCount == 0);
        if (ensureLastDecrement)  {
            increment();
        }
        mLastDecRunnableList.add(r);
        if (ensureLastDecrement) {
            decrement();
        }
    }

    /**
     * Decrements the ref count
     */
    public void decrement() {
        mCount--;
        if (mCount == 0 && !mLastDecRunnableList.isEmpty()) {
            int runnableNum = mLastDecRunnableList.size();
            for (int i = 0; i < runnableNum; i++) {
                mLastDecRunnableList.get(i).run();
            }
        } else if (mCount < 0) {
            if (mErrorRunnable != null) {
                mErrorRunnable.run();
            } else {
                new Throwable("Invalid ref count").printStackTrace();
            }
        }
    }

}
