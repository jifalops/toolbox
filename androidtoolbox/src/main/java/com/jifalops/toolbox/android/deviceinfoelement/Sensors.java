package com.jifalops.toolbox.android.deviceinfoelement;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.jifalops.toolbox.android.R;

import java.util.ArrayList;
import java.util.List;

// TODO add microphone
public class Sensors extends ActiveElement {
	private static final String LOG_TAG = Sensors.class.getSimpleName();
	
	public static final int
        TYPE_UNDEFINED = 0,
        TYPE_ACCELEROMETER = Sensor.TYPE_ACCELEROMETER,
        TYPE_GYROSCOPE = Sensor.TYPE_GYROSCOPE,
        TYPE_LIGHT = Sensor.TYPE_LIGHT,
        TYPE_MAGNETIC_FIELD = Sensor.TYPE_MAGNETIC_FIELD,
        TYPE_PRESSURE = Sensor.TYPE_PRESSURE,
        TYPE_PROXIMITY = Sensor.TYPE_PROXIMITY,
        TYPE_ORIENTATION = Sensor.TYPE_ORIENTATION,
        TYPE_TEMPERATURE = Sensor.TYPE_TEMPERATURE,
        // API 9
        TYPE_GRAVITY = 9,
        TYPE_LINEAR_ACCELERATION = 10,
        TYPE_ROTATION_VECTOR = 11,
        // API 14
        TYPE_RELATIVE_HUMIDITY = 12,
        TYPE_AMBIENT_TEMPERATURE = 13,

        FREQUENCY_HIGH     = 100,
        FREQUENCY_MEDIUM   = 200,
        FREQUENCY_LOW      = 500,

        ACTION_SENSOR_ACCURACY = 0,
        ACTION_SENSOR_EVENT = 1;

//        ACTION_ACCELEROMETER_ACCURACY    = 0,
//        ACTION_ACCELEROMETER_EVENT       = 1,
//        ACTION_AMBIENT_TEMPERATURE_ACCURACY    = 2,
//        ACTION_AMBIENT_TEMPERATURE_EVENT       = 3,
//        ACTION_GRAVITY_ACCURACY    = 4,
//        ACTION_GRAVITY_EVENT       = 5,
//        ACTION_GYROSCOPE_ACCURACY    = 6,
//        ACTION_GYROSCOPE_EVENT       = 7,
//        ACTION_LIGHT_ACCURACY    = 8,
//        ACTION_LIGHT_EVENT       = 9,
//        ACTION_LINEAR_ACCELERATION_ACCURACY    = 10,
//        ACTION_LINEAR_ACCELERATION_EVENT       = 11,
//        ACTION_MAGNETIC_FIELD_ACCURACY    = 12,
//        ACTION_MAGNETIC_FIELD_EVENT       = 13,
//        ACTION_ORIENTATION_ACCURACY    = 14,
//        ACTION_ORIENTATION_EVENT       = 15,
//        ACTION_PRESSURE_ACCURACY    = 16,
//        ACTION_PRESSURE_EVENT       = 17,
//        ACTION_PROXIMITY_ACCURACY    = 18,
//        ACTION_PROXIMITY_EVENT       = 19,
//        ACTION_RELATIVE_HUMIDITY_ACCURACY    = 20,
//        ACTION_RELATIVE_HUMIDITY_EVENT       = 21,
//        ACTION_ROTATION_VECTOR_ACCURACY    = 22,
//        ACTION_ROTATION_VECTOR_EVENT       = 23,
//        ACTION_TEMPERATURE_ACCURACY    = 24,
//        ACTION_TEMPERATURE_EVENT       = 25;

    private static final int
        ACTIVE_ACTIONS     = 2;


    public interface Callbacks extends ActiveElement.Callbacks {
		/** Corresponds to SensorEventListener.onAccuracyChanged() */
		void onAccuracyChanged(SensorWrapper sw);
		/** Corresponds to SensorEventListener.onSensorChanged() */
		void onSensorChanged(SensorWrapper sw);
	}

