
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import static java.lang.System.exit;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;
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
public class ClientUDPTransmission extends Thread {
    
    private DatagramSocket ds;
    private ClientExchangeProbe exchangeTime;
    private ClientExchangeFile clientExchangeFile;
    private SendACK automaticACK;
    
    
    String banda;
    String filename;

    
       // Vars do file a enviar
       File file;
       byte[] filedata;
       byte[][] dataFragments = null;
       int fragmentsN = 0;
       int windowS = 10;
       int lastConfirmed = 0;
       int nonConfirmed = 0;
       int packetIndex = 0;
       
       
       //Vars da receçao do file
       File newfile;
       DataRecieved dataRecieved = null;
       int pWritten = 0;
       
       
       //int lastSegment = 0;
       
       private void writeToFile(Datagram dataPacket){
        try {
            FileOutputStream fos = clientExchangeFile.getFileOutputStream();
            int nn = dataPacket.dataSize();

            for(int i = PDU.EXTENDED_HEADER_SIZE; i < nn && i < PDU.MAX_SIZE; i++)
                fos.write(dataPacket.data[i]);
            fos.flush();
        } catch (IOException ex) {
            Logger.getLogger(ClientUDPTransmission.class.getName()).log(Level.SEVERE, null, ex);
        }
       }
       
    int fail;   
    boolean failed;
    public ClientUDPTransmission(DatagramSocket ds,ClientExchangeProbe exchangeTime,
            ClientExchangeFile clientExchangeFile, SendACK automaticACK){
        this.ds = ds;
        this.exchangeTime = exchangeTime;
        this.clientExchangeFile = clientExchangeFile;
        this.automaticACK = automaticACK;
        this.dataRecieved = new DataRecieved();
        
        Random rand = new Random();
        fail = rand.nextInt(10);
        failed = false;

    }
    
