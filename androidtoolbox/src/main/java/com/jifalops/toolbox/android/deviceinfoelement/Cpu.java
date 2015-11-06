package com.jifalops.toolbox.android.deviceinfoelement;

import android.content.Context;
import android.util.Log;

import com.deviceinfoapp.util.BackgroundRepeatingTask;
import com.deviceinfoapp.util.ShellHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

//TODO exact current frequency???
public class Cpu extends ActiveElement {

    private static final String CPU_INFO_LOCATION = "/sys/devices/system/cpu/cpu";
    private static final String CPU_INFO_PROC = "cpuinfo";
    private static final String CPU_STAT_PROC = "stat";

    public static final int FREQUENCY_HIGH = 1000;
    public static final int FREQUENCY_MEDIUM = 2000;
    public static final int FREQUENCY_LOW = 5000;

    private static final int ACTIVE_ACTIONS = 1;
    public static final int ACTION_UPDATED = 0;

    public interface Callbacks extends ActiveElement.Callbacks {
		void onCpuUpdated(int numCpuStatsUpdated);
	}

	private List<String> mCpuinfo;
	private final List<LogicalCpu> mLogicalCpus;
	private final CpuStat mCpuStat;
	
	private int mNumStatsUpdated;

	private final BackgroundRepeatingTask mUpdateTask;
	
	public Cpu(Context context, Callbacks callbacks) {
		super(context, callbacks);
		mCpuinfo = ShellHelper.getProc(CPU_INFO_PROC);
		mLogicalCpus = new ArrayList<LogicalCpu>();
		mCpuStat = new CpuStat();
		mUpdateTask = new BackgroundRepeatingTask(new Runnable() {			
			@Override
			public void run() {
                // Throttle set by frequency (because it's not a system event but my own)
				mCpuinfo = ShellHelper.getProc(CPU_INFO_PROC);
				updateCpuStats();
				for (LogicalCpu c : mLogicalCpus) {
					c.updateFrequency();
					c.updateGovernor();
					c.updateTimeInFrequency();
					c.updateTotalTransitions();
				}
			}
		});

		File f = null;
		int i = 0;
		while (true) {
			f = new File(CPU_INFO_LOCATION + i);
			if (f.exists()) mLogicalCpus.add(new LogicalCpu(f, i));
			else break;
			++i;
		}

        mUpdateTask.setInterval(FREQUENCY_MEDIUM);
        mUpdateTask.setCallback(new Runnable() {
            @Override
            public void run() {
                setActionTime(ACTION_UPDATED);
                ((Callbacks) mCallbacks).onCpuUpdated(mNumStatsUpdated);
            }
        });

        setActiveActionCount(ACTIVE_ACTIONS);
	}	
	
	public void setUpdateInterval(int milliseconds) {
		mUpdateTask.setInterval(milliseconds);
	}
	
	public int getUpdateInterval() {
		return mUpdateTask.getInterval();
	}
	
	public List<String> getCpuinfo() {
		return mCpuinfo;
	}
	
	public CpuStat getCpuStat() {
		return mCpuStat;
	}
	
	public List<LogicalCpu> getLogicalCpus() {
		return mLogicalCpus;
	}
	
	public int getNumStatsUpdated() {
		return mNumStatsUpdated;
	}
	
	/** Updates the CpuStat for this and all logical CPUs. */
	private void updateCpuStats() {
		mNumStatsUpdated = 0;
		List<String> stats = ShellHelper.getProc(CPU_STAT_PROC);
		if (stats == null || stats.isEmpty()) return;
		
		String[] parts = null;
		String line = null;
		for (int i = 0; i < stats.size(); ++i) {
			line = stats.get(i);
			if (line.startsWith("cpu")) { //TODO if 0% usage the cpu is omitted
				parts = line.split("\\s+");				
				if (parts[0].endsWith(String.valueOf(i - 1))) {
					if (mLogicalCpus.size() >= i &&
							mLogicalCpus.get(i - 1).getCpuStat().update(parts)) {					
						++mNumStatsUpdated;
					}
				}
				else if (mCpuStat.update(parts)) {
					++mNumStatsUpdated;
				}
			}
		}
	}
	
	
	public class LogicalCpu {
		private final String LOG_TAG = LogicalCpu.class.getSimpleName();

