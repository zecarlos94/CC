/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */



import static com.oracle.jrockit.jfr.ContentType.Timestamp;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.Long.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Gustavo
 */
public class ClientReciever extends Thread {
    
    private InputStream is;
    private OutputStream os;
    private Socket s;
    private String username;
    private String ip;
    private int port;
    private DatagramSocket ds;
    
    //last file request data
    String filename;
    String banda;
    
    public ClientReciever(Socket s,OutputStream os,DatagramSocket ds,String user,String ip,int port) throws IOException{
        this.s=s;
        this.os =os;
        this.is=s.getInputStream();
        this.username = user;
        this.ip = ip;
        this.port = port;
    }
    
    public void run(){
    
        byte[] mensagem = new byte[PDU.MAX_SIZE];
        
        
      
        while( true ){
            
            try {
            
            if(is.read(mensagem) == -1) break;
           
            
            switch(mensagem[PDU.TYPE_INDEX]){
                case PDU.MESSAGE:
                    System.out.println( PDU.readMessage(mensagem) );
                    break;
                case PDU.CONSULT_REQUEST:
                    // Verificar se tem o ficheiro e enviar a resposta
                    
                    String pduCR[] = PDU.readConsultRequest(mensagem);
                    banda = pduCR[0]; 
                    filename = pduCR[1];
                    System.out.println("Recieved CONSULLT_REQUEST: asking for file " + filename);
                    
                    // função para verificar se tem o ficheiro
                    int hasFile = 0;
                    File f = new File("music/" + filename);
                    if(f.length() > 0) hasFile = 1;
                    
                    if(hasFile == 1) System.out.println("I have the file:" + filename);
                    
                    byte[] response = PDU.sendConsultResponse(hasFile, 1 , username, ip, port);
                    os.write(response);
                    os.flush();
                    System.out.println("Sent CONSULTRESPONSE");
                    break;
                case PDU.CONSULT_RESPONSE:
                    String[][] hosts = PDU.readConsultResponse(mensagem);
                    
                    //Probe to find host data with smaller OWD
                    String hostID;
                    int hostPort = 0;
                    InetAddress hostAddress = null;
                    long bestOWD = Long.MAX_VALUE;
                    
                    System.out.println("Recieved CONSULT_RESPONSE with " + hosts.length + " hosts");
                    System.out.println("Probing hosts to find the lowest OWD");
                    
                    for(String[] host : hosts){
                        String id = host[0];
                        String ip = host[1];
                        int port = Integer.parseInt(host[2]);
                        byte probe_message[] = PDU.sendProbeRequest();
                        InetAddress target_address = InetAddress.getByName(ip);
                        DatagramPacket probe_packet = new DatagramPacket(probe_message,probe_message.length , target_address, port);
                        
                        ds.connect(target_address,port);
                        ds.send(probe_packet);
                        long now = System.currentTimeMillis();
                        
                        byte response_data[] = new byte[1024];
                        DatagramPacket probe_response = new DatagramPacket(response_data,response_data.length);
                        ds.receive(probe_response);
                        
                        long response_timeStamp = Long.parseLong(PDU.readProbeResponse(response_data));
                        long OWD = response_timeStamp - now;
                        
                        if(OWD < bestOWD){
                            hostID = id;
                            hostAddress = target_address;
                            hostPort = port;
                            bestOWD = OWD;
                        }
                    }
                    // Send packet REQUEST data to start file transfer
                    
                    System.out.println("Sending Request to start file transfer with user:" + hostAddress);
                    byte[] request_data = PDU.sendRequest(banda, filename);
                    DatagramPacket request = new DatagramPacket(request_data,request_data.length,hostAddress,hostPort);
                    ds.send(request);
                    
                    break;
                default:
                    break;
            }
            
            } catch (IOException ex) {
                Logger.getLogger(ClientReciever.class.getName()).log(Level.SEVERE, null, ex);
            }
              //end recieving cycle
        }
     
    }
    
}
