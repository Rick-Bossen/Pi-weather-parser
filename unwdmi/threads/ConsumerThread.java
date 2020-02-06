package unwdmi.threads;

import java.io.*;

/**
 * This class represents a consumer thread.
 * The consumer thread writes the generated data from a {@link ByteArrayOutputStream} to a binary file in the predefined path.
 *
 * @author Rick
 * @author Martijn
 */
public class ConsumerThread implements Runnable {

    private ByteArrayOutputStream stream;

    private static final String PATH = "/mnt/nfs/var/nfs/temp_files/";

    /**
     * Initializes new ConsumerThread.
     *
     * @param stream The {@link ByteArrayOutputStream} to be used.
     */
    public ConsumerThread(ByteArrayOutputStream stream){
        this.stream = stream;
    }

    /**
     * Function that writes all the data from the {@link ByteArrayOutputStream} to a binary file in the predefined path.
     */
    private void write(){
        int unixTime = (int)(System.currentTimeMillis() / 1000L);
        File file = new File(PATH + unixTime + ".dat");
        try {
            FileOutputStream output = new FileOutputStream(file, true);
            stream.writeTo(output);
            stream.reset();
            output.close();
        } catch (IOException e){
            e.printStackTrace();
        }
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