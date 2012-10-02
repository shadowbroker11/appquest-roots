package org.siriusnet.metaldetector;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MetalDetectorFragment extends Fragment implements
		MetalDetectorView {

	private TextView geomagneticFieldValue;
	private GraphView graphView;
	private SensorManager sensorService;
	private Sensor magneticFieldSensor;
	private SensorEventListener sensorController;
	private ToneGenerator generator;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sensorService = (SensorManager) getActivity().getSystemService(
				Activity.SENSOR_SERVICE);
		magneticFieldSensor = sensorService
				.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		sensorController = new MagneticFieldSensorController(this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.fragment_metal_detector,
				container, false);

		graphView = (GraphView) view.findViewById(R.id.graph);
		geomagneticFieldValue = (TextView) view.findViewById(R.id.value);

		return view;
	}

	@Override
	public void onResume() {
		super.onResume();
		generator = new ToneGenerator(AudioManager.STREAM_MUSIC,
				ToneGenerator.MAX_VOLUME);
		if (magneticFieldSensor != null) {
			sensorService.registerListener(sensorController,
					magneticFieldSensor, SensorManager.SENSOR_DELAY_NORMAL);
		} else {
			String msg = "magnetic field sensor couldn't be found";
			Log.e(MetalDetectorConstants.LOG_TAG, msg);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		sensorService.unregisterListener(sensorController);
		generator.release();
	}

	@Override
	public void setGeomagneticFieldStrength(int x, int y, int z) {
		int val = (int) Math.sqrt(x * x + y * y + z * z);
		if (val > 100) {
			val = 100;
		}
		if (val > 50) {
			generateTone(val);
		}
		graphView.setYValue(val);
	}

	@Override
	public void setVectorValue(double strength) {
		if (isVisible()) {
			geomagneticFieldValue.setText(getString(
					R.string.geomagnetic_field_value, strength));
		}
	}

	private void generateTone(int value) {
		try {
			generator.stopTone();
			generator.startTone(ToneGenerator.TONE_CDMA_ABBR_INTERCEPT, 300);
		} catch (RuntimeException e) {
			Log.d(MetalDetectorConstants.LOG_TAG, "failed to play tone", e);
		}
	}
}
