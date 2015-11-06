package com.jifalops.toolbox.android.deviceinfoelement;

import android.content.Context;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.GpsStatus;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.AsyncTask;
import android.os.Bundle;

import com.jifalops.toolbox.android.R;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

//TODO keep a best-guess location
// TODO use a separate thread for listeners and publishProgress when appropriate
public class Location extends ActiveElement implements GpsStatus.Listener, GpsStatus.NmeaListener, LocationListener {

    private static final int ACTIVE_ACTIONS = 7;
    public static final int ACTION_LOCATION_CHANGED = 0;
    public static final int ACTION_PROVIDER_DISABLED = 1;
    public static final int ACTION_PROVIDER_ENABLED = 2;
    public static final int ACTION_STATUS_CHANGED = 3;
    public static final int ACTION_ADDRESS_CHANGED = 4;
    public static final int ACTION_GPS_STATUS_CHANGED = 5;
    public static final int ACTION_NMEA_RECEIVED = 6;
	
	public interface Callbacks extends ActiveElement.Callbacks {
		/** Corresponds to LocationListener.onLocationChanged() */
		void onLocationChanged(android.location.Location location);
		/** Corresponds to LocationListener.onProviderDisabled() */
		void onProviderDisabled(String provider);
		/** Corresponds to LocationListener.onProviderEnabled() */
		void onProviderEnabled(String provider);
		/** Corresponds to LocationListener.onStatusChanged() */
		void onStatusChanged(String provider, int status, Bundle extras);
		/** Custom callback that is called after the Geocoder updates the closest address */
		void onAddressChanged(Address address, android.location.Location location);

        /** Corresponds to GpsStatus.Listener.onGpsStatusChanged() */
        void onGpsStatusChanged(int event);
        /** Corresponds to GpsStatus.NmeaListener.onNmeaReceived() */
        void onNmeaReceived(long timestamp, String nmea);
	}

	// Nmea update throttle (ms)
	public static final int NMEA_FREQUENCY_HIGH = 1000;
	public static final int NMEA_FREQUENCY_MEDIUM = 2000;
	public static final int NMEA_FREQUENCY_LOW = 3000;
	
	// GPS Status update throttle (ms)
	public static final int GPSSTATUS_FREQUENCY_HIGH = 1000;
	public static final int GPSSTATUS_FREQUENCY_MEDIUM = 2000;
	public static final int GPSSTATUS_FREQUENCY_LOW = 3000;

    // Status update throttle (ms)
    public static final int STATUS_FREQUENCY_HIGH = 1000;
    public static final int STATUS_FREQUENCY_MEDIUM = 2000;
    public static final int STATUS_FREQUENCY_LOW = 3000;
	
	// Network/GPS location update throttle (ms)
	public static final int LOCATION_FREQUENCY_HIGH = 1000;
	public static final int LOCATION_FREQUENCY_MEDIUM = 2000;
	public static final int LOCATION_FREQUENCY_LOW = 5000;
	
	// Reverse geocoding of location to an address throttle (ms)
	public static final int ADDRESS_FREQUENCY_HIGH = 2000;
	public static final int ADDRESS_FREQUENCY_MEDIUM = 5000;
	public static final int ADDRESS_FREQUENCY_LOW = 10000;
	
	public final String STATUS_AVAILABLE;
	public final String STATUS_OUT_OF_SERVICE;
	public final String STATUS_TEMPORARILY_UNAVAILABLE;	
	public final String ACCURACY_COARSE;
	public final String ACCURACY_FINE;	
	public final String POWER_REQUIREMENT_HIGH;
	public final String POWER_REQUIREMENT_MEDIUM;
	public final String POWER_REQUIREMENT_LOW;
	public final String POWER_REQUIREMENT_NONE;	
	public final String GPS_EVENT_FIRST_FIX;
	public final String GPS_EVENT_SATELLITE_STATUS;
	public final String GPS_EVENT_STARTED;
	public final String GPS_EVENT_STOPPED;

    private LocationManager mLocationManager;
    private Geocoder mGeocoder;

    private GpsStatus mGpsStatus;

    private int mGpsStatusUpdateFrequency;
    private int mNmeaUpdateFrequency;

    private android.location.Location mLastGeocodedLocation;

    private long mMinTime;
    private float mMinDistance;
	
