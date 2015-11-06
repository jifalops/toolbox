package com.jifalops.toolbox.android.deviceinfoelement;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;

import com.jifalops.toolbox.android.R;

public class Battery extends ActiveElement {

    private static final int ACTIVE_ACTIONS = 1;
    public static final int ACTION_BATTERY = 0;

	public interface Callbacks extends ActiveElement.Callbacks {
		void onReceive(Context context, Intent intent);
	}
	
	// BatteryManager constants
	public final String HEALTH_COLD;
	public final String HEALTH_DEAD;
	public final String HEALTH_GOOD;
	public final String HEALTH_OVERHEAT;
	public final String HEALTH_OVER_VOLTAGE;
	public final String HEALTH_UNKNOWN;
	public final String HEALTH_UNSPECIFIED_FAILURE;
	public final String PLUGGED_AC;
	public final String PLUGGED_USB;
	public final String STATUS_CHARGING;
	public final String STATUS_DISCHARGING;
	public final String STATUS_FULL;
	public final String STATUS_NOT_CHARGING;
	public final String STATUS_UNKNOWN;
		
	private BatteryChangedBroadcastReceiver mBatteryReceiver;
	private IntentFilter mIntentFilter;

	private int mLevel;
	private int mMaxLevel;
	private int mVoltage; 	// mV
	private float mTemp; 		// C
	private String mTechnology;
	private int mStatus;
	private int mHealth;
	private int mPluggedStatus;
	private int mIconResourceId;
	private boolean mPresent; // battery exists
	
	public Battery(Context context, Callbacks callbacks) {
		super(context, callbacks);
		HEALTH_COLD = context.getString(R.string.battery_health_cold);
		HEALTH_DEAD = context.getString(R.string.battery_health_dead);
		HEALTH_GOOD = context.getString(R.string.battery_health_good);
		HEALTH_OVERHEAT = context.getString(R.string.battery_health_overheat);
		HEALTH_OVER_VOLTAGE = context.getString(R.string.battery_health_over_voltage);
		HEALTH_UNKNOWN = context.getString(R.string.battery_health_unknown);
		HEALTH_UNSPECIFIED_FAILURE = context.getString(R.string.battery_health_unspecified_failure);
		PLUGGED_AC = context.getString(R.string.battery_plugged_ac);
		PLUGGED_USB = context.getString(R.string.battery_plugged_usb);
		STATUS_CHARGING = context.getString(R.string.battery_status_charging);
		STATUS_DISCHARGING = context.getString(R.string.battery_status_discharging);
		STATUS_FULL = context.getString(R.string.battery_status_full);
		STATUS_NOT_CHARGING = context.getString(R.string.battery_status_not_charging);
		STATUS_UNKNOWN = context.getString(R.string.battery_status_unknown);
		
		mBatteryReceiver = new BatteryChangedBroadcastReceiver();
		mIntentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);

        setActiveActionCount(ACTIVE_ACTIONS);

	}
	
	public String getHealth(int health) {
		switch (health) {
		case BatteryManager.BATTERY_HEALTH_DEAD: return HEALTH_DEAD;
		case BatteryManager.BATTERY_HEALTH_GOOD: return HEALTH_GOOD;
		case BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE: return HEALTH_OVER_VOLTAGE;
		case BatteryManager.BATTERY_HEALTH_OVERHEAT: return HEALTH_OVERHEAT;
		case BatteryManager.BATTERY_HEALTH_UNKNOWN: return HEALTH_UNKNOWN;
		case BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE: return HEALTH_UNSPECIFIED_FAILURE;
		}
		if (Build.VERSION.SDK_INT >= 11 && mHealth == BatteryManager.BATTERY_HEALTH_COLD) return HEALTH_COLD;		
		return null;
	}
	
	public String getPluggedInStatus(int status) {
		switch (status) {
		case BatteryManager.BATTERY_PLUGGED_AC: return PLUGGED_AC;
		case BatteryManager.BATTERY_PLUGGED_USB: return PLUGGED_USB;
		default: return null;
		}
	}
	
	public String getChargingStatus(int status) {
		switch (status) {
		case BatteryManager.BATTERY_STATUS_CHARGING: return STATUS_CHARGING;
		case BatteryManager.BATTERY_STATUS_DISCHARGING: return STATUS_DISCHARGING;
		case BatteryManager.BATTERY_STATUS_NOT_CHARGING: return STATUS_NOT_CHARGING;
		case BatteryManager.BATTERY_STATUS_FULL: return STATUS_FULL;
		case BatteryManager.BATTERY_STATUS_UNKNOWN: return STATUS_UNKNOWN;
		default: return null;
		}		
	}
	
	public String getChargingStatus() {
		return getChargingStatus(mStatus);
	}
	
	public String getHealth() {
		return getHealth(mHealth);
	}
	
	public String getPluggedInStatus() {
		return getPluggedInStatus(mPluggedStatus);
	}


	public BroadcastReceiver getReceiver() {
		return mBatteryReceiver;
	}
	
	public IntentFilter getIntentFilter() {
		return mIntentFilter;
	}
	
	public int getLevel() {
		return mLevel;
	}
	
	public int getLevelMax() {
		return mMaxLevel;
	} 
	
	/** Get the reported voltage in milli volts */
	public int getVoltage() {
		return mVoltage;
	}
	
	/** Get the reported temperature in celsius */
	public float getTemperature() {
		return mTemp;
	}
	
	public String getTechnology() {
		return mTechnology;
	}
	
	public int getChargingStatusInt() {
		return mStatus;
	}
	
	public int getHealthInt() {
		return mHealth;
	}
	
	public int getPluggedInStatusInt() {
		return mPluggedStatus;
	}
	
	public int getIconResourceId() {
		return mIconResourceId;
	}
	
	public boolean isBatteryPresent() {
		return mPresent;
	}

    @Override
    public void start() {
        if (mIsActive) return;
        mContext.registerReceiver(mBatteryReceiver, mIntentFilter);
        mIsActive = true;
    }

    @Override
    public void stop() {
        if (!mIsActive) return;
        mContext.unregisterReceiver(mBatteryReceiver);
        mIsActive = false;
    }

	private class BatteryChangedBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
            if (!isActionAllowed(ACTION_BATTERY)) return;

            mLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
			mMaxLevel = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 0);
			mVoltage = intent.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0);
			mTemp = intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10f;
			mTechnology = intent.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY);
			mStatus = intent.getIntExtra(BatteryManager.EXTRA_STATUS, 0);
			mHealth = intent.getIntExtra(BatteryManager.EXTRA_HEALTH, 0);
			mPluggedStatus = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
			mIconResourceId = intent.getIntExtra(BatteryManager.EXTRA_ICON_SMALL, 0);
			mPresent = intent.getBooleanExtra(BatteryManager.EXTRA_PRESENT, true);

            setActionTime(ACTION_BATTERY);
            ((Callbacks) mCallbacks).onReceive(context, intent);
		}
	}
}
