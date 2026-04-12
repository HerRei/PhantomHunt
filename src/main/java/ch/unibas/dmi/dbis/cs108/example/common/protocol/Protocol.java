package ch.unibas.dmi.dbis.cs108.example.common.protocol;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Translates between strings and packet objects.
 */
public class Protocol {

  private static final Logger LOGGER = LogManager.getLogger(Protocol.class);

  /**
   * Translates between strings and packet objects.
   */
  private Protocol() {}

  /**
   * Converts a text line into a packet.
   *
   * @param line The text line from the network.
   * @return The decoded Packet.
   * @throws IllegalArgumentException If the line is empty or the command is unknown.
   */
  public static Packet decode(String line) {
    if (line == null || line.isBlank()) {
      LOGGER.error("Decoding failed: input line is null or blank");
      throw new IllegalArgumentException("Input line cannot be null or blank");
    }

    line = line.trim();
    String[] parts = line.split("\\s+", 2);
    String cmdToken = parts[0];
    String rest = (parts.length > 1) ? parts[1] : "";

    try {
      Command cmd = Command.valueOf(cmdToken); // tests if the command is in the enum
      LOGGER.debug("Command identified: {}", cmd);

      if (rest.isEmpty()) {
        return new Packet(cmd, List.of()); //
      }
      return Packet.of(cmd, rest);

    } catch (IllegalArgumentException e) {
      LOGGER.error("Unknown command token received: {}", cmdToken);
      throw new IllegalArgumentException("Unknown command: " + cmdToken, e);
    }
  }

  /**
   * Converts a package into a string to be sent to the network.
   *
   * @param p The packet to encode.
   * @return The formatted string.
   * @throws IllegalArgumentException If the packet or its command is null.
   */
  public static String encode(Packet p) {
    if (p == null || p.cmd() == null) {
      LOGGER.error("Encoding failed: Packet or Command is null");
      throw new IllegalArgumentException("Packet and its Command must not be null");
    }

    StringBuilder sb = new StringBuilder();
    sb.append(p.cmd().name());

    List<String> args = p.args();

    if (!args.isEmpty()) {
      for (String s : args) {
        sb.append(' ').append(s);
      }
    }

    String result = sb.toString();
    LOGGER.debug("Encoded packet to string: {}", result);
    return result;
  }
}
