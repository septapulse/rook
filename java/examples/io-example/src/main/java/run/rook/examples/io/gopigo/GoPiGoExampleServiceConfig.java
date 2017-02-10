package run.rook.examples.io.gopigo;

import run.rook.api.config.Configurable;

public class GoPiGoExampleServiceConfig {
	@Configurable(comment="GoPiGo Service ID", defaultValue="IO")
	public String goPiGoServiceId = "IO";
	@Configurable(comment="Ultrasonic Distance to trigger excape sequence", defaultValue="25")
	public int escapeDistance = 10;
}
