import java.sql.Timestamp;
import java.util.Date;
import java.util.ArrayList;

public class TimeStamp {
  private long received;
  private ArrayList<Long> timestamps;


  public TimeStamp(){
    this.received   = 0L;
    this.timestamps = new ArrayList<>();
  }

  public TimeStamp(long r, ArrayList<Long> a){
    this.received   = r;
    this.timestamps = new ArrayList<>();
    for(long aux : a){
      this.timestamps.add(aux);
    }
  }

  public long getReceived(){
    return this.received;
  }

  public ArrayList<Long> getTimeList(){
    return this.timestamps;
  }

  public void showTimestamps(ArrayList<Long> a){
    long timeSeconds;
    for(long aux : a){
      timeSeconds=aux/1000;
      System.out.println("Timestamps: "+ timeSeconds + " seconds.");
    }
  }

  public ArrayList<Long> timestampsToLong(ArrayList<Timestamp> sent){
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

  public long minTimeDiff(long received,ArrayList<Long> a){
       long result=0L,min=100000L;
       for(long aux : a){
         result = (received - aux)/1000;
         if(result <= min) min = result;
       }
       return min;
  }

}
