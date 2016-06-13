/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Gustavo
 */
public class WriteToFileBuffer {
    
    private int max_size = 1000; //buffer
    
    int threads[];
    Map<Integer,Thread> threadsInfo;
    int size;
    int printed;
    
    //FileChannel file;
    FileOutputStream fos;
    
    
    public WriteToFileBuffer(FileOutputStream fos){
        size = 0;
        printed = 0;
        this.fos = fos;
        threads = new int[max_size];
        threadsInfo = new HashMap<Integer,Thread>();
    }
    
    public int getPrinted(){ return printed; }
    public int getSize(){ return size; }
    
    public void add(int packet_index, byte packet_data[]){
        threads[size++] = packet_index;
        Thread writeJob = new WriteJob(packet_data);
        threadsInfo.put(packet_index,writeJob);
    }
    
    public synchronized void update() {
        if (printed < size) Arrays.sort(threads, printed, size);
            else return;
        List<Thread> jobs = new ArrayList<Thread>();
        while(threads[printed] == printed && threadsInfo.containsKey(printed)){
            Thread write = threadsInfo.get(printed);
            write.start();
            
            jobs.add(write);
            threadsInfo.remove(printed);
            printed++;
            try {write.join();} catch (Exception ex) { }
        }
        /*
        for(Thread job : jobs)
            try {      job.join();} catch (Exception ex) { }
        */
        
        if(jobs.size()>0) System.out.println("Writed " + jobs.size() + " segments");
        notifyAll();
    }
    // call when every packet is confirmed
    public synchronized void waitBuffer(){
        while(printed <= threads[size]) 
            try { wait(); } catch (Exception ex) {}
        
        System.out.println("Total segments writted to file:" + printed);
        try { fos.close(); } catch (IOException ex) {}
    }
    
    
    
    class WriteJob extends Thread{
        byte[] data;
        
        public WriteJob(byte[] d){
            int dataSize = d.length - PDU.EXTENDED_HEADER_SIZE;
            this.data = new byte[dataSize];
            // remove header 
            for(int i = PDU.EXTENDED_HEADER_SIZE,di = 0 ; di < dataSize ;i++,di++)
                data[di] = d[i];
        }
        public void run(){
            try {
                fos.write(data,0,data.length);
                fos.flush();
            } catch (Exception ex) {
            //  Logger.getLogger("Segment failed to write to file").log(Level.SEVERE, null, ex);
            }
        }
    }
    
}
