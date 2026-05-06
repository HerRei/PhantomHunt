package ch.unibas.dmi.dbis.cs108.phantomhunt.sound;

import java.util.List;

/** Shared server/client playlist definition for synchronized menu music. */
public final class MenuMusicPlaylist {

  private static final List<Track> TRACKS =
      List.of(
          new Track(SoundEffect.DESCENT_INTO_THE_OUBLIETTE, 147_200L),
          new Track(SoundEffect.WINTER_AT_THE_GATE, 104_829L),
          new Track(SoundEffect.VOR_TA_KHEI, 174_237L));

  private static final long CYCLE_DURATION_MILLIS =
      TRACKS.stream().mapToLong(Track::durationMillis).sum();

  private MenuMusicPlaylist() {}

  public record Track(SoundEffect effect, long durationMillis) {}

  public record PlaybackState(SoundEffect effect, long offsetMillis, long remainingMillis) {}

  public static List<Track> tracks() {
    return TRACKS;
  }

  public static PlaybackState currentState(long nowMillis) {
    return stateAtCycleOffset(Math.floorMod(nowMillis, CYCLE_DURATION_MILLIS));
  }

  public static PlaybackState advance(PlaybackState state, long elapsedMillis) {
    int trackIndex = indexOf(state.effect());
    long cycleOffset =
        offsetBefore(trackIndex) + state.offsetMillis() + Math.max(0L, elapsedMillis);
    return stateAtCycleOffset(Math.floorMod(cycleOffset, CYCLE_DURATION_MILLIS));
  }

  public static String toPayload(PlaybackState state) {
    return state.effect().name() + " " + state.offsetMillis();
  }

  public static PlaybackState fromPayload(String payload) {
    String[] parts = payload.trim().split("\\s+");
    if (parts.length < 2) {
      throw new IllegalArgumentException("Menu music payload must contain track and offset.");
    }

    SoundEffect effect = SoundEffect.valueOf(parts[0]);
    Track track = findTrack(effect);
    long offsetMillis = Math.floorMod(Long.parseLong(parts[1]), track.durationMillis());
    return new PlaybackState(effect, offsetMillis, track.durationMillis() - offsetMillis);
  }

  private static PlaybackState stateAtCycleOffset(long cycleOffsetMillis) {
    long cursor = cycleOffsetMillis;
    for (Track track : TRACKS) {
      if (cursor < track.durationMillis()) {
        return new PlaybackState(
            track.effect(), cursor, Math.max(1L, track.durationMillis() - cursor));
      }
      cursor -= track.durationMillis();
    }
    Track first = TRACKS.getFirst();
    return new PlaybackState(first.effect(), 0L, first.durationMillis());
  }

  private static long offsetBefore(int trackIndex) {
    long offset = 0L;
    for (int i = 0; i < trackIndex; i++) {
      offset += TRACKS.get(i).durationMillis();
    }
    return offset;
  }

  private static Track findTrack(SoundEffect effect) {
    for (Track track : TRACKS) {
      if (track.effect() == effect) {
        return track;
      }
    }
    throw new IllegalArgumentException("Not a menu music track: " + effect);
  }

  private static int indexOf(SoundEffect effect) {
    for (int i = 0; i < TRACKS.size(); i++) {
      if (TRACKS.get(i).effect() == effect) {
        return i;
      }
    }
    throw new IllegalArgumentException("Not a menu music track: " + effect);
  }
}
