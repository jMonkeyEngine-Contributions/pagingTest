/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pagingTests.supportFiles;

import com.jme3.asset.AssetManager;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;
import com.jme3.texture.Texture;
import java.io.IOException;
import paging.core.ManagedMeshDelegator;
import paging.core.spatials.ManagedMesh;
import paging.core.spatials.ManagedNode;

/**
 *
 * @author t0neg0d
 */
public class TerrainSimpleGrassDelegator extends ManagedMeshDelegator {
	
	AssetManager assetManager;
	Material grassMat;
	Texture grassTex;
	float tileDimensions, imposterHeight, imposterWidth;
	
	public TerrainSimpleGrassDelegator(AssetManager assetManager, float tileDimensions, float imposterHeight, float imposterWidth) {
		this.assetManager = assetManager;
		this.tileDimensions = tileDimensions;
		this.imposterHeight = imposterHeight;
		this.imposterWidth = imposterWidth;
		
		grassTex = assetManager.loadTexture("Textures/Vegetation/Grass002.png");
		grassTex.setMinFilter(Texture.MinFilter.BilinearNearestMipMap);
		grassTex.setMagFilter(Texture.MagFilter.Bilinear);
		grassTex.setWrap(Texture.WrapMode.Repeat);
		
		grassMat = new Material(assetManager, "MatDefs/Lighting.j3md");
		grassMat.setBoolean("UseMaterialColors", true);
		grassMat.setBoolean("HighQuality", true);
		grassMat.setFloat("Shininess", .0f);
		grassMat.setColor("Ambient", ColorRGBA.White);
		grassMat.setColor("Diffuse", ColorRGBA.White);
	//	grassMat.setTexture("DiffuseMap1", grassTex);
	//	grassMat.setFloat("DiffuseScale1", .5f);
	//	grassMat.setTexture("DiffuseMap2", grassTex);
	//	grassMat.setFloat("DiffuseScale2", .5f);
	//	grassMat.setTexture("DiffuseMap3", grassTex);
	//	grassMat.setFloat("DiffuseScale3", .5f);
		grassMat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Off);
		grassMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
	}
	
	@Override
	protected ManagedMesh createMesh(Vector3f position, ManagedNode dependantNode) {
		TerrainSimpleGrass grass = new TerrainSimpleGrass(assetManager, "Textures/Vegetation/Grass002.png", 2, 2);
		grass.setImpostorHeight(imposterHeight);
		grass.setImpostorWidth(imposterWidth);
		grass.applyToTerrainGrid(dependantNode, tileDimensions);
		return grass;
	}

	@Override
	public void delegatorUpdate(float tpf) {
	//	throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public Geometry getGeometry() {
		Geometry geom = new Geometry();
		geom.setMaterial(grassMat);
		return geom;
	}

	public Control cloneForSpatial(Spatial spatial) {
		return this;
	}

	public void setSpatial(Spatial spatial) {
		this.spatial = spatial;
	}

	public void render(RenderManager rm, ViewPort vp) {
	//	throw new UnsupportedOperationException("Not supported yet.");
	}

	public void write(JmeExporter ex) throws IOException {
	//	throw new UnsupportedOperationException("Not supported yet.");
	}

	public void read(JmeImporter im) throws IOException {
	//	throw new UnsupportedOperationException("Not supported yet.");
	}
}
