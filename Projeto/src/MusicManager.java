
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Central server
 *  Manages servers activity
*/
class MusicManager{
    
  public static int CENTRAL_SERVER_PORT = 4545;
    //public String CENTRAL_SERVER_IP;
    
  class ServerActivity{
      boolean connected = true;
      Socket socket; // central - server socket
      String serverIP;
      int serverPort;
      public ServerActivity(Socket s,String ip,int p){ 
          this.socket = s;
          serverIP = ip; serverPort = p;
      }
  }
  
  class ServerManegement{
     private Map<Integer,ServerActivity> activity = new HashMap<>();
     private int serverID = 0;  
     private ServerSocket server;
     
     public Map<Integer,ServerActivity> allActive(){
         Map<Integer,ServerActivity> active = new HashMap<>();
         for( Entry<Integer,ServerActivity> s : activity.entrySet()){
             int id = s.getKey(); ServerActivity sa = s.getValue();
             if(sa.connected) active.put(id,sa);
         }
         return active;
     }
     
     public synchronized int addServer(ServerActivity sa){
         
         // Notify other servers of the new server
         Collection<ServerActivity> activeList = allActive().values();
         for(ServerActivity e : activeList){
             try { Socket s = e.socket;
              OutputStream os = s.getOutputStream();
              byte pdu[] = new byte[1024]; 
              pdu = PDU.sendRegPDU(1, serverID+"", "nopw", e.serverIP, e.serverPort);
              os.write(pdu);
         
             
             } catch (IOException ex) { }
         }
         
       
         activity.put(serverID, sa);
         serverID++;
         System.out.println("Added Server " + (serverID - 1) + " actualised other servers list");
         return serverID-1;
     }
     
     public synchronized void remServer(int id){
         if(!activity.containsKey(id)) return;
         activity.get(id).connected = false;
         Map<Integer,ServerActivity> activeList = allActive();
         for(Entry<Integer,ServerActivity> e : activeList.entrySet()){
             try {
              ServerActivity sa = e.getValue();int sid = e.getKey();
              OutputStream os = sa.socket.getOutputStream();
              byte pdu[] = new byte[1024]; 
              pdu = PDU.sendRegPDU(0, id+"", "nopw", sa.serverIP, sa.serverPort);
              os.write(pdu);
             } catch (IOException ex) { }
         }
     }
     
  }
  
  private ServerManegement servers;
  private ServerSocket server;
  
  public void main (String args[]) {
    this.servers = new ServerManegement();
    try { this.server = new ServerSocket(CENTRAL_SERVER_PORT); } catch (IOException ex) {}
    run();
  }
  
  public void run () {
    try {
      // Aceitar ligacoes dos servers e atribuir uma thread a cada um.
      Socket      socket;
      while ((socket = server.accept()) != null) {
          System.out.println("Conecçao socket aceite");
        class CentralServerThread extends Thread{
            private Socket socket;
            private int serverID = 0;
            public CentralServerThread(Socket socket){
                this.socket = socket; 
            }
            public void run(){
             try{   
                InputStream is = socket.getInputStream();
                OutputStream os = socket.getOutputStream();
                byte pdu[] = new byte[1024];
                while(is.read(pdu) != -1){
                    switch(pdu[PDU.TYPE_INDEX]){
                        case PDU.REGISTER:
                            String m[] = PDU.readRegPDU(pdu);
                           
                            if(Integer.parseInt(m[0]) == 1) { // register type
                                // adds server and actualises all servers activity list
                                serverID = servers.addServer( new ServerActivity(this.socket,m[3],Integer.parseInt(m[4])));
                                
                                  // Send active server list to the new server
                                Map<Integer,ServerActivity> activeList = servers.allActive();
                                for(Entry<Integer,ServerActivity> e : activeList.entrySet()){
                                     try {
                                      int id = e.getKey();
                                      if(id != serverID){
                                          ServerActivity sl = e.getValue();
                                          byte reg[] = new byte[1024]; 
                                          reg = PDU.sendRegPDU(1, id+"", "nopw", sl.serverIP, sl.serverPort);
                                          os.write(reg);
                                      }
                                     } catch (IOException ex) { }
                                 }
                                System.out.println("Updated new server "+ serverID + " active list" );
                            } else { // unregister type
                                // removes server and actualises all servers activity list
                                servers.remServer(serverID);
                                System.out.println("Removed server with id " + serverID);
                            }break;
               
                    }
                }
                servers.remServer(serverID);
             }catch(Exception e){ }
            }
        }  
          
        new CentralServerThread(socket).start();
      }  
      
    }
    catch (Exception e) {
      // Se falhar fazer print da excepcao e da stack.
      System.out.println(e);
    }
  }

}
