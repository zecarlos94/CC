import java.util.*;
import java.io.*;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
// package ccprojet;

/**
 *
 * @author gustavo
 */

public class PDU {

	private static final Charset UTF8_CHARSET = Charset.forName("UTF-8");


        public static final int MAX_SIZE = 48 * 1024; // bytes
        
        public static final int FIXED_HEADER_SIZE = 3;
        public static final int EXTENDED_HEADER_SIZE = 7; //UDP DATA
        
        public static final int TYPE_INDEX = 2;
        
        // types code
        public static final int MESSAGE = 1;
        public static final int REGISTER = 2;
        public static final int LOGIN = 3;
        public static final int CONSULT_REQUEST = 4;
        public static final int CONSULT_RESPONSE = 5;
        public static final int PROBE_REQUEST = 6;
        public static final int PROBE_RESPONSE = 7;
        public static final int REQUEST = 8;
        public static final int DATA = 9; // tipo usado no socket UDP 
        
 /*
	byte version;
	byte security;
	byte type; // 1 - Register, 2 - Consult_Request

    	// Options 4 bytes pdu
    	byte byte1;
    	byte byte2;
    	byte byte3;
    	byte byte4;
    	// data
    	byte data[]; 
*/
        
        public static String[] readCampos(byte[] info,int ncampos){
            String[] r = new String[ncampos];
            
            int c = 0,i= FIXED_HEADER_SIZE; // ignora header bytes
            while(c < ncampos) // 5 campos
            {
                StringBuilder sb = new StringBuilder();
                while(true){
                    char ch = (char)info[i++];
                    if(ch == '#')
                        break;
                    sb.append(ch);
                }
                r[c++] = sb.toString();
            }
            
            return r;
        
        }

        
        /*
            Order 0 tipo = 0 para logout 1 para registo&&login
                  1 username
                  2 password
                  3 ip
                  4 port
        */
        public static String[] readRegPDU(byte[] info){
            String[] r = readCampos(info,5);
            return r;
        }
                // tipo 0 saida, 1 entrada
        public static byte[] sendRegPDU(int tipo,String username,String pw,String ip,int port) {
            String s = new String(tipo + "#" + username + "#" + pw + "#" + ip + "#" + port + "#");
            
            byte[] r = new byte[MAX_SIZE];
            int i = 0;
            
            r[i++] = 0; // version ?
            r[i++] = 0; // security
            r[i++] = REGISTER; // type REGISTER      
            
            // teste
            System.out.println("RegPDU data a enviar: " + s);
            
            for(int j= 0;j < s.length(); i++,j++)
                r[i] = (byte) s.charAt(j);
         
            return r;
        }
        
        
        
  
        public static String readMessage(byte[] info){
            String[] r = readCampos(info,1);
            return r[0];
        }
        
        public static byte[] sendMessage(String message){
            String s = new String(message +"#");
            byte[] r = new byte[MAX_SIZE];
            int i = 0; r[i++] = 0; r[i++] = 0; r[i++] = MESSAGE;
              // teste
            System.out.println("RegPDU data a enviar: " + s);
            for(int j= 0;j < s.length(); i++,j++)
                r[i] = (byte) s.charAt(j);
         
            return r;
        }
        
        
        
        /**
         * 
         * @param info
         * @return String[0] = banda
         *         String[1] = musica.extensao
         */
        public static String[] readConsultRequest(byte[] info){
            String[] r = readCampos(info,2);
            return r;
        }
        
        public static byte[] sendConsultRequest(String banda,String musica){
           String s = new String(banda + "#" + musica + "#");
            
            byte[] r = new byte[s.length() + 10];
            int i = 0;
            
            r[i++] = 0; // version ?
            r[i++] = 0; // security
            r[i++] = CONSULT_REQUEST; // type REGISTER      
            
            // teste
            System.out.println("ConsultRequestPDU data a enviar: " + s);
            
            for(int j= 0;j < s.length(); i++,j++)
                r[i] = (byte) s.charAt(j);
         
            return r;
        }

        
       
        
         /**
         * 
         * @param info
         * @return String[0] tipo = 1 se encontrou o ficheiro senao 0
         *         String[1] numeroHosts = nº clientes com o ficheiro
         *         String[2] ID = username do utilizador
         *         String[3] IP = Ip do utilizador src com o ficheiro
         *         String[4] porta = Porta do utilizador src
         *          
         */
        public static String[] readConsultResponse(byte[] info){
            String[] r = readCampos(info,5);
            return r;
        }
        
        public static byte[] sendConsultResponse(int tipo,int numeroHosts,
                String id,String ip,int porta){
            String s = new String(tipo + "#"+numeroHosts + "#" + id 
                    + "#" + ip + "#" + porta + "#");
            
            byte[] r = new byte[s.length() + 5];
            int i = 0;
            
            r[i++] = 0; // version ?
            r[i++] = 0; // security
            r[i++] = CONSULT_RESPONSE; // type REGISTER      
            
            // teste
            System.out.println("ConsultResponsePDU data a enviar: " + s);
            
            for(int j= 0;j < s.length(); i++,j++)
                r[i] = (byte) s.charAt(j);
         
            return r;
        }
       
        /**
         * So tem header por isso n precisa da função readProbeRequest
         * @return 
         */
        public static byte[] sendProbeRequest(){
            byte[] r = new byte[3];
            int i = 0;
            
            r[i++] = 0; // version ?
            r[i++] = 0; // security
            r[i++] = PROBE_REQUEST; // type REGISTER      
        
            return r;
        }
        
        
        /**
         * 
         * @param info
         * @return TimeStamp
         */
        public static String readProbeResponse(byte[] info){
            String[] r = readCampos(info,1);
            return r[0];
        }
        
        public static byte[] sendProbeResponse(String timestamp){
           String s = new String(timestamp + "#");
            
            byte[] r = new byte[s.length() + 5];
            int i = 0;
            
            r[i++] = 0; // version ?
            r[i++] = 0; // security
            r[i++] = PROBE_RESPONSE; // type REGISTER      
            
            for(int j= 0;j < s.length(); i++,j++)
                r[i] = (byte) s.charAt(j);
         
            return r;
        }
        
        /**
         * 
         * @param info
         * @return String[0] banda
         *         String[1] musica.extensao
         */
        public static String[] readRequest(byte[] info){
            String[] r = readCampos(info,2);
            return r;
        }
       
        public static byte[] sendRequest(String banda,String musica){
            String s = new String(banda + "#" + musica + "#");
            
            byte[] r = new byte[s.length() + 5];
            int i = 0;
            
            r[i++] = 0; // version ?
            r[i++] = 0; // security
            r[i++] = REQUEST;       
            
            for(int j= 0;j < s.length(); i++,j++)
                r[i] = (byte) s.charAt(j);
         
            return r;   
        }
       
        
}