        private static final String MAX_FREQUENCY = "/cpufreq/cpuinfo_max_freq";
        private static final String MIN_FREQUENCY = "/cpufreq/cpuinfo_min_freq";
        private static final String CURRENT_FREQUENCY = "/cpufreq/scaling_cur_freq";
        private static final String AVAILABLE_FREQUENCIES = "/cpufreq/scaling_available_frequencies";
        private static final String AVAILABLE_GOVERNERS = "/cpufreq/scaling_available_governors";
        private static final String GOVERNER = "/cpufreq/scaling_governor";
        private static final String DRIVER = "/cpufreq/scaling_driver";
        private static final String TRANSITION_LATENCY = "/cpufreq/cpuinfo_transition_latency";
        private static final String TRANSITIONS = "/cpufreq/stats/total_trans";
        private static final String TIME_IN_FREQUENCY = "/cpufreq/stats/time_in_state";

        //		public final float bogoMips;
		private final int mId;
		private final File mRoot;
		private final CpuStat mCpuStat;
		
		private int mMaxFrequency;
		private int mMinFrequency;
		private int mCurFrequency;
		private int[] mAvailableFrequencies;
		private String[] mAvailableGovernors;
		private String mCurGoverner;
		private String mDriver;
		private int mTransitionLatency;
		private int mTotalTransitions;
		private int[][] mTimeInFrequencies;
		
		
		/** A file pointing to a logical cpu structure
		 * in the file system, such as /sys/devices/system/cpu/cpu0. */
		public LogicalCpu(File file, int id) {	
			if (mLogicalCpus.size() > id) {
				throw new AssertionError("Logical CPU with id " + id + " already exists!");
			}
			
			mRoot = file;
			mId = id;
			mCpuStat = new CpuStat(id);
		}
		
		public CpuStat getCpuStat() {
			return mCpuStat;
		}
		
		/** Get the id of this logical cpu. */
		public int getId() {
			return mId;
		}
		
		public File getRoot() {
			return mRoot;
		}
		
		/** Get the maximum frequency in MHz */
		public int getMaxFrequency() {
			if (mMaxFrequency == 0) {
				List<String> list = ShellHelper.cat(
					mRoot.getAbsolutePath() + MAX_FREQUENCY);
				if (list == null || list.isEmpty()) return 0;
				int value = 0;
				try { value = Integer.valueOf(list.get(0)); }
				catch (NumberFormatException ignored) {}
				mMaxFrequency = value / 1000;
			}
			return mMaxFrequency;
		}
		
		/** Get the minimum frequency in MHz */
		public int getMinFrequency() {
			if (mMinFrequency == 0) {
				List<String> list = ShellHelper.cat(
					mRoot.getAbsolutePath() + MIN_FREQUENCY);
				if (list == null || list.isEmpty()) return 0;
				int value = 0;
				try { value = Integer.valueOf(list.get(0)); }
				catch (NumberFormatException ignored) {}
				mMinFrequency = value / 1000;
			}
			return mMinFrequency;
		}
		
		/** Get the current frequency in MHz */
		public int getFrequency() {
			return mCurFrequency;
		}	
		
		public void updateFrequency() {
			List<String> list = ShellHelper.cat(
				mRoot.getAbsolutePath() + CURRENT_FREQUENCY);
			if (list == null || list.isEmpty()) return;
			int value = 0;
			try { value = Integer.valueOf(list.get(0)); }
			catch (NumberFormatException ignored) {}
			mCurFrequency =  value / 1000;
		}
		
