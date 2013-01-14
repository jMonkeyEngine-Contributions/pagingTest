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
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.VertexBuffer;
import com.jme3.scene.control.Control;
import com.jme3.scene.mesh.IndexBuffer;
import com.jme3.texture.Texture;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.List;
import paging.core.DelegatorListener;
import paging.core.ManagedMeshDelegator;
import paging.core.ManagedMeshesAsNodeDelegator;
import paging.core.ManagedNodeDelegator;
import paging.core.spatials.ManagedMesh;
import paging.core.spatials.ManagedMeshFromTemplate;
import paging.core.spatials.ManagedNode;
import paging.core.tasks.DelegatorTask;
import paging.core.utils.MeshInfo;
import paging.core.utils.MeshUtils;

/**
 *
 * @author t0neg0d
 */
public class TerrainSimpleTreeDelegator extends ManagedMeshesAsNodeDelegator {
	AssetManager assetManager;
	Material treeMat, leavesMat;
	Texture treeTex, leavesTex;
	float tileDimensions;
	
	FloatBuffer verts, verts2, coords, coords2, normals, normals2;
	IndexBuffer indexes, indexes2;
	
	Mesh templateTree, templateLeaves;
	
	Quaternion qR = new Quaternion();
	
	public TerrainSimpleTreeDelegator(AssetManager assetManager, float tileDimensions) {
		this.assetManager = assetManager;
		this.tileDimensions = tileDimensions;
		
		templateTree = ((Geometry)((Node)assetManager.loadModel("Models/Vegetation/Pine.j3o")).getChild(0)).getMesh();
		templateLeaves = ((Geometry)((Node)assetManager.loadModel("Models/Vegetation/Leaves.j3o")).getChild(0)).getMesh();
		
		// Extract template buffers
		verts = MeshUtils.getPositionBuffer(templateTree);
		coords = MeshUtils.getTexCoordBuffer(templateTree);
		indexes = MeshUtils.getIndexBuffer(templateTree);
		normals = MeshUtils.getNormalsBuffer(templateTree);
		verts2 = MeshUtils.getPositionBuffer(templateLeaves);
		coords2 = MeshUtils.getTexCoordBuffer(templateLeaves);
		indexes2 = MeshUtils.getIndexBuffer(templateLeaves);
		normals2 = MeshUtils.getNormalsBuffer(templateLeaves);
	}

	@Override
	public void initDelegatorMaterials() {
		treeTex = assetManager.loadTexture("Textures/Vegetation/Bark.jpg");
		treeTex.setMinFilter(Texture.MinFilter.BilinearNearestMipMap);
		treeTex.setMagFilter(Texture.MagFilter.Bilinear);
		treeTex.setWrap(Texture.WrapMode.Repeat);
		
		treeMat = new Material(assetManager, "MatDefs/Lighting.j3md");
		treeMat.setBoolean("UseMaterialColors", true);
		treeMat.setBoolean("HighQuality", true);
		treeMat.setFloat("Shininess", 0f);
		treeMat.setColor("Ambient", ColorRGBA.White);
		treeMat.setColor("Diffuse", ColorRGBA.White);
		treeMat.setTexture("DiffuseMap", treeTex);
		treeMat.setBoolean("UseFade", true);
		treeMat.setFloat("FadeStartDistance", 400f);
		treeMat.setFloat("FadeMaxDistance", 600f);
	//	treeMat.setFloat("AlphaDiscardThreshold", 0.75f);
		treeMat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Back);
		treeMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
		
		leavesTex = assetManager.loadTexture("Textures/Vegetation/Leaves.png");
		leavesTex.setMinFilter(Texture.MinFilter.BilinearNearestMipMap);
		leavesTex.setMagFilter(Texture.MagFilter.Bilinear);
		leavesTex.setWrap(Texture.WrapMode.Repeat);
		
