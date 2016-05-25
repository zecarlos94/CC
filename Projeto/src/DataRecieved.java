/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Gustavo
 */
public class DataRecieved {
    
    Packet[] buffer;
    int index; // first elem
    int size;
    int MAX_SIZE = 2048;
    
    
    class Packet{
        private int seq;
        byte[] data;
        public Packet(byte[] d,int s,int packet_size){
           seq = s;
           data = new byte[packet_size];
           for(int i = 0; i < packet_size;i++) data[i] = d[i];
        }
    }
    
    public DataRecieved(){
        buffer = new Packet[MAX_SIZE];
        size = 0;
        index = 0;
    }
    
    public synchronized void push(byte[] p,int s,int packet_size){
        buffer[(index + size)%MAX_SIZE] = new Packet(p,s,packet_size);
        size++;
    }
    public synchronized byte[] pop(){
        size--;
        byte[] r = buffer[index].data;
        index = ++index % MAX_SIZE; 
        return r;
    }
    /*
    public synchronized void cleanLastPopped(){
        int i = (index == 0 )? MAX_SIZE - 1 : index-1;
        buffer[index-1] = null;
    }*/
    
    public synchronized int firstSeq(){
        return buffer[index].seq;
    }
    
    private void sort(){
        for(int i = index,c =0; c < size - 1;i= ++i % MAX_SIZE,c++){
            int min = buffer[i].seq;
            for(int j = i + 1; j < size ;j++ ){
                if(buffer[j].seq < min){
                    Packet tmp = buffer[i];
                    buffer[i] = buffer[j];
                    buffer[j] = tmp; min = buffer[i].seq;
                }
            }
        }
    }
    
    public synchronized boolean hasSegment(int seqI){
        boolean r = false;
        for(int i = index,c = 0; c < size; c++,i = ++i % MAX_SIZE ){
            if(buffer[i].seq == seqI){r = true; c = size;}
        }
        return r;
    }
    
    // returns Number of packets in sequence
    public synchronized int getNPackets(){
        sort();
        int n = 1;
        for(int i = index, next = (index + 1) % MAX_SIZE; buffer[next]!=null && (buffer[i].seq + 1) == buffer[next].seq ; n++){
            i = ++i % MAX_SIZE;
            next = ++next % MAX_SIZE;
        }
        return n;
    }
    
    
    
    
}
