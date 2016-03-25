/** Thread atribuida ao user.  */

import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.PrintWriter;

import Exceptions.UserAlreadyInException;
import Exceptions.UserAlreadyRegisteredException;
import static java.lang.Float.parseFloat;


class UserThread extends Thread {
  // BufferedReader para ler do socket.
  private Socket          socket;
  private BufferedReader  br;
  private PrintWriter     out;
  private Server          server;
  private String          username;

  /** Construtor com argumentos.
   *  @param s Socket por onde comunicar. */
  public UserThread (Socket socket, Server server) throws IOException {
    this.socket = socket;
    this.br     = new BufferedReader(new InputStreamReader(
          socket.getInputStream(), "UTF-8"));
    this.out    = new PrintWriter(socket.getOutputStream(), true);
    this.server = server;
  }

  public void run () {
    // Input vindo do user.
    String    in;
    String[]  splitted;
    boolean   r;

    // Ler do socket e fazer eco.
    try {
      // Primeira operacao a realizar, ou login ou registro.
      in = br.readLine();
      splitted  = in.split("\\s+");

      if (splitted[0].equals("r"))
        server.registerUser(splitted[1], splitted[2]);
      else if(splitted[0].equals("l"))
        server.loginUser(splitted[1], splitted[2]);

      // Imprime se não houver erro
      out.println("OK");
      out.flush();

      username = splitted[1];
      server.updateSocket(username, socket);

      while ((in = br.readLine()) != null) {
        try{
          if(in.equals("cliente"))
            servirUsers();
        } catch(IOException e){
          out.println("Erro:" + e);
          out.flush();
        }
      }
    }
    catch (IOException e) {
      System.out.println("Exception caught when trying to listen on port "
          + " or listening for a connection");
      System.out.println(e.getMessage());
    }
    catch (NullPointerException e) {
      // log
    }
    catch (UserAlreadyRegisteredException e) {
      out.println("Utilizador já registado, experimente fazer log in");
      out.flush();
    }
    catch (UserAlreadyInException e) {
      out.println("Ou não se encontra registado ou já se encontra ligado com estas credenciais através de outro terminal");
      out.flush();
    }
    catch (Exception e) {System.out.println(e);}
    finally  {
      // Fazer logout da plataforma.
      if (username != null)
          server.logout(username);
    }
  }

  private void servirUsers() throws IOException{

    String in = br.readLine();
    System.out.println("Pedido de Música:");

  }
}
