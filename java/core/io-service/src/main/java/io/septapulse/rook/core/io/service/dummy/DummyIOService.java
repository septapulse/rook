package io.septapulse.rook.core.io.service.dummy;

import io.septapulse.rook.api.RID;
import io.septapulse.rook.api.config.Configurable;
import io.septapulse.rook.api.exception.InitException;
import io.septapulse.rook.core.io.service.IOService;
import io.septapulse.rook.core.io.service.dummy.DummyIOServiceConfig.Entry;

/**
 * Used for testing {@link IOService}
 * 
 * @author Eric Thill
 *
 */
public class DummyIOService extends IOService {

	private final DummyIOServiceConfig config;
	
	@Configurable
	public DummyIOService(DummyIOServiceConfig config) throws InitException {
		super(config);
		this.config = config;
	}

	@Override
	public void onInit() throws InitException {
		if(config.getInputs() != null) {
			for(Entry input : config.getInputs()) {
				RID id = RID.create(input.getId()).immutable();
				ioManager.addInput(id, new DummyIOInput(id, input.getType()));
			}
		}
		if(config.getOutputs() != null) {
			for(Entry output : config.getOutputs()) {
				RID id = RID.create(output.getId()).immutable();
				ioManager.addOutput(id, new DummyIOOutput(id, output.getType()), null);
			}
		}
	}
	
	@Override
	public void onShutdown() {
		
	}

}
