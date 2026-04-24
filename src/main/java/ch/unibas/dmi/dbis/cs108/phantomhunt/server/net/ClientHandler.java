package ch.unibas.dmi.dbis.cs108.phantomhunt.server.net;

import ch.unibas.dmi.dbis.cs108.phantomhunt.common.protocol.Command;
import ch.unibas.dmi.dbis.cs108.phantomhunt.common.protocol.Packet;
import ch.unibas.dmi.dbis.cs108.phantomhunt.common.protocol.Protocol;
import ch.unibas.dmi.dbis.cs108.phantomhunt.server.game.GameHandler;
import ch.unibas.dmi.dbis.cs108.phantomhunt.server.game.state.PlayerState;
import ch.unibas.dmi.dbis.cs108.phantomhunt.server.lobby.Lobby;
import ch.unibas.dmi.dbis.cs108.phantomhunt.server.lobby.LobbyHandler;
import ch.unibas.dmi.dbis.cs108.phantomhunt.server.session.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Handles the connection to a single client on the server. Listens for incoming packets, processes
 * them, and can send packets back.
 * <p>
 * <b>Attention:</b> Please refrain from putting any GameLogic into this class.
 * It should mainly handle Socket reading, Packet decoding, and sending.
 */
public class ClientHandler implements Runnable {

  private static final Logger LOGGER = LogManager.getLogger(ClientHandler.class);
  private final Socket socket;
  private final Registry registry;
  private final LobbyHandler lobbyHandler;
  private BufferedWriter out;
  private Lobby currentLobby;
  private String name;
  private long lastSeen = System.currentTimeMillis();
  private ScheduledExecutorService scheduler;

  // ---------------------------------------------------------------------------------------------
  // Constructor
  // ---------------------------------------------------------------------------------------------

  /**
   * Creates a new handler for a connected client.
   *
   * @param socket       The socket connection to the client.
   * @param registry     The server registry that manages all connected players.
   * @param lobbyHandler The handler for managing lobbies.
   */
  public ClientHandler(Socket socket, Registry registry, LobbyHandler lobbyHandler) {
    this.socket = socket;
    this.registry = registry;
    this.lobbyHandler = lobbyHandler;
  }

  // ---------------------------------------------------------------------------------------------
  // Getters and Setters
  // ---------------------------------------------------------------------------------------------

  /**
   * Returns the current nickname of this client.
   *
   * @return the client nickname, or {@code "UKNW"} if no nickname is set yet
   */
  public String getName() {
    if (name == null) {
      return "UKNW =(";
    }
    return name;
  }

  public Lobby getCurrentLobby() {
    return currentLobby;
  }

  public void setCurrentLobby(Lobby lobby) {
    this.currentLobby = lobby;
  }

  public boolean isInLobby(Lobby lobby) {
    return lobby == currentLobby;
  }

  // ---------------------------------------------------------------------------------------------
  // Communication Methods
  // ---------------------------------------------------------------------------------------------

  /**
   * Sends a packet to this client. This method is synchronized to safely allow other threads to
   * send messages to this client.
   *
   * @param p The packet to send.
   */
  public synchronized void sendMessage(Packet p) {
    if (p == null) {
      LOGGER.error("Server tried sending an empty packet");
      return;
    }
    if (out == null) {
      LOGGER.error("Output stream is closed. Cannot send message to {}", getName());
      return;
    }
    String str = Protocol.encode(p);
    try {
      out.write(str);
      out.newLine();
      out.flush();
    } catch (IOException e) {
      LOGGER.error("Error sending message to {}", getName(), e);
      disconnect();
    }
  }

  // ---------------------------------------------------------------------------------------------
  // Packet Handlers & Helper Methods
  // ---------------------------------------------------------------------------------------------

  /**
   * Unregisters the client and closes the underlying socket connection.
   */
  public void disconnect() {
    if (scheduler != null && !scheduler.isShutdown()) {
      scheduler.shutdownNow();
    }
    if (currentLobby != null) {
      lobbyHandler.leaveLobby(currentLobby.getId(), this);
    }
    registry.unregister(this);
    try {
      if (!socket.isClosed()) {
        socket.close();
      }
    } catch (IOException e) {
      LOGGER.error("Error while closing socket for client {}", getName(), e);
    }
    registry.broadcast(this, Packet.of(Command.INFO, getName() + ": left the Server"));
    LOGGER.info("Client {} disconnected.", name);
    sendPlayers();
  }

  /**
   * Starts periodic ping checks and disconnects the client if no pong arrives in time.
   */
  private void startPinging() {
    sendMessage(Packet.of(Command.PING));
    this.scheduler = Executors.newSingleThreadScheduledExecutor();
    scheduler.scheduleAtFixedRate(() -> {
      long now = System.currentTimeMillis();
      if (now - lastSeen > 16000) {
        LOGGER.warn("Ping timeout for {}, client kicked.", name);
        disconnect();
      } else {
        sendMessage(Packet.of(Command.PING));
      }
    }, 15, 15, TimeUnit.SECONDS);
  }

