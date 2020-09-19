import java.util.concurrent.atomic.AtomicBoolean;
/**
 * A Thread class in control of executing the thread-like behaviour of the "play" button in
 * the Flow class.
 * @author Alison Soutar
 */
public class FlowThread extends Thread{
    Terrain landdata;
    Water waterdata;
    int start, end;
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
            if (!(Flow.paused.get())) {
                waterdata.waterFlow(0, landdata.dim());
                done.set(true);//it's done one iteration
            } 
            else {// is paused is true
                try {
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        //System.exit(0);
    }
}