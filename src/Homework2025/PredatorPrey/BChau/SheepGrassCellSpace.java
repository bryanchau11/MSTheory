package Homework2025.PredatorPrey.BChau;

import simView.*;
import twoDCellSpace.*;
import genDevs.modeling.*;
import genDevs.simulation.*;
import genDevs.simulation.realTime.*;
import GenCol.*;
import genDevs.plots.*;
import java.util.*;
import java.awt.*;

import java.text.*;
import java.io.*;

public class SheepGrassCellSpace extends TwoDimCellSpace {

    newCellGridPlot plot;

    public SheepGrassCellSpace(int scenarioNumber) {
        this(40, 40, scenarioNumber);
    }

    public SheepGrassCellSpace() {
        this(40, 40, 1); // Default to scenario 1
    }

    public SheepGrassCellSpace(int xDim, int yDim, int scenarioNumber) {
        super("SheepGrass Cell Space", xDim, yDim);
        // Initialize GlobalRef FIRST
        GlobalRef globalRef = GlobalRef.getInstance();
        globalRef.setDim(xDim, yDim);

        plot = new newCellGridPlot("SheepGrassCellSpace", 0.1, "", 400, "", 400);
        plot.setCellSize(10);
        plot.setCellGridViewLocation(570, 100);

        this.numCells = xDim * yDim;

        // Create all cells
        for (int i = 0; i < xDimCellspace; i++) {
            for (int j = 0; j < yDimCellspace; j++) {
                System.out.println("Creating cell at (" + i + "," + j + ")");
                SheepGrassCell cell = new SheepGrassCell(i, j);
                addCell(cell, xDimCellspace, yDimCellspace);
            }
        }
        // for (int i = 0; i < xDimCellspace; i++) {
        // for (int j = 0; j < yDimCellspace; j++) {
        // SheepGrassCell cell = (SheepGrassCell) withId(i, j);
        // cell.initialize(); // Initialize after parent is set
        // }
        // }
        doScenario(scenarioNumber);
        DoNeighborCouplings();
        DoBoundaryToBoundaryCoupling();
    }

    private void doScenario(int scenarioNumber) {
        switch (scenarioNumber) {
            case 1:
                setupScenario1();
                break;
            case 2:
                setupScenario2();
                break;
            case 3:
                setupScenario3();
                break;
            default:
                System.out.println("✗ ERROR: Invalid scenario number!");
        }
    }

    private void setupScenario1() {
        int centerX = xDimCellspace / 2; // 20 for 40x40 grid
        int centerY = yDimCellspace / 2; // 20 for 40x40 grid

        System.out.println("✓ Scenario 1: Setting up single grass cell at center location");

        // Get the center cell and set it to grass
        SheepGrassCell centerCell = (SheepGrassCell) withId(centerX, centerY);
        if (centerCell != null) {
            centerCell.setInitialState(SheepGrassCell.CellState.GRASS);
            System.out.println("  → Set cell (" + centerX + "," + centerY + ") to GRASS");
        } else {
            System.out.println("✗ ERROR: Could not find center cell at (" + centerX + "," + centerY + ")!");
        }

        System.out.println("✓ Total grass cells created: 1");
    }

    private void setupScenario2() {
        // Define multiple grass locations across the space
        int[][] grassLocations = {
                { 10, 10 }, // Top-left area
                { 30, 10 }, // Top-right area
                { 10, 30 }, // Bottom-left area
                { 30, 30 }, // Bottom-right area
                { 20, 20 }, // Center
                { 5, 15 }, // Left side
                { 35, 25 }, // Right side
                { 15, 5 }, // Top side
                { 25, 35 } // Bottom side
        };
        // SheepGrassCell centerCell2 = (SheepGrassCell) withId(xDimCellspace / 2 + 1,
        // 20);
        // centerCell2.setInitialState(SheepGrassCell.CellState.SHEEP);
        System.out.println("✓ Scenario 3: Setting up multiple grass cells at different locations");

        // Set each location to grass
        for (int i = 0; i < grassLocations.length; i++) {
            int x = grassLocations[i][0];
            int y = grassLocations[i][1];

            // Make sure coordinates are within bounds
            if (x < xDimCellspace && y < yDimCellspace) {
                SheepGrassCell grassCell = (SheepGrassCell) withId(x, y);
                if (grassCell != null) {
                    grassCell.setInitialState(SheepGrassCell.CellState.GRASS);
                    System.out.println("  → Set cell (" + x + "," + y + ") to GRASS");
                } else {
                    System.out.println("✗ ERROR: Could not find cell at (" + x + "," + y + ")!");
                }
            } else {
                System.out.println("✗ ERROR: Coordinates (" + x + "," + y + ") are out of bounds!");
            }
        }

        System.out.println("✓ Total grass cells created: " + grassLocations.length);
    }

