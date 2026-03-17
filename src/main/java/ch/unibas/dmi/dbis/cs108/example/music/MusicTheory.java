package ch.unibas.dmi.dbis.cs108.example.music;

import java.util.Arrays;

public final class MusicTheory {
    private MusicTheory() {
    }

    public static final int[] MAJOR_7 = {0, 4, 7, 11};
    public static final int[] MINOR_7 = {0, 3, 7, 10};
    public static final int[] DOMINANT_7 = {0, 4, 7, 10};
    public static final int[] MINOR_9 = {0, 3, 7, 10, 14};

    public static final int[] SCALE_IONIAN = {0, 2, 4, 5, 7, 9, 11};
    public static final int[] SCALE_DORIAN = {0, 2, 3, 5, 7, 9, 10};
    public static final int[] SCALE_HARMONIC_MIN = {0, 2, 3, 5, 7, 8, 11};
    public static final int[] PENTA_MAJOR = {0, 2, 4, 7, 9};
    public static final int[] PENTA_MINOR = {0, 3, 5, 7, 10};

    public static int[] buildChord(int rootNote, int octave, int[] chordType) {
        int baseMidi = (octave + 1) * 12 + rootNote;
        int[] chord = new int[chordType.length];
        for (int i = 0; i < chordType.length; i++) {
            chord[i] = baseMidi + chordType[i];
        }
        return chord;
    }

    public static int[] buildScale(int rootNote, int[] scaleMode) {
        int[] fullScale = new int[scaleMode.length * 2];
        int index = 0;

        for (int oct = 4; oct <= 5; oct++) {
            int baseMidi = (oct + 1) * 12 + rootNote;
            for (int interval : scaleMode) {
                fullScale[index++] = baseMidi + interval;
            }
        }

        return fullScale;
    }

    public static int[] applyVoiceLeading(int[] previousChord, int[] targetChord) {
        if (previousChord == null) return targetChord.clone();

        int prevAvg = Arrays.stream(previousChord).sum() / previousChord.length;
        int[] result = targetChord.clone();

        for (int i = 0; i < result.length; i++) {
            while (result[i] - prevAvg > 6) result[i] -= 12;
            while (prevAvg - result[i] > 6) result[i] += 12;
        }

        Arrays.sort(result);
        return result;
    }

    public static int snapToScale(int generatedNote, int[] allowedNotes) {
        int closest = allowedNotes[0];
        int minDiff = Math.abs(generatedNote - allowedNotes[0]);

        for (int note : allowedNotes) {
            int diff = Math.abs(generatedNote - note);
            if (diff < minDiff) {
                minDiff = diff;
                closest = note;
            }
        }

        return closest;
    }

    public static int clampMidi(int note, int min, int max) {
        return Math.max(min, Math.min(max, note));
    }

    public static boolean containsPitchClass(int[] notes, int note) {
        int pc = Math.floorMod(note, 12);
        for (int n : notes) {
            if (Math.floorMod(n, 12) == pc) return true;
        }
        return false;
    }

    public static boolean isChordTone(int note, int[] chord) {
        return containsPitchClass(chord, note);
    }

    public static boolean isScaleTone(int note, int[] scale) {
        return containsPitchClass(scale, note);
    }

    public static int nearestScaleIndex(int note, int[] scale) {
        int idx = 0;
        int minDiff = Integer.MAX_VALUE;

        for (int i = 0; i < scale.length; i++) {
            int diff = Math.abs(scale[i] - note);
            if (diff < minDiff) {
                minDiff = diff;
                idx = i;
            }
        }
        return idx;
    }

    public static int moveScaleSteps(int note, int[] scale, int steps) {
        int idx = nearestScaleIndex(note, scale);
        int newIdx = Math.max(0, Math.min(scale.length - 1, idx + steps));
        return scale[newIdx];
    }

    public static int nearestChordToneInRegister(int preferredNote, int[] chord, int min, int max) {
        int best = clampMidi(chord[0], min, max);
        int bestDiff = Integer.MAX_VALUE;

        for (int chordTone : chord) {
            for (int shift = -24; shift <= 24; shift += 12) {
                int candidate = chordTone + shift;
                if (candidate < min || candidate > max) continue;

                int diff = Math.abs(candidate - preferredNote);
                if (diff < bestDiff) {
                    bestDiff = diff;
                    best = candidate;
                }
            }
        }

        return best;
    }

    public static int chooseGuidedAINote(
            float[] logits,
            int[] scale,
            int[] chord,
            int lastNote,
            int phase,
            boolean strongBeat
    ) {
        int lower = 48;
        int upper = Math.min(90, logits.length - 1);

        double[] scores = new double[128];
        Arrays.fill(scores, Double.NEGATIVE_INFINITY);

        for (int note = lower; note <= upper; note++) {
            if (!isScaleTone(note, scale)) continue;

            double score = logits[note];

            if (isChordTone(note, chord)) {
                score += strongBeat ? 6.0 : 2.0;
            } else {
                score += strongBeat ? -2.0 : 0.6;
            }

            if (lastNote != -1) {
                int leap = Math.abs(note - lastNote);
                if (leap <= 2) score += 2.0;
                else if (leap <= 5) score += 1.0;
                else if (leap <= 7) score -= 0.6;
                else score -= 4.0;
            }

            if (phase == 1 && note > 78) score -= 1.3;
            scores[note] = score;
        }

        int topK = 5;
        int[] topNotes = new int[topK];
        double[] topScores = new double[topK];
        Arrays.fill(topNotes, 60);
        Arrays.fill(topScores, Double.NEGATIVE_INFINITY);

        for (int note = lower; note <= upper; note++) {
            double s = scores[note];
            for (int i = 0; i < topK; i++) {
                if (s > topScores[i]) {
                    for (int j = topK - 1; j > i; j--) {
                        topScores[j] = topScores[j - 1];
                        topNotes[j] = topNotes[j - 1];
                    }
                    topScores[i] = s;
                    topNotes[i] = note;
                    break;
                }
            }
        }

        double max = topScores[0];
        double sum = 0.0;
        double[] probs = new double[topK];

        for (int i = 0; i < topK; i++) {
            probs[i] = Math.exp((topScores[i] - max) / 0.9);
            sum += probs[i];
        }

        double r = Math.random() * sum;
        double running = 0.0;

        for (int i = 0; i < topK; i++) {
            running += probs[i];
            if (running >= r) {
                return clampMidi(topNotes[i], 48, 90);
            }
        }

        return 60;
    }

    public static void fillScaleLine(int[] target, int from, int to, int[] scale) {
        int start = target[from];
        int end = target[to];

        int startIdx = nearestScaleIndex(start, scale);
        int endIdx = nearestScaleIndex(end, scale);

        int span = to - from;
        for (int i = 1; i < span; i++) {
            double t = (double) i / span;
            int idx = (int) Math.round(startIdx + (endIdx - startIdx) * t);
            idx = Math.max(0, Math.min(scale.length - 1, idx));
            target[from + i] = scale[idx];
        }
    }
}