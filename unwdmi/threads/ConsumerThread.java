package unwdmi.threads;

import java.io.*;

public class ConsumerThread implements Runnable {

    private ByteArrayOutputStream stream;

    private static final String PATH = "/mnt/nfs/var/nfs/temp_files/";

    public ConsumerThread(ByteArrayOutputStream stream){
        this.stream = stream;
    }

    private boolean write(){
        int unixTime = (int)(System.currentTimeMillis() / 1000L);
        File file = new File(PATH + unixTime + ".dat");
        try {
            FileOutputStream output = new FileOutputStream(file, true);
            stream.writeTo(output);
            stream.reset();
            output.close();
        } catch (IOException e){
            return false;
        }
        return true;
    }


    @Override
    public void run() {

        long last = 0;
        while (true){
            if (last + 1 < System.currentTimeMillis() && stream.size() > 0){
                last = System.currentTimeMillis();
                write();

            } else {
                try {
                    Thread.sleep(1);
                } catch (InterruptedException ignore) { }
            }
        }
    }
}
