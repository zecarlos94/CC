
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * Shared memory between ClientReciever Thread and ClientUDPTransmission
 * Shares probeResponse TimeStamp
 * @author Gustavo
 */
public class ClientExchangeProbe {
    private long time;
    private boolean actualised;
    
    public ClientExchangeProbe(){
        time = 0; actualised = false;
    }

    public synchronized void setTime(long time){ 
        this.time = time;
        actualised = true;
        notify();
    }
    public synchronized long getTime(){ 
        while(!actualised){         
            try {
                wait();
            } catch (InterruptedException ex) {
                Logger.getLogger(ClientExchangeProbe.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        long r = time;
        //reset
        time = 0; actualised = false;
        return r;
    
    }
}
