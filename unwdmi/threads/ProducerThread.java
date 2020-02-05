package unwdmi.threads;

import unwdmi.helpers.MeasurementHelper;

import java.io.*;
import java.util.List;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ProducerThread implements Runnable {

    private Socket connection;
    private ByteArrayOutputStream stream;


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

//            System.err.println("Connection closed for local port: " + connection.getPort());
            try {
                connection.close();
            } catch (IOException ignored) { }
        }
    }
}
