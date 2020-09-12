/**
 * This class provides several methods to allow for Water manipulation, clearing and 
 * behaviour patterns for the water flow simulator. Changes on the BufferedImage representing water
 * are reflected here, and vice versa. This class is used to control the functioning of said
 * BufferedImage, which is overlaid on top of the Terrain BufferedImage.
 * @author Alison Soutar
 */
public class Water{
    private int x, y, dimx, dimy; //dimensions which water can occupy
    private float[][] depth; //how many units of water in this grid point
    private float[][] height; //this height INCLUDES the water depth!
    Terrain landdata;

    public Water(){ }

    /** 
     * This is the method needing to be used whenever the Water grid is updated.
     * This is to prevent bad interleavings and incorrect values being written.
     * It can be used for addition and subtraction.
     * @param x Current cell x to be altered
     * @param y Current cell y to be altered
     * @param val Value which we will add to / subtract from (x,y) for total depth
     */
    synchronized void updateDepth(int x, int y, float val){
        float temp = depth[x][y];
        depth[x][y] = temp + val; //if val is negative, it will still minus
    }

    /**
     * This is used to alter the grid information for height data of overall terrain,
     * including height added by water. It is used to update the height grid and prevent
     * bad interleavings.
     * @param x Current cell x to be altered
     * @param y Current cell y to be altered
     * @param val Value which we will add to / subtract from (x,y) for total height
     */
    synchronized void updateHeight(int x, int y, float val){
        float temp = height[x][y];
        height[x][y] = temp + val;
    }

    /**
     * Goes to each cell in the Water grid, minusing water height (if any) from the overall grid.
     * The depth value of each cell is then set to 0f, thereby removing all water from the terrain
     * and normalising the height values to their original form.
     * This clears the entire terrain.
     */
    public void clear(){ //clear whole grid
        for(int col = 0; col < dimx+1; col++){
            for(int row = 0; row < dimy+1; row++){
                height[col][row] = height[col][row] - depth[col][row]; //makes sure the height goes back to normal
                depth[col][row] = 0f; //clears of water, flows off edge
            }
        }
    }

    /**
     * Clears water along the edges of the simulation. This simulates water flowing off the edge of
     * the grid and off the map. Water depth is minused from overall height, to prevent
     * the map-edge from growing "taller".
     */
    public void runOff(){ //water flowing off side of map
        for(int firstRow = 0; firstRow < dimx+1; firstRow ++){
            height[firstRow][0] -= depth[firstRow][0]; //minusing the depth from each cell along topmost border
            depth[firstRow][0] = 0.0f; //resets depth value of water to 0, water's "moving out of sight"
        }
        for(int lastRow = 0; lastRow < dimx+1; lastRow++){
            height[lastRow][dimy] -= depth[lastRow][dimy];
            depth[lastRow][dimy] = 0.0f;
        }
        for(int firstCol = 1; firstCol < dimy; firstCol++ ){ //excluding the points we've already zero'd in the previous 2 if-statements
            height[0][firstCol] -= depth[0][firstCol];
            depth[0][firstCol] = 0.0f;
        }
        for(int lastCol = 1; lastCol < dimy; lastCol++){
            height[dimx][lastCol] -= depth[dimx][lastCol];
            depth[dimx][lastCol] = 0.0f;
        }
    }

    /**
     * This allows the Water class to access the Terrain information directly.
     * It is called in Flow's main method, so as soon as the file is processed
     * the information is avaliable to the Water class as well.
     * It contains a method call to createOverlay.
     * @param l Terrain data that the simulation is currently running.
     */
    public void giveTerrain(Terrain l) {
        landdata = l;
        createOverlay(landdata.getDimX(), landdata.getDimY()); // passes dimensional info so that a water layer
                                                            // can be placed on top of the given terrain later
    }

    /**
     * This method creates two 2D arrays: One to hold water depth information, and 
     * another which stores the height of various gridpoints over which the water 
     * is placed. This height includes the height added by water units.
     * @param dimx The x-dimension for the Terrain over which the Water is placed.
     * @param dimy The y-dimension for the Terrain over which the Water is placed.
     */
    public void createOverlay(int dimx, int dimy){
        this.dimx = dimx;
        this.dimy = dimy;
        this.height = landdata.getHeight(); //this is the height data of the terrain
        depth = new float[dimx][dimy]; //this is the depth data of any water flowing over terrain
    }

