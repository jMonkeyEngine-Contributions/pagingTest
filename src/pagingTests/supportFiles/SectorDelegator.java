/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pagingTests.supportFiles;

import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import com.jme3.scene.Geometry;
import com.jme3.scene.Spatial;
import com.jme3.scene.control.Control;
import java.io.IOException;
import paging.core.ManagedNodeDelegator;

/**
 *
 * @author t0neg0d
 */
public class SectorDelegator extends ManagedNodeDelegator {

	public SectorDelegator() {
		
	}
	
	@Override
	public void delegatorUpdate(float tpf) {
	//	throw new UnsupportedOperationException("Not supported yet.");
	}

	@Override
	public Geometry getGeometry() {
		return new Geometry("geom");
	//	throw new UnsupportedOperationException("Not supported yet.");
	}

	public Control cloneForSpatial(Spatial spatial) {
		return this;
	//	throw new UnsupportedOperationException("Not supported yet.");
	}

	public void setSpatial(Spatial spatial) {
		this.spatial = spatial;
	//	throw new UnsupportedOperationException("Not supported yet.");
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
