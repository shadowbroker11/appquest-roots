package org.siriusnet.metaldetector;

public interface MetalDetectorView {
	void setGeomagneticFieldStrength(int x, int y, int z);
	
	void setVectorValue(double strength);
}
