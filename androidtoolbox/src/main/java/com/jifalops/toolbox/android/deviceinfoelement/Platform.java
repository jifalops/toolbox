package com.jifalops.toolbox.android.deviceinfoelement;

import android.content.Context;
import android.telephony.TelephonyManager;

import com.jifalops.toolbox.android.R;
import com.deviceinfoapp.util.ShellHelper;

import java.util.List;

public class Platform extends AbsElement {
    private static final String PROC_VERSION = "version";

	public final String ECLAIR;
	public final String FROYO;
	public final String GINGERBREAD;
	public final String HONEYCOMB;
	public final String ICE_CREAM_SANDWICH;
    public final String JELLY_BEAN;
	
	private TelephonyManager mTelephonyManager;
	
	public Platform(Context context) {
		super(context);
		ECLAIR = context.getString(R.string.platform_eclair);
		FROYO = context.getString(R.string.platform_froyo);
		GINGERBREAD = context.getString(R.string.platform_gingerbread);
		HONEYCOMB = context.getString(R.string.platform_honeycomb);
		ICE_CREAM_SANDWICH = context.getString(R.string.platform_ice_cream_sandwich);
		JELLY_BEAN = context.getString(R.string.platform_jelly_bean);

		mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
	}
	
	public String getVersionName(int version) {
		switch (version) {
		case 7: return ECLAIR;
		case 8: return FROYO;
		case 9: // g
		case 10: return GINGERBREAD;
		case 11: // h
		case 12: // h
		case 13: return HONEYCOMB;
		case 14: // i
		case 15: return ICE_CREAM_SANDWICH;
        case 16: // j
        case 17: return JELLY_BEAN;
		default: return null;
		}
	}
	
	public static String getKernelVersion() {
		List<String> list = ShellHelper.getProc(PROC_VERSION);
		if (list == null || list.isEmpty()) return null;
		String[] parts = list.get(0).split("\\s+");		
		if (parts.length >= 4) 	return parts[2] + " " + parts[3];
		else if (parts.length == 3) return parts[2];
		return null;	
	}
}
