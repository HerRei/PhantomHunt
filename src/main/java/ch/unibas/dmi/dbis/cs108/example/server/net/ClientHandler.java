package ch.unibas.dmi.dbis.cs108.example.server.net;

import ch.unibas.dmi.dbis.cs108.example.server.state.Registry;
import ch.unibas.dmi.dbis.cs108.example.common.protocol.*;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


//# todo do the pings/pongs and kick dead clients

/**
 * The type Client handler.
 */

public class ClientHandler implements Runnable {

  private final Socket socket;
  private BufferedWriter out; //used for the chat function
  private final Registry registry; //keeps all of the users
  private String name; // nickname
  private long lastSeen; //used for PingPong
  private ScheduledExecutorService scheduler;

  //getter for the name as to keep access private
  public String getName(){
    if (name == null) return "UKNW";
    return name;
  }

  //constructor
  public ClientHandler(Socket socket, Registry registry) {
    this.socket = socket;
    this.registry = registry;
  }


  @Override
  public void run() {
    //try with resources to not get a leak
    try (BufferedReader in =
             new BufferedReader(
                 new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
         BufferedWriter out =
             new BufferedWriter(
                 new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))) {
      this.out = out;
      registry.register(this); //hand the client to the register
      String line;
      pinging();

      //input loop, as soon as a commamd is entered this will run.
      //#todo input validation, reject garbage inputs!(i
      while ((line = in.readLine()) != null) {
        Packet p;

        try {
          p = Protocol.decode(line);
        } catch (IllegalArgumentException e) {
          sendMessage((Packet.of(Command.REJECT, e.getMessage())));
          continue;
        }


        switch (p.cmd()) {

          case PONG -> {
            lastSeen = System.currentTimeMillis();
            break;
          }

          case UNICOM -> {
            String msg = (p.argc() >= 1) ? p.args().get(0) : "";
            registry.broadcast(this, (Packet.of(Command.UNICOM, msg)));
          }

          case LOGOUT -> {
            sendMessage(Packet.of(Command.UNICOM, "Okay, Bye") );
            disconnect();
            return; // triggers finally which cleans up
          }

          case NICK -> {
            String nick = (p.argc() >= 1) ? p.args().get(0) : "";

            if (nick.isBlank()) {
              sendMessage((Packet.of(Command.REJECT, "Error - no name found")));
              continue;
            }
            if (!registry.claimName(nick, this)) {
              sendMessage((Packet.of(Command.REJECT, "Name taken")));
              registry.claimName(NameGenerator.randomName(), this);
              continue;
            }

            if(this.name != null){
              registry.releaseName(this.name, this);
            }
            this.name = nick;

            sendMessage((Packet.of(Command.CLEARED, "NICK", this.name)));

          }

          default -> {
            sendMessage((Packet.of(Command.REJECT, "Unsupported command: " + p.cmd())));
          }
        }
      }


    } catch (IOException e) { //this is for the try with resources to be memorysafe
      // do nothing
    } finally {
      disconnect();
    }
  }

  //pings the player all 15 seconds and handles if he left
  public void pinging(){
    sendMessage(Packet.of(Command.PING));
    this.scheduler = Executors.newSingleThreadScheduledExecutor();
    scheduler.scheduleAtFixedRate(() -> {
      long now = System.currentTimeMillis();
      if (now - lastSeen > 16000)//checks if lastpong > 16 seconds
      {
        disconnect();
        System.err.println("Timeout: No Pong received from " + (name != null ? name : "client"));
      }
      else {
        // Send the Ping again
        sendMessage(Packet.of(Command.PING));
      }
    }, 15, 15, TimeUnit.SECONDS);
  }

  private void assignName(){
    double x = Math.random() * 10;
    String name = Double.toString(x);
    registry.claimName(name, this);
  }


  //this is just resetting the bufferdwriter and clearing the register and sockets
  //Maybe this should not be public????
  public void disconnect() {

    try {
      this.out = null;
      registry.unregister(this);
      socket.close();
    } catch (IOException e) {
      //do nothing
    }
  }

  public void sendMessage(Packet p) {
    // synchronising fixes the thread issue of the thread-per-client server that this here is
    // is used to send messages to people conneected to the server with then the according logic
    String str = Protocol.encode(p);
    synchronized (this.out) {
      try {
        this.out.write(str);
        this.out.newLine();
        this.out.flush();
      } catch (IOException e) {
        e.getMessage();
      }
    }
  }
}
