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
            } else if (currentState.equals(CellState.SHEEP)) {
                cellGridView.drawCellToScale(x_pos, y_pos, Color.RED);
                System.out.println("Initialized SHEEP cell at (" + getXcoord() + "," + getYcoord() + ")");
            } else {
                cellGridView.drawCellToScale(x_pos, y_pos, Color.WHITE);
            }
        }

        // Initialize message list
        messageList = new message();

        // Schedule next event based on current state

        scheduleNext();
    }

    private void scheduleNext() {
        if (currentState.equals(CellState.GRASS)) {
            // Grass reproduction cycle
            holdIn("GROW", grassReproduceT);

        } else if (currentState.equals(CellState.SHEEP)) {
            // Check if sheep should die FIRST (before any other action)
            if (sheepLifeT <= 0) {
                holdIn("DIE", 0); // Die immediately
            } else {
                // Schedule next action based on timers
                double nextEventTime = Math.min(sheepMoveT, sheepReproduceT);
                nextEventTime = Math.min(nextEventTime, sheepLifeT);

                if (nextEventTime == sheepMoveT) {
                    holdIn("MOVE", sheepMoveT);
                } else if (nextEventTime == sheepReproduceT) {
                    holdIn("REPRODUCE", sheepReproduceT);
                } else {
                    holdIn("CHECK_LIFE", sheepLifeT);
                }
            }
        } else {
            // Empty cells are passive
            holdIn(CellState.EMPTY, INFINITY);
        }
    }

    // public void deltint() {
    // // Clear previous messages
    // messageList = new message();

    // if (phaseIs("GROW") && currentState.equals(CellState.GRASS)) {
    // // Time to try reproducing grass
    // grassReproduceT -= sigma;
    // if (grassReproduceT <= 0) {
    // grassReproduceT = GlobalRef.grassReproduceT; // Reset cycle
    // System.out.println("Grass at (" + getXcoord() + "," + getYcoord() + ") trying
    // to reproduce");
    // tryToReproduceGrass();
    // }
    // } else if (phaseIs("MOVE") && currentState.equals(CellState.SHEEP)) {
    // // Time for sheep to move
    // sheepMoveT -= sigma;
    // if (sheepMoveT <= 0) {
    // sheepMoveT = GlobalRef.sheepMoveT; // Reset cycle
    // System.out.println("Sheep at (" + getXcoord() + "," + getYcoord() + ") trying
    // to move");
    // tryToMoveSheep();
    // }
    // }
    // // Schedule next event
    // scheduleNext();
    // }

    // private void tryToMoveSheep() {
    // // First try to find grass neighbors (preferred)
    // Integer grassNeighborDirection = globalRef.getRandomNeighbor(getXcoord(),
    // getYcoord(), CellState.GRASS);

    // if (grassNeighborDirection != null) {
    // // Found grass, move there and eat it
    // String outputPort = getOutportForDirection(grassNeighborDirection);
    // content con = makeContent(outputPort, new entity("move_sheep_eat"));
    // messageList.add(con);

    // System.out.println("Sheep at (" + getXcoord() + "," + getYcoord() +
    // ") moving to eat grass in direction " + grassNeighborDirection);

    // // This sheep will move out, so become empty
    // currentState = CellState.EMPTY;
    // GlobalRef.state[getXcoord()][getYcoord()] = currentState;

    // // Update visualization
    // if (cellGridView != null) {
    // cellGridView.drawCellToScale(x_pos, y_pos, Color.WHITE);
    // }

    // } else {
    // // No grass found, try to move to empty cell
    // Integer emptyNeighborDirection = globalRef.getRandomNeighbor(getXcoord(),
    // getYcoord(), CellState.EMPTY);

    // if (emptyNeighborDirection != null) {
    // // Found empty cell, move there
    // String outputPort = getOutportForDirection(emptyNeighborDirection);
    // content con = makeContent(outputPort, new entity("move_sheep"));
    // messageList.add(con);

    // System.out.println("Sheep at (" + getXcoord() + "," + getYcoord() +
    // ") moving to empty cell in direction " + emptyNeighborDirection);

    // // This sheep will move out, so become empty
    // currentState = CellState.EMPTY;
    // GlobalRef.state[getXcoord()][getYcoord()] = currentState;

    // // Update visualization
    // if (cellGridView != null) {
    // cellGridView.drawCellToScale(x_pos, y_pos, Color.WHITE);
    // }

    // } else {
    // System.out.println("Sheep at (" + getXcoord() + "," + getYcoord() +
    // ") cannot move - no available neighbors");
    // }
    // }
    // }

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

        String[] inputPorts = { "inN", "inNE", "inE", "inSE", "inS", "inSW", "inW", "inNW" };

        // system print to check phase
        System.out.println("Cell (" + getXcoord() + "," + getYcoord() + ") in phase: " + phase);
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

                    scheduleNext();

                } else if (messageContent.equals("move_sheep_eat") && currentState.equals(CellState.GRASS)) {
                    // Sheep is moving here and eating the grass
                    currentState = CellState.SHEEP;
                    sheepMoveT = GlobalRef.sheepMoveT;
                    GlobalRef.state[getXcoord()][getYcoord()] = currentState;

                    // Update visualization for sheep
                    if (cellGridView != null) {
                        cellGridView.drawCellToScale(x_pos, y_pos, Color.RED);
                    }

                    System.out.println("Sheep moved to (" + getXcoord() + "," + getYcoord() +
                            ") and ate grass, life timer reset");

                    scheduleNext();

                } else if (messageContent.equals("move_sheep") && currentState.equals(CellState.EMPTY)) {
                    // Sheep is moving here to empty cell
                    currentState = CellState.SHEEP;
                    sheepMoveT = GlobalRef.sheepMoveT;
                    GlobalRef.state[getXcoord()][getYcoord()] = currentState;

                    // Update visualization for sheep
                    if (cellGridView != null) {
                        cellGridView.drawCellToScale(x_pos, y_pos, Color.RED);
                    }

                    System.out.println("Sheep moved to (" + getXcoord() + "," + getYcoord() + ")");

                    scheduleNext();
                } else if (messageContent.equals("reproduce_sheep") && currentState.equals(CellState.EMPTY)) {
                    // New sheep is born here
                    currentState = CellState.SHEEP;
                    sheepMoveT = GlobalRef.sheepMoveT;
                    sheepLifeT = GlobalRef.sheepLifeT;
                    sheepReproduceT = GlobalRef.sheepReproduceT;
                    GlobalRef.state[getXcoord()][getYcoord()] = currentState;

                    // Update visualization for new sheep
                    if (cellGridView != null) {
                        cellGridView.drawCellToScale(x_pos, y_pos, Color.RED);
                    }

                    System.out.println("New sheep born at (" + getXcoord() + "," + getYcoord() + ")");

                    scheduleNext();
                }
            }
        }
    }

    // ...existing code...
    // pending move chosen in out(), applied in deltint()
    private Integer pendingMoveDirection = null;
    private String pendingMoveAction = null; // "move_sheep" or "move_sheep_eat"
    // ...existing code...

    private void tryToMoveSheep() {
        pendingMoveDirection = null;
        pendingMoveAction = null;
        Integer grassNeighborDirection = globalRef.getRandomNeighbor(getXcoord(), getYcoord(), CellState.GRASS);
        if (grassNeighborDirection != null) {
            // Found grass, move there
            pendingMoveDirection = grassNeighborDirection;
            pendingMoveAction = "move_sheep_eat";
            System.out.println("Sheep at (" + getXcoord() + "," + getYcoord() + ") will move to eat grass in direction "
                    + grassNeighborDirection);
            return;
        }

        Integer emptyNeighborDirection = globalRef.getRandomNeighbor(getXcoord(), getYcoord(), CellState.EMPTY);
        if (emptyNeighborDirection != null) {
            pendingMoveDirection = emptyNeighborDirection;
            pendingMoveAction = "move_sheep";
            System.out.println("Sheep at (" + getXcoord() + "," + getYcoord()
                    + ") will move to empty cell in direction " + emptyNeighborDirection);
        } else {
            System.out
                    .println("Sheep at (" + getXcoord() + "," + getYcoord() + ") cannot move - no available neighbors");
        }
    }

    public message out() {
        // build messageList here for the current phase so simulator sends it before
        // deltint()
        messageList = new message(); // reset
        if (phaseIs("MOVE") && currentState.equals(CellState.SHEEP)) {
            // choose move (do NOT change currentState here
            tryToMoveSheep();

            if (pendingMoveDirection != null && pendingMoveAction != null) {
                String outputPort = getOutportForDirection(pendingMoveDirection);
                content con = makeContent(outputPort, new entity(pendingMoveAction));
                messageList.add(con);
                System.out.println("Cell (" + getXcoord() + "," + getYcoord() + ") outgoing move: " + pendingMoveAction
                        + " dir=" + pendingMoveDirection);
            }
        } else if (phaseIs("GROW") && currentState.equals(CellState.GRASS)) {
            // Grass reproduction messages are generated in deltint() when it's time
            // to reproduce, so nothing to do here.
            tryToReproduceGrass();
        } else if (phaseIs("REPRODUCE") && currentState.equals(CellState.SHEEP)) {
            tryToReproduceSheep();
        }

        System.out.println(
                "Cell (" + getXcoord() + "," + getYcoord() + ") outputting messages,  messageList: " + messageList);
        return messageList;
    }

    public void deltint() {
        messageList = new message();

        if (phaseIs("GROW") && currentState.equals(CellState.GRASS)) {
            // Grass reproduction logic
            tryToReproduceGrass();
            grassReproduceT = GlobalRef.grassReproduceT; // Reset timer

        } else if (phaseIs("MOVE") && currentState.equals(CellState.SHEEP)) {
            // Decrement all sheep timers by the time that passed (sigma)
            sheepLifeT -= sigma;
            sheepReproduceT -= sigma;

            // Handle movement
            if (pendingMoveAction != null && pendingMoveAction.equals("move_sheep_eat")) {
                // Reset life timer when eating grass
                sheepLifeT = GlobalRef.sheepLifeT;
                System.out.println("Sheep at (" + getXcoord() + "," + getYcoord() + ") reset life timer by eating");
            }

            if (pendingMoveAction != null && pendingMoveDirection != null) {
                // This sheep moves out, become empty
                currentState = CellState.EMPTY;
                GlobalRef.state[getXcoord()][getYcoord()] = currentState;
                if (cellGridView != null) {
                    cellGridView.drawCellToScale(x_pos, y_pos, Color.WHITE);
                }
                System.out.println("Sheep departed from (" + getXcoord() + "," + getYcoord() + ")");
            }

            // Reset pending move
            pendingMoveAction = null;
            pendingMoveDirection = null;

        } else if (phaseIs("REPRODUCE") && currentState.equals(CellState.SHEEP)) {
            // Sheep reproduction logic
            sheepReproduceT = GlobalRef.sheepReproduceT; // Reset reproduction timer
            sheepLifeT -= sigma; // Decrement life timer during reproduction
            tryToReproduceSheep();

        } else if (phaseIs("CHECK_LIFE") && currentState.equals(CellState.SHEEP)) {
            // Life timer expired - sheep dies
            sheepLifeT = 0;

        } else if (phaseIs("DIE") && currentState.equals(CellState.SHEEP)) {
            // Sheep dies
            currentState = CellState.EMPTY;
            GlobalRef.state[getXcoord()][getYcoord()] = currentState;
            if (cellGridView != null) {
                cellGridView.drawCellToScale(x_pos, y_pos, Color.WHITE);
            }
            System.out.println("Sheep at (" + getXcoord() + "," + getYcoord() + ") died of starvation");
        }

        // Schedule next event
        scheduleNext();
    }

    private void tryToReproduceSheep() {
        Integer emptyNeighborDirection = globalRef.getRandomNeighbor(getXcoord(), getYcoord(), CellState.EMPTY);
        if (emptyNeighborDirection != null) {
            String outputPort = getOutportForDirection(emptyNeighborDirection);
            content con = makeContent(outputPort, new entity("reproduce_sheep"));
            messageList.add(con);
            System.out.println("Sheep at (" + getXcoord() + "," + getYcoord() +
                    ") reproducing to direction " + emptyNeighborDirection);
        } else {
            System.out.println("Sheep at (" + getXcoord() + "," + getYcoord() +
                    ") cannot reproduce - no empty neighbors");
        }
    }

    public void setInitialState(String state) {
        currentState = state;
        if (globalRef != null && GlobalRef.state != null) {
            GlobalRef.state[getXcoord()][getYcoord()] = state;

            // Update visualization when state is set
            if (cellGridView != null) {
                if (state.equals(CellState.GRASS)) {
                    cellGridView.drawCellToScale(x_pos, y_pos, Color.GREEN);
                } else if (state.equals(CellState.SHEEP)) {
                    cellGridView.drawCellToScale(x_pos, y_pos, Color.RED);
                } else {
                    cellGridView.drawCellToScale(x_pos, y_pos, Color.WHITE);
                }
            }
        }
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