package ch.unibas.dmi.dbis.cs108.example.music;

public class Orchestra {
    public final AudioEngine engine;

    public final int CH_SOLO = 0;
    public final int CH_STRINGS = 1;
    public final int CH_BRASS = 2;
    public final int CH_ARPEGGIO = 3;
    public final int CH_CHOIR = 4;
    public final int CH_BASS = 5;
    public final int CH_DRUMS = 9;

    public Orchestra(AudioEngine engine) {
        this.engine = engine;

        engine.changeInstrument(CH_STRINGS, 48);
        engine.changeInstrument(CH_BRASS, 61);
        engine.changeInstrument(CH_ARPEGGIO, 45);
        engine.changeInstrument(CH_CHOIR, 52);
        engine.changeInstrument(CH_BASS, 43);
    }

    public void playChord(int channel, int[] chord, int velocity) {
        for (int note : chord) engine.playNote(channel, note, velocity);
    }

    public void stopChord(int channel, int[] chord) {
        for (int note : chord) engine.stopNote(channel, note);
    }

    public void play(int channel, int note, int velocity) {
        engine.playNote(channel, note, velocity);
    }

    public void stop(int channel, int note) {
        engine.stopNote(channel, note);
    }

    public void drum(int note, int velocity) {
        engine.playDrum(note, velocity);
    }
}