    private void setupScenario3() {
        int centerX = xDimCellspace / 2;
        int centerY = yDimCellspace / 2;

        System.out.println("✓ Scenario 3: Setting up single sheep at center location (no grass)");

        // Get the center cell and set it to sheep
        SheepGrassCell centerCell = (SheepGrassCell) withId(centerX, centerY);
        if (centerCell != null) {
            centerCell.setInitialState(SheepGrassCell.CellState.SHEEP);
            System.out.println("  → Set cell (" + centerX + "," + centerY + ") to SHEEP");
        } else {
            System.out.println("✗ ERROR: Could not find center cell at (" + centerX + "," + centerY + ")!");
        }

        System.out.println("✓ Total sheep cells created: 1");
    }

    public static void main(String args[]) {
        int scenarioNumber = 3;
        SheepGrassCellSpace model = new SheepGrassCellSpace(scenarioNumber);
        TunableCoordinator r = new TunableCoordinator(model);
        r.setTimeScale(0.2);

        r.initialize();
        r.simulate(10000);
        // System.exit(0);
    }

    ///////////////////////////////////////////////////////////////////////////////////////
    // The following are two utility functions that can be useful for you to finish
    /////////////////////////////////////////////////////////////////////////////////////// the
    /////////////////////////////////////////////////////////////////////////////////////// homework.
    // Feel free to modify them, and/or copy them to other places of your code that
    /////////////////////////////////////////////////////////////////////////////////////// work
    /////////////////////////////////////////////////////////////////////////////////////// for
    /////////////////////////////////////////////////////////////////////////////////////// your
    /////////////////////////////////////////////////////////////////////////////////////// model

    /**
     * Add couplings among boundary cells to make the cell space wrapped
     */
    /**
     * Add couplings between each cell and its immediate neighbors
     * This creates the normal adjacent cell connections
     */
    private void DoNeighborCouplings() {
        for (int x = 0; x < xDimCellspace; x++) {
            for (int y = 0; y < yDimCellspace; y++) {
                TwoDimCell cell = (TwoDimCell) withId(x, y);

                // Couple to all 8 immediate neighbors
                for (int dir = 0; dir < 8; dir++) {
                    int[] neighborCoord = getNeighborXYCoord(cell, dir);
                    TwoDimCell neighborCell = (TwoDimCell) withId(neighborCoord[0], neighborCoord[1]);

                    // Get the correct output and input ports for this direction
                    String outPort = getOutputPortForDirection(dir);
                    String inPort = getInputPortForDirection(getOppositeDirection(dir));

                    // Create the coupling: cell outputs to neighbor's input
                    addCoupling(cell, outPort, neighborCell, inPort);
                }
            }
        }
    }

