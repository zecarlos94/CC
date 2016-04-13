import java.util.HashMap;
import java.net.Socket;
import java.util.concurrent.locks.ReentrantLock;


class Users {
  // Mapeamento entre usernames e passwords.
  private HashMap<String, String> users;
  private HashMap<String, String> ips;
  private HashMap<String, Integer> ports;


  // Mapeamento entre usernames e estado atual da sessão (ligado ou não).
  private HashMap<String, Boolean> connected;
  public HashMap<String, Socket> userSockets;
  public ReentrantLock lock;

  /** Construtor não parametrizado */
  public Users () {
    this.users       = new HashMap<String, String>();
    this.connected   = new HashMap<String, Boolean>();
    this.userSockets = new HashMap<String, Socket>();
    this.ips = new HashMap<String,String>();
    this.ports = new HashMap<String,Integer>();
    this.lock        = new ReentrantLock();
  }

  /** Efetuar login com um utilizador.
   *  @param  username  Nome de utilizador.
   *  @param  password  Password do utilizador.
   *  @param ip User ip.
   *  @param porta .
   *  @return true se o utilizador ainda não estiver ligado e as passwords
   *          coincidirem, false em qualquer outro caso. */
  public Boolean login (String username, String password,String ip,int porta) {
    lock.lock();
                    System.out.println("cfsfason");

    if (!users.containsKey(username)){
                System.out.println("userrep");

      lock.unlock();
      return false;
    }
    else if (connected.get(username)){
                System.out.println("con");

      lock.unlock();
      return false;
    }
    else {
      String storedPassword = users.get(username);
                System.out.println("cdson");

      if (password.equals(storedPassword)) {
        
        System.out.println("cI");
        connected.put(username, true);
                System.out.println("ports");

        ports.put(username,porta);
                System.out.println("ips");

        ips.put(username,ip);

                System.out.println("unlock");

        lock.unlock();
        return true;
      }
      else{
        lock.unlock();
        return false;
      }
    }
  }
  
  public Boolean isRegisted(String username){
      lock.lock();
      boolean r = users.containsKey(username);
      lock.unlock();
      return r;
  }

  /** Registar novo utilizador.
   *  @param username Nome de utilizador.
   *  @param password Password.

   *  @return true se o username não estiver em utilização, false
   *          caso contrário. */

  public Boolean register (String username, String password) {
    lock.lock();
    if (!users.containsKey(username)) {
      users.put(username, password);
      
    //  connected.put(username, true);
      
      lock.unlock();
      return true;
    }
    lock.unlock();
    return false;
  }

  /** Fazer logout com um dado utilizador. Se o nome de utilizador não
   *  existir o pedido é ignorado.
   *  @param username Nome de utilizador.
   *  @param password Password. */

  public void logout (String username) {
    lock.lock();
    if (connected.containsKey(username))
      connected.put(username, false);
    lock.unlock();
 }
}
