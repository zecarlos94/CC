import java.util.*;
import java.io.*;
import java.nio.charset.Charset;


public class conversionsCharAndByte{

   private static final Charset UTF8_CHARSET = Charset.forName("UTF-8");
   public static void main(String args[]){
      String Str1 = new String("Zé Carlos");

      try{
         byte[] Str2 = Str1.getBytes( "UTF-8" );
         System.out.println("String to Byte[] " + Str2 );

	 String s = new String(Str2, UTF8_CHARSET);
	 System.out.println("Byte[] to String: "+s);

      }catch( UnsupportedEncodingException e){
         System.out.println("Unsupported character set");
      }
   }
}