    public final String
        SENSOR_ACCELEROMETER,
        SENSOR_AMBIENT_TEMPERATURE,
        SENSOR_GRAVITY,
        SENSOR_GYROSCOPE,
        SENSOR_LIGHT,
        SENSOR_LINEAR_ACCELERATION,
        SENSOR_MAGNETIC_FIELD,
        SENSOR_PRESSURE,
        SENSOR_PROXIMITY,
        SENSOR_RELATIVE_HUMIDITY,
        SENSOR_ROTATION_VECTOR,
        SENSOR_ORIENTATION,
        SENSOR_TEMPERATURE,

        UNIT_NONE,
        UNIT_CELSIUS,
        UNIT_METERS_PER_SEC_SQUARED,
        UNIT_RADIANS_PER_SEC,
        UNIT_LUX,
        UNIT_MICRO_TESLA,
        UNIT_DEGREES,
        UNIT_HECTO_PASCAL,
        UNIT_CM,
        UNIT_PERCENT,
        UNIT_GRAMS_PER_CUBIC_METER,

        ACCURACY_HIGH,
        ACCURACY_MEDIUM,
        ACCURACY_LOW,
        ACCURACY_UNRELIABLE,

        CATEGORY_OTHER,
        CATEGORY_MOTION,
        CATEGORY_POSITION,
        CATEGORY_ENVIRONMENT;

    private float[]
        mAccelerometerValues,
        mMagneticFieldValues,
        mRelativeHumidityValues,
        mAmbientTemperatureValues;

    
	private SensorManager mSensorManager;
    
    private SensorWrapper[] mSensors;

    private List<SensorWrapper> 
        mAccelerometerSensors,
        mAmbientTemperatureSensors,
        mGravitySensors,
        mGyroscopeSensors,
        mLightSensors,
        mLinearAccelerationSensors,
        mMagneticFieldSensors,
        mOrientationSensors,
        mPressureSensors,
        mProximitySensors,
        mRelativeHumiditySensors,
        mRotationVectorSensors,
        mTemperatureSensors;
    
	public Sensors(Context context, Callbacks callbacks) {
		super(context, callbacks);
		mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);	
		
		SENSOR_ACCELEROMETER = context.getString(R.string.sensor_type_accelerometer);
        SENSOR_AMBIENT_TEMPERATURE = context.getString(R.string.sensor_type_ambient_temperature);
        SENSOR_GRAVITY = context.getString(R.string.sensor_type_gravity);
        SENSOR_GYROSCOPE = context.getString(R.string.sensor_type_gyroscope);
        SENSOR_LIGHT = context.getString(R.string.sensor_type_light);
        SENSOR_LINEAR_ACCELERATION = context.getString(R.string.sensor_type_linear_acceleration);
        SENSOR_MAGNETIC_FIELD = context.getString(R.string.sensor_type_magnetic_field);
        SENSOR_RELATIVE_HUMIDITY = context.getString(R.string.sensor_type_relative_humidity);
        SENSOR_ORIENTATION = context.getString(R.string.sensor_type_orientation);
        SENSOR_PRESSURE = context.getString(R.string.sensor_type_pressure);
        SENSOR_PROXIMITY = context.getString(R.string.sensor_type_proximity);
        SENSOR_ROTATION_VECTOR = context.getString(R.string.sensor_type_rotation_vector);
        SENSOR_TEMPERATURE = context.getString(R.string.sensor_type_temperature);

        UNIT_NONE = context.getString(R.string.unit_unitless);
        UNIT_CELSIUS = context.getString(R.string.unit_degrees_celsius);
		UNIT_METERS_PER_SEC_SQUARED = context.getString(R.string.unit_meters_per_second_squared);
        UNIT_RADIANS_PER_SEC = context.getString(R.string.unit_radians_per_second);
        UNIT_LUX = context.getString(R.string.unit_lux);
        UNIT_MICRO_TESLA = context.getString(R.string.unit_micro_tesla);
        UNIT_DEGREES = context.getString(R.string.unit_degrees);
        UNIT_HECTO_PASCAL = context.getString(R.string.unit_hecto_pascal);
        UNIT_CM = context.getString(R.string.unit_centimeter);
        UNIT_PERCENT = context.getString(R.string.unit_percent);
        UNIT_GRAMS_PER_CUBIC_METER = context.getString(R.string.unit_grams_per_cubic_meter);

