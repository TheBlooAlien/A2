import javax.swing.*;
import javax.swing.event.MouseInputListener;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.BorderLayout;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.concurrent.atomic.AtomicBoolean;
import java.awt.event.MouseEvent;

public class Flow {
	static long startTime = 0;
	static int frameX;
	static int frameY;
	static AtomicBoolean paused = new AtomicBoolean(false);
	static FlowPanel fp;
	static FlowPanel wfp;
	static int flattenedMap[];

	// start timer
	private static void tick() {
		startTime = System.currentTimeMillis();
	}

	// stop timer, return time elapsed in seconds
	private static float tock() {
		return (System.currentTimeMillis() - startTime) / 1000.0f;
	}

	/**
	 * Private innner class for handling mouse events.
	 */
	private static class MouseEventListener implements MouseListener {
		int xClick = -1;
		int yClick = -1;
		Water waterdata;

		MouseEventListener(FlowPanel fp, Water waterdata) {
			this.waterdata = waterdata;
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			xClick = e.getX(); // x location of click
			yClick = e.getY(); // y location of click
			waterdata.addWater(xClick, yClick);
			fp.repaint(); // remove when threadding
		}

		// Empty methods to appease Java gods
		public void mousePressed(MouseEvent e) {
		}

		public void mouseReleased(MouseEvent e) {
		}

		public void mouseEntered(MouseEvent e) {
		}

		public void mouseExited(MouseEvent e) {
		}
	}

	public static void setupGUI(int frameX, int frameY, Terrain landdata, Water waterdata) { // edit

		Dimension fsize = new Dimension(800, 800);
		JFrame frame = new JFrame("Waterflow");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new BorderLayout());

		JPanel panel1 = new JPanel();
		panel1.setLayout(new BoxLayout(panel1, BoxLayout.PAGE_AXIS));

		fp = new FlowPanel(landdata);
		fp.addMouseListener(new MouseEventListener(fp, waterdata));
		fp.setLayout(new BorderLayout());
		fp.setPreferredSize(new Dimension(frameX, frameY));
		fp.setWater(waterdata);
		fp.setPauseVar(paused);

		Thread fpt = new Thread(fp);// make 4, and split permuted array into them
		fpt.start();

		panel1.add(fp);

		// Making the buttons
		JButton resetB = new JButton("Reset");
		resetB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				waterdata.clear();
				fp.repaint();
			}
		});

		JButton pauseB = new JButton("Pause");
		pauseB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				(new Thread() {
					public void run() {
						try {
							paused.set(true);
							Thread.sleep(50);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}).start();

			}
		});

		FlowThread rThread1 = new FlowThread(landdata, waterdata, 0, landdata.dim() / 4);// first quarter
		FlowThread rThread2 = new FlowThread(landdata, waterdata, landdata.dim() / 4, landdata.dim() / 2);// second
																											// quarter
		FlowThread rThread3 = new FlowThread(landdata, waterdata, landdata.dim() / 2,
				landdata.dim() / 2 + landdata.dim() / 4); // third quarter
		FlowThread rThread4 = new FlowThread(landdata, waterdata, landdata.dim() / 2 + landdata.dim() / 4,
				landdata.dim());

		JButton playB = new JButton("Play");
		playB.addActionListener(new ActionListener() {
			boolean started = false;

			public void actionPerformed(ActionEvent e) {
				if (started == false) {
					rThread1.start();
					rThread2.start();
					rThread3.start();
					rThread4.start();
					started = true;
				} else {
					paused.set(false);// if they clicked on this button, it's evaluated to false again
				}
			}
		});

		JButton endB = new JButton("End");
		// add the listener to the jbutton to handle the "pressed" event
		endB.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// to do ask threads to stop
				fp.setFlowStatus(false); // so FlowPanel knows that the simulation is over
				rThread1.isRunning.set(false);
				rThread2.isRunning.set(false);
				rThread3.isRunning.set(false);
				rThread4.isRunning.set(false);
				frame.dispose();
				System.exit(0);
			}
		});

		JLabel counterLabel = new JLabel(); // okay this doesn't work. maybe put a JPanel over flowPanel and update
											// that, it's also in a gross location

		(new Thread() {
			int counter = 0;

			public void run() {
				while (true) {
					if (rThread1.done.get() & rThread2.done.get() & rThread3.done.get() & rThread4.done.get()) {
						counterLabel.setText(counter+"");
						counter++;//make atomic counter and update on each thread. This isn't accurate.
					}
				}
			}
		}).start();

		JPanel panelButton = new JPanel();
		panelButton.setLayout(new BoxLayout(panelButton, BoxLayout.LINE_AXIS));

		panelButton.add(resetB);
		panelButton.add(pauseB);
		panelButton.add(playB);
		panelButton.add(endB);
		panelButton.add(counterLabel);
		panel1.add(panelButton);

		frame.setSize(frameX, frameY + 50); // a little extra space at the bottom for buttons
		frame.setLocationRelativeTo(null); // center window on screen
		frame.add(panel1); // add contents to window
		frame.setContentPane(panel1);
		frame.setVisible(true);

	}

	public static void main(String[] args) {
		Terrain landdata = new Terrain();
		Water waterdata;

		// check that number of command line arguments is correct
		if (args.length != 1) {
			System.out.println(
					"Incorrect number of command line arguments. Should have form: java -jar flow.java intputfilename");
			System.exit(0);
		}

		// landscape information from file supplied as argument
		landdata.readData(args[0]);
		waterdata = new Water(landdata.getDimX(), landdata.getDimY(), landdata);// so a water overlay can be made with
																				// same info of Terrain

		frameX = landdata.getDimX();
		frameY = landdata.getDimY();
		SwingUtilities.invokeLater(() -> setupGUI(frameX, frameY, landdata, waterdata));

		// TODO: initialise and start simulation
	}
}
