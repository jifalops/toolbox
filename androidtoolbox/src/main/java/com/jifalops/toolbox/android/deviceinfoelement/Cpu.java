package com.jifalops.toolbox.android.deviceinfoelement;

import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

//TODO exact current frequency??
public class Cpu {
	static final String TAG = /*Constants.BASE_TAG +*/ Cpu.class.getSimpleName();

    private static final String CPU_INFO_LOCATION = "/sys/devices/system/cpu/";
    private static final String CPU_INFO_PROC = "cpuinfo";
    private static final String CPU_STAT_PROC = "stat";
    private static final String PREFIX = "cpu";
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");

    private Stat stat, prevStat;
    private final Info info;
	private final List<LogicalCpu> cores = new ArrayList<>();


    private static Cpu instance;
    public static Cpu getInstance() {
        if (instance == null) instance = new Cpu();
        return instance;
    }
	private Cpu() {
        info = new Info();
        checkCores();
        updateStats();
	}

//    public Usage checkUsage() {
//        updateStats();
//        return new Usage(stat, prevStat);
//    }

    public Info getInfo() {
        return info;
    }

    public List<LogicalCpu> getCores() {
        return cores;
    }

    public int availableCores() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1
                ? Runtime.getRuntime().availableProcessors()
                : cores.size();
    }

    /**
     * Checks the number of logical CPUs used in the system.
     * This is typically the number of cores on the CPU, but can also work with multiple processors.
     */
    private int checkCores() {
        cores.clear();
        File f;
        int i = 0;
        while (true) {
            f = new File(CPU_INFO_LOCATION + PREFIX + i);
            if (f.exists()) cores.add(new LogicalCpu(f, i));
            else break;
            ++i;
        }
        return cores.size();
    }

	/** Updates the Stat for the overall and all logical CPUs. */
	private void updateStats() {
		List<String> stats = ShellHelper.getProc(CPU_STAT_PROC);
        for (String line : stats) {
            if (line.startsWith(PREFIX)) { //TODO if 0% usage the cpu can be omitted
                Stat stat = new Stat(line);
                if (PREFIX.equals(stat.id)) {
                    // This is the overall CPU stat
                    prevStat = this.stat;
                    this.stat = stat;
                } else {
                    // This is a component CPU stat
                    int id = Integer.valueOf(stat.id.substring(PREFIX.length()));
                    LogicalCpu cpu = cores.get(id);
                    cpu.prevStat = cpu.stat;
                    cpu.stat = stat;
                    cpu.stat.timeInFreqs = cpu.checkTimeInFrequency();
                }
            }
		}
	}
	
	public static class LogicalCpu {
        private static final String MAX_FREQUENCY = 		"/cpufreq/cpuinfo_max_freq";
        private static final String MIN_FREQUENCY = 		"/cpufreq/cpuinfo_min_freq";
        private static final String CURRENT_FREQUENCY = 	"/cpufreq/scaling_cur_freq";
        private static final String AVAILABLE_FREQUENCIES = "/cpufreq/scaling_available_frequencies";
        private static final String AVAILABLE_GOVERNERS = 	"/cpufreq/scaling_available_governors";
        private static final String GOVERNOR = 				"/cpufreq/scaling_governor";
        private static final String DRIVER = 				"/cpufreq/scaling_driver";
        private static final String TRANSITION_LATENCY = 	"/cpufreq/cpuinfo_transition_latency";
        private static final String TRANSITIONS = 			"/cpufreq/stats/total_trans";
        private static final String TIME_IN_FREQUENCY = 	"/cpufreq/stats/time_in_state";

        //public final float bogoMips;
		public final int id;
		public final File dir;
		private Stat stat, prevStat;
		
		public final int maxFrequency;
        public final int minFrequency;
		public final int[] availableFrequencies;
		public final String[] availableGovernors;
		public final String curGoverner;
		public final String driver;
		public final int transitionLatency;
		
		
		/** A file pointing to a logical cpu structure
		 * in the file system, such as /sys/devices/system/cpu/cpu0. */
		private LogicalCpu(File dir, int id) {
			this.dir = dir;
			this.id = id;
            maxFrequency = checkMaxFrequency();
            minFrequency = checkMinFrequency();
            availableFrequencies = checkAvailableFrequencies();
            availableGovernors = checkAvailableGovernors();
            curGoverner = checkCurrentGoverner();
            driver = checkDriver();
            transitionLatency = checkTransitionLatency();
		}

        /** Get the current frequency in MHz */
        public int checkCurrentFrequency() {
            int freq = 0;
            List<String> list = ShellHelper.cat(dir.getAbsolutePath() + CURRENT_FREQUENCY);
            if (list.size() > 0) {
                try { freq = Integer.valueOf(list.get(0)); }
                catch (NumberFormatException ignored) {}
                freq /= 1000;
            }
            return freq;
        }

        /** Get the total number of frequency transitions */
        public int checkTotalTransitions() {
            int value = 0;
            List<String> list = ShellHelper.cat(dir.getAbsolutePath() + TRANSITIONS);
            if (list.size() > 0) {
                try {
                    value = Integer.valueOf(list.get(0));
                } catch (NumberFormatException ignored) {}
            }
            return value;
        }

        /** Get the total amount of time spent in frequency transitions in seconds */
        public float checkTimeInTransitions() {
            return (float) ((long) checkTotalTransitions() * (long) checkTransitionLatency() / 1E9);
        }

        /** Get a list of the total user time (in Jiffies) spent at each frequency (in MHz) */
        public Map<Integer, Integer> checkTimeInFrequency() {
            List<String> list = ShellHelper.cat(dir.getAbsolutePath() + TIME_IN_FREQUENCY);
            int len = list.size();
            Map<Integer, Integer> times = new HashMap<>(len);
            if (len > 0) {
                String[] parts;
                for (int i = 0; i < len; ++i) {
                    parts = WHITESPACE.split(list.get(i));
                    if (parts.length != 2) {
                        Log.e(TAG, "time in state did not have exactly 2 parts.");
                        continue;
                    }
                    try {
                        times.put(Integer.valueOf(parts[0]) / 1000, Integer.valueOf(parts[1]));
                    } catch (NumberFormatException ignored) {}
                }
            }
            return times;
        }
		
		/** Get the maximum frequency in MHz */
		private int checkMaxFrequency() {
            int freq = 0;
            List<String> list = ShellHelper.cat(dir.getAbsolutePath() + MAX_FREQUENCY);
            if (list.size() > 0) {
                try { freq = Integer.valueOf(list.get(0)); }
                catch (NumberFormatException ignored) {}
                freq /= 1000;
            }
			return freq;
		}
		
		/** Get the minimum frequency in MHz */
        private int checkMinFrequency() {
            int freq = 0;
            List<String> list = ShellHelper.cat(dir.getAbsolutePath() + MIN_FREQUENCY);
            if (list.size() > 0) {
                try { freq = Integer.valueOf(list.get(0)); }
                catch (NumberFormatException ignored) {}
                freq /= 1000;
            }
            return freq;
        }
		

		/** Get the available frequencies in MHz */
		private int[] checkAvailableFrequencies() {
            List<String> list = ShellHelper.cat(dir.getAbsolutePath() + AVAILABLE_FREQUENCIES);
            if (list.size() > 0) {
                String[] results = WHITESPACE.split(list.get(0));
                int len = results.length;
                int[] freqs = new int[len];
                for (int i = 0; i < len; ++i) {
                    int value = 0;
                    try { value = Integer.valueOf(results[i]); }
                    catch (NumberFormatException ignored) {}
                    freqs[i] = value / 1000;
                }
                return freqs;
            } else return new int[0];
		}
		
		/** Get the available governors */
		private String[] checkAvailableGovernors() {
            List<String> list = ShellHelper.cat(dir.getAbsolutePath() + AVAILABLE_GOVERNERS);
            if (list.size() > 0) {
                return WHITESPACE.split(list.get(0));
            } else return new String[0];
		}

        private String checkCurrentGoverner() {
            List<String> list = ShellHelper.cat(dir.getAbsolutePath() + GOVERNOR);
            if (list.size() > 0) return list.get(0);
            else return "";
        }
		
		/** Get the current driver */
		private String checkDriver() {
            List<String> list = ShellHelper.cat(dir.getAbsolutePath() + DRIVER);
            if (list.size() > 0) return list.get(0);
            else return "";
		}
		
		/** Get the frequency transition latency in nano-seconds */
		private int checkTransitionLatency() {
			int value = 0;
            List<String> list = ShellHelper.cat(dir.getAbsolutePath() + TRANSITION_LATENCY);
            if (list.size() > 0) {
				try { value = Integer.valueOf(list.get(0)); }
				catch (NumberFormatException ignored) {}
			}
			return value;
		}
	}


	public static class Stat {
        public final String id;
        public final long time = System.nanoTime();
		public final long user;
		public final long nice;
		public final long system;
		public final long idle;
		public final long ioWait;
		public final long intr;
		public final long softIrq;		
		// The other two fields, Steal and Guest, seem to always be zero.

		// typical combinations
		/** User + Nice */
		public final long userTotal;
		/** System + Intr + SoftIrq */
		public final long systemTotal;
		/** Idle + IoWait */
		public final long idleTotal;
		public final long total;

        private Map<Integer, Integer> timeInFreqs;

		private Stat(String line) {
			String[] parts = WHITESPACE.split(line);
			id = parts[0];
			user = Long.valueOf(parts[1]);
			nice = Long.valueOf(parts[2]);
			system = Long.valueOf(parts[3]);
			idle = Long.valueOf(parts[4]);
			ioWait = Long.valueOf(parts[5]);
			intr = Long.valueOf(parts[6]);
			softIrq = Long.valueOf(parts[7]);

			userTotal = user + nice;
			systemTotal = system + intr + softIrq;
			idleTotal = idle + ioWait;
			total = userTotal + systemTotal + idleTotal;
		}
	}

	/** CPU usage as a percent.
     * This is harder than it looks.
     * @see <a href="https://source.android.com/reference/com/android/tradefed/device/CpuStatsCollector.CpuStats.html#getEstimatedMhz()">CpuStats</a>*/