		leavesMat = new Material(assetManager, "MatDefs/Lighting.j3md");
		leavesMat.setBoolean("UseMaterialColors", true);
		leavesMat.setBoolean("HighQuality", true);
		leavesMat.setFloat("Shininess", 0f);
		leavesMat.setColor("Ambient", ColorRGBA.White);
		leavesMat.setColor("Diffuse", ColorRGBA.White);
		leavesMat.setTexture("DiffuseMap", leavesTex);
		leavesMat.setFloat("AlphaDiscardThreshold", 0.35f);
		leavesMat.setBoolean("UseFade", true);
		leavesMat.setFloat("FadeStartDistance", 400f);
		leavesMat.setFloat("FadeMaxDistance", 600f);
		leavesMat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Back);
		leavesMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
	}

	@Override
	protected ManagedNode createNode(Vector3f position, ManagedNode dependantNode, Object customData) {
		ManagedNode node = new ManagedNode();
	//	System.out.println("Custom data is null? " + (customData == null));
		if (customData != null) {
			if (!((List<MeshInfo>)customData).isEmpty()) {
				node.setName(UID + ":" + position + ":Node");
				
				ManagedMeshFromTemplate trees = new ManagedMeshFromTemplate(assetManager, verts, coords, indexes, normals);
				trees.setMeshInfo((List<MeshInfo>)customData);
				
				ManagedMeshFromTemplate leaves = new ManagedMeshFromTemplate(assetManager, verts2, coords2, indexes2, normals2);
				leaves.setMeshInfo((List<MeshInfo>)customData);
			
				Geometry geom = new Geometry(UID + ":Trunks" + position.x + ":" + position.y + ":" + position.z + ":Geom");
				geom.setMesh(trees);
				geom.setMaterial(treeMat);
				node.attachChild(geom);
				
				Geometry geom2 = new Geometry(UID + ":Leaves:" + position.x + ":" + position.y + ":" + position.z + ":Geom");
				geom2.setMesh(leaves);
				geom2.setMaterial(leavesMat);
				node.attachChild(geom2);
				
				node.setQueueBucket(bucket);
			} else {
				System.out.println("Custom data had 0 MeshInfo entries.");
			}
		} else {
			System.out.println("Custom data was null =(");
		}
		return node;
	}
	
	@Override
	public void delegatorTaskCustomData(float tpf, DelegatorTask task) {
		DelegatorTask terrain = pm.getDelegatorByUID("Terrain").getTaskContainingLocation(task.getPosition());
		if (terrain != null) {
			if (terrain.getStage() != DelegatorTask.STAGE.COMPLETE) {
			//	System.out.println("Terrain task not complete, removing current task.");
				tiles.remove(task.getPosition());
			} else {
			//	System.out.println("Terrain task complete. Creating MeshInfo.");
				List<MeshInfo> positions = new ArrayList();
				int cVerts = 3;
				for (int i = 0; i < cVerts; i++) {
				//	float offset = (tileDimensions/cVerts*i);
				//	float rem = tileDimensions-offset;
					float rand1 = ((float)Math.random()*tileDimensions);
					float rand2 = ((float)Math.random()*tileDimensions);;

					Ray ray = new Ray();

					ray.setOrigin(task.getPosition().add(rand1, 4000f, rand2));
					ray.setDirection(Vector3f.UNIT_Y.negate());

					CollisionResults rayResults = new CollisionResults();
					CollisionResult result = null;

					terrain.getNode().collideWith(ray, rayResults);

					if (rayResults.size() > 0) {
						result = rayResults.getCollision(0);
					}
					if (result != null) {
					//	System.out.println("Adding MeshInfo entry.");
						positions.add(
							new MeshInfo(
								result.getContactPoint(),
								new Quaternion().fromAngleAxis((float)Math.random()*360f*2f*FastMath.DEG_TO_RAD, Vector3f.UNIT_Y),
								0.5f+(float)Math.random()
							)
						);
					}
				}
			//	System.out.println("Setting task's custom data");
				task.setCustomData(positions);
			}
		}
	}

	@Override
	public void delegatorUpdate(float tpf) {
		
	}
	
	@Override
	public Geometry getGeometry() {
		Geometry geom = new Geometry();
		geom.setMaterial(treeMat);
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
