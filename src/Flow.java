import javax.swing.*;
import javax.swing.event.MouseInputListener;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.BorderLayout;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;

public class Flow{
	static long startTime = 0;
	static int frameX;
	static int frameY;
	static FlowPanel fp;
	static FlowPanel wfp;

	// start timer
	private static void tick(){
		startTime = System.currentTimeMillis();
	}
	
	// stop timer, return time elapsed in seconds
	private static float tock(){
		return (System.currentTimeMillis() - startTime) / 1000.0f; 
	}

	/**
	 * Private innner class for handling mouse events.
	 */
	private static class MouseEventListener implements MouseListener{
		int xClick = -1;
		int yClick = -1;
		Water waterdata;

		MouseEventListener(Water waterdata){
			this.waterdata = waterdata;
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			xClick = e.getX(); //x location of click
			yClick = e.getY(); //y location of click
			waterdata.addWater(xClick,yClick);
		}

		//Empty methods to appease Java gods
		public void mousePressed(MouseEvent e) { }
		public void mouseReleased(MouseEvent e) { }
		public void mouseEntered(MouseEvent e) { }
		public void mouseExited(MouseEvent e) { }
	}
	
	public static void setupGUI(int frameX,int frameY,Terrain landdata, Water waterdata) { //edit
		
		Dimension fsize = new Dimension(800, 800);
    	JFrame frame = new JFrame("Waterflow"); 
    	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	frame.getContentPane().setLayout(new BorderLayout());
    	
      	JPanel g = new JPanel();//add things onto this jpanel
        g.setLayout(new BoxLayout(g, BoxLayout.PAGE_AXIS)); 
   
		fp = new FlowPanel(landdata);
		wfp = new FlowPanel(waterdata); //Water Flow Panel
		fp.setPreferredSize(new Dimension(frameX,frameY));
		g.add(fp);
		g.add(wfp);
		g.addMouseListener(new MouseEventListener(waterdata));
		
		//TODO: adding water overlay here

		//TODO: MouseListener and convert clicked on points to co ords, and send to water

		// to do: add a MouseListener, buttons and ActionListeners on those buttons
	   	
		JButton resetB = new JButton("Reset");
		resetB.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				waterdata.clear();
			}
		});

		JButton pauseB = new JButton("Pause");
		
		JButton playB = new JButton("Play");
		
		JButton endB = new JButton("End");;
		// add the listener to the jbutton to handle the "pressed" event
		endB.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				// to do ask threads to stop
				frame.dispose();
			}
		});

		JPanel b = new JPanel();
	    b.setLayout(new BoxLayout(b, BoxLayout.LINE_AXIS));
		
		b.add(resetB);
		b.add(pauseB);
		b.add(playB);
		b.add(endB);
		g.add(b);
    	
		frame.setSize(frameX, frameY+50);	// a little extra space at the bottom for buttons
      	frame.setLocationRelativeTo(null);  // center window on screen
      	frame.add(g); //add contents to window
        frame.setContentPane(g);
        frame.setVisible(true);
        Thread fpt = new Thread(fp);
        fpt.start();
	}
	
		
	public static void main(String[] args) {
		Terrain landdata = new Terrain();
		Water waterdata;
		
		// check that number of command line arguments is correct
		if(args.length != 1)
		{
			System.out.println("Incorrect number of command line arguments. Should have form: java -jar flow.java intputfilename");
			System.exit(0);
		}
				
		// landscape information from file supplied as argument
		// 
		landdata.readData(args[0]);
		waterdata = new Water(landdata.getDimX(), landdata.getDimY(), landdata);//so a water overlay can be made with same info of Terrain

		frameX = landdata.getDimX();
		frameY = landdata.getDimY();
		SwingUtilities.invokeLater(()->setupGUI(frameX, frameY, landdata, waterdata));
		
		// to do: initialise and start simulation
	}
}
