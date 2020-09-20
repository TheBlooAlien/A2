import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
/**
 * A Thread class in control of executing the thread-like behaviour of the "play" button in
 * the Flow class.
 * @author Alison Soutar
 */
public class FlowThread extends Thread{
    Terrain landdata;
    Water waterdata;
    int start, end;
    Object lock = new Object();
    static AtomicBoolean isRunning = new AtomicBoolean(true);
    static AtomicBoolean done = new AtomicBoolean(false); //to see if this thread has done one iteration

    public FlowThread(Terrain landdata, Water waterdata, int start, int end){
        this.landdata = landdata;
        this.waterdata = waterdata;
        this.start = start;
        this.end = end;
        
    }

    public void run() {
        while (isRunning.get()) {
            done.set(false);//still busy
            if (!(Water.paused.get())) {
                waterdata.waterFlow(0, landdata.dim(), lock);
                done.set(true);//it's done one iteration
                
            } 
            else {// is paused is true
                try {
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}