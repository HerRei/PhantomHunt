package ch.unibas.dmi.dbis.cs108.example.common.protocol;

import java.util.Random;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A utility class that generates random names for players.
 */
public class NameGenerator {

  private static final String[] PREFIXES = {
    "Bel", "Nar", "Zel", "Kael", "Jor", "Fae", "Lumi", "Ty", "Xan"
  };
  private static final String[] CONNECTORS = {
    "an", "ori", "el", "in", "al", "os", "um", "eth"
  };
  private static final String[] SUFFIXES = {
    "dor", "th", "via", "ius", "ra", "lon", "is", "ax"
  };

  private static final Random RANDOM = new Random();
  private static final Logger LOGGER = LogManager.getLogger(NameGenerator.class);

  /**
   * Generates and returns a random, multi-syllable name.
   *
   * @return A randomly generated name string.
   */
  public static String randomName() {
    return generateName();
  }

  private static String generateName() {
    String pre = getRandomElement(PREFIXES);
    String suf = getRandomElement(SUFFIXES);

    StringBuilder con = new StringBuilder();

    // 50% Chance for Connector
    if (RANDOM.nextBoolean()) {
      con.append(getRandomElement(CONNECTORS));

      // 50% Chance for a second Connector
      if (RANDOM.nextBoolean()) {
        con.append(getRandomElement(CONNECTORS));
      }
    }

    String fullName = pre + con.toString() + suf;
    LOGGER.debug("Generated random name: {}", fullName);

    return fullName;
  }

  /**
   * Helper method to pick a random element from a String array.
   *
   * @param array The array to pick from.
   * @return A random element from the array.
   */
  private static String getRandomElement(String[] array) {
    return array[RANDOM.nextInt(array.length)];
  }
}
