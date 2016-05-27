
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
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
    private ClientExchangeFile fileEx;
    private SendRET automaticRET;
    
    public static boolean SHOW_RET = true;
    public static boolean SHOW_OTHERS = true; // ACK and other not so relevant messages
    

       // Vars do file a enviar
       File file;
       FileInputStream fis;
       Map<Integer,byte[]> fragments;
       int nfragments;
       int fragmentDataSize;
       int lastFragmentSize;
       float windowS;
       boolean threshold = false;
       int packetIndex;
       int balance;
       
       //Vars da receçao do file
       File newfile;
       boolean lastPacketRecieved;
       TreeSet<Integer> packets_recieved = new TreeSet<Integer>();
       TreeSet<Integer> pending_ret = new TreeSet<Integer>();
       
       
       private int missingPacket(){
           Iterator<Integer> it = packets_recieved.iterator();
           int prev = -1;
           while(it.hasNext()){
               Integer i = it.next();
               if( i - 1 == prev) prev = i; 
                else return prev + 1;
           }
           return prev + 1;
       }
       
    private void resetSender(){
       this.fragments = new HashMap<Integer,byte[]>();
       Client.printMenuInicial();
    }   
    private void resetReciever(){
       TreeSet<Integer> packets_recieved = new TreeSet<Integer>();
       TreeSet<Integer> pending_ret = new TreeSet<Integer>();
       Client.printMenuInicial();
    }
    
    int fail;   
    boolean failed;
    public ClientUDPTransmission(DatagramSocket ds,ClientExchangeProbe exchangeTime,
            ClientExchangeFile clientExchangeFile, SendRET automaticACK){
        this.ds = ds;
        this.exchangeTime = exchangeTime;
        this.fileEx = clientExchangeFile;
        this.automaticRET = automaticACK;
        this.fragments = new HashMap<Integer,byte[]>();
        
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
                    String banda = s[0];
                    String filename = "music/"+s[1];
                    windowS = 2; // slow Start
                    balance = (int)windowS;
                    
                    file = new File(filename);
                    
                     long filesize = file.length();
                     fragmentDataSize = PDU.MAX_SIZE - PDU.EXTENDED_HEADER_SIZE;
                     nfragments =  (int)filesize / fragmentDataSize;
                     if( filesize > nfragments*fragmentDataSize ) nfragments++;
                     this.lastFragmentSize = (int)filesize - ((nfragments - 1) * fragmentDataSize );
                      System.out.println("File size " + filesize + " bytes");
                    System.out.println("Number of fragments to send:" + nfragments);
                    System.out.println("Max data on fragment:" +fragmentDataSize);
                    System.out.println("LastFragment size:" +lastFragmentSize);

                    fis = new FileInputStream(filename);
                    
                    for(packetIndex = 0; packetIndex < windowS; packetIndex++){
                        int fragmentSize = packetIndex == (nfragments - 1) ? 
                                           lastFragmentSize + PDU.EXTENDED_HEADER_SIZE 
                                           : fragmentDataSize + PDU.EXTENDED_HEADER_SIZE;  
                        byte fragmentData[] = new byte[fragmentSize];
                        try {
                            fis.read(fragmentData,PDU.EXTENDED_HEADER_SIZE,fragmentSize - PDU.EXTENDED_HEADER_SIZE);
                            fragments.put(packetIndex, fragmentData);
                        } catch(IOException e) {e.printStackTrace();}
                       
                        Datagram.fillDataHeader(fragmentData,packetIndex);
                        
                        DatagramPacket fragment = new DatagramPacket(fragmentData,fragmentData.length
                                                                        ,dp.getAddress(),dp.getPort());
                        ds.send(fragment);
                        balance--;
                    }
                    if(SHOW_OTHERS) System.out.println("Sent inital " + packetIndex + " packets");

                    break;
                case PDU.DATA:
                    Datagram datagram = new Datagram(packet);
        
                    if(datagram.isFIN()){
                        System.out.println("FIN recieved, file transfer over");
                        resetSender();
                    } else
                    
                    // TODO: Create more rejects
                    // Reciever side
                    if(datagram.hasData()){
                        int sequence = datagram.seqIndex();
                        
                        if(packets_recieved.contains(sequence)) 
                            break; // ignores repeated packet
                         else packets_recieved.add(sequence);
                        
                        
                        
                             fileEx.wb.add(sequence, packet);
                    
                         if( datagram.moreFragments == 0 ){
                                    // notify Client
                                    if(SHOW_OTHERS) System.out.println("Lastpacket recieved");
                                    lastPacketRecieved = true;
                                    
                                    // Disable forgotten timeouts
                                    for(int packet_ret : pending_ret){
                                        if( packets_recieved.contains(packet_ret ) || packet_ret > sequence)
                                            automaticRET.confirmSegment(packet_ret); 
                                    }
                                    // find missing packets and prepare Timeouts
                                    for(int i = missingPacket(); i < sequence;i++)
                                        if(!packets_recieved.contains(i) && !pending_ret.contains(i))
                                              automaticRET.prepare(Datagram.makeRET(i),i);
                         } else {     
                             class UpdateBuffer extends Thread{
                                 public void run(){fileEx.wb.update();}
                             }

                             if(pending_ret.contains(sequence)){
                                automaticRET.confirmSegment(sequence);
                                pending_ret.remove(sequence);
                             }
                             new UpdateBuffer().start();

                             // add more timeouts if needed
                             int maxRej = !lastPacketRecieved ? sequence + 5 : sequence;
                             
                             for(int i = missingPacket(); i < maxRej;i++)
                                 if(!packets_recieved.contains(i) && !pending_ret.contains(i)){
                                      automaticRET.prepare(Datagram.makeRET(i),i);
                                      pending_ret.add(i);
                                 }
                             
                             if(SHOW_OTHERS) System.out.println("Sending selective ACK packet " + sequence);
                             // Packet ack
                             byte ackP[] = Datagram.makeACK(sequence);
                             DatagramPacket ackDP = new DatagramPacket(ackP,ackP.length
                                                                        ,dp.getAddress(),dp.getPort());
                             try { ds.send(ackDP); } catch (IOException ex) {}
                             
                        }
                         if(SHOW_OTHERS) System.out.println("Confirmed " + packets_recieved.size() + " packets");
                         // every packet is confirmed
                        if( lastPacketRecieved && packets_recieved.last() == packets_recieved.size() - 1){
                              System.out.println("Every packet is confirmed, waiting for threads writing on file");
                              //Disable missing timeouts
                              for(int packet_ret : pending_ret){
                                        if( packets_recieved.contains(packet_ret ) || packet_ret > sequence)
                                            automaticRET.confirmSegment(packet_ret); 
                                    }
                              fileEx.wb.waitBuffer();
                              
                              byte fin[] = Datagram.makeFIN();
                              DatagramPacket finP = new DatagramPacket(fin,fin.length
                                                                        ,dp.getAddress(),dp.getPort());
                              try { ds.send(finP); } catch (IOException ex) {}
                              
                              System.out.println("Sent FIN");
                              System.out.println("File transfer over");
                              resetReciever();
                        }
                        break;
                    }  //sender side
                    else if( datagram.isACK()){
                        int ack = datagram.iseq;
                  
                        balance++;
                        balance+= threshold ? 0.25f : 1;
                        windowS+= threshold ? 0.25f : 1;
                        System.out.println("Recieved ACK for packet " + ack);
                        // remove packet from memomry
                        fragments.remove(ack);

                        for(; (balance > 0 ) && (packetIndex < nfragments) ; packetIndex++){
    
                            int fragmentSize = packetIndex == (nfragments - 1) ? 
                                               lastFragmentSize + PDU.EXTENDED_HEADER_SIZE 
                                               : fragmentDataSize + PDU.EXTENDED_HEADER_SIZE;  
                            byte fragmentData[] = new byte[fragmentSize];
                            try {
                                fis.read(fragmentData,PDU.EXTENDED_HEADER_SIZE,fragmentSize - PDU.EXTENDED_HEADER_SIZE);
                                fragments.put(packetIndex, fragmentData);
                            } catch(IOException e) {e.printStackTrace();}

                            Datagram.fillDataHeader(fragmentData,packetIndex);
                             //MoreFragmentsFlag set to 0
                            if(packetIndex == nfragments - 1) 
                                fragmentData[PDU.FIXED_HEADER_SIZE + 1] = 0;

                            DatagramPacket fragment = new DatagramPacket(fragmentData,fragmentData.length
                                                                            ,dp.getAddress(),dp.getPort());
                            ds.send(fragment);
                            balance--;
                            if(SHOW_OTHERS) System.out.println("Sent packet " + packetIndex);
                        }
                        
                    } 
                    else if( datagram.isRET()){
                        int ret = datagram.iseq;
                        
                        if(!threshold){
                            threshold = true;
                            int rupture = (int)windowS / 2;
                            balance-=rupture; windowS-=rupture; 
                        }
                        if(SHOW_RET) System.out.println("RET: " + ret +" Recieved Selective Reject ");
                        
                        if( fragments.containsKey(ret) && ret < nfragments){
                            byte data[] = fragments.get(ret);
                            Datagram.fillDataHeader(data,ret);
                            DatagramPacket fragment = new DatagramPacket(data,data.length
                                                                     ,dp.getAddress(),dp.getPort());
                            ds.send(fragment);
                            if(SHOW_RET) System.out.println("Resent " + ret);
                        } else { 
                            if(SHOW_RET) System.out.println("Packet: " + ret +" Not loaded");}
                    }       
               
                    packet = new byte[PDU.MAX_SIZE];
                  break;
                default:
                    break;

            }
            
        }catch(Exception e){ e.printStackTrace(); }
          
       //  for(int i = 0; i < packet.length;i++) packet[i] = 0;

       }
       
    }
}

