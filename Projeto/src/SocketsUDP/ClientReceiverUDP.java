import java.net.*;
public class ClientReceiverUDP{
  public static void main(String[] args) throws Exception {
    DatagramSocket ds = new DatagramSocket(3000);
    byte[] buf = new byte[1024];
    DatagramPacket dp_receiving = new DatagramPacket(buf, 1024);
    boolean end=false;
    System.out.println("Receiver is on...");
    while(!end){
      ds.receive(dp_receiving);
      String str = new String(dp_receiving.getData(), 0, dp_receiving.getLength());
      if(str.equals("end")){ //condição de paragem é descartado esse pacote
        end=true;
      }
      else{ System.out.println("DatagramPacket recebido "+str); }
    }
    ds.close();
    System.out.println("Receiver is off!");
  }
}