    /**
     * When the Water layer has been clicked on, the x and y co ordinates of the click
     * are captured and sent here. The clicked on block, as well as neighbouring blocks
     * up to 3 grid positions away are filled with 3 units of water.
     * @param x X co-ord of click
     * @param y Y co-ord of click
     */
    public void addWater(int x, int y){ //water water to current point and its neighbours
        
        if(x>3 && y>3 && x<(dimx-3) && y<(dimy-3)){//it won't run off the edge of the map: prevent nullpointer
            //populating surrounding points and clicked point
            for(int xCoOrd = x-3; xCoOrd < x+4; x++){
                for(int yCoOrd = y-3; yCoOrd < y+4; y++){
                    updateDepth(xCoOrd, yCoOrd, 0.03f);//3 units, or height of 0.03m is added to this grid point
                    updateHeight(xCoOrd, yCoOrd, 0.03f); //0.03m of height is added to the "terrain" height, so when
                    //comparing the water height is also taken into consideration
                }
            }
        }
    }

    /**
     * When it's been determined that a neighbouring grid point is lower down than the current 
     * grid point, this method is called so that a unit of water can be moved to this neighbour
     * from this point. 
     * The entire method is synchronised to prevent bad interleavings.
     * @param x Current grid cell's x co-ordinate
     * @param y Current grid cell's y co-ordinate
     * @param newX New grid cell's x co-ordinate
     * @param newY New grid cell's y co-ordinate
     */
    synchronized void moveWater(int x, int y, int newX, int newY){
        if(!(depth[x][y] == 0)){ //ensures there's actually water here
            updateDepth(x, y, -0.01f); //0.01 is minused from current depth
            updateHeight(x, y, -0.01f); //the height value updated, because water is flowing out
            updateDepth(newX, newY, 0.01f); //water depth of new point updated
            updateHeight(newX, newY, 0.01f); //increase height of new gridpoint
        }   
    }

    /**
     * Checks surrounding cell's neighbours heights to determine where water should flow next.
     * If it is shorter, one unit of water is moved to the lowest neighbour. If this operation
     * is a success, it returns true. If not, it returns false.
     * This method will only work on current cells that are not on the border. If it it one
     * cell away from the border, water can still be moved to a border cell, which will later 
     * be cleared with runOff(), which is a specialised clear()
     * This checks where water in the current cell should traverse next
     * @param x Current cell's x co-ordinate
     * @param y Current cell's y co-ordinate
     */
    public boolean checkNeighbours(int x, int y){
        boolean success = true;
        if(!(x == 0 && y == 0 && x == dimx && y == dimy)){ //ensuring we aren't dealing with bordering values here
            synchronized (this){
                int tempx = x; //temporary values for holding the current lowest neighbour calculated
                int tempy = y; //will be overwritten
                float tempval = height[x][y];
                    
                for (int col = x - 1; col < x + 2; col++) { // i = changing values of x
                    // if current cell's height is taller than neighbour's height and neighbour's
                    // height is smaller than current "smallest"
                    if (height[x][y] > height[col][y - 1] && height[col][y - 1] < tempval) { // checking "upstairs"
                                                                                             // neighbours
                        tempx = col;
                        tempy = y - 1; // current lowest height is stored in temp values
                        tempval = height[col][y - 1];
                    }
                }

                for (int col = x - 1; col < x + 2; col = col + 2) {// misses current co-ord
                    if (height[x][y] > height[col][y] && height[col][y] < tempval) { // checking "nextdoor" neighbours
                        tempx = col;
                        tempy = y; // current lowest height is stored in temp values
                        tempval = height[col][y];
                    }
                }

                for (int col = x - 1; col < x + 2; col++) {
                    if (height[x][y] > height[col][y + 1] && height[col][y + 1] < tempval) { // checking "downstairs"
                                                                                             // neighbours
                        tempx = col;
                        tempy = y + 1; // current lowest height is stored in temp values
                        tempval = height[col][y];
                    }
                }
                
                if(!(tempval == height[x][y])){// the height has been updated
                    moveWater(x, y, tempx, tempy); //water is added to the location of next lowest point
                }
                else{ //the height wasn't updated, therefore no water was moved
                    success = false;
                }
            }
        }
        return success;
    }


    public void genWater(){//Creates water overlay

    }
}