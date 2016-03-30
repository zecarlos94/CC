/** Thread atribuida ao user.  */

import java.net.Socket;
import java.io.BufferedOutputStream;
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
  private BufferedOutputStream  bos;
  private Server          server;

  /** Construtor com argumentos.
   *  @param s Socket por onde comunicar. */
  public UserThread (Socket socket, Server server) throws IOException {
    this.socket = socket;
    this.bos = new BufferedOutputStream( socket.getOuputStream() ); 
    InputStream inputStream = socket.getInputStream();
    this.server = server;
  }

  public void run () {
    // Input vindo do user.
    
    byte[] in = new byte[PDU.MAX_SIZE]; // PDU header Size

    // Ler do socket e fazer eco.
    try {
      // Primeira operacao a realizar, ou login ou registro.

      PDU pdu = new PDU(inputStream.read());


      switch(pdu.type){
	case 1 :
		pdu.loginData();

      }
	
     server.registerUser(id,ip,porta);

      username = splitted[1];


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
