import javax.swing.*;
//https://www.bitinvert.com/java/pause-resume/

public class PauseResume {
    private JFrame frame = new JFrame("PauseResume");
    private JButton button = new JButton("Start");
    private JTextArea textArea = new JTextArea(5,20);
    
    private Object lock = new Object();
    private volatile boolean paused = true;
    
    public static void main(String[] args) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                new PauseResume();
            }
        });
    }
    
    public PauseResume() {
        counter.start();
        button.addActionListener(pauseResume);
        
        textArea.setLineWrap(true);
        frame.add(button,java.awt.BorderLayout.NORTH);
        frame.add(textArea,java.awt.BorderLayout.SOUTH);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
    
    private Thread counter = new Thread(new Runnable() {
        @Override
        public void run() {
            while(true) {
                work();
            }
        }
    });
    
    private void work() { //FP "Run"
        for(int i = 0;i < 10;i++) {
            allowPause();
            write(Integer.toString(i));
            sleep();
        }
        done();
    }
    
    private void allowPause() {
        synchronized(lock) {
            while(paused) {
                try {
                    lock.wait();
                } catch(InterruptedException e) {
                    // nothing
                }
            }
        }
    }
    
    private java.awt.event.ActionListener pauseResume =
        new java.awt.event.ActionListener() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                    paused = !paused;
                    button.setText(paused?"Resume":"Pause");
                synchronized(lock) {
                    lock.notifyAll();
                }
            }
        };
    
    private void sleep() {
        try {
            Thread.sleep(500);
        } catch(InterruptedException e) {
            // nothing
        }
    }
    
    private void done() {
        button.setText("Start");
        paused = true;
    }
    
    public void write(String str) {
        textArea.append(str);
    }
}