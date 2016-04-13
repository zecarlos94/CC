/** Servidor para controlar os sockets e atribuir uma thread a cada
 *  user.  */

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;


import Exceptions.AuthenticationErrorException;
import Exceptions.UserAlreadyInException;

public class Server {
  private ServerSocket server;
  private Users users;


  /**
   *  Construtor parametrizado.
   *  @param port Port na qual ligar o servidor.
     * @throws java.io.IOException
   */
  public Server (int port) throws IOException {
    this.server = new ServerSocket(port);
    this.users  = new Users();
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
        user = new UserThread(socket, this);
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
  public void registerUser (String user,String pass, String ip, int porta)
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
          loggedInOK = users.login(user, pass, ip, porta);
          //loggedInOK é true caso seja true em todos os testes efetuados pelo login
          System.out.println("Ended user login:" + loggedInOK);
          if(loggedInOK) { System.out.println("User '" + user + "' ligou"); }
          else { throw new AuthenticationErrorException("Credencias do utilizador erradas"); }
        }
      }
      else {
        // o método register faz o login logo no lado da classe Users
        logOK = users.register(user, pass, ip, porta);
        if(logOK) { System.out.println("User '" + user + "' registou e ligou"); }
        else { throw new AuthenticationErrorException("Encontrou-se algo de errado no processo de registo"); }
      }
  }


  public void loginUser (String user, String pass,String ip,int porta) throws UserAlreadyInException {
     boolean loggedInOK = users.login(user, pass, ip, porta);
     if (loggedInOK) { System.out.println("User '" + user + "' ligou"); }
     else { throw new UserAlreadyInException("Outro utilizador ligado com mesmas credencias"); }
   }

  public void logoutUser (String username) {
     boolean logOut;
     logOut = users.logout(username);
     System.out.println("User '" + username +"' desligou? " + logOut);
   }

}
