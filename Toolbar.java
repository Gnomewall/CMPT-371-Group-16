import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.DataOutputStream;
import java.net.Socket;
import java.awt.event.ActionEvent;

public class Toolbar extends JToolBar {

    private DrawArea drawArea;
    private boolean eraserSelected = false;

    private boolean isDrawing;
    private Socket socket;

    public void setSocket(Socket s) {
        socket = s;
    }

    public void setIsDrawing(boolean b) {
        isDrawing = b;
    }

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
                if (isDrawing) {
                    eraserSelected = true;
                    drawArea.setEraserMode(eraserSelected);
                    drawArea.setBackground(Color.white);
                    drawArea.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR)); // Change cursor to crosshair
                }
            }
        });

        // Pencil Button
        pencilButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isDrawing) {
                    eraserSelected = false;
                    drawArea.setEraserMode(eraserSelected);
                    // drawArea.setBackground(Color.white);
                    drawArea.setCursor(pencilCursor); // Change cursor back to default
                }
            }
        });

        // Call clear method in DrawArea.java
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (isDrawing) {
                    // Clear the drawing area
                    drawArea.clear();
                    eraserSelected = false;
                    drawArea.setEraserMode(eraserSelected);
                    drawArea.setCursor(pencilCursor);
                    
                    
                    String message = "broadcast:erase,all,clear";
                    try {
                        DataOutputStream outStream = new DataOutputStream(socket.getOutputStream());
                        outStream.writeUTF(message);
                    } catch (Exception excep) {
                        excep.printStackTrace();
                    }
                }
            }
        });

        // add to buttons
        add(eraserButton);
        add(pencilButton);
        // add(colorButton);
        add(clearButton);

    }

}