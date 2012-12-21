/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pagingTests.supportFiles;

import pagingTests.supportFiles.Vector2i;
import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState.FaceCullMode;
import com.jme3.math.ColorRGBA;
import com.jme3.math.FastMath;
import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.MagFilter;
import com.jme3.texture.Texture.MinFilter;
import com.jme3.texture.Texture.WrapMode;
import com.jme3.util.BufferUtils;
import paging.core.PagingManager.LOD;
import paging.core.spatials.ManagedMesh;

/**
 *
 * @author t0neg0d
 */
public final class TerrainGrid extends ManagedMesh {
	
	private float[][] noiseMap, noiseMapH, noiseMapM, noiseMapL;
	private Vector3f[][] normalVertMapH, normalVertMapM, normalVertMapL, vertMapH, vertMapM, vertMapL;
	private int gridSize = 50;
	private float gridQuadSize = 0.1f;
	private Vector2i gridPosition;
	private float gridDimensions;
	private Vector3f startVec;
	
	private String geomName;
	
	private Vector3f[] finVertices = new Vector3f[0];
	private Vector2f[] finTexCoord = new Vector2f[0];
	private int[] finIndexes = new int[0];
	private float[] finNormals = new float[0];
	
	private Vector3f[] finVerticesM = new Vector3f[0];
	private Vector2f[] finTexCoordM = new Vector2f[0];
	private int[] finIndexesM = new int[0];
	private float[] finNormalsM = new float[0];
	
	private Vector3f[] finVerticesL = new Vector3f[0];
	private Vector2f[] finTexCoordL = new Vector2f[0];
	private int[] finIndexesL = new int[0];
	private float[] finNormalsL = new float[0];
	
	// Texture info
	AssetManager assetManager;
	
	int textureCount = 4;
	Texture tex1, tex2, tex3, tex4, texA1, texA2, texA3, texA4;
	Material mat;
	