	public Location(Context context, Callbacks callbacks) {
		super(context, callbacks);
		mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		mGeocoder = new Geocoder(context, Locale.getDefault());
		
		STATUS_AVAILABLE = context.getString(R.string.location_available);
		STATUS_OUT_OF_SERVICE = context.getString(R.string.location_out_of_service);
		STATUS_TEMPORARILY_UNAVAILABLE = context.getString(R.string.location_temporarily_unavailable);		
		ACCURACY_COARSE = context.getString(R.string.location_accuracy_coarse);
		ACCURACY_FINE = context.getString(R.string.location_accuracy_fine);		
		POWER_REQUIREMENT_HIGH = context.getString(R.string.location_power_requirement_high);
		POWER_REQUIREMENT_MEDIUM = context.getString(R.string.location_power_requirement_medium);
		POWER_REQUIREMENT_LOW = context.getString(R.string.location_power_requirement_low);
		POWER_REQUIREMENT_NONE = context.getString(R.string.location_power_requirement_none);		
		GPS_EVENT_FIRST_FIX = context.getString(R.string.gps_first_fix);
		GPS_EVENT_SATELLITE_STATUS = context.getString(R.string.gps_satellite_status);
		GPS_EVENT_STARTED = context.getString(R.string.gps_started);
		GPS_EVENT_STOPPED = context.getString(R.string.gps_stopped);
		
		
		mGpsStatusUpdateFrequency = GPSSTATUS_FREQUENCY_MEDIUM;
		mNmeaUpdateFrequency = NMEA_FREQUENCY_MEDIUM;

        mMinTime = LOCATION_FREQUENCY_MEDIUM;

		updateGpsStatus();

        setActiveActionCount(ACTIVE_ACTIONS);
        setActionThrottle(ACTION_ADDRESS_CHANGED, ADDRESS_FREQUENCY_MEDIUM);
        setActionThrottle(ACTION_GPS_STATUS_CHANGED, GPSSTATUS_FREQUENCY_MEDIUM);
        setActionThrottle(ACTION_NMEA_RECEIVED, NMEA_FREQUENCY_LOW);
        setActionThrottle(ACTION_LOCATION_CHANGED, LOCATION_FREQUENCY_MEDIUM);
        setActionThrottle(ACTION_STATUS_CHANGED, STATUS_FREQUENCY_MEDIUM);
	}
	
	public LocationManager getLocationManager() {
		return mLocationManager;
	}

	public String getBestProvider() {
		return getBestProvider(new Criteria(), false);
	}
	
	public String getBestProvider(Criteria criteria, boolean enabledOnly) {
		return mLocationManager.getBestProvider(criteria, enabledOnly);
	}
	
	/** Blocks thread if Geocoder is in use */
	public Geocoder getGeocoder() {
		synchronized (mGeocoder) {
			return mGeocoder;
		}
	}
	
	public GpsStatus getGpsStatus() {
		return mGpsStatus;
	}

	public String getLastGpsStatusEvent(int event) {
		switch(event) {
		case GpsStatus.GPS_EVENT_FIRST_FIX: return GPS_EVENT_FIRST_FIX;
		case GpsStatus.GPS_EVENT_SATELLITE_STATUS: return GPS_EVENT_SATELLITE_STATUS;
		case GpsStatus.GPS_EVENT_STARTED: return GPS_EVENT_STARTED;
		case GpsStatus.GPS_EVENT_STOPPED: return GPS_EVENT_STOPPED;
		}
		return null;
	}

	private void updateGpsStatus() {
		mGpsStatus = mLocationManager.getGpsStatus(null);
	}
	
	/** 
	 * Gets the minimum time between GPS status updates in milliseconds.
	 */
	public int getGpsStatusUpdateFrequency() {
		return mGpsStatusUpdateFrequency;
	}
	
	/** 
	 * Sets the minimum time between GPS status updates in milliseconds.
	 */
	public void setAddressUpdateFrequency(int frequency) {
		mGpsStatusUpdateFrequency = frequency;
	}

    private void setLastAddresses(Address address, android.location.Location location) {
        setActionTime(ACTION_ADDRESS_CHANGED);
        ((Callbacks) mCallbacks).onAddressChanged(address, location);
    }

    private boolean shouldGeocode(android.location.Location location) {
        if (mLastGeocodedLocation == null) return true;
        return  isActionAllowed(ACTION_ADDRESS_CHANGED) &&
                (mLastGeocodedLocation.getLatitude() != location.getLatitude()
                        || mLastGeocodedLocation.getLongitude() != location.getLongitude());
    }


    // TODO need all these calls to updateGpsStatus() ???
	
	@Override
	public void onGpsStatusChanged(int event) {
        if (!isActionAllowed(ACTION_GPS_STATUS_CHANGED)) return;

		updateGpsStatus();

        setActionTime(ACTION_GPS_STATUS_CHANGED);
		((Callbacks) mCallbacks).onGpsStatusChanged(event);
	}

