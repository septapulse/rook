package run.rook.examples.io.gopigo;

import run.rook.api.config.Configurable;

public class GoPiGoExampleConfig {
	@Configurable(comment="GoPiGo Service ID", defaultValue="IO")
	public String goPiGoServiceId = "IO";
	@Configurable(comment="Ultrasonic Distance to trigger excape sequence", defaultValue="10")
	public int escapeDistance = 10;
	@Configurable(comment="Ultrasonic Distance to trigger avoid", defaultValue="50")
	public int avoidDistance = 50;
}
