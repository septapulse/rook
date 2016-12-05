package io.septapulse.rook.examples.io.dummy;

import io.septapulse.rook.api.config.Configurable;

public class DummyIOExampleServiceConfig {
	@Configurable(comment="Frequence of output update (in millis)", defaultValue="1000")
	public long outputInterval = 1000;
	@Configurable(comment="ID of output to intermittently change between 0 and 1")
	public String outputId;
}
