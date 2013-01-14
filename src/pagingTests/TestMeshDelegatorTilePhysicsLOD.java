package pagingTests;

import com.jme3.app.SimpleApplication;
import com.jme3.app.state.VideoRecorderAppState;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.bullet.collision.PhysicsCollisionEvent;
import com.jme3.bullet.collision.PhysicsCollisionListener;
import com.jme3.bullet.collision.PhysicsCollisionObject;
import com.jme3.bullet.collision.PhysicsRayTestResult;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.input.ChaseCamera;
import com.jme3.input.KeyInput;
import com.jme3.input.MouseInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.InputListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.input.controls.MouseButtonTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.post.FilterPostProcessor;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import paging.core.DelegatorListener;
import paging.core.PagingManager;
import pagingTests.supportFiles.FogFilter;
import pagingTests.supportFiles.SkyDome;
import pagingTests.supportFiles.TerrainGridDelegator;
import pagingTests.supportFiles.TerrainSimpleGrassDelegator;
import pagingTests.supportFiles.TerrainSimpleTreeDelegator;

/**
 * test
 * @author t0neg0d
 */
public class TestMeshDelegatorTilePhysicsLOD extends SimpleApplication implements DelegatorListener, ActionListener {
	
	ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(2);
	private PagingManager pm;
	
	BulletAppState bulletAppState;
	CharacterControl character;
	Node cameraNode;
	ChaseCamera chaseCam;
	
	SkyDome skyDome;
	
	VideoRecorderAppState vrAppState;
	
	boolean left = false, right = false, up = false, down = false; 
	Vector3f walkDirection = Vector3f.ZERO;
	float walkSpeed = .6f;
	
	boolean enableCharacter = false;
	
    public static void main(String[] args) {
        TestMeshDelegatorTilePhysicsLOD app = new TestMeshDelegatorTilePhysicsLOD();
	//	app.settings = new AppSettings(true);
	//	app.settings.setVSync(false);
        app.start();
    }

