package ch.unibas.dmi.dbis.cs108.phantomhunt.util;

import ch.unibas.dmi.dbis.cs108.phantomhunt.common.protocol.Packet;
import ch.unibas.dmi.dbis.cs108.phantomhunt.server.lobby.Lobby;
import ch.unibas.dmi.dbis.cs108.phantomhunt.server.net.ClientHandler;

import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class FakeClientHandler extends ClientHandler {
  public List<Packet> receivedPackets = new ArrayList<>();
  private String fakeName;
  private Lobby fakeLobby;
  private boolean wisdomRoundBonus;

  public FakeClientHandler(String name) {
    super(new Socket(), null, null); // dead socket
    this.fakeName = name;
  }

  @Override
  public String getName() {
    return fakeName;
  }

  @Override
  public void sendMessage(Packet p) {
    receivedPackets.add(p);
  }

  @Override
  public Lobby getCurrentLobby() {
    return fakeLobby;
  }

  @Override
  public void setCurrentLobby(Lobby lobby) {
    this.fakeLobby = lobby;
  }

  @Override
  public boolean isInLobby(Lobby lobby) {
    return this.fakeLobby == lobby;
  }

  public void setWisdomRoundBonus(boolean wisdomRoundBonus) {
    this.wisdomRoundBonus = wisdomRoundBonus;
  }

  @Override
  public synchronized boolean consumeWisdomRoundBonus() {
    boolean ready = wisdomRoundBonus;
    wisdomRoundBonus = false;
    return ready;
  }
}
