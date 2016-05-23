/** Servidor para controlar os sockets e atribuir uma thread a cada
 *  user.  */

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;


import Exceptions.AuthenticationErrorException;
import Exceptions.UserAlreadyInException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.Vector;

public class Server {
  private ServerSocket server;
  private Users users;
  private HostsResponse hostsResponse;

  /**
   *  Construtor parametrizado.
   *  @param port Port na qual ligar o servidor.
     * @throws java.io.IOException
   */
  public Server (int port) throws IOException {
    this.server = new ServerSocket(port);
    this.users  = new Users();
    this.hostsResponse = new HostsResponse();
  }

  /** Colocar o servidor a correr. */
  public void run () {
    Socket      socket;
    UserThread  user;

    
    try {
      // Aceitar ligacoes dos users e atribuir uma thread a cada um.
      System.out.println("Servidor online");

      while ((socket = server.accept()) != null) {
          System.out.println("Conecçao socket aceite");
        
        user = new UserThread(socket, this,hostsResponse);
        user.start();
      }
    }
    catch (Exception e) {
      // Se falhar fazer print da excepcao e da stack.
      System.out.println(e);
      PrintWriter pw = new PrintWriter(System.out);
      e.printStackTrace(pw);
    }
  }

  public void updateSocket (String username, Socket socket) {
    users.userSockets.put(username, socket);
  }


  /**
   *  Registar na plataforma.
   *
   *  @param  user Nome de utilizador.
   *  @param  pass Password para o utilizador.
     * @param ip
     * @param porta
     * @throws Exceptions.AuthenticationErrorException
     * @throws Exceptions.UserAlreadyInException
   */
  public void registerUser (String user,String pass, String ip,Socket userSocket, int porta)
    throws AuthenticationErrorException {
      System.out.println("Checking if registed...");
      boolean isRegisted, isLoggedIn, loggedInOK, logOK;
      isRegisted = users.isRegisted(user);
      if(isRegisted) {
        System.out.println("Is already registed in the system!");
        isLoggedIn = users.isLoggedIn(user);
        if(isLoggedIn) { System.out.println("Is already logged in the system!"); }
        else {
          System.out.println("Start users login");
          loggedInOK = users.login(user, pass, ip, userSocket,porta);
          //loggedInOK é true caso seja true em todos os testes efetuados pelo login
          System.out.println("Ended user login:" + loggedInOK);
          if(loggedInOK) { System.out.println("User '" + user + "' ligou"); }
          else { throw new AuthenticationErrorException("Credencias do utilizador erradas"); }
        }
      }
      else {
        // o método register faz o login logo no lado da classe Users
        logOK = users.register(user, pass, ip,userSocket, porta);
        if(logOK) { System.out.println("User '" + user + "' registou e ligou"); }
        else { throw new AuthenticationErrorException("Encontrou-se algo de errado no processo de registo"); }
      }
  }


  public void loginUser (String user, String pass,String ip,Socket socket,int porta) throws UserAlreadyInException {
     boolean loggedInOK = users.login(user, pass, ip,socket, porta);
     if (loggedInOK) { System.out.println("User '" + user + "' ligou"); }
     else { throw new UserAlreadyInException("Outro utilizador ligado com mesmas credencias"); }
   }

  public void logoutUser (String username) {
     boolean logOut;
     logOut = users.logout(username);
     System.out.println("User '" + username +"' desligou? " + logOut);
   }
  
  /**
   * 
   * @return Object[0] : numeroHosts = nº clientes com o ficheiro
         *   Object[1] : username do utilizador
         *   Object[2] : Ip do utilizador src com o ficheiro
         *   Object[3] : Porta do utilizador src
         *          
  */
  public Vector<String[]> findHosts(String banda,String fileName,String userRequesting){
  
      Set<String> usersSet = users.getUsers();
      if(userRequesting!=null)
          usersSet.remove(userRequesting);
      
      Vector<String[]> hosts;
      
      
      hostsResponse.setNHosts(usersSet.size());
      // Look for hosts with the file within the server domain
      for(String username : usersSet){
          Socket userSocket = users.getSocket(username);
          Boolean connected = users.connected(username);
          if(connected){
             try{
             OutputStream os =  userSocket.getOutputStream();
             byte[] pduCR = PDU.sendConsultRequest(banda, fileName);
             os.write(pduCR); 
             os.flush();
        //     os.close();
             } catch(IOException e) {}
          }
      }
      
      class readResponseJob extends Thread{
          HostsResponse r;
          public readResponseJob(HostsResponse r){
              this.r = r;
          }
          public void run(){
               r.waitForHosts();
          }   
      }
      Thread readResponse = new readResponseJob(hostsResponse);        
      readResponse.start();
             
      long ABORT_TIME = 500; // 0.5 segundos
             try{
                 // stops waiting for response after ABORT_TIME ms 
                 
                readResponse.join();
               
                 
             }catch(Exception e){}
      
      hosts = new Vector<String[]>(hostsResponse.getHosts());
          
      
      
      // Se não houver hosts neste server, procurar nos restantes servers, FASE III
      if(hosts.size()==0){
      
      }
      return hosts;
  }
                  
}
