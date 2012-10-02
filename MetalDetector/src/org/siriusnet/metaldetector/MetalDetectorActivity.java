package org.siriusnet.metaldetector;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

public class MetalDetectorActivity extends FragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_metal_detector);
		if (savedInstanceState == null) {
			FragmentTransaction fragmentTransaction = getSupportFragmentManager()
					.beginTransaction();
			fragmentTransaction.add(R.id.fragment_container,
					new MetalDetectorFragment());
			fragmentTransaction.commit();
		}
	}
}
