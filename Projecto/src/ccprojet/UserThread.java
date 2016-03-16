/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ccprojet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 *
 * @author gustavo
 */
   
class UserThread extends Thread {
  // BufferedReader para ler do socket.
  private Socket          socket;
  private BufferedReader  br;
  private PrintWriter     out;


  /** Construtor com argumentos.
   *  @param s Socket por onde comunicar. */
  public UserThread (Socket socket, Server server) throws IOException {
    this.socket = socket;
    this.br     = new BufferedReader(new InputStreamReader(
          socket.getInputStream(), "UTF-8"));
    this.out    = new PrintWriter(socket.getOutputStream(), true);
    
  }

  public void run () {
    // Input vindo do user.
    String    in; // mudar para bytes
    String[]  splitted;
    boolean   r;

    // Ler do socket e fazer eco.
    try {

      in = br.readLine();


      out.println("OK");
      out.flush();

      }catch(IOException e){}

  }
  
}