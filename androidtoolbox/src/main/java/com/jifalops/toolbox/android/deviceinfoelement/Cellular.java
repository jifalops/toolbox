package com.jifalops.toolbox.android.deviceinfoelement;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;

import com.jifalops.toolbox.android.R;
import com.deviceinfoapp.util.ShellHelper;

public class Cellular extends ActiveElement {
	
	public static final int FREQUENCY_HIGH = 1000;
	public static final int FREQUENCY_MEDIUM = 2000;
	public static final int FREQUENCY_LOW = 5000;

	private static final int ACTIVE_ACTIONS = 8;
	public static final int ACTION_FORWARDING_INDICATOR = 0;
    public static final int ACTION_CALL_STATE = 1;
    public static final int ACTION_CELL_LOCATION = 2;
    public static final int ACTION_DATA_ACTIVITY = 3;
    public static final int ACTION_DATA_CONNECTION_STATE = 4;
    public static final int ACTION_MSG_WAITING = 5;
    public static final int ACTION_SERVICE_STATE = 6;
    public static final int ACTION_SIGNAL_STRENGTHS = 7;

	/** Methods correspond to PhoneStateListener methods */
	public interface Callbacks extends ActiveElement.Callbacks {
		void onCallForwardingIndicatorChanged(boolean cfi);
		void onCallStateChanged(int state, String incomingNumber);
		void onCellLocationChanged(CellLocation location);
		void onDataActivity(int direction);
		void onDataConnectionStateChanged(int state, int networkType);
		void onMessageWaitingIndicatorChanged(boolean mwi);
		void onServiceStateChanged(ServiceState serviceState);
		void onSignalStrengthsChanged(SignalStrength signalStrength);
	}

    // TelephonyManager Strings
    public final String CALL_STATE_IDLE;
    public final String CALL_STATE_OFFHOOK;
    public final String CALL_STATE_RINGING;
    public final String DATA_ACTIVITY_DORMANT;
    public final String DATA_ACTIVITY_IN;
    public final String DATA_ACTIVITY_INOUT;
    public final String DATA_ACTIVITY_NONE;
    public final String DATA_ACTIVITY_OUT;
    public final String DATA_CONNECTED;
    public final String DATA_CONNECTING;
    public final String DATA_DISCONNECTED;
    public final String DATA_SUSPENDED;
    public final String NETWORK_TYPE_1xRTT;
    public final String NETWORK_TYPE_CDMA;
    public final String NETWORK_TYPE_EDGE;
    public final String NETWORK_TYPE_EHRPD;
    public final String NETWORK_TYPE_EVDO_0;
    public final String NETWORK_TYPE_EVDO_A;
    public final String NETWORK_TYPE_EVDO_B;
    public final String NETWORK_TYPE_GPRS;
    public final String NETWORK_TYPE_HSDPA;
    public final String NETWORK_TYPE_HSPA;
    public final String NETWORK_TYPE_HSPAP;
    public final String NETWORK_TYPE_HSUPA;
    public final String NETWORK_TYPE_IDEN;
    public final String NETWORK_TYPE_LTE;
    public final String NETWORK_TYPE_UMTS;
    public final String NETWORK_TYPE_UNKNOWN;
    public final String PHONE_TYPE_CDMA;
    public final String PHONE_TYPE_GSM;
    public final String PHONE_TYPE_NONE;
    public final String PHONE_TYPE_SIP;
    public final String SIM_STATE_ABSENT;
    public final String SIM_STATE_NETWORK_LOCKED;
    public final String SIM_STATE_PIN_REQUIRED;
    public final String SIM_STATE_PUK_REQUIRED;
    public final String SIM_STATE_READY;
    public final String SIM_STATE_UNKNOWN;    
    // NeighboringCellInfo Strings
    public final String UNKNOWN_CID;
    public final String UNKNOWN_RSSI;    
    // ServiceState Strings
    public final String STATE_EMERGENCY_ONLY;
    public final String STATE_IN_SERVICE;
    public final String STATE_OUT_OF_SERVICE;
    public final String STATE_POWER_OFF;
    
