package ch.unibas.dmi.dbis.cs108.example.server.state;

import ch.unibas.dmi.dbis.cs108.example.common.protocol.Command;
import ch.unibas.dmi.dbis.cs108.example.common.protocol.Packet;
import ch.unibas.dmi.dbis.cs108.example.server.net.ClientHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class LobbyHandler {

    private static final Logger LOGGER = LogManager.getLogger(LobbyHandler.class);

    private final ConcurrentHashMap<String, Lobby> lobbies = new ConcurrentHashMap<>(); //is a thread safe map
    private final AtomicInteger lobbyCounter = new AtomicInteger(1); //atmoic integer is thread safe!!

    public ConcurrentHashMap<String, Lobby> getLobbies(){
    return lobbies;
    }


    public void createLobby(String name, ClientHandler host){
        String id = "lobby" + lobbyCounter.getAndIncrement();
        Lobby lobby = new Lobby(id, name, host);
        lobbies.put(id, lobby);
        LOGGER.info("Lobby {} ({}) created by {}", name, id, host.getName());

        host.setCurrentLobby(lobby);
        host.sendMessage(Packet.of(Command.CLEARED, "Lobby created: " + name + "Id: " + id));
    }

    public void joinLobby(String id, ClientHandler player){ //NPE? idk if its ever cought like
        Lobby lobby = lobbies.get(id);
        if (lobby.addPlayer(player)) {
            player.setCurrentLobby(lobby);
        }
    }

    public void leaveLobby(String id, ClientHandler player){ //thrad saftey is not here, we have a race condition but whatevs oh and NPE's :)
        Lobby lobby = lobbies.get(id);
        if (lobby.removePlayer(player)) {
            player.setCurrentLobby(null);
            if (lobby.getPlayers().isEmpty()) {
                lobbies.remove(id);
        }
    }
}
