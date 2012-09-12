package org.siriusnet.metaldetector;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.FloatMath;

public class MagneticFieldSensorController implements SensorEventListener {
	private static float MAX_VALUE = 70.0f;

	private MetalDetectorView view;

	public MagneticFieldSensorController(MetalDetectorView view) {
		this.view = view;
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		view.setGeomagneticFieldStrength(normalizeValue(event.values[0]),
				normalizeValue(event.values[1]),
				normalizeValue(event.values[2]));
		view.setVectorValue(getVectorValue(event.values));
	}

	private int normalizeValue(float value) {
		if (value < 0) {
			return 0;
		}
		if (value > MAX_VALUE) {
			return 100;
		}
		return Math.round(value / MAX_VALUE * 100);

	}

	private double getVectorValue(float[] rawValues) {
		float value = 0;
		for (float rawValue : rawValues) {
			value += rawValue * rawValue;
		}
		return FloatMath.sqrt(value);
	}

}
