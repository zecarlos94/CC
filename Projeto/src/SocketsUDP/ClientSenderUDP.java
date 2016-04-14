import java.net.*;
import java.util.Scanner;

public class ClientSenderUDP{
  public static void main(String[] args) throws Exception {
    Scanner sc = new Scanner(System.in);
    DatagramSocket ds = new DatagramSocket();
    InetAddress ip;
    DatagramPacket dp_sending;
    boolean end=false;
    String str = "THIS IS NOT GOING TO BE SENT";
    System.out.println("Sender is on...");
    while(!end){
      ip = InetAddress.getByName("localhost");
      dp_sending = new DatagramPacket(str.getBytes(), str.length(), ip, 3000);
      ds.connect(ip, dp_sending.getPort());
      System.out.println("Type your message!");
      str = sc.next();
      if(str.equals("end")){ // última mensagem que será descartado pelo receiver
        end=true;
      }
      System.out.println("Sending...");
      dp_sending = new DatagramPacket(str.getBytes(), str.length(), ip, 3000);
      ds.send(dp_sending);
    }
    System.out.println("Sent all packets with UDP Socket!");
    ds.disconnect();
    ds.close();
    System.out.println("Sender is off!");

  }
}
