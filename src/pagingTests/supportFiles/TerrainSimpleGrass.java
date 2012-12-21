/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pagingTests.supportFiles;

import com.jme3.asset.AssetManager;
import com.jme3.collision.CollisionResult;
import com.jme3.collision.CollisionResults;
import com.jme3.material.Material;
import com.jme3.material.RenderState.BlendMode;
import com.jme3.material.RenderState.FaceCullMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.math.Ray;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.renderer.queue.RenderQueue.Bucket;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.MagFilter;
import com.jme3.texture.Texture.MinFilter;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.util.BufferUtils;
import com.jme3.util.TangentBinormalGenerator;
import java.util.ArrayList;
import java.util.List;
import paging.core.PagingManager.LOD;
import paging.core.spatials.ManagedMesh;

/**
 *
 * @author t0neg0d
 */
public class TerrainSimpleGrass extends ManagedMesh {
	AssetManager assetManager;
	
	List<Vector3f> verts = new ArrayList();
	List<Vector2f> texCoord = new ArrayList();
	List<Integer> indexes = new ArrayList();
	List<Float> normals = new ArrayList();
	
	private Vector3f[] finVertices = new Vector3f[0];
	private Vector2f[] finTexCoord = new Vector2f[0];
	private int[] finIndexes = new int[0];
	private float[] finNormals = new float[0];
	
	Quaternion qR = new Quaternion();
	Quaternion q180 = new Quaternion();
	Quaternion q33 = new Quaternion();
	Quaternion q66 = new Quaternion();
	
	float width = 0.2f;
	float height = 0.2f;
	float widthUnit = 0.1f;
	float heightUnit = 0.1f;
	
//	Material mat;
//	Texture tex;
	String texPath;
	int imgCols = 1;
	int imgRows = 1;
	
	Spatial terrain;
	
	private Vector2f[] coordTemplate = new Vector2f[] {
		new Vector2f(0f,0f),
		new Vector2f(1f,0f),
		new Vector2f(0f,1f),
		new Vector2f(1f,1f)
	};
	private Vector3f[] normalsTemplate = new Vector3f[] {
		new Vector3f(1f, 0f, 0f),
		new Vector3f(-1f, 0f, 0f),
		new Vector3f(1f, 0f, 0f),
		new Vector3f(-1f, 0f, 0f),
		new Vector3f(1f, 0f, 0f),
		new Vector3f(-1f, 0f, 0f)
	//	q33.mult(new Vector3f(1f, 0f, 0f)),
	//	q33.mult(new Vector3f(-1f, 0f, 0f)),
	//	q66.mult(new Vector3f(1f, 0f, 0f)),
	//	q66.mult(new Vector3f(-1f, 0f, 0f))
	};
	
	float size = 0.2f;
	
	public TerrainSimpleGrass(AssetManager assetManager, String texPath, int imgCols, int imgRows) {
		this.assetManager = assetManager;
		this.imgCols = imgCols;
		this.imgRows = imgRows;
		this.texPath = texPath;
		
		q180 = q180.fromAngleAxis(180f*2f*FastMath.DEG_TO_RAD, Vector3f.UNIT_Y);
		q33 = q33.fromAngleAxis(33f*2f*FastMath.DEG_TO_RAD, Vector3f.UNIT_Y);
		q66 = q66.fromAngleAxis(66f*2f*FastMath.DEG_TO_RAD, Vector3f.UNIT_Y);
		/*
		this.coordTemplate = new Vector2f[imgCols+imgRows][4];
		int index = 0;
		for (int i = 0; i < imgCols; i++) {
			for (int j = 0; j < imgRows; j++) {
				this.coordTemplate[index][0] = new Vector2f(1.0f/(float)imgCols*(i),	1.0f/(float)imgRows*(j));
				this.coordTemplate[index][1] = new Vector2f(1.0f/(float)imgCols*(i+1),	1.0f/(float)imgRows*(j));
				this.coordTemplate[index][2] = new Vector2f(1.0f/(float)imgCols*(i),	1.0f/(float)imgRows*(j+1));
				this.coordTemplate[index][3] = new Vector2f(1.0f/(float)imgCols*(i+1),	1.0f/(float)imgRows*(j+1));
				index++;
			}
		}
		*/
	}
	
