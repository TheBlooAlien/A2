import java.awt.image.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.awt.Color;

/**
 * This class provides several methods to allow for Water manipulation, clearing
 * and behaviour patterns for the water flow simulator. Changes on the
 * BufferedImage representing water are reflected here, and vice versa. This
 * class is used to control the functioning of said BufferedImage, which is
 * overlaid on top of the Terrain BufferedImage.
 * 
 * @author Alison Soutar
 */
public class Water {
    private int dimx, dimy; // dimensions which water can occupy
    private float[][] depth; // how many units of water in this grid point
    private float[][] height; // this height INCLUDES the water depth!
    static int loc[] = new int[2]; // locations yeilded from getPermute
    static AtomicBoolean paused = new AtomicBoolean(false); // allowing for pausing which reflects throughout entire
                                                            // programme
    static AtomicInteger count = new AtomicInteger(0); // Counting number of steps in the searching and moving of water
    Terrain landdata;
    BufferedImage img;
    int blue = Color.BLUE.getRGB();
    Color transparent = new Color(0, 0, 0, 0.0f);
    int rgbTransparent = transparent.getRGB();
    static float startingDepth = 0.0f;
    static float endingDepth = 0.0f;
    static float basinDepth = 0.0f;

    /**
     * Default constructor for Water class.
     */
    public Water() {
    }

    /**
     * 
     * This parameterised constructor initialises two 2D arrays: One to hold water
     * depth information, and another which stores the height of various gridpoints
     * over which the water is placed. This height includes the height added by
     * water units. It also takes the dimensions of the x and y values of the
     * currently running Terrain, used in constructing this class' image.
     * 
     * @param dimx The x-dimension for the Terrain over which the Water is placed.
     * @param dimy The y-dimension for the Terrain over which the Water is placed.
     * @param l    Terrain data that the simulation is currently running.
     */
    public Water(int dimx, int dimy, Terrain landdata) {
        this.dimx = dimx;
        this.dimy = dimy;
        this.landdata = landdata;
        this.height = landdata.getHeight(); // this is the height data of the terrain
        depth = new float[dimx][dimy]; // this is the depth data of any water flowing over terrain
        deriveImage();
    }

    /**
     * Accessor method to obtain Water's BufferedImage representation.
     * 
     * @return Transparent image with cells occupied by water shown in blue.
     */
    public BufferedImage getImage() {
        return img;
    }

    /**
     * Method for accessing water depth of current cell.
     * 
     * @param x Cell's x co-ord
     * @param y Cell's y co-ord
     * @return Current cell's depth.
     */
    public float getDepth(int x, int y) {
        return depth[x][y];
    }

    /**
     * @return X's Dimension
     */
    public int getDimX() {
        return dimx;
    }

    /**
     * @return Y's Dimension
     */
    public int getDimY() {
        return dimy;
    }

    /**
     * This is the method needing to be used whenever the Water grid is updated.
     * This is to prevent bad interleavings and incorrect values being written. It
     * can be used for addition and subtraction.
     * 
     * @param x   Current cell x to be altered
     * @param y   Current cell y to be altered
     * @param val Value which we will add to / subtract from (x,y) for total depth
     */
    synchronized void updateDepth(int x, int y, float val) {
        float temp = depth[x][y];
        depth[x][y] = temp + val; // if val is negative, it will still minus
    }

    /**
     * This is used to alter the grid information for height data of terrain,
     * including water height. It is used to update the height grid and prevent bad
     * interleavings.
     * 
     * @param x   Current cell x to be altered
     * @param y   Current cell y to be altered
     * @param val Value which we will add to / subtract from (x,y) for total height
     */
    synchronized void updateHeight(int x, int y, float val) {
        float temp = height[x][y];
        height[x][y] = temp + val;
    }

    /**
     * Goes to each cell in the Water grid, minusing water height (if any) from the
     * overall grid. The depth value of each cell is then set to 0f, thereby
     * removing all water from the terrain and normalising the height values to
     * their original form. This clears the entire terrain.
     */
    synchronized public void clear() { // clear whole grid
        for (int col = 0; col < dimx; col++) {
            for (int row = 0; row < dimy; row++) {
                updateHeight(col, row, - depth[col][row]); // makes sure the height goes back to normal
                basinDepth += depth[col][row];// TODO: This is broken
                updateDepth(col,row, -depth[col][row]); // clears of water, flows off edge
                img.setRGB(col, row, rgbTransparent);
            }
        }
        // refresh();
    }

    /**
     * When the Water layer has been clicked on, the x and y co ordinates of the
     * click are captured and sent here. The clicked on block, as well as
     * neighbouring blocks up to 3 grid positions away are filled with 3 units of
     * water.
     * 
     * @param x X co-ord of click
     * @param y Y co-ord of click
     */
    public void addWater(int x, int y) { // water water to current point and its neighbours
        int pointCount = 0;
        if (x > 3 && y > 3 && x < (dimx - 3) && y < (dimy - 3)) {// it won't run off the edge of the map: prevent
                                                                 // nullpointer
            // populating surrounding points and clicked point
            for (int xCoOrd = x - 3; xCoOrd < x + 4; xCoOrd++) {
                for (int yCoOrd = y - 3; yCoOrd < y + 4; yCoOrd++) {
                    pointCount++;
                    depth[xCoOrd][yCoOrd] += 0.03f;// 3 units, or height of 0.03m is added to this grid point. Not
                                                   // synchronised: not necessary
                    height[xCoOrd][yCoOrd] += 0.03f; // 0.03m of height is added to the "terrain" height, so when
                    img.setRGB(xCoOrd, yCoOrd, blue);
                    // comparing the water height is also taken into consideration
                    synchronized (this) {
                        startingDepth = Float.sum(startingDepth, 0.03f);
                       }

                }
            }
        }

    }

