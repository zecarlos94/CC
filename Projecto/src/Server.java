/** Servidor para controlar os sockets e atribuir uma thread a cada
 *  user.  */

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.net.ServerSocket;
import java.net.Socket;

import java.lang.System;
import java.lang.Runtime;

import Exceptions.UserAlreadyRegisteredException;
import Exceptions.UserAlreadyInException;

public class Server {
  private ServerSocket server;
  private Users users;


  /**
   *  Construtor parametrizado.
   *  @param port Port na qual ligar o servidor.
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
   *  @return true se o registo for feito com sucesso,
   *          false caso contrario.
   */
  public void registerUser (String user, String id, int porta) throws UserAlreadyRegisteredException {
    boolean registeredOK = users.register(user, pass);

    if (registeredOK)
      System.out.println("User '" + user + "' registou e ligou");
    else
      throw new UserAlreadyRegisteredException("Utilizador já registado");
  }

  public void unRegister (String user) {
    users.unRegister(user);
    System.out.println("User '" + user +"' desligou");
  }

}
