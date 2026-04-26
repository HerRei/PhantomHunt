package ch.unibas.dmi.dbis.cs108.phantomhunt.common.protocol;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/** Represents a network packet consisting of a command and optional arguments. */
public final class Packet {

  private static final Logger LOGGER = LogManager.getLogger(Packet.class);
  private final Command cmd;
  private final List<String> args;

  /**
   * Creates a new packet.
   *
   * @param cmd The command of this packet.
   * @param args The list of arguments for this command.
   */
  public Packet(Command cmd, List<String> args) {
    this.cmd = cmd;

    // Create a strictly unmodifiable list to ensure immutability
    this.args = (args == null) ? Collections.emptyList() : args;

    // here we log this.args, so that we get an empty list in the log and not "null"
    LOGGER.trace("New Packet instance created: cmd={}, args={}", this.cmd, this.args);
  }

  public Command cmd() {
    return cmd;
  }

  public List<String> args() {
    return args;
  }

  public int argc() {
    // Since we made sure in the constructor, that args never is null,
    // we can directly ask for the size here.
    return args.size();
  }

  /**
   * Returns the first argument as text.
   *
   * @return The text content of the packet.
   * @throws IllegalStateException If the packet contains no text arguments.
   */
  public String text() {
    if (args.isEmpty()) {
      LOGGER.warn("No text found for command {}", cmd);
      throw new IllegalStateException("Packet contains no text");
    }
    return args.get(0);
  }

  /**
   * A helper method to easily create a new packet.
   *
   * @param cmd The command of the packet.
   * @param args The arguments
   * @return A new packet instance.
   */
  public static Packet of(Command cmd, String... args) {
    LOGGER.debug("Static factory Packet.of called for command: {}", cmd);
    return new Packet(cmd, args == null ? null : Arrays.asList(args));
  }

  @Override
  public String toString() {
    return "Packet[command=" + cmd + ", args=" + args + "]";
  }
}
