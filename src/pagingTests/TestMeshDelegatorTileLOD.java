package pagingTests;

import com.jme3.app.SimpleApplication;
import com.jme3.app.state.VideoRecorderAppState;
import com.jme3.bullet.BulletAppState;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.AmbientLight;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import paging.core.PagingManager;
import pagingTests.supportFiles.TerrainGridDelegator;

/**
 * test
 * @author t0neg0d
 */
public class TestMeshDelegatorTileLOD extends SimpleApplication {
	
	ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(2);
	private PagingManager pm;
	BulletAppState bulletAppState;
	VideoRecorderAppState vrAppState;
	
    public static void main(String[] args) {
        TestMeshDelegatorTileLOD app = new TestMeshDelegatorTileLOD();
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
		vrAppState.setQuality(0.5f);
		
		cam.setFrustumFar(36000f);
		float aspect = (float)cam.getWidth() / (float)cam.getHeight();
		cam.setFrustumPerspective( 45f, aspect, 0.1f, cam.getFrustumFar() );
		cam.update();
	//	cam.setLocation(cam.getLocation().add(100f, 100f, 100f));
		cam.lookAt(Vector3f.UNIT_Z.negate(), Vector3f.UNIT_Y);
		cam.setLocation(cam.getLocation().add(0f, 5f, 0f));
		flyCam.setMoveSpeed(50);
		
		pm = new PagingManager(exec, cam);
		pm.addPhysicsSupport(bulletAppState.getPhysicsSpace());
		
		
		int tSize = 12;
		int gSize = 41;
		float gQSize = 1.5f;
		
		TerrainGridDelegator terrainDelegator = new TerrainGridDelegator(assetManager, gSize, gQSize);
		terrainDelegator.setTile(((float)(gSize-1))*gQSize, tSize, true);
		terrainDelegator.setManagePhysics(false);
		terrainDelegator.setManageLOD(true);
		terrainDelegator.addLOD(PagingManager.LOD.LOD_1, 0f);
		terrainDelegator.addLOD(PagingManager.LOD.LOD_2, 180f);
		terrainDelegator.addLOD(PagingManager.LOD.LOD_3, 300f);
		
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
		
		inputManager.addMapping("F9",new KeyTrigger(KeyInput.KEY_F9)); // Enable/disable video recorder
		inputManager.addListener(eventListener, "F9");
    }
	
	private ActionListener eventListener = new ActionListener() {
		public void onAction(String binding, boolean value, float tpf) {
			if (binding.equals("F9") && value) {
				if (!stateManager.hasState(vrAppState)) {
					stateManager.attach(vrAppState);
				} else {
					stateManager.detach(vrAppState);
				}
			}
		}
	};

    @Override
    public void simpleUpdate(float tpf) {
        //TODO: add update code
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
}
