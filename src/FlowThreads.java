import java.util.concurrent.atomic.AtomicBoolean;
/**
 * A Thread class in control of executing the thread-like behaviour of the "play" button in
 * the Flow class.
 * @author Alison Soutar
 */
public class FlowThreads extends Thread{
    Terrain landdata;
    Water waterdata;
    int start, end;
    static AtomicBoolean isRunning = new AtomicBoolean(true);

    public FlowThreads(Terrain landdata, Water waterdata, int start, int end){
        this.landdata = landdata;
        this.waterdata = waterdata;
        this.start = start;
        this.end = end;
        
    }

    public void run() {
        while (isRunning.get() == true) {
            if (Flow.paused.get() == false) {
                waterdata.waterFlow(0, landdata.dim());
            } 
            else {// is paused is true
                try {
                    wait();
                    sleep(20);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.println("Running is now "+isRunning.get()); //Issue: This doesn't die with the frame
    }
}