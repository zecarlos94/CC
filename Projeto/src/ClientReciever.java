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
import static java.lang.System.exit;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Vector;


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
    private ClientExchangeProbe exchangeTime;
    private ClientExchangeFile clientExchangeFile;
    private SendRET automaticACK;
    
    //last file request data
    String filename;
    String banda;
    
    public ClientReciever(Socket s,OutputStream os,DatagramSocket ds,String user,String ip,int port,
            ClientExchangeProbe exchangeTime,ClientExchangeFile clientExchangeFile,SendRET automaticACK) throws IOException{
        this.s=s;
        this.os =os;
        this.is=s.getInputStream();
        this.username = user;
        this.ip = ip;
        this.port = port;
        this.ds = ds;
        this.exchangeTime = exchangeTime;
        this.clientExchangeFile = clientExchangeFile;
        this.automaticACK = automaticACK;
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
                    
                    File dir = new File(username);
                    if(dir.exists() && new File(dir,filename).exists())
                        hasFile = 1;
                            /*
                    File f = new File(username + "/" + filename);
                    if(f.length() > 0) hasFile = 1;
                    */
                    if(hasFile == 1) System.out.println("I have the file:" + filename);
                    else System.out.println("I dont have the file:" + filename);
                    
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
                    
                    if(hosts == null || hosts.length == 0){
                        System.out.println("Noone has the music request");
                        exit(1);
                    }

                    System.out.println("Recieved CONSULT_RESPONSE with " + hosts.length + " hosts");
                    System.out.println("Probing hosts to find the lowest OWD");

                    for(String[] host : hosts){
                        String targetId = host[0];
                        String targetIp = host[1];
                        int targetPort = Integer.parseInt(host[2]);
                        byte probe_message[] = PDU.sendProbeRequest();
                        InetAddress target_address = InetAddress.getByName(targetIp);
                        String ipTeste = target_address.getHostAddress();
                        System.out.println("Probing user " + targetId + " with ip:" + targetIp + " on port:" + targetPort);
                        DatagramPacket probe_packet = new DatagramPacket(probe_message,probe_message.length , target_address, targetPort);
                      
                        long now = System.currentTimeMillis();
                        Vector<Long> l = new Vector<Long>();
                        //Prepare thread to recieve response before sending
                        class readResponseJob extends Thread{
                            private ClientExchangeProbe exchangeTime;
                            Vector<Long> response_timeStamp;
                            public readResponseJob(ClientExchangeProbe exchangeTime,Vector<Long> response_timeStamp){
                                this.exchangeTime = exchangeTime;
                                this.response_timeStamp = response_timeStamp;
                            }
                            public void run(){
                                long t = exchangeTime.getTime();
                                response_timeStamp.add(new Long(t));
                                System.out.println("ResponseTime: " + t);
                            }   
                        }

                        Thread readResponse = new readResponseJob(exchangeTime,l);        
                        readResponse.start();                      
                        
                        ds.send(probe_packet);
                         try {
                               readResponse.join(500);
                               System.out.println("DatagramPacket successfull load");
                            } catch (InterruptedException ex) {
                               Logger.getLogger(ClientReciever.class.getName()).log(Level.SEVERE, null, ex);
                        }
                         
                        long response_timeStamp = l.get(0);
                        long OWD = response_timeStamp - now;
                        System.out.println("Client probed, OWD =" + OWD);
                        if(OWD < bestOWD){
                            hostID = targetId;
                            hostAddress = target_address;
                            hostPort = targetPort;
                            bestOWD = OWD;
                        }
                    }
                    // Send packet REQUEST data to start file transfer
                    
                    System.out.println("Sending Request to start file " + clientExchangeFile.getFilename() + " transfer with user addrss: " + hostAddress);
                    byte[] request_data = PDU.sendRequest(banda, clientExchangeFile.getFilename());
                    DatagramPacket request = new DatagramPacket(request_data,request_data.length,hostAddress,hostPort);
                    ds.send(request);
                    
                    clientExchangeFile.setOWD(bestOWD);
                    automaticACK.startSendRET(ds, bestOWD, hostAddress, hostPort);
                    
                    break;
                default:
                    break;
            }
            
            } catch (IOException ex) {
                Logger.getLogger(ClientReciever.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            //clean buffer
            for(int i = 0; i < mensagem.length;i++) mensagem[i] = 0;
              //end recieving cycle
        }
     
    }
    
}