		ACCURACY_HIGH = context.getString(R.string.sensor_accuracy_high);
		ACCURACY_MEDIUM = context.getString(R.string.sensor_accuracy_medium);
		ACCURACY_LOW = context.getString(R.string.sensor_accuracy_low);
		ACCURACY_UNRELIABLE = context.getString(R.string.sensor_accuracy_unreliable);

        CATEGORY_MOTION = context.getString(R.string.sensor_category_motion);
        CATEGORY_POSITION = context.getString(R.string.sensor_category_position);
        CATEGORY_ENVIRONMENT = context.getString(R.string.sensor_category_environment);
        CATEGORY_OTHER = context.getString(R.string.sensor_category_other);

        setActiveActionCount(ACTIVE_ACTIONS);
        setActionThrottle(ACTION_SENSOR_ACCURACY, FREQUENCY_MEDIUM);
        setActionThrottle(ACTION_SENSOR_EVENT, FREQUENCY_MEDIUM);
//        setActionThrottle(ACTION_ACCELEROMETER_ACCURACY, FREQUENCY_LOW);
//        setActionThrottle(ACTION_ACCELEROMETER_EVENT, FREQUENCY_LOW);
//        setActionThrottle(ACTION_AMBIENT_TEMPERATURE_ACCURACY, FREQUENCY_LOW);
//        setActionThrottle(ACTION_AMBIENT_TEMPERATURE_EVENT, FREQUENCY_LOW);
//        setActionThrottle(ACTION_GRAVITY_ACCURACY, FREQUENCY_LOW);
//        setActionThrottle(ACTION_GRAVITY_EVENT, FREQUENCY_LOW);
//        setActionThrottle(ACTION_GYROSCOPE_ACCURACY, FREQUENCY_LOW);
//        setActionThrottle(ACTION_GYROSCOPE_EVENT, FREQUENCY_LOW);
//        setActionThrottle(ACTION_LIGHT_ACCURACY, FREQUENCY_LOW);
//        setActionThrottle(ACTION_LIGHT_EVENT, FREQUENCY_LOW);
//        setActionThrottle(ACTION_LINEAR_ACCELERATION_ACCURACY, FREQUENCY_LOW);
//        setActionThrottle(ACTION_LINEAR_ACCELERATION_EVENT, FREQUENCY_LOW);
//        setActionThrottle(ACTION_MAGNETIC_FIELD_ACCURACY, FREQUENCY_LOW);
//        setActionThrottle(ACTION_MAGNETIC_FIELD_EVENT, FREQUENCY_LOW);
//        setActionThrottle(ACTION_ORIENTATION_ACCURACY, FREQUENCY_LOW);
//        setActionThrottle(ACTION_ORIENTATION_EVENT, FREQUENCY_LOW);
//        setActionThrottle(ACTION_PRESSURE_ACCURACY, FREQUENCY_LOW);
//        setActionThrottle(ACTION_PRESSURE_EVENT, FREQUENCY_LOW);
//        setActionThrottle(ACTION_PROXIMITY_ACCURACY, FREQUENCY_LOW);
//        setActionThrottle(ACTION_PROXIMITY_EVENT, FREQUENCY_LOW);
//        setActionThrottle(ACTION_RELATIVE_HUMIDITY_ACCURACY, FREQUENCY_LOW);
//        setActionThrottle(ACTION_RELATIVE_HUMIDITY_EVENT, FREQUENCY_LOW);
//        setActionThrottle(ACTION_ROTATION_VECTOR_ACCURACY, FREQUENCY_LOW);
//        setActionThrottle(ACTION_ROTATION_VECTOR_EVENT, FREQUENCY_LOW);
//        setActionThrottle(ACTION_TEMPERATURE_ACCURACY, FREQUENCY_LOW);
//        setActionThrottle(ACTION_TEMPERATURE_EVENT, FREQUENCY_LOW);

