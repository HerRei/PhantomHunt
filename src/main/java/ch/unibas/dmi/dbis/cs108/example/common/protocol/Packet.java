package ch.unibas.dmi.dbis.cs108.example.common.protocol;

import java.util.Arrays;
import java.util.List;

/**
 * This is the Package that gets sent around, rn not all methods are implemented - but for the start
 * i think this will do
 */
public final class Packet {

  private final Command cmd;
  private final List<String> args;


public Packet(Command cmd, List<String> args) {
    this.cmd = cmd;
    this.args = args;
  }

// getters
  public Command cmd() {

    return cmd;
  }

public List<String> args() {
    return args;
  }


public int argc() {
    return (args == null) ? 0 : args.size();
  }


// #todo
  // useful error if package was empty, also idk when this would be the case anyway
  public String text() {

    if (args == null) throw new UnsupportedOperationException();
    return args.get(0);
  }

  // Methode zum einfach instanzen zu machen - wird vermutlich oft passieren
  public static Packet of(Command cmd, String... args) {
    return new Packet(cmd, Arrays.asList(args));
  }

  @Override
  public String toString() {
    return "Packet[command=" + cmd + ", args=" + args + "]";
  }
}