    @Override
    public void simpleInitApp() {
        bulletAppState = new BulletAppState();
		bulletAppState.setThreadingType(BulletAppState.ThreadingType.PARALLEL);
		stateManager.attach(bulletAppState);
		
		vrAppState = new VideoRecorderAppState();
		vrAppState.setQuality(0.35f);
		
		cam.setFrustumFar(36000f);
		float aspect = (float)cam.getWidth() / (float)cam.getHeight();
		cam.setFrustumPerspective( 45f, aspect, 0.1f, cam.getFrustumFar() );
		cam.update();
		cam.setLocation(cam.getLocation().add(200f, 0f, 200f));
		cam.lookAt(Vector3f.UNIT_Z, Vector3f.UNIT_Y);
		cam.setLocation(cam.getLocation().add(0f, 15f, 0f));
		flyCam.setMoveSpeed(100);
		
		pm = new PagingManager(exec, cam);
		pm.addPhysicsSupport(bulletAppState.getPhysicsSpace());
		
		rootNode.addControl(pm);
		
		int tSize = 6;
		int gSize = 81;
		float gQSize = 10f;
		
		TerrainGridDelegator terrainDelegator = new TerrainGridDelegator(assetManager, gSize, gQSize);
		terrainDelegator.setTile(((float)(gSize-1))*gQSize, tSize, true);
		terrainDelegator.setManagePhysics(true);
		terrainDelegator.setManageLOD(true);
		terrainDelegator.addLOD(PagingManager.LOD.LOD_1, 0f);
		terrainDelegator.addLOD(PagingManager.LOD.LOD_2, 1600f);
		terrainDelegator.addLOD(PagingManager.LOD.LOD_3, 2400f);
		
		terrainDelegator.addListener(this);
		
		pm.registerDelegator("Terrain", terrainDelegator, rootNode, 5);
		/*
		TerrainSimpleGrassDelegator terrainGrassDelegator = new TerrainSimpleGrassDelegator(assetManager, ((float)(gSize-1))*gQSize, 1.25f, 3f, gQSize*1.5f, 1);
		terrainGrassDelegator.setManagePhysics(false);
		terrainGrassDelegator.setManageLOD(false);
	//	terrainGrassDelegator.setManageObjectFading(true);
		terrainGrassDelegator.setRenderBucket(RenderQueue.Bucket.Transparent);
		terrainDelegator.addDependantDelegator("Grass", terrainGrassDelegator);
		*/
		tSize = 4;
		gSize = 41;
		gQSize = 2.5f;
		
		TerrainSimpleTreeDelegator terrainTreeDelegator = new TerrainSimpleTreeDelegator(assetManager, ((float)(gSize-1))*gQSize);
		terrainTreeDelegator.setTile(((float)(gSize-1))*gQSize, tSize, true);
		terrainTreeDelegator.setManagePhysics(false);
		terrainTreeDelegator.setManageLOD(false);
		terrainTreeDelegator.setRenderBucket(RenderQueue.Bucket.Transparent);
		pm.registerDelegator("Tree", terrainTreeDelegator, rootNode, 5);
		
		createCharacter();
		setupKeys();
		
		AmbientLight al = new AmbientLight();
		al.setColor(new ColorRGBA(1f, 1f, 1f, 1f));
		rootNode.addLight(al);
		
		ColorRGBA dayLight = new ColorRGBA(1f,1f,1f,1f);
		ColorRGBA nightLight = new ColorRGBA(.4f,.4f,.4f,1f);
		
		DirectionalLight sun = new DirectionalLight();
		sun.setDirection(new Vector3f(0f,-.25f,0f).normalizeLocal());
		sun.setColor(dayLight);
		rootNode.addLight(sun);
		
		ColorRGBA fogColor = new ColorRGBA(0.7f, 0.7f, 0.7f, 0.6f);
		ColorRGBA fogNightColor = new ColorRGBA(0.35f, 0.35f, 0.45f, 0.6f);
		/*
		// FPP
		FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
		
		FogFilter fog = new FogFilter();
		fog.setFogMode(FogFilter.FOG_MODE.EXP2_DISTANCE_TO_INFINITY);
		fog.setFogColor(fogNightColor);
		fog.setFogStartDistance(600f);
	//	fog.setFogEndDistance(7000f);
		fog.setFogDensity(0.15f);
		fog.setExcludeSky(true);
	//	fpp.addFilter(fog);
		
	//	viewPort.addProcessor(fpp);
	//	viewPort.setBackgroundColor(fogNightColor);
		
		skyDome = new SkyDome(assetManager, cam,
				"Models/Skies/SkyDome.j3o",
				"Textures/Skies/SkyNight_L.png",
				"Textures/Skies/Sun_L.png",
				"Textures/Skies/Moon_L.png",
				"Textures/Skies/Clouds_L.png",
				"Textures/Skies/Fog_Alpha.png");
		Node sky = new Node();
		sky.setQueueBucket(RenderQueue.Bucket.Sky);
	//	skyDome.setSpatial(sky);
		sky.addControl(skyDome);
		sky.setCullHint(Spatial.CullHint.Never);
		skyDome.setFogFilter(fog, viewPort);
		skyDome.setFogColor(fogColor);
		skyDome.setFogNightColor(fogNightColor);
		skyDome.setControlFog(true);
		skyDome.setSun(sun);
		skyDome.setSunDayLight(dayLight);
		skyDome.setSunNightLight(nightLight);
		skyDome.setControlSun(true);
		skyDome.initializeCalendar(1, 5, 24, 7, 4, 12);
		skyDome.setUseCalendar(false);
	//	skyDome.setEnabled(true);
	//	rootNode.attachChild(sky);
		*/
	}
	
