package ch.unibas.dmi.dbis.cs108.example.gui.javafx;

import javafx.application.Application;

/**
 * Starts the JavaFX GUI application
 */
public class ClientGuiMain {

  /**
   * This is simply a wrapper to launch the {@link GUI} class.
   * The reason this class exists is documented in {@link GUI#main(String[])}
   */
  public static void main(String[] args) {
    Application.launch(GUI.class, args);
  }
}