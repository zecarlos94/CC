
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
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
public class SendRET {
    
    private long time;
    private DatagramSocket ds;
    private InetAddress ip;
    private int port;
    
    Map<Integer,Thread> timeoutJob;
    
    ReentrantLock l = new ReentrantLock();
    
    class AutomaticRET extends Thread{
        byte[] d;
        int ret;
        public AutomaticRET(byte[] d,int ret){
            this.d = d;
            this.ret = ret;
        }
        public void run(){
            try {
                sleep(time);
                       DatagramPacket dp = new DatagramPacket(d,d.length,ip,port);
                        try {
                            ds.send(dp);
                            if(ClientUDPTransmission.SHOW_RET) System.out.println("Sent RET " + ret);
                            Thread reTimeout = new AutomaticRET(d,ret);
                                l.lock();
                                    if(timeoutJob.containsKey(ret)){
                                        timeoutJob.remove(ret);
                                        timeoutJob.put(ret,reTimeout);
                                        reTimeout.start();
                                    }
                                l.unlock();
                            // prepares another AutomaticRET incase this one fails
                           // automaticACK = new AutomaticRET(d);
                           // automaticACK.start();
                        } catch (IOException ex) {
                            Logger.getLogger(SendRET.class.getName()).log(Level.SEVERE, null, ex);
                        }
            } catch (InterruptedException ex) {
               // ACK not needed
            }
        }
    }
    
    
    public void startSendRET(DatagramSocket ds,long OWD,InetAddress ip, int port){
        time = 2*OWD + 3;
        this.ds = ds;
        this.ip = ip;
        this.port = port;
        timeoutJob = new HashMap<Integer,Thread>();
    }
    
    // Recieves the ACK data to send incase time limit is reached
    public void prepare(byte[] packet,int packetIndex){
        Thread automaticACK = new AutomaticRET(packet,packetIndex);
        automaticACK.start();
        l.lock();
        timeoutJob.put(packetIndex,automaticACK);
        l.unlock();
    }
    
    public  void confirmSegment(int confirmedS){
       
        l.lock();
        if( timeoutJob.containsKey(confirmedS)){
            Thread timeout = timeoutJob.get(confirmedS);
            timeout.interrupt();
            timeoutJob.remove(confirmedS);
        }
        l.unlock();
  
    }  
    public long getTime(){return time;}
    
}
