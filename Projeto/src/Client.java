/** Programa user que permite comunicacao com o servidor. */

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import static java.lang.System.exit;
import java.net.Socket;
import java.net.InetAddress;
import java.util.Scanner;

public class Client {

  static private int hostPort=9091;

  static Socket socket;
 
  static Scanner sc;
  
  // TODO: Ask client ip/port or find progammaticly ?? 
  // valores atribuidos à sorte ( n sao usados para ja)
  // static String ip = "1322.2322.2313.1321"; 
  // static int porta = 2322; 
  
  
  public static void main (String args[]) throws IOException {
    OutputStream os;
    InputStream is;
      
      try{

      socket = new Socket("localhost", hostPort);
      os = socket.getOutputStream();
      is = socket.getInputStream();

      InetAddress local = socket.getInetAddress();
      String ip = local.getHostAddress();
      int porta = socket.getLocalPort();

      sc = new Scanner(System.in);

         // Validar argumentos.
     switch (validateArgs(args)) {
      // Login.
      case 0:
        byte[] pdu = PDU.sendRegPDU( 1 , args[1], args[2], ip, porta);
        os.write(pdu);
        os.flush();
        
        break;
      // Registar.
      case 1:
 
        byte[] pdu2 = PDU.sendRegPDU( 1 , args[1], args[2], ip, porta);
        os.write(pdu2);
        os.flush();
 
        break;
      default:
        printUsage();
        System.exit(1);
        break;
    }

     
     
  // antiga verificação de dados de login  aux.respostaCredenciais();
    printMenuInicialLogIn();

    boolean end = false;
    while (sc.hasNextLine() && !end) {
      // Ler do stdin.
      // Opções do menu inicial: 0 cliente, 1 sair

      int opcao = sc.nextInt();
      boolean KO = false;

      switch(opcao){
          case 0:
             
              break;
          case 1:
            
              // LOGOUT
              byte[] pdu2 = PDU.sendRegPDU( 0 , args[1], args[2], ip, porta);
              os.write(pdu2);
              os.flush();
                        
              break;

          case 2:
              System.out.println("Saiu");
              end = true;
              break;
              
              
          default:
              System.out.println("Opção inexistente");
              printMenuInicial();
              KO=true;
              break;
      }
      // if(!KO) printMenuInicialLogIn(); //para evitar a situação de apresentar duas vezes o menu qd ocorre "Opção inexistente"
    }
    // Fechar ligacao e streams
    socket.close();
      os.close();
      is.close();    
    
  }
  //Qd se liga o user sem o servidor estar "online"
  catch (IOException e) {
      System.err.println("Couldn't get I/O for the connection to " + hostPort);
      System.exit(1);
  }
}
  private static void printMenuInicialLogIn() {
    System.out.println(
    "***** ***** ***** *****\n" +
    "Bem vindo");
    printMenuInicial();
  }

 private static void printMenuInicial(){
   System.out.println(
      "***** ***** ***** *****\n" +
      "Escreva:\n" +
      "0 - Para pedir uma música\n" +
      "1 - Para fazer unregister.\n"+
      "2 - Para sair\n");
  }
  /**
   *  Validar argumentos e retornar valor indicativo da operacao a realizar.
   *
   *  @return -1  : Argumentos invalidos,
   *          0   : Login,
   *          1   : Registar.
   */
  private static int validateArgs (String args[]) {
    if (args.length < 3)
      return -1;
    else if (!(args[0].equals("-l")) && !(args[0].equals("-r")))
      return -1;
    else if (args[0].equals("-l"))
      return 0;
    else
      return 1;
  }

  /** Imprimir utilizacao do programa. */
  private static void printUsage () {
    System.err.println(
        "UTILIZACAO\n\n" +
        "Registro: \tjava Client -r <username> <password>\n" +
        "Login: \t\tjava Client -l <username> <password>"
        );
  }

}
