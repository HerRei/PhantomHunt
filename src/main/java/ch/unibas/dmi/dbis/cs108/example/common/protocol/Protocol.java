package ch.unibas.dmi.dbis.cs108.example.common.protocol;

import java.util.List;

/**
 * The type Protocol.
 */
public class Protocol {

  private Protocol() {} // no instances of this class

  /**
   * Decode packet.
   *
   * @param line the line
   * @return  the packet
   */
// line decoder LF -> into a "Packet"
  public static Packet decode(String line) {
    if (line == null || line.isBlank()) {
      throw new IllegalArgumentException("line cannot be empty");
    }

    line = line.trim();

    // Split by first whitespace
    String[] parts = line.split("\\s+", 2);

    String cmdToken;
    String rest;

    // Determine command (workaround for no message)
    if (parts.length >= 2) {
      cmdToken = parts[0];
      rest = parts[1];
    } else {
      cmdToken = line;
      rest = "";
    }

    try {
      Command cmd = Command.valueOf(cmdToken); // tests if the command is in the enum

      if (rest.isEmpty()) return new Packet(cmd, List.of()); //
      return Packet.of(cmd, rest); // keep tail as one arg
    } catch (IllegalArgumentException e) {
      throw new IllegalArgumentException("Faced with unsupported Command token: " + cmdToken);
    }
  }

  /**
   * Encode string.
   *
   * @param p the p
   * @return the string
   */
  // Encode a package into a line
  public static String encode(Packet p) {
    if (p == null || p.cmd() == null)
      throw new IllegalArgumentException("Packet doesnt exist or has no command");

    StringBuilder sb = new StringBuilder();
    sb.append(p.cmd().name());

    List<String> args = p.args();
    if (args == null || args.isEmpty()) return sb.toString();

    for (String s : args) {
      sb.append(' ').append(s);
    }
    return sb.toString();
  }
}