	private void createCharacter() {
		CapsuleCollisionShape capsule = new CapsuleCollisionShape(2f, 4f);
		character = new CharacterControl(capsule, 0.1f);
		
	//	flyCam.setEnabled(false);
		cameraNode = new Node("Camera Node");
		//model.setLocalScale(0.5f);
		
		cameraNode.addControl(character);
		character.setPhysicsLocation(new Vector3f(-140, 13, -10));
		rootNode.attachChild(cameraNode);
		
		
		flyCam.setEnabled(false);
		chaseCam = new ChaseCamera(cam, cameraNode, inputManager);

		chaseCam.setDefaultDistance(.005f);
		chaseCam.setMaxDistance(40f);
		chaseCam.setDefaultHorizontalRotation(0f);
		chaseCam.setDefaultVerticalRotation(0f);
		chaseCam.setZoomSensitivity(1f);
		cam.setFrustumFar(36000f);
		float aspect = (float)cam.getWidth() / (float)cam.getHeight();
		cam.setFrustumPerspective( 45f, aspect, 0.1f, cam.getFrustumFar() );
		chaseCam.setUpVector(Vector3f.UNIT_Y);
		chaseCam.setMinDistance(.005f);
		chaseCam.setToggleRotationTrigger(new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
		chaseCam.setInvertVerticalAxis(true);
	}
	
	private PhysicsSpace getPhysicsSpace() {
		return bulletAppState.getPhysicsSpace();
	}
	
	private void setupKeys() {
		inputManager.addMapping("CharLeft", new KeyTrigger(KeyInput.KEY_A));
		inputManager.addMapping("CharRight", new KeyTrigger(KeyInput.KEY_D));
		inputManager.addMapping("CharUp", new KeyTrigger(KeyInput.KEY_W));
		inputManager.addMapping("CharDown", new KeyTrigger(KeyInput.KEY_S));
		inputManager.addMapping("F9", new KeyTrigger(KeyInput.KEY_F9));
		inputManager.addListener(this, "CharLeft");
		inputManager.addListener(this, "CharRight");
		inputManager.addListener(this, "CharUp");
		inputManager.addListener(this, "CharDown");
		inputManager.addListener(this, "F9");
	}

    @Override
    public void simpleUpdate(float tpf) {
        if (enableCharacter) {
			Vector3f camDir = cam.getDirection().clone().multLocal(walkSpeed);
			Vector3f camLeft = cam.getLeft().clone().multLocal(walkSpeed);
			camDir.y = 0;
			camLeft.y = 0;
			walkDirection.set(0, 0, 0);
			
			if (left)	{ walkDirection.addLocal(camLeft); }
			if (right)	{ walkDirection.addLocal(camLeft.negate()); }
			if (up)		{ walkDirection.addLocal(camDir); }
			if (down)	{ walkDirection.addLocal(camDir.negate()); }
			character.setViewDirection(cam.getDirection().setX(0).setZ(0));
			character.setWalkDirection(walkDirection); 
		}
    }

	public void onAction(String binding, boolean value, float tpf) {
		if (binding.equals("CharLeft")) {
			if (value)	{ left = true; } else { left = false; }
		} else if (binding.equals("CharRight")) {
			if (value)	{ right = true; } else { right = false; }
		} else if (binding.equals("CharUp")) {
			if (value)	{ up = true; } else { up = false; }
		} else if (binding.equals("CharDown")) {
			if (value)	{ down = true; } else { down = false; }
		} else if (binding.equals("F9")) {
			if (!value) {
				if (stateManager.hasState(vrAppState))	{ 
					System.out.println("Stopping video recorder");
					stateManager.detach(vrAppState);
				} else {
					System.out.println("Starting video recorder");
					stateManager.attach(vrAppState);
				}
			}
		}
	}

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }
	
	@Override
	public void stop() {
		super.stop();
		exec.shutdown();
	}

	public void onAddToScene(Node node) {
		if (!enableCharacter) {
			List<PhysicsRayTestResult> list = getPhysicsSpace().rayTest(cameraNode.getLocalTranslation(),cameraNode.getLocalTranslation().subtract(0f,200f,0f));
			PhysicsCollisionObject result = null;
			if (list.size() > 0) {
				result = list.get(0).getCollisionObject();
			}
			if (result != null) {
				getPhysicsSpace().add(character);
				enableCharacter = true;
			}
		}
	}

	public void onRemoveFromScene(Node node) {
	//	throw new UnsupportedOperationException("Not supported yet.");
	}
}
