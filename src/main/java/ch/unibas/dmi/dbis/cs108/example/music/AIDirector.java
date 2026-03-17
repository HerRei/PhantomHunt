package ch.unibas.dmi.dbis.cs108.example.music;

import java.util.ArrayList;
import java.util.List;

public class AIDirector {
    public enum MotifFamily {
        CALM,
        RISING,
        WANDERING
    }

    public enum ArpPattern {
        OFFBEAT,
        UP,
        SPARSE
    }

    public enum BassPattern {
        HOLD,
        PULSE,
        OCTAVE
    }

    public static class DirectorState {
        public double tempoFactor;
        public int stepSleepMs;

        public MotifFamily motifFamily;
        public ArpPattern arpPattern;
        public BassPattern bassPattern;

        public int drumIntensity;   // 0..2
        public int brassDensity;    // eher klein und kontrolliert
        public int noiseLevel;      // 0..1
        public int ornamentChance;  // 0..100

        public boolean choirOn;
        public boolean cadenceHint;

        public int stringVelocity;
        public int choirVelocity;
        public int bassVelocity;
    }

    private final MusicAI ai;
    private final List<Long> memory = new ArrayList<>();

    public AIDirector(MusicAI ai) {
        this.ai = ai;
        memory.add(60L);
        memory.add(64L);
        memory.add(67L);
        memory.add(69L);
    }

    private long[] prompt() {
        long[] arr = new long[memory.size()];
        for (int i = 0; i < memory.size(); i++) {
            arr[i] = memory.get(i);
        }
        return arr;
    }

    private void remember(long value) {
        memory.add(value);
        while (memory.size() > 16) {
            memory.remove(0);
        }
    }

    public DirectorState planBar(int phase, int root, int nextRoot) throws Exception {
        long[] prompt = prompt();
        float[] logits = ai.holeLogits(prompt);

        DirectorState state = new DirectorState();
        state.cadenceHint = (root == 4) || (nextRoot == 9 && phase >= 2);

        if (phase == 1) {
            double[] tempoPalette = {0.98, 1.00, 1.02};
            state.tempoFactor = tempoPalette[ai.waehleIndex(logits.clone(), prompt, tempoPalette.length, 1.05f)];

            MotifFamily[] motifs = {MotifFamily.CALM, MotifFamily.RISING, MotifFamily.WANDERING};
            state.motifFamily = motifs[ai.waehleIndex(logits.clone(), prompt, motifs.length, 1.10f)];

            state.arpPattern = ArpPattern.SPARSE;
            state.bassPattern = BassPattern.HOLD;
            state.drumIntensity = 0;
            state.brassDensity = 0;
            state.noiseLevel = 0;
            state.ornamentChance = 18 + ai.waehleIndex(logits.clone(), prompt, 8, 1.05f);
            state.choirOn = false;
            state.stringVelocity = 30 + ai.waehleIndex(logits.clone(), prompt, 5, 1.05f);
            state.choirVelocity = 0;
            state.bassVelocity = 40;
        } else if (phase == 2) {
            double[] tempoPalette = {1.00, 1.03, 1.06};
            state.tempoFactor = tempoPalette[ai.waehleIndex(logits.clone(), prompt, tempoPalette.length, 1.05f)];

            MotifFamily[] motifs = {MotifFamily.CALM, MotifFamily.RISING, MotifFamily.WANDERING};
            state.motifFamily = motifs[ai.waehleIndex(logits.clone(), prompt, motifs.length, 1.08f)];

            state.arpPattern = (ai.waehleIndex(logits.clone(), prompt, 100, 1.05f) > 60)
                    ? ArpPattern.UP
                    : ArpPattern.OFFBEAT;
            state.bassPattern = BassPattern.PULSE;
            state.drumIntensity = 1;
            state.brassDensity = 12;
            state.noiseLevel = 0;
            state.ornamentChance = 24 + ai.waehleIndex(logits.clone(), prompt, 10, 1.05f);
            state.choirOn = true;
            state.stringVelocity = 74 + ai.waehleIndex(logits.clone(), prompt, 6, 1.05f);
            state.choirVelocity = 44;
            state.bassVelocity = 92;
        } else {
            double[] tempoPalette = {1.06, 1.09, 1.12};
            state.tempoFactor = tempoPalette[ai.waehleIndex(logits.clone(), prompt, tempoPalette.length, 1.05f)];

            MotifFamily[] motifs = {MotifFamily.RISING, MotifFamily.WANDERING};
            state.motifFamily = motifs[ai.waehleIndex(logits.clone(), prompt, motifs.length, 1.08f)];

            state.arpPattern = (ai.waehleIndex(logits.clone(), prompt, 100, 1.05f) > 55)
                    ? ArpPattern.UP
                    : ArpPattern.OFFBEAT;
            state.bassPattern = BassPattern.OCTAVE;
            state.drumIntensity = 2;
            state.brassDensity = state.cadenceHint ? 85 : 70;
            state.noiseLevel = state.cadenceHint ? 1 : 0;
            state.ornamentChance = 30 + ai.waehleIndex(logits.clone(), prompt, 12, 1.05f);
            state.choirOn = true;
            state.stringVelocity = 86 + ai.waehleIndex(logits.clone(), prompt, 6, 1.05f);
            state.choirVelocity = 46;
            state.bassVelocity = 98;
        }

        if (state.cadenceHint && phase >= 2) {
            state.tempoFactor *= 0.99;
        }

        state.stepSleepMs = (int) Math.round(120.0 / state.tempoFactor);

        remember(root + 48L);
        remember(nextRoot + 48L);
        remember(60L + state.ornamentChance / 4);

        return state;
    }

    public int sampleScream() throws Exception {
        long[] prompt = prompt();
        float[] logits = ai.holeLogits(prompt);

        return MusicTheory.clampMidi(
                (int) ai.sampleAusLogits(logits.clone(), prompt, 72, 96, 1.4f, 0.0f),
                72,
                96
        );
    }
}