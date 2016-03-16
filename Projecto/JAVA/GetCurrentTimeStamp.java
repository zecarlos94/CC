import java.sql.Timestamp;
import java.util.Date;

public class GetCurrentTimeStamp {

 public static long timestampsDiff(long received,long sent){
      /*1 second = 1000 milliseconds*/
      long result = (received - sent)/1000;
      return result;
 }

 public static void main( String[] args ) {
	 java.util.Date clientReceived= new java.util.Date();
	 System.out.println(new Timestamp(clientReceived.getTime()));
	 Timestamp clientResponse = Timestamp.valueOf("2016-03-16 10:35:0.0");
	 long diff = clientReceived.getTime() - clientResponse.getTime();
	 System.out.println("Difference of Timestamps is: "+ diff + " milliseconds.");
   long res = timestampsDiff(clientReceived.getTime(),clientResponse.getTime());
   System.out.println("Difference of Timestamps is: "+ res + " seconds.");
 }
}