    private final TelephonyManager mTelephonyManager;    
    private PhoneStateListener mListener;
    
    private final int mMcc;
    private final int mMnc;
    
    private ServiceState mServiceState;
    private CellLocation mCellLocation;
    private SignalStrength mSignalStrength;
    
	public Cellular(Context context, Callbacks callbacks) {
		super(context, callbacks);
		
		mTelephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);				
		
		Configuration config = context.getResources().getConfiguration();
		mMcc = config.mcc;
		mMnc = config.mnc;
		
		mServiceState = new ServiceState();
		mCellLocation = mTelephonyManager.getCellLocation();
		
		CALL_STATE_IDLE = context.getString(R.string.call_state_idle);
		CALL_STATE_OFFHOOK = context.getString(R.string.call_state_offhook);
		CALL_STATE_RINGING = context.getString(R.string.call_state_ringing);
		DATA_ACTIVITY_DORMANT = context.getString(R.string.data_activity_dormant);
		DATA_ACTIVITY_IN = context.getString(R.string.data_activity_in);
	    DATA_ACTIVITY_INOUT = context.getString(R.string.data_activity_inout);
	    DATA_ACTIVITY_NONE = context.getString(R.string.data_activity_none);
	    DATA_ACTIVITY_OUT = context.getString(R.string.data_activity_out);
	    DATA_CONNECTED = context.getString(R.string.data_connected);
	    DATA_CONNECTING = context.getString(R.string.data_connecting);
	    DATA_DISCONNECTED = context.getString(R.string.data_disconnected);
	    DATA_SUSPENDED = context.getString(R.string.data_suspended);
	    NETWORK_TYPE_1xRTT = context.getString(R.string.network_type_1xrtt);
	    NETWORK_TYPE_CDMA = context.getString(R.string.network_type_cmda);
	    NETWORK_TYPE_EDGE = context.getString(R.string.network_type_edge);
	    NETWORK_TYPE_EHRPD = context.getString(R.string.network_type_ehrpd);
	    NETWORK_TYPE_EVDO_0 = context.getString(R.string.network_type_evdo_0);
	    NETWORK_TYPE_EVDO_A = context.getString(R.string.network_type_evdo_a);
	    NETWORK_TYPE_EVDO_B = context.getString(R.string.network_type_evdo_b);
	    NETWORK_TYPE_GPRS = context.getString(R.string.network_type_gprs);
	    NETWORK_TYPE_HSDPA = context.getString(R.string.network_type_hsdpa);
	    NETWORK_TYPE_HSPA = context.getString(R.string.network_type_hspa);
	    NETWORK_TYPE_HSPAP = context.getString(R.string.network_type_hspap);
	    NETWORK_TYPE_HSUPA = context.getString(R.string.network_type_hsupa);
	    NETWORK_TYPE_IDEN = context.getString(R.string.network_type_iden);
	    NETWORK_TYPE_LTE = context.getString(R.string.network_type_lte);
	    NETWORK_TYPE_UMTS = context.getString(R.string.network_type_umts);
	    NETWORK_TYPE_UNKNOWN = context.getString(R.string.network_type_unknown);
	    PHONE_TYPE_CDMA = context.getString(R.string.phone_type_cdma);
	    PHONE_TYPE_GSM = context.getString(R.string.phone_type_gsm);
	    PHONE_TYPE_NONE = context.getString(R.string.phone_type_none);
	    PHONE_TYPE_SIP = context.getString(R.string.phone_type_sip);
	    SIM_STATE_ABSENT = context.getString(R.string.sim_state_absent);
	    SIM_STATE_NETWORK_LOCKED = context.getString(R.string.sim_state_network_locked);
	    SIM_STATE_PIN_REQUIRED = context.getString(R.string.sim_state_pin_required);
	    SIM_STATE_PUK_REQUIRED = context.getString(R.string.sim_state_puk_required);
	    SIM_STATE_READY = context.getString(R.string.sim_state_ready);
	    SIM_STATE_UNKNOWN = context.getString(R.string.sim_state_unknown);    
	    // NeighboringCellInfo Strings
	    UNKNOWN_CID = context.getString(R.string.cell_info_unknown_cid);
	    UNKNOWN_RSSI = context.getString(R.string.cell_info_unknown_rssi);    
	    // ServiceState Strings
	    STATE_EMERGENCY_ONLY = context.getString(R.string.service_state_emergency_only);
	    STATE_IN_SERVICE = context.getString(R.string.service_state_in_service);
	    STATE_OUT_OF_SERVICE = context.getString(R.string.service_state_out_of_service);
	    STATE_POWER_OFF = context.getString(R.string.service_state_power_off);

