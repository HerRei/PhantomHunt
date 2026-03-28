package ch.unibas.dmi.dbis.cs108.example.server.state;

import ch.unibas.dmi.dbis.cs108.example.common.protocol.Command;
import ch.unibas.dmi.dbis.cs108.example.common.protocol.Packet;
import ch.unibas.dmi.dbis.cs108.example.server.net.ClientHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.Optional;
import java.util.Vector;

public class Lobby {

    private boolean isTheGameRunning = false;
    private final Vector<ClientHandler> players = new Vector<>();
    private final Vector<ClientHandler> spectators = new Vector<>();
    private final String id;
    private final String name;
    private final ClientHandler host;

    private static final Logger LOGGER = LogManager.getLogger(Lobby.class);

    public Lobby(String id, String name, ClientHandler host) {
        this.id = id;
        this.name = name;
        this.host = host;
        this.players.add(host);
        LOGGER.info("Lobby {} ({}) created by {}", name, id, host.getName());
    }

    public boolean isTheGameRunning() {
        return isTheGameRunning;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public ClientHandler getHost() { return host; }
    public Optional<Vector<ClientHandler>> getPlayers() {
        return Optional.of(players);
    }

    public Optional<Vector<ClientHandler>> getSpectators() {
        return Optional.of(spectators);
    }

    public boolean addPlayer(ClientHandler player) {
        if (isTheGameRunning) {
            player.sendMessage(Packet.of(Command.REJECT, "Game is already running."));
            return false;
        }
        if (players.contains(player)) {
            LOGGER.warn("Player {} is already in lobby {}", player.getName(), this.id);
            return false;
        }
        if (players.size() >= 4) {
            LOGGER.warn("This lobby is already full");
            return false;
        }

        players.add(player);
        LOGGER.info("Player {} joined lobby {}", player.getName(), this.name);
        return true;
    }

    public boolean removePlayer(ClientHandler player) {
        if (isTheGameRunning) {
            player.sendMessage(Packet.of(Command.REJECT, "Game is already running."));
            return false;
        }
        if (!players.contains(player)) {
            LOGGER.warn("Player {} is not in lobby {}", player.getName(), this.id);
            return false;
        }

        players.remove(player);
        LOGGER.info("Player {} left lobby {}", player.getName(), this.id);
        return true;
    }

    public boolean addSpectator(ClientHandler spectator) {
        if (spectators.contains(spectator)) {
            LOGGER.warn("Spectator {} is already in lobby {}", spectator.getName(), this.id);
            return false;
        }
        spectators.add(spectator);
        LOGGER.info("Spectator {} joined lobby {}", spectator.getName(), this.name);
        return true;
    }

    public boolean removeSpectator(ClientHandler spectator) {
        if (!spectators.contains(spectator)) {
            LOGGER.warn("Spectator {} is not in lobby {}", spectator.getName(), this.id);
            return false;
        }
        spectators.remove(spectator);
        LOGGER.info("Spectator {} left lobby {}", spectator.getName(), this.id);
        return true;
    }

    public void startGame(ClientHandler requester) {
        if (requester != host) return;
        if (isTheGameRunning) return;
        isTheGameRunning = true;
        LOGGER.info("Starting the game in lobby {}", this.id);
        //here we need to actually start the lobby!!! (jan's task for the moment) game handler etc...
    }
}
