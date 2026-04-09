package ch.unibas.dmi.dbis.cs108.example.sound;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * High-level audio API backed by the OpenAL SoundEngine.
 */
public final class SoundManager {

  private static final SoundManager INSTANCE = new SoundManager();
  private static final Logger LOGGER = LogManager.getLogger(SoundManager.class);

  private final SoundEngine soundEngine = new SoundEngine();
  private boolean initialized;
  private boolean notFailed = true;

  private SoundManager() {
  }

  public static SoundManager getInstance() {
    return INSTANCE;
  }

  public synchronized void initialize() {
    if (initialized || !notFailed) {
      return;
    }
    try {
      soundEngine.init();
      for (SoundEffect effect : SoundEffect.values()) {
        soundEngine.loadSound(effect);
      }
      initialized = true;
    } catch (RuntimeException e) {
      notFailed = false;
      initialized = false;
      LOGGER.warn("Audio initialization failed: {}", e.getMessage());
    }
  }

  public void play(SoundEffect effect) {
    ensureInitialized();
    soundEngine.playSound(effect);
  }

  public void stop(SoundEffect effect) {
    ensureInitialized();
    soundEngine.stopSound(effect);
  }

  public synchronized void stopAll() {
    if (!initialized) {
      return;
    }

    soundEngine.stopAll();
  }

  public synchronized void shutdown() {
    if (!initialized) {
      return;
    }

    soundEngine.shutdown();
    initialized = false;
  }

  public void playWindOutsideRoomToneLoop() {
    play(SoundEffect.WIND_OUTSIDE_ROOM_TONE);
  }

  public void stopWindOutsideRoomTone() {
    stop(SoundEffect.WIND_OUTSIDE_ROOM_TONE);
  }

  public void playManScream() {
    play(SoundEffect.MAN_SCREAM);
  }

  public void playDraggingChain() {
    play(SoundEffect.DRAGGING_CHAIN);
  }

  public void playRunningOnFloor() {
    play(SoundEffect.RUNNING_ON_FLOOR);
  }

  public void playDescentWhoosh() {
    play(SoundEffect.DESCENT_WHOOSH);
  }

  private synchronized void ensureInitialized() {
    if (!initialized) {
      initialize();
    }
  }
}