	public void setImpostorWidth(float width) {
		this.width = width;
		this.widthUnit = width/2f;
	}
	
	public float getImpostorWidth() {
		return this.width;
	}
	
	public void setImpostorHeight(float height) {
		this.height = height;
		this.heightUnit = height/2f;
	}
	
	public float getImpostorHeight() {
		return this.height;
	}
	
	public void applyToTerrainGrid(List<Vector3f> positions) {
		/*
		int cVerts = 15;//(int)(FastMath.sqrt(terrain.getVertexCount()/4));
		float offset = gridDimensions/(float)cVerts;
		offset += offset*0.1f;
		float randOffset = offset;//*0.25f;
		Vector3f position = null;
	//	Ray ray = new Ray();
	//	CollisionResults rayResults = new CollisionResults();
	//	CollisionResult result = null;
		
		for (int x = -1; x < cVerts+1; x++) {
			for (int z = -1; z < cVerts+1; z++) {
				for (int i = 0; i < 3; i++) {
					float rand1 = (float)Math.random()*randOffset;
					float rand2 = (float)Math.random()*randOffset;
					if ((int)Math.round(Math.random()) == 0) {
						rand1 = -rand1;
					}
					if ((int)Math.round(Math.random()) == 0) {
						rand2 = -rand2;
					}
					Ray ray = new Ray();
					ray.setOrigin(terrain.getLocalTranslation().add(offset*(float)x+rand1, 4000f, offset*(float)z+rand2));
					ray.setDirection(Vector3f.UNIT_Y.negate());
					
					CollisionResults rayResults = new CollisionResults();
					terrain.collideWith(ray, rayResults);
					
					CollisionResult result = null;
					
					if (rayResults.size() > 0) {
						result = rayResults.getCollision(0);
					}
					if (result != null) {
						position = result.getContactPoint();
					//	position.subtractLocal(startVec);
						addGrass(position.subtract(terrain.getLocalTranslation()));
					}
				}
			}
		}
		*/
		for (int i = 0; i < positions.size(); i++) {
			addGrass(positions.get(i));
		}
		build();
		
	//	verts.clear();
		verts = null;
	//	texCoord.clear();
		texCoord = null;
	//	indexes.clear();
		indexes = null;
	//	normals.clear();
		normals = null;
	}
	
