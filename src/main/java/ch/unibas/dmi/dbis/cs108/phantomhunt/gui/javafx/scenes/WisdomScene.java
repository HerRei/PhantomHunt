package ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.scenes;

import ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.mvc.controller.EventHandlers;
import ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.mvc.controller.SceneManager;
import ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.mvc.model.GameModel;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/** Shows the daily quote and unlocks a small round-score bonus after reflection time. */
public class WisdomScene implements SceneInterface {
  private static final int REQUIRED_SECONDS = 15;
  private static final String QOTD_HOST = "djxmmx.net";
  private static final int QOTD_PORT = 17;
  private static final String FALLBACK_QUOTE =
      "\"Your mind is like water, my friend. When it is agitated, it becomes difficult to see. "
          + "But if you allow it to settle, the answer becomes clear.\" - Master Oogway";

  private final Scene scene;
  private final Label quoteLabel;
  private final Label timerLabel;
  private final Label statusLabel;
  private final ProgressBar progressBar;
  private final Button backButton;
  private Timeline timeline;
  private long openedAtMillis;
  private boolean claimSent;
  private SceneProtocol returnScene = SceneProtocol.HOME;

  public WisdomScene() {
    Label titleLabel = new Label("Daily Wisdom");
    titleLabel.setStyle(SceneStyle.WISDOM_TITLE);

    Label sloganLabel = new Label("A little wisdom a day keeps the brainrot away.");
    sloganLabel.setStyle(SceneStyle.WISDOM_SLOGAN);

    quoteLabel = new Label();
    quoteLabel.setWrapText(true);
    quoteLabel.setMaxWidth(560);
    quoteLabel.setStyle(SceneStyle.WISDOM_QUOTE);

    timerLabel = new Label();
    timerLabel.setStyle(SceneStyle.WISDOM_TIMER);

    progressBar = new ProgressBar(0.0D);
    progressBar.setMaxWidth(420);

    statusLabel = new Label();
    statusLabel.textProperty().bind(GameModel.getInstance().wisdomStatusProperty());
    statusLabel.setWrapText(true);
    statusLabel.setMaxWidth(560);
    statusLabel.setStyle(SceneStyle.WISDOM_STATUS);

    backButton = new Button("Back");
    backButton.setStyle(SceneStyle.BUTTON);
    backButton.setOnAction(e -> closeWisdom());

    VBox root =
        new VBox(
            18, titleLabel, sloganLabel, quoteLabel, timerLabel, progressBar, statusLabel, backButton);
    root.setAlignment(Pos.CENTER);
    root.setPadding(new Insets(40));
    root.setStyle(SceneStyle.DARK_BACKGROUND);

    SceneManager sceneManager = SceneManager.getInstance();
    scene = new Scene(root, sceneManager.getWidth(), sceneManager.getHeight());
  }

  public void openFrom(SceneProtocol returnScene) {
    this.returnScene = returnScene == null ? SceneProtocol.HOME : returnScene;
    loadWisdom();
    openedAtMillis = System.currentTimeMillis();
    claimSent = false;
    progressBar.setProgress(0.0D);
    GameModel.getInstance().setWisdomStatus("");
    EventHandlers.getInstance().sendWisdomStart();
    startTimer();
  }

  private void startTimer() {
    if (timeline != null) {
      timeline.stop();
    }
    timeline = new Timeline(new KeyFrame(Duration.millis(250), e -> updateTimer()));
    timeline.setCycleCount(Timeline.INDEFINITE);
    timeline.play();
    updateTimer();
  }

  private void updateTimer() {
    if (GameModel.getInstance().wisdomBonusReadyProperty().get()) {
      timerLabel.setText("Thank you and enjoy ;)");
      progressBar.setProgress(1.0D);
      stopTimer();
      return;
    }

    long elapsedMillis = System.currentTimeMillis() - openedAtMillis;
    int elapsedSeconds = (int) Math.min(REQUIRED_SECONDS, elapsedMillis / 1000L);
    timerLabel.setText("Reflecting... " + elapsedSeconds + " / " + REQUIRED_SECONDS + "s");
    progressBar.setProgress(Math.min(1.0D, elapsedMillis / (REQUIRED_SECONDS * 1000.0D)));

    if (!claimSent && elapsedMillis >= REQUIRED_SECONDS * 1000L) {
      claimSent = true;
      EventHandlers.getInstance().sendWisdomClaim();
    }
  }

  private void closeWisdom() {
    if (!claimSent && !GameModel.getInstance().wisdomBonusReadyProperty().get()) {
      EventHandlers.getInstance().sendWisdomCancel();
    }
    stopTimer();
    SceneManager.getInstance().showScene(returnScene);
  }

  private void stopTimer() {
    if (timeline != null) {
      timeline.stop();
    }
  }

  private void loadWisdom() {
    quoteLabel.setText("Loading wisdom...");
    new Thread(
            () -> {
              String quote = FALLBACK_QUOTE;
              try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(QOTD_HOST, QOTD_PORT), 3000);
                socket.setSoTimeout(3000);
                BufferedReader reader =
                    new BufferedReader(
                        new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder wisdom = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                  if (!line.isBlank()) {
                    if (wisdom.length() > 0) {
                      wisdom.append(" ");
                    }
                    wisdom.append(line.trim());
                  }
                }
                if (wisdom.length() > 0) {
                  quote = wisdom.toString();
                }
              } catch (Exception ignored) {
                quote = FALLBACK_QUOTE;
              }
              String finalQuote = quote;
              Platform.runLater(() -> quoteLabel.setText(finalQuote));
            })
        .start();
  }

  @Override
  public Scene getScene() {
    return scene;
  }
}