        mAccelerometerSensors = new ArrayList<SensorWrapper>();
        mAmbientTemperatureSensors = new ArrayList<SensorWrapper>();
        mGravitySensors = new ArrayList<SensorWrapper>();
        mGyroscopeSensors = new ArrayList<SensorWrapper>();
        mLightSensors = new ArrayList<SensorWrapper>();
        mLinearAccelerationSensors = new ArrayList<SensorWrapper>();
        mMagneticFieldSensors = new ArrayList<SensorWrapper>();
        mOrientationSensors = new ArrayList<SensorWrapper>();
        mPressureSensors = new ArrayList<SensorWrapper>();
        mProximitySensors = new ArrayList<SensorWrapper>();
        mRelativeHumiditySensors = new ArrayList<SensorWrapper>();
        mRotationVectorSensors = new ArrayList<SensorWrapper>();
        mTemperatureSensors = new ArrayList<SensorWrapper>();

        List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);

        // Add default sensors first.
        for (Sensor s : sensors) {
            if (s == mSensorManager.getDefaultSensor(s.getType()))
                addSensor(s);
        }
        // Then other sensors.
        for (Sensor s : sensors) {
            if (s != mSensorManager.getDefaultSensor(s.getType()))
                addSensor(s);
        }

        List<SensorWrapper> list = new ArrayList<SensorWrapper>();
        list.addAll(mAccelerometerSensors);
        list.addAll(mAmbientTemperatureSensors);
        list.addAll(mGravitySensors);
        list.addAll(mGyroscopeSensors);
        list.addAll(mLightSensors);
        list.addAll(mLinearAccelerationSensors);
        list.addAll(mMagneticFieldSensors);
        list.addAll(mOrientationSensors);
        list.addAll(mPressureSensors);
        list.addAll(mProximitySensors);
        list.addAll(mRelativeHumiditySensors);
        list.addAll(mRotationVectorSensors);
        list.addAll(mTemperatureSensors);
        mSensors = list.toArray(new SensorWrapper[list.size()]);
	}

    private void addSensor(Sensor s) {
        SensorWrapper sw = new SensorWrapper(mSensorManager, s, (Callbacks) mCallbacks);
        switch (s.getType()) {
        case TYPE_ACCELEROMETER:            mAccelerometerSensors.add(sw);           break;
        case TYPE_AMBIENT_TEMPERATURE:      mAmbientTemperatureSensors.add(sw);      break;
        case TYPE_GRAVITY:                  mGravitySensors.add(sw);                 break;
        case TYPE_GYROSCOPE:                mGyroscopeSensors.add(sw);               break;
        case TYPE_LIGHT:                    mLightSensors.add(sw);                   break;
        case TYPE_LINEAR_ACCELERATION:      mLinearAccelerationSensors.add(sw);      break;
        case TYPE_MAGNETIC_FIELD:           mMagneticFieldSensors.add(sw);           break;
        case TYPE_ORIENTATION:              mOrientationSensors.add(sw);             break;
        case TYPE_PRESSURE:                 mPressureSensors.add(sw);                break;
        case TYPE_PROXIMITY:                mProximitySensors.add(sw);               break;
        case TYPE_RELATIVE_HUMIDITY:        mRelativeHumiditySensors.add(sw);        break;
        case TYPE_ROTATION_VECTOR:          mRotationVectorSensors.add(sw);          break;
        case TYPE_TEMPERATURE:              mTemperatureSensors.add(sw);             break;
        }
    }

    public List<SensorWrapper> getSensors(int type) {
        switch (type) {
        case TYPE_ACCELEROMETER:            return mAccelerometerSensors;
        case TYPE_AMBIENT_TEMPERATURE:      return mAmbientTemperatureSensors;
        case TYPE_GRAVITY:                  return mGravitySensors;
        case TYPE_GYROSCOPE:                return mGyroscopeSensors;
        case TYPE_LIGHT:                    return mLightSensors;
        case TYPE_LINEAR_ACCELERATION:      return mLinearAccelerationSensors;
        case TYPE_MAGNETIC_FIELD:           return mMagneticFieldSensors;
        case TYPE_ORIENTATION:              return mOrientationSensors;
        case TYPE_PRESSURE:                 return mPressureSensors;
        case TYPE_PROXIMITY:                return mProximitySensors;
        case TYPE_RELATIVE_HUMIDITY:        return mRelativeHumiditySensors;
        case TYPE_ROTATION_VECTOR:          return mRotationVectorSensors;
        case TYPE_TEMPERATURE:              return mTemperatureSensors;
        default:                            return null;
        }
    }

    public String getSensorName(int type) {
        switch (type) {
            case TYPE_ACCELEROMETER:            return SENSOR_ACCELEROMETER;
            case TYPE_AMBIENT_TEMPERATURE:      return SENSOR_AMBIENT_TEMPERATURE;
            case TYPE_GRAVITY:                  return SENSOR_GRAVITY;
            case TYPE_GYROSCOPE:                return SENSOR_GYROSCOPE;
            case TYPE_LIGHT:                    return SENSOR_LIGHT;
            case TYPE_LINEAR_ACCELERATION:      return SENSOR_LINEAR_ACCELERATION;
            case TYPE_MAGNETIC_FIELD:           return SENSOR_MAGNETIC_FIELD;
            case TYPE_ORIENTATION:              return SENSOR_ORIENTATION;
            case TYPE_PRESSURE:                 return SENSOR_PRESSURE;
            case TYPE_PROXIMITY:                return SENSOR_PROXIMITY;
            case TYPE_RELATIVE_HUMIDITY:        return SENSOR_RELATIVE_HUMIDITY;
            case TYPE_ROTATION_VECTOR:          return SENSOR_ROTATION_VECTOR;
            case TYPE_TEMPERATURE:              return SENSOR_TEMPERATURE;
            default:                            return null;
        }
    }

    public SensorManager getSensorManager() {
        return mSensorManager;
    }

    public SensorWrapper[] getSensors() {
        return mSensors;
    }

    public String getCategory(int type) {
        switch (type) {
        case TYPE_UNDEFINED: return CATEGORY_OTHER;

        case TYPE_GRAVITY:
        case TYPE_GYROSCOPE:
        case TYPE_LINEAR_ACCELERATION:
        case TYPE_ROTATION_VECTOR:
        case TYPE_ACCELEROMETER: return CATEGORY_MOTION;

        case TYPE_LIGHT:
        case TYPE_RELATIVE_HUMIDITY:
        case TYPE_PRESSURE:
        case TYPE_TEMPERATURE:
        case TYPE_AMBIENT_TEMPERATURE: return CATEGORY_ENVIRONMENT;


        case TYPE_ORIENTATION:
        case TYPE_PROXIMITY:
        case TYPE_MAGNETIC_FIELD: return CATEGORY_POSITION;
        }
        return CATEGORY_OTHER;
    }
    
    public String getUnit(int type) {
        switch (type) {
        case TYPE_ROTATION_VECTOR:
        case TYPE_UNDEFINED: return UNIT_NONE;

        case TYPE_GRAVITY:
        case TYPE_LINEAR_ACCELERATION:
        case TYPE_ACCELEROMETER: return UNIT_METERS_PER_SEC_SQUARED;

        case TYPE_TEMPERATURE:
        case TYPE_AMBIENT_TEMPERATURE: return UNIT_CELSIUS;

        case TYPE_GYROSCOPE: return UNIT_RADIANS_PER_SEC;
        case TYPE_LIGHT: return UNIT_LUX;
        case TYPE_MAGNETIC_FIELD: return UNIT_MICRO_TESLA;
        case TYPE_PRESSURE: return UNIT_HECTO_PASCAL;
        case TYPE_PROXIMITY: return UNIT_CM;
        case TYPE_RELATIVE_HUMIDITY: return UNIT_PERCENT;
        case TYPE_ORIENTATION: return UNIT_DEGREES;
        }
        return UNIT_NONE;
    }
    
    public String getAccuracy(int accuracy) {
        switch(accuracy) {
            case SensorManager.SENSOR_STATUS_ACCURACY_HIGH: return ACCURACY_HIGH;
            case SensorManager.SENSOR_STATUS_ACCURACY_LOW: return ACCURACY_LOW;
            case SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM: return ACCURACY_MEDIUM;
            case SensorManager.SENSOR_STATUS_UNRELIABLE: return ACCURACY_UNRELIABLE;
        }
        return null;
    }
