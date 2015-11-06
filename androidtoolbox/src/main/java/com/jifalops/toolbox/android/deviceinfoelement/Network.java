package com.jifalops.toolbox.android.deviceinfoelement;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.DetailedState;
import android.net.NetworkInfo.State;

import com.jifalops.toolbox.android.R;

import java.util.ArrayList;
import java.util.List;

public class Network extends AbsElement {

	// ConnectivityManager strings
	public final String TYPE_BLUETOOTH;
	public final String TYPE_DUMMY;
	public final String TYPE_ETHERNET;
	public final String TYPE_MOBILE;
	public final String TYPE_MOBILE_DUN;
	public final String TYPE_MOBILE_HIPRI;
	public final String TYPE_MOBILE_MMS;
	public final String TYPE_MOBILE_SUPL;
	public final String TYPE_WIFI;
	public final String TYPE_WIMAX;
	// NetworkInfo.DetailedState strings
	public final String STATE_AUTHENTICATING;
	public final String STATE_BLOCKED;
	public final String STATE_CONNECTED;
	public final String STATE_CONNECTING;
	public final String STATE_DISCONNECTED;
	public final String STATE_DISCONNECTING;
	public final String STATE_FAILED;
	public final String STATE_IDLE;
	public final String STATE_OBTAINING_IP;
	public final String STATE_SCANNING;
	public final String STATE_SUSPENDED;
	public final String STATE_UNKNOWN;
	
	private ConnectivityManager mConnectivityManager;
	
