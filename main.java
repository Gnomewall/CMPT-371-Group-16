import javax.swing.*;
import java.awt.*;

class scribble {
    public static void main(String[] args) {
        // System.out.println("Hello World");
        SwingUtilities.invokeLater(() -> {

            // call show method to open window
            new drawing_board().show();
        });
    }
}