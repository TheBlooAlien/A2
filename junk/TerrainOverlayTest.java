import java.awt.Graphics2D;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.net.URL;

public class TerrainOverlayTest{
    public static void main(String args[]) throws Exception{
        int dimx = -1;
        int dimy = -1;

        Terrain terrain = new Terrain();
        terrain.readData(args[0]);
        dimx = terrain.getDimX(); //okay so this is fetching 0
        dimy = terrain.getDimY();

        Water water = new Water(terrain.getDimX(), terrain.getDimY(), terrain);//image is derived here too
        
        water.addWater(dimx/2, dimy/2);

        JFrame frame = new JFrame("This is a simulation!");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new BorderLayout());

        FlowPanel fpTerrain = new FlowPanel(terrain);
        fpTerrain.setPreferredSize(new Dimension(terrain.getDimX(), terrain.getDimY()));
        
        FlowPanel fpWater = new FlowPanel(water);
        fpWater.setPreferredSize(new Dimension(terrain.getDimX(), terrain.getDimY()));

        JLayeredPane jlp = new JLayeredPane();
        jlp.add(fpWater, 2);
        jlp.add(fpTerrain, 1);

        frame.setSize(dimx, dimy);//so it doesn't emerge as a spek
        frame.setLocationRelativeTo(null);
        frame.add(jlp);
        frame.setContentPane(jlp);
        frame.setVisible(true);

    }
}
