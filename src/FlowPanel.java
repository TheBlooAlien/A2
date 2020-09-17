import java.awt.Graphics;
import javax.swing.JPanel;

public class FlowPanel extends JPanel implements Runnable {//TODO inherit from Thread?
	Terrain land;
	Water water;
	
	FlowPanel(Terrain terrain) {
		land=terrain;
	}

	FlowPanel(Water water){
		this.water = water;
	}
		
	// responsible for painting the terrain and water
	// as images
	@Override
    protected void paintComponent(Graphics g) {
		int width = getWidth();
		int height = getHeight();
		  
		super.paintComponent(g);
		
		// draw the landscape in greyscale as an image
		if (land.getImage() != null){
			g.drawImage(land.getImage(), 0, 0, null);
		}
		if (water.getImage() != null){
			g.drawImage(water.getImage(), 0, 0, null);
		}
	}
	
	public void run() {	//TODO: Volatile tight loop?
		// display loop here
		// to do: this should be controlled by the GUI
		// to allow stopping and starting
	    repaint();
	}
}