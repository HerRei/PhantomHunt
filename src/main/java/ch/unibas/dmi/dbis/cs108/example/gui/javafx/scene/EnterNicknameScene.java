package ch.unibas.dmi.dbis.cs108.example.gui.javafx.scene;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class EnterNicknameScene {
  private Button enterNicknameButton;
  private TextField nicknameField;

  public Scene createScene(java.util.function.Consumer<String> onNicknameEntered, String currentNickname) {
    Label titleLabel = new Label("Phantom Hunt - Nickname Selection");
    titleLabel.setStyle("-fx-font-size: 28px; -fx-font-weight: bold;");

    Label infoLabel = new Label("Pleas enter a Nickname. (can be changed afterwards)");
    infoLabel.setStyle("-fx-font-size: 16px;");

    Label descriptionLabel = new Label("FYI: If the username should already be used, you will be assigened an other Username");
    descriptionLabel.setStyle("-fx-font-size: 16px;");

    nicknameField = new TextField();
    nicknameField.setPromptText("Enter nickname here");
    nicknameField.setMaxWidth(250);
    nicknameField.setText(currentNickname);

    enterNicknameButton = new Button("Enter Nickname");
    enterNicknameButton.setOnAction(event -> {
      String nickname = nicknameField.getText().trim();
      if(!nickname.isEmpty()) {
        onNicknameEntered.accept(nickname);
      }
    });

    /* Old version
    enterNicknameButton.setOnAction(event -> onContinue.run()); //sets action to clicking of button
     */

    enterNicknameButton.setMaxWidth(150);

    VBox layout = new VBox();
    layout.setAlignment(Pos.CENTER);
    layout.setSpacing(15);
    layout.setPadding(new Insets(30));
    layout.getChildren().addAll(
        titleLabel,
        infoLabel,
        descriptionLabel,
        nicknameField,
        enterNicknameButton
        );


    return new Scene(layout, 1280, 960);
  }

  public String getNickname() {
    return nicknameField.getText().trim();
  }

  public Button getContinueButton() { return enterNicknameButton; }
}
