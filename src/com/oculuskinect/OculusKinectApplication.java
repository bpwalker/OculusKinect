package com.oculuskinect;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.LinkedList;

import kinect.Kinect;

import com.jme3.app.SimpleApplication;
import com.jme3.app.state.StereoCamAppState;
import com.jme3.font.BitmapFont;
import com.jme3.font.BitmapText;
import com.jme3.font.Rectangle;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.material.RenderState.FaceCullMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.control.BillboardControl;
import com.jme3.scene.control.BillboardControl.Alignment;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Quad;
import com.jme3.scene.shape.Sphere;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.jme3.texture.plugins.AWTLoader;
import com.jme3.util.TangentBinormalGenerator;

public class OculusKinectApplication extends SimpleApplication implements
		ActionListener {
	private static StereoCamAppState stereoCamAppState;
	private Geometry geom;
	private Node scene = new Node("scene");
	private Kinect ok;
	private Texture world;
	private AWTLoader imageLoader;
	private BitmapText hudText;
	private int colorBlindType;
	private boolean colorBlindMode = false;
	private boolean shootLaserMode = false;
	private boolean nightVisionMode = false;
	private Geometry laserStart;
	private Geometry laserEnd;
	private LinkedList<Ball> balls = new LinkedList<Ball>();
	private ArrayList<Ball> ballsToRemove = new ArrayList<Ball>();
	private float maxDistanceMM = 4000;
	
	private enum Actions {
		ONE, TWO, THREE, R, SPACE, L, N
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
		initGUI();
	}

	@Override
	public void simpleUpdate(float tpf) {
		BufferedImage img;
		if(nightVisionMode){
			img = ok.getInfraredImage(false);
		} else {
			img = ok.getColorImage(false);
		}
		Graphics2D g = img.createGraphics();
		g.setColor(Color.RED);
		g.drawOval(317, 237, 6, 6);
		g.drawString(ok.getDepthString(320, 240, true, false), 312, 255);
		if(colorBlindMode){
			img = colorBlindConversion(img, colorBlindType);
		}
		
		if(shootLaserMode){
			shootLaser();
		}
		
		for(Ball ball : balls){
			if(ball.travel(tpf)){
				ballsToRemove.add(ball);
			}
		}
		
		if(!ballsToRemove.isEmpty()){
			for(Ball ball : ballsToRemove){
				rootNode.detachChild(ball.getBall());
				balls.remove(ball);
			}
			ballsToRemove.clear();
		}
		
		world.setImage(imageLoader.load(img, true));

		// hudText.setText("text");
	}

	private void initWorld() {
		Quad worldQuad = new Quad(7, 7);
		Geometry worldGeom = new Geometry("World", worldQuad);
		worldGeom.setLocalTranslation(-3.5f, -3.5f, 0);
		Material mat = new Material(assetManager,
				"Common/MatDefs/Misc/Unshaded.j3md");
		mat.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
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
		
	    DirectionalLight sun = new DirectionalLight();
	    sun.setDirection(new Vector3f(0,0,1).normalizeLocal());
	    sun.setColor(ColorRGBA.White);
	    rootNode.addLight(sun);
	}

	private void initOculus() {
		stereoCamAppState = new StereoCamAppState();
		stateManager.attach(stereoCamAppState);
	}

	private void initKinect() {
		ok = new Kinect(0);
		ok.initialize(Kinect.INITIALIZE_FLAGS.USES_COLOR
				| Kinect.INITIALIZE_FLAGS.USES_DEPTH);
		ok.setAngle(0);
		imageLoader = new AWTLoader();
		world = new Texture2D();
		world.setImage(imageLoader.load(ok.getColorImage(false), true));
	}

	private void initTerrain() {
		@SuppressWarnings("deprecation")
		Box b = new Box(Vector3f.ZERO, 1, 1, 1);
		geom = new Geometry("Box", b);
		Material mat = new Material(assetManager,
				"Common/MatDefs/Misc/Unshaded.j3md");
		mat.setTexture("ColorMap",
				assetManager.loadTexture("Interface/Logo/Monkey.jpg"));
		geom.setMaterial(mat);
		rootNode.attachChild(geom);
		rootNode.attachChild(new Chunk(10, 10, assetManager));
	}

	private void initInputs() {
		inputManager.addMapping(Actions.ONE.name(), new KeyTrigger(
				KeyInput.KEY_1));
		inputManager.addMapping(Actions.TWO.name(), new KeyTrigger(
				KeyInput.KEY_2));
		inputManager.addMapping(Actions.THREE.name(), new KeyTrigger(
				KeyInput.KEY_3));
		inputManager.addMapping(Actions.R.name(),
				new KeyTrigger(KeyInput.KEY_R));
		inputManager.addMapping(Actions.SPACE.name(), new KeyTrigger(
				KeyInput.KEY_SPACE));
		inputManager.addMapping(Actions.L.name(),
				new KeyTrigger(KeyInput.KEY_L));
		inputManager.addMapping(Actions.N.name(),
				new KeyTrigger(KeyInput.KEY_N));
		inputManager.addListener(this, Actions.ONE.name());
		inputManager.addListener(this, Actions.TWO.name());
		inputManager.addListener(this, Actions.THREE.name());
		inputManager.addListener(this, Actions.R.name());
		inputManager.addListener(this, Actions.SPACE.name());
		inputManager.addListener(this, Actions.L.name());
		inputManager.addListener(this, Actions.N.name());
	}

	private void initGUI() {
		BitmapFont fnt = assetManager.loadFont("Interface/Fonts/Default.fnt");
		hudText = new BitmapText(fnt, false);
		hudText.setBox(new Rectangle(settings.getWidth() * 0.25f, settings
				.getHeight() * 0.25f, settings.getWidth() * 0.75f, settings
				.getHeight() * 0.75f));
		hudText.setSize(fnt.getPreferredSize() * 2f);
		hudText.setColor(ColorRGBA.Red);
		hudText.setLocalTranslation(0, hudText.getHeight() - 400, 0);
		guiNode.attachChild(hudText);
	}

	private BufferedImage colorBlindConversion(BufferedImage image, int type) {
		for (int i = 0; i < image.getWidth(); i++) {
			for (int j = 0; j < image.getHeight(); j++) {
				image.setRGB(
						i,
						j,
						ColorBlind.getDichromatColor(
								new Color(image.getRGB(i, j)), type).getRGB());
			}
		}
		return image;
	}
	
	private void shootBall(){
		Camera cam = stereoCamAppState.getCameraControl().getCamera();
		double reflectionVector[] = ok.getReflectionVector(320, 240, false, 45);
		Ball ball = new Ball(cam.getLocation().add(cam.getDirection().mult(.4f)), cam.getDirection(), cam.getLocation().add(cam.getDirection().mult((ok.getDepthMM(320, 240, false, false) / maxDistanceMM) * 12)).z, cam.getRotation().mult(new Vector3f((float)reflectionVector[0] * -1, (float)reflectionVector[1], (float)reflectionVector[2])).normalize(), 5, 1f);
		balls.add(ball);
		rootNode.attachChild(ball.getBall());
		rootNode.updateGeometricState();
	}
	
	private void shootLaser(){
		if(laserStart != null){
			removeLaser();
		}
		Camera cam = stereoCamAppState.getCameraControl().getCamera();
		Vector3f startStart = cam.getLocation().add(new Vector3f(0,-1,0));
		Vector3f startEnd = cam.getLocation().add(cam.getDirection().mult((ok.getDepthMM(320, 240, false, false) / maxDistanceMM) * 12));
		Mesh startLineMesh = new Mesh();
		startLineMesh.setMode(Mesh.Mode.Lines);
		startLineMesh.setBuffer(VertexBuffer.Type.Position, 3, new float[]{ startStart.x, startStart.y, startStart.z, startEnd.x, startEnd.y, startEnd.z});
		startLineMesh.setBuffer(VertexBuffer.Type.Index, 2, new short[]{ 0, 1 });
		startLineMesh.updateBound();
		startLineMesh.updateCounts();
		laserStart = new Geometry("line", startLineMesh);
		laserStart.setMaterial(getAssetManager().loadMaterial("red_color.j3m"));
		rootNode.attachChild(laserStart);
		
		double reflectionVector[] = ok.getReflectionVector(320, 240, false, 45);
		Vector3f endEnd = cam.getRotation().mult(new Vector3f((float)reflectionVector[0] * -1, (float)reflectionVector[1], (float)reflectionVector[2])).normalize().mult((ok.getDepthMM(320, 240, false, false) / maxDistanceMM) * 12);
		Vector3f endStart = startEnd;
		
		Mesh endLineMesh = new Mesh();
		endLineMesh.setMode(Mesh.Mode.Lines);
		endLineMesh.setBuffer(VertexBuffer.Type.Position, 3, new float[]{ startEnd.x, startEnd.y, startEnd.z, endEnd.x, endEnd.y, endEnd.z});
		endLineMesh.setBuffer(VertexBuffer.Type.Index, 2, new short[]{ 0, 1 });
		endLineMesh.updateBound();
		endLineMesh.updateCounts();
		laserEnd = new Geometry("line", endLineMesh);
		laserEnd.setMaterial(getAssetManager().loadMaterial("red_color.j3m"));
		rootNode.attachChild(laserEnd);
		
		rootNode.updateGeometricState();
	}
	
	private void removeLaser(){
		rootNode.detachChild(laserStart);
		rootNode.detachChild(laserEnd);
		
		rootNode.updateGeometricState();
	}

	@Override
	public void onAction(String name, boolean keyPressed, float tpf) {
		if(keyPressed){
			if (name.equals(Actions.ONE.name())) {
				colorBlindMode = true;
				colorBlindType = ColorBlind.PROTANOPIA;
			} else if (name.equals(Actions.TWO.name())) {
				colorBlindMode = true;
				colorBlindType = ColorBlind.DEUTERANOPIA;
			} else if (name.equals(Actions.THREE.name())) {
				colorBlindMode = true;
				colorBlindType = ColorBlind.TRITANOPIA;
			} else if (name.equals(Actions.R.name())) {
				colorBlindMode = false;
				colorBlindType = ColorBlind.NORMAL;
			} else if (name.equals(Actions.SPACE.name())) {
				shootBall();
			} else if (name.equals(Actions.L.name())) {
				shootLaserMode = !shootLaserMode;
				if(!shootLaserMode){
					removeLaser();
				}
			} else if (name.equals(Actions.N.name())) {
				nightVisionMode = !nightVisionMode;
			}
		}
	}
	
	public class Ball{
		private Geometry ball;
		private boolean rebound = false;
		private Vector3f startDirection;
		private float endZ;
		private Vector3f reboundDirection;
		private float velocity;
		private float reboundExpirationTime;
		private float timeAfterCollision = 0;
		
		public Ball(Vector3f startPosition, Vector3f startDirection, float endZ, Vector3f reboundDirection, float velocity, float reboundExpirationTime){
			this.startDirection = startDirection;
			this.endZ = endZ;
			this.reboundDirection = reboundDirection;
			this.velocity = velocity;
			this.reboundExpirationTime = reboundExpirationTime;
			
			Sphere b = new Sphere(32, 32, .0625f);
			ball = new Geometry("sphere", b);
			b.setTextureMode(Sphere.TextureMode.Projected); // better quality on spheres
		    TangentBinormalGenerator.generate(b);           // for lighting effect
			Material mat = new Material(assetManager, 
			        "Common/MatDefs/Light/Lighting.j3md");
			mat.setTexture("DiffuseMap", 
				        assetManager.loadTexture("Textures/Terrain/Pond/Pond.jpg"));
			mat.setTexture("NormalMap", 
				        assetManager.loadTexture("Textures/Terrain/Pond/Pond_normal.png"));
			mat.setBoolean("UseMaterialColors",true);    
			mat.setColor("Diffuse",ColorRGBA.White);
			mat.setColor("Specular",ColorRGBA.White);
			mat.setFloat("Shininess", 64f);
			ball.setMaterial(mat);
			ball.setLocalTranslation(startPosition.x, startPosition.y, startPosition.z);
		}
		
		public Geometry getBall(){
			return this.ball;
		}
		
		public boolean travel(float tpf){
			boolean done = false;
			if(!rebound){
				ball.setLocalTranslation(ball.getLocalTranslation().add(startDirection.mult(velocity * tpf)));
				if(ball.getLocalTranslation().z >= endZ){
					rebound = true;
				}
			} else {
				timeAfterCollision += tpf;
				ball.setLocalTranslation(ball.getLocalTranslation().add(reboundDirection.mult(velocity * tpf)));
				if(reboundExpirationTime <= timeAfterCollision){
					done = true;
				}
			}
			
			ball.rotate(1f * tpf, 1f * tpf, 0);
			
			return done;
		}
	}
}
