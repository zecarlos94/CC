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
    byte data[]; // pd ser mudado para char* , Nota 1 char = 2bytes


    public byte[] compactToSend(){
        return null;
    }

    // le os bytes do buffer e cria PDU, method input a definir
    public PDU readBuffer(){return null;}
}
