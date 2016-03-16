/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ccprojet;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author gustavo
 */
public class Server {
  private ServerSocket server;
  
  /**
   *  Construtor parametrizado.
   *  @param port Port na qual ligar o servidor.
   */
  public Server (int port) throws IOException {
    this.server = new ServerSocket(port);
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
  
}