import java.awt.*;
import javax.swing.*;

// The GameFrame class extends JFrame and serves as the main window for the game.
public class GameFrame extends JFrame {

    GamePanel panel; // A panel where the game content will be displayed.

    // Constructor for the GameFrame class.
    public GameFrame() {
        panel = new GamePanel(); // Initialize the game panel.
        this.add(panel); // Add the game panel to the frame.
        this.setTitle("Endless Night"); // Set the title of the window.
        this.setResizable(false); // Prevent the window from being resized.
        this.setBackground(Color.BLACK); // Set the background color of the frame.
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Ensure the application exits when the window is closed.

        // Set the icon of the window using an image from the assets folder.
        ImageIcon icon = new ImageIcon("./assets/images/logo.png");
        this.setIconImage(icon.getImage());

        this.pack(); // Adjust the frame size to fit the components.
        this.setVisible(true); // Make the frame visible.
        this.setLocationRelativeTo(null); // Center the frame on the screen.
    }
}