class Datagram {
    
    static int DATA = 0, ACK = 1, RET = 2, FIN = 3; 
    
    byte type; // packet with data = 0 , retransmit = 1   
    
    byte moreFragments;
    
    byte iseq; // Bytes alocados para a sequenciação em ints de 2 bytes

    byte[] data;// includes 4 byte header
    
    public Datagram(byte[] d){
        type = d[PDU.FIXED_HEADER_SIZE];
        moreFragments = d[PDU.FIXED_HEADER_SIZE + 1];
        iseq = d[PDU.FIXED_HEADER_SIZE + 2];
        
        data = d;
    }
    
    public static void fillDataHeader(byte p[],int seq){
        p[0] = 0; p[1] = 0;
        p[2] = PDU.DATA; 
        
        p[3] = (byte)DATA;
        p[4] = 1;
        p[5]= (byte)seq;
         
    }
    
    public static byte[] makeACK(int i){
        byte[] p = new byte[PDU.EXTENDED_HEADER_SIZE];
        p[0] = 0; p[1] = 0;
        p[2] = PDU.DATA; 
        
        p[3] = (byte)ACK;
        p[4] = 1;
        p[5]= (byte)i;
      
        return p;
    }
    
    public static byte[] makeRET(int i){
        byte[] p = new byte[PDU.EXTENDED_HEADER_SIZE];
        p[0] = 0; p[1] = 0;
        p[2] = PDU.DATA; 
        
        p[3] = (byte)RET;
        p[4] = 1;
        p[5]= (byte)i;
        
        return p;
    }
    
    public static byte[] makeFIN(){
        byte[] p = new byte[PDU.EXTENDED_HEADER_SIZE];
        p[0] = 0; p[1] = 0;
        p[2] = PDU.DATA; 
        
        p[3] = (byte)FIN;
        p[4] = 1;
        p[5]= 0;
        return p;
    }
    
    public boolean isFIN(){ return FIN == (int)type;}
    
    public boolean isRET(){
            return (int)type == RET;
    }
    
    public boolean hasData(){ return (int)type == DATA;}
    
    public boolean isACK() { 
        return (int)type == ACK;
    }
    
    
    public boolean lastSegment(){
        return moreFragments == 0;
    }
    // Sequence index
    public int seqIndex(){ 
        return iseq; 
    }
    
}