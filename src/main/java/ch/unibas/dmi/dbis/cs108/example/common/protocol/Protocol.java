package ch.unibas.dmi.dbis.cs108.example.common.protocol;

import java.util.List;

public class Protocol {

  //this is the serer protocol - its used to encode and decode server packages
  //so far the server doesnt use this to send packages because it just decodes the type of message with if logic
  //this should probalby be used right here but im too sure on how to implement this definitley when facing the garbage inputs this should be used

  private Protocol() {} // no instances of this class

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

    // Determine command (FIX for if no second part)
    if (parts.length >= 2) {
      cmdToken = parts[0];
      rest = parts[1];
    } else {
      cmdToken = line;
      rest = "";
    }

    try {
      Command cmd = Command.valueOf(cmdToken); // tests if the command is in the enum

      if (rest.isEmpty()) return new Packet(cmd, List.of()); //empty list
      return Packet.of(cmd, rest);
    } catch (IllegalArgumentException e) { //catch if cmd isnt known
      throw new IllegalArgumentException("Faced with unsupported Command token: " + cmdToken);
    }
  }

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
