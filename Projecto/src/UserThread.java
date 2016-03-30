/** Thread atribuida ao user.  */

import java.net.Socket;
import java.io.InputStream;
import java.io.IOException;

import Exceptions.UserAlreadyInException;
import Exceptions.UserAlreadyRegisteredException;
import java.io.OutputStream;


class UserThread extends Thread {
  // BufferedReader para ler do socket.
  private Socket          socket;
  private OutputStream  os;
  private InputStream is;
  private Server          server;

  /** Construtor com argumentos.
   *  @param s Socket por onde comunicar. */
  public UserThread (Socket socket, Server server) throws IOException {
    this.socket = socket;
    
    os = socket.getOutputStream();
    is = socket.getInputStream();

    this.server = server;
  }

  @Override
  public void run () {
    // Input vindo do user.
    
    byte[] input = new byte[PDU.MAX_SIZE]; // PDU header Size

    // Ler do socket e fazer eco.
    try {
      // Primeira operacao a realizar, ou login ou registro.

      is.read(input);
      
      System.out.println("InputStream lida");

      System.out.println( "PDU type " + (int)input[PDU.TYPE_INDEX] );
      
      switch ((int)input[PDU.TYPE_INDEX]){
          case (PDU.REGISTER):
            {
                String[] s = PDU.readRegPDU(input);
                
                System.out.println("PDU read");
                
                int tipo = Integer.parseInt(s[0]);
                
                if(tipo == 1){
                    server.registerUser(s[1],s[2],s[3] , Integer.parseInt(s[4]));
                    System.out.println(" User registado");
                } else if(tipo == 0){
                    
                        server.logoutUser(s[1]);
                        System.out.println("User desligado");

                }
            }
          // case outros tipos -> lidar
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
        // Mensagens de texto não suportadas ( A imprimir no server para observar comportamento)
        System.out.println("Utilizador já registado, experimente fazer log in");
        /*
      out.println("Utilizador já registado, experimente fazer log in");
      out.flush();*/
    }
    catch (UserAlreadyInException e) {
        // Mensagens de texto não suportadas ( A imprimir no server para observar comportamento)
        System.out.println("Ou não se encontra registado ou já se encontra ligado com estas credenciais através de outro terminal");
        /*
      out.println("Ou não se encontra registado ou já se encontra ligado com estas credenciais através de outro terminal");
      out.flush();*/
    } 
    catch (Exception e) {System.out.println(e);}
  
  }

}