    public void run(){
    
       byte[] packet = new byte[PDU.MAX_SIZE];
       

       while(true){ 
           
        DatagramPacket dp = new DatagramPacket(packet,PDU.MAX_SIZE);
  
        try{
            ds.receive(dp);
            switch (packet[PDU.TYPE_INDEX]){
                case PDU.PROBE_RESPONSE:{
                      long response_timeStamp = Long.parseLong(PDU.readProbeResponse(packet));
                      exchangeTime.setTime(response_timeStamp);
                    }break;
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
                    filename = "music/"+s[1];
                    windowS = 10; // slow Start
                    nonConfirmed = 0;
                    
                    file = new File(filename);
                    
                    filedata = new byte[(int)file.length()];
                    
                    try {
                        FileInputStream fis = new FileInputStream(filename);
                        fis.read(filedata);
                        System.out.println("Read file");
                        fis.close();
                    }
                    catch(IOException e) {e.printStackTrace();}
  
                    dataFragments = Datagram.fragmentFile(filedata);
                    fragmentsN = dataFragments.length;
                    System.out.println("Number of fragments to send:" + fragmentsN);
                    System.out.println("First packet size:" + dataFragments[0].length);

                    if(true){
                        byte[] fdata = dataFragments[packetIndex++];
                        DatagramPacket fragment = new DatagramPacket(fdata,fdata.length
                                                                        ,dp.getAddress(),dp.getPort());
                    
                        ds.send(fragment);
                    }
                    /*
                    while(windowS > nonConfirmed ){
                        byte[] fdata = dataFragments[packetIndex++];
                        nonConfirmed++;    
                        DatagramPacket fragment = new DatagramPacket(fdata,fdata.length
                                                                        ,dp.getAddress(),dp.getPort());
                    
                        ds.send(fragment);
                    }
                    System.out.println("Sent initial "+ nonConfirmed + "packets");
                    */
                    break;
                case PDU.DATA:
                    Datagram datagram = new Datagram(packet);
                  //  System.out.println("New datagram!");

                    
                   // Reciever side
                    if(datagram.hasData()){
                        int sequence = datagram.seqIndex();
                        
                        if(sequence == pWritten){
                            writeToFile(datagram);
                             if( datagram.moreFragments == 0 ){
                                        // notify Client
                                        System.out.println("Music file transfer over");
                                        clientExchangeFile.getFileOutputStream().close();
                                        exit(1);
                                        
                             } 
                            automaticACK.confirmSegment(pWritten);

                            byte ackP[] = Datagram.makeACK(pWritten);
                            pWritten++;
                            automaticACK.prepare(Datagram.makeRET(pWritten));

                            
                            DatagramPacket ackDP = new DatagramPacket(ackP,ackP.length
                                                                        ,dp.getAddress(),dp.getPort());
                            
                            ds.send(ackDP);
                        } else {
                            byte ackP[] = Datagram.makeRET(pWritten);
                            DatagramPacket ackDP = new DatagramPacket(ackP,ackP.length
                                                                        ,dp.getAddress(),dp.getPort());
                           ds.send(ackDP);
                        }
                        break;
                    }
                    //sender side
                    else if( datagram.isACK()){
                        int ack = datagram.iseq;
                  
                        packetIndex = ack + 1;
                        if( packetIndex == fail && !failed){
                            System.out.println("Failed to transmit packet: " + fail);
                            failed = true;
                        }else{
                          byte[] fdata = dataFragments[packetIndex++];
                          DatagramPacket fragment = new DatagramPacket(fdata,fdata.length
                                                                        ,dp.getAddress(),dp.getPort());
                          ds.send(fragment);
                        }
                    }
                    
                    else if( datagram.isRET()){
                        int ret = datagram.iseq;
                        
                        packetIndex = ret;
                        byte[] fdata = dataFragments[packetIndex++];
                        DatagramPacket fragment = new DatagramPacket(fdata,fdata.length
                                                                        ,dp.getAddress(),dp.getPort());
                        ds.send(fragment);
                    }
                        
                        /*
                        if(dataRecieved.hasSegment(sequence)) {
                            System.out.println("Fragment number:" + sequence +" already on memory");
                            break;
                        }
                        
                        dataRecieved.push(packet, sequence,datagram.dataSize() + PDU.EXTENDED_HEADER_SIZE);
                        
                        int nReady = dataRecieved.getNPackets();
                        
                        System.out.println("Currently needs segment " + pWritten );
                        System.out.println("First on memory " + dataRecieved.firstSeq() );

                        
                        if(nReady > 0 && pWritten == dataRecieved.firstSeq() ){
                            System.out.println("Writting " + nReady + " packets from memory");
                                for(int i = 0; i < nReady;i++){
                                 //   System.out.println("DataRecieved index:" + dataRecieved.index);
                                    Datagram dg = new Datagram(dataRecieved.pop()); 
                                    writeToFile(dg);
                                    //dataRecieved.cleanLastPopped();
                                    
                                    automaticACK.confirmSegment(pWritten);
                                    pWritten++;
                                    
                                    if( datagram.moreFragments == 0 ){
                                        // notify Client
                                        System.out.println("Music file transfer over");
                                    } 
                                    
                                }
                        
                        
                           byte ackP[] = Datagram.makeACK(pWritten);
                           DatagramPacket ackDP = new DatagramPacket(ackP,ackP.length
                                                                        ,dp.getAddress(),dp.getPort());
                           ds.send(ackDP);
                           
                           // Retransmit ACK incase ACK fails
                           automaticACK.prepare(Datagram.makeRET(pWritten));
                        }
                        
                    }
                        */
                    /*  Sender Side
                        read Response
                     */
                      /*
                    if( datagram.isACK()){
                        int ack = datagram.iseq;
                        int confirmed = ack - lastConfirmed;
                        //windowS+= confirmed;
                        nonConfirmed-=confirmed;
                        
                        lastConfirmed = ack;

                        System.out.println("NonConfirmed " + nonConfirmed);
                        System.out.println("Recieved ACK asking for" + ack);
                        
                      while(windowS > nonConfirmed && packetIndex < fragmentsN){
                        byte[] fdata = dataFragments[packetIndex++];
                        DatagramPacket fragment = new DatagramPacket(fdata,fdata.length
                                                                        ,dp.getAddress(),dp.getPort());
                       // sleep(100);
                        ds.send(fragment);
                      }
                        
                    }
                    
                    if( datagram.isRET()){
                        int ret = datagram.iseq;
                        System.out.println("Recieved RET asking for" + ret);
                        
                        packetIndex = ret;
                      while(windowS > nonConfirmed && packetIndex < fragmentsN){
                         byte[] fdata = dataFragments[packetIndex++];
                         DatagramPacket fragment = new DatagramPacket(fdata,fdata.length
                                                                        ,dp.getAddress(),dp.getPort());
                        // sleep(100);
                         ds.send(fragment);
                      } 
                    
                    }
                    */
                    packet = new byte[PDU.MAX_SIZE];
                  break;
                default:
                    break;

            }
            
        }catch(Exception e){ e.printStackTrace(); }
          
         for(int i = 0; i < packet.length;i++) packet[i] = 0;

       }
       
    }
}

