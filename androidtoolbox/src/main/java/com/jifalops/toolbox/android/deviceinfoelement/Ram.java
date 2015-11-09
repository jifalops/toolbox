package com.jifalops.toolbox.android.deviceinfoelement;

import com.jifalops.toolbox.android.util.ShellHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @see <a href="https://www.centos.org/docs/5/html/5.2/Deployment_Guide/s2-proc-meminfo.html">https://www.centos.org/docs/5/html/5.2/Deployment_Guide/s2-proc-meminfo.html</a>
 */
public class Ram {
	private static final String TAG = Ram.class.getSimpleName();

    public static final String UNITS = "kB";

    private static final String MEMINFO_PROC = "meminfo";
    private static final Pattern DELIM = Pattern.compile(":");
    private static final Pattern SPACE = Pattern.compile(" ");

	public final Map<String, String> memInfo;
	public final long total;
	public final long free;
	public final long cached;
	public final long swapTotal;
	public final long swapFree;
	public final long swapCached;
	public final long kernelCache;
	public final long vTotal;
	public final long vUsed;
	public final long dirty;        // waiting to be written to disk


    public Ram() {
        memInfo = checkMemInfo();
        total = parse(memInfo.get("MemTotal"));
        free = parse(memInfo.get("MemFree"));
        cached = parse(memInfo.get("Cached"));
        swapTotal = parse(memInfo.get("SwapTotal"));
        swapFree = parse(memInfo.get("SwapFree"));
        swapCached = parse(memInfo.get("SwapCached"));
        kernelCache = parse(memInfo.get("Slab"));
        vTotal = parse(memInfo.get("VMallocTotal"));
        vUsed = parse(memInfo.get("VMallocUsed"));
        dirty = parse(memInfo.get("Dirty"));
	}

    private Map<String, String> checkMemInfo() {
        List<String> lines = ShellHelper.getProc(MEMINFO_PROC);
        Map<String, String> info = new HashMap<>(lines.size());
        String[] parts;
        for (String line : lines) {
            parts = DELIM.split(line, 2);
            if (parts.length == 2) info.put(parts[0].trim(), parts[1].trim());
        }
        return info;
    }

    private long parse(String value) {
        try { return Long.valueOf(SPACE.split(value)[0]); }
        catch (Exception e) { return 0; }
    }
}
