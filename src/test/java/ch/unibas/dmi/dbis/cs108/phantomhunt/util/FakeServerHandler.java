package ch.unibas.dmi.dbis.cs108.phantomhunt.util;

import ch.unibas.dmi.dbis.cs108.phantomhunt.client.net.ServerHandler;
import ch.unibas.dmi.dbis.cs108.phantomhunt.common.protocol.Packet;

import java.net.Socket;

public class FakeServerHandler extends ServerHandler {
    public Packet lastSentPacket = null;

    public FakeServerHandler() {
        super(new Socket()); // dead Socket
    }

    @Override
    public synchronized void sendMessage(Packet p) {
        this.lastSentPacket = p;
    }
}
