import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class Toolbar extends JToolBar {

    private DrawArea drawArea;
    private boolean eraserSelected = false;

    public Toolbar(DrawArea drawArea) {

        this.drawArea = drawArea;

        // Set toolbar Layout
        setFloatable(false);
        setLayout(new FlowLayout(FlowLayout.CENTER));

        // Create buttons
        JButton eraserButton = new JButton("Eraser");
        // JButton colorButton = new JButton("Color");
        JButton pencilButton = new JButton("Pencil");
        JButton clearButton = new JButton("Clear");

        // Get pencil cursor
        ImageIcon pencil = new ImageIcon("Cursor image/pencil cartoon.png");
        Cursor pencilCursor = Toolkit.getDefaultToolkit().createCustomCursor(pencil.getImage(), new Point(0, 0),
                "PencilCursor");

        // Eraser Button
        eraserButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                eraserSelected = true;
                drawArea.setEraserMode(eraserSelected);
                drawArea.setBackground(Color.white);
                drawArea.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR)); // Change cursor to crosshair
            }
        });

        // Pencil Button
        pencilButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                eraserSelected = false;
                drawArea.setEraserMode(eraserSelected);
                // drawArea.setBackground(Color.white);
                drawArea.setCursor(pencilCursor); // Change cursor back to default
            }
        });

        // Call clear method in DrawArea.java
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Clear the drawing area
                drawArea.clear();
                eraserSelected = false;
                drawArea.setEraserMode(eraserSelected);
                drawArea.setCursor(pencilCursor);

            }
        });

        // add to buttons
        add(eraserButton);
        add(pencilButton);
        // add(colorButton);
        add(clearButton);

    }

}