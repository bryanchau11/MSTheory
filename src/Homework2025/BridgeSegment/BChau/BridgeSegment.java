package Homework2025.BridgeSegment.BChau;

import BridgeSegment.AbstractBridgeSystem.*;
import BridgeSegment.AbstractBridgeSystem;
import GenCol.*;
import simView.ViewableAtomic;
import genDevs.modeling.*;

public class BridgeSegment extends ViewableAtomic {
    protected DEVSQueue qWestToEast;
    protected DEVSQueue qEastToWest;
    protected entity currentCar;
    protected double travelTime = 10.0;
    protected int bridgeId;
    protected double lightRemaining;
    protected BridgeState currentLightState;
    protected double lightDuration;

    public BridgeSegment(String name) {
        // Default settings
        this(name, 1, AbstractBridgeSystem.BridgeState.WEST_TO_EAST, 100);
    }

    public BridgeSegment(String name, int bridgeId, BridgeState initialState, double duration) {
        // Initialize the DEVSAtomic
        super(name);
        addInport("westbound_in");
        addOutport("westbound_out");
        addInport("eastbound_in");
        addOutport("eastbound_out");
        this.bridgeId = bridgeId;
        this.currentLightState = initialState;
        this.lightDuration = duration;
        this.lightRemaining = duration;
        qWestToEast = new DEVSQueue();
        qEastToWest = new DEVSQueue();
    }

    public void initialize() {
        qWestToEast = new DEVSQueue();
        qEastToWest = new DEVSQueue();
        currentCar = null;
        holdIn("passive", lightRemaining);
    }

    public void deltint() {
        lightRemaining -= sigma;

        if (phaseIs("serving")) {
            currentCar = null;
            // just finished serving a car
            // Check if there are more cars waiting in the queue for the current green light
            // System.out.println("Finished serving a car on BridgeSegment" + bridgeId + "
            // at time " + getSimulationTime());
            if (currentLightState == BridgeState.WEST_TO_EAST && !qWestToEast.isEmpty()
                    && lightRemaining >= travelTime) {
                currentCar = (entity) qWestToEast.remove();
                holdIn("serving", travelTime);
            } else if (currentLightState == BridgeState.EAST_TO_WEST && !qEastToWest.isEmpty()
                    && lightRemaining >= travelTime) {
                currentCar = (entity) qEastToWest.remove();
                holdIn("serving", travelTime);
            } else {
                // no more cars to serve, remain passive until next light change
                holdIn("passive", lightRemaining);
            }

        }
        // check if it's time to switch the light, switch when it is below 0
        if (lightRemaining <= 0) {
            lightSwitch();
        }
    }

    public void lightSwitch() {
        // If there are still time left on the light, and no cars to serve, remain
        if (lightRemaining > 0 && currentCar == null) {
            holdIn("passive", lightRemaining);
        }
        // System.out.println("Light switch on BridgeSegment" + bridgeId + " at time "
        // + getSimulationTime());
        currentLightState = (currentLightState == BridgeState.WEST_TO_EAST)
                ? BridgeState.EAST_TO_WEST
                : BridgeState.WEST_TO_EAST;
        lightRemaining = lightDuration; // Reset light timer
        // After switching light, check if there are cars waiting in the queue for the
        if (currentLightState == BridgeState.WEST_TO_EAST && !qWestToEast.isEmpty() && lightRemaining >= travelTime) {
            currentCar = (entity) qWestToEast.remove();
            holdIn("serving", travelTime);
        } else if (currentLightState == BridgeState.EAST_TO_WEST &&
                !qEastToWest.isEmpty() && lightRemaining >= travelTime) {
            currentCar = (entity) qEastToWest.remove();
            holdIn("serving", travelTime);
        }
        // If no cars to serve, remain passive until next light change
        else {
            holdIn("passive", lightDuration);
        }
    }

    public void deltext(double e, message x) {
        Continue(e);
        lightRemaining -= e;
        // Process incoming cars
        for (int i = 0; i < x.getLength(); i++) {
            if (messageOnPort(x, "westbound_in", i)) {
                entity car = (entity) x.getValOnPort("westbound_in", i);
                qWestToEast.add(car);
            }
            if (messageOnPort(x, "eastbound_in", i)) {
                entity car = (entity) x.getValOnPort("eastbound_in", i);
                qEastToWest.add(car);
            }
        }
        // System.out.println("BridgeSegment" + bridgeId + " received " + x.getLength()
        // + " cars at time "
        // + getSimulationTime());
        // System.out.println("light remaining: " + lightRemaining);
        // System.out.println("West Queue: " + qWestToEast.size() + " East Queue: " +
        // qEastToWest.size());
        // System.out.println("");

        // If not currently serving a car, try to serve the next car in the queue for
        // the current green light
        if (currentCar == null) {
            if (currentLightState == BridgeState.WEST_TO_EAST && !qWestToEast.isEmpty()
                    && lightRemaining >= travelTime) {
                currentCar = (entity) qWestToEast.remove();
                holdIn("serving", travelTime);
            } else if (currentLightState == BridgeState.EAST_TO_WEST &&
                    !qEastToWest.isEmpty() && lightRemaining >= travelTime) {
                currentCar = (entity) qEastToWest.remove();
                holdIn("serving", travelTime);

            } else { // no car to serve
                holdIn("passive", lightRemaining);
            }
        }

    }

    public message out() {
        message m = new message();
        // If currently serving a car, output it to the appropriate direction
        if (phaseIs("serving") && currentCar != null) {
            if (currentLightState == BridgeState.WEST_TO_EAST) {
                // Westbound
                content con = makeContent("eastbound_out", currentCar);
                m.add(con);
            } else {
                // Eastbound
                content con = makeContent("westbound_out", currentCar);
                m.add(con);
            }
            // String carName = currentCar.getName();
            // String timeStr = String.format("%.6f", getSimulationTime());
            // String line = carName + " exits from BridgeSegment" + bridgeId + " at " +
            // timeStr;
            // System.out.println(line);
            // System.out.println("light remaining: " + lightRemaining);
            // System.out.println("");

        }

        return m;

    }

    public void deltcon(double e, message x) {
        deltint();
        deltext(0, x);
    }

}