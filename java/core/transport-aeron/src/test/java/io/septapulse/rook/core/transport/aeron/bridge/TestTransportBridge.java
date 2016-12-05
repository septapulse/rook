package io.septapulse.rook.core.transport.aeron.bridge;

import io.aeron.driver.MediaDriver;
import io.septapulse.rook.api.RID;
import io.septapulse.rook.api.transport.AbstractTransportTest;
import io.septapulse.rook.api.transport.ControllableTransport;
import io.septapulse.rook.api.transport.bridge.TransportBridge;
import io.septapulse.rook.core.transport.aeron.AeronTransport;
import io.septapulse.rook.core.transport.aeron.AeronTransportConfig;
import io.septapulse.rook.core.transport.aeron.EmbeddedAeronDriver;

public class TestTransportBridge extends AbstractTransportTest {

	private static final RID BRIDGE_SERVICE_ID = RID.create(0);
	
	private EmbeddedAeronDriver embedded1;
	private AeronTransportConfig config1;
	private EmbeddedAeronDriver embedded2;
	private AeronTransportConfig config2;
	private EmbeddedAeronDriver embedded3;
	private AeronTransportConfig config3;
	
	private ControllableTransport bridgeTransport1;
	private ControllableTransport bridgeTransport2;
	private ControllableTransport bridgeTransport3;
	
	@Override
	protected void beforeStartup() throws Exception {
		embedded1 = new EmbeddedAeronDriver(); 
		MediaDriver driver1 = embedded1.start(); 
		config1 = new AeronTransportConfig()
				.setAeronDirectoryName(driver1.aeronDirectoryName());
		embedded2 = new EmbeddedAeronDriver(); 
		MediaDriver driver2 = embedded2.start(); 
		config2 = new AeronTransportConfig()
				.setAeronDirectoryName(driver2.aeronDirectoryName());
		embedded3 = new EmbeddedAeronDriver(); 
		MediaDriver driver3 = embedded3.start(); 
		config3 = new AeronTransportConfig()
				.setAeronDirectoryName(driver3.aeronDirectoryName());
		
		bridgeTransport1 = new AeronTransport(config1);
		bridgeTransport1.setServiceId(BRIDGE_SERVICE_ID);
		bridgeTransport1.setRespondToProbes(false);
		bridgeTransport1.start();
		bridgeTransport2 = new AeronTransport(config2);
		bridgeTransport2.setServiceId(BRIDGE_SERVICE_ID);
		bridgeTransport2.setRespondToProbes(false);
		bridgeTransport2.start();
		bridgeTransport3 = new AeronTransport(config3);
		bridgeTransport3.setServiceId(BRIDGE_SERVICE_ID);
		bridgeTransport3.setRespondToProbes(false);
		bridgeTransport3.start();
	}
	
	@Override
	protected void afterStartup() throws Exception {
		new TransportBridge(bridgeTransport1, bridgeTransport2).start();
		new TransportBridge(bridgeTransport1, bridgeTransport3).start();
		Thread.sleep(500);
		q1.clear();
		q2.clear();
		q3.clear();
	}
	
	@Override
	protected ControllableTransport createTransport1() throws Exception {
		return new AeronTransport(config1);
	}
	
	@Override
	protected ControllableTransport createTransport2() throws Exception {
		return new AeronTransport(config2);
	}
	
	@Override
	protected ControllableTransport createTransport3() throws Exception {
		return new AeronTransport(config3);
	}
	
	@Override
	protected void beforeTeardown() {
		
	}
	
	@Override
	protected void afterTeardown() {
		bridgeTransport1.shutdown();
		bridgeTransport2.shutdown();
		bridgeTransport3.shutdown();
		embedded1.stop();
		embedded2.stop();
		embedded3.stop();
	}
	
	@Override
	public void testUnicastIncognitoConsume() throws Exception {
		// incognito consumption is not guarenteed to work in a distributed environment
	}

}
