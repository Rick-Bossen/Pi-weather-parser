package unwdmi;

import unwdmi.threads.ConsumerThread;
import unwdmi.threads.CountTread;
import unwdmi.threads.ProducerThread;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private static final int PORT = 7789;

    private ByteArrayOutputStream stream;

    public static void main(String[] args){
        Server server = new Server();
        server.start();
    }

    private Server(){
        stream = new ByteArrayOutputStream();
    }

    private void start(){
        try(ServerSocket server = new ServerSocket(PORT)) {
            System.err.println("Server started with port: " + PORT);

            System.err.println("Spawning 1 consumer thread");
            Thread consumer = new Thread(new ConsumerThread(stream));
            consumer.setPriority(8);
            consumer.start();

            System.err.println("Spawning 1 counting thread");
            new Thread(new CountTread(stream)).start();

            while (true) {
                Socket connection = server.accept();
                new Thread(new ProducerThread(connection, stream)).start();
            }
        }
        catch (IOException e) {
            System.err.printf("Cannot start server with port: %d\n", PORT);
        }
    }

}
