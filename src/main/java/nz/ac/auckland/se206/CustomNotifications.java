package nz.ac.auckland.se206;

import javafx.geometry.Pos;
import org.controlsfx.control.Notifications;

/** Utility class for generating custom notifications in the application. */
public class CustomNotifications {

  /**
   * Generates and displays a custom notification with the specified title and text.
   *
   * @param title The title of the notification.
   * @param text The content text of the notification.
   */
  public static void generateNotification(String title, String text) {
    // Generate a custom notification with the provided title and text
    Notifications.create()
        .title(title)
        .position(Pos.BOTTOM_RIGHT)
        .owner(null)
        .text(text)
        .threshold(
            1,
            Notifications.create()
                .title("Collapsed Notification")) // Set the threshold for collapsed notifications
        .showWarning(); // Show the generated notification as a warning
  }
}