  /**
   * Updates the last-seen timestamp after a pong response from the client.
   */
  private void handlePong() {
    lastSeen = System.currentTimeMillis();
    LOGGER.trace("Received pong from {}", name);
  }

  /**
   * Broadcasts a global chat message from this client to all connected sessions.
   *
   * @param p packet containing the chat text
   */
  private void handleUnicom(Packet p) {
    String msg = String.join(" ", p.args());
    LOGGER.info("UNICOM from {}: {}", name, msg);
    registry.broadcast(this, Packet.of(Command.UNICOM, getName() + ": " + msg));
  }

  private void handleLobbyList(){
    sendMessage(Packet.of(Command.LIST_LOBBY, lobbyHandler.getLobbies()));
  }

  /**
   * Creates a new lobby with the given name.
   *
   * @param p The packet containing the lobby name.
   */
  private void handleMkl(Packet p) {
    if (currentLobby != null) {
      sendMessage(Packet.of(Command.REJECT, "You are already in a lobby."));
      return;
    }
    String lobbyName = String.join(" ", p.args());
    lobbyName = lobbyName.trim();
    lobbyName = lobbyName.replace(" ","");
    lobbyName = lobbyName.replace(":","");
    lobbyName = lobbyName.replace(";","");
    if (lobbyName.isBlank()){
      return;
    }
    LOGGER.info("MKL from {}: {}", name, lobbyName);
    lobbyHandler.createLobby(lobbyName, this);
  }

  /**
   * Handles yapping in the current lobby.
   *
   * @param p The packet containing the yap message.
   */
  private void handleYap(Packet p) {
    if (currentLobby == null) {
      sendMessage(Packet.of(Command.REJECT, "You are not in a lobby."));
      return;
    }
    String msg = String.join(" ", p.args());
    LOGGER.info("YAP from {}: {}", name, msg);
    registry.yapping(this, Packet.of(Command.YAP, getName() + ": " + msg));
  }

  /**
   * Handles a request to spectate a lobby.
   *
   * @param p The packet containing the lobby ID.
   */
  private void handleSpec(Packet p) {
    if (currentLobby != null) {
      sendMessage(Packet.of(Command.REJECT, "You are already in a lobby."));
      return;
    }
    if (p.argc() < 1) {
      sendMessage(Packet.of(Command.REJECT, "Lobby ID is required."));
      return;
    }
    String lobbyId = p.args().get(0);
    LOGGER.info("SPEC from {}: {}", name, lobbyId);
    lobbyHandler.spectateLobby(lobbyId, this);
  }

  /**
   * Sends a goodbye message and disconnects the client.
   */
  private void handleLogout() {
    LOGGER.info("Logging out {}.", name);
    sendMessage(Packet.of(Command.UNICOM, "Okay, Bye."));
    disconnect();
  }

  private void sendPlayers() {
    registry.broadcast(this, Packet.of(Command.PLAYERS, registry.names()));
  }

  private void handleLobbyLogout() {
    if (currentLobby == null) {
      return;
    }
    String lobbyId = currentLobby.getId();
    lobbyHandler.leaveLobby(lobbyId, this);
  }

  /**
   * Handles a client's check-in request to join a lobby.
   *
   * @param p The packet containing the lobby ID.
   */
  private void handleCheckin(Packet p) {
    if (currentLobby != null) {
      sendMessage(Packet.of(Command.REJECT, "You are already in a lobby."));
      return;
    }
    if (p.argc() < 1) {
      sendMessage(Packet.of(Command.REJECT, "Lobby ID is required."));
      return;
    }
    String lobbyId = p.args().get(0);
    LOGGER.info("CHECKIN from {}: {}", name, lobbyId);
    lobbyHandler.joinLobby(lobbyId, this);
  }

  /**
   * Extracts the whisper target and message text and forwards both to the registry.
   *
   * @param p packet containing whisper target and message text
   */
  private void handleWhisper(Packet p) {
    String[] args = p.args().get(0).split(" ", 2);
    if (args.length < 2) {
      sendMessage(Packet.of(Command.REJECT, "Invalid whisper format. Use: WHISPER <user> <message>"));
      return;
    }
    String target = args[0];
    String message = args[1];
    if (!registry.whisper(this, target, message)) {
      sendMessage(Packet.of(Command.REJECT, "User not found: " + target));
    }
  }

  private void handleInput(Packet p) {
    Lobby lobby = this.getCurrentLobby();
    if (lobby == null) {
      return;
    }

    GameHandler gameHandler = lobby.getActiveGame().orElse(null);
    if (gameHandler == null) {
      return;
    }

    try {
      if (p.argc() < 1) {
        return;
      }
      String[] inputs = p.args().get(0).split("\\s+");
      if (inputs.length < 2) {
        sendMessage(Packet.of(Command.REJECT, "Invalid input packet."));
        return;
      }
      int vertical = Integer.parseInt(inputs[0]);
      int horizontal = Integer.parseInt(inputs[1]);

      gameHandler.updateInput(getName(), vertical, horizontal);

    } catch (Exception e) {
      LOGGER.error("Error parsing input for player {}: {}", name, p.args());
    }
  }

