package unwdmi.threads;

import unwdmi.helpers.MeasurementHelper;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

/**
 * This class represents a producer thread.
 * A producer thread receives XML data from a {@link Socket} and passes it to a {@link MeasurementHelper} for conversion,
 * after which it adds it to the {@link ByteArrayOutputStream}.
 *
 * @author Rick
 * @author Martijn
 */
public class ProducerThread implements Runnable {

    private Socket connection;
    private ByteArrayOutputStream stream;


    /**
     * Initializes a new ProducerThread.
     *
     * @param connection The {@link Socket} to receive data from.
     * @param stream The {@link ByteArrayOutputStream} to add processed data to.
     */
    public ProducerThread(Socket connection, ByteArrayOutputStream stream){
        this.connection = connection;
        this.stream = stream;
    }

    @Override
    public void run() {
        MeasurementHelper helper = new MeasurementHelper();

        try(
            InputStreamReader input = new InputStreamReader(connection.getInputStream());
            BufferedReader reader = new BufferedReader(input)
        ){
            ArrayList<String> lines = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                if(line.equals("\t</MEASUREMENT>")){
                    stream.write(helper.parseXML(lines));
                    lines.clear();
                }else{
                    lines.add(line);
                }
            }
        }catch (IOException ignored){}
        finally {
            try {
                connection.close();
            } catch (IOException ignored) { }
        }
    }
}