    /**
     * Checks surrounding cell's neighbours heights to determine where water should
     * flow next. If it is shorter, one unit of water is moved to the lowest
     * neighbour. If this operation is a success, it returns true. If not, it
     * returns false. This method will only work on current cells that are not on
     * the border. If the current cell is on the border, than the depth of the water
     * will be minused from that cell's height, the cell will be made transparent
     * and the depth will be zerod. moveWater is then called to move the water to
     * the next lowest gridpoint.
     * 
     * @param x Current cell's x co-ordinate
     * @param y Current cell's y co-ordinate
     */
    synchronized boolean checkNeighbours(int x, int y) {
        boolean success = false;
        if (x != 0 & y != 0 & x != dimx - 1 & y != dimy - 1) {// if not edge cell
            if (depth[x][y] > 0) {// checking there's water here
                // ensuring we aren't dealing with bordering values here

                int tempx = x; // temporary values for holding the current lowest neighbour calculated
                int tempy = y; // will be overwritten
                float tempval = height[x][y];

                for (int col = x - 1; col < x + 2; col++) { // i = changing values of x
                    // if current cell's height is taller than neighbour's height and neighbour's
                    // height is smaller than current "smallest"
                    if (height[x][y] > height[col][y - 1] & height[col][y - 1] < tempval) { // checking "upstairs"
                        // neighbours
                        tempx = col;
                        tempy = y - 1; // current lowest height is stored in temp values
                        tempval = height[col][y - 1];
                    }
                }

                for (int col = x - 1; col < x + 2; col = col + 2) {// misses current co-ord
                    if (height[x][y] > height[col][y] & height[col][y] < tempval) { // checking "nextdoor"
                                                                                    // neighbours
                        tempx = col;
                        tempy = y; // current lowest height is stored in temp values
                        tempval = height[col][y];
                    }
                }

                for (int col = x - 1; col < x + 2; col++) {
                    if (height[x][y] > height[col][y + 1] & height[col][y + 1] < tempval) { // checking
                                                                                            // "downstairs"
                                                                                            // neighbours
                        tempy = y + 1; // current lowest height is stored in temp values
                        tempval = height[col][y + 1];
                    }
                }

                if (!(tempval == height[x][y])) {// the height has been updated
                    success = true;
                    moveWater(x, y, tempx, tempy); // water is added to the location of next lowest point
                } else { // the height wasn't updated, therefore no water was moved
                    success = false;
                }
            }

        } // if edge cell, clear it
        else {// this emulates the runoff
            img.setRGB(x, y, rgbTransparent);
                if (depth[x][y] != 0.0f) {
                    endingDepth += depth[x][y]; // checking for accuracy
                    updateHeight(x, y, -(depth[x][y]));
                    updateDepth(x, y, -(depth[x][y]));
                }
        }
        count.incrementAndGet();
        return success;

    }

    /**
     * When it's been determined that a neighbouring grid point is lower down than
     * the current grid point, this method is called so that a unit of water can be
     * moved to this neighbour from this point. The entire method is synchronised to
     * prevent bad interleavings.
     * 
     * @param x    Current grid cell's x co-ordinate
     * @param y    Current grid cell's y co-ordinate
     * @param newX New grid cell's x co-ordinate
     * @param newY New grid cell's y co-ordinate
     */
    synchronized void moveWater(int x, int y, int newX, int newY) {
        if ((depth[x][y] > 0)) { // ensures there's actually water here
            updateDepth(x, y, -0.01f); // 0.01 is minused from current depth
            updateHeight(x, y, -0.01f); // the height value updated, because water is flowing out
            updateDepth(newX, newY, 0.01f); // water depth of new point updated
            updateHeight(newX, newY, 0.01f); // increase height of new gridpoint

            if (depth[x][y] == 0f) {// after updating and it's zero now
                img.setRGB(x, y, rgbTransparent);
            }

            img.setRGB(newX, newY, blue);// make the new co-ords blue
        }
    }

    /**
     * 
     * @param start Starting index to permute through array
     * @param end   Ending index of array permutation
     */
    synchronized void waterFlow(int start, int end) {
        while (paused.get() == false) {
            try {
                Thread.sleep(50);
                for (int i = start; i < end; i++) {
                    landdata.getPermute(i, loc);
                    checkNeighbours(loc[0], loc[1]);
                }
            } catch (Exception e) {
                e.printStackTrace();

            }
        }
    }

    public void deriveImage() {// Creates water overlay
        img = new BufferedImage(dimy, dimx, BufferedImage.TYPE_INT_ARGB);

        for (int x = 0; x < dimx; x++) {
            for (int y = 0; y < dimy; y++) {
                img.setRGB(x, y, rgbTransparent);
            }
        }

        // add another method to refresh the 2D array so values can be updated;
        // something about assigning a depth > 0.0f with the colour blue,
        // and doing this check routinely for every clock count
    }
}