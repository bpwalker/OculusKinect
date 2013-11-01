package com.oculuskinect;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.app.state.AppStateManager;
import com.jme3.app.state.StereoCamAppState;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

public class OculusKinectStereoCamAppState extends StereoCamAppState{
	public OculusKinectApplication okApp;
	
	public OculusKinectStereoCamAppState(OculusKinectApplication okApp){
		this.okApp = okApp;
	}
	
    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        okApp.getLeftScene().updateGeometricState();
        okApp.getRightScene().updateGeometricState();
        viewPortLeft.attachScene(okApp.getLeftScene());
        viewPortRight.attachScene(okApp.getRightScene());
    }
    
    @Override
    public void update(float tpf) {
        super.update(tpf);
    }
}