	/**
	 * Creates a new Terrain Grid
	 * @param gridSize  The number of vertices between x to x+size & z to z+size
	 * @param gridQuadSize  The actual float size of each quad in the grid
	 * @param gridPosition  int[] containing the x, y incremental position of the grid
	 * @param startVec  a Vector3f containing the actualy position of the grid
	 */
	public TerrainGrid(AssetManager assetManager, int gridSize, float gridQuadSize, Vector3f position) { //, float[][] atlasMap) {
		this.startVec = position;
		this.gridSize = gridSize;
		this.gridQuadSize = gridQuadSize;
		this.gridDimensions = ((gridSize-1)*gridQuadSize);
		this.assetManager = assetManager;
		
		this.gridPosition = new Vector2i(
			(int)FastMath.floor(position.x/gridDimensions),
			(int)FastMath.floor(position.z/gridDimensions)
		);
		this.geomName = String.valueOf(gridPosition.x) + "-" + String.valueOf(gridPosition.y);
		
		
		tex1 = assetManager.loadTexture("Textures/TerrainTiles/diffuse3.png");
		tex1.setMinFilter(MinFilter.BilinearNearestMipMap);
		tex1.setMagFilter(MagFilter.Bilinear);
		tex1.setWrap(WrapMode.Repeat);
		
		tex2 = assetManager.loadTexture("Textures/TerrainTiles/diffuse4.png");
		tex2.setMinFilter(MinFilter.BilinearNearestMipMap);
		tex2.setMagFilter(MagFilter.Bilinear);
		tex2.setWrap(WrapMode.Repeat);
		
		tex3 = assetManager.loadTexture("Textures/TerrainTiles/diffuse3.png");
		tex3.setMinFilter(MinFilter.BilinearNearestMipMap);
		tex3.setMagFilter(MagFilter.Bilinear);
		tex3.setWrap(WrapMode.Repeat);
		
		mat = new Material(assetManager, "MatDefs/TerrainLighting.j3md");
		mat.setBoolean("UseMaterialColors", true);
		mat.setBoolean("HighQuality", true);
		mat.setFloat("Shininess", .0f);
		mat.setColor("Ambient", ColorRGBA.White);
		mat.setColor("Diffuse", ColorRGBA.White);
		mat.getAdditionalRenderState().setFaceCullMode(FaceCullMode.Off);
		mat.setTexture("DiffuseMap1", tex1);
		mat.setFloat("DiffuseScale1", .1f);
		mat.setTexture("DiffuseMap2", tex2);
		mat.setFloat("DiffuseScale2", .15f);
		mat.setTexture("DiffuseMap3", tex3);
		mat.setFloat("DiffuseScale3", .2f);
		
		this.noiseMap = generateNoiseMap();
		// Full detail
		this.noiseMapH = spliceNoiseMap(this.noiseMap,1);
		this.normalVertMapH = getNormalsVertMap(this.noiseMapH, 1);
		this.vertMapH = extractVertMap(this.normalVertMapH);
		
		int mapSize = vertMapH.length+2;
		finVertices = new Vector3f[mapSize*mapSize];
		finTexCoord = new Vector2f[mapSize*mapSize];
		finNormals = new float[mapSize*mapSize*3];
		finIndexes = new int[(mapSize-1)*(mapSize-1)*6];
		
		buildTerrainGrid(vertMapH, normalVertMapH, 1, finVertices, finTexCoord, finNormals, finIndexes);
		
		this.noiseMapM = spliceNoiseMap(this.noiseMap,2);
		this.normalVertMapM = getNormalsVertMap(this.noiseMapM, 2);
		this.vertMapM = extractVertMap(this.normalVertMapM);
		
		mapSize = vertMapM.length+2;
		finVerticesM = new Vector3f[mapSize*mapSize];
		finTexCoordM = new Vector2f[mapSize*mapSize];
		finNormalsM = new float[mapSize*mapSize*3];
		finIndexesM = new int[(mapSize-1)*(mapSize-1)*6];
		
		buildTerrainGrid(vertMapM, normalVertMapM, 2, finVerticesM, finTexCoordM, finNormalsM, finIndexesM);
	
		this.noiseMapL = spliceNoiseMap(this.noiseMap,5);
		this.normalVertMapL = getNormalsVertMap(this.noiseMapL, 5);
		this.vertMapL = extractVertMap(this.normalVertMapL);
		
		mapSize = vertMapL.length+2;
		finVerticesL = new Vector3f[mapSize*mapSize];
		finTexCoordL = new Vector2f[mapSize*mapSize];
		finNormalsL = new float[mapSize*mapSize*3];
		finIndexesL = new int[(mapSize-1)*(mapSize-1)*6];
		
		buildTerrainGrid(vertMapL, normalVertMapL, 5, finVerticesL, finTexCoordL, finNormalsL, finIndexesL);
	}
	
