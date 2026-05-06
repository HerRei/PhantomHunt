package ch.unibas.dmi.dbis.cs108.phantomhunt.sound;

/**
 * Defines the available sound effects for the game. Each sound effect includes its file path,
 * looping behavior, and base volume (gain).
 */
public enum SoundEffect {

  /** Continuous background wind noise. */
  WIND_OUTSIDE_ROOM_TONE("/audio/wind-outside-room-tone.wav", true, 0.50f),

  /** A sudden, loud man's scream. */
  MAN_SCREAM("/audio/man-scream.wav", false, 0.85f),

  /** The sound of heavy chains being dragged across the floor */
  DRAGGING_CHAIN("/audio/dragging-chain.wav", true, 0.65f),

  /** Quick footsteps running across a wooden or stone floor. */
  RUNNING_ON_FLOOR("/audio/running-on-floor.wav", true, 0.75f),

  /** A deep whooshing sound, typically used for transitions or jump scares. */
  DESCENT_WHOOSH("/audio/descent-whoosh.wav", false, 0.85f),

  /** A short coin pickup sound for rewards or score events. */
  COIN_UP("/audio/coin-up.wav", false, 0.80f),

  /** A short alert sound for incoming messages or notifications. */
  UNIVERSFIELD_MESSAGE("/audio/universfield-message.wav", false, 0.75f),

  /** A wet slime impact sound for hit or collision feedback. */
  UNIVERSFIELD_SLIME_IMPACT("/audio/universfield-slime-impact.wav", false, 0.80f),

  /** A short scare sting for jump-scare or surprise feedback. */
  MAGIAZ_SCARE("/audio/magiaz-scare.wav", false, 0.85f);

  private final String resourcePath;
  private final boolean loops;
  private final float gain;

  /**
   * Constructs a new SoundEffect configuration.
   *
   * @param resourcePath The internal path to the audio file in the resources folder.
   * @param loops Whether the sound should loop indefinitely when played.
   * @param gain The default volume level (0.0f to 1.0f).
   */
  SoundEffect(String resourcePath, boolean loops, float gain) {
    this.resourcePath = resourcePath;
    this.loops = loops;
    this.gain = gain;
  }

  public String resourcePath() {
    return resourcePath;
  }

  public boolean loops() {
    return loops;
  }

  public float gain() {
    return gain;
  }
}
