
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * Shared memory between ClientReciever Thread and ClientUDPTransmission
 * Shares probeResponse TimeStamp
 * @author Gustavo
 */
public class ClientExchangeFile {
    private String filename;
    private String banda;
    private File file;
    private FileOutputStream fos;
    private long OWD;
    public WriteToFileBuffer wb;
    private String username;
    
    public ClientExchangeFile(String user){
        username = user;
    }
    
    public synchronized void createFile(String filename) throws FileNotFoundException{
        this.filename = filename;
        //this.banda = banda;
        
        
        File dir = new File(username);
        if(!dir.exists()){
              dir.mkdir();
        }
        
        file = new File(dir, "R" +filename);
        
        fos = new FileOutputStream(file);
        wb = new WriteToFileBuffer(fos);
    }
    
    public synchronized void setOWD(long t) { this.OWD = t;}
    
    public synchronized long getOWD(){ return OWD;}
    public synchronized FileOutputStream getFileOutputStream(){return fos;}
    public synchronized String getFilename(){return filename;}
    public synchronized String getBanda(){return banda;}
    
}
