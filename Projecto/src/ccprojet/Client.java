/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ccprojet;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 *
 * @author gustavo
 */

class Client {

        public static void main (String[] args) throws Exception{
                Socket cliente = new Socket("localhost",Integer.parseInt(args[0]));
                Scanner leitura = new Scanner(System.in);
                PrintWriter enviar = new PrintWriter(cliente.getOutputStream());

                BufferedReader buffer = new BufferedReader(new InputStreamReader(cliente.getInputStream()));

                while (leitura.hasNextLine()){
                        enviar.println(leitura.nextLine());
                        enviar.flush();
                        String temp;
                        temp = buffer.readLine();
                        System.out.println(temp);
                }
                cliente.shutdownOutput();
                String media = buffer.readLine();
                System.out.println(media);

                leitura.close();
                enviar.close();
                cliente.close();

        }
}
