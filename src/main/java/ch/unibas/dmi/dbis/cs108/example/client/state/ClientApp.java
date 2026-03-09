package ch.unibas.dmi.dbis.cs108.example.client.state;

import ch.unibas.dmi.dbis.cs108.example.client.net.TcpClient;
import ch.unibas.dmi.dbis.cs108.example.common.protocol.Protocol;


public class ClientApp {

    private static final String DEFAULT_HOST = "localhost"; //Loopback domain
    private static final int DEFAULT_PORT = 2222; //Port which we also use in ServerApp

    //Creates a Client on host DEFAULT_HOST and port DEFAULTPORT
    public static void main(String[] args) {
        String host = DEFAULT_HOST;
        int port = DEFAULT_PORT;

        System.out.println("Connecting to " + host + ":" + port);
        TcpClient client = new TcpClient(host, port);

        try (java.util.Scanner scanner = new java.util.Scanner(System.in)) {
            while (scanner.hasNextLine()) {
                String input = scanner.nextLine();
                client.getServerHandler().send(Protocol.decode(input));
            }
        }
    }
}