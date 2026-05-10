package ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.scenes;

/** Central JavaFX inline styles shared by all scenes.
 * Disclaimer: For this Class we used ChatGpt to create a template dor darm mode Ui Fields.
 * */
public final class SceneStyle {

  private SceneStyle() {}

  public static final String DARK_BACKGROUND = "-fx-background-color: #2b2b2b;";
  public static final String PANEL_BACKGROUND = "-fx-background-color: #313335;";
  public static final String GAME_BACKGROUND = "-fx-background-color: black;";

  public static final String BUTTON =
      "-fx-background-color: #444; -fx-text-fill: white; -fx-font-size: 13px; "
          + "-fx-font-weight: bold; -fx-padding: 8 22; -fx-background-radius: 6;";
  public static final String BUTTON_COMPACT =
      "-fx-background-color: #444; -fx-text-fill: white; -fx-font-size: 13px; "
          + "-fx-font-weight: bold; -fx-background-radius: 6;";
  public static final String BUTTON_LARGE =
      "-fx-background-color: #444; -fx-text-fill: white; -fx-font-size: 14px; "
          + "-fx-font-weight: bold; -fx-padding: 10 28; -fx-background-radius: 6;";
  public static final String BUTTON_MUTED =
      "-fx-background-color: #555; -fx-text-fill: white; -fx-font-size: 13px;";
  public static final String BUTTON_PRIMARY =
      "-fx-background-color: #007ACC; -fx-text-fill: white; -fx-font-size: 14px; "
          + "-fx-font-weight: bold; -fx-padding: 10 28; -fx-background-radius: 6;";
  public static final String BUTTON_PRIMARY_SMALL =
      "-fx-background-color: #007ACC; -fx-text-fill: white; -fx-font-size: 13px; "
          + "-fx-font-weight: bold; -fx-padding: 10 28; -fx-background-radius: 6;";
  public static final String BUTTON_ACTIVE =
      "-fx-background-color: #007ACC; -fx-text-fill: white; -fx-font-size: 13px;";

  public static final String INPUT =
      "-fx-background-color: #3c3f41; -fx-text-fill: white; -fx-prompt-text-fill: #888;";
  public static final String INPUT_LARGE =
      INPUT + " -fx-font-size: 14px;";
  public static final String LIST =
      "-fx-background-color: #3c3f41; -fx-text-fill: grey;";
  public static final String TABLE =
      "-fx-background-color: #3c3f41; -fx-text-fill: grey;";
  public static final String TEXT_AREA =
      "-fx-control-inner-background: #3c3f41; -fx-text-fill: grey;";

  public static final String TITLE =
      "-fx-text-fill: white; -fx-font-size: 22px; -fx-font-weight: bold;";
  public static final String TITLE_LARGE =
      "-fx-text-fill: white; -fx-font-size: 26px; -fx-font-weight: bold;";
  public static final String TITLE_HERO =
      "-fx-text-fill: white; -fx-font-size: 36px; -fx-font-weight: bold;";
  public static final String PANEL_TITLE =
      "-fx-text-fill: grey; -fx-font-size: 15px; -fx-font-weight: bold;";
  public static final String PROFILE_NAME =
      "-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;";
  public static final String SECTION_LABEL =
      "-fx-text-fill: #aaaaaa; -fx-font-size: 12px; -fx-font-weight: bold;";
  public static final String SECTION_LABEL_SMALL =
      "-fx-text-fill: #aaaaaa; -fx-font-size: 11px; -fx-font-weight: bold;";
  public static final String SUBTLE_TEXT =
      "-fx-text-fill: #aaaaaa; -fx-font-size: 13px;";
  public static final String BODY_TEXT =
      "-fx-text-fill: white; -fx-font-size: 13px;";
  public static final String BODY_TEXT_LARGE =
      "-fx-text-fill: white; -fx-font-size: 14px;";
  public static final String GOLD_LABEL =
      "-fx-text-fill: #FFD700; -fx-font-size: 16px; -fx-font-weight: bold;";
  public static final String GOLD_TEXT =
      "-fx-text-fill: #FFD700; -fx-font-size: 22px; -fx-font-weight: bold;";
  public static final String SCORE_TEXT =
      "-fx-text-fill: #00FF00; -fx-font-size: 48px; -fx-font-weight: bold;";
  public static final String SCORE_TEXT_SMALL =
      "-fx-text-fill: #00FF00; -fx-font-size: 26px; -fx-font-weight: bold;";
  public static final String ERROR_TEXT =
      "-fx-text-fill: #ff6b6b; -fx-font-size: 12px; -fx-font-weight: bold;";
  public static final String NAME_BADGE =
      "-fx-text-fill: black; -fx-font-weight: bold; -fx-font-size: 13px; "
          + "-fx-background-color: white; -fx-padding: 2 6; -fx-background-radius: 4;";
  public static final String ROLE_LABEL =
      "-fx-font-weight: bold; -fx-font-size: 13px;";
  public static final String ROLE_WAITING =
      "-fx-text-fill: #aaaaaa; -fx-font-size: 13px; -fx-font-weight: bold;";
  public static final String HINT_TEXT =
      "-fx-text-fill: #555; -fx-font-size: 10px;";
  public static final String TIME_TEXT =
      "-fx-text-fill: #FFD700; -fx-font-weight: bold; -fx-font-size: 13px;";
  public static final String WISDOM_TITLE =
      "-fx-text-fill: white; -fx-font-size: 32px; -fx-font-weight: bold;";
  public static final String WISDOM_SLOGAN =
      "-fx-text-fill: #aaaaaa; -fx-font-size: 15px;";
  public static final String WISDOM_QUOTE =
      "-fx-text-fill: white; -fx-font-size: 20px; -fx-font-style: italic;";
  public static final String WISDOM_TIMER =
      "-fx-text-fill: white; -fx-font-size: 16px;";
  public static final String WISDOM_STATUS =
      "-fx-text-fill: #5fbf62; -fx-font-size: 14px;";

  public static String comboBoxCell(boolean empty) {
    return "-fx-background-color: #3c3f41; -fx-text-fill: " + (empty ? "#888;" : "white;");
  }

  public static String roleColor(String color) {
    return "-fx-text-fill: " + color + "; -fx-font-size: 13px; -fx-font-weight: bold;";
  }
}
