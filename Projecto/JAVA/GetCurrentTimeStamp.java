import java.sql.Timestamp;
import java.util.Date;
import java.util.ArrayList;

public class GetCurrentTimeStamp {

public static void showTimestamps(ArrayList<Long> a){
  long timeSeconds;
  for(long aux : a){
    timeSeconds=aux/1000;
    System.out.println("Timestamps: "+ timeSeconds + " seconds.");
  }
}

public static ArrayList<Long> timestampsToLong(ArrayList<Timestamp> sent){
  ArrayList<Long> res = new ArrayList<>();
  for(Timestamp aux : sent){
    res.add(aux.getTime());
  }
  return res;
}
public static long timestampsDiff(long received,long sent){
     /*1 second = 1000 milliseconds*/
     long result = (received - sent)/1000;
     return result;
}

public static long minTimeDiff(long received,ArrayList<Long> a){
     long result=0L,min=100000L;
     for(long aux : a){
       result = (received - aux)/1000;
       if(result <= min) min = result;
     }
     return min;
}

public static void main( String[] args ) {
   ArrayList<Long> timestamps = new ArrayList<>();
   java.util.Date clientReceived= new java.util.Date();
   long received = clientReceived.getTime();
   System.out.println("clientReceived is: "+ (received/1000) + " seconds.");
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
   showTimestamps(timestamps);
   long diff = timestampsDiff(received,clientResponse1.getTime());
   System.out.println("Difference of Timestamps is: "+ diff + " seconds.");
   long minimum = minTimeDiff(received,timestamps);
   System.out.println("Minimum of Timestamps is: "+ minimum + " seconds.");
 }
}
