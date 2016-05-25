
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
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
public class SendACK {
    
    private long time;
    private int ask;  // segment to ask 
    private DatagramSocket ds;
    private InetAddress ip;
    private int port;
    Thread automaticACK;
    
    class AutomaticACK extends Thread{
        byte[] d;
        public AutomaticACK(byte[] d){
            this.d = d;
        }
        public void run(){
            try {
                sleep(time);
                       DatagramPacket dp = new DatagramPacket(d,d.length,ip,port);
                        try {
                            ds.send(dp);
                            // prepares another AutomaticACK incase this one fails
                            automaticACK = new AutomaticACK(d);
                            automaticACK.start();
                        } catch (IOException ex) {
                            Logger.getLogger(SendACK.class.getName()).log(Level.SEVERE, null, ex);
                        }
            } catch (InterruptedException ex) {
               // ACK not needed
            }
        }
    }
    
    
    public void startSendACK(DatagramSocket ds,long OWD,InetAddress ip, int port){
        time = 4*OWD + 1000;
        ask = 0;
        this.ds = ds;
        this.ip = ip;
        this.port = port;
    }
    
    // Recieves the ACK data to send incase time limit is reached
    public void prepare(byte[] packet){
        if(automaticACK != null && automaticACK.isAlive()) automaticACK.interrupt();
        Thread automaticACK = new AutomaticACK(packet);
        automaticACK.start();
    }
    
    public void confirmSegment(int confirmedS){
        ask = confirmedS + 1;
        if(automaticACK != null && automaticACK.isAlive()) automaticACK.interrupt();
    }
    
    
}
