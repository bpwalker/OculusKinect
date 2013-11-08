package com.oculuskinect;

import kinect.Kinect;

import com.jme3.app.SimpleApplication;
import com.jme3.app.state.StereoCamAppState;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.material.Material;
import com.jme3.math.Quaternion;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.BillboardControl;
import com.jme3.scene.control.BillboardControl.Alignment;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Quad;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.jme3.texture.plugins.AWTLoader;

public class OculusKinectApplication extends SimpleApplication implements ActionListener{
    private static StereoCamAppState stereoCamAppState;
    private Geometry geom;
    private Node scene = new Node("scene");
    private Kinect ok;
    private Texture world;
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
    }
    
    private void initWorld(){
        Quad worldQuad = new Quad(7, 7);
        Geometry worldGeom = new Geometry("World", worldQuad);
        worldGeom.setLocalTranslation(-3.5f, -3.5f, 0);
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setTexture("ColorMap", world);
        worldGeom.setMaterial(mat);
        worldGeom.setQueueBucket(RenderQueue.Bucket.Sky);
        worldGeom.setCullHint(Spatial.CullHint.Never);
        
        BillboardControl control = new BillboardControl();
        control.setAlignment(Alignment.Screen);
        scene.addControl(control);
        scene.attachChild(worldGeom);
        
        rootNode.attachChild(scene);
        scene.addControl(stereoCamAppState.getCameraControl());
    }
	
    private void initOculus(){
        stereoCamAppState = new StereoCamAppState();
        stateManager.attach(stereoCamAppState);  
    }
	
	private void initKinect(){
		ok = new Kinect(0);
		imageLoader = new AWTLoader();
        
        world = new Texture2D();
        world.setImage(imageLoader.load(ok.getColorImage(true), true));
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
}
