package ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.scenes;

import ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.mvc.controller.SceneManager;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;

public class IntroScene implements SceneInterface {

  private static final Logger LOGGER = LogManager.getLogger(IntroScene.class);

  private Scene localScene;
  private MediaPlayer player;
  private Text continueText;
  private boolean videoFinished;
  private boolean available;

  public IntroScene() {
    this.videoFinished = false;

    try {
      URL video = getClass().getResource("/assets/intro/intro_PL.mp4");

      if (video == null) {
        LOGGER.warn("Intro video asset was not found; skipping intro.");
        return;
      }

      Media media = new Media(video.toExternalForm());
      player = new MediaPlayer(media);

      MediaView view = new MediaView(player);

      double width = Screen.getPrimary().getBounds().getWidth();
      double height = Screen.getPrimary().getBounds().getHeight();

      view.setFitWidth(width);
      view.setFitHeight(height);
      view.setPreserveRatio(true);

      continueText = new Text("Press any key to continue...");
      continueText.setFont(new Font("Arial", 28));
      continueText.setFill(Color.BLACK);
      continueText.setVisible(false);

      StackPane.setAlignment(continueText, Pos.BOTTOM_CENTER);

      StackPane.setMargin(continueText, new Insets(0, 0, 80, 0));

      StackPane root = new StackPane();
      root.getChildren().add(view);
      root.getChildren().add(continueText);
      root.setAlignment(Pos.CENTER);

      localScene = new Scene(root, width, height);

      localScene.setOnKeyPressed(new EventHandler<KeyEvent>() {
        @Override
        public void handle(KeyEvent event) {
          if (videoFinished) {
            if (player != null) {
              player.stop();
            }
            SceneManager.getInstance().setFullscreen(false);
            SceneManager.getInstance().showScene(SceneProtocol.HOME);
          }
        }
      });

      player.setOnEndOfMedia(new Runnable() {
        @Override
        public void run() {
          videoFinished = true;
          continueText.setVisible(true);
        }
      });

      SceneManager.getInstance().setFullscreen(true);
      player.play();
      available = true;
    } catch (RuntimeException e) {
      available = false;
      localScene = null;
      SceneManager.getInstance().setFullscreen(false);
      if (player != null) {
        player.dispose();
        player = null;
      }
      LOGGER.warn("Intro video is unavailable; skipping intro.", e);
    }
  }

  @Override
  public Scene getScene() {
    return localScene;
  }

  public boolean isAvailable() {
    return available && localScene != null;
  }
}
