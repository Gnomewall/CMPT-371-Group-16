import javax.swing.*;
import java.awt.*;

public class drawing_board {

    //REFERENCE:
    //https://www.ssaurel.com/blog/learn-how-to-make-a-swing-painting-and-drawing-application/
    DrawArea drawArea;

    //show canvas/frame
    public void show(){
        //create main frame
        //TODO: name the game
        JFrame frame = new JFrame("TODO: Name The Game!");

        //get frame container
        Container content = frame.getContentPane();
        //set layout on content pane
        content.setLayout(new BorderLayout());

        //set cursor image
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Image image = toolkit.getImage("Cursor image/pencil cartoon.png");
        Cursor c = toolkit.createCustomCursor(image , new Point(content.getX(),
                content.getY()), "img");
        content.setCursor (c);

        //create the drawing area
        drawArea = new DrawArea();

        //and add to content pane
        content.add(drawArea,BorderLayout.CENTER);

        //set size of frame
        frame.setSize(600, 600);
        // can close frame
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        // show the swing paint result
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        // might be useful if we need multiple threads
        SwingUtilities.invokeLater(() -> {

            //call show method to open window
            new drawing_board().show();
        });
    }

}
