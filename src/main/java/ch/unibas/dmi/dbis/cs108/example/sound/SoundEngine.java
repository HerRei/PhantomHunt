package ch.unibas.dmi.dbis.cs108.example.sound;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SoundEngine {

    private final Map<String, URL> soundCache = new HashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public void loadSound(String name, String path) {
        URL soundURL = getClass().getResource(path);
        if (soundURL != null) {
            soundCache.put(name, soundURL);
        } else {
            System.err.println("Sound file not found: " + path);
        }
    }

    public Clip startSound(String name, boolean loop) {
        URL soundURL = soundCache.get(name);
        if (soundURL != null) {
            try {
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundURL);
                Clip clip = AudioSystem.getClip();
                clip.open(audioInputStream);
                if (loop) {
                    clip.loop(Clip.LOOP_CONTINUOUSLY);
                } else {
                    clip.start();
                }
                return clip;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public void stopSound(Clip clip) {
        if (clip != null) {
            clip.stop();
            clip.close();
        }
    }

    public void playSoundForDuration(String name, long durationMillis) {
        Clip clip = startSound(name, false);
        if (clip != null) {
            scheduler.schedule(() -> stopSound(clip), durationMillis, TimeUnit.MILLISECONDS);
        }
    }
}