	private void buildTerrainGrid(Vector3f[][] vMap, Vector3f[][] vNMap, float scale, Vector3f[] bVerts, Vector2f[] bTexCoords, float[] bNormals, int[] bIndexes) {
		int mapSize = vMap.length;
		
		int index = 0;
		int nIndex = 0;
		int cIndex = 0;
		
		for (int x = 0; x < mapSize; x++) {
			for (int y = 0; y < mapSize; y++) {
				// Vertex positions
				if (x == 0 || x == mapSize-1 || y == 0 || y == mapSize-1)
					bVerts[index] = vMap[x][y].subtract(0, gridQuadSize*scale, 0);
				else
					bVerts[index] = vMap[x][y];
				// Texture Coords
				float TCSize = 1.0f/(mapSize-3);
				bTexCoords[index] = new Vector2f(TCSize*(x-1),TCSize*(y-1));
				// Default normals
				bNormals[nIndex] = 0; nIndex++;
				bNormals[nIndex] = 1; nIndex++;
				bNormals[nIndex] = 0; nIndex++;
				index++;
			}
		}
		
		// Build faces
		generateFaces(bIndexes, mapSize);
		
		// Recalculate vertex normals
		recalculateNormals(bNormals, vNMap);
	}
	private float[][] generateNoiseMap() {
		int mapSize = gridSize+20;
		float[][] nMap = new float[mapSize][mapSize];
		int cX = (int)FastMath.floor(startVec.x/gridQuadSize)-10;
		int cY = (int)FastMath.floor(startVec.z/gridQuadSize)-10;
		
		int incX = 0;
		int incY = 0;
		for (int x = cX; x < cX + mapSize; x++) {
			for (int y = cY; y < cY + mapSize; y++) {
				nMap[incX][incY] = SimplexNoise3.simplex_noise(1, ((float)x+1000000)*0.002f, ((float)y+1000000)*0.002f, 1f)*820f;
				nMap[incX][incY] += SimplexNoise3.simplex_noise(1, ((float)x+1000000)*0.008f, ((float)y+1000000)*0.008f, 1f)*420f;
				nMap[incX][incY] += SimplexNoise3.simplex_noise(4, ((float)x+1000000)*0.0085f, ((float)y+1000000)*0.0073f, 1f)*15f;
				nMap[incX][incY] += SimplexNoise3.simplex_noise(3, ((float)x+1000000)*0.0073f, ((float)y+1000000)*0.0085f, 1f)*05f;
				if (nMap[incX][incY] > 500f) {
					nMap[incX][incY] += (nMap[incX][incY]-500f)*0.2f;
				}
				if (nMap[incX][incY] > 1000f) {
					nMap[incX][incY] += (nMap[incX][incY]-1000f)*0.25f;
				}
				if (nMap[incX][incY] > 1500f) {
					nMap[incX][incY] += (nMap[incX][incY]-1500f)*0.35f;
				}
				if (nMap[incX][incY] > 2000f) {
					nMap[incX][incY] += (nMap[incX][incY]-2000f)*0.4f;
				}
				if (nMap[incX][incY] > 2500f) {
					nMap[incX][incY] += (nMap[incX][incY]-2500f)*0.45f;
				}
				if (nMap[incX][incY] > 3000f) {
					nMap[incX][incY] += (nMap[incX][incY]-3000f)*0.5f;
				}
				nMap[incX][incY] += SimplexNoise3.simplex_noise(3, ((float)x+1000000)*0.035f, ((float)y+1000000)*0.035f, 1f)*20f;
				nMap[incX][incY] += SimplexNoise3.simplex_noise(4, ((float)x+1000000)*0.01f, ((float)y+1000000)*0.01f, 1f)*25f;
				
				nMap[incX][incY] -= 1500f;
				incY++;
			}
			incX++;
			incY = 0;
		}
		
		return nMap;
	}
	private float[][] spliceNoiseMap(float[][] nMap, int inc) {
		int innerBounds = 10;
		int borderPos = innerBounds-(inc*2);
		int len = nMap.length-(borderPos*2);
		if (inc > 1) len += (inc-1);
		len /= inc;
		float[][] rNMap = new float[len][len];
		int incX = 0, incY = 0;
		for (int x = borderPos; x < nMap.length-borderPos; x += inc) {
			for (int y = borderPos; y < nMap.length-borderPos; y += inc) {
				rNMap[incX][incY] = nMap[x][y];
				incY++;
			}
			incX++;
			incY = 0;
		}
		return rNMap;
	}
	private Vector3f[][] getNormalsVertMap(float[][] nMap, int scale) {
		float xGridQuadSize = gridQuadSize * scale;
		int mapSize = nMap.length;
		Vector3f[][] vMap = new Vector3f[mapSize][mapSize];
		int incX = 0, incY = 0;
		for (int x = 0; x < nMap.length; x++) {
			for (int y = 0; y < nMap.length; y++) {
				float height = nMap[x][y]/255*(gridQuadSize*10);
			//	vMap[incX][incY] = startVec.add(new Vector3f(x*xGridQuadSize, height, y*xGridQuadSize));
				vMap[incX][incY] = new Vector3f(x*xGridQuadSize, height, y*xGridQuadSize);
				if (scale == 2) vMap[incX][incY].subtractLocal(new Vector3f().set(xGridQuadSize, 0, xGridQuadSize));
				else if (scale == 5) vMap[incX][incY].subtractLocal(new Vector3f().set(gridQuadSize*(scale*1.6f), 0, gridQuadSize*(scale*1.6f)));
				incY++;
			}
			incX++;
			incY = 0;
		}
		return vMap;
	}
	private Vector3f[][] extractVertMap(Vector3f[][] normalsMap) {
		int mapSize = normalsMap.length-2;
		Vector3f[][] vMap = new Vector3f[mapSize][mapSize];
		for (int x = 1; x < normalsMap.length-1; x++) {
			for (int y = 1; y < normalsMap.length-1; y++) {
				vMap[x-1][y-1] = normalsMap[x][y];
			}
		}
		return vMap;
	}
	private void generateFaces(int[] buffer, int mapSize) {
		// Build faces
		int index = 0;
		for (int x = 0; x < mapSize-1; x++) {
			for (int y = 0; y < mapSize-1; y++) {
				//2, 0, 1, 1, 3, 2
				int ind = x*mapSize;
				int v1 = ind+y;
				int v2 = ind+y+1;
				int v3 = ind+y+mapSize;
				int v4 = v3+1;
				buffer[index] = v3;
				index++;
				buffer[index] = v1;
				index++;
				buffer[index] = v2;
				index++;
				buffer[index] = v2;
				index++;
				buffer[index] = v4;
				index++;
				buffer[index] = v3;
				index++;
			}
		}
	}
	private void recalculateNormals(float[] buffer, Vector3f[][] vMap) {
		int index = 0;
		
		int mapSize = vMap.length;
		
		Vector3f	v0 = new Vector3f(),
					v1 = new Vector3f(),
					v2 = new Vector3f(),
					v3 = new Vector3f(),
					v4 = new Vector3f(),
					v5 = new Vector3f(),
					v6 = new Vector3f(),
					v7 = new Vector3f(),
					v8 = new Vector3f(),
					r = new Vector3f();
		for (int x = 1; x < mapSize-1; x++) {
			for (int y = 1; y < mapSize-1; y++) {
				// Assume grid normals are facing upward
				v1.set(0,1,0);
				v2.set(0,1,0);
				v3.set(0,1,0);
				v4.set(0,1,0);
				v5.set(0,1,0);
				v6.set(0,1,0);
				v7.set(0,1,0);
				v8.set(0,1,0);
				
				// Set center vertex
				v0.set(vMap[x][y]);
				// Grab adjacent vertices in a clockwise fashoin
				v1.set(vMap[x-1][y-1]);
				v2.set(vMap[x-1][y]);
				v3.set(vMap[x-1][y+1]);
				v4.set(vMap[x][y+1]);
				v5.set(vMap[x+1][y+1]);
				v6.set(vMap[x+1][y]);
				v7.set(vMap[x+1][y-1]);
				v8.set(vMap[x][y-1]);
				
				// Substract the center vertex from each of the adjacent
				v1.subtractLocal(v0);
				v2.subtractLocal(v0);
				v3.subtractLocal(v0);
				v4.subtractLocal(v0);
				v5.subtractLocal(v0);
				v6.subtractLocal(v0);
				v7.subtractLocal(v0);
				v8.subtractLocal(v0);
				
				// Get the normalized cross product of each vertex
				// and it's clockwise adjacent vertex
				Vector3f r1 = v1.cross(v2).normalize();
				Vector3f r2 = v2.cross(v3).normalize();
				Vector3f r3 = v3.cross(v4).normalize();
				Vector3f r4 = v4.cross(v5).normalize();
				Vector3f r5 = v5.cross(v6).normalize();
				Vector3f r6 = v6.cross(v7).normalize();
				Vector3f r7 = v7.cross(v8).normalize();
				Vector3f r8 = v8.cross(v1).normalize();
				
				// Get a sum of the results
				r.set(r1);
				r.addLocal(r2);
				r.addLocal(r3);
				r.addLocal(r4);
				r.addLocal(r5);
				r.addLocal(r6);
				r.addLocal(r7);
				r.addLocal(r8);
				
				// Append it to the normals float buffer
				buffer[index] = r.x;
				index++;
				buffer[index] = r.y;
				index++;
				buffer[index] = r.z;
				index++;
			}
		}
	}
	public Material getMaterial() {
		return this.mat;
	}
	
