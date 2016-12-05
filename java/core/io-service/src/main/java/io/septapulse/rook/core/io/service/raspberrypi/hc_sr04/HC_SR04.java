package io.septapulse.rook.core.io.service.raspberrypi.hc_sr04;

import java.io.IOException;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;

import io.septapulse.rook.api.RID;
import io.septapulse.rook.api.exception.InitException;
import io.septapulse.rook.core.io.proxy.message.Cap;
import io.septapulse.rook.core.io.proxy.message.CapType;
import io.septapulse.rook.core.io.proxy.message.DataType;
import io.septapulse.rook.core.io.proxy.message.IOValue;
import io.septapulse.rook.core.io.service.IOInput;
import io.septapulse.rook.core.io.service.IOManager;
import io.septapulse.rook.core.io.service.raspberrypi.RaspberryPiDevice;

/**
 * An HC_SR04 ultrasonic device using GPIO pins
 * reference implementation: https://github.com/OlivierLD/raspberry-pi4j-samples/blob/master/RangeSensor/src/rangesensor/HC_SR04.java
 * 
 * @author Eric Thill
 *
 */
public class HC_SR04 implements IOInput, RaspberryPiDevice {

	private static final long SOUND_ONE_METER_NANOS = 2938669;
	private static final long SOUND_ONE_METER_NANOS_ROUND_TRIP = SOUND_ONE_METER_NANOS * 2;
	private static final double SPEED_OF_SOUND_IN_CM = 34029;
	private static final double HALF_SPEED_OF_SOUND_IN_CM = SPEED_OF_SOUND_IN_CM / 2;
	private static final long BILLION = (long) 10E9;
	private static final int TEN_MICRO_SEC_IN_NANOS = 10 * 1000;

	private final IOValue value = new IOValue();
	private final Cap cap;
	private final RID id;
	private final Pin trigPin;
	private final Pin echoPin;
	private final double minDistance;
	private final double maxDistance;
	private final boolean convertToInteger;
	private GpioPinDigitalOutput trigOutput;
	private GpioPinDigitalInput echoInput;
	private double lastDistance = 2.0;
	
	public HC_SR04(HC_SR04Config config) {
		this(config.trigPin, config.echoPin, RID.create(config.id), 
				config.convertToInteger, config.minDistance, config.maxDinstance);
	}
	
	public HC_SR04(String trigPin, String echoPin, RID id, boolean convertToInteger,
			double minDistance, double maxDinstance) {
		this.trigPin = RaspiPin.getPinByName(trigPin);
		this.echoPin = RaspiPin.getPinByName(echoPin);
		this.convertToInteger = convertToInteger;
		this.minDistance = maxDinstance;
		this.maxDistance = maxDinstance;
		this.id = id.immutable();
		this.cap = new Cap().setCapType(CapType.INPUT)
				.setDataType(convertToInteger ? DataType.INTEGER : DataType.FLOAT)
				.setMinValue(minDistance)
				.setMaxValue(maxDistance)
				.setIncrement(convertToInteger ? 1.0 : Double.MIN_VALUE);
	}
	
	@Override
	public void init(IOManager ioManager) throws InitException {
		init();
		ioManager.addInput(id, this);
	}
	
	@Override
	public void init() {
		GpioController gpio = GpioFactory.getInstance();
		trigOutput = gpio.provisionDigitalOutputPin(trigPin, "Trig", PinState.LOW);
		echoInput = gpio.provisionDigitalInputPin(echoPin, "Echo");
	}

	@Override
	public IOValue read() throws IOException {
		if(convertToInteger) {
			value.setValue((int)measure());
		} else {
			value.setValue(measure());
		}
		return value;
	}
	
	@Override
	public RID id() {
		return id;
	}

	@Override
	public Cap cap() {
		return cap;
	}

	@Override
	public void shutdown() {
		
	}
	
	private double measure() {
		trigOutput.high();
		try {
			Thread.sleep(0, TEN_MICRO_SEC_IN_NANOS);
		} catch (InterruptedException ex) {
			return lastDistance;
		}
		trigOutput.low();

		// Wait for the signal to return
		while (echoInput.isLow());
		long start = System.nanoTime();
		while (echoInput.isHigh() && System.nanoTime()-start < SOUND_ONE_METER_NANOS_ROUND_TRIP);
		long end = System.nanoTime();

		trigOutput.low();
		
		if (end > start)
		{
			// return distance
			double pulseDuration = (double) (end - start) / (double) BILLION; // in
			double distance = pulseDuration * HALF_SPEED_OF_SOUND_IN_CM;
			if(distance < minDistance) {
				distance = minDistance;
			} else if(distance > maxDistance) {
				distance = maxDistance;
			}
			lastDistance = distance;
			return distance;
		} else {
			// cannot take measurement
			return lastDistance;
		}
	}

	@Override
	public String toString() {
		return "HC_SR04 [id=" + id + ", trigPin=" + trigPin + ", echoPin="
				+ echoPin + ", minDistance=" + minDistance + ", maxDistance=" + maxDistance + ", convertToInteger="
				+ convertToInteger + "]";
	}
	
}