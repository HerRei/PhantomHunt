package ch.unibas.dmi.dbis.cs108.example.music;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MelodyPlanner {
    public static class MelodyEvent {
        public boolean play;
        public int note;
        public int velocity;
        public int holdSteps;
    }

    private static final int[][] CALM_MOTIFS = {
            {0, 1, 2, 1},
            {0, 1, 0, -1},
            {0, 2, 1, 0}
    };

    private static final int[][] RISING_MOTIFS = {
            {0, 1, 2, 3},
            {0, 2, 3, 2},
            {0, 1, 3, 2}
    };

    private static final int[][] WANDERING_MOTIFS = {
            {0, -1, 1, 0},
            {0, 1, -1, 0},
            {0, 2, 0, 1}
    };

    private final MusicAI ai;
    private final Random rand = new Random();
    private final List<Long> memory = new ArrayList<>();

    private int lastNote = -1;
    private final int[] currentBarMelody = new int[16];

    public MelodyPlanner(MusicAI ai) {
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

    private int[][] motifPoolFor(AIDirector.MotifFamily family) {
        return switch (family) {
            case CALM -> CALM_MOTIFS;
            case RISING -> RISING_MOTIFS;
            case WANDERING -> WANDERING_MOTIFS;
        };
    }

    public void beginBar(
            int phase,
            int root,
            int[] chord,
            int[] scale,
            AIDirector.DirectorState state
    ) throws Exception {
        long[] prompt = prompt();
        float[] logits = ai.holeLogits(prompt);

        int[][] motifPool = motifPoolFor(state.motifFamily);
        int motifIndex = ai.waehleIndex(logits.clone(), prompt, motifPool.length, 1.05f);
        int[] motif = motifPool[motifIndex];

        int preferredStart = (lastNote != -1)
                ? lastNote
                : MusicTheory.nearestChordToneInRegister(root + 60, chord, 54, 78);

        int anchor0 = MusicTheory.nearestChordToneInRegister(preferredStart, chord, 54, 80);
        int anchor4 = MusicTheory.nearestChordToneInRegister(
                MusicTheory.moveScaleSteps(anchor0, scale, motif[1]),
                chord,
                54,
                82
        );
        int anchor8 = MusicTheory.nearestChordToneInRegister(
                MusicTheory.moveScaleSteps(anchor0, scale, motif[2]),
                chord,
                54,
                84
        );
        int anchor12 = MusicTheory.nearestChordToneInRegister(
                MusicTheory.moveScaleSteps(anchor0, scale, motif[3]),
                chord,
                54,
                82
        );

        int finalAnchor = state.cadenceHint
                ? MusicTheory.nearestChordToneInRegister(root + 57, chord, 54, 82)
                : MusicTheory.nearestChordToneInRegister(anchor12, chord, 54, 82);

        currentBarMelody[0] = anchor0;
        currentBarMelody[4] = anchor4;
        currentBarMelody[8] = anchor8;
        currentBarMelody[12] = anchor12;
        currentBarMelody[15] = finalAnchor;

        MusicTheory.fillScaleLine(currentBarMelody, 0, 4, scale);
        MusicTheory.fillScaleLine(currentBarMelody, 4, 8, scale);
        MusicTheory.fillScaleLine(currentBarMelody, 8, 12, scale);
        MusicTheory.fillScaleLine(currentBarMelody, 12, 15, scale);

        remember(root + 48L);
        remember(chord[chord.length - 1]);
    }

    private int blendBaseWithAI(
            int baseNote,
            int[] scale,
            int[] chord,
            int phase,
            boolean strongBeat,
            AIDirector.DirectorState state
    ) throws Exception {
        if (strongBeat) {
            return MusicTheory.nearestChordToneInRegister(baseNote, chord, 48, 88);
        }

        int hardcodedBias = 100 - state.ornamentChance;
        int roll = rand.nextInt(100);

        if (roll < hardcodedBias) {
            if (rand.nextInt(100) < Math.max(8, state.ornamentChance / 2)) {
                int decoration = MusicTheory.moveScaleSteps(baseNote, scale, rand.nextBoolean() ? 1 : -1);
                return MusicTheory.clampMidi(decoration, 48, 88);
            }
            return MusicTheory.clampMidi(baseNote, 48, 88);
        }

        long[] prompt = prompt();
        float[] logits = ai.holeLogits(prompt);

        int aiNote = MusicTheory.chooseGuidedAINote(
                logits.clone(),
                scale,
                chord,
                lastNote,
                phase,
                false
        );

        int baseIdx = MusicTheory.nearestScaleIndex(baseNote, scale);
        int aiIdx = MusicTheory.nearestScaleIndex(aiNote, scale);
        int diff = aiIdx - baseIdx;

        int move;
        if (diff == 0) {
            move = 0;
        } else {
            move = (diff > 0) ? 1 : -1;
        }

        int blended = MusicTheory.moveScaleSteps(baseNote, scale, move);

        if (lastNote != -1 && Math.abs(blended - lastNote) > 7) {
            blended = MusicTheory.moveScaleSteps(lastNote, scale, blended > lastNote ? 1 : -1);
        }

        return MusicTheory.clampMidi(blended, 48, 88);
    }

    public MelodyEvent planStep(
            int phase,
            int step,
            int[] scale,
            int[] chord,
            AIDirector.DirectorState state
    ) throws Exception {
        MelodyEvent event = new MelodyEvent();

        boolean strongBeat = (step % 4 == 0);
        boolean mediumBeat = (step % 2 == 0);

        int baseNote = currentBarMelody[step];
        int note = blendBaseWithAI(baseNote, scale, chord, phase, strongBeat, state);

        boolean shouldPlay;
        if (strongBeat) {
            shouldPlay = true;
        } else if (mediumBeat) {
            if (phase == 1) shouldPlay = true;
            else if (phase == 2) shouldPlay = rand.nextDouble() > 0.20;
            else shouldPlay = rand.nextDouble() > 0.10;
        } else {
            if (phase == 1) shouldPlay = false;
            else if (phase == 2) shouldPlay = rand.nextDouble() > 0.88;
            else shouldPlay = rand.nextDouble() > 0.75;
        }

        if (!strongBeat && lastNote != -1 && note == lastNote) {
            shouldPlay = false;
        }

        event.play = shouldPlay;
        event.note = note;

        if (phase == 1) {
            event.velocity = strongBeat ? 56 : 48;
        } else if (phase == 2) {
            event.velocity = strongBeat ? 82 : 72;
        } else {
            event.velocity = strongBeat ? 96 : 84;
        }

        event.holdSteps = strongBeat ? 2 : 1;

        if (event.play) {
            lastNote = note;
            remember(note);
        } else {
            remember(baseNote);
        }

        return event;
    }
}