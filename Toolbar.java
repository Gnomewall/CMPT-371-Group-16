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
        JButton clearButton = new JButton("Clear");

        // Eraser Button
        eraserButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                eraserSelected = true;
                drawArea.setEraserMode(eraserSelected);
                drawArea.setBackground(Color.white);
            }
        });

        // Call clear method in DrawArea.java
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Clear the drawing area
                drawArea.clear();
            }
        });

        // add to buttons
        add(eraserButton);
        // add(colorButton);
        add(clearButton);

    }

    // public boolean isEraserSelected() {
    // return eraserSelected;
    // }

}