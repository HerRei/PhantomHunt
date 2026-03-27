package ch.unibas.dmi.dbis.cs108.example.sound;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SoundEngine {

    private static final Logger LOGGER = LogManager.getLogger(SoundEngine.class);
    private final Map<String, URL> soundCache = new HashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public void loadSound(String name, String path) {
        URL soundURL = getClass().getResource(path);
        if (soundURL != null) {
            soundCache.put(name, soundURL);
            LOGGER.info("Sound loaded: {} -> {}", name, path);
        } else {
            LOGGER.error("Sound file not found: {}", path);
        }
    }

    public Clip startSound(String name, boolean loop) {
        URL soundURL = soundCache.get(name);
        if (soundURL == null) {
            LOGGER.warn("Attempted to play unloaded sound: {}", name);
            return null;
        }
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundURL);
            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);
            if (loop) {
                clip.loop(Clip.LOOP_CONTINUOUSLY);
            } else {
                clip.start();
            }
            LOGGER.info("Started sound: {}", name);
            return clip;
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            LOGGER.error("Error playing sound: {}", name, e);
        }
        return null;
    }

    public void stopSound(Clip clip) {
        if (clip != null && clip.isOpen()) {
            clip.stop();
            clip.close();
            LOGGER.info("Stopped sound");
        }
    }

    public void playSoundForDuration(String name, long durationMillis) {
        Clip clip = startSound(name, false);
        if (clip != null) {
            scheduler.schedule(() -> stopSound(clip), durationMillis, TimeUnit.MILLISECONDS);
            LOGGER.info("Scheduled sound '{}' to stop in {} ms", name, durationMillis);
        }
    }

    public void shutdown() {
        scheduler.shutdown();
    }
}
