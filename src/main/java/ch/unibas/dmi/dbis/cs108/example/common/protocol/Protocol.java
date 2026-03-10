package ch.unibas.dmi.dbis.cs108.example.common.protocol;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class Protocol {

  private static final Logger LOGGER = LogManager.getLogger(Protocol.class);

  private Protocol() {} // no instances of this class

  // line decoder LF -> into a "Packet"
  public static Packet decode(String line) {
    if (line == null || line.isBlank()) {
      LOGGER.error("Decoding failed: input line is null or blank");
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
      LOGGER.debug("Command identified: {}", cmd);

      if (rest.isEmpty()) return new Packet(cmd, List.of()); //
      return Packet.of(cmd, rest); // keep tail as one arg
    } catch (IllegalArgumentException e) {
      LOGGER.error("Unknown command token received: {}", cmdToken);
      throw new IllegalArgumentException("Faced with unsupported Command token: " + cmdToken);
    }
  }

  // Encode a package into a line
  public static String encode(Packet p) {
    if (p == null || p.cmd() == null) {
      LOGGER.error("Encoding failed: Packet or Command is null");
      throw new IllegalArgumentException("Packet doesnt exist or has no command");
    }

    StringBuilder sb = new StringBuilder();
    sb.append(p.cmd().name());

    List<String> args = p.args();
    if (args == null || args.isEmpty()) {
      LOGGER.debug("Encoded packet to string: {}", sb.toString());
      return sb.toString();
    }

    for (String s : args) {
      sb.append(' ').append(s);
    }
    LOGGER.debug("Encoded packet to string: {}", sb.toString());
    return sb.toString();
  }
}
