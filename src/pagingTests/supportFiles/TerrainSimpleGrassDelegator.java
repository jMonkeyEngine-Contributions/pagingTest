/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pagingTests.supportFiles;

import com.jme3.asset.AssetManager;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;
import com.jme3.texture.Texture;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import paging.core.ManagedMeshDelegator;
import paging.core.spatials.ManagedMesh;
import paging.core.spatials.ManagedNode;
import paging.core.tasks.DelegatorTask;

/**
 *
 * @author t0neg0d
 */
public class TerrainSimpleGrassDelegator extends ManagedMeshDelegator {
	
	AssetManager assetManager;
	Material grassMat;
	Texture grassTex;
	float tileDimensions, imposterHeight, imposterWidth;
//	int impostorTotal = 200;
	float terrainQuadSize = 1.5f;
	int impostorsPerQuad = 3;
	
	public TerrainSimpleGrassDelegator(AssetManager assetManager, float tileDimensions, float imposterHeight, float imposterWidth, float terrainQuadSize, int impostorsPerQuad) {
		this.assetManager = assetManager;
		this.tileDimensions = tileDimensions;
		this.imposterHeight = imposterHeight;
		this.imposterWidth = imposterWidth;
		this.terrainQuadSize = terrainQuadSize;
		this.impostorsPerQuad = impostorsPerQuad;
		
		grassTex = assetManager.loadTexture("Textures/Vegetation/Grass001.png");
		grassTex.setMinFilter(Texture.MinFilter.BilinearNearestMipMap);
		grassTex.setMagFilter(Texture.MagFilter.Bilinear);
		grassTex.setWrap(Texture.WrapMode.Repeat);
		
		grassMat = new Material(assetManager, "MatDefs/Lighting.j3md");
		grassMat.setBoolean("UseMaterialColors", true);
		grassMat.setBoolean("HighQuality", true);
		grassMat.setFloat("Shininess", .0f);
		grassMat.setColor("Ambient", ColorRGBA.White);
		grassMat.setColor("Diffuse", ColorRGBA.White);
		grassMat.setTexture("DiffuseMap", grassTex);
		grassMat.setFloat("AlphaDiscardThreshold", 0.75f);
		grassMat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Back);
		grassMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
	}
	
	@Override
	protected ManagedMesh createMesh(Vector3f position, ManagedNode dependantNode, Object customData) {
		TerrainSimpleGrass grass = new TerrainSimpleGrass(assetManager, "Textures/Vegetation/Grass002.png", 1, 1);
		grass.setImpostorHeight(imposterHeight);
		grass.setImpostorWidth(imposterWidth);
		
		ManagedNode n = (ManagedNode) dependantNode.clone();
		n.removeFromParent();
		
		grass.applyToTerrainGrid((ArrayList)customData);
		return grass;
	}

	@Override
	public void delegatorTaskCustomData(float tpf, DelegatorTask task) {
		Vector3f position;
		List<Vector3f> positions = new ArrayList();
		for (int x = 0; x < (int)(tileDimensions/terrainQuadSize); x++) {
			for (int z = 0; z < (int)(tileDimensions/terrainQuadSize); z++) {
				for (int i = 0; i < impostorsPerQuad; i++) {
					float rand1 = (float)Math.random()*terrainQuadSize;
					float rand2 = (float)Math.random()*terrainQuadSize;
					if ((int)Math.round(Math.random()) == 0) {
						rand1 = -rand1;
					}
					if ((int)Math.round(Math.random()) == 0) {
						rand2 = -rand2;
					}
					Ray ray = new Ray();
					ray.setOrigin(task.getDependentNode().getLocalTranslation().add(terrainQuadSize*(float)x+rand1, 4000f, terrainQuadSize*(float)z+rand2));
					ray.setDirection(Vector3f.UNIT_Y.negate());

					CollisionResults rayResults = new CollisionResults();
					task.getDependentNode().collideWith(ray, rayResults);

					CollisionResult result = null;

					if (rayResults.size() > 0) {
						result = rayResults.getCollision(0);
					}
					if (result != null) {
						position = result.getContactPoint();
					//	position.subtractLocal(startVec);
						positions.add(position.subtract(task.getDependentNode().getLocalTranslation()));
					}
				}
			}
		}
		task.setCustomData(positions);
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
