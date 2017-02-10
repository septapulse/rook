package run.rook.core.io.service.dummy;

import run.rook.api.RID;
import run.rook.api.config.Configurable;
import run.rook.api.exception.InitException;
import run.rook.core.io.service.IOService;
import run.rook.core.io.service.dummy.DummyIOServiceConfig.Entry;

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
		super(config.getBroadcastInterval());
		this.config = config;
	}

	@Override
	public void onInit() throws InitException {
		if(config.getInputs() != null) {
			for(Entry input : config.getInputs()) {
				RID id = RID.create(input.getId()).immutable();
				addInput(new DummyIOInput(id, input.getType()));
			}
		}
		if(config.getOutputs() != null) {
			for(Entry output : config.getOutputs()) {
				RID id = RID.create(output.getId()).immutable();
				addOutput(new DummyIOOutput(id, output.getType()));
			}
		}
	}
	
	@Override
	public void onShutdown() {
		
	}

}