	public Network(Context context) {	
		super(context);
		TYPE_BLUETOOTH = context.getString(R.string.connection_type_bluetooth);
		TYPE_DUMMY = context.getString(R.string.connection_type_dummy);
		TYPE_ETHERNET = context.getString(R.string.connection_type_ethernet);
		TYPE_MOBILE = context.getString(R.string.connection_type_mobile);
		TYPE_MOBILE_DUN = context.getString(R.string.connection_type_mobile_dun);
		TYPE_MOBILE_HIPRI = context.getString(R.string.connection_type_mobile_hipri);
		TYPE_MOBILE_MMS = context.getString(R.string.connection_type_mobile_mms);
		TYPE_MOBILE_SUPL = context.getString(R.string.connection_type_mobile_supl);
		TYPE_WIFI = context.getString(R.string.connection_type_wifi);
		TYPE_WIMAX = context.getString(R.string.connection_type_wimax);
		STATE_AUTHENTICATING = context.getString(R.string.network_state_authenticating);
		STATE_BLOCKED = context.getString(R.string.network_state_blocked);
		STATE_CONNECTED = context.getString(R.string.network_state_connected);
		STATE_CONNECTING = context.getString(R.string.network_state_connecting);
		STATE_DISCONNECTED = context.getString(R.string.network_state_disconnected);
		STATE_DISCONNECTING = context.getString(R.string.network_state_disconnecting);
		STATE_FAILED = context.getString(R.string.network_state_failed);
		STATE_IDLE = context.getString(R.string.network_state_idle);
		STATE_OBTAINING_IP = context.getString(R.string.network_state_obtaining_ip);
		STATE_SCANNING = context.getString(R.string.network_state_scanning);
		STATE_SUSPENDED = context.getString(R.string.network_state_suspended);
		STATE_UNKNOWN = context.getString(R.string.network_state_unknown);
		
		mConnectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);		
	}
	
	public String getNetworkTypeString(int type) {
		if (type == ConnectivityManager.TYPE_WIFI) return TYPE_WIFI;
		if (type == ConnectivityManager.TYPE_MOBILE) return TYPE_MOBILE;
		if (API >= 13 && type == ConnectivityManager.TYPE_BLUETOOTH) return TYPE_BLUETOOTH;
		if (API >= 14 && type == ConnectivityManager.TYPE_DUMMY) return TYPE_DUMMY;
		if (API >= 13 && type == ConnectivityManager.TYPE_ETHERNET) return TYPE_ETHERNET;
		if (API >= 8 && type == ConnectivityManager.TYPE_MOBILE_DUN) return TYPE_MOBILE_DUN;
		if (API >= 8 && type == ConnectivityManager.TYPE_MOBILE_HIPRI) return TYPE_MOBILE_HIPRI;
		if (API >= 8 && type == ConnectivityManager.TYPE_MOBILE_MMS) return TYPE_MOBILE_MMS;
		if (API >= 8 && type == ConnectivityManager.TYPE_MOBILE_SUPL) return TYPE_MOBILE_SUPL;
		if (API >= 8 && type == ConnectivityManager.TYPE_WIMAX) return TYPE_WIMAX;
		return null;
	}
	
	public String getStateString(DetailedState state) {
		// TODO Dunno required API levels
		try { if (state == DetailedState.AUTHENTICATING) return STATE_AUTHENTICATING; }
		catch (NoSuchFieldError ignored) {}
		try { if (state == DetailedState.BLOCKED) return STATE_BLOCKED; }
		catch (NoSuchFieldError ignored) {}
		try { if (state == DetailedState.CONNECTED) return STATE_CONNECTED; }
		catch (NoSuchFieldError ignored) {}
		try { if (state == DetailedState.CONNECTING) return STATE_CONNECTING; }
		catch (NoSuchFieldError ignored) {}
		try { if (state == DetailedState.DISCONNECTED) return STATE_DISCONNECTED; }
		catch (NoSuchFieldError ignored) {}
		try { if (state == DetailedState.DISCONNECTING) return STATE_DISCONNECTING; }
		catch (NoSuchFieldError ignored) {}
		try { if (state == DetailedState.FAILED) return STATE_FAILED; }
		catch (NoSuchFieldError ignored) {}
		try { if (state == DetailedState.IDLE) return STATE_IDLE; }
		catch (NoSuchFieldError ignored) {}
		try { if (state == DetailedState.OBTAINING_IPADDR) return STATE_OBTAINING_IP; }
		catch (NoSuchFieldError ignored) {}
		try { if (state == DetailedState.SCANNING) return STATE_SCANNING; }
		catch (NoSuchFieldError ignored) {}
		try { if (state == DetailedState.SUSPENDED) return STATE_SUSPENDED; }
		catch (NoSuchFieldError ignored) {}
		return null;
	}
	
	public String getStateString(State state) {
		// TODO Dunno required API levels
		try { if (state == State.CONNECTED) return STATE_CONNECTED; }
		catch (NoSuchFieldError ignored) {}
		try { if (state == State.CONNECTING) return STATE_CONNECTING; }
		catch (NoSuchFieldError ignored) {}
		try { if (state == State.DISCONNECTED) return STATE_DISCONNECTED; }
		catch (NoSuchFieldError ignored) {}
		try { if (state == State.DISCONNECTING) return STATE_DISCONNECTING; }
		catch (NoSuchFieldError ignored) {}
		try { if (state == State.SUSPENDED) return STATE_SUSPENDED; }
		catch (NoSuchFieldError ignored) {}
		try { if (state == State.UNKNOWN) return STATE_UNKNOWN; }
		catch (NoSuchFieldError ignored) {}
		return null;
	}
	
	public String getNetworkPreferenceString() {
		return getNetworkTypeString(mConnectivityManager.getNetworkPreference());
	}
	
	public ConnectivityManager getConnectivityManager() {
		return mConnectivityManager;
	}
	
	public boolean getBackgroundDataSetting() {
		if (API < 14) return mConnectivityManager.getBackgroundDataSetting();
		return mConnectivityManager.getActiveNetworkInfo() != null;
	}
	
	public boolean isNetworkTypeValid(int type) {		
		if (getNetworkTypeString(type) == null) return false;
		return ConnectivityManager.isNetworkTypeValid(type);
	}
	
	public boolean isNetworkTypeValid(String type) {		
		if (type.equals(TYPE_WIFI)) return ConnectivityManager
				.isNetworkTypeValid(ConnectivityManager.TYPE_WIFI);
		if (type.equals(TYPE_MOBILE)) return ConnectivityManager
				.isNetworkTypeValid(ConnectivityManager.TYPE_MOBILE);
		
		if (API < 8) return false;
		if (type.equals(TYPE_MOBILE_DUN)) return ConnectivityManager
				.isNetworkTypeValid(ConnectivityManager.TYPE_MOBILE_DUN);
		if (type.equals(TYPE_MOBILE_HIPRI)) return ConnectivityManager
				.isNetworkTypeValid(ConnectivityManager.TYPE_MOBILE_HIPRI);
		if (type.equals(TYPE_MOBILE_MMS)) return ConnectivityManager
				.isNetworkTypeValid(ConnectivityManager.TYPE_MOBILE_MMS);
		if (type.equals(TYPE_MOBILE_SUPL)) return ConnectivityManager
				.isNetworkTypeValid(ConnectivityManager.TYPE_MOBILE_SUPL);
		if (type.equals(TYPE_WIMAX)) return ConnectivityManager
				.isNetworkTypeValid(ConnectivityManager.TYPE_WIMAX);
		
		if (API < 13) return false;
		if (type.equals(TYPE_BLUETOOTH)) return ConnectivityManager
				.isNetworkTypeValid(ConnectivityManager.TYPE_BLUETOOTH);
		if (type.equals(TYPE_ETHERNET)) return ConnectivityManager
				.isNetworkTypeValid(ConnectivityManager.TYPE_ETHERNET);
		
		if (API < 14) return false;
		if (type.equals(TYPE_DUMMY)) return ConnectivityManager
				.isNetworkTypeValid(ConnectivityManager.TYPE_DUMMY);
		
		return false;
	}
	
	public String[] getValidNetworkTypes() {
		List<String> list = new ArrayList<String>();
		if (ConnectivityManager.isNetworkTypeValid(ConnectivityManager.TYPE_WIFI))
			list.add(TYPE_WIFI);
		if (ConnectivityManager.isNetworkTypeValid(ConnectivityManager.TYPE_MOBILE))
			list.add(TYPE_MOBILE);
		
		if (API >= 8) {
			if (ConnectivityManager.isNetworkTypeValid(ConnectivityManager.TYPE_MOBILE_DUN))
				list.add(TYPE_MOBILE_DUN);
			if (ConnectivityManager.isNetworkTypeValid(ConnectivityManager.TYPE_MOBILE_HIPRI))
				list.add(TYPE_MOBILE_HIPRI);
			if (ConnectivityManager.isNetworkTypeValid(ConnectivityManager.TYPE_MOBILE_MMS))
				list.add(TYPE_MOBILE_MMS);
			if (ConnectivityManager.isNetworkTypeValid(ConnectivityManager.TYPE_MOBILE_SUPL))
				list.add(TYPE_MOBILE_SUPL);
			if (ConnectivityManager.isNetworkTypeValid(ConnectivityManager.TYPE_WIMAX))
				list.add(TYPE_WIMAX);
		}
		if (API >= 13) {
			if (ConnectivityManager.isNetworkTypeValid(ConnectivityManager.TYPE_BLUETOOTH))
				list.add(TYPE_BLUETOOTH);
			if (ConnectivityManager.isNetworkTypeValid(ConnectivityManager.TYPE_ETHERNET))
				list.add(TYPE_ETHERNET);	
		}
		if (API >= 14) {		
			if (ConnectivityManager.isNetworkTypeValid(ConnectivityManager.TYPE_DUMMY))
				list.add(TYPE_DUMMY);
		}
		return list.toArray(new String[list.size()]);
	}
	
	public boolean isConnecting(NetworkInfo ni) {		
		return ni.isConnectedOrConnecting() && !ni.isConnected();
	}
}
