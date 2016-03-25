import java.net.Socket;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/*
    Usado para receber as mensagens do
*/
class UserAux {
  private BufferedReader  in;

  public UserAux (Socket socket) {
    try {
      this.in = new BufferedReader(new InputStreamReader(
            socket.getInputStream(), "UTF-8"));
    }
    catch (Exception e) {
      System.out.println(e);
    }
  }

  public void respostaCredenciais () {
    String input;

    try {
      input = in.readLine();

      // Imprime se houver erro.
      if (!input.equals("OK")){
        System.out.println(input);
        // Para fechar o user imediatamente
        System.exit(1);
      }

    }
    catch (Exception e) {
      System.out.println(e);
    }
  }
}
