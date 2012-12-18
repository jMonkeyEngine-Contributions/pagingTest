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
import com.jme3.renderer.RenderManager;
import com.jme3.scene.Node;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import paging.core.DelegatorListener;
import paging.core.PagingManager;
import pagingTests.supportFiles.TerrainGridDelegator;

/**
 * test
 * @author t0neg0d
 */
public class TestMeshDelegatorTilePhysicsLOD extends SimpleApplication implements DelegatorListener, InputListener {
	
	ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(2);
	private PagingManager pm;
	BulletAppState bulletAppState;
	VideoRecorderAppState vrAppState;
	boolean left = false, right = false, up = false, down = false; 
	Vector3f walkDirection = Vector3f.ZERO;
	float walkSpeed = 1.0f;
	CharacterControl character;
	Node cameraNode;
	ChaseCamera chaseCam;
	
	boolean enabled = false;
	
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
		
		cam.setFrustumFar(36000f);
		float aspect = (float)cam.getWidth() / (float)cam.getHeight();
		cam.setFrustumPerspective( 45f, aspect, 0.1f, cam.getFrustumFar() );
		cam.update();
		cam.setLocation(cam.getLocation().add(200f, 0f, 200f));
		cam.lookAt(Vector3f.UNIT_Z, Vector3f.UNIT_Y);
		cam.setLocation(cam.getLocation().add(0f, 5f, 0f));
		flyCam.setMoveSpeed(100);
		
		setupKeys();
		createCharacter();
		
		pm = new PagingManager(exec, cam);
		pm.addPhysicsSupport(bulletAppState.getPhysicsSpace());
		
		
		int tSize = 12;
		int gSize = 41;
		float gQSize = 1.5f;
		
		TerrainGridDelegator terrainDelegator = new TerrainGridDelegator(assetManager, gSize, gQSize);
		terrainDelegator.setTile(((float)(gSize-1))*gQSize, tSize, true);
		terrainDelegator.setManagePhysics(true);
		terrainDelegator.setManageLOD(true);
		terrainDelegator.addLOD(PagingManager.LOD.LOD_1, 0f);
		terrainDelegator.addLOD(PagingManager.LOD.LOD_2, 180f);
		terrainDelegator.addLOD(PagingManager.LOD.LOD_3, 300f);
		
		terrainDelegator.addListener(this);
		
		pm.registerDelegator("Terrain", terrainDelegator, rootNode, 60);
		
		AmbientLight al = new AmbientLight();
		al.setColor(new ColorRGBA(1f, 1f, 1f, 1f));
		rootNode.addLight(al);
		
		ColorRGBA dayLight = new ColorRGBA(1f,1f,1f,1f);
		ColorRGBA nightLight = new ColorRGBA(.4f,.4f,.4f,1f);
		
		DirectionalLight sun = new DirectionalLight();
		sun.setDirection(new Vector3f(-.2f,-1f,-.2f).normalizeLocal());
		sun.setColor(dayLight);
		rootNode.addLight(sun);
	}
	
	private void createCharacter() {
		CapsuleCollisionShape capsule = new CapsuleCollisionShape(3f, 4f);
		character = new CharacterControl(capsule, 5.01f);
		cameraNode = new Node("Camera Node");
		
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
		
		cameraNode.addControl(character);
		character.setPhysicsLocation(new Vector3f(-5f, 4f, -5f));
		rootNode.attachChild(cameraNode);
	}
	
	private PhysicsSpace getPhysicsSpace() {
		return bulletAppState.getPhysicsSpace();
	}
	
	private void setupKeys() {
		inputManager.addMapping("CharLeft", new KeyTrigger(KeyInput.KEY_A));
		inputManager.addMapping("CharRight", new KeyTrigger(KeyInput.KEY_D));
		inputManager.addMapping("CharUp", new KeyTrigger(KeyInput.KEY_W));
		inputManager.addMapping("CharDown", new KeyTrigger(KeyInput.KEY_S));
		inputManager.addMapping("CharSpace", new KeyTrigger(KeyInput.KEY_RETURN));
		inputManager.addMapping("CharShoot", new KeyTrigger(KeyInput.KEY_SPACE));
		inputManager.addListener(this, "CharLeft");
		inputManager.addListener(this, "CharRight");
		inputManager.addListener(this, "CharUp");
		inputManager.addListener(this, "CharDown");
		inputManager.addListener(this, "CharSpace");
		inputManager.addListener(this, "CharShoot");
	}

    @Override
    public void simpleUpdate(float tpf) {
        if (enabled) {
			System.out.println("enabled!");
			Vector3f camDir = cam.getDirection().clone().multLocal(walkSpeed);
			Vector3f camLeft = cam.getLeft().clone().multLocal(walkSpeed);
			camDir.y = 0;
			camLeft.y = 0;
			walkDirection.set(0, 0, 0);

			if (left)	{ walkDirection.addLocal(camLeft); }
			if (right)	{ walkDirection.addLocal(camLeft.negate()); }
			if (up)		{ walkDirection.addLocal(camDir); }
			if (down)	{ walkDirection.addLocal(camDir.negate()); }
			character.setWalkDirection(walkDirection); 
		}
    }

	public void onAction(String binding, boolean value, float tpf) {
		if (binding.equals("CharLeft")) {
			if (value)	{ left = true; }
			else		{ left = false; }
		} else if (binding.equals("CharRight")) {
			if (value)	{ right = true; }
			else		{ right = false; }
		} else if (binding.equals("CharUp")) {
			if (value)	{ up = true; }
			else		{ up = false; }
		} else if (binding.equals("CharDown")) {
			if (value)	{ down = true; }
			else		{ down = false; }
		}
	}

    @Override
    public void simpleRender(RenderManager rm) {
        //TODO: add render code
    }
	
	@Override
	public void stop() {
		exec.shutdown();
		while (!exec.isTerminating()) {  }
		super.stop();
	}

	public void onAddToScene(Node node) {
		if (!enabled) {
			List<PhysicsRayTestResult> list = getPhysicsSpace().rayTest(cameraNode.getLocalTranslation(),cameraNode.getLocalTranslation().subtract(0f,200f,0f));
			PhysicsCollisionObject result = null;
			if (list.size() > 0) {
				result = list.get(0).getCollisionObject();
			}
			if (result != null) {
				getPhysicsSpace().add(character);
				enabled = true;
			}
		}
	}

	public void onRemoveFromScene(Node node) {
	//	throw new UnsupportedOperationException("Not supported yet.");
	}
}
