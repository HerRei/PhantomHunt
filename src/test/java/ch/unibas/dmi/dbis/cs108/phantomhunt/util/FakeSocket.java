package ch.unibas.dmi.dbis.cs108.phantomhunt.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class FakeSocket extends Socket {
  private final InputStream in;
  private final ByteArrayOutputStream out;
  private boolean closed = false;

  public FakeSocket(String simulatedInput) {
    this.in = new ByteArrayInputStream(simulatedInput.getBytes(StandardCharsets.UTF_8));
    this.out = new ByteArrayOutputStream();
  }

  public FakeSocket(InputStream customIn) {
    this.in = customIn;
    this.out = new ByteArrayOutputStream();
  }

  @Override
  public InputStream getInputStream() {
    return in;
  }

  @Override
  public OutputStream getOutputStream() {
    return out;
  }

  @Override
  public synchronized void close() {
    closed = true;
  }

  @Override
  public boolean isClosed() {
    return closed;
  }

  public String getSentData() {
    return new String(out.toByteArray(), StandardCharsets.UTF_8);
  }

  public void clearOutput() {
    out.reset();
  }
}
