import javax.swing.*;
import java.awt.*;

public class drawing_board {

    // REFERENCE:
    // https://www.ssaurel.com/blog/learn-how-to-make-a-swing-painting-and-drawing-application/
    public DrawArea drawArea;
    public Toolbar toolbar;

    drawing_board() {}

    public JPanel get_panel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        // set cursor image
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Image image = toolkit.getImage("Cursor image/pencil cartoon.png");
        Cursor c = toolkit.createCustomCursor(image, new Point(panel.getX(),
                panel.getY()), "img");
        panel.setCursor(c);

        // create the drawing area
        drawArea = new DrawArea();
        toolbar = new Toolbar(drawArea);

        // and add to content pane
        panel.add(drawArea, BorderLayout.CENTER);
        panel.add(toolbar, BorderLayout.SOUTH);

        panel.setSize(600, 600);

        panel.setVisible(true);

        return panel;
    }

}