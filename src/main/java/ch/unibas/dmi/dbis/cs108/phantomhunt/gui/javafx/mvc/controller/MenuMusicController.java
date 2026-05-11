package ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.mvc.controller;

import ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.scenes.SceneProtocol;
import ch.unibas.dmi.dbis.cs108.phantomhunt.sound.MenuMusicPlaylist;
import ch.unibas.dmi.dbis.cs108.phantomhunt.sound.SoundEffect;
import ch.unibas.dmi.dbis.cs108.phantomhunt.sound.SoundManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/** Keeps menu music synchronized to the server playlist while the client is in menu scenes. */
public final class MenuMusicController {

  private static final Logger LOGGER = LogManager.getLogger(MenuMusicController.class);
  private static final MenuMusicController INSTANCE = new MenuMusicController();
  private static final Set<SceneProtocol> MENU_SCENES =
      EnumSet.of(
          SceneProtocol.HOME,
          SceneProtocol.NICKNAME,
          SceneProtocol.JOINLOBBY,
          SceneProtocol.CREATELOBBY,
          SceneProtocol.KEY_BINDING,
          SceneProtocol.WISDOM,
          SceneProtocol.HIGHSCORE);

  private final ScheduledExecutorService scheduler =
      Executors.newSingleThreadScheduledExecutor(
          runnable -> {
            Thread thread = new Thread(runnable, "phantom-hunt-menu-music");
            thread.setDaemon(true);
            return thread;
          });

  private ScheduledFuture<?> nextRotation;
  private MenuMusicPlaylist.PlaybackState lastSync;
  private long lastSyncReceivedAtMillis;
  private boolean menuActive;

  private MenuMusicController() {}

  public static MenuMusicController getInstance() {
    return INSTANCE;
  }

  public synchronized void onSceneChanged(SceneProtocol scene) {
    menuActive = MENU_SCENES.contains(scene);
    if (menuActive) {
      playCurrentSync();
    } else {
      stopMenuTracks();
      cancelNextRotation();
    }
  }

  public synchronized void applyServerSync(String payload) {
    try {
      lastSync = MenuMusicPlaylist.fromPayload(payload);
      lastSyncReceivedAtMillis = System.currentTimeMillis();
      if (menuActive) {
        playCurrentSync();
      }
    } catch (IllegalArgumentException e) {
      LOGGER.warn("Ignoring invalid menu music sync '{}': {}", payload, e.getMessage());
    }
  }

  public synchronized void stop() {
    menuActive = false;
    stopMenuTracks();
    cancelNextRotation();
  }

  public synchronized void shutdown() {
    stop();
    scheduler.shutdownNow();
  }

  private void playCurrentSync() {
    if (lastSync == null) {
      return;
    }

    long now = System.currentTimeMillis();
    MenuMusicPlaylist.PlaybackState current =
        MenuMusicPlaylist.advance(lastSync, now - lastSyncReceivedAtMillis);
    lastSync = current;
    lastSyncReceivedAtMillis = now;

    stopMenuTracks();
    SoundManager.getInstance().playFrom(current.effect(), current.offsetMillis() / 1000.0f);
    scheduleNextRotation(current.remainingMillis());
  }

  private void scheduleNextRotation(long delayMillis) {
    cancelNextRotation();
    long safeDelayMillis = Math.max(200L, delayMillis);
    nextRotation =
        scheduler.schedule(
            () -> {
              synchronized (MenuMusicController.this) {
                if (menuActive) {
                  playCurrentSync();
                }
              }
            },
            safeDelayMillis,
            TimeUnit.MILLISECONDS);
  }

  private void cancelNextRotation() {
    if (nextRotation != null) {
      nextRotation.cancel(false);
      nextRotation = null;
    }
  }

  private void stopMenuTracks() {
    for (MenuMusicPlaylist.Track track : MenuMusicPlaylist.tracks()) {
      SoundEffect effect = track.effect();
      SoundManager.getInstance().stop(effect);
    }
  }
}
