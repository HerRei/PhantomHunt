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

/**
 * A simple sound engine for loading and playing audio clips.
 * This class provides an API to start, stop, and play sounds for a specified duration.
 * Sound files are cached to avoid redundant loading.
 */
public final class SoundEngine {

    private static final Logger LOGGER = LogManager.getLogger(SoundEngine.class);

    // A cache to store loaded sound files
    private final Map<String, URL> soundCache = new HashMap<>();

    // A scheduler to handle time-based tasks
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    /**
     * Loads a sound file from the given path and caches it with a specified name.
     * The path should be absolute.
     *
     * @param name A name to identify the sound.
     * @param path The resource path to the sound file.
     */
    public void loadSound(String name, String path) {
        URL soundURL = getClass().getResource(path);
        if (soundURL != null) {
            soundCache.put(name, soundURL);
            LOGGER.info("Sound loaded and cached: '{}' at path '{}'", name, path);
        } else {
            LOGGER.error("Could not find sound file at path: {}", path);
        }
    }

    /**
     * Starts playing a cached sound.
     *
     * @param name The name of the sound to play.
     * @param loop If true, the sound will loop while.
     * @return object that can be used to control the sound.
     */
    public Clip startSound(String name, boolean loop) {
        URL soundURL = soundCache.get(name);
        if (soundURL == null) {
            LOGGER.warn("Attempted to play a sound that was not loaded: '{}'", name);
            return null;
        }

        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundURL);
            Clip clip = AudioSystem.getClip();
            clip.open(audioStream);

            if (loop) {
                clip.loop(Clip.LOOP_CONTINUOUSLY);
            } else {
                clip.start();
            }

            LOGGER.info("Playing sound: '{}' (Loop: {})", name, loop);
            return clip;
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            LOGGER.error("An error occurred while trying to play sound: '{}'", name, e);
            return null;
        }
    }

    /**
     * Stops a currently playing sound clip.
     */
    public void stopSound(Clip clip) {
        if (clip != null && clip.isOpen()) {
            clip.stop();
            clip.close();
            LOGGER.info("Sound clip stopped and closed.");
        }
    }

    /**
     * Plays a sound for a specific duration and then automatically stops it.
     */
    public void playSoundForDuration(String name, long durationMillis) {
        // Start the sound without looping.
        Clip clip = startSound(name, false);

        // If the clip started successfully, schedule it to stop after the specified duration.
        if (clip != null) {
            scheduler.schedule(() -> stopSound(clip), durationMillis, TimeUnit.MILLISECONDS);
            LOGGER.info("Sound '{}' is scheduled to stop in {} milliseconds.", name, durationMillis);
        }
    }

    /**
     * Shuts down the sound engine and releases its resources.
     */
    public void shutdown() {
        scheduler.shutdownNow(); //
        LOGGER.info("Sound engine has been shut down.");
    }
}