		/** Get the available frequencies in MHz */
		public int[] getAvailableFrequencies() {
			if (mAvailableFrequencies == null) {
				List<String> list = ShellHelper.cat(
					mRoot.getAbsolutePath() + AVAILABLE_FREQUENCIES);
				if (list == null || list.isEmpty()) return null;
				String[] results = list.get(0).split("\\s+");
				if (results == null || results.length == 0) {
					return null;
				}
				int len = results.length;
				mAvailableFrequencies = new int[len];
				for (int i = 0; i < len; ++i) {
					int value = 0;
					try { value = Integer.valueOf(results[i]); }
					catch (NumberFormatException ignored) {}
					mAvailableFrequencies[i] = value / 1000;
				}
			}
			return mAvailableFrequencies;
		}
		
		/** Get the available governors */
		public String[] getAvailableGovernors() {
			if (mAvailableGovernors == null) {
				List<String> list = ShellHelper.cat(
					mRoot.getAbsolutePath() + AVAILABLE_GOVERNERS);
				if (list == null || list.isEmpty()) return null;
				mAvailableGovernors = list.get(0).split("\\s+");
			}
			return mAvailableGovernors;
		}
		
		/** Get the current governor */
		public String getGovernor() {
			return mCurGoverner;
		}
		
		public void updateGovernor() {
			List<String> list = ShellHelper.cat(
					mRoot.getAbsolutePath() + GOVERNER);
			if (list == null || list.isEmpty()) return;
			mCurGoverner = list.get(0);
		}
		
		/** Get the current driver */
		public String getDriver() {
			if (mDriver == null) {
				List<String> list = ShellHelper.cat(
					mRoot.getAbsolutePath() + DRIVER);
				if (list == null || list.isEmpty()) return null;
				mDriver = list.get(0);
			}
			return mDriver;
		}
		
		/** Get the frequency transition latency in nano-seconds */
		public int getTransitionLatency() {
			if (mTransitionLatency == 0) {
				List<String> list = ShellHelper.cat(
					mRoot.getAbsolutePath() + TRANSITION_LATENCY);
				if (list == null || list.isEmpty()) return 0;
				int value = 0;
				try { value = Integer.valueOf(list.get(0)); }
				catch (NumberFormatException ignored) {}
				mTransitionLatency = value;			
			}
			return mTransitionLatency;
		}
		
		/** Get the total number of frequency transitions */
		public int getTotalTransitions() {
			return mTotalTransitions;
		}
		
		public void updateTotalTransitions() {
			List<String> list = ShellHelper.cat(
					mRoot.getAbsolutePath() + TRANSITIONS);
			if (list == null || list.isEmpty()) return;
			int value = 0;
			try { value = Integer.valueOf(list.get(0)); }
			catch (NumberFormatException ignored) {}
			mTotalTransitions = value;
		}
		
		/** Get the total amount of time spent in frequency transitions in seconds */
		public float getTimeInTransitions() {
			return (float) ((long) getTotalTransitions() * (long) getTransitionLatency() / 1E9);
		}

		/** Get a list of the total time (in Jiffies) spent at each frequency (in MHz) */
		public int[][] getTimeInFrequency() {
			return mTimeInFrequencies;
		}
		
		public void updateTimeInFrequency() {
			List<String> list = ShellHelper.cat(
					mRoot.getAbsolutePath() + TIME_IN_FREQUENCY);
			if (list == null || list.isEmpty()) return;
			int len = list.size();
			int[][] times = new int[len][2];
			String[] parts = null;
			for (int i = 0; i < len; ++i) {
				parts = list.get(i).split("\\s+");
				if (parts.length != 2) {
					Log.d(LOG_TAG, "time in state did not have exactly 2 parts.");
					continue;
				}
				int freq = 0, time = 0;
				try { 
					freq = Integer.valueOf(parts[0]) / 1000;
					time = Integer.valueOf(parts[1]);
				}
				catch (NumberFormatException ignored) {}
				times[i][0] = freq;
				times[i][1] = time;
			}
			mTimeInFrequencies = times;
		}
		
		/** Get the total time (in Jiffies) spent a frequency given in MHz. */
		public int getTimeInFrequency(int frequency) {
			int[][] times = getTimeInFrequency();
			if (times == null || times.length == 0) return 0;
			for (int[] f : times) {
				if (f[0] == frequency) return f[1];
			}
			return 0;
		}
		