//
//    private int getAccuracyAction(int sensorType) {
//        switch (sensorType) {
//            case TYPE_GRAVITY:              return ACTION_GRAVITY_ACCURACY;
//            case TYPE_GYROSCOPE:            return ACTION_GYROSCOPE_ACCURACY;
//            case TYPE_LINEAR_ACCELERATION:  return ACTION_LINEAR_ACCELERATION_ACCURACY;
//            case TYPE_ROTATION_VECTOR:      return ACTION_ROTATION_VECTOR_ACCURACY;
//            case TYPE_ACCELEROMETER:        return ACTION_ACCELEROMETER_ACCURACY;
//            case TYPE_LIGHT:                return ACTION_LIGHT_ACCURACY;
//            case TYPE_RELATIVE_HUMIDITY:    return ACTION_RELATIVE_HUMIDITY_ACCURACY;
//            case TYPE_PRESSURE:             return ACTION_PRESSURE_ACCURACY;
//            case TYPE_TEMPERATURE:          return ACTION_TEMPERATURE_ACCURACY;
//            case TYPE_AMBIENT_TEMPERATURE:  return ACTION_AMBIENT_TEMPERATURE_ACCURACY;
//            case TYPE_ORIENTATION:          return ACTION_ORIENTATION_ACCURACY;
//            case TYPE_PROXIMITY:            return ACTION_PROXIMITY_ACCURACY;
//            case TYPE_MAGNETIC_FIELD:       return ACTION_MAGNETIC_FIELD_ACCURACY;
//            default:                        return -1;
//        }
//    }
//
//    private int getEventAction(int sensorType) {
//        switch (sensorType) {
//            case TYPE_GRAVITY:              return ACTION_GRAVITY_EVENT;
//            case TYPE_GYROSCOPE:            return ACTION_GYROSCOPE_EVENT;
//            case TYPE_LINEAR_ACCELERATION:  return ACTION_LINEAR_ACCELERATION_EVENT;
//            case TYPE_ROTATION_VECTOR:      return ACTION_ROTATION_VECTOR_EVENT;
//            case TYPE_ACCELEROMETER:        return ACTION_ACCELEROMETER_EVENT;
//            case TYPE_LIGHT:                return ACTION_LIGHT_EVENT;
//            case TYPE_RELATIVE_HUMIDITY:    return ACTION_RELATIVE_HUMIDITY_EVENT;
//            case TYPE_PRESSURE:             return ACTION_PRESSURE_EVENT;
//            case TYPE_TEMPERATURE:          return ACTION_TEMPERATURE_EVENT;
//            case TYPE_AMBIENT_TEMPERATURE:  return ACTION_AMBIENT_TEMPERATURE_EVENT;
//            case TYPE_ORIENTATION:          return ACTION_ORIENTATION_EVENT;
//            case TYPE_PROXIMITY:            return ACTION_PROXIMITY_EVENT;
//            case TYPE_MAGNETIC_FIELD:       return ACTION_MAGNETIC_FIELD_EVENT;
//            default:                        return -1;
//        }
//    }



    public static class SensorWrapper implements SensorEventListener {
        private final Sensor mSensor;
        private boolean mIsActive;
        protected float[] mValues;
        private int mAccuracy;
        private SensorManager mSensorManager;
        private int mThrottle;
        private Callbacks mCallbacks;
        private int mType;

        public SensorWrapper(SensorManager sensorManager, Sensor sensor, Callbacks callbacks) {
            mSensorManager = sensorManager;
            mSensor = sensor;
            mCallbacks = callbacks;
            mType = sensor.getType();
        }

        public Sensor getSensor() {
            return mSensor;
        }

        public int getAccuracy() {
            return mAccuracy;
        }

        public float[] getValues() {
            return mValues;
        }

        public int getType() {
            return mType;
        }

        public void setThrottle(int millis) {
            mThrottle = millis;
        }

        public void start() {
            if (mIsActive) return;
            mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_UI);
            mIsActive = true;
        }

        public void stop() {
            if (!mIsActive) return;
            mSensorManager.unregisterListener(this);
            mIsActive = false;
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            mAccuracy = accuracy;
            mCallbacks.onAccuracyChanged(this);
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            mAccuracy = event.accuracy;
            mValues = event.values.clone();
            mCallbacks.onSensorChanged(this);
        }
    }



        //
    // Aggregate sensor info
    //

    public float[] getOrientationInWorldCoordinateSystem() {
		if (mAccelerometerValues == null || mMagneticFieldValues == null) return null;
		
		float[] rotationMatrix = new float[9], 
				orientation = new float[3];

		if (SensorManager.getRotationMatrix(rotationMatrix, null, mAccelerometerValues, mMagneticFieldValues)) {
			SensorManager.getOrientation(rotationMatrix, orientation);
		}
		return orientation;
	}
	
	/** Calculates the dew point in degrees Celsius */
	public float getDewPoint() {
		if (mRelativeHumidityValues == null || mAmbientTemperatureValues == null) return 0f;
		float rh = mRelativeHumidityValues[0];
		float t = mAmbientTemperatureValues[0];
		double h = Math.log(rh / 100.0) + (17.62 * t) / (243.12 + t);
		return (float) (243.12 * h / (17.62 - h));
	}
	
	/** Calculates the absolute humidity in g/m^3 */
	public float getAbsoluteHumidity() {
        if (mRelativeHumidityValues == null || mAmbientTemperatureValues == null) return 0f;
        float rh = mRelativeHumidityValues[0];
        float t = mAmbientTemperatureValues[0];
		return (float) (216.7 * (rh / 100.0 * 6.112 * Math.exp(17.62 * t / (243.12 + t)) / (273.15 + t)));
	}

