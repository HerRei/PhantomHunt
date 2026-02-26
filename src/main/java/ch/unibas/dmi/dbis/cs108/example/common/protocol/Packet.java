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

  /**
   * Instantiates a new Packet.
   *
   * @param cmd the cmd
   * @param args the args
   */
public Packet(Command cmd, List<String> args) {
    this.cmd = cmd;
    this.args = args;
  }

  /**
   * Cmd command.
   *
   * @return  the command
   */
// getters
  public Command cmd() {

    return cmd;
  }

  /**
   * Args list.
   *
   * @return  the list
   */
public List<String> args() {
    return args;
  }

  /**
   * Argc int.
   *
   * @return  the int
   */
public int argc() {
    return (args == null) ? 0 : args.size();
  }

  /**
   * Text string.
   *
   * @return  the string
   */
// #todo
  // useful error if package was empty, also idk when this would be the case anyway
  public String text() {

    if (args == null) throw new UnsupportedOperationException();
    return args.get(0);
  }

  /**
   * Of packet.
   *
   * @param cmd the cmd
   * @param args the args
   * @return the packet
   */
  // Methode zum einfach instanzen zu machen
  public static Packet of(Command cmd, String... args) {
    return new Packet(cmd, Arrays.asList(args));
  }

  @Override
  public String toString() {
    return "Packet[command=" + cmd + ", args=" + args + "]";
  }
}
