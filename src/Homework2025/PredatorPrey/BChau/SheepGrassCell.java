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
            holdIn("GROW", grassReproduceT);
        } else if (currentState.equals(CellState.SHEEP)) {
            // Calculate time until next events for sheep
            double timeUntilMove = sheepMoveT;
            double timeUntilDeath = sheepLifeT; // Use global life time
            double timeUntilReproduce = sheepReproduceT;

            System.out.println("DEBUG: Sheep at (" + getXcoord() + "," + getYcoord() +
                    ") - moveIn: " + timeUntilMove + ", dieIn: " + timeUntilDeath + ", reproduceIn: "
                    + timeUntilReproduce);

            if (timeUntilDeath <= 0) {
                System.out.println("Sheep at (" + getXcoord() + "," + getYcoord() + ") will DIE NOW");
                holdIn("DIE", 0);
            } else {
                // Find the minimum time among all events
                double minTime = Math.min(Math.min(timeUntilMove, timeUntilDeath), timeUntilReproduce);
                // handle confluent events by priority: DIE > REPRODUCE > MOVE
                if (minTime == timeUntilDeath) {
                    holdIn("DIE", timeUntilDeath);
                } else if (minTime == timeUntilReproduce) {
                    holdIn("REPRODUCE", timeUntilReproduce);
                } else {
                    holdIn("MOVE", timeUntilMove);
                }
            }
        } else {
            holdIn("EMPTY", INFINITY);
        }
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

                } else if (messageContent.startsWith("move_sheep_eat") && currentState.equals(CellState.GRASS)) {
                    // Sheep is moving here and eating the grass
                    currentState = CellState.SHEEP;
                    sheepMoveT = GlobalRef.sheepMoveT;
                    sheepLifeT = GlobalRef.sheepLifeT; // Reset life timer when eating grass
                    sheepReproduceT = GlobalRef.sheepReproduceT;
                    GlobalRef.state[getXcoord()][getYcoord()] = currentState;

                    // Update visualization for sheep
                    if (cellGridView != null) {
                        cellGridView.drawCellToScale(x_pos, y_pos, Color.RED);
                    }

                    System.out.println("Sheep moved to (" + getXcoord() + "," + getYcoord() +
                            ") and ate grass, life timer reset");

                    scheduleNext();

                } else if (messageContent.startsWith("move_sheep:") && currentState.equals(CellState.EMPTY)) {
                    // Sheep is moving here to empty cell
                    String[] parts = messageContent.split(":");
                    double inheritedLifeT = Double.parseDouble(parts[1]);
                    double inheritedReproduceT = (parts.length >= 3) ? Double.parseDouble(parts[2])
                            : GlobalRef.sheepReproduceT;

                    sheepLifeT = inheritedLifeT;
                    sheepReproduceT = inheritedReproduceT;
                    currentState = CellState.SHEEP;
                    sheepMoveT = GlobalRef.sheepMoveT;
                    GlobalRef.state[getXcoord()][getYcoord()] = currentState;

                    // Update visualization for sheep
                    if (cellGridView != null) {
                        cellGridView.drawCellToScale(x_pos, y_pos, Color.RED);
                    }

                    System.out.println("Sheep moved to (" + getXcoord() + "," + getYcoord() +
                            ") with life: " + inheritedLifeT + ", reproduce: " + inheritedReproduceT);

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
        messageList = new message(); // reset
        if (phaseIs("MOVE") && currentState.equals(CellState.SHEEP)) {
            tryToMoveSheep();

            if (pendingMoveDirection != null && pendingMoveAction != null) {
                // Calculate what the timers will be AFTER this move
                double lifeAfterMove = sheepLifeT - sigma;
                double reproduceAfterMove = sheepReproduceT - sigma;

                // If eating grass, life will be reset
                if (pendingMoveAction.equals("move_sheep_eat")) {
                    lifeAfterMove = GlobalRef.sheepLifeT;
                }

                String messageWithTimers = pendingMoveAction + ":" + lifeAfterMove + ":" + reproduceAfterMove;
                String outputPort = getOutportForDirection(pendingMoveDirection);
                content con = makeContent(outputPort, new entity(messageWithTimers));
                messageList.add(con);
                System.out.println("Cell (" + getXcoord() + "," + getYcoord() + ") outgoing move: " + pendingMoveAction
                        + " dir=" + pendingMoveDirection + " life=" + lifeAfterMove + " reproduce="
                        + reproduceAfterMove);
            }
        } else if (phaseIs("GROW") && currentState.equals(CellState.GRASS)) {
            tryToReproduceGrass();
        } else if (phaseIs("REPRODUCE") && currentState.equals(CellState.SHEEP)) {
            tryToReproduceSheep();
        }

        System.out.println(
                "Cell (" + getXcoord() + "," + getYcoord() + ") outputting messages,  messageList: " + messageList);
        return messageList;
    }

    public void deltint() {
        if (phaseIs("GROW") && currentState.equals(CellState.GRASS)) {
            // Reset grass reproduction timer (out() already emitted reproduction messages)
            grassReproduceT = GlobalRef.grassReproduceT;
            System.out.println("Grass at (" + getXcoord() + "," + getYcoord() + ") completed reproduction cycle");

        } else if (phaseIs("MOVE") && currentState.equals(CellState.SHEEP)) {
            // Decrement all sheep timers by the time that passed (sigma)
            sheepLifeT -= sigma;
            sheepReproduceT -= sigma;

            System.out.println("Sheep at (" + getXcoord() + "," + getYcoord() + ") after move - life: " + sheepLifeT
                    + ", reproduce: " + sheepReproduceT);

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

            // Reset pending move and move timer
            pendingMoveAction = null;
            pendingMoveDirection = null;
            sheepMoveT = GlobalRef.sheepMoveT; // Reset move timer

        } else if (phaseIs("REPRODUCE") && currentState.equals(CellState.SHEEP)) {
            // Sheep reproduction logic - decrease life timer but reset reproduction timer
            sheepLifeT -= sigma;
            sheepReproduceT = GlobalRef.sheepReproduceT; // Reset reproduction timer

            System.out.println("Sheep at (" + getXcoord() + "," + getYcoord() + ") reproduced, life: " + sheepLifeT);
            // Note: tryToReproduceSheep() is called in out(), not here

        } else if (phaseIs("DIE") && currentState.equals(CellState.SHEEP)) {
            // Sheep dies
            currentState = CellState.EMPTY;
            GlobalRef.state[getXcoord()][getYcoord()] = currentState;
            if (cellGridView != null) {
                cellGridView.drawCellToScale(x_pos, y_pos, Color.WHITE);
            }
            System.out.println("Sheep at (" + getXcoord() + "," + getYcoord() + ") died of old age");
        }

        // Schedule next event
        scheduleNext();
    }
    // public void deltint() {
    // messageList = new message();

    // if (phaseIs("GROW") && currentState.equals(CellState.GRASS)) {
    // // Grass reproduction logic
    // tryToReproduceGrass();
    // grassReproduceT = GlobalRef.grassReproduceT; // Reset timer

    // } else if (phaseIs("MOVE") && currentState.equals(CellState.SHEEP)) {
    // // Decrement all sheep timers by the time that passed (sigma)
    // sheepLifeT -= sigma;
    // sheepReproduceT -= sigma;

    // // Handle movement
    // if (pendingMoveAction != null && pendingMoveAction.equals("move_sheep_eat"))
    // {
    // // Reset life timer when eating grass
    // sheepLifeT = GlobalRef.sheepLifeT;
    // System.out.println("Sheep at (" + getXcoord() + "," + getYcoord() + ") reset
    // life timer by eating");
    // }

    // if (pendingMoveAction != null && pendingMoveDirection != null) {
    // // This sheep moves out, become empty
    // currentState = CellState.EMPTY;
    // GlobalRef.state[getXcoord()][getYcoord()] = currentState;
    // if (cellGridView != null) {
    // cellGridView.drawCellToScale(x_pos, y_pos, Color.WHITE);
    // }
    // System.out.println("Sheep departed from (" + getXcoord() + "," + getYcoord()
    // + ")");
    // }

    // // Reset pending move
    // pendingMoveAction = null;
    // pendingMoveDirection = null;

    // } else if (phaseIs("REPRODUCE") && currentState.equals(CellState.SHEEP)) {
    // // Sheep reproduction logic
    // sheepReproduceT = GlobalRef.sheepReproduceT; // Reset reproduction timer
    // sheepLifeT -= sigma; // Decrement life timer during reproduction
    // tryToReproduceSheep();

    // } else if (phaseIs("CHECK_LIFE") && currentState.equals(CellState.SHEEP)) {
    // // Life timer expired - sheep dies
    // sheepLifeT = 0;

    // } else if (phaseIs("DIE") && currentState.equals(CellState.SHEEP)) {
    // // Sheep dies
    // currentState = CellState.EMPTY;
    // GlobalRef.state[getXcoord()][getYcoord()] = currentState;
    // if (cellGridView != null) {
    // cellGridView.drawCellToScale(x_pos, y_pos, Color.WHITE);
    // }
    // System.out.println("Sheep at (" + getXcoord() + "," + getYcoord() + ") died
    // of starvation");
    // }

    // // Schedule next event
    // scheduleNext();
    // }

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