//    @Override
//    public void onAccuracyChanged(Sensor sensor, int accuracy) {
////        int action = getAccuracyAction(sensor.getType());
//        if (!isActionAllowed(ACTION_SENSOR_ACCURACY)) return;
//
//        // TODO other classes implement these in the opposite order.
//        ((Callbacks) mCallbacks).onAccuracyChanged(sensor, accuracy);
//        setActionTime(ACTION_SENSOR_ACCURACY);
//    }

//    @Override
//    public void onSensorChanged(SensorEvent event) {
////        int action = getEventAction(event.sensor.getType());
//        if (!isActionAllowed(ACTION_SENSOR_EVENT)) return;
//
//        switch (event.sensor.getType()) {
//            case TYPE_ACCELEROMETER:
//                mAccelerometerValues = event.values.clone();
//                break;
//            case TYPE_MAGNETIC_FIELD:
//                mMagneticFieldValues = event.values.clone();
//                break;
//            case TYPE_AMBIENT_TEMPERATURE:
//                mAmbientTemperatureValues = event.values.clone();
//                break;
//            case TYPE_RELATIVE_HUMIDITY:
//                mRelativeHumidityValues = event.values.clone();
//                break;
//        }
//
//        // TODO other classes implement these in the opposite order.
//        ((Callbacks) mCallbacks).onSensorChanged(event);
//        setActionTime(ACTION_SENSOR_EVENT);
//    }
	
	@Override
	public void start() {
		if (mIsActive) return;
//        for (Sensor s : mSensorManager.getSensorList(Sensor.TYPE_ALL)) {
//            mSensorManager.registerListener(this, s, SensorManager.SENSOR_DELAY_UI);
//        }

        for (SensorWrapper sw : mSensors) {
            sw.start();
        }

        mIsActive = true;
	}
	
	@Override
	public void stop() {

//        mSensorManager.unregisterListener(this);

        for (SensorWrapper sw : mSensors) {
            sw.stop();
        }

		mIsActive = false;
    }

}