	/**
	 * Updates the mesh buffers with the current level of detail specified
	 * @param lod
	 */
	public void buildMesh() {
		//Rebuild mesh buffers
		this.clearBuffer(Type.Position);
				this.setBuffer(Type.Position,	3, BufferUtils.createFloatBuffer(finVertices));
				this.clearBuffer(Type.TexCoord);
				this.setBuffer(Type.TexCoord,	2, BufferUtils.createFloatBuffer(finTexCoord));
				this.clearBuffer(Type.Index);
				this.setBuffer(Type.Index,		3, BufferUtils.createIntBuffer(finIndexes));
				this.clearBuffer(Type.Normal);
				this.setBuffer(Type.Normal,		3, BufferUtils.createFloatBuffer(finNormals));
		this.setCurrentLOD(LOD.LOD_1);
		this.updateBound();
	}
	
	@Override
	public void build() {
		throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public void updateLOD(LOD lod) {
		//Rebuild mesh buffers
		if (lod != this.getCurrentLOD()) {
			if (lod == LOD.LOD_3) {
				this.clearBuffer(Type.Position);
				this.setBuffer(Type.Position,	3, BufferUtils.createFloatBuffer(finVerticesL));
				this.clearBuffer(Type.TexCoord);
				this.setBuffer(Type.TexCoord,	2, BufferUtils.createFloatBuffer(finTexCoordL));
				this.clearBuffer(Type.Index);
				this.setBuffer(Type.Index,		3, BufferUtils.createIntBuffer(finIndexesL));
				this.clearBuffer(Type.Normal);
				this.setBuffer(Type.Normal,		3, BufferUtils.createFloatBuffer(finNormalsL));
			} else if (lod == LOD.LOD_2) {
				this.clearBuffer(Type.Position);
				this.setBuffer(Type.Position,	3, BufferUtils.createFloatBuffer(finVerticesM));
				this.clearBuffer(Type.TexCoord);
				this.setBuffer(Type.TexCoord,	2, BufferUtils.createFloatBuffer(finTexCoordM));
				this.clearBuffer(Type.Index);
				this.setBuffer(Type.Index,		3, BufferUtils.createIntBuffer(finIndexesM));
				this.clearBuffer(Type.Normal);
				this.setBuffer(Type.Normal,		3, BufferUtils.createFloatBuffer(finNormalsM));
			} else { 
				this.clearBuffer(Type.Position);
				this.setBuffer(Type.Position,	3, BufferUtils.createFloatBuffer(finVertices));
				this.clearBuffer(Type.TexCoord);
				this.setBuffer(Type.TexCoord,	2, BufferUtils.createFloatBuffer(finTexCoord));
				this.clearBuffer(Type.Index);
				this.setBuffer(Type.Index,		3, BufferUtils.createIntBuffer(finIndexes));
				this.clearBuffer(Type.Normal);
				this.setBuffer(Type.Normal,		3, BufferUtils.createFloatBuffer(finNormals));
				
			}
			this.setCurrentLOD(lod);
			this.updateBound();
		}
	}
	
	/**
	 * Returns a Vector3f containing the world position of the terrain grid
	 * @return startVec
	 */
	public Vector3f getStartVec() {
		return this.startVec;
	}
	
	/**
	 * Returns the name of the the terrain grid containing the terrain grid
	 * @return geomName
	 */
	public String getGeomName() {
		return this.geomName;
	}
	
	/**
	 * Returns a Vector2i containing the terrain grids incremental positions for x, z
	 * @return gridPosition
	 */
	public Vector2i getGridPosition() {
		return this.gridPosition;
	}
	
	public Vector2f getGridDimensions() {
		return new Vector2f(this.gridDimensions,this.gridDimensions);
	}
}