	public void addGrass(Vector3f offsetVec) {
		qR = qR.fromAngleAxis( ((float)(Math.random()*360f))*2f*FastMath.DEG_TO_RAD, Vector3f.UNIT_Y);
		
		// Create vertices
		int cIndex = verts.size();
		
		float nHeightUnit;// = heightUnit;
		
		int rand = 0;//(int)Math.floor(Math.random() * (imgCols*imgRows));
		int rand2 = (int)Math.floor(Math.random() * 16);
		int rand3 = (int)Math.floor(Math.random() * 8);
		int rand4 = (int)Math.floor(Math.random() * 2);
		if (rand2 == 0) {
			if (rand3 == 0)	{ rand = 0; }
			else			{ rand = 3; }
		} else {
			if (rand4 == 0)	{ rand = 1; }
			else			{ rand = 2; }
		}
		rand = 0;
		
		if (rand == 0 || rand == 3) {
			nHeightUnit = ((float)Math.random()*(heightUnit/3f))+(heightUnit*2f);
		} else {
			nHeightUnit = ((float)Math.random()*(heightUnit/3f))+heightUnit;
		}
		verts.add(new Vector3f( widthUnit,	nHeightUnit*1.5f,	0f));
		verts.add(new Vector3f(-widthUnit,	nHeightUnit*1.5f,	0f));
		verts.add(new Vector3f( widthUnit, -(nHeightUnit*.5f),	0f));
		verts.add(new Vector3f(-widthUnit, -(nHeightUnit*.5f),	0f));
		verts.add(new Vector3f( widthUnit,	nHeightUnit*1.5f,	0f));
		verts.add(new Vector3f(-widthUnit,	nHeightUnit*1.5f,	0f));
		verts.add(new Vector3f( widthUnit, -(nHeightUnit*.5f),	0f));
		verts.add(new Vector3f(-widthUnit, -(nHeightUnit*.5f),	0f));
		
		for (int i = cIndex; i < cIndex+8; i++) {
			verts.set(i, qR.mult(verts.get(i).clone()));
		}
		
		for (int i = cIndex; i < cIndex+8; i++) {
			verts.add(q33.mult(verts.get(i).clone()));
		}
		
		for (int i = cIndex; i < cIndex+8; i++) {
			verts.add(q66.mult(verts.get(i).clone()));
		}
		
		for (int i = cIndex; i < verts.size(); i++) {
		//	verts.get(i).addLocal(startVec);
			verts.get(i).addLocal(offsetVec);
		}
		
		// Map triangle faces
		int offsetInc = 8;
		int offset = cIndex;
		
		for (int i = 0; i < 3; i++) {
			indexes.add(offset+0);
			indexes.add(offset+2);
			indexes.add(offset+3);
			indexes.add(offset+3);
			indexes.add(offset+1);
			indexes.add(offset+0);
			
			indexes.add(offset+3);
			indexes.add(offset+2);
			indexes.add(offset+0);
			indexes.add(offset+0);
			indexes.add(offset+1);
			indexes.add(offset+3);
			
			offset += offsetInc;
		}
		
		// Map texture coordinates
	//	System.out.println(rand);
		for (int x = 0; x < 6; x++) {
			for (int i = 0; i < 4; i++) {
				texCoord.add(coordTemplate[i]);
			}
		}
		
		int templateIndex = 0;
		for (int x = 0; x < 6; x++) {
			for (int i = 0; i < 4; i++) {
				normals.add(normalsTemplate[templateIndex].x);
				normals.add(normalsTemplate[templateIndex].y);
				normals.add(normalsTemplate[templateIndex].z);
			}
			templateIndex++;
		}
	}

	@Override
	public void build() {
		this.finVertices = new Vector3f[verts.size()];
		this.finTexCoord = new Vector2f[texCoord.size()];
		this.finIndexes = new int[indexes.size()];
		this.finNormals = new float[normals.size()];
		
		int index = 0;
		for (Vector3f vert : verts) {
			finVertices[index] = vert;
			index++;
		}
		
		index = 0;
		for (Vector2f coord : texCoord) {
			finTexCoord[index] = coord;
			index++;
		}
		
		index = 0;
		for (Integer ind : indexes) {
			finIndexes[index] = ind.intValue();
			index++;
		}
		
		index = 0;
		for (Float normal : normals) {
			finNormals[index] = normal.floatValue();
			index++;
		}
		
		this.clearBuffer(Type.Position);
		this.setBuffer(Type.Position,	3, BufferUtils.createFloatBuffer(finVertices));
		this.clearBuffer(Type.TexCoord);
		this.setBuffer(Type.TexCoord,	2, BufferUtils.createFloatBuffer(finTexCoord));
		this.clearBuffer(Type.Index);
		this.setBuffer(Type.Index,		3, BufferUtils.createIntBuffer(finIndexes));
		this.clearBuffer(Type.Normal);
		this.setBuffer(Type.Normal,		3, BufferUtils.createFloatBuffer(finNormals));
		this.updateBound();
	}

	@Override
	public void updateLOD(LOD lod) {
	//	throw new UnsupportedOperationException("Not supported yet.");
	}
}
