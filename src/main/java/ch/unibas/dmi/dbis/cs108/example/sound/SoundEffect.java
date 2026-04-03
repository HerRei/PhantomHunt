package ch.unibas.dmi.dbis.cs108.example.sound;

public enum SoundEffect {
  WIND_OUTSIDE_ROOM_TONE("/audio/wind-outside-room-tone.wav", true, 0.30f),
  MAN_SCREAM("/audio/man-scream.wav", false, 0.85f),
  DRAGGING_CHAIN("/audio/dragging-chain.wav", false, 0.65f),
  RUNNING_ON_FLOOR("/audio/running-on-floor.wav", false, 0.75f),
  DESCENT_WHOOSH("/audio/descent-whoosh.wav", false, 0.85f);

  private final String resourcePath;
  private final boolean loops;
  private final float gain;

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
