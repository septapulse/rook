package rook.api.transport.aeron;

import io.aeron.driver.MediaDriver;
import rook.api.transport.AbstractTransportTest;
import rook.api.transport.ControllableTransport;

public class TestAeronTransport extends AbstractTransportTest {

	private EmbeddedAeronDriver embedded;
	private AeronTransportConfig config;
	
	@Override
	protected void beforeStartup() throws Exception {
		embedded = new EmbeddedAeronDriver(); 
		MediaDriver driver = embedded.start(); 
		config = new AeronTransportConfig()
				.setAeronDirectoryName(driver.aeronDirectoryName());
	}
	
	@Override
	protected void afterStartup() throws Exception {
		
	}
	
	@Override
	protected ControllableTransport createTransport1() throws Exception {
		return new AeronTransport(config);
	}
	
	@Override
	protected ControllableTransport createTransport2() throws Exception {
		return new AeronTransport(config);
	}
	
	@Override
	protected ControllableTransport createTransport3() throws Exception {
		return new AeronTransport(config);
	}
	
	@Override
	protected void beforeTeardown() {
		
	}
	
	@Override
	protected void afterTeardown() {
		embedded.stop();
	}

}
