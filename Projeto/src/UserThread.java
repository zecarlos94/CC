/** Thread atribuida ao user.  */

import java.net.Socket;
import java.io.InputStream;
import java.io.IOException;

import Exceptions.UserAlreadyInException;
import Exceptions.AuthenticationErrorException;
import java.io.OutputStream;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;


class UserThread extends Thread {
  // BufferedReader para ler do socket.
  private Socket          socket;
  private OutputStream  os;
  private InputStream is;
  private Server          server;

  private String username;
  
  private HostsResponse hostsResponse;

  /** Construtor com argumentos.
   *  @param s Socket por onde comunicar. */
  public UserThread (Socket socket, Server server,HostsResponse hostsResponse) throws IOException {
    this.socket = socket;

    os = socket.getOutputStream();
    is = socket.getInputStream();

    this.server = server;
    this.hostsResponse = hostsResponse;
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
          case PDU.REGISTER:
          {
                String[] s = PDU.readRegPDU(input);
                this.username = s[1];

                int tipo = Integer.parseInt(s[0]);
                       System.out.println("REGPDU read type:" +tipo +" from:" + username);

                if(tipo == 1){
                    server.registerUser(s[1],s[2],s[3] ,socket, Integer.parseInt(s[4]));
                    //System.out.println(" User " + username +" registado");

                } else if(tipo == 0){

                    System.out.println("User " + username +" desligado");
                    server.logoutUser(s[1]);
                    System.out.println("User desligado com sucesso");
                    
                    byte[] message = PDU.sendMessage("Utilizador desligado");
                    os.write(message); os.flush();
                    
                    end = true;
                }
                break;
          }
          case PDU.CONSULT_REQUEST :{
              /* readConsultRequest
                    * @return String[0] = banda
                    *         String[1] = musica.extensao
         */
                  System.out.println("Reading CONSULT_REQUEST");
                  String[] s = PDU.readConsultRequest(input);
                  System.out.println("CONSULT_REQUEST read banda:" +s[0] + ",filename: " + s[1] + " from:" + username);
    
                  System.out.println("Probing hosts for file");
                  Vector<String[]> r = server.findHosts(s[0],s[1],username);
                  System.out.println("Found " + r.size() + " hosts");
                  
                  int nHosts = r.size();
                  
                  int tipo = (nHosts > 0)? 1 : 0;
                  
                  String[][] hosts = new String[nHosts][3];
                  int h = 0;
                  for(String[] host : hosts)
                      hosts[h++] = host;
                  
                  byte[] consultResponsePacket = PDU.sendConsultResponse(tipo, nHosts, hosts);
                  
                  os.write(consultResponsePacket);
                  os.flush();
                  
                  System.out.println("Sent ConsultResponse with hosts data to client");
                  
              break;
          }
          case PDU.CONSULT_RESPONSE: {
                  System.out.println("User: " + username +" responded CONSUT_REQUEST");
                     String[] host = new String[3];
                     // TODO: SOLVE BUG (INPUT HAS STRANGE BYTES, NUMEROHOSTS = 49 )
                     String[][] pduResponse = PDU.readConsultResponse(input);
                     
                     // Se cliente tem o ficheiro
                     if(pduResponse != null){
                        host[0] = pduResponse[0][0]; // id
                        host[1] = pduResponse[0][1];  //ip
                        host[2] = pduResponse[0][2]; // porta UDP
                        hostsResponse.addHost(host);
                     } else  hostsResponse.addHost(null);
          
          }
          default:
              break;
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
            os.flush();
        } catch (IOException ex) {
            Logger.getLogger(UserThread.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    catch (Exception e) {System.out.println(e);end=true;}


  }// end while

  }

}
