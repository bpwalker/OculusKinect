/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.jme3.app.state;

import com.jme3.app.Application;
import com.jme3.app.SimpleApplication;
import com.jme3.post.FilterPostProcessor;
import com.jme3.renderer.Camera;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.control.StereoCameraControl;
import com.jme3.system.JmeSystem;
import com.jme3.system.Natives;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.jme3.post.BarrelDistortionFilter;
import com.jme3.post.Filter;
import com.jme3.post.SceneProcessor;
import com.jme3.post.filters.FogFilter;
import com.jme3.post.ssao.SSAOFilter;
import com.jme3.post.util.FilterUtil;
import com.jme3.scene.control.CameraControl;
import com.jme3.water.WaterFilter;
import java.util.List;
import oculusvr.input.HMDInfo;
import oculusvr.input.OculusRiftReader;

/**
 *
 * @author reden
 */
public class StereoCamAppState extends AbstractAppState{
    
    private SimpleApplication app;
    private FilterPostProcessor ppLeft, ppRight;
    private BarrelDistortionFilter filterLeft, filterRight;
    Camera camLeft,camRight;
    protected ViewPort viewPortLeft, viewPortRight, guiViewPortRight;
    private static OculusRiftReader oculus;
    private StereoCameraControl camControl = new StereoCameraControl();
    private HMDInfo info;
    
    static {
        String platform = JmeSystem.getPlatform().name();
        
        if(platform.startsWith("Win")){
            try {
                if(platform.endsWith("64")){
                    Natives.extractNativeLib("windows","OculusLib64", false, false);
                } else {
                    Natives.extractNativeLib("windows","OculusLib", false, false);
                }
                
            } catch (IOException ex) {
                System.out.println("failed to extract " + ex);
                Logger.getLogger(StereoCamAppState.class.getName()).log(Level.SEVERE, null, "Could not extract Oculus Rift library" + ex);
            }
        } else {
            try {
                throw new Exception("Sorry, platform not supported yet!");
            } catch (Exception ex) {
                Logger.getLogger(StereoCamAppState.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    @Override
    public void initialize(AppStateManager stateManager, Application app) {
        super.initialize(stateManager, app);
        this.app = (SimpleApplication) app;

        viewPortLeft = app.getViewPort();
        camLeft = app.getCamera();
        if(camControl != null){
            camControl.setCamera(camLeft);
        }
        
        if(camRight == null){
            camRight = camLeft.clone();
            camControl.setCamera2(camRight);
            
        }
        camControl.setControlDir(CameraControl.ControlDirection.SpatialToCamera);
        camLeft.setViewPort(0.0f, 0.5f, 0.0f, 1.0f);
        camRight.setViewPort(0.5f, 1f, 0.0f, 1f);
        viewPortRight = app.getRenderManager().createPostView("Right viewport", camRight);
        viewPortRight.setClearFlags(true, true, true);
        viewPortRight.attachScene(((SimpleApplication)app).getRootNode());

        try {
            oculus = new OculusRiftReader();
            camControl.setOculus(oculus);
            info = oculus.getHMDInfo();
            
        } catch (Exception ex) {
            info = new HMDInfo();
            info.createFakeValues();
            Logger.getLogger(StereoCamAppState.class.getName()).log(Level.SEVERE, null, ex);
        }
        filterRight =new BarrelDistortionFilter(info, false);
        filterLeft=new BarrelDistortionFilter(info, true);
        ppRight =new FilterPostProcessor(app.getAssetManager());
        for(SceneProcessor sceneProcessor : viewPortLeft.getProcessors()){
            if(sceneProcessor instanceof FilterPostProcessor){
                ppLeft = (FilterPostProcessor) sceneProcessor;
                break;
            }
        }
        if(ppLeft == null){
            ppLeft =new FilterPostProcessor(app.getAssetManager());
            viewPortLeft.addProcessor(ppLeft);
        }
        
        ppRight.addFilter(filterRight);
        ppLeft.addFilter(filterLeft);
        viewPortRight.addProcessor(ppRight);
        
        
        float offset = info.getInterpupillaryDistance() * 0.5f;
        camControl.setCamHalfDistance(offset);
        setupGuiViewports(0.045f);
        
        cloneProcessors();
    }
    
   
    
    @Override
    public void update(float tpf) {
        //super.update(tpf);
        
        if(oculus != null){
            //oculus.update();
        }
    }
    
    public void setCameraControl(StereoCameraControl control){
        this.camControl = control;
        camRight = control.getCamera2();
    }
    
    public StereoCameraControl getCameraControl(){
        return camControl;
    }
    
    public OculusRiftReader getOculus(){
        return oculus;
    }
    
    public static void setOculusRiftReader(OculusRiftReader orr){
        oculus = orr;
    }

    @Override
    public void cleanup() {
        super.cleanup();
        if(oculus != null){
            oculus.destroy();
        }
    }

    private void setupGuiViewports(float diff){
        ViewPort guiViewPortLeft = app.getGuiViewPort();
        Camera guiCamLeft = guiViewPortLeft.getCamera();
        
        
   
        Camera guiCamRight = guiCamLeft.clone();
        
        guiCamLeft.setViewPort(0.0f + diff, 0.5f + diff, 0.0f, 1.0f);
        guiCamRight.setViewPort(0.5f - diff, 1f - diff, 0.0f, 1f); // l,r,b,t

        guiViewPortRight = app.getRenderManager().createPostView("Gui Default Right", guiCamRight);
        guiViewPortRight.setClearFlags(false, false, false);
        guiViewPortRight.attachScene(((SimpleApplication)app).getGuiNode());
        
//        List<SceneProcessor> processors = guiViewPortLeft.getProcessors();
//        for(SceneProcessor sp: processors){
//            if(sp instanceof NiftyJmeDisplay){
//                
//                ClonedNiftyJmeDisplay niftyRight = new ClonedNiftyJmeDisplay((NiftyJmeDisplay)sp,
//                        viewPortRight);
//                guiViewPortRight.addProcessor(niftyRight);
//                ((NiftyJmeDisplay)sp).reshape(guiViewPortLeft, guiCamLeft.getWidth(), guiCamRight.getHeight());
//            }
//        }
    }
    
    private void cloneProcessors(){
        List<SceneProcessor> processors = viewPortLeft.getProcessors();
        for(SceneProcessor sp: processors){
            if(sp instanceof FilterPostProcessor){
                FilterPostProcessor fpp1 = (FilterPostProcessor) sp;
                
                for(Filter filter: fpp1.getFilterList()){
                    BarrelDistortionFilter bdf = ppRight.getFilter(BarrelDistortionFilter.class);
                    
                    Filter f2 = null;
                    if(filter instanceof FogFilter){
                        f2 = FilterUtil.cloneFogFilter((FogFilter)filter);
                        
                    } 
//                    else if (filter instanceof WaterFilter){
//                        f2 = ((WaterFilter)filter).clone();
//                    } 
                    else if (filter instanceof SSAOFilter){
                        
                        
                        f2 = FilterUtil.cloneSSAOFilter((SSAOFilter)filter);
                    } 
                    else if (!(filter instanceof BarrelDistortionFilter)){
                        f2 = filter; // dof, bloom, lightscattering
                    }
                    
                    if(f2 != null){
                        ppRight.removeFilter(bdf);
                        ppRight.addFilter(f2);
                        ppRight.addFilter(bdf);
                    }
                    
                }
            }
        }
    }
    
}