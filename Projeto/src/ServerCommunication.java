
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.ClosedByInterruptException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
 public class ServerCommunication extends Thread{
       Socket socket;
       OutputStream os;
       InputStream is;
       InetAddress local;
       String myIP;
       int myPort;
       
       Server server;
       
       class ServerContactInfo{
           int id;
           String ip;
           int port;
           public ServerContactInfo(int id,String ip, int port){
               this.id = id; this.ip = ip; this.port = port;
           }
       }
       Map<Integer,ServerContactInfo> serverList;
       
       class Host{
           String username; String ip; int port;
           public Host(String[] host){
               username = host[0]; ip = host[1]; 
               port = Integer.parseInt(host[2]);
           }
       }
       
       class HostsRecieved{
          private List<Host> fileRecieved;
          private int N;
          public HostsRecieved(int N){
              this.N = N; fileRecieved = new ArrayList<Host>();
          }
          
          public synchronized void addHost(Host h){
              if(h!=null) fileRecieved.add(h);
              else N--;
              if(N ==  fileRecieved.size()) notify();
          }

          public synchronized void waitResponses(){
              while(fileRecieved.size() < N)
                  try{ wait(); } catch(InterruptedException e) {}
          }
          public Vector<String[]> getHosts(){
              Vector<String[]> r = new Vector<String[]>();
              for(Host h : fileRecieved){
                  String d[] = new String[3];
                  d[0] = h.username; d[1] = h.ip; d[2] = h.port +""; 
                  r.add(d);
              }
              return r;
          }
       }
       
       public ServerCommunication(Server server){
        try{
         this.socket = new Socket("localhost", MusicManager.CENTRAL_SERVER_PORT );
         os = socket.getOutputStream();
         is = socket.getInputStream();
         local = socket.getInetAddress();
         myIP = local.getHostAddress();
         myPort = socket.getLocalPort();
         serverList = new HashMap<Integer,ServerContactInfo>();
         this.server = server;
        }catch(Exception e){}
        
        
       }
       
       public void reg(){
         try{
          byte[] pdu2 = PDU.sendRegPDU( 1 , "server", "nopw", myIP, server.serverConPort);
          os.write(pdu2);
          os.flush(); 
         }catch(Exception e){}
       }
       
       public void unreg(){
         try{
          byte[] pdu2 = PDU.sendRegPDU( 0 , "server", "nopw", myIP, server.serverConPort);
          os.write(pdu2);
          os.flush(); 
         }catch(Exception e){}
       }
       
       class ServerToServer extends Thread{
           Socket socket;
           OutputStream os;InputStream is;
           HostsRecieved hostsR;
           public ServerToServer(Socket s,HostsRecieved h) throws IOException{ 
            this.socket = s;
            os = s.getOutputStream(); is = s.getInputStream();
            hostsR = h;
           }
           
           public void run(){
              byte[] mensagem = new byte[PDU.MAX_SIZE];

                try {
                if(is.read(mensagem) == -1) return;

                switch(mensagem[PDU.TYPE_INDEX]){
                    case PDU.CONSULT_RESPONSE:
                        System.out.println("Recieved Server CONSULT_RESPONSE");
                        String[][] hosts = PDU.readConsultResponse(mensagem);

                        if(hosts.length == 0) {
                            hostsR.addHost(null);
                            break;
                        }
                        
                        for(String[] host : hosts)
                            hostsR.addHost(new Host(host));
                        
                        break;
                    case PDU.CONSULT_REQUEST:
                        // Look for file on serverHost
                        System.out.println("Recieved Server CONSULT_REQUEST");
                        String[] fileRequest = PDU.readConsultRequest(mensagem);
                        Vector<String[]> hostsData = server.findHosts(fileRequest[0],fileRequest[1],null);
                        
                        String fhosts[][] = new String[hostsData.size()][3];
                        for(int i = 0; i < fhosts.length;i++)
                            fhosts[i] = hostsData.get(i);

                        int tipo = hostsData.size() == 0 ? 0 : 1;
                        
                        try{
                          byte[] pdu2 = PDU.sendConsultResponse(tipo, hostsData.size(), fhosts);
                          os.write(pdu2);
                          os.flush(); 
                         }catch(Exception e){}
                        break;
                    default: break;
                    }

                } catch(ClosedByInterruptException e){ 
                    try {
                        os.close();is.close();
                    } catch (IOException ex) { }
                }
                  catch(Exception e) {
                } 
         }
           
       }
       
       public Vector<String[]> sendConsultRequest(String banda,String filename,long ABORT_TIME){
           System.out.println("Sending consultRequest to other " + serverList.size() + " servers");
         HostsRecieved hr = new HostsRecieved(serverList.size());  
         ArrayList<Thread> serverconnections = new ArrayList<>();
         for(ServerContactInfo targetServer : serverList.values())  
          try{
              System.out.println("Connecting to server port " + targetServer.port);
           Socket ss = new Socket("localhost",targetServer.port);
           OutputStream sos = ss.getOutputStream();
           // thread recieves hosts data from each server 
           Thread con = new ServerToServer(ss, hr);
           con.start(); serverconnections.add(con);
           
           
           byte[] pdu2 = PDU.sendConsultRequest(banda, filename);
           sos.write(pdu2);
           sos.flush();
           
           con.join();
          }catch(Exception e){}
         /*
         class WaitResponse extends Thread{
            public void run(){hr.waitResponses();}
         }
         Thread wr = new WaitResponse(); wr.start();
         try { wr.join(); } catch (InterruptedException ex) {}
         
         for(Thread t : serverconnections) {
           if(t.isAlive()) t.interrupt(); // terminates tcp connection thread
         }
                 */
         Vector<String[]> v = hr.getHosts();
         System.out.println( v.size() + " hosts found");
         return hr.getHosts();
       }
       
       public void run(){
       
          byte[] mensagem = new byte[PDU.MAX_SIZE];
         
           ServerSocket interServers = null;
           try{interServers = new ServerSocket(server.serverConPort); } catch(Exception e){ }

           class AceptServerConnects extends Thread{
              private ServerSocket interServers;
              public AceptServerConnects(ServerSocket ss){ 
                  this.interServers = ss;
              }
              public void run(){
                  Socket      socket;
                  try{
                      while ((socket = interServers.accept()) != null) {
                        System.out.println("Conecçao entre Servidores aceite");
                        new ServerToServer(socket,null).start();
                      }
                  } catch(Exception e){}
              }
          }
           // Handles connections with other servers
          new AceptServerConnects(interServers).start();
          
          while( true ){
              //Handles messages with central Server
            try {
            
            if(is.read(mensagem) == -1) break;
           
            switch(mensagem[PDU.TYPE_INDEX]){  
                    //recieve side
                case PDU.REGISTER: // Used to add or rem a server from active serverList
                    System.out.println("Recieved Server REGISTER");

                    String[] s = PDU.readRegPDU(mensagem);
                    String id = s[1];
                    int tipo = Integer.parseInt(s[0]);
              
                    if(tipo == 0){
                      System.out.println("Removed server " + id + " from active list");
                      serverList.remove(Integer.parseInt(id));
                    } else { 
                        System.out.println("Added server " + id + " to active list");
                        int sid = Integer.parseInt(id);
                        ServerContactInfo si = new ServerContactInfo(sid,s[3],Integer.parseInt(s[4]));
                        serverList.put(sid,si);
                    }
                    break;        
                default: 
                    break;
            }
            
            } catch(Exception e){}
       }
       
     }
   }
     