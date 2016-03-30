import java.io.PrintWriter;
import java.net.Socket;

class Client extends User {

  public Client () {
    pw.println("Client");
    pw.flush();
  }

  public void runClient () {
    System.out.println("Aguarde: ");
  }
}