  private void handleGameFinish() {
    Lobby lobby = this.getCurrentLobby();
    if (lobby == null) return;

    GameHandler gameHandler = lobby.getActiveGame().orElse(null);
    if (gameHandler == null) return;

    if (lobby.getHost() == this) {
      lobbyHandler.resetLobby(lobby.getId());
    }
  }

  private void handleAbility() {
    Lobby lobby = this.getCurrentLobby();
    if (lobby == null) {
      return;
    }

    GameHandler gameHandler = lobby.getActiveGame().orElse(null);
    if (gameHandler == null) {
      return;
    }
    LOGGER.info("Ability wird gehandelt");

    gameHandler.tryAbility(getName());
  }

  /**
   * Applies a nickname change request and ensures the assigned nickname is unique.
   *
   * @param p packet containing the requested nickname
   */
  private void handleNickChange(Packet p) {
    String newNick = String.join(" ", p.args()).replaceAll("\\s+", "_");

    if (newNick.isBlank()) {
      sendMessage(Packet.of(Command.REJECT, "Nickname cannot be blank."));
      return;
    }
    String oldName = this.name;
    String assignedNick = registry.claimName(newNick, this);
    this.name = assignedNick;

    if (oldName != null && !oldName.equals(assignedNick)) {
      registry.releaseName(oldName, this);
      sendMessage(Packet.of(Command.CLEARED, "NICK", this.name));
      LOGGER.info("Nick changed from {} to {}", oldName, this.name);
    }

    if (!assignedNick.equals(newNick)) {
      sendMessage(Packet.of(Command.REJECT, "Name was taken. You are now: " + this.name));
    }
    sendMessage(Packet.of(Command.WELCOME, this.name));
    if (oldName != null) {
      registry.broadcast(this, Packet.of(Command.INFO, oldName + ": changed nickname to -> " + this.name));
    } else {
      registry.broadcast(this, Packet.of(Command.INFO, "Welcome to the Server: " + this.name));
    }
    sendPlayers();
  }

  private void handleStart(Packet p) {
    if (currentLobby == null) {
      sendMessage(Packet.of(Command.REJECT, "You are not in a lobby."));
      return;
    }
    lobbyHandler.startGame(currentLobby.getId(), this);
  }

  private void handleShowHighscore() {
    sendMessage(Packet.of(Command.SHOW_HIGHSCORE, registry.getHighscoreBoard()));
  }

  /**
   * Handles unsupported commands by informing the client that the command is rejected.
   *
   * @param p unsupported packet
   */
  private void handleDefault(Packet p) {
    LOGGER.warn("Unsupported command from {}: {}", name, p.cmd());
    sendMessage(Packet.of(Command.REJECT, "Unsupported command: " + p.cmd()));
  }

  // ---------------------------------------------------------------------------------------------
  // Main Logic
  // ---------------------------------------------------------------------------------------------

  /**
   * The main loop for this client thread. It constantly reads incoming text, decodes it into
   * Packets, and triggers the correct action based on the command.
   */
  @Override
  public void run() {
    try (var in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
         var out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))) {
      this.out = out;
      registry.register(this);
      LOGGER.info("New client connected: {}", name);
      startPinging();
      String line;
      while ((line = in.readLine()) != null) {
        try {
          Packet p = Protocol.decode(line);
          LOGGER.trace("Received packet from {}: {}", name, line);
          switch (p.cmd()) {
            case PONG -> handlePong();
            case UNICOM -> handleUnicom(p);
            case LOGOUT -> {
              handleLogout();
              return;
            }
            case INPUT -> handleInput(p);
            case ABILITY -> handleAbility();
            case NICK -> handleNickChange(p);
            case WHISPER -> handleWhisper(p);
            case CHECKIN -> handleCheckin(p);
            case LIST_LOBBY -> handleLobbyList();
            case GAME_FINISH -> handleGameFinish();
            case YAP -> handleYap(p);
            case MKL -> handleMkl(p);
            case SPEC -> handleSpec(p);
            case START -> handleStart(p);
            case LOGOUT_LOBBY -> handleLobbyLogout();
            case SHOW_HIGHSCORE -> handleShowHighscore();
            default -> handleDefault(p);
          }
        } catch (IllegalArgumentException e) {
          LOGGER.error("Invalid packet from {}: {}", name, line, e);
          sendMessage(Packet.of(Command.REJECT, e.getMessage()));
        }
      }
    } catch (IOException e) {
      LOGGER.error("IOException in ClientHandler for {}: {}", getName(), e.getMessage());
    } finally {
      disconnect();
    }
  }
}
