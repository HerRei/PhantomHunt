package ch.unibas.dmi.dbis.cs108.example.music;

import java.net.URL;
import java.nio.file.Paths;

public class MusicTest {

    public static void main(String[] args) {
        AudioEngine rawEngine = null;

        try {
            URL resource = MusicTest.class.getResource("/music_brain.onnx");
            if (resource == null) {
                throw new IllegalStateException("Konnte /music_brain.onnx im resources-Ordner nicht finden.");
            }

            String absoluterPfad = Paths.get(resource.toURI()).toFile().getAbsolutePath();

            try (MusicAI ai = new MusicAI(absoluterPfad)) {
                rawEngine = new AudioEngine();
                Orchestra orch = new Orchestra(rawEngine);
                AIDirector director = new AIDirector(ai);
                MelodyPlanner melodyPlanner = new MelodyPlanner(ai);

                int[] letzterAkkord = null;

                int[] progressionRoots = {9, 5, 2, 4}; // A, F, D, E
                int[][] progressionTypes = {
                        MusicTheory.MINOR_9,
                        MusicTheory.MAJOR_7,
                        MusicTheory.MINOR_7,
                        MusicTheory.DOMINANT_7
                };

                int[][] chillScales = {
                        MusicTheory.PENTA_MINOR,
                        MusicTheory.PENTA_MAJOR,
                        MusicTheory.PENTA_MINOR,
                        MusicTheory.SCALE_HARMONIC_MIN
                };

                int[][] epicScales = {
                        MusicTheory.SCALE_DORIAN,
                        MusicTheory.SCALE_IONIAN,
                        MusicTheory.SCALE_DORIAN,
                        MusicTheory.SCALE_HARMONIC_MIN
                };

                for (int phase = 1; phase <= 4; phase++) {

                    if (phase == 4) {
                        System.out.println("\n💥>>> PHASE 4: GAME OVER! DER GEIST HAT DICH! <<<💥");

                        int[] deathChord = {36, 37, 38, 48, 49, 50, 60, 61, 62};
                        int aiScream = director.sampleScream();

                        orch.playChord(orch.CH_BRASS, deathChord, 127);
                        orch.playChord(orch.CH_STRINGS, deathChord, 127);
                        orch.play(orch.CH_SOLO, aiScream, 127);
                        orch.drum(49, 127);
                        orch.drum(35, 127);

                        Thread.sleep(3000);

                        orch.stopChord(orch.CH_BRASS, deathChord);
                        orch.stopChord(orch.CH_STRINGS, deathChord);
                        orch.stop(orch.CH_SOLO, aiScream);
                        break;
                    }

                    if (phase == 1) {
                        System.out.println("\n>>> PHASE 1: Verträumtes Piano (ruhiger Director) <<<");
                        orch.engine.changeInstrument(orch.CH_SOLO, 0);
                    } else if (phase == 2) {
                        System.out.println("\n>>> PHASE 2: Streicher & Holzbläser (ruhiger Director) <<<");
                        orch.engine.changeInstrument(orch.CH_SOLO, 73);
                    } else {
                        System.out.println("\n>>> PHASE 3: Eskalation (ruhiger Director) <<<");
                        orch.engine.changeInstrument(orch.CH_SOLO, 81);
                    }

                    for (int durchlauf = 0; durchlauf < 2; durchlauf++) {
                        for (int c = 0; c < progressionRoots.length; c++) {
                            int root = progressionRoots[c];
                            int nextRoot = progressionRoots[(c + 1) % progressionRoots.length];

                            int[] rawChord = MusicTheory.buildChord(root, 3, progressionTypes[c]);
                            int[] aktChord = MusicTheory.applyVoiceLeading(letzterAkkord, rawChord);
                            letzterAkkord = aktChord;

                            int[] choirChord = MusicTheory.buildChord(root, 4, progressionTypes[c]);
                            int[] aktScale = MusicTheory.buildScale(
                                    root,
                                    (phase == 1) ? chillScales[c] : epicScales[c]
                            );

                            AIDirector.DirectorState state = director.planBar(phase, root, nextRoot);
                            melodyPlanner.beginBar(phase, root, aktChord, aktScale, state);

                            orch.playChord(orch.CH_STRINGS, aktChord, state.stringVelocity);

                            if (phase >= 2 && state.choirOn) {
                                orch.playChord(orch.CH_CHOIR, choirChord, state.choirVelocity);
                            }

                            Integer sustainedBass = null;
                            if (state.bassPattern == AIDirector.BassPattern.HOLD) {
                                sustainedBass = root + 24;
                                orch.play(orch.CH_BASS, sustainedBass, state.bassVelocity);
                            }

                            int activeSoloNote = -1;
                            int soloHoldSteps = 0;
                            boolean brassOn = false;

                            for (int step = 0; step < 16; step++) {
                                Integer arpNote = maybePlayArp(orch, phase, step, aktChord, state);
                                Integer bassPulse = maybePlayBassPulse(orch, step, root, state);
                                playDrums(orch, phase, step, state);

                                boolean shouldBrassHit = maybePlayBrass(orch, phase, step, aktChord, state);
                                if (shouldBrassHit) {
                                    brassOn = true;
                                } else if (brassOn && (step == 2 || step == 8 || step == 14)) {
                                    orch.stopChord(orch.CH_BRASS, aktChord);
                                    brassOn = false;
                                }

                                if (state.noiseLevel > 0 && phase == 3 && (step == 7 || step == 15)) {
                                    orch.drum(46, 48);
                                }

                                MelodyPlanner.MelodyEvent event = melodyPlanner.planStep(
                                        phase,
                                        step,
                                        aktScale,
                                        aktChord,
                                        state
                                );

                                if (soloHoldSteps > 0) {
                                    soloHoldSteps--;
                                }

                                if (event.play) {
                                    if (activeSoloNote != -1) {
                                        orch.stop(orch.CH_SOLO, activeSoloNote);
                                    }
                                    orch.play(orch.CH_SOLO, event.note, event.velocity);
                                    activeSoloNote = event.note;
                                    soloHoldSteps = event.holdSteps;
                                } else if (soloHoldSteps <= 0 && activeSoloNote != -1) {
                                    orch.stop(orch.CH_SOLO, activeSoloNote);
                                    activeSoloNote = -1;
                                }

                                Thread.sleep(state.stepSleepMs);

                                if (arpNote != null) {
                                    orch.stop(orch.CH_ARPEGGIO, arpNote);
                                }

                                if (bassPulse != null) {
                                    orch.stop(orch.CH_BASS, bassPulse);
                                }
                            }

                            if (brassOn) {
                                orch.stopChord(orch.CH_BRASS, aktChord);
                            }

                            if (activeSoloNote != -1) {
                                orch.stop(orch.CH_SOLO, activeSoloNote);
                            }

                            if (sustainedBass != null) {
                                orch.stop(orch.CH_BASS, sustainedBass);
                            }

                            orch.stopChord(orch.CH_STRINGS, aktChord);
                            if (phase >= 2 && state.choirOn) {
                                orch.stopChord(orch.CH_CHOIR, choirChord);
                            }
                        }
                    }
                }

                System.out.println("\n✅ Symphonie beendet.");
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (rawEngine != null) rawEngine.close();
        }
    }

    private static Integer maybePlayArp(
            Orchestra orch,
            int phase,
            int step,
            int[] chord,
            AIDirector.DirectorState state
    ) {
        if (phase < 2) return null;

        boolean play;
        int arpIndex;

        switch (state.arpPattern) {
            case OFFBEAT -> {
                play = (step % 2 == 0);
                arpIndex = (step / 2) % chord.length;
            }
            case UP -> {
                play = true;
                arpIndex = step % chord.length;
            }
            case SPARSE -> {
                play = (step == 2 || step == 6 || step == 10 || step == 14);
                arpIndex = (step / 2) % chord.length;
            }
            default -> {
                play = false;
                arpIndex = 0;
            }
        }

        if (!play) return null;

        int note = MusicTheory.clampMidi(chord[arpIndex] + 12, 50, 96);
        int vel = (phase == 2) ? 72 : 84;
        orch.play(orch.CH_ARPEGGIO, note, vel);
        return note;
    }

    private static Integer maybePlayBassPulse(
            Orchestra orch,
            int step,
            int root,
            AIDirector.DirectorState state
    ) {
        if (state.bassPattern == AIDirector.BassPattern.HOLD) {
            return null;
        }

        Integer note = null;

        switch (state.bassPattern) {
            case PULSE -> {
                if (step == 0 || step == 8) {
                    note = root + 24;
                }
            }
            case OCTAVE -> {
                if (step == 0 || step == 8) note = root + 24;
                else if (step == 4 || step == 12) note = root + 36;
            }
            default -> {
            }
        }

        if (note != null) {
            orch.play(orch.CH_BASS, note, state.bassVelocity);
        }

        return note;
    }

    private static void playDrums(
            Orchestra orch,
            int phase,
            int step,
            AIDirector.DirectorState state
    ) {
        if (phase < 2) return;

        if (step == 0 || step == 8) {
            orch.drum(36, 110);
        }

        if (phase == 2) {
            if (step % 4 == 0) {
                orch.drum(42, 90);
            }
        } else {
            if (step % 2 == 0) {
                orch.drum(42, 82);
            }
            if (step == 4 || step == 12) {
                orch.drum(38, 96);
            }
        }
    }

    private static boolean maybePlayBrass(
            Orchestra orch,
            int phase,
            int step,
            int[] chord,
            AIDirector.DirectorState state
    ) {
        if (phase < 3) return false;

        boolean shouldHit = (step == 0 || step == 6);
        if (state.cadenceHint && step == 12) {
            shouldHit = true;
        }

        if (shouldHit && state.brassDensity >= 70) {
            orch.playChord(orch.CH_BRASS, chord, 116);
            return true;
        }

        return false;
    }
}