    /**
     * Convert direction number to output port name
     */
    private String getOutputPortForDirection(int direction) {
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

    /**
     * Convert direction number to input port name
     */
    private String getInputPortForDirection(int direction) {
        switch (direction) {
            case 0:
                return "inN"; // North
            case 1:
                return "inNE"; // Northeast
            case 2:
                return "inE"; // East
            case 3:
                return "inSE"; // Southeast
            case 4:
                return "inS"; // South
            case 5:
                return "inSW"; // Southwest
            case 6:
                return "inW"; // West
            case 7:
                return "inNW"; // Northwest
            default:
                return "inN";
        }
    }

    /**
     * Get the opposite direction (for input port matching)
     */
    private int getOppositeDirection(int direction) {
        return (direction + 4) % 8; // Opposite direction is 180 degrees away
    }

    private void DoBoundaryToBoundaryCoupling() {
        // top and bottom rows
        for (int x = 1; x < xDimCellspace - 1; x++) {
            // (x,0) -- bottom to top
            addCoupling(withId(x, 0), "outS", withId(x, yDimCellspace - 1), "inN");
            addCoupling(withId(x, 0), "outSW", withId(x - 1, yDimCellspace - 1), "inNE");
            addCoupling(withId(x, 0), "outSE", withId(x + 1, yDimCellspace - 1), "inNW");

            // (x,29) -- top to bottom
            addCoupling(withId(x, yDimCellspace - 1), "outN", withId(x, 0), "inS");
            addCoupling(withId(x, yDimCellspace - 1), "outNE", withId(x + 1, 0), "inSW");
            addCoupling(withId(x, yDimCellspace - 1), "outNW", withId(x - 1, 0), "inSE");
        }

        // west and east columns
        for (int y = 1; y < yDimCellspace - 1; y++) {
            // (0,y) -- West - east
            addCoupling(withId(0, y), "outW", withId(xDimCellspace - 1, y), "inE");
            addCoupling(withId(0, y), "outSW", withId(xDimCellspace - 1, y - 1), "inNE");
            addCoupling(withId(0, y), "outNW", withId(xDimCellspace - 1, y + 1), "inSE");

            // (29,y) -- West - east
            addCoupling(withId(xDimCellspace - 1, y), "outE", withId(0, y), "inW");
            addCoupling(withId(xDimCellspace - 1, y), "outNE", withId(0, y + 1), "inSW");
            addCoupling(withId(xDimCellspace - 1, y), "outSE", withId(0, y - 1), "inNW");
        }
        // corners
        // (0, 0)
        addCoupling(withId(0, 0), "outNW", withId(xDimCellspace - 1, 1), "inSE");
        addCoupling(withId(0, 0), "outW", withId(xDimCellspace - 1, 0), "inE");
        addCoupling(withId(0, 0), "outSW", withId(xDimCellspace - 1, yDimCellspace - 1), "inNE");
        addCoupling(withId(0, 0), "outS", withId(0, yDimCellspace - 1), "inN");
        addCoupling(withId(0, 0), "outSE", withId(1, yDimCellspace - 1), "inNW");
        // (29, 0)
        addCoupling(withId(xDimCellspace - 1, 0), "outSW", withId(xDimCellspace - 2, yDimCellspace - 1), "inNE");
        addCoupling(withId(xDimCellspace - 1, 0), "outE", withId(0, 0), "inW");
        addCoupling(withId(xDimCellspace - 1, 0), "outSE", withId(0, yDimCellspace - 1), "inNW");
        addCoupling(withId(xDimCellspace - 1, 0), "outS", withId(xDimCellspace - 1, yDimCellspace - 1), "inN");
        addCoupling(withId(xDimCellspace - 1, 0), "outNE", withId(0, 1), "inSW");
        // (0, 29)
        addCoupling(withId(0, yDimCellspace - 1), "outSW", withId(xDimCellspace - 1, yDimCellspace - 2), "inNE");
        addCoupling(withId(0, yDimCellspace - 1), "outW", withId(xDimCellspace - 1, yDimCellspace - 1), "inE");
        addCoupling(withId(0, yDimCellspace - 1), "outNE", withId(1, 0), "inSW");
        addCoupling(withId(0, yDimCellspace - 1), "outN", withId(0, 0), "inS");
        addCoupling(withId(0, yDimCellspace - 1), "outNW", withId(xDimCellspace - 1, 0), "inSE");
        // (29, 29)
        addCoupling(withId(xDimCellspace - 1, yDimCellspace - 1), "outNW", withId(xDimCellspace - 2, 0), "inSE");
        addCoupling(withId(xDimCellspace - 1, yDimCellspace - 1), "outE", withId(0, yDimCellspace - 1), "inW");
        addCoupling(withId(xDimCellspace - 1, yDimCellspace - 1), "outSE", withId(0, yDimCellspace - 2), "inNW"); // Xiaolin
                                                                                                                  // Hu,
                                                                                                                  // 10/16/2016
        addCoupling(withId(xDimCellspace - 1, yDimCellspace - 1), "outN", withId(xDimCellspace - 1, 0), "inS");
        addCoupling(withId(xDimCellspace - 1, yDimCellspace - 1), "outNE", withId(0, 0), "inSW");
    }

    /**
     * Get the x and y coordinate (int[2]) of a neighbor cell based on the direction
     * in a wrapped cell space
     * 
     * @param myCell:    the center cell
     * @param direction: the direction defines which neighbor cell to get. 0 - N; 1
     *                   - NE; 2 - E; ... (clokewise)
     * @return the x and y coordinate
     */
    public int[] getNeighborXYCoord(TwoDimCell myCell, int direction) {
        int[] myneighbor = new int[2];
        int tempXplus1 = myCell.getXcoord() + 1;
        int tempXminus1 = myCell.getXcoord() - 1;
        int tempYplus1 = myCell.getYcoord() + 1;
        int tempYminus1 = myCell.getYcoord() - 1;

        if (tempXplus1 >= xDimCellspace)
            tempXplus1 = 0;

        if (tempXminus1 < 0)
            tempXminus1 = xDimCellspace - 1;

        if (tempYplus1 >= yDimCellspace)
            tempYplus1 = 0;

        if (tempYminus1 < 0)
            tempYminus1 = yDimCellspace - 1;

        // N
        if ((direction == 0)) {
            myneighbor[0] = myCell.getXcoord();
            myneighbor[1] = tempYplus1;
        }
        // NE
        else if ((direction == 1)) {
            myneighbor[0] = tempXplus1;
            myneighbor[1] = tempYplus1;
        }
        // E
        else if ((direction == 2)) {
            myneighbor[0] = tempXplus1;
            myneighbor[1] = myCell.getYcoord();
        }
        // SE
        else if ((direction == 3)) {
            myneighbor[0] = tempXplus1;
            myneighbor[1] = tempYminus1;
        }
        // S
        else if ((direction == 4)) {
            myneighbor[0] = myCell.getXcoord();
            myneighbor[1] = tempYminus1;
        }
        // SW
        else if ((direction == 5)) {
            myneighbor[0] = tempXminus1;
            myneighbor[1] = tempYminus1;
        }
        // W
        else if ((direction == 6)) {
            myneighbor[0] = tempXminus1;
            myneighbor[1] = myCell.getYcoord();
        }
        // NW
        // ( (direction == 7) )
        else {
            myneighbor[0] = tempXminus1;
            myneighbor[1] = tempYplus1;
        }
        return myneighbor;
    }

}
// End SheepGrassCellSpace
