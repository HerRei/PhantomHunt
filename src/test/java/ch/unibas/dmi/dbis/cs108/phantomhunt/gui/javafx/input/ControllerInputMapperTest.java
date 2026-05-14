package ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.input;

import ch.unibas.dmi.dbis.cs108.phantomhunt.gui.javafx.input.ControllerInputMapper.MovementInput;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ControllerInputMapperTest {

  @Test
  void map_neutralStickAndDpad_returnsIdle() {
    MovementInput input = ControllerInputMapper.map(0.1F, -0.2F, false, false, false, false);

    assertEquals(new MovementInput(0, 0), input);
  }

  @Test
  void map_leftStickVerticalAxis_mapsToUpAndDown() {
    assertEquals(
        new MovementInput(-1, 0),
        ControllerInputMapper.map(0.0F, -0.8F, false, false, false, false));
    assertEquals(
        new MovementInput(1, 0),
        ControllerInputMapper.map(0.0F, 0.8F, false, false, false, false));
  }

  @Test
  void map_leftStickHorizontalAxis_mapsToLeftAndRight() {
    assertEquals(
        new MovementInput(0, -1),
        ControllerInputMapper.map(-0.8F, 0.0F, false, false, false, false));
    assertEquals(
        new MovementInput(0, 1),
        ControllerInputMapper.map(0.8F, 0.0F, false, false, false, false));
  }

  @Test
  void map_diagonalStickInput_usesDominantAxis() {
    assertEquals(
        new MovementInput(0, 1),
        ControllerInputMapper.map(0.9F, -0.6F, false, false, false, false));
    assertEquals(
        new MovementInput(-1, 0),
        ControllerInputMapper.map(0.6F, -0.9F, false, false, false, false));
  }

  @Test
  void map_dpadOverridesLeftStick() {
    MovementInput input = ControllerInputMapper.map(0.9F, 0.0F, true, false, false, false);

    assertEquals(new MovementInput(-1, 0), input);
  }

  @Test
  void map_opposingDpadDirectionsCancelEachOther() {
    assertEquals(
        new MovementInput(0, 1),
        ControllerInputMapper.map(0.0F, 0.0F, true, true, false, true));
    assertEquals(
        new MovementInput(0, 0),
        ControllerInputMapper.map(0.0F, 0.0F, false, false, true, true));
  }
}
