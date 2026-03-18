package ch.unibas.dmi.dbis.cs108.example.server.state;

import ch.unibas.dmi.dbis.cs108.example.common.protocol.Command;
import ch.unibas.dmi.dbis.cs108.example.common.protocol.Packet;
import ch.unibas.dmi.dbis.cs108.example.server.net.ClientHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.util.Optional;
import java.util.Optional;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class Lobby {

    private boolean isTheGameRunning = false;
    private Vector<ClientHandler> players = new Vector<>();
    private String id;
    private String name;
    private ClientHandler host;


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
        return Optional.ofNullable(players);
    }

    public boolean addPlayer(ClientHandler player) {

        if (isTheGameRunning) {
            player.sendMessage(Packet.of(Command.REJECT, "Game is already running."));
            return false;
        }
        else if (players.contains(player)) {
            LOGGER.warn("Player {} is already in lobby {}", player.getName(), this.id);
            return false;
        }
        else if(players.size()>= 4){
            LOGGER.warn("This lobby is already full");
            return false;
        }

        players.add(player);
        LOGGER.info("Player {} joined lobby {}", player.getName(), this.name);
        return true;
    }

    public boolean removePlayer(ClientHandler player) {
        if(isTheGameRunning){
            player.sendMessage(Packet.of(Command.REJECT, "Game is already running."));
            return false;
        }

        if(!players.contains(player)){
            LOGGER.warn("Player {} is not in lobby {}", player.getName(), this.id);
            return false;
        }

        players.remove(player);
        LOGGER.info("Player {} left lobby {}", player.getName(), this.id);
        return true;

    }

    public void startGame(ClientHandler requester){
        if(requester != host) return;
        if(isTheGameRunning) return;
        LOGGER.info("Starting the game in lobby {}", this.id);

        //here we need to actually start the lobby!!! (jan's task for the moment) game handler etc...
    }


}