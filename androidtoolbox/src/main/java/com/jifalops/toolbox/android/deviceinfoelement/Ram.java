package com.jifalops.toolbox.android.deviceinfoelement;

import android.content.Context;

import com.deviceinfoapp.DeviceInfo;
import com.deviceinfoapp.util.BackgroundRepeatingTask;
import com.deviceinfoapp.util.Convert;
import com.deviceinfoapp.util.ShellHelper;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class Ram extends ActiveElement {
	private static final String LOG_TAG = Ram.class.getSimpleName();

    private static final String MEMINFO_PROC = "meminfo";
    private static final String MEMINFO_DELIM = ":";
    private static final String KEY_TOTAL = "MemTotal";
    private static final String KEY_FREE = "MemFree";

    public static final int FREQUENCY_HIGH = 1000;
    public static final int FREQUENCY_MEDIUM = 2000;
    public static final int FREQUENCY_LOW = 3000;

    private static final int ACTIVE_ACTIONS = 1;
    public static final int ACTION_UPDATE = 0;

	public interface Callbacks extends ActiveElement.Callbacks {
		void onUpdated(LinkedHashMap<String, String> meminfo);
	}

	private LinkedHashMap<String, String> mMeminfo;
	private BackgroundRepeatingTask mUpdateTask;
    private int mUpdateFrequency;

	public Ram(Context context, Callbacks callbacks) {
		super(context, callbacks);

        mUpdateFrequency = FREQUENCY_MEDIUM;
		mMeminfo = new LinkedHashMap<String, String>();
		mUpdateTask = new BackgroundRepeatingTask(new Runnable() {			
			@Override
			public void run() {
				updateMeminfo();
			}
		});		
		mUpdateTask.setInterval(mUpdateFrequency);
		mUpdateTask.setCallback(new Runnable() {			
			@Override
			public void run() {
                setActionTime(ACTION_UPDATE);
				((Callbacks) mCallbacks).onUpdated(new LinkedHashMap<String, String>(mMeminfo));
			}
		});

        setActiveActionCount(ACTIVE_ACTIONS);
	}
	
	/** Get the current meminfo from /proc */
	public boolean updateMeminfo() {
        // Throttle set by frequency (because it's not a system event but my own)
        List<String> meminfo = ShellHelper.getProc(MEMINFO_PROC);
        if (meminfo == null || meminfo.isEmpty()) return false;        
        String[] parts = null;
        mMeminfo.clear();
        for (String s : meminfo) {
        	parts = s.split(MEMINFO_DELIM);
        	if (parts.length != 2) continue;
        	mMeminfo.put(parts[0].trim(), parts[1].trim());
        }
               
        return !mMeminfo.isEmpty();
    }
	
	public Map<String, String> getMeminfo() {
		return new LinkedHashMap<String, String>(mMeminfo);
	}
	
	public String getMeminfo(String key) {
        if (key == null || !mMeminfo.containsKey(key)) return null;
        return mMeminfo.get(key);        
    }
	
	public String getTotal() {
		return getMeminfo(KEY_TOTAL);
    }
	
	public String getFree() {
        return getMeminfo(KEY_FREE);
    }
	
	public String getUsagePercent() {
		long total = getLongFromValue(getTotal());		
		long using = total - getLongFromValue(getFree());
		return Convert.round(DeviceInfo.getPercent(using, total), 1);
	}
	
	public long getLongFromValue(String value) {
		if (value == null) return 0;
		String[] parts = value.split("\\s+");
		if (parts == null || parts.length == 0) return 0;
		try { return Long.valueOf(parts[0]); }
		catch (NumberFormatException e) {
			return 0;
		}
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
