import java.util.*;
import java.io.*;
import java.nio.charset.Charset;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ccprojet;

/**
 *
 * @author gustavo
 */

public class PDU {

	private static final Charset UTF8_CHARSET = Charset.forName("UTF-8");

    	public static int REGISTER = 1;
    	public static int MAX_SIZE = 48 * 1024; // bytes

	byte version;
	byte security;
	byte type; // 1 - Register, 2 - Consult_Request

    	// Options 4 bytes pdu
    	byte byte1;
    	byte byte2;
    	byte byte3;
    	byte byte4;
    	// data
    	byte data[]; // pd ser mudado para char* , Nota 1 char = 1byte

	public PDU(byte[] in) {
		int j = 0;
		this.version = in[0];
		this.security = in[1];
		this.type = in[2];
		this.byte1 = in[3];
		this.byte2 = in[4];
		this.byte3 = in[5];
		this.byte4 = in[6];
		for(int i = 7; i < in.length; i++) data[j] = in[i];
	}

	// Recebe id, ip, username em forma de bytes e devolve uma string
	public String loginData() {
		try {
			String s = new String(data, UTF8_CHARSET);
		} catch(UnsupportedEncodingException e) {
         		System.out.println("Unsupported character set");
      		}
		return s;
	}

	// Transforma um array de bytes numa string
	public byte[] stringToBytes(String s) {
		try {
			byte[] bytes = s.getBytes("UTF-8");
		} catch(UnsupportedEncodingException e) {
         		System.out.println("Unsupported character set");
      		}
         	return bytes;
	}

    	public byte[] compactToSend(){
        	return null;
    	}

    	// le os bytes do buffer e cria PDU, method input a definir
    	public PDU readBuffer(){return null;}
}
