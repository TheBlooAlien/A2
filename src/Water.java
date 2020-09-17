import java.awt.image.*;
import java.awt.Color;

/**
 * This class provides several methods to allow for Water manipulation, clearing and 
 * behaviour patterns for the water flow simulator. Changes on the BufferedImage representing water
 * are reflected here, and vice versa. This class is used to control the functioning of said
 * BufferedImage, which is overlaid on top of the Terrain BufferedImage.
 * @author Alison Soutar
 */
public class Water{
    private int dimx, dimy; //dimensions which water can occupy
    private float[][] depth; //how many units of water in this grid point
    private float[][] height; //this height INCLUDES the water depth!
    public boolean paused = false;
    Terrain landdata;
    BufferedImage img;
    Color blue = new Color(0, 0, 1); //makes it blue

    /**
     * Default constructor for Water class.
     */
    public Water(){ }

    /**
     *  
     * This parameterised constructor initialises two 2D arrays: One to hold water depth information, and  another which stores the height of various gridpoints
     * over which the water is placed. This height includes the height added by water units. It also takes the dimensions of the x and y values of the currently 
     * running Terrain, used in constructing this class' image.
     * @param dimx The x-dimension for the Terrain over which the Water is placed.
     * @param dimy The y-dimension for the Terrain over which the Water is placed.
     * @param l Terrain data that the simulation is currently running.
     */
    public Water(int dimx, int dimy, Terrain landdata){
        this.dimx = dimx;
        this.dimy = dimy;
        this.landdata = landdata;
        this.height = landdata.getHeight(); //this is the height data of the terrain
        depth = new float[dimx][dimy]; //this is the depth data of any water flowing over terrain
        deriveImage();
    }

    /**
     * Accessor method to obtain Water's BufferedImage representation.
     * @return Transparent image with cells occupied by water shown in blue.
     */
    public BufferedImage getImage(){
        return img;
    }

    /**
     * Method for accessing water depth of current cell.
     * @param x Cell's x co-ord
     * @param y Cell's y co-ord
     * @return Current cell's depth.
     */
    public float getDepth(int x, int y){
        return depth[x][y];
    }

    /**
     * @return X's Dimension
     */
    public int getDimX(){
        return dimx;
    }

    /**
     * @return Y's Dimension
     */
    public int getDimY(){
        return dimy;
    }

    /** 
     * This is the method needing to be used whenever the Water grid is updated. This is to prevent bad interleavings and incorrect values being written.
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
     * This is used to alter the grid information for height data of terrain, including water height. It is used to update the height grid and prevent bad interleavings.
     * @param x Current cell x to be altered
     * @param y Current cell y to be altered
     * @param val Value which we will add to / subtract from (x,y) for total height
     */
    synchronized void updateHeight(int x, int y, float val){
        float temp = height[x][y];
        height[x][y] = temp + val;
    }

    /**
     * Goes to each cell in the Water grid, minusing water height (if any) from the overall grid. The depth value of each cell is then set to 0f, thereby removing all water from the terrain
     * and normalising the height values to their original form. This clears the entire terrain.
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
     * Clears water along the edges of the simulation. This simulates water flowing off the edge of the grid and off the map. Water depth is minused from overall height, to prevent
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
     * Refreshes the Water BufferedImage, making any cell on the grid that is occupied by water blue. 
     * If there isn't any water in that cell, its colour is set to transparent.
     */
    public void refresh(){//will refresh the water on the map, get a new image to reoverlay
		for(int x = 0; x < dimx; x++){
			for(int y = 0; y < dimy; y++){
                if(depth[x][y] != 0.0f){//if there's any water in here
                    img.setRGB(x, y, Color.BLUE.getRGB());
                }
                else{
                    img.setRGB(x, y, 0); //this SHOULD BE transparent... I hope.
                }				
			}
		}
	}

    /**
     * Checks surrounding cell's neighbours heights to determine where water should flow next. If it is shorter, one unit of water is moved to the lowest neighbour. If this operation
     * is a success, it returns true. If not, it returns false. This method will only work on current cells that are not on the border. If it it one cell away from the border, water
     * can still be moved to a border cell, which will later be cleared with runOff(). This checks where water in the current cell should traverse next.
     * @param x Current cell's x co-ordinate
     * @param y Current cell's y co-ordinate
     */
    public boolean checkNeighbours(int x, int y) {
        // TODO: Check how this would work with getPermute()
        // TODO: Change so it operates on different parts? Can't wait so long for lock
        // to free up
        boolean success = true;
        if (!(x == 0 && y == 0 && x == dimx && y == dimy)) { // ensuring we aren't dealing with bordering values here
            synchronized (this) {
                if (paused == false) {
                    int tempx = x; // temporary values for holding the current lowest neighbour calculated
                    int tempy = y; // will be overwritten
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
                        if (height[x][y] > height[col][y] && height[col][y] < tempval) { // checking "nextdoor"
                                                                                         // neighbours
                            tempx = col;
                            tempy = y; // current lowest height is stored in temp values
                            tempval = height[col][y];
                        }
                    }

                    for (int col = x - 1; col < x + 2; col++) {
                        if (height[x][y] > height[col][y + 1] && height[col][y + 1] < tempval) { // checking
                                                                                                 // "downstairs"
                                                                                                 // neighbours
                            tempx = col;
                            tempy = y + 1; // current lowest height is stored in temp values
                            tempval = height[col][y];
                        }
                    }

                    if (!(tempval == height[x][y])) {// the height has been updated
                        moveWater(x, y, tempx, tempy); // water is added to the location of next lowest point
                    } else { // the height wasn't updated, therefore no water was moved
                        success = false;
                    }
                }
                else{ //ideally this will keep running until paused is false again
                    while(paused == true){
                        continue;
                    }
                }
            }
        }
        return success;
    }

    public void pause(){

    }

    public void deriveImage(){//Creates water overlay
        img = new BufferedImage(dimy, dimx, BufferedImage.TYPE_INT_ARGB);
        
        //TODO: Create a transparent buffered image over the terrain, add this as a component
        //add another method to refresh the 2D array so values can be updated; something about assigning a depth > 0.0f with the colour blue, 
        //and doing this check routinely for every clock count
    }
}