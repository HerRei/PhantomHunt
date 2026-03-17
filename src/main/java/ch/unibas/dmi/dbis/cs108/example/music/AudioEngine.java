package ch.unibas.dmi.dbis.cs108.example.music;

import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Synthesizer;

public class AudioEngine {
    private Synthesizer synthesizer;
    private MidiChannel[] channels;

    public AudioEngine() {
        try {
            synthesizer = MidiSystem.getSynthesizer();
            synthesizer.open();
            channels = synthesizer.getChannels();

            channels[0].programChange(11); // Solo default
            channels[1].programChange(48); // Strings
            channels[2].programChange(61); // Brass
            channels[3].programChange(45); // Arp
            channels[4].programChange(52); // Choir
            channels[5].programChange(43); // Bass

            System.out.println("🎛️ Audio-Engine bereit!");
        } catch (MidiUnavailableException e) {
            System.err.println("Fehler beim Starten der Audio-Engine.");
            e.printStackTrace();
        }
    }

    public void playNote(int channel, int note, int velocity) {
        if (channels != null && channel >= 0 && channel < channels.length) {
            channels[channel].noteOn(note, velocity);
        }
    }

    public void stopNote(int channel, int note) {
        if (channels != null && channel >= 0 && channel < channels.length) {
            channels[channel].noteOff(note);
        }
    }

    public void playDrum(int drumType, int velocity) {
        if (channels != null && channels.length > 9) {
            channels[9].noteOn(drumType, velocity);
        }
    }

    public void changeInstrument(int channel, int instrumentId) {
        if (channels != null && channel >= 0 && channel < channels.length) {
            channels[channel].programChange(instrumentId);
        }
    }

    public void close() {
        if (synthesizer != null && synthesizer.isOpen()) {
            synthesizer.close();
        }
    }
}