
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;
import java.util.Collection;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Gustavo
 */
public class ClientUDPTransmission extends Thread {
    
    private DatagramSocket ds;
    
    String banda;
    String filename;
    
    public ClientUDPTransmission(DatagramSocket ds){
        this.ds = ds;
    }
    
    public void run(){
    
       byte[] packet = new byte[PDU.MAX_SIZE];
       
       // Vars do file a enviar
       File file;
       byte[] filedata;
       
       //Vars da receçao do file
       File newfile;
       int lastSegment = 0;
       
       while(true){ 
           
        DatagramPacket dp = new DatagramPacket(packet,PDU.MAX_SIZE);
  
        try{
            ds.receive(dp);
            switch (packet[PDU.TYPE_INDEX]){
                case PDU.PROBE_REQUEST:
                    System.out.println("Probed by client ip:" + dp.getAddress().getHostAddress() + "on port:" + dp.getPort() );
                    long timestamp = System.currentTimeMillis();
                    byte[] response_data = PDU.sendProbeResponse(timestamp + "");
                    
                    DatagramPacket probe_response = new DatagramPacket(response_data,response_data.length
                                                                        ,dp.getAddress(),dp.getPort());
                    
                    ds.send(probe_response);
                    break;
                case PDU.REQUEST:    
                    String[] s = PDU.readRequest(packet);
                    banda = s[0];
                    filename = s[1];
                    
                    file = new File(filename);
                    
                    filedata = new byte[(int)file.length()];
                    
                    try {
                        FileInputStream fis = new FileInputStream("music/" + filename);
                        fis.read(filedata);
                        fis.close();
                    }
                    catch(IOException e) {e.printStackTrace();}
  
                    byte[][] dataFragments = Datagram.fragmentFile(filedata);
                    
                    for(byte[] fdata : dataFragments){
                        DatagramPacket fragment = new DatagramPacket(fdata,fdata.length
                                                                        ,dp.getAddress(),dp.getPort());
                    
                        ds.send(fragment);
                    }
                    
                    break;
                case PDU.DATA:
                    Datagram datagram = new Datagram(packet);
                    if(datagram.seqIndex() == 0){
                        newfile = new File("music/" + filename);
                    }
                    
                    if(datagram.hasData()){
                        int i = datagram.seqIndex();
                        
                    }
            }
            
        }catch(Exception e){ e.printStackTrace(); }
          
         for(int i = 0; i < packet.length;i++) packet[i] = 0;

       }
       
    }
}

class Datagram {
    
    byte type; // packet with data = 0 , retransmit = 1   
    
    byte moreFragments;
    
    byte i1,i2; // Bytes alocados para a sequenciação em ints de 2 bytes
    
    byte[] data;// includes 4 byte header
    
    public Datagram(byte[] d){
        type = d[0];
        moreFragments = d[1];
        i1 = d[2];i2 = d[3];
        data = d;
    }
    
    public boolean hasData(){ return type == 0;}
    
    public boolean lastSegment(){
        return moreFragments == 0;
    }
    // Sequence index
    public int seqIndex(){ 
        return i2; // a usar so 1 byte de seq
    }
    
    public static byte[][] fragmentFile(byte[] filedata){
        int dataSize = PDU.MAX_SIZE - PDU.EXTENDED_HEADER_SIZE;
        int n = filedata.length / dataSize; // n fragments
        
        byte[][] r = new byte[n][PDU.MAX_SIZE];
        for(int i = 0; i < n ; i++){
            // Fixed Header
                r[i][0] = 0; r[i][1] = 0;
                r[i][2] = PDU.DATA;
            
            //Extended Header
                r[i][4] = 0; //data flag
                r[i][5] = (i != n - 1)? (byte)1 : 0; // moreFragments flag
                // seq
                if( i < 256 ) { r[i][6] = 0; r[i][7] = (byte)i;}
                    else{
                        // dividir o int em 2 bytes
                    }
                
                r[i] = Arrays.copyOfRange(filedata, i * dataSize, (i+1) * dataSize);
        }
        
        return r;
    }
}