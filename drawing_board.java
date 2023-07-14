import javax.swing.*;
import java.awt.*;

public class drawing_board {
    public static void main(String[] args) {
        // might be useful if we need multiple threads
        SwingUtilities.invokeLater(() -> {
            // adding a jFrame and a title to the drawing board. We can probably add the
            // game title here
            JFrame frame = new JFrame("TODO: Name The Game!");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            JPanel canvas = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    // have to add drawing logic here
                }
            };

            // the size of the canvas
            canvas.setPreferredSize(new Dimension(500, 500));

            frame.getContentPane().add(canvas);
            // frame sizing
            frame.pack();
            // makes the things visible on our canvas
            frame.setVisible(true);
        });
    }

}
