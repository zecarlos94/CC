/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */



import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Gustavo
 */
public class ClientReciever extends Thread {
    
    private InputStream is;
    private Socket s;
    public ClientReciever(InputStream is,Socket s){
        this.s=s;
        this.is=is;
    }
    
    public void run(){
    
        byte[] mensagem = new byte[PDU.MAX_SIZE];
        
        
        try {
        while( true ){
            
            
                if(is.read(mensagem) == -1) break;
           
            
            switch(mensagem[PDU.TYPE_INDEX]){
                case PDU.MESSAGE:
                    System.out.println( PDU.readMessage(mensagem) );
                default:
            }
        }
        } catch (IOException ex) {
                Logger.getLogger(ClientReciever.class.getName()).log(Level.SEVERE, null, ex);
            }
    
    }
    
}