		/** Get a list of the percentage of time spent at each frequency (in MHz) */
		public Map<Integer, Float> getPercentInFrequency() {
			Map<Integer, Float> percents = new LinkedHashMap<Integer, Float>();
			
			int[][] times = getTimeInFrequency();
			if (times == null || times.length == 0) return null;
			
			long total = 0;
			
			for (int i = 0; i < times.length; ++i) {
				total += times[i][1];
			}
			
			if (total == 0) return null;
			
			for (int i = 0; i < times.length; ++i) {
				percents.put(times[i][0], (float) times[i][1] / total * 100);				
			}
			
			return percents;
		}
		
		/** Get the percentage of time spent a frequency given in MHz. */
		public float getPercentInFrequency(int frequency) {
			Map<Integer, Float> percents = getPercentInFrequency();
			if (percents == null || percents.size() == 0) return 0;

			for (int f : percents.keySet()) {
				if (f == frequency) return percents.get(f);
			}
			return 0;
		}
	}
	
	public class CpuStat {
		private final String LOG_TAG = CpuStat.class.getSimpleName();

        private final String PREFIX = "cpu";
		
		public final int OVERALL_ID = -1;
		
		private int mId;
		
		
		private long mUser = 0;
		private long mNice = 0;
		private long mSystem = 0;
		private long mIdle = 0;
		private long mIoWait = 0;
		private long mIntr = 0;
		private long mSoftIrq = 0;		
		// The other two fields, Steal and Guest, seem to always be zero.
		
		
		private long mUserPrevious = 0;
		private long mNicePrevious = 0;
		private long mSystemPrevious = 0;
		private long mIdlePrevious = 0;
		private long mIoWaitPrevious = 0;
		private long mIntrPrevious = 0;
		private long mSoftIrqPrevious = 0;
		
		public CpuStat() {
			// The overall cpu stat.
			mId = OVERALL_ID;
		}
		
		public CpuStat(int id) {
			// Cpu stat for a particular cpu.
			mId = id;
		}
		
		/** 
		 * Update the cpu stats for this cpu.
		 * @param parts
		 * 		An array with at least 8 elements:<br>
		 * 	cpu[id] user nice system idle io_wait intr soft_irq<br>
		 * The id for cpu is optional.
		 * @return whether the update completed successfully.
		 */
		private boolean update(String[] parts) {		
			if (parts == null || parts.length < 8) {
				Log.d(LOG_TAG, "invalid array length to perform update.");
				return false;
			}
			
			String value = PREFIX;
			if (mId != OVERALL_ID) value += mId;
			
			if (!parts[0].equals(value)) {
				Log.d(LOG_TAG, "Tried to perform update on wrong CpuStat. Got '" 
						+ parts[0] + "', expected '" + value + "'.");
				return false;
			}
			
			long[] values = new long[7];
			
			try {				
				for (int i = 0; i < 7; ++i) {
					values[i] = Long.parseLong(parts[i + 1]);
				}				
			} 
			catch (NumberFormatException ignored) {
				return false;
			}
			
			mUserPrevious = mUser;
			mNicePrevious = mNice;
			mSystemPrevious = mSystem;
			mIdlePrevious = mIdle;
			mIoWaitPrevious = mIoWait;
			mIntrPrevious = mIntr;
			mSoftIrqPrevious = mSoftIrq;
			
			mUser = values[0];
			mNice = values[1];
			mSystem = values[2];
			mIdle = values[3];
			mIoWait = values[4];
			mIntr = values[5];
			mSoftIrq = values[6];
			
			return true;
		}
		
		public int getId() {
			return mId;
		}
		
		public float getUserPercent() {
			float totalDif = getTotalDifference();
			if (totalDif == 0) return 0;
			return ((totalDif - (totalDif - getUserDifference())) / totalDif) * 100;
		}
		
