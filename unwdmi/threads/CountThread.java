package unwdmi.threads;

import java.io.ByteArrayOutputStream;

/**
 * This class represents a count tread.
 * The count tread keeps track of the amount of bytes currently in the {@link ByteArrayOutputStream} waiting to be written.
 *
 * @author Rick
 * @author Martijn
 */
public class CountThread implements Runnable {

    private ByteArrayOutputStream stream;

    /**
     * Initializes new CountThread.
     *
     * @param stream The {@link ByteArrayOutputStream} to keep track of.
     */
    public CountThread(ByteArrayOutputStream stream){
        this.stream = stream;
    }

    @Override
    public void run() {
        int s;
        int lastGc = 0;
        while (true){
            try {
                Thread.sleep(10000);
            } catch (InterruptedException ignored) { }

            // Output queue size
            s = stream.size();
                long durationInMillis = System.currentTimeMillis();
                long millis = durationInMillis % 1000;
                long second = (durationInMillis / 1000) % 60;
                long minute = (durationInMillis / (1000 * 60)) % 60;
                long hour = (durationInMillis / (1000 * 60 * 60)) % 24;

                String time = String.format("%02d:%02d:%02d.%d", hour, minute, second, millis);
                System.out.println("["+time+"] In Queue: " + s);

            if (lastGc > 100){ // 10 seconds
                lastGc = 0;
                System.gc();
            }else {
                lastGc++;
            }
        }
    }
}