package ch.unibas.dmi.dbis.cs108.example.server.state;

import ch.unibas.dmi.dbis.cs108.example.common.protocol.Command;
import ch.unibas.dmi.dbis.cs108.example.common.protocol.Packet;
import ch.unibas.dmi.dbis.cs108.example.server.net.ClientHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;
import java.util.Vector;
import java.util.concurrent.atomic.AtomicInteger;

public class LobbyHandler {

    private static final Logger LOGGER = LogManager.getLogger(LobbyHandler.class);

    private final Vector<Lobby> waitingLobbies = new Vector<>();
    private final Vector<Lobby> playingLobbies = new Vector<>();
    private final Vector<Lobby> finishedLobbies = new Vector<>();
    private final AtomicInteger lobbyCounter = new AtomicInteger(1);

    public Optional<Vector<Lobby>> getWaitingLobbies() {
        return Optional.ofNullable(waitingLobbies);
    }

    public Optional<Vector<Lobby>> getPlayingLobbies() {
        return Optional.ofNullable(playingLobbies);
    }

    public Optional<Vector<Lobby>> getFinishedLobbies() {
        return Optional.ofNullable(finishedLobbies);
    }

    public String getLobbies() {
        StringBuilder sb = new StringBuilder();
        for (Lobby lobby : waitingLobbies) {
            sb.append(lobby.getId()).append(", ");
        }
        for (Lobby lobby : playingLobbies) {
            sb.append(lobby.getId()).append(", ");
        }
        for (Lobby lobby : finishedLobbies) {
            sb.append(lobby.getId()).append(", ");
        }
        // Remove the trailing comma and space if the list is not empty
        if (sb.length() > 0) {
            sb.setLength(sb.length() - 2);
        }
        return sb.toString();
    }

    public void createLobby(String name, ClientHandler host) {
        String id = "lobby" + lobbyCounter.getAndIncrement();
        Lobby lobby = new Lobby(id, name, host);
        waitingLobbies.add(lobby);
        LOGGER.info("Lobby {} ({}) created by {}", name, id, host.getName());

        host.setCurrentLobby(lobby);
        host.sendMessage(Packet.of(Command.CLEARED, "Lobby created: " + name + " Id: " + id));
    }

    public void joinLobby(String id, ClientHandler player) {
        Optional<Lobby> lobbyOpt = findLobbyById(id, waitingLobbies);
        if (lobbyOpt.isEmpty()) {
            player.sendMessage(Packet.of(Command.REJECT, "Lobby not found or has already started: " + id));
            return;
        }

        Lobby lobby = lobbyOpt.get();
        if (lobby.addPlayer(player)) {
            player.setCurrentLobby(lobby);
        }
    }

    public void spectateLobby(String id, ClientHandler player) {
        Optional<Lobby> lobbyOpt = findLobbyById(id);
        if (lobbyOpt.isEmpty()) {
            player.sendMessage(Packet.of(Command.REJECT, "Lobby not found: " + id));
            return;
        }

        Lobby lobby = lobbyOpt.get();
        if (lobby.addSpectator(player)) {
            player.setCurrentLobby(lobby);
        }
    }

    public void leaveLobby(String id, ClientHandler player) {
        Optional<Lobby> lobbyOpt = findLobbyById(id);
        if (lobbyOpt.isEmpty()) return;

        Lobby lobby = lobbyOpt.get();
        if (lobby.removePlayer(player) || lobby.removeSpectator(player)) {
            player.setCurrentLobby(null);
            if (lobby.getPlayers().isPresent() && lobby.getPlayers().get().isEmpty() && lobby.getSpectators().isPresent() && lobby.getSpectators().get().isEmpty()) {
                waitingLobbies.remove(lobby);
                playingLobbies.remove(lobby);
                finishedLobbies.remove(lobby);
                LOGGER.info("Empty lobby {} removed.", id);
            }
        }
    }

    private Optional<Lobby> findLobbyById(String id) {
        Optional<Lobby> lobby = findLobbyById(id, waitingLobbies);
        if (lobby.isPresent()) {
            return lobby;
        }
        lobby = findLobbyById(id, playingLobbies);
        if (lobby.isPresent()) {
            return lobby;
        }
        return findLobbyById(id, finishedLobbies);
    }

    private Optional<Lobby> findLobbyById(String id, Vector<Lobby> lobbyList) {
        for (Lobby lobby : lobbyList) {
            if (lobby.getId().equals(id)) {
                return Optional.of(lobby);
            }
        }
        return Optional.empty();
    }
}
