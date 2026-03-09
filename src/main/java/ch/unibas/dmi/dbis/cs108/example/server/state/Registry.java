package ch.unibas.dmi.dbis.cs108.example.server.state;

import ch.unibas.dmi.dbis.cs108.example.common.protocol.Packet;
import ch.unibas.dmi.dbis.cs108.example.common.protocol.Protocol;
import ch.unibas.dmi.dbis.cs108.example.server.net.ClientHandler;

import java.util.concurrent.ConcurrentHashMap;

public final class Registry {

  // hash map that is thread safe as we dont want a O(n) lookup with e.g. vector that is also
  // threadsafe.
  // ArrayList doesnt(?) have a threadsafe version?!, also the thing is that there is no concurrent
  // hashset so we need to use boolean.TRUE
  // as a dummy key value pair, this is definently not the optimal solution so if anyone has a
  // better idea - please do indulge me.
  private final ConcurrentHashMap<ClientHandler, Boolean> sessions = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, ClientHandler> byName =
      new ConcurrentHashMap<>(); // Nickname handling

  // nickname logic - every person that is in the sessions need to  e in byName etc
  public boolean claimName(String name, ClientHandler h) {
    return byName.putIfAbsent(name, h)
        == null; // returns null when it works thus == null returns true if succesfull
  }

  public String names() {
    return byName.keySet().toString(); // this should return all the people that are in the registry
  }

  public void releaseName(String name, ClientHandler h) {
    byName.remove(name, h); // remove name
  }

  // keeping track of all of the people that got a client handler.
  public void register(ClientHandler h) {
    sessions.put(h, true); // autoboxing of Boolean.TRUE - might bring issues idk
  }

  // logged off players should be removed
  public void unregister(ClientHandler h) {
    sessions.remove(h);
  }

  // send messages to everyone exept for the sender with the bufferdrwriter from the ClientHandler
  // class

  public void broadcast(ClientHandler sender, Packet p) {
    String str = Protocol.encode(p);
    for (ClientHandler h : sessions.keySet()) {
      if (h == sender) continue;
      str = sender.getName() + ": " + str; //this breaks the protcol
      h.sendMessage(p);
    }
  }
}