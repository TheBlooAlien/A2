import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.*;

public class FlowPanel extends JPanel implements Runnable {//TODO inherit from Thread?
	Terrain terrain;
	Water water;
	Object lock = new Object();
	AtomicBoolean paused;
	boolean flowActive = true;
	int loc[] = new int[2];
	
	FlowPanel(Terrain terrain) {
		this.terrain=terrain;
	}

	/**
	 * Method for accessing used Water object.
	 * @return Water object controlling depths and runoffs, ect.
	 */
	public void setWater(Water water){
		this.water = water;
	}

	/**
	 * AtomicBoolean variable controlling paused status, reflecting across all classes.
	 * @param paused AtomicBoolean used in Flow class to control pause/start.
	 */
	void setPauseVar(AtomicBoolean paused){
		this.paused = paused;
	}

	public void setFlowStatus(boolean status){
		flowActive = (status);
	}

	// responsible for painting the terrain and water
	// as images
	@Override
    protected void paintComponent(Graphics g) {
		int width = getWidth();
		int height = getHeight();
		  
		super.paintComponent(g);
		
		// draw the landscape in greyscale as an image
		if(terrain != null){
			if (terrain.getImage() != null){
				g.drawImage(terrain.getImage(), 0, 0, null);
			}
		}
			if(water != null){
			if (water.getImage() != null){
				g.drawImage(water.getImage(), 0, 0, null);
			}
		}
	}
	
	private void allowPause(){
		synchronized(lock) {
			while(paused.get() == true){
				try{
					lock.wait();
				}
				catch(InterruptedException e){
					e.printStackTrace();
				}
			}
		}
	}

	public void unpause(){
		synchronized(lock){
			lock.notifyAll();
		}
	}
	
	

	// TODO: this should be controlled by the GUI
	// to allow stopping and starting
	public void run() {
		while(true){
			try{
				Thread.sleep(50);
				repaint();
			}
			catch (Exception e){
				e.printStackTrace();
			}
		}
	}
}