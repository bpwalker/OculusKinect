package com.oculuskinect;

import kinect.Kinect;

import com.jme3.app.SimpleApplication;
import com.jme3.app.state.StereoCamAppState;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.renderer.ViewPort;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.CameraNode;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.BillboardControl;
import com.jme3.scene.control.BillboardControl.Alignment;
import com.jme3.scene.control.CameraControl.ControlDirection;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.jme3.texture.plugins.AWTLoader;

public class OculusKinectApplication extends SimpleApplication implements ActionListener{
    private static StereoCamAppState stereoCamAppState;
    private Geometry geom;
    private Node worldLeft;
    private Node worldRight;
    private Kinect leftKinect;
    private Kinect rightKinect;
    private Texture leftEye;
    private Texture rightEye;
    private AWTLoader imageLoader;
    
    private enum Actions {
        LEFT, RIGHT, UP, DOWN, BACKWARD, FORWARD;
    }
    
    public static void main(String[] args) {
        OculusKinectApplication app = new OculusKinectApplication();
        app.start();
    }
	
	@Override
    public void simpleInitApp() {   
		flyCam.setEnabled(false);

        initOculus();
		initKinect();
        initWorld();
        initTerrain();
        initInputs();
    }
	
    @Override
    public void update(){
       super.update();
       Vector3f t = worldLeft.getLocalTranslation();
       cam.setLocation(new Vector3f(t.x, t.y, t.z + 10));
       leftEye.setImage(imageLoader.load(leftKinect.getColorImage(true), true));
       rightEye.setImage(imageLoader.load(rightKinect.getColorImage(true), true));
    }
    
    private void initWorld(){
        Quad worldLeftQuad = new Quad(7, 7);
        Geometry worldLeftGeom = new Geometry("World", worldLeftQuad);
        worldLeftGeom.setLocalTranslation(-3.5f, -3.5f, 0);
        Material leftMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        leftMat.setTexture("ColorMap", leftEye);
        worldLeftGeom.setMaterial(leftMat);
        worldLeftGeom.setQueueBucket(RenderQueue.Bucket.Sky);
        worldLeftGeom.setCullHint(Spatial.CullHint.Never);
        
        worldLeft = new Node("WorldLeft");
        BillboardControl controlLeft = new BillboardControl();
        controlLeft.setAlignment(Alignment.Screen);
        worldLeft.addControl(controlLeft);
        worldLeft.attachChild(worldLeftGeom);
        
        Quad worldRightQuad = new Quad(7, 7);
        Geometry worldRightGeom = new Geometry("World", worldRightQuad);
        worldRightGeom.setLocalTranslation(-3.5f, -3.5f, 0);
        Material rightMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        rightMat.setTexture("ColorMap", rightEye);
        worldRightGeom.setMaterial(rightMat);
        worldRightGeom.setQueueBucket(RenderQueue.Bucket.Sky);
        worldRightGeom.setCullHint(Spatial.CullHint.Never);
        
        worldRight = new Node("WorldRight");
        BillboardControl controlRight = new BillboardControl();
        controlRight.setAlignment(Alignment.Screen);
        worldRight.addControl(controlRight);
        worldRight.attachChild(worldRightGeom);
    }
	
    private void initOculus(){
        stereoCamAppState = new OculusKinectStereoCamAppState(this);
        stateManager.attach(stereoCamAppState);  
    }
	
	private void initKinect(){
		leftKinect = new Kinect(0);
		rightKinect = new Kinect(1);
		imageLoader = new AWTLoader();
        
        leftEye = new Texture2D();
        rightEye = new Texture2D();
        leftEye.setImage(imageLoader.load(leftKinect.getColorImage(true), true));
        rightEye.setImage(imageLoader.load(rightKinect.getColorImage(true), true));
	}
	
	private void initTerrain(){
        @SuppressWarnings("deprecation")
		Box b = new Box(Vector3f.ZERO, 1, 1, 1);
        geom = new Geometry("Box", b);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setTexture("ColorMap", assetManager.loadTexture("Interface/Logo/Monkey.jpg"));
        geom.setMaterial(mat);
        rootNode.attachChild(geom);
        rootNode.attachChild(new Chunk(10, 10, assetManager));
	}

     private void initInputs() {
         inputManager.addMapping(Actions.LEFT.name(), new KeyTrigger(KeyInput.KEY_A));
         inputManager.addMapping(Actions.RIGHT.name(), new KeyTrigger(KeyInput.KEY_D));
         inputManager.addMapping(Actions.UP.name(), new KeyTrigger(KeyInput.KEY_W));
         inputManager.addMapping(Actions.DOWN.name(), new KeyTrigger(KeyInput.KEY_S));
         inputManager.addMapping(Actions.BACKWARD.name(), new KeyTrigger(KeyInput.KEY_LCONTROL));
         inputManager.addMapping(Actions.FORWARD.name(), new KeyTrigger(KeyInput.KEY_SPACE));
         inputManager.addListener(this, Actions.LEFT.name());
         inputManager.addListener(this, Actions.RIGHT.name());
         inputManager.addListener(this, Actions.UP.name());
         inputManager.addListener(this, Actions.DOWN.name());
         inputManager.addListener(this, Actions.BACKWARD.name());
         inputManager.addListener(this, Actions.FORWARD.name());
    }
     
     @Override
     public void onAction(String name, boolean keyPressed, float tpf) {
         if (name.equals(Actions.LEFT.name())) {
        	 geom.setLocalTranslation(geom.getLocalTranslation().add(new Vector3f(-1, 0, 0)));
         } else if (name.equals(Actions.RIGHT.name())) {
        	 geom.setLocalTranslation(geom.getLocalTranslation().add(new Vector3f(1, 0, 0)));
         } else if (name.equals(Actions.UP.name())) {
        	 geom.setLocalTranslation(geom.getLocalTranslation().add(new Vector3f(0, 0, -1)));
         } else if (name.equals(Actions.DOWN.name())) {
        	 geom.setLocalTranslation(geom.getLocalTranslation().add(new Vector3f(0, 0, 1)));
         } else if (name.equals(Actions.BACKWARD.name())) {
        	 geom.setLocalTranslation(geom.getLocalTranslation().add(new Vector3f(0, -1, 0)));
         } else if (name.equals(Actions.FORWARD.name())) {
        	 geom.setLocalTranslation(geom.getLocalTranslation().add(new Vector3f(0, 1, 0)));
         }
     } 
     
     public Node getLeftScene(){
    	 return worldLeft;
     }
     
     public Node getRightScene(){
    	 return worldRight;
     }
}
