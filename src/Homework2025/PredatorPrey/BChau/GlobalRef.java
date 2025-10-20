
package Homework2025.PredatorPrey.BChau;

import genDevs.modeling.*;
import java.util.ArrayList;
import java.util.Random;
// import getNeighborXYCoord from SheepGrassCellSpace =>

/**
 * This class defines an GlobalRef that makes it easy to find a cell and its
 * information, such as current state of the cell, and the cell's reference
 *
 * @author Xiaolin Hu
 * @Date: Sept. 2007
 */
public class GlobalRef {
    protected static int xDim;
    protected static int yDim;
    protected static GlobalRef _instance = null;
    protected Random random;

    public static String[][] state; // state of all cells
    public static IODevs[][] cell_ref; // reference to all cells

    public static final double grassReproduceT = 2.0; // change to the appropriate value for your model
    public static final double sheepMoveT = 1.5; // change to the appropriate value for your model
    public static final double sheepLifeT = 3.0; // change to the appropriate value for your model
    public static final double sheepReproduceT = 4.0; // change to the appropriate value for your model

    private GlobalRef() {

        random = new Random(123456);
    }

    public static GlobalRef getInstance() {
        if (_instance != null)
            return _instance;
        else {
            _instance = new GlobalRef();
            return _instance;
        }
    }

    public void setDim(int x, int y) {
        xDim = x;
        yDim = y;
        state = new String[xDim][yDim];
        cell_ref = new IODevs[xDim][yDim];
        for (int i = 0; i < xDim; i++) {
            for (int j = 0; j < yDim; j++) {
                state[i][j] = "EMPTY";
            }
        }
    }

    // Overloaded method to accept (int x, int y, int direction)
    public int[] getNeighborXYCoord(int x, int y, int direction) {
        int tempXplus1 = x + 1;
        int tempXminus1 = x - 1;
        int tempYplus1 = y + 1;
        int tempYminus1 = y - 1;

        if (tempXplus1 >= xDim)
            tempXplus1 = 0;

        if (tempXminus1 < 0)
            tempXminus1 = xDim - 1;

        if (tempYplus1 >= yDim)
            tempYplus1 = 0;

        if (tempYminus1 < 0)
            tempYminus1 = yDim - 1;

        int[] myneighbor = new int[2];

        // N
        if ((direction == 0)) {
            myneighbor[0] = x;
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
            myneighbor[1] = y;
        }
        // SE
        else if ((direction == 3)) {
            myneighbor[0] = tempXplus1;
            myneighbor[1] = tempYminus1;
        }
        // S
        else if ((direction == 4)) {
            myneighbor[0] = x;
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
            myneighbor[1] = y;
        }
        // NW
        // ( (direction == 7) )
        else {
            myneighbor[0] = tempXminus1;
            myneighbor[1] = tempYplus1;
        }
        return myneighbor;
    }

    public Integer getRandomNeighbor(int x, int y, String targetState) { // e.g., "EMPTY" or "GRASS"
        ArrayList<Integer> candidates = new ArrayList<>();

        // Check all 8 directions
        for (int dir = 0; dir < 8; dir++) {
            int[] neighbor = getNeighborXYCoord(x, y, dir); // Get neighbor coordinates
            if (state[neighbor[0]][neighbor[1]].equals(targetState)) { // e.g., "EMPTY" or "GRASS"
                candidates.add(dir);
            }
        }

        if (candidates.isEmpty()) {
            return null;
        } else {
            return candidates.get(random.nextInt(candidates.size())); // Randomly select one
        }
    }

}
