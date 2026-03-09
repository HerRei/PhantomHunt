package ch.unibas.dmi.dbis.cs108.example.common.protocol;


import java.util.Random;

public class NameGenerator  {

        private static final String[] PREFIXES = {"Bel", "Nar", "Zel", "Kael", "Jor", "Fae", "Lumi", "Ty", "Xan"};
        private static final String[] CONNECTORS = {"an", "ori", "el", "in", "al", "os", "um", "eth"};
        private static final String[] SUFFIXES = {"dor", "th", "via", "ius", "ra", "lon", "is", "ax"};
    private static final Random RANDOM = new Random();

      public static String randomName() {
         return generateName();
      }

    private static String generateName() {
        String pre = PREFIXES[RANDOM.nextInt(PREFIXES.length)];
        String suf = SUFFIXES[RANDOM.nextInt(SUFFIXES.length)];

        String con;
        if (RANDOM.nextBoolean()) {
            String first = CONNECTORS[RANDOM.nextInt(CONNECTORS.length)];
            con = RANDOM.nextBoolean()
                    ? first + CONNECTORS[RANDOM.nextInt(CONNECTORS.length)]
                    : first;
        } else {
            con = "";
        }

        return pre + con + suf;
    }

}