class Datagram {
    
    static int DATA = 0, ACK = 1, RET = 2; 
    
    byte type; // packet with data = 0 , retransmit = 1   
    
    byte moreFragments;
    
    byte iseq; // Bytes alocados para a sequenciação em ints de 2 bytes

    
    byte dataSize1,dataSize2,dataSize3,dataSize4;
    
    
    byte[] data;// includes 4 byte header
    
    public Datagram(byte[] d){
        type = d[PDU.FIXED_HEADER_SIZE];
        moreFragments = d[PDU.FIXED_HEADER_SIZE + 1];
        iseq = d[PDU.FIXED_HEADER_SIZE + 2];
        dataSize1 = d[PDU.FIXED_HEADER_SIZE + 3];
        dataSize2 = d[PDU.FIXED_HEADER_SIZE + 4];
        dataSize3 = d[PDU.FIXED_HEADER_SIZE + 5];
        dataSize4 = d[PDU.FIXED_HEADER_SIZE + 6];
        
        data = d;
    }
    
    public static byte[] makeACK(int i){
        byte[] p = new byte[PDU.EXTENDED_HEADER_SIZE];
        p[0] = 0; p[1] = 0;
        p[2] = PDU.DATA; 
        
        p[3] = (byte)ACK;
        p[4] = 1;
        p[5]= (byte)i;
        
        p[6] = 0;
        p[7] = 0;
        p[8] = 0;
        p[9] = 0;
        return p;
    }
    
    public static byte[] makeRET(int i){
        byte[] p = new byte[PDU.EXTENDED_HEADER_SIZE];
        p[0] = 0; p[1] = 0;
        p[2] = PDU.DATA; 
        
        p[3] = (byte)RET;
        p[4] = 1;
        p[5]= (byte)i;
        
        p[6] = 0;
        p[7] = 0;
        p[8] = 0;
        p[9] = 0;
        return p;
    }
    
    public boolean isRET(){
            return (int)type == RET;
    }
    
    public boolean hasData(){ return type == 0;}
    
    public boolean isACK() { 
        return (int)type == ACK;
    }
    
    public int dataSize(){
        byte[] s = new byte[4]; 
        s[0] = dataSize1;s[1] = dataSize2;
        s[2] = dataSize3;s[3] = dataSize4;
        
        return ByteBuffer.wrap(s).getInt();
    }
    
    public boolean lastSegment(){
        return moreFragments == 0;
    }
    // Sequence index
    public int seqIndex(){ 
        return iseq; 
    }
    
  
    
    public static byte[][] fragmentFile(byte[] filedata){
        int dataSize = PDU.MAX_SIZE - PDU.EXTENDED_HEADER_SIZE;
        int n = filedata.length / dataSize; // n fragments
        int d = 0;
        
        byte[][] r = new byte[n][PDU.MAX_SIZE];
        for(int i = 0; i < n ; i++){
            // Fixed Header
                r[i][0] = 0; 
                r[i][1] = 0;
                r[i][2] = PDU.DATA;
            
            //Extended Header
                r[i][3] = (byte) DATA; //data flag
                r[i][4] = (i != n - 1)? (byte)1 : 0; // moreFragments flag
                r[i][5] = (byte)i;   // seq

                int size;                  
                if( i < n-1) size = dataSize;
                    else size = filedata.length - dataSize * i;
                
                byte[] ibytes =  ByteBuffer.allocate(4).putInt(size).array();
                
                    r[i][6] = ibytes[0];
                    r[i][7] = ibytes[1];   
                    r[i][8] = ibytes[2];   
                    r[i][9] = ibytes[3];   
                
                
                for(int j = PDU.EXTENDED_HEADER_SIZE; j < PDU.MAX_SIZE;j++)
                    r[i][j] = filedata[d++];
        }
        
        return r;
    }
}