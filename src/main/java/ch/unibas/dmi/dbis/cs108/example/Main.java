package ch.unibas.dmi.dbis.cs108.example;

import ch.unibas.dmi.dbis.cs108.example.client.net.TcpClient;
import ch.unibas.dmi.dbis.cs108.example.common.protocol.NameGenerator;
import ch.unibas.dmi.dbis.cs108.example.server.net.TcpServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class Main {

    private static final Logger LOGGER = LogManager.getLogger(Main.class);
    private static final String DEFAULT_HOST = "localhost"; // Loopback domain
    private static final int DEFAULT_PORT = 2222; // Port which we also use in ServerApp

    public static void main(String[] args) {
        // [0] = server/client [1] = host [2] = port
        if (args.length >= 3) {
            String host = args[1];
            int port = Integer.parseInt(args[2]);
            switch (args[0]) {
                case "client" -> {
                    connect(host, port);
                }

                case "server" -> {
                    startServer(port);
                }

                default -> {
                    LOGGER.error("Invalid role: {}", args[0]);
                }
            }
        }

        else if(args.length >= 1){
            switch (args[0]) {
                case "client" -> {
                    connect(DEFAULT_HOST, DEFAULT_PORT);
                }

                case "server" -> {
                    startServer(DEFAULT_PORT);
                }

                default -> {
                    LOGGER.error("Invalid role: {} (join, host)", args[0]);
                }
            }
        }

        else {
            LOGGER.error("Empty Input");
        }
    }

    /**
     * @param port port for socket
     * User is host
     */
    public static void startServer(int port){
        LOGGER.info("Starting Server...");
        CountDownLatch serverReadySignal = new CountDownLatch(1); //used to know when server ready

        // Start Server via thread
        Thread serverThread = new Thread(() -> {
            TcpServer server = new TcpServer(port, serverReadySignal);
            server.start();
        });
        serverThread.start();
    }

    /**
     * @param host ip-adress for socket
     * @param port port for socket
     * User is client
     */
    public static void connect(String host, int port) {
        LOGGER.info("Connecting to {}:{}...", host, port);
        new TcpClient(host, port);
        //#todo open ui and game loop...
    }

}