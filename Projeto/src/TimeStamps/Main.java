import java.sql.Timestamp;
import java.util.Date;
import java.util.ArrayList;

public class Main {
  public static void main( String[] args ) {
     java.util.Date clientReceived= new java.util.Date();
     long received = clientReceived.getTime();

     ArrayList<Long> timestamps = new ArrayList<>();
     Timestamp clientResponse1 = Timestamp.valueOf("2016-03-16 10:35:0.0");
     Timestamp clientResponse2 = Timestamp.valueOf("2016-03-16 09:33:0.0");
     Timestamp clientResponse3 = Timestamp.valueOf("2016-03-16 10:30:0.0");
     Timestamp clientResponse4 = Timestamp.valueOf("2016-03-16 10:01:0.0");
     Timestamp clientResponse5 = Timestamp.valueOf("2016-03-16 09:22:0.0");
     timestamps.add(clientResponse1.getTime());
     timestamps.add(clientResponse2.getTime());
     timestamps.add(clientResponse3.getTime());
     timestamps.add(clientResponse4.getTime());
     timestamps.add(clientResponse5.getTime());

     TimeStamp time = new TimeStamp(received,timestamps);
     long answer    = time.getReceived();
     System.out.println("Answer clientReceived is: "+ (answer/1000) + " seconds.");

     time.showTimestamps(timestamps);

    //  long diff = timestampsDiff(received,clientResponse1.getTime());
    //  System.out.println("Difference of Timestamps is: "+ diff + " seconds.");
    //  long minimum = minTimeDiff(received,timestamps);
    //  System.out.println("Minimum of Timestamps is: "+ minimum + " seconds.");
   }
}
