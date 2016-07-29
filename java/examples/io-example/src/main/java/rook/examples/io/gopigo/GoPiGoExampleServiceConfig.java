package rook.examples.io.gopigo;

import rook.api.config.Configurable;

public class GoPiGoExampleServiceConfig {
	@Configurable(comment="Ultrasonic Distance to trigger excape sequence", defaultValue="25")
	public int escapeDistance = 10;
}
