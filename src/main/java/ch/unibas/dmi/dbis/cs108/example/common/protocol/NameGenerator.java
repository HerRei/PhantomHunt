package ch.unibas.dmi.dbis.cs108.example.common.protocol;

import java.util.Random;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A class that generates random names for players.
 */
public class NameGenerator {

  private static final String[] PREFIXES = {
    "Bel", "Nar", "Zel", "Kael", "Jor", "Fae", "Lumi", "Ty", "Xan"
  };
  private static final String[] CONNECTORS = {"an", "ori", "el", "in", "al", "os", "um", "eth"};
  private static final String[] SUFFIXES = {"dor", "th", "via", "ius", "ra", "lon", "is", "ax"};
  private static final Random RANDOM = new Random();
  private static final Logger LOGGER = LogManager.getLogger(NameGenerator.class);

  /**
   * Generates and returns a random name.
   * @return A randomly generated name string.
   */
  public static String randomName() {
    return generateName();
  }

  private static String generateName() {
    String pre = PREFIXES[RANDOM.nextInt(PREFIXES.length)];
    String suf = SUFFIXES[RANDOM.nextInt(SUFFIXES.length)];

    String con;
    if (RANDOM.nextBoolean()) {
      String first = CONNECTORS[RANDOM.nextInt(CONNECTORS.length)];
      con = RANDOM.nextBoolean() ? first + CONNECTORS[RANDOM.nextInt(CONNECTORS.length)] : first;
      LOGGER.debug("Connector chosen: {}", con);
    } else {
      con = "";
      LOGGER.debug("No connector chosen");
    }
    LOGGER.info(
        "Generated name: {}{}{}",
        pre,
        con,
        suf); // all the logging in here was just for testing and serves no purpose at all, this
              // whole class is never used really
    return pre + con + suf;
  }
}
