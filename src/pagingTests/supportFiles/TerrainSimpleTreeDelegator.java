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
import com.jme3.texture.Texture;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;
import paging.core.DelegatorListener;
import paging.core.ManagedMeshDelegator;
import paging.core.ManagedNodeDelegator;
import paging.core.spatials.ManagedMesh;
import paging.core.spatials.ManagedNode;
import paging.core.tasks.DelegatorTask;

/**
 *
 * @author t0neg0d
 */
public class TerrainSimpleTreeDelegator extends ManagedNodeDelegator implements DelegatorListener {
	
	AssetManager assetManager;
	Material treeMat, leavesMat;
	Texture treeTex, leavesTex;
	float tileDimensions;
	
	List<Vector3f> verts = new ArrayList();
	List<Vector2f> coords = new ArrayList();
	List<Integer> indexes = new ArrayList();
	List<Float> normals = new ArrayList();
	
	Mesh templateTree, templateLeaves;
	
	Quaternion qR = new Quaternion();
	
	public TerrainSimpleTreeDelegator(AssetManager assetManager, float tileDimensions) {
		this.assetManager = assetManager;
		this.tileDimensions = tileDimensions;
		
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
		leavesMat.getAdditionalRenderState().setFaceCullMode(RenderState.FaceCullMode.Back);
		leavesMat.getAdditionalRenderState().setBlendMode(RenderState.BlendMode.Alpha);
	//	treeMat.setTexture("Diffuse", null);
		
		templateTree = ((Geometry)((Node)assetManager.loadModel("Models/Vegetation/Pine.j3o")).getChild(0)).getMesh();
		templateLeaves = ((Geometry)((Node)assetManager.loadModel("Models/Vegetation/Leaves.j3o")).getChild(0)).getMesh();
		/*
		FloatBuffer positions = template.getFloatBuffer(VertexBuffer.Type.Position);
		for (int i = 0; i < positions.limit(); i += 3) {
			verts.add(new Vector3f(positions.get(i), positions.get(i+1), positions.get(i+2)));
		}
		
		FloatBuffer texMap = template.getFloatBuffer(VertexBuffer.Type.TexCoord);
		for (int i = 0; i < texMap.limit(); i += 2) {
			coords.add(new Vector2f(texMap.get(i), texMap.get(i+1)));
		}
		
		for (int i = 0; i < template.getIndexBuffer().size(); i++) {
			indexes.add(template.getIndexBuffer().get(i));
		}
		
		FloatBuffer tempNormals = template.getFloatBuffer(VertexBuffer.Type.Normal);
		for (int i = 0; i < tempNormals.limit(); i ++) {
			normals.add(tempNormals.get(i));
		}
		*/
	}
	
	@Override
	public void delegatorTaskCustomData(float tpf, DelegatorTask task) {
	//	throw new UnsupportedOperationException("Not supported yet.");
	}
	
	@Override
	public Geometry getGeometry() {
		Geometry geom = new Geometry();
	//	geom.setMaterial(treeMat);
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

	@Override
	public void delegatorUpdate(float tpf) {
		
	}

	public void onAddToScene(Node node) {
		Vector3f position = node.getLocalTranslation();
		if (!tiles.containsKey(position)) {
			ManagedNode nNode = addTrees(new ManagedNode(), node, node.getLocalTranslation());
			nNode.setLocalTranslation(position);
		//	nNode.setMaterial(treeMat);
			this.addManagedNode(nNode);
		}
	}
	
	private ManagedNode addTrees(ManagedNode ret, Node n, Vector3f position) {
		int cVerts = 1;//(int)Math.round(Math.random()*5);
		for (int i = 0; i < cVerts; i++) {
			float rand1 = (float)Math.random()*tileDimensions;
			float rand2 = (float)Math.random()*tileDimensions;
			
			Ray ray = new Ray();
			
			ray.setOrigin(position.add(rand1, 4000f, rand2));
			ray.setDirection(Vector3f.UNIT_Y.negate());
			
			CollisionResults rayResults = new CollisionResults();
			CollisionResult result = null;
			
			n.collideWith(ray, rayResults);
			
			if (rayResults.size() > 0) {
				result = rayResults.getCollision(0);
			}
			if (result != null) {
				Vector3f loc = result.getContactPoint();
			//	position.subtractLocal(startVec);
				
				qR = qR.fromAngleAxis( ((float)(Math.random()*360f))*FastMath.DEG_TO_RAD, Vector3f.UNIT_Y);
				float scale = (float)Math.random();
				
				Geometry geomt = new Geometry("Tree");
				geomt.setMesh(templateTree);
				geomt.setLocalTranslation(loc.subtract(position));
				geomt.setLocalRotation(qR);
				geomt.setLocalScale(1f+scale);
				geomt.setMaterial(treeMat);
				
				Geometry geoml = new Geometry("Leaves");
				geoml.setMesh(templateLeaves);
				geoml.setLocalTranslation(loc.subtract(position));
				geoml.setLocalRotation(qR);
				geoml.setLocalScale(1f+scale);
				geoml.setMaterial(leavesMat);
				
				ret.attachChild(geomt);
				ret.attachChild(geoml);
			}
		}
		return ret;
	}
	
	public void onRemoveFromScene(Node node) {
	//	throw new UnsupportedOperationException("Not supported yet.");
	}
}
