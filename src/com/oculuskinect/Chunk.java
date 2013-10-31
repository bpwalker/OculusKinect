package com.oculuskinect;

import com.jme3.asset.AssetManager;
import com.jme3.bounding.BoundingVolume;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.util.BufferUtils;

public class Chunk extends Geometry{
        Mesh mesh = new Mesh();
        public Chunk(int width,int height,AssetManager assetManager){
                float vertices[]= new float[4*3*width*height];
                int vertexIndex=0;
                for(int x=0; x<width; x++){
                        for(int y=0; y<height; y++){
                                int currentVertex=vertexIndex/3;

                                vertices[vertexIndex++]=x;
                                vertices[vertexIndex++]=0;
                                vertices[vertexIndex++]=y;

                                vertices[vertexIndex++]=x+1;
                                vertices[vertexIndex++]=0;
                                vertices[vertexIndex++]=y;

                                vertices[vertexIndex++]=x;
                                vertices[vertexIndex++]=0;
                                vertices[vertexIndex++]=y+1;

                                vertices[vertexIndex++]=x+1;
                                vertices[vertexIndex++]=0;
                                vertices[vertexIndex++]=y+1;

                        }
                }
                mesh.setBuffer(Type.Position, 3, BufferUtils.createFloatBuffer(vertices));
                mesh.setMode(Mesh.Mode.Points);
                mesh.setPointSize(4f);
                mesh.updateBound();
                setMesh(mesh);
                Material mat = new Material(assetManager,"Common/MatDefs/Misc/Unshaded.j3md");  // create a simple material
                mat.setColor("Color", ColorRGBA.Blue);   // set color of material to blue
                setMaterial(mat); 
        }
        public BoundingVolume getBound(){
                return mesh.getBound();
        }

}