import javax.swing.*;
import java.awt.*;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.BorderLayout;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.event.MouseEvent;

public class LayeringPanels{
    static JPanel panel = new JPanel();
    static JLabel label = new JLabel();
    public static void refresh(final BufferedImage img){
        label.setIcon(new ImageIcon(img));
        label.setPreferredSize(new Dimension(img.getWidth(), img.getHeight()));
    }
    public static void main(String args[]){
        Terrain terrain = new Terrain();
        terrain.readData(args[0]);
        int dimx = terrain.getDimX();
        int dimy = terrain.getDimY();
        Water water = new Water(dimx, dimy, terrain);

        JFrame frame = new JFrame("I want to die");
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(new GridLayout(2,1));
        frame.setSize(new Dimension(dimx, dimy));

        FlowPanel fpTerrain = new FlowPanel(terrain);
        fpTerrain.setPreferredSize(new Dimension(dimx, dimy));


        /*Draws two images over each other
        BufferedImage finalImage = new BufferedImage(dimx, dimy, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = finalImage.createGraphics();
        g.drawImage(terrain.getImage(), 0, 0, null);
        g.drawImage(water.getImage(), 0, 0, null);
        g.dispose();
        */

        JButton button = new JButton("Add water");
        button.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                water.addWater(dimx/2, dimy/2);
                BufferedImage finalImage = new BufferedImage(dimx, dimy, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = finalImage.createGraphics();
                g.drawImage(terrain.getImage(), 0, 0, null);
                g.drawImage(water.getImage(), 0, 0, null);
                g.dispose();
                refresh(finalImage);
            }
        });
        panel.setLayout(new GridLayout(2,1));
        panel.add(label, BorderLayout.CENTER);
        refresh(terrain.getImage());
        panel.add(button);
        frame.add(panel);
        frame.setContentPane(panel);
        frame.setVisible(true);

        
    }
}