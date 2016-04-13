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
    this.ips         = new HashMap<String,String>();
    this.ports       = new HashMap<String,Integer>();
    this.lock        = new ReentrantLock();
  }

  /** Efetuar login com um utilizador.
   *  @param  username  Nome de utilizador.
   *  @param  password  Password do utilizador.
   *  @param ip User ip.
   *  @param porta .
   *  @return true se o utilizador ainda não estiver ligado e as passwords
   *          coincidirem, false em qualquer outro caso. */

  // usa private HashMap<String, String> users;
  // usa private HashMap<String, Boolean> connected;
  // usa private HashMap<String, String> ips;
  // usa private HashMap<String, Integer> ports;
  public Boolean login (String username, String password,String ip,int porta) {
    lock.lock();
    boolean res,ipOK,connectedOK,portsOK;
    // atualiza o estado do user username para ligado
    connected.put(username, true);
    ips.put(username, ip);
    ports.put(username, porta);
    //connectedOK é true caso valor do username em connected estiver a true (ligado)
    connectedOK=connected.get(username);
    ipOK=ips.containsKey(username);
    portsOK=ports.containsKey(username);
    //res é true caso todos os boolean sejam verdadeiros (true)
    res=connectedOK && ipOK && portsOK;
    lock.unlock();
    return res;
  }

  /** Verifica se um utilizador está registado.
   *  @param  username  Nome de utilizador.
   *  @return true se o utilizador se estiver registado,
   *  false no outro caso. */
  public Boolean isRegisted(String username){
      lock.lock();
      boolean	isRegistedOK;
      isRegistedOK=users.containsKey(username);
      lock.unlock();
      return isRegistedOK;
  }

  /** Verifica se um utilizador está ligado.
   *  @param  username  Nome de utilizador.
   *  @return true se o utilizador se estiver ligado,
   *  false no outro caso. */
  public Boolean isLoggedIn(String username){
      lock.lock();
      boolean	isLoggedInOK;
      isLoggedInOK=connected.get(username);
      lock.unlock();
      return isLoggedInOK;
  }

  /** Registar novo utilizador.
   *  @param username Nome de utilizador.
   *  @param password Password.

   *  @return true se o username não estiver em utilização, false
   *          caso contrário. */
  public Boolean register (String username, String password, String ip, int porta) {
      lock.lock();
      boolean OK,res;
      OK = isRegisted(username);
      //true se o utilizador se estiver registado,
      //false no outro caso
      if(OK){
        // register falhou! pq ele já está registado
        lock.unlock();
        return false;
      }
      else{
        // Esse username ainda não está registado
        // Insere-se na base de dados users este novo user
        users.put(username, password);
        res=login(username, password, ip, porta);
        // res toma valor false caso falhe o login
        lock.unlock();
        return res;
      }
  }

  /** Fazer logout com um dado utilizador. Se o nome de utilizador não
   *  existir o pedido é ignorado.
   *  @param username Nome de utilizador.
   *  @param password Password. */
  public Boolean logout (String username) {
    lock.lock();
    boolean res,ipOK,connectedOK,portsOK;
    // atualiza o estado do user username para desligado
    connected.put(username, false);
    ips.remove(username);
    ports.remove(username);
    //connectedOK é true caso valor do username em connected estiver a false (desligado)
    connectedOK=connected.get(username);
    ipOK=ips.containsKey(username);
    portsOK=ports.containsKey(username);
    res= !connectedOK && !ipOK && !portsOK;
    lock.unlock();
    return res;
 }

}