		public float getNicePercent() {
			float totalDif = getTotalDifference();
			if (totalDif == 0) return 0;
			return ((totalDif - (totalDif - getNiceDifference())) / totalDif) * 100;
		}
		
		public float getSystemPercent() {
			float totalDif = getTotalDifference();
			if (totalDif == 0) return 0;
			return ((totalDif - (totalDif - getSystemDifference())) / totalDif) * 100;
		}
		
		public float getIdlePercent() {
			float totalDif = getTotalDifference();
			if (totalDif == 0) return 0;
			return ((totalDif - (totalDif - getIdleDifference())) / totalDif) * 100;
		}
		
		public float getIoWaitPercent() {
			float totalDif = getTotalDifference();
			if (totalDif == 0) return 0;
			return ((totalDif - (totalDif - getIoWaitDifference())) / totalDif) * 100;
		}
		
		public float getIntrPercent() {
			float totalDif = getTotalDifference();
			if (totalDif == 0) return 0;
			return ((totalDif - (totalDif - getIntrDifference())) / totalDif) * 100;
		}
		
		public float getSoftIrqPercent() {
			float totalDif = getTotalDifference();
			if (totalDif == 0) return 0;
			return ((totalDif - (totalDif - getSoftIrqDifference())) / totalDif) * 100;
		}
		
		/** User + Nice */
		public float getUserTotalPercent() {
			float idleDif = getIdleTotalDifference();
			float systemDif = getSystemTotalDifference();
			float divisor = getUserTotalDifference() + idleDif + systemDif;
			if (divisor == 0) return 0;
			return ((divisor - idleDif - systemDif) / divisor) * 100;
		}
		
//		/** User + Nice */
//		public float getUserTotalPercent() {
//			float divisor = mUserPrevious + mNicePrevious;
//			if (divisor == 0) return 0;
//			return ((mUser - mUserPrevious) + (mNice - mNicePrevious))
//				/ divisor * 100;
//		}
		
		/** System + Intr + SoftIrq */
		public float getSystemTotalPercent() {
			float idleDif =  getIdleTotalDifference();
			float userDif = getUserTotalDifference();
			float divisor = getSystemTotalDifference() + idleDif + userDif;
			if (divisor == 0) return 0;
			return ((divisor - idleDif - userDif) / divisor) * 100;
		}
		
//		/** System + Intr + SoftIrq */
//		public float getSystemTotalPercent() {
//			float divisor = mSystemPrevious + mIntrPrevious + mSoftIrqPrevious;
//			if (divisor == 0) return 0;
//			return ((mSystem - mSystemPrevious) 
//				+ (mIntr - mIntrPrevious) 
//				+ (mSoftIrq - mSoftIrqPrevious))
//				/ divisor * 100;
//		}
		
		/** Idle + IoWait */
		public float getIdleTotalPercent() {
			float userDif = getUserTotalDifference();
			float systemDif = getSystemTotalDifference();
			float divisor = getIdleTotalDifference() + userDif + systemDif;
			if (divisor == 0) return 0;
			return ((divisor - userDif - systemDif) / divisor) * 100;
		}
		
//		/** Idle + IoWait */
//		public float getIdleTotalPercent() {
//			float divisor = mIdlePrevious + mIoWaitPrevious;
//			if (divisor == 0) return 0;
//			return ((mIdle - mIdlePrevious) + (mIoWait - mIoWaitPrevious))
//					/ divisor * 100;
//		}
		
		public float getTotalPercent() {
			float divisor = getTotalDifference();
			if (divisor == 0) return 0;
			return ((divisor - getIdleTotalDifference()) / divisor) * 100;
		}
		
//		public float getTotalPercent() {
//			float divisor = mUserPrevious
//					+ mNicePrevious
//					+ mSystemPrevious
//					+ mIdlePrevious
//					+ mIoWaitPrevious
//					+ mIntrPrevious 
//					+ mSoftIrqPrevious;
//			if (divisor == 0) return 0;
//			return ((mUser - mUserPrevious)
//				+ (mNice - mNicePrevious)
//				+ (mSystem - mSystemPrevious) 
//				+ (mIdle - mIdlePrevious)
//				+ (mIoWait - mIoWaitPrevious)
//				+ (mIntr - mIntrPrevious)				
//				+ (mSoftIrq - mSoftIrqPrevious))
//				/ divisor * 100;
//		}

