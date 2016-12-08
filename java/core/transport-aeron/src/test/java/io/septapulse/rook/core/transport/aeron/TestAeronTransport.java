package io.septapulse.rook.core.transport.aeron;

import io.aeron.driver.MediaDriver;
import io.septapulse.rook.api.transport.ControllableTransport;
import io.septapulse.rook.core.transport.aeron.AeronTransport;
import io.septapulse.rook.core.transport.aeron.AeronTransportConfig;
import io.septapulse.rook.test.transport.AbstractTransportTest;

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
