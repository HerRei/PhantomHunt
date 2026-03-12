package ch.unibas.dmi.dbis.cs108.example.server.net;

import ch.unibas.dmi.dbis.cs108.example.server.state.Registry;
import ch.unibas.dmi.dbis.cs108.example.common.protocol.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

// # todo do the pings/pongs and kick dead clients

/** The type Client handler. */
public class ClientHandler implements Runnable {

  private static final Logger LOGGER = LogManager.getLogger(ClientHandler.class);
  private final Socket socket;
  private BufferedWriter out; // used for the chat function
  private final Registry registry; // keeps all of the users
  private String name; // nickname
  private long lastSeen = System.currentTimeMillis(); // used for PingPong
  private ScheduledExecutorService scheduler;

  // getter for the name as to keep access private
  public String getName() {
    if (name == null) return "UKNW";
    return name;
  }

  // constructor
  public ClientHandler(Socket socket, Registry registry) {
    this.socket = socket;
    this.registry = registry;
    this.name = NameGenerator.randomName();
    LOGGER.info("New client connected. {}", name);
  }

  @Override
  public void run() {
    // try with resources to not get a leak
    try (BufferedReader in =
            new BufferedReader(
                new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
        BufferedWriter out =
            new BufferedWriter(
                new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))) {
      this.out = out;
      registry.register(this); // hand the client to the register
      LOGGER.info("New client connected. {}", name);
      String line;
      pinging();

      // input loop, as soon as a commamd is entered this will run.
      // #todo input validation, reject garbage inputs!(i
      while ((line = in.readLine()) != null) {
        Packet p;

        try {
          p = Protocol.decode(line);
          LOGGER.trace("Received packet: {} with:", p, line);
        } catch (IllegalArgumentException e) {
          LOGGER.error("Invalid packet: {}", line, e);
          sendMessage((Packet.of(Command.REJECT, e.getMessage())));
          continue;
        }

        switch (p.cmd()) {
          case PONG -> {
            handlePong();
            break;
          }

          case UNICOM -> {
            handleUnicom(p);
          }

          case LOGOUT -> {
            LOGGER.info("Logging out {}.", name);
            handleLogout();
            return; // triggers finally which cleans up // if even triggerd at all (shoudlnt)
          }

          case NICK -> {
            handleNickChange(p);
          }

          case WHISPER -> {
            handleWhisper(p);
          }

          default -> {
            handleDefault(p);
          }
        }
      }

    } catch (IOException e) { // this is for the try with resources to be memorysafe
      LOGGER.error("IOException in ClientHandler for {}", getName(), e);
    } finally {
      disconnect();
    }
  }

  private void handlePong() {
    lastSeen = System.currentTimeMillis();
    LOGGER.trace("Received pong: {} from {}", lastSeen, name);
  }

  private void handleUnicom(Packet p) {
    String msg = (p.argc() >= 1) ? p.args().get(0) : "";
    LOGGER.trace("Received unicom: {} from {}", msg, name);
    registry.broadcast(this, (Packet.of(Command.UNICOM, getName() + ": " + msg)));
  }

  private void handleLogout() {
    sendMessage(Packet.of(Command.UNICOM, "Okay, Bye"));
    disconnect();
  }

  private void handleWhisper(Packet p) {
    String payload = p.argc() >= 1 ? p.args().get(0) : "";
    int space = payload.indexOf(' ');

    String target = payload.substring(0, space);
    String message = payload.substring(space + 1);

    if (registry.whisper(this, target, message)) {
      // this used to be here for debugging , might need this later when registry has errors
    } else {
      sendMessage(Packet.of(Command.REJECT, "User not found: " + target));
    }
  }

  private void handleDefault(Packet p) {
    LOGGER.warn("Received default: {} from {}", p.argc(), name);
    sendMessage((Packet.of(Command.REJECT, "Unsupported command: " + p.cmd())));
  }

  // pings the player all 15 seconds and handles if he left
  public void pinging() {
    LOGGER.trace("Pinging... {}", name);
    sendMessage(Packet.of(Command.PING));
    this.scheduler = Executors.newSingleThreadScheduledExecutor();
    scheduler.scheduleAtFixedRate(
        () -> {
          long now = System.currentTimeMillis();
          if (now - lastSeen > 16000) // checks if lastpong > 16 seconds
          {
            LOGGER.warn("Pinging took {}, client kicked.", now - lastSeen);
            disconnect();
            System.err.println(
                "Timeout: No Pong received from " + (name != null ? name : "client"));
          } else {
            // Send the Ping again
            LOGGER.debug("Pinging took {}, trying again", now - lastSeen);
            sendMessage(Packet.of(Command.PING));
          }
        },
        15,
        15,
        TimeUnit.SECONDS);
  }

  private void assignName() {
    double x = Math.random() * 10;
    String name = Double.toString(x);
    LOGGER.trace("Assigning name to {}:", name);
    registry.claimName(name, this);
  }

  private void handleNickChange(Packet p) {
    String newNick = (p.argc() >= 1) ? p.args().get(0) : "";

    if (newNick.isBlank()) {
      LOGGER.warn("Nick name is blank :(");
      sendMessage(Packet.of(Command.REJECT, "Error - no name found"));
      return;
    }

    String oldName = this.name;
    String assignedNick = registry.claimName(newNick, this);

    this.name = assignedNick;

    // Alten Namen aus der Registry löschen
    if (oldName != null && !oldName.equals(assignedNick)) {
      registry.releaseName(oldName, this);
    }

    sendMessage(Packet.of(Command.CLEARED, "NICK", this.name));
    LOGGER.info("Nick changed from {} to {}", oldName, newNick);

    if (!assignedNick.equals(newNick)) {
      LOGGER.warn("Nick forcefully changed due to duplicate from {} to {}", oldName, newNick);
      sendMessage(Packet.of(Command.REJECT, "Name was taken. You are now: " + this.name));
    }
  }

  // this is just resetting the bufferdwriter and clearing the register and sockets
  // Maybe this should not be public????
  public void disconnect() {

    try {
      scheduler.shutdown();
      this.out = null;
      registry.unregister(this);
      socket.close();
    } catch (IOException e) {
      LOGGER.error("Error while disconnecting client {}", getName(), e);
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
        LOGGER.error("Error sending message to {}", getName(), e);
      }
    }
  }
}