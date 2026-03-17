package ch.unibas.dmi.dbis.cs108.example.music; // Passe das an deinen genauen Pfad an!

import javax.sound.midi.*;

public class AudioEngine {
    private Synthesizer synthesizer;
    private MidiChannel[] channels;

    public AudioEngine() {
        try {
            synthesizer = MidiSystem.getSynthesizer();
            synthesizer.open();
            channels = synthesizer.getChannels();

            // Kanal 0 auf Instrument 0 (Acoustic Grand Piano) setzen
            channels[0].programChange(0);
            System.out.println("🎵 Synthesizer hochgefahren!");
        } catch (MidiUnavailableException e) {
            System.err.println("Fehler beim Starten des Synthesizers!");
            e.printStackTrace();
        }
    }

    public void playNote(int note, int velocity) {
        if (channels != null) {
            channels[0].noteOn(note, velocity);
        }
    }

    public void stopNote(int note) {
        if (channels != null) {
            channels[0].noteOff(note);
        }
    }

    // Wichtig, um am Ende aufzuräumen
    public void close() {
        if (synthesizer != null && synthesizer.isOpen()) {
            synthesizer.close();
        }
    }
}