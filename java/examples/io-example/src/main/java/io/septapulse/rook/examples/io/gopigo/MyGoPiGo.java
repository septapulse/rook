package io.septapulse.rook.examples.io.gopigo;

import java.util.LinkedHashMap;
import java.util.Map;

import io.septapulse.rook.api.RID;
import io.septapulse.rook.core.io.proxy.IOProxy;
import io.septapulse.rook.core.io.proxy.message.IOValue;

public class MyGoPiGo {

	// inputs
	private final RID idSpeedLeft = RID.create("SPEED_LEFT").immutable();
    private final RID idSpeedRight = RID.create("SPEED_RIGHT").immutable();
    private final RID idUltrasonicDistance = RID.create("ULTRASONIC").immutable();
    private final RID idVoltage = RID.create("VOLTAGE").immutable();
    // outputs
    private final RID idLedLeft = RID.create("LED_LEFT").immutable();
    private final RID idLedRight = RID.create("LED_RIGHT").immutable();
    private final RID idMotorLeft = RID.create("MOTOR_LEFT").immutable();
    private final RID idMotorRight = RID.create("MOTOR_RIGHT").immutable();
    private final RID idServoPosition = RID.create("SERVO").immutable();
    private final RID idServoPower = RID.create("SERVO_POWER").immutable();
    
    private volatile int speedLeft;
    private volatile int speedRight;
    private volatile int distance = Integer.MAX_VALUE;
    private volatile double voltage = 12;
    
    private final IOProxy io;
    
    public MyGoPiGo(IOProxy ioProxy) {
    	this.io = ioProxy;
    	io.inputs().addConsumer(this::handleInput);
    }
    
    private void handleInput(RID id, IOValue v) {
    	if(idSpeedLeft.equals(id)) {
    		speedLeft = v.getValueAsInt();
    	} else if(idSpeedRight.equals(id)) {
    		speedRight = v.getValueAsInt();
    	} else if(idUltrasonicDistance.equals(id)) {
    		distance = v.getValueAsInt();
    	} else if(idVoltage.equals(id)) {
    		voltage = v.getValueAsDouble();
    	}
    }
    
    public int getSpeedLeft() {
		return speedLeft;
	}
    
    public int getSpeedRight() {
		return speedRight;
	}
    
    public int getDistance() {
		return distance;
	}
    
    public double getVoltage() {
		return voltage;
	}
    
    public void setLedLeft(boolean value) {
    	io.outputs().setOutput(idLedLeft, new IOValue(value));
    }
    
	public void setLedRight(boolean value) {
		io.outputs().setOutput(idLedRight, new IOValue(value));
    }
	
    public void setMotors(int left, int right) {
    	if(left < -255 || left > 255)
    		throw new IllegalArgumentException("left=" + left + " not in range [-255,255]");
    	if(right < -255 || right > 255)
    		throw new IllegalArgumentException("right=" + right + " not in range [-255,255]");
    	Map<RID, IOValue> m = new LinkedHashMap<>();
    	m.put(idMotorLeft, new IOValue(left));
    	m.put(idMotorRight, new IOValue(right));
    	io.outputs().setOutputs(m);
    }
    
    public void setServoPower(boolean value) {
    	io.outputs().setOutput(idServoPower, new IOValue(value));
    }
    
    public void setServoPosition(int value) {
		if(value < 0 || value > 255)
    		throw new IllegalArgumentException(value + " not in range [0,255]");
		io.outputs().setOutput(idServoPosition, new IOValue(value));
	}

}
