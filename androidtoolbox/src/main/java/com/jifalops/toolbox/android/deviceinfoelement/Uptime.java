package com.jifalops.toolbox.android.deviceinfoelement;

import android.content.Context;

import com.deviceinfoapp.util.ForegroundRepeatingTask;
import com.deviceinfoapp.util.ShellHelper;

import java.util.List;


public class Uptime extends ActiveElement {
	private static final int UPDATE_INTERVAL = 1000;

    private static final String UPTIME_PROC = "uptime";

    private static final int ACTIVE_ACTIONS = 1;
    public static final int ACTION_UPDATED = 0;

	public interface Callbacks extends ActiveElement.Callbacks {
		void onUptimeUpdated(float uptimeTotal, float uptimeAsleep);
	}
	
	private float mUptimeTotal;
	private float mUptimeAsleep;

    // TODO File IO on main thread!
	private final ForegroundRepeatingTask mUpdateTask;

	public Uptime(Context context, Callbacks callbacks) {
		super(context, callbacks);
		mUpdateTask = new ForegroundRepeatingTask(new Runnable() {
			public void run() {		        
				updateUptime();
		   }
		});
		mUpdateTask.setInterval(UPDATE_INTERVAL);
	}
	
	public float getUptimeTotal() {
		return mUptimeTotal;
	}
	
	public float getUptimeAsleep() {
		return mUptimeAsleep;
	}
	
	public float getUptimeAwake() {
		return mUptimeTotal - mUptimeAsleep;
	}
	
	
	private void updateUptime() {
		List<String> list = ShellHelper.getProc(UPTIME_PROC);
		if (list == null || list.isEmpty()) return;
		String[] parts = list.get(0).split("\\s+");
		try {
			if (parts.length >= 2) {
				mUptimeTotal = Float.valueOf(parts[0]);
				mUptimeAsleep = Float.valueOf(parts[1]);
			}
			else if (parts.length == 1) mUptimeTotal = Float.valueOf(parts[0]);
		}
		catch (NumberFormatException ignored) {}	

        setActionTime(ACTION_UPDATED);
        ((Callbacks) mCallbacks).onUptimeUpdated(mUptimeTotal, mUptimeAsleep);
	}

	@Override
	public void start() {
		if (mIsActive) return;
		mUpdateTask.start();		
		mIsActive = true;
	}

	@Override
	public void stop() {
		
		mUpdateTask.stop();
		mIsActive = false;
	}
}
