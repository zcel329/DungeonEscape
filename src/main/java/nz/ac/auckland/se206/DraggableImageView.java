package nz.ac.auckland.se206;

import javafx.event.EventHandler;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

/**
 * Custom implementation of an ImageView that can be dragged and dropped. This class extends the
 * JavaFX ImageView class to add dragging functionality.
 */
public class DraggableImageView {

  private double horizontalOffset = 0;
  private double verticalOffset = 0;

  /**
   * Makes the provided ImageView draggable within its parent container.
   *
   * @param imageView The ImageView to make draggable.
   */
  public void makeDraggable(ImageView imageView) {
    // Mouse Pressed Event
    imageView.setOnMousePressed(
        new EventHandler<MouseEvent>() {
          @Override
          public void handle(MouseEvent event) {
            // Record the initial mouse cursor position
            horizontalOffset = event.getSceneX() - imageView.getLayoutX();
            verticalOffset = event.getSceneY() - imageView.getLayoutY();
          }
        });

    // Mouse Dragged Event
    imageView.setOnMouseDragged(
        new EventHandler<MouseEvent>() {
          @Override
          public void handle(MouseEvent event) {
            // Calculate new position based on mouse cursor position
            double newX = event.getSceneX() - horizontalOffset;
            double newY = event.getSceneY() - verticalOffset;

            // Set the new position for the image
            imageView.setLayoutX(newX);
            imageView.setLayoutY(newY);
          }
        });

    // Mouse Released Event (optional)
    imageView.setOnMouseReleased(
        new EventHandler<MouseEvent>() {
          @Override
          public void handle(MouseEvent event) {
            // Perform any actions you want when the mouse is released
          }
        });
  }
}
