package com.jifalops.toolbox.android.deviceinfoelement;

import android.content.Context;

public abstract class ActiveElement extends AbsElement {

    private static final int DEFAULT_ACTION_THROTTLE = 100; // ms

	public interface Callbacks {
        void onAction(int item);
    }

    protected abstract void start();
    protected abstract void stop();

	protected Callbacks mCallbacks;
    protected boolean mIsActive;
    private boolean mIsThrottled;
    private long[] mActionTimestamps;
    private int[] mActionThrottles;


	public ActiveElement(Context context, Callbacks callbacks) {
		super(context);
        mCallbacks = callbacks;
	}

    public final boolean isActive() {
        return mIsActive;
    }

    public void setActiveActionCount(int count) {
        mActionTimestamps = new long[count];
        mActionThrottles = new int[count];
        long time = System.currentTimeMillis() - (DEFAULT_ACTION_THROTTLE + 1);
        for (int i = 0; i < count; ++i) {
            mActionTimestamps[i] = time;
            mActionThrottles[i] = DEFAULT_ACTION_THROTTLE;
        }
    }

    public int getActiveActionCount() {
        return mActionThrottles.length;
    }



    public boolean isActionAllowed(int action) {
        if (!mIsThrottled) return true;
        return mActionThrottles[action] < (System.currentTimeMillis() - mActionTimestamps[action]);
    }

    public void setActionTime(int action) {
        long time = System.currentTimeMillis();
        mActionTimestamps[action] = time;
        mCallbacks.onAction(action);
    }

    public long getActionTime(int action) {
        return mActionTimestamps[action];
    }

    public void setActionThrottle(int action, int millis) {
        mActionThrottles[action] = millis;
    }

    public int getActionThrottle(int action) {
        return mActionThrottles[action];
    }

    public boolean isThrottled() {
        return mIsThrottled;
    }

    public void setIsThrottled(boolean throttled) {
        mIsThrottled = throttled;
    }
}
