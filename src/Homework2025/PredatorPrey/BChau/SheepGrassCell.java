package Homework2025.PredatorPrey.BChau;

import twoDCellSpace.*;
import simView.*;
import genDevs.modeling.*;
import genDevs.plots.newCellGridView;
import GenCol.*;
import java.awt.*;
import java.util.*;

public class SheepGrassCell extends TwoDimCell {

    newCellGridView cellGridView;

    public static class CellState {
        public static final String EMPTY = "EMPTY";
        public static final String GRASS = "GRASS";
        public static final String SHEEP = "SHEEP";
    }

    // Current state of this cell
    private String currentState = CellState.EMPTY;

    // Time tracking for grass growth
    private double grassReproduceT = GlobalRef.grassReproduceT;

    // Time tracking for sheep actions
    private double sheepLifeT = GlobalRef.sheepLifeT;
    private double sheepReproduceT = GlobalRef.sheepReproduceT;
    private double sheepMoveT = GlobalRef.sheepMoveT;

    // Global reference to access parameters
    private GlobalRef globalRef;

    // Random number generator
    private Random rand;

    // Message list for output
    protected message messageList;

    public SheepGrassCell() {
        this(0, 0);
    }

    public SheepGrassCell(int x, int y) {
        super(new Pair(new Integer(x), new Integer(y)));

        globalRef = GlobalRef.getInstance();
        rand = new Random();
        initializePorts();
    }

    private void initializePorts() {
        // Add input ports for all 8 directions
        addInport("inN"); // North
        addInport("inNE"); // Northeast
        addInport("inE"); // East
        addInport("inSE"); // Southeast
        addInport("inS"); // South
        addInport("inSW"); // Southwest
        addInport("inW"); // West
        addInport("inNW"); // Northwest

        // Add output ports for all 8 directions
        addOutport("outN");
        addOutport("outNE");
        addOutport("outE");
        addOutport("outSE");
        addOutport("outS");
        addOutport("outSW");
        addOutport("outW");
        addOutport("outNW");
    }

    public void initialize() {
        cellGridView = ((SheepGrassCellSpace) getParent()).plot.getCellGridView();
        // Start with empty state by default

        // Update global state tracking
        if (globalRef != null && GlobalRef.state != null) {
            GlobalRef.state[getXcoord()][getYcoord()] = currentState;
            GlobalRef.cell_ref[getXcoord()][getYcoord()] = this;

            // Draw based on current state
            System.out.println("Cell (" + getXcoord() + "," + getYcoord() + ") initial state: " + currentState);
            if (currentState.equals(CellState.GRASS)) {
                cellGridView.drawCellToScale(x_pos, y_pos, Color.GREEN);
                System.out.println("Initialized GRASS cell at (" + getXcoord() + "," + getYcoord() + ")");
            } else {
                cellGridView.drawCellToScale(x_pos, y_pos, Color.WHITE);
            }
        }

        // Initialize message list
        messageList = new message();

        // Schedule next event based on current state

        scheduleNextEvent();
    }

    private void scheduleNextEvent() {
        if (currentState.equals(CellState.GRASS)) {
            // Grass needs to check for reproduction
            grassReproduceT -= GlobalRef.grassReproduceT;
            if (grassReproduceT <= 0) {
                grassReproduceT = GlobalRef.grassReproduceT; // Reset cycle
            }
            holdIn("GROW", grassReproduceT);
        } else {
            // Empty cells are passive (no events scheduled)
            holdIn(CellState.EMPTY, INFINITY);
        }
    }

    public void deltint() {
        // Clear previous messages
        messageList = new message();

        if (phaseIs("GROW") && currentState.equals(CellState.GRASS)) {
            // Time to try reproducing grass
            // grassReproduceT += sigma; // Update growth time
            grassReproduceT -= sigma;
            if (grassReproduceT <= 0) {
                grassReproduceT = GlobalRef.grassReproduceT; // Reset cycle
                System.out.println("Grass at (" + getXcoord() + "," + getYcoord() + ") trying to reproduce");
                tryToReproduceGrass();
            }

        }
        // Schedule next event
        scheduleNextEvent();
    }

    private void tryToReproduceGrass() {
        Integer randomNeighborDirection = globalRef.getRandomNeighbor(getXcoord(), getYcoord(), CellState.EMPTY);
        System.out.println("Random Neighbor Direction: " + randomNeighborDirection);
        if (randomNeighborDirection != null) {
            String outputPort = getOutportForDirection(randomNeighborDirection);
            content con = makeContent(outputPort, new entity("reproduce_grass"));
            messageList.add(con);

            System.out.println("Grass at (" + getXcoord() + "," + getYcoord() +
                    ") reproducing to direction " + randomNeighborDirection);
        }
    }

    private String getOutportForDirection(int direction) {
        switch (direction) {
            case 0:
                return "outN"; // North
            case 1:
                return "outNE"; // Northeast
            case 2:
                return "outE"; // East
            case 3:
                return "outSE"; // Southeast
            case 4:
                return "outS"; // South
            case 5:
                return "outSW"; // Southwest
            case 6:
                return "outW"; // West
            case 7:
                return "outNW"; // Northwest
            default:
                return "outN";
        }
    }

    public void deltext(double e, message x) {
        Continue(e);

        if (currentState.equals(CellState.GRASS)) {
            grassReproduceT -= e;
        }

        String[] inputPorts = { "inN", "inNE", "inE", "inSE", "inS", "inSW", "inW", "inNW" };

        for (String portName : inputPorts) {
            if (messageOnPort(x, portName, 0)) {
                String messageContent = x.getValOnPort(portName, 0).toString();

                if (messageContent.equals("reproduce_grass") && currentState.equals(CellState.EMPTY)) {
                    currentState = CellState.GRASS;
                    grassReproduceT = GlobalRef.grassReproduceT;
                    GlobalRef.state[getXcoord()][getYcoord()] = currentState;

                    // Update visualization for new grass
                    if (cellGridView != null) {
                        cellGridView.drawCellToScale(x_pos, y_pos, Color.GREEN);
                    }

                    System.out.println("Cell (" + getXcoord() + "," + getYcoord() +
                            ") became GRASS due to reproduction");

                    scheduleNextEvent();
                }
            }
        }
    }

    public message out() {
        return messageList;
    }

    public void setInitialState(String state) {
        currentState = state;
        if (globalRef != null && GlobalRef.state != null) {
            GlobalRef.state[getXcoord()][getYcoord()] = state;

            // Update visualization when state is set
            if (cellGridView != null) {
                if (state.equals(CellState.GRASS)) {
                    cellGridView.drawCellToScale(x_pos, y_pos, Color.GREEN);
                } else {
                    cellGridView.drawCellToScale(x_pos, y_pos, Color.WHITE);
                }
            }
        }
        // check state of (10,10)
        System.out.println("Check state of (10,10): " + GlobalRef.state[10][10]);
        System.out.println("Set initial state of (" + getXcoord() + "," + getYcoord() + ") to " + state);
    }

    public Color getBackgroundColor() {
        if (currentState.equals(CellState.EMPTY)) {
            return Color.WHITE;
        } else if (currentState.equals(CellState.GRASS)) {
            return Color.GREEN;
        } else {
            return Color.RED; // For SHEEP or any other state
        }
    }

    @Override
    public String getTooltipText() {
        return super.getTooltipText() +
                "\nState: " + currentState +
                (currentState.equals(CellState.GRASS) ? "\nGrowth time: " + String.format("%.2f", grassReproduceT)
                        : "");
    }
}