//	public static class Usage {
//		public final double user;
//		public final double nice;
//		public final double system;
//		public final double idle;
//		public final double ioWait;
//		public final double intr;
//		public final double softIrq;
//		/** User + Nice */
//		public final double userTotal;
//		/** System + Intr + SoftIrq */
//		public final double systemTotal;
//		/** Idle + IoWait */
//		public final double idleTotal;
//
//        /** Estimated frequency in MHz */
//        public final double freq;
//
//        /** Used MHz percentage */
//        public final double freqPercentage;
//
//		private Usage(Stat cur, Stat prev) {
//            double totalDiff = 0;
//            if (cur != null && prev != null) {
//                totalDiff = cur.total - prev.total;
//            }
//			if (cur == null || prev == null || totalDiff == 0 ||
//                    cur.timeInFreqs == null || prev.timeInFreqs == null) {
//				user = 0;
//				nice = 0;
//				system = 0;
//				idle = 0;
//				ioWait = 0;
//				intr = 0;
//				softIrq = 0;
//				userTotal = 0;
//				systemTotal = 0;
//				idleTotal = 0;
//                freq = 0;
//                freqPercentage = 0;
//			} else {
//                int len = cur.timeInFreqs.size();
//                Diff d = new Diff(cur, prev);
//                double utotal = 0, stotal = 0;
//                int dfreq, dtime;
//                // total user time spent at each frequency
//                Map<Integer, Integer> times = new HashMap<>(len);
//                for (Map.Entry<Integer, Integer> e : cur.timeInFreqs.entrySet()) {
//                    dfreq = e.getKey();
//                    dtime = e.getValue() - prev.timeInFreqs.get(dfreq);
////                    times.put(e.getKey(), e.getValue() - prev.timeInFreqs.get(e.getKey()));
//                    utotal += ((d.userTotal - d.idleTotal) / d.userTotal) * ((times))
//                }
//                userTotal = ((d.userTotal - d.idleTotal) / d.userTotal) * ((times))
//
//
//			}
//		}
//
//        static class Diff {
//            final long user;
//            final long nice;
//            final long system;
//            final long idle;
//            final long ioWait;
//            final long intr;
//            final long softIrq;
//
//            final long userTotal;
//            final long systemTotal;
//            final long idleTotal;
//            final long total;
//            Diff(Stat cur, Stat prev) {
//                user = cur.user - prev.user;
//                nice = cur.nice - prev.nice;
//                system = cur.system - prev.system;
//                idle = cur.idle - prev.idle;
//                ioWait = cur.ioWait - prev.ioWait;
//                intr = cur.intr - prev.intr;
//                softIrq = cur.softIrq - prev.softIrq;
//
//                userTotal = cur.userTotal - prev.userTotal;
//                systemTotal = cur.systemTotal - prev.systemTotal;
//                idleTotal = cur.idleTotal - prev.idleTotal;
//				total = cur.total - prev.total;
//            }
//        }
//	}

    public static class Info {
        public final Map<String, String> info;
        public final String model;
        public final float bogoMips;
        public final String features;
        public final String cpuImplementer;
        public final String cpuArchitecture;
        public final String cpuVariant;
        public final String cpuPart;
        public final String cpuRevision;
        public final String hardware;
        public final String revision;
        public final String serial;
        public final String device;
        public final String radio;
        public final String msmHardware;
        public final int cores;

        private static final Pattern delim = Pattern.compile(":");

        private Info() {
            List<String> lines = ShellHelper.cat(CPU_INFO_PROC);
            info = parseLines(lines);

            String model = info.get("model name");
            if (TextUtils.isEmpty(model)) model = info.get("Processor");
            this.model = model;

            float bogoMips = 0;
            try { bogoMips = Float.valueOf(info.get("BogoMIPS")); }
            catch (Exception ignored) {}
            this.bogoMips = bogoMips;

            features = info.get("Features");
            cpuImplementer = info.get("CPU implementer");
            cpuArchitecture = info.get("CPU architecture");
            cpuVariant = info.get("CPU variant");
            cpuPart = info.get("CPU part");
            cpuRevision = info.get("CPU revision");

            hardware = info.get("Hardware");
            revision = info.get("Revision");
            serial = info.get("Serial");
            device = info.get("Device");
            radio = info.get("Radio");
            msmHardware = info.get("MSM Hardware");

            int cores = 0;
            try { cores = Integer.valueOf(info.get("processor")) + 1; }
            catch (Exception ignored) {}
            this.cores = cores;
        }

        private Map<String, String> parseLines(List<String> lines) {
            Map<String, String> info = new HashMap<>(lines.size());
            String[] parts;
            for (String line : lines) {
                parts = delim.split(line, 2);
                if (parts.length == 2) {
                    info.put(parts[0].trim(), parts[1].trim());
                }
            }
            return info;
        }
    }
}
