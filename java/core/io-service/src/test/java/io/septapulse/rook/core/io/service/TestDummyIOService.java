package io.septapulse.rook.core.io.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import io.septapulse.rook.api.RID;
import io.septapulse.rook.api.transport.ControllableTransport;
import io.septapulse.rook.core.io.proxy.IOProxy;
import io.septapulse.rook.core.io.proxy.IOValueConsumer;
import io.septapulse.rook.core.io.proxy.message.Cap;
import io.septapulse.rook.core.io.proxy.message.CapType;
import io.septapulse.rook.core.io.proxy.message.DataType;
import io.septapulse.rook.core.io.proxy.message.IOValue;
import io.septapulse.rook.core.io.proxy.message.PrimitiveType;
import io.septapulse.rook.core.io.service.dummy.DummyIOService;
import io.septapulse.rook.core.io.service.dummy.DummyIOServiceConfig;
import io.septapulse.rook.core.io.service.dummy.DummyIOServiceConfig.Entry;
import io.septapulse.rook.core.transport.tcp.TcpRouter;
import io.septapulse.rook.core.transport.tcp.TcpRouterConfig;
import io.septapulse.rook.core.transport.tcp.TcpTransport;
import io.septapulse.rook.core.transport.tcp.TcpTransportConfig;

public class TestDummyIOService {

	private TcpRouter router;
	private DummyIOService service;
	private ControllableTransport proxyTransport;
	private IOProxy proxy;
	
	@Before
	public void init() throws Exception {
		router = new TcpRouter(new TcpRouterConfig());
		router.start();
		Thread.sleep(100);
		DummyIOServiceConfig config = new DummyIOServiceConfig();
		config.setBroadcastInterval(100);
		config.setInputs(new ArrayList<>());
		config.getInputs().add(new Entry().setId("I1").setType(DataType.BOOLEAN));
		config.getInputs().add(new Entry().setId("I2").setType(DataType.INTEGER));
		config.setOutputs(new ArrayList<>());
		config.getOutputs().add(new Entry().setId("O1").setType(DataType.BOOLEAN));
		config.getOutputs().add(new Entry().setId("O2").setType(DataType.INTEGER));
		service = new DummyIOService(config);
		TcpTransport serviceTransport = new TcpTransport(new TcpTransportConfig());
		service.setTransport(serviceTransport);
		serviceTransport.setServiceId(RID.create("IO"));
		serviceTransport.start();
		service.init();
		proxyTransport = new TcpTransport(new TcpTransportConfig());
		proxyTransport.setServiceId(RID.create("PROXY"));
		proxyTransport.start();
		proxy = new IOProxy(proxyTransport, RID.create("IO"));
	}
	
	@After
	public void shutdown() throws Exception {
		if(service != null)
			service.shutdown();
		Thread.sleep(100);
		if(router != null)
			router.stop();
		if(proxy != null)
			proxy.stop();
		Thread.sleep(100);
	}
	
	@Test
	public void testCapsProbe() throws Exception {
		Map<RID, List<Cap>> capsMap = IOProxy.probe(proxyTransport, 100);
		Assert.assertEquals(1, capsMap.size());
		List<Cap> caps = capsMap.get(RID.create("IO"));
		Assert.assertEquals(4, caps.size());
		Assert.assertEquals("I1", caps.get(0).getId().toString());
		Assert.assertEquals(DataType.BOOLEAN, caps.get(0).getDataType());
		Assert.assertEquals(CapType.INPUT, caps.get(0).getCapType());
		Assert.assertEquals("I2", caps.get(1).getId().toString());
		Assert.assertEquals(DataType.INTEGER, caps.get(1).getDataType());
		Assert.assertEquals(CapType.INPUT, caps.get(1).getCapType());
		Assert.assertEquals("O1", caps.get(2).getId().toString());
		Assert.assertEquals(DataType.BOOLEAN, caps.get(2).getDataType());
		Assert.assertEquals(CapType.OUTPUT, caps.get(2).getCapType());
		Assert.assertEquals("O2", caps.get(3).getId().toString());
		Assert.assertEquals(DataType.INTEGER, caps.get(3).getDataType());
		Assert.assertEquals(CapType.OUTPUT, caps.get(3).getCapType());
	}
	
	@Test
	public void testInputBroadcast() throws Exception {
		Set<RID> inputIds = Collections.synchronizedSet(new HashSet<>());
		proxy.inputs().addConsumer(new IOValueConsumer() {
			@Override
			public void onValue(RID id, IOValue value) {
				inputIds.add(id);
			}
		});
		Thread.sleep(250);
		Assert.assertEquals(2, inputIds.size());
	}
	
	@Test
	public void testOutputs() throws Exception {
		Map<RID, IOValue> outputValues = Collections.synchronizedMap(new HashMap<>());
		proxy.outputs().addConsumer(new IOValueConsumer() {
			@Override
			public void onValue(RID id, IOValue value) {
				outputValues.put(id.immutable(), value.copy());
			}
		});
		
		proxy.outputs().setOutput(RID.create("O1"), new IOValue(true));
		Thread.sleep(250);
		Assert.assertEquals(1, outputValues.size());
		Assert.assertNotNull(outputValues.get(RID.create("O1")));
		Assert.assertEquals(true, outputValues.get(RID.create("O1")).getValueAsBoolean());
		
		outputValues.clear();
		proxy.outputs().setOutput(RID.create("O2"), new IOValue(10));
		Thread.sleep(250);
		Assert.assertEquals(2, outputValues.size());
		Assert.assertNotNull(outputValues.get(RID.create("O1")));
		Assert.assertNotNull(outputValues.get(RID.create("O2")));
		Assert.assertEquals(true, outputValues.get(RID.create("O1")).getValueAsBoolean());
		Assert.assertEquals(10, outputValues.get(RID.create("O2")).getValueAsInt());
	}
	
	@Test
	public void testInputs() throws Exception {
		Map<RID, IOValue> inputValues = Collections.synchronizedMap(new HashMap<>());
		proxy.inputs().addConsumer(new IOValueConsumer() {
			@Override
			public void onValue(RID id, IOValue value) {
				inputValues.put(id.immutable(), value.copy());
			}
		});

		Thread.sleep(250);
		Assert.assertEquals(2, inputValues.size());
		Assert.assertNotNull(inputValues.get(RID.create("I1")));
		Assert.assertNotNull(inputValues.get(RID.create("I2")));
		Assert.assertEquals(PrimitiveType.BOOLEAN, inputValues.get(RID.create("I1")).getType());
		Assert.assertEquals(PrimitiveType.TWOS_COMPLIMENT_INT, inputValues.get(RID.create("I2")).getType());
	}
	
}
