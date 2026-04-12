package ch.unibas.dmi.dbis.cs108.phantomhunt.sound;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * High-level audio API backed by the OpenAL SoundEngine.
 * Operates as a Singleton and gracefully falls back to "silent mode" if no audio device is present.
 */
public final class SoundManager {

  private static final SoundManager INSTANCE = new SoundManager();
  private static final Logger LOGGER = LogManager.getLogger(SoundManager.class);

  private final SoundEngine soundEngine = new SoundEngine();
  private boolean initialized;
  private boolean hasFailed = false;

  private SoundManager() {
  }

  /**
   * Retrieves the singleton instance of the SoundManager.
   *
   * @return the singleton instance
   */
  public static SoundManager getInstance() {
    return INSTANCE;
  }

  /**
   * Attempts to initialize the underlying sound engine and load all sound effects.
   * If initialization fails, it fails silently to prevent game crashes-
   */
  public synchronized void initialize() {
    if (initialized || hasFailed) {
      return;
    }
    try {
      soundEngine.init();
      for (SoundEffect effect : SoundEffect.values()) {
        soundEngine.loadSound(effect);
      }
      initialized = true;
    } catch (RuntimeException e) {
      hasFailed = true;
      initialized = false;
      LOGGER.warn("Audio initialization failed: {}", e.getMessage());
    }
  }

  /**
   * Plays the specified sound effect if the engine is initialized.
   *
   * @param effect the sound effect to play
   */
  public void play(SoundEffect effect) {
    ensureInitialized();
    if (initialized) {
      soundEngine.playSound(effect);
    }
  }

  /**
   * Stops the specified sound effect.
   *
   * @param effect the sound effect to stop
   */
  public void stop(SoundEffect effect) {
    ensureInitialized();
    soundEngine.stopSound(effect);
  }

  /**
   * Stops all currently playing sounds.
   */
  public synchronized void stopAll() {
    if (!initialized) {
      return;
    }
    soundEngine.stopAll();
  }

  /**
   * Shuts down the sound engine and frees native resources.
   */
  public synchronized void shutdown() {
    if (!initialized) {
      return;
    }
    soundEngine.shutdown();
    initialized = false;
  }

  /* no usage
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
  } */

  private synchronized void ensureInitialized() {
    if (!initialized) {
      initialize();
    }
  }
}
