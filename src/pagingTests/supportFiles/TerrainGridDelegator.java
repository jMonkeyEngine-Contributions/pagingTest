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
import paging.core.tasks.DelegatorTask;

/**
 *
 * @author t0neg0d
 */
public class TerrainGridDelegator extends ManagedMeshDelegator {
	private AssetManager assetManager;
	private int gridSize;
	private float gridQuadSize;
	private Texture diffuse1, diffuse2, diffuse3, normal1, normal2;
	private Material terrainMat;
	
	public TerrainGridDelegator(AssetManager assetManager, int gridSize, float gridQuadSize) {
		this.assetManager = assetManager;
		this.gridSize = gridSize;
		this.gridQuadSize = gridQuadSize;
	}

	@Override
	public void initDelegatorMaterials() {
		normal1 = assetManager.loadTexture("Textures/TerrainTiles/diffuse3_n.png");
		normal1.setMinFilter(Texture.MinFilter.BilinearNearestMipMap);
		normal1.setMagFilter(Texture.MagFilter.Bilinear);
		normal1.setWrap(Texture.WrapMode.Repeat);
		
		normal2 = assetManager.loadTexture("Textures/TerrainTiles/diffuse4_n.png");
		normal2.setMinFilter(Texture.MinFilter.BilinearNearestMipMap);
		normal2.setMagFilter(Texture.MagFilter.Bilinear);
		normal2.setWrap(Texture.WrapMode.Repeat);
		
		diffuse1 = assetManager.loadTexture("Textures/TerrainTiles/diffuse3.png");
		diffuse1.setMinFilter(Texture.MinFilter.BilinearNearestMipMap);
		diffuse1.setMagFilter(Texture.MagFilter.Bilinear);
		diffuse1.setWrap(Texture.WrapMode.Repeat);
		
		diffuse2 = assetManager.loadTexture("Textures/TerrainTiles/diffuse4.png");
		diffuse2.setMinFilter(Texture.MinFilter.BilinearNearestMipMap);
		diffuse2.setMagFilter(Texture.MagFilter.Bilinear);
		diffuse2.setWrap(Texture.WrapMode.Repeat);
		
		diffuse3 = assetManager.loadTexture("Textures/TerrainTiles/diffuse3.png");
		diffuse3.setMinFilter(Texture.MinFilter.BilinearNearestMipMap);
		diffuse3.setMagFilter(Texture.MagFilter.Bilinear);
		diffuse3.setWrap(Texture.WrapMode.Repeat);
		
		terrainMat = new Material(assetManager, "MatDefs/TerrainLighting.j3md");
		terrainMat.setBoolean("UseMaterialColors", true);
		terrainMat.setBoolean("HighQuality", true);
		terrainMat.setFloat("Shininess", 0f);
		terrainMat.setColor("Ambient", ColorRGBA.White);
		terrainMat.setColor("Diffuse", ColorRGBA.White);
		terrainMat.setTexture("DiffuseMap1", diffuse1);
	//	terrainMat.setTexture("NormalMap1", normal1);
		terrainMat.setFloat("DiffuseScale1", 1f/12f);
		terrainMat.setTexture("DiffuseMap2", diffuse2);
	//	terrainMat.setTexture("NormalMap2", normal2);
		terrainMat.setFloat("DiffuseScale2", 1f/12f);
	//	terrainMat.setBoolean("UseFade", true);
	//	terrainMat.setFloat("FadeStartDistance", 1000f);
	//	terrainMat.setFloat("FadeMaxDistance", 1800f);
		terrainMat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Back);
	//	terrainMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
	//	terrainMat.getAdditionalRenderState().setWireframe(true);
	}
	
	@Override
	public Geometry getGeometry() {
		Geometry geom = new Geometry();
		geom.setMaterial(terrainMat);
		return geom;
	}

	@Override
	protected ManagedMesh createMesh(Vector3f position, ManagedNode dependentNode, Object customData) {
		TerrainGrid grid = new TerrainGrid(assetManager, gridSize, gridQuadSize, position);
		grid.buildMesh();
		return grid;
	}
	
	@Override
	public void delegatorTaskCustomData(float tpf, DelegatorTask task) {
	//	throw new UnsupportedOperationException("Not supported yet.");
	}
	
	@Override
	public void delegatorUpdate(float tpf) {
	//	throw new UnsupportedOperationException("Not supported yet.");
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
