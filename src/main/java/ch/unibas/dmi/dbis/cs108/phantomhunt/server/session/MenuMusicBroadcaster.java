package ch.unibas.dmi.dbis.cs108.phantomhunt.server.session;

import ch.unibas.dmi.dbis.cs108.phantomhunt.common.protocol.Command;
import ch.unibas.dmi.dbis.cs108.phantomhunt.common.protocol.Packet;
import ch.unibas.dmi.dbis.cs108.phantomhunt.sound.MenuMusicPlaylist;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Broadcasts the authoritative menu music playlist position to all connected clients. */
public final class MenuMusicBroadcaster {

  private static final Logger LOGGER = LogManager.getLogger(MenuMusicBroadcaster.class);
  private static final long MIN_RESYNC_DELAY_MILLIS = 250L;

  private final Registry registry;
  private final ScheduledExecutorService scheduler =
      Executors.newSingleThreadScheduledExecutor(
          runnable -> {
            Thread thread = new Thread(runnable, "phantom-hunt-menu-music-sync");
            thread.setDaemon(true);
            return thread;
          });
  private boolean started;

  public MenuMusicBroadcaster(Registry registry) {
    this.registry = registry;
  }

  public synchronized void start() {
    if (started) {
      return;
    }
    started = true;
    scheduleNextSync(0L);
  }

  private void broadcastAndScheduleNext() {
    try {
      MenuMusicPlaylist.PlaybackState state =
          MenuMusicPlaylist.currentState(System.currentTimeMillis());
      registry.broadcast(Packet.of(Command.MENU_MUSIC, MenuMusicPlaylist.toPayload(state)));
      scheduleNextSync(Math.max(MIN_RESYNC_DELAY_MILLIS, state.remainingMillis()));
    } catch (RuntimeException e) {
      LOGGER.warn("Menu music sync failed; retrying shortly.", e);
      scheduleNextSync(1_000L);
    }
  }

  private void scheduleNextSync(long delayMillis) {
    scheduler.schedule(
        this::broadcastAndScheduleNext, Math.max(0L, delayMillis), TimeUnit.MILLISECONDS);
  }
}