		public long getUser() {
			return mUser;
		}
		
		public long getNice() {
			return mNice;
		}
		
		public long getSystem() {
			return mSystem;
		}
		
		public long getIdle() {
			return mIdle;
		}
		
		public long getIoWait() {
			return mIoWait;
		}
		
		public long getIntr() {
			return mIntr;
		}
		
		public long getSoftIrq() {
			return mSoftIrq;
		}
		
		/** User + Nice */
		public long getUserTotal() {
			return mUser + mNice;
		}
		
		/** System + Intr + SoftIrq */
		public long getSystemTotal() {
			return mSystem + mIntr + mSoftIrq;
		}
		
		/** Idle + IoWait */
		public long getIdleTotal() {
			return mIdle + mIoWait;
		}
		
		public long getTotal() {
			return mUser + mNice + mSystem
				+ mIdle + mIoWait + mIntr + mSoftIrq;
		}
		
		
		
		
		
		
		public long getUserPrevious() {
			return mUserPrevious;
		}
		
		public long getNicePrevious() {
			return mNicePrevious;
		}
		
		public long getSystemPrevious() {
			return mSystemPrevious;
		}
		
		public long getIdlePrevious() {
			return mIdlePrevious;
		}
		
		public long getIoWaitPrevious() {
			return mIoWaitPrevious;
		}
		
		public long getIntrPrevious() {
			return mIntrPrevious;
		}
		
		public long getSoftIrqPrevious() {
			return mSoftIrqPrevious;
		}
		
		/** User + Nice */
		public long getUserTotalPrevious() {
			return mUserPrevious + mNicePrevious;
		}
		
		/** System + Intr + SoftIrq */
		public long getSystemTotalPrevious() {
			return mSystemPrevious + mIntrPrevious + mSoftIrqPrevious;
		}
		
		/** Idle + IoWait */
		public long getIdleTotalPrevious() {
			return mIdlePrevious + mIoWaitPrevious;
		}
		
		public long getTotalPrevious() {
			return mUserPrevious + mNicePrevious + mSystemPrevious
				+ mIdlePrevious + mIoWaitPrevious + mIntrPrevious + mSoftIrqPrevious;
		}
		
		
		
		public long getUserDifference() {
			return (mUser - mUserPrevious);
		}
		
		public long getNiceDifference() {
			return (mNice - mNicePrevious);
		}
		
		public long getSystemDifference() {
			return (mSystem - mSystemPrevious);
		}
		
		public long getIdleDifference() {
			return (mIdle - mIdlePrevious);
		}
		
		public long getIoWaitDifference() {
			return (mIoWait - mIoWaitPrevious);
		}
		
		public long getIntrDifference() {
			return (mIntr - mIntrPrevious);
		}
		
		public long getSoftIrqDifference() {
			return (mSoftIrq - mSoftIrqPrevious);
		}
		
		/** User + Nice */
		public long getUserTotalDifference() {
			return (mUser - mUserPrevious) + (mNice - mNicePrevious);
		}
		
		/** System + Intr + SoftIrq */
		public long getSystemTotalDifference() {
			return (mSystem - mSystemPrevious) + (mIntr - mIntrPrevious) + (mSoftIrq - mSoftIrqPrevious);
		}
		
		/** Idle + IoWait */
		public long getIdleTotalDifference() {
			return (mIdle - mIdlePrevious) + (mIoWait - mIoWaitPrevious);
		}
		
		public long getTotalDifference() {
			return (mUser - mUserPrevious) 
				+ (mNice - mNicePrevious) + (mSystem - mSystemPrevious)
				+ (mIdle - mIdlePrevious) + (mIoWait - mIoWaitPrevious)
				+ (mIntr - mIntrPrevious) + (mSoftIrq - mSoftIrqPrevious);
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
