
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Gustavo
 */
public class HostsResponse {
    
    private Vector<String[]> hosts;
    private int responses;
    private int nHosts;
    
    
    public HostsResponse(int nHosts){
        this.nHosts = nHosts;
        responses = 0;
        hosts = new Vector<String[]>();
    }
    public synchronized Vector<String[]> getHosts(){ return hosts;}
            
    /*
      Recieves null when the host doesnt have the file
    */
    public synchronized void addHost(String[] host){
        if(host!=null) hosts.add(host); 
        responses++;
        if(responses == nHosts)
            notify();
    }
    
    /* Client's UserThread which is looking for hosts waits here */
    public synchronized void waitForHosts(){
        while(responses < nHosts)
            try {
                wait();
            } catch (InterruptedException ex) {
                Logger.getLogger(HostsResponse.class.getName()).log(Level.SEVERE, null, ex);
            }
    }
    
    
    
}