	@Override
	public void onNmeaReceived(long timestamp, String nmea) {
        if (!isActionAllowed(ACTION_NMEA_RECEIVED)) return;

		updateGpsStatus();

        setActionTime(ACTION_NMEA_RECEIVED);
		((Callbacks) mCallbacks).onNmeaReceived(timestamp, nmea);
	}

    @Override
    public void onLocationChanged(android.location.Location location) {
        if(!isActionAllowed(ACTION_LOCATION_CHANGED)) return;

        if (shouldGeocode(location)) {
            new GeocoderTask().execute(location);
            mLastGeocodedLocation = location;
        }

        setActionTime(ACTION_LOCATION_CHANGED);
        ((Callbacks) mCallbacks).onLocationChanged(location);
    }

    @Override
    public void onProviderDisabled(String provider) {
        if (!isActionAllowed(ACTION_PROVIDER_DISABLED)) return;

        setActionTime(ACTION_PROVIDER_DISABLED);
        ((Callbacks) mCallbacks).onProviderDisabled(provider);
    }

    @Override
    public void onProviderEnabled(String provider) {
        if (!isActionAllowed(ACTION_PROVIDER_ENABLED)) return;

        setActionTime(ACTION_PROVIDER_ENABLED);
        ((Callbacks) mCallbacks).onProviderEnabled(provider);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        if (!isActionAllowed(ACTION_STATUS_CHANGED)) return;

        setActionTime(ACTION_STATUS_CHANGED);
        ((Callbacks) mCallbacks).onStatusChanged(provider, status, extras);
    }
	

		
    public android.location.Location getLastKnownLocation(String provider) {
        return mLocationManager.getLastKnownLocation(provider);
    }


    /**
     * Gets the minimum time between location updates in milliseconds.
     */
    public long getMinTime() {
        return mMinTime;
    }

    /**
     * Sets the minimum time between location updates in milliseconds.
     */
    public void setMinTime(long minTime) {
        mMinTime = minTime;
    }

    /**
     * Gets the minimum distance change that triggers a location update
     * in meters.
     */
    public float getMinDistance() {
        return mMinDistance;
    }

    /**
     * Sets the minimum distance change that triggers a location update
     * in meters.
     */
    public void setMinDistance(float minDistance) {
        mMinDistance = minDistance;
    }

    public boolean isEnabled(String provider) {
        return mLocationManager.isProviderEnabled(provider);
    }

    public String getLastStatus(int status) {
        switch (status) {
        case LocationProvider.AVAILABLE: return STATUS_AVAILABLE;
        case LocationProvider.OUT_OF_SERVICE: return STATUS_OUT_OF_SERVICE;
        case LocationProvider.TEMPORARILY_UNAVAILABLE: return STATUS_TEMPORARILY_UNAVAILABLE;
        }
        return null;
    }

    public String getAccuracy(int accuracy) {
        switch (accuracy) {
        case Criteria.ACCURACY_COARSE: return ACCURACY_COARSE;
        case Criteria.ACCURACY_FINE: return ACCURACY_FINE;
        }
        return null;
    }

    public String getPowerRequirement(int powerRequirements) {
        switch (powerRequirements) {
        case Criteria.POWER_HIGH: return POWER_REQUIREMENT_HIGH;
        case Criteria.POWER_LOW: return POWER_REQUIREMENT_MEDIUM;
        case Criteria.POWER_MEDIUM: return POWER_REQUIREMENT_LOW;
        case Criteria.NO_REQUIREMENT: return POWER_REQUIREMENT_NONE;
        }
        return null;
    }

	private class GeocoderTask extends AsyncTask<android.location.Location, Void, Address> {
        android.location.Location location;

		@Override
		protected Address doInBackground(android.location.Location... params) {
            location = params[0];
			List<Address> addresses = null;
			synchronized (mGeocoder) {
				try {
					addresses = mGeocoder.getFromLocation(params[0].getLatitude(), 
							params[0].getLongitude(), 1);
				} 
				catch (IOException ignored) {}
				catch (IllegalArgumentException ignored) {}				
			}
			
			if (addresses != null && !addresses.isEmpty()) {
				return addresses.get(0);
			}
			return null;
		}
		
		@Override
		protected void onPostExecute(Address result) {
			setLastAddresses(result, location);
		}
	}

    @Override
    public void start() {
        if (mIsActive) return;
        mLocationManager.addGpsStatusListener(this);
        mLocationManager.addNmeaListener(this);
        for (String s : mLocationManager.getAllProviders()) {
            mLocationManager.requestLocationUpdates(s, mMinTime, mMinDistance, this);
        }
        mIsActive = true;
    }

    @Override
    public void stop() {
        
        mLocationManager.removeGpsStatusListener(this);
        mLocationManager.removeNmeaListener(this);

        mLocationManager.removeUpdates(this);

        mIsActive = false;
    }
}
