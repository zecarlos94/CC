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
        public static final int EXTENDED_HEADER_SIZE = 7;
        
        public static final int TYPE_INDEX = 2;
        
        // types code
        public static final int REGISTER = 1;
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

	public PDU(byte[] in) {
		int j = 0,i = 0;
		this.version = in[i++];
		this.security = in[i++];
		this.type = in[i++];
                
                if(type == 7){
		this.byte1 = in[i++];
		this.byte2 = in[i++];
		this.byte3 = in[i++];
		this.byte4 = in[i++];
                }
                
                for(; i < in.length; i++) 
                        data[j++] = in[i++];
	}
*/
        // tipo 0 saida, 1 entrada
        public static byte[] sendRegPDU(int tipo,String username,String pw,String ip,int port) {
            String s = new String(tipo + "#" + username + "#" + pw + "#" + ip + "#" + port + "#");
            
            byte[] r = new byte[MAX_SIZE];
            int i = 0;
            
            r[i++] = 0; // version ?
            r[i++] = 0; // security
            r[i++] = 1; // type REGISTER      
            
            // teste
            System.out.println("RegPDU data a enviar: " + s);
            
            for(int j= 0;j < s.length(); i++,j++)
                r[i] = (byte) s.charAt(j);
         
            return r;
        }
        
        /*
            Order 0 tipo
                  1 username
                  2 password
                  3 ip
                  4 port
        */
        public static String[] readRegPDU(byte[] info){
            String[] r = new String[5];
            
            int c = 0,i= FIXED_HEADER_SIZE; // ignora header bytes
            while(c < 5) // 5 campos
            {
                StringBuilder sb = new StringBuilder();
                while(true){
                    char ch = (char)info[i++];
                    System.out.println(ch);
                    if(ch == '#')
                        break;
                    sb.append(ch);
                }
                System.out.println("Campo " + c + ":" + sb);
                r[c++] = sb.toString();
            }
            
            return r;
        }
	
}
