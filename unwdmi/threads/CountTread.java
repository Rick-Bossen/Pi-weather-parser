package unwdmi.threads;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class CountTread implements Runnable {

    private ByteArrayOutputStream stream;

    public CountTread(ByteArrayOutputStream stream){
        this.stream = stream;
    }

    @Override
    public void run() {
        int i = 0, s;
        int lastGc = 0;
        while (true){
            try {
                Thread.sleep(10000);
            } catch (InterruptedException ignored) { }

            // Output queue size
            s = stream.size();
//            if(i != s){
                i = s;
                long durationInMillis = System.currentTimeMillis();
                long millis = durationInMillis % 1000;
                long second = (durationInMillis / 1000) % 60;
                long minute = (durationInMillis / (1000 * 60)) % 60;
                long hour = (durationInMillis / (1000 * 60 * 60)) % 24;

                String time = String.format("%02d:%02d:%02d.%d", hour, minute, second, millis);
                System.out.println("["+time+"] In Queue: " + s);
//            }

            if (lastGc > 100){ // 10 seconds
                lastGc = 0;
                System.gc();
            }else {
                lastGc++;
            }

        }
    }
}