        setActiveActionCount(ACTIVE_ACTIONS);
        setActionThrottle(ACTION_CELL_LOCATION, FREQUENCY_MEDIUM);
	}
	
	public TelephonyManager getTelephonyManager() {
		return mTelephonyManager;
	}
	
	public PhoneStateListener getPhoneStateListener() {
		return mListener;
	}
	
	public ServiceState getServiceState() {
		return mServiceState;
	}
	
	public CellLocation getCellLocation() {
		return mCellLocation;
	}
	
	public SignalStrength getSignalStrength() {
		return mSignalStrength;
	}
	
	public int getMcc() {
		return mMcc;
	}
	
	public int getMnc() {
		return mMnc;
	}
	
	public String getCallState(int state) { 
		switch (state) {
		case TelephonyManager.CALL_STATE_IDLE: return CALL_STATE_IDLE;
		case TelephonyManager.CALL_STATE_OFFHOOK: return CALL_STATE_OFFHOOK;
		case TelephonyManager.CALL_STATE_RINGING: return CALL_STATE_RINGING;
		}
		return null;
	}

	public String getDataActivity(int activity) {
		switch (activity) {
		case TelephonyManager.DATA_ACTIVITY_DORMANT: return DATA_ACTIVITY_DORMANT;
		case TelephonyManager.DATA_ACTIVITY_IN: return DATA_ACTIVITY_IN;
		case TelephonyManager.DATA_ACTIVITY_INOUT: return DATA_ACTIVITY_INOUT;
		case TelephonyManager.DATA_ACTIVITY_NONE: return DATA_ACTIVITY_NONE;
		case TelephonyManager.DATA_ACTIVITY_OUT: return DATA_ACTIVITY_OUT;
		}
		return null;
	}

	public String getDataState(int state) {
		switch (state) {
		case TelephonyManager.DATA_CONNECTED: return DATA_CONNECTED;
		case TelephonyManager.DATA_CONNECTING: return DATA_CONNECTING;
		case TelephonyManager.DATA_DISCONNECTED: return DATA_DISCONNECTED;
		case TelephonyManager.DATA_SUSPENDED: return DATA_SUSPENDED;
		}
		return null;
	}
	
	public String getNetworkType(int type) {
		switch (type) { 
		case TelephonyManager.NETWORK_TYPE_1xRTT: return NETWORK_TYPE_1xRTT;
		case TelephonyManager.NETWORK_TYPE_CDMA: return NETWORK_TYPE_CDMA;
		case TelephonyManager.NETWORK_TYPE_EDGE: return NETWORK_TYPE_EDGE;
		case TelephonyManager.NETWORK_TYPE_EVDO_0: return NETWORK_TYPE_EVDO_0;
		case TelephonyManager.NETWORK_TYPE_EVDO_A: return NETWORK_TYPE_EVDO_A;
		case TelephonyManager.NETWORK_TYPE_GPRS: return NETWORK_TYPE_GPRS;
		case TelephonyManager.NETWORK_TYPE_HSDPA: return NETWORK_TYPE_HSDPA;
		case TelephonyManager.NETWORK_TYPE_HSPA: return NETWORK_TYPE_HSPA;
		case TelephonyManager.NETWORK_TYPE_HSUPA: return NETWORK_TYPE_HSUPA;
		case TelephonyManager.NETWORK_TYPE_UMTS: return NETWORK_TYPE_UMTS;
		case TelephonyManager.NETWORK_TYPE_UNKNOWN: return NETWORK_TYPE_UNKNOWN;
		}
		if (API >= 11 && type == TelephonyManager.NETWORK_TYPE_EHRPD) return NETWORK_TYPE_EHRPD;
		if (API >= 9 && type == TelephonyManager.NETWORK_TYPE_EVDO_B) return NETWORK_TYPE_EVDO_B;
		if (API >= 13 && type == TelephonyManager.NETWORK_TYPE_HSPAP) return NETWORK_TYPE_HSPAP;
		if (API >= 8 && type == TelephonyManager.NETWORK_TYPE_IDEN) return NETWORK_TYPE_IDEN;
		if (API >= 11 && type == TelephonyManager.NETWORK_TYPE_LTE) return NETWORK_TYPE_LTE;
		return null;
	}
	
	public String getPhoneType(int type) {
		switch (type) { 
		case TelephonyManager.PHONE_TYPE_CDMA: return PHONE_TYPE_CDMA;
		case TelephonyManager.PHONE_TYPE_GSM: return PHONE_TYPE_GSM;
		case TelephonyManager.PHONE_TYPE_NONE: return PHONE_TYPE_NONE;
		}
		if (API >= 11 && type == TelephonyManager.PHONE_TYPE_SIP) return PHONE_TYPE_SIP;
		return null;
	}
	
	public String getSimState(int state) {
		switch (state) { 
		case TelephonyManager.SIM_STATE_ABSENT: return SIM_STATE_ABSENT;
		case TelephonyManager.SIM_STATE_NETWORK_LOCKED: return SIM_STATE_NETWORK_LOCKED;
		case TelephonyManager.SIM_STATE_PIN_REQUIRED: return SIM_STATE_PIN_REQUIRED;
		case TelephonyManager.SIM_STATE_PUK_REQUIRED: return SIM_STATE_PUK_REQUIRED;
		case TelephonyManager.SIM_STATE_READY: return SIM_STATE_READY;
		case TelephonyManager.SIM_STATE_UNKNOWN: return SIM_STATE_UNKNOWN;
		}
		return null;
	}
	
	public String getServiceState(int state) {
		switch (state) { 
		case ServiceState.STATE_EMERGENCY_ONLY: return STATE_EMERGENCY_ONLY;
		case ServiceState.STATE_IN_SERVICE: return STATE_IN_SERVICE;
		case ServiceState.STATE_OUT_OF_SERVICE: return STATE_OUT_OF_SERVICE;
		case ServiceState.STATE_POWER_OFF: return STATE_POWER_OFF;
		}
		return null;
	}
	
	public String getCallState() { 
		return getCallState(mTelephonyManager.getCallState());
	}
	
	public String getDataActivity() {
		return getDataActivity(mTelephonyManager.getDataActivity());
	}
	
	public String getDataState() {
		return getDataState(mTelephonyManager.getDataState());
	}
	
	public String getNetworkType() {
		return getNetworkType(mTelephonyManager.getNetworkType());
	}
	
	public String getPhoneType() {
		return getPhoneType(mTelephonyManager.getPhoneType());
	}
	
	public String getSimState() {
		return getSimState(mTelephonyManager.getSimState());
	}
	
	public String getServiceStateString() {
		return getServiceState(mServiceState.getState());
	}

	public static String getRadioVersion() {
		if (API >= 14) return Build.getRadioVersion();
		else if (API >= 8) return Build.RADIO;
		return null;
	}
	
	public static String getBaseband() {
		String bb = ShellHelper.getProp("gsm.version.baseband");
		if (bb == null || bb.equalsIgnoreCase("unknown")) {
			bb = ShellHelper.getProp("ro.baseband");
		}
		return bb;
	}
	
	public static String getRilVersion() {
		return ShellHelper.getProp("gsm.version.ril-impl");
	}
	
	public static String getRilBarcode() {
		return ShellHelper.getProp("ro.ril.barcode");
	}

    @Override
    public void start() {
        if (mIsActive) return;
        mListener = new MyPhoneStateListener();
        mTelephonyManager.listen(mListener,
                PhoneStateListener.LISTEN_CALL_FORWARDING_INDICATOR
                        | PhoneStateListener.LISTEN_CALL_STATE
                        | PhoneStateListener.LISTEN_CELL_LOCATION
                        | PhoneStateListener.LISTEN_DATA_ACTIVITY
                        | PhoneStateListener.LISTEN_DATA_CONNECTION_STATE
                        | PhoneStateListener.LISTEN_MESSAGE_WAITING_INDICATOR
                        | PhoneStateListener.LISTEN_SERVICE_STATE
                        | PhoneStateListener.LISTEN_SIGNAL_STRENGTHS
        );
        mIsActive = true;
    }

    @Override
    public void stop() {
        
        mTelephonyManager.listen(mListener, PhoneStateListener.LISTEN_NONE);
        mIsActive = false;
    }

	private class MyPhoneStateListener extends PhoneStateListener {
		@Override
		public void onCallForwardingIndicatorChanged(boolean cfi) {
            if (!isActionAllowed(ACTION_FORWARDING_INDICATOR)) return;

            setActionTime(ACTION_FORWARDING_INDICATOR);
			((Callbacks) mCallbacks).onCallForwardingIndicatorChanged(cfi);
		}
		
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
            if (!isActionAllowed(ACTION_CALL_STATE)) return;

            setActionTime(ACTION_CALL_STATE);
			((Callbacks) mCallbacks).onCallStateChanged(state, incomingNumber);
		}
		
		@Override
		public void onCellLocationChanged(CellLocation location) {
			if (!isActionAllowed(ACTION_CELL_LOCATION)) return;

            mCellLocation = location;

            setActionTime(ACTION_CELL_LOCATION);
            ((Callbacks) mCallbacks).onCellLocationChanged(location);
		}
		
		@Override
		public void onDataActivity(int direction) {
            if (!isActionAllowed(ACTION_DATA_ACTIVITY)) return;

            setActionTime(ACTION_DATA_ACTIVITY);
			((Callbacks) mCallbacks).onDataActivity(direction);
		}
		
		@Override
		public void onDataConnectionStateChanged(int state, int networkType) {
            if (!isActionAllowed(ACTION_DATA_CONNECTION_STATE)) return;

            setActionTime(ACTION_DATA_CONNECTION_STATE);
            ((Callbacks) mCallbacks).onDataConnectionStateChanged(state, networkType);
		}
		
		@Override
		public void onMessageWaitingIndicatorChanged(boolean mwi) {
            if (!isActionAllowed(ACTION_MSG_WAITING)) return;

            setActionTime(ACTION_MSG_WAITING);
			((Callbacks) mCallbacks).onMessageWaitingIndicatorChanged(mwi);
		}
		
		@Override
		public void onServiceStateChanged(ServiceState serviceState) {
            if (!isActionAllowed(ACTION_SERVICE_STATE)) return;

            mServiceState = serviceState;

            setActionTime(ACTION_SERVICE_STATE);
			((Callbacks) mCallbacks).onServiceStateChanged(serviceState);
		}
		
		@Override
		public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            if (!isActionAllowed(ACTION_SIGNAL_STRENGTHS)) return;

            mSignalStrength = signalStrength;

            setActionTime(ACTION_SIGNAL_STRENGTHS);
			((Callbacks) mCallbacks).onSignalStrengthsChanged(signalStrength);
		}
	}
}
