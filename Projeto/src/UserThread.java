/** Thread atribuida ao user.  */

import java.net.Socket;
import java.io.InputStream;
import java.io.IOException;

import Exceptions.UserAlreadyInException;
import Exceptions.AuthenticationErrorException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;


class UserThread extends Thread {
  // BufferedReader para ler do socket.
  private Socket          socket;
  private OutputStream  os;
  private InputStream is;
  private Server          server;

  private String username;

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

    boolean end = false;

    while(!end){
    // Ler do socket e fazer eco.
    try {
      // Primeira operacao a realizar, ou login ou registro.

      if(is.read(input) == -1) { end= true; break;}



      System.out.println("InputStream lida");

      System.out.println( "PDU type " + (int)input[PDU.TYPE_INDEX] );

      switch ((int)input[PDU.TYPE_INDEX]){
          case (PDU.REGISTER):
            {
                String[] s = PDU.readRegPDU(input);
                this.username = s[1];



                int tipo = Integer.parseInt(s[0]);
                       System.out.println("REGPDU read type:" +tipo +" from:" + username);


                if(tipo == 1){
                    server.registerUser(s[1],s[2],s[3] , Integer.parseInt(s[4]));
                    //System.out.println(" User " + username +" registado");

                } else if(tipo == 0){

                    System.out.println("User " + username +" desligado");
                    server.logoutUser(s[1]);
                    System.out.println("User desligado com sucesso");
                    end = true;
                }
            }
          // case outros tipos -> lidar
      }

    }
    catch (IOException e) {
      System.out.println("Exception caught when trying to listen on port "
          + " or listening for a connection");
      System.out.println(e.getMessage()); end = true;
    }
    catch (NullPointerException e) {

    }
    catch (AuthenticationErrorException e) {

        System.out.println("Erro de autenticação do utilizador " + username);
        byte[] mensagem = PDU.sendMessage(e.getMessage());
        try {
            os.write(mensagem);
        } catch (IOException ex) {
            Logger.getLogger(UserThread.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    catch (Exception e) {System.out.println(e);end=true;}


  }// end while

  }

}
