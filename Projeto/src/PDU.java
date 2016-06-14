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


public class PDU {

	private static final Charset UTF8_CHARSET = Charset.forName("UTF-8");


        public static final int MAX_SIZE = 48 * 1024; // bytes
        
        public static final int FIXED_HEADER_SIZE = 3;
        public static final int EXTENDED_HEADER_SIZE = FIXED_HEADER_SIZE + 3; //UDP DATA
        
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
            
            for(int j= 0;j < s.length(); i++,j++)
                r[i] = (byte) s.charAt(j);
         
            return r;
        }

        
       
        
         /**
         * 
         * @param info
         * @return lista com a informação de cada host
         *         String[][0] ID = username do utilizador
         *         String[][1] IP = Ip do utilizador src com o ficheiro
         *         String[][2] porta = Porta do utilizador src
         *          
         */
        public static String[][] readConsultResponse(byte[] info){
            // n tiver o ficheiro
            if( (int)info[FIXED_HEADER_SIZE] == 0) return null;
            //se numeroHosts > 255 program bugs 
            int numeroHosts = (int)info[FIXED_HEADER_SIZE + 1]; 
            String[][] r = new String[numeroHosts][3];

            int j = FIXED_HEADER_SIZE + 2;
            for(int i = 0; i < numeroHosts;i++){
                int c = 0;
                StringBuilder sb = new StringBuilder();
                while(c < 3){
                    char ch = (char)info[j++];
                    if(ch == '#'){
                        r[i][c] = sb.toString();
                        sb = new StringBuilder();
                        c++;
                    } else sb.append(ch);

                }
            }
            
            return r;
        }
        /*
            tipo 0 se n encontrou o ficheiro senao 1
            String[3] has {id,ip,port}
        */
        public static byte[] sendConsultResponse(int tipo,int numeroHosts,String id,String ip, int port){
            String[][] s = new String[1][3]; 
            s[0][0] = id; s[0][1] = ip; s[0][2] = port+"";
            return sendConsultResponse(tipo,1,s);
        }
        public static byte[] sendConsultResponse(int tipo,int numeroHosts,
                String[][] hosts){

            StringBuilder sb = new StringBuilder();
            
            for(int h = 0; h < numeroHosts; h++){
                sb.append(hosts[h][0] + "#" + hosts[h][1] + "#" + hosts[h][2] + "#");
            }
            String s = sb.toString();
            
            byte[] r = new byte[s.length() + 5 + 2];
            int i = 0;
            
            r[i++] = 0; // version ?
            r[i++] = 0; // security
            r[i++] = CONSULT_RESPONSE; // type REGISTER      
            
            r[i++] = (byte)tipo;
            r[i++] = (byte)numeroHosts; 
            
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
