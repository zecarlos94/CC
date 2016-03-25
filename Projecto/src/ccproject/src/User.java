/** Programa user que permite comunicacao com o servidor. */

import java.io.PrintWriter;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class User {

  static private int hostPort=9091;

  static Socket socket;
  static PrintWriter pw;
  static Scanner sc;
  static UserAux aux;

  public static void main (String args[]) throws IOException {
    try{

      socket = new Socket("localhost", hostPort);
      // PrintWriter para escrever no socket.
      pw     = new PrintWriter(socket.getOutputStream());
      sc     = new Scanner(System.in);
      aux    = new UserAux(socket);

    // Validar argumentos.
    switch (validateArgs(args)) {
      // Login.
      case 0:
        pw.println("l " + args[1] + " " + args[2]);
        pw.flush();
        break;
      // Registar.
      case 1:
        pw.println("r " + args[1] + " " + args[2]);
        pw.flush();
        break;
      default:
        printUsage();
        System.exit(1);
        break;
    }

    aux.respostaCredenciais();
    printMenuInicialLogIn();

    while (sc.hasNextLine()) {
      // Ler do stdin.
      // Opções do menu inicial: 0 cliente, 1 sair

      int opcao = sc.nextInt();
      boolean KO = false;

      switch(opcao){
          case 0:
              Client cliente = new Client();
              cliente.runClient();

              break;
          case 1:
              System.out.println("Saiu");
              socket.close();
              pw.close();
              System.exit(1);
              break;

          default:
              System.out.println("Opção inexistente");
              printMenuInicial();
              KO=true;
              break;
      }
      if(!KO) printMenuInicialLogIn(); //para evitar a situação de apresentar duas vezes o menu qd ocorre "Opção inexistente"
    }
    // Fechar ligacao e escritor.
    socket.close();
    pw.close();
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
      "1 - Para sair.\n");
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
        "Registro: \tjava User -r <username> <password>\n" +
        "Login: \t\tjava User -l <username> <password>"
        );
  }

}
