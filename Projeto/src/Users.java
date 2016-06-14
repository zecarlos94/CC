import java.util.HashMap;
import java.net.Socket;
import java.util.Set;
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
  
  /*gets */
  public Socket getSocket(String username){
      lock.lock();
       Socket r = userSockets.get(username);
      lock.unlock();
      return r;
  }
  
  public String getIP(String username){
      lock.lock();
       String r = ips.get(username);
      lock.unlock();
      return r;
  }
   
  public int getPort(String username){
      lock.lock();
       int r = ports.get(username);
      lock.unlock();
      return r;
  }
  
  
  public Set<String> getUsers(){
      lock.lock();
        Set<String> r = users.keySet();
      lock.unlock();
      return r;
  }
  
  public Boolean connected(String username){
      lock.lock();
       Boolean r = connected.get(username);
      lock.unlock();
      return r;
  }
  

  /** Efetuar login com um utilizador.
   *  @param  username  Nome de utilizador.
   *  @param  password  Password do utilizador.
   *  @param ip User ip.
   *  @param porta .
   *  @return true se o utilizador ainda não estiver ligado e as passwords
   *          coincidirem, false em qualquer outro caso. */

  public Boolean login (String username, String password,String ip,Socket socket,int porta) {
    lock.lock();
    boolean res,ipOK,connectedOK,portsOK;
    // atualiza o estado do user username para ligado
    connected.put(username, true);
    ips.put(username, ip);
    userSockets.put(username, socket);
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
  public Boolean register (String username, String password, String ip,Socket userSocket, int porta) {
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
        res=login(username, password, ip,userSocket, porta);
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
    this.userSockets.remove(username);
    //connectedOK é true caso valor do username em connected estiver a false (desligado)
    connectedOK=connected.get(username);
    ipOK=ips.containsKey(username);
    portsOK=ports.containsKey(username);
    res= !connectedOK && !ipOK && !portsOK;
    lock.unlock();
    return res;
 }


  
  
}
