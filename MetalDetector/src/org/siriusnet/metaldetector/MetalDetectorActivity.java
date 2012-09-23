package org.siriusnet.metaldetector;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

public class MetalDetectorActivity extends Activity implements
		MetalDetectorView {

	private GraphView graphView;
	private SensorManager sensorService;
	private Sensor magneticFieldSensor;
	private SensorEventListener sensorController;
	private ToneGenerator generator;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_metal_detector);
		graphView = (GraphView) findViewById(R.id.graph);

		sensorService = (SensorManager) getSystemService(SENSOR_SERVICE);
		magneticFieldSensor = sensorService
				.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		sensorController = new MagneticFieldSensorController(this);
	}

	@Override
	protected void onResume() {
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
	protected void onPause() {
		super.onPause();
		sensorService.unregisterListener(sensorController);
		generator.release();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_metal_detector, menu);
		return true;
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
		// onScreenLog.setText("val: " + strength);
	}

	private void generateTone(int value) {
		generator.stopTone();
		generator.startTone(ToneGenerator.TONE_CDMA_ABBR_INTERCEPT, 300);
	}
}
