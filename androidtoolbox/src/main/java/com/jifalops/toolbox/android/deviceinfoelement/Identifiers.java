package com.jifalops.toolbox.android.deviceinfoelement;

import android.content.Context;
import android.os.Build;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;

import com.deviceinfoapp.util.ShellHelper;

import java.util.List;


public class Identifiers extends AbsElement {
    
    private static final String PROC_PHONE_ID = "phoneid";
    private static final String NULL_STRING = "null";
    private static final String PROP_SERIAL = "ro.serialno";

	public final String ANDROID_ID;
	public final String DEVICE_ID;
	public final String PHONE_ID;
	public final String SIM_SERIAL;
	public final String LINE_1_NUMBER;
	public final String DEVICE_SERIAL;
	public final String SUBSCRIBER_ID;
	
	public Identifiers(Context context) {
		super(context);
		TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		DEVICE_ID = tm.getDeviceId();
		SIM_SERIAL = tm.getSimSerialNumber();
		LINE_1_NUMBER = tm.getLine1Number();
		SUBSCRIBER_ID = tm.getSubscriberId();
		ANDROID_ID = Secure.getString(context.getContentResolver(), Secure.ANDROID_ID);
		DEVICE_SERIAL = getDeviceSerial();		
		List<String> list = ShellHelper.getProc(PROC_PHONE_ID);
		if (list == null || list.isEmpty()) PHONE_ID = null;
		else PHONE_ID = list.get(0);
	}
	
	private String getDeviceSerial() {
		String s = null;
		if (API >= 9) {
			s = Build.SERIAL;
			if (s != null && s.length() > 0 
				&& !s.equals(Build.UNKNOWN) && !s.equalsIgnoreCase(NULL_STRING)) {
				return s;
			}
		}
		return ShellHelper.getProp(PROP_SERIAL);
	}
}
