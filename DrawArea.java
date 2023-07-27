import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.DataOutputStream;
import java.net.Socket;

import javax.swing.JComponent;


//REFERENCE:
//https://www.ssaurel.com/blog/learn-how-to-make-a-swing-painting-and-drawing-application/

public class DrawArea extends JComponent {

    // Image in which we're going to draw
    private Image image;
    // Graphics2D object ==> used to draw on
    private Graphics2D g2;
    // Mouse coordinates
    private int currentX, currentY, oldX, oldY;
    private boolean eraserMode = false;
    private int eraserSize = 10;

    private boolean isDrawing;
    private Socket socket;

    public DrawArea() {
        setDoubleBuffered(false);
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                // save coord x,y when mouse is pressed
                oldX = e.getX();
                oldY = e.getY();
            }
        });

        // addMouseMotionListener(new MouseMotionAdapter() {
        // public void mouseDragged(MouseEvent e) {
        // // coord x,y when drag mouse
        // currentX = e.getX();
        // currentY = e.getY();

        // if (g2 != null) {
        // // draw line if g2 context not null
        // g2.drawLine(oldX, oldY, currentX, currentY);
        // // refresh draw area to repaint
        // repaint();
        // // store current coords x,y as olds x,y
        // oldX = currentX;
        // oldY = currentY;
        // }
        // }
        // });

        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (isDrawing) {
                    // coord x,y when drag mouse
                    currentX = e.getX();
                    currentY = e.getY();
                    
                    if (g2 != null) {
                        if (eraserMode) {
                            // Use the eraser if in eraser mode
                            g2.setColor(getBackground()); // Use the background color to simulate erasing
                            g2.fillRect(currentX - eraserSize / 2, currentY - eraserSize / 2, eraserSize,
                                    eraserSize);
                        
                            String message = "broadcast:erase," + currentX + ","+ currentY;
                            try {
                                DataOutputStream outStream = new DataOutputStream(socket.getOutputStream());
                                outStream.writeUTF(message);
                            } catch (Exception excep) {
                                excep.printStackTrace();
                            }
                        } else {
                            // Draw with the selected color or tool
                            g2.drawLine(oldX, oldY, currentX, currentY);
                        
                            String message = "broadcast:paint," + oldX + ","+ oldY + ","+ currentX + ","+ currentY;
                            try {
                                DataOutputStream outStream = new DataOutputStream(socket.getOutputStream());
                                outStream.writeUTF(message);
                            } catch (Exception excep) {
                                excep.printStackTrace();
                            }
                        }
                    
                        // refresh draw area to repaint
                        repaint();
                        // store current coords x,y as olds x,y
                        oldX = currentX;
                        oldY = currentY;
                    }
                }
            }
        });

    }

    public void setSocket(Socket s) {
        socket = s;
    }

    public void setIsDrawing(boolean b) {
        isDrawing = b;
    }

    // ****************************************************************************
    // * THIS SHOULD BE USED/CALLED BY "client.java" !!!
    // *
    // ****************************************************************************
    public void drawHelper(int ox, int oy, int x, int y) {
        g2.setColor(Color.black);
        g2.drawLine(ox, oy, x, y);
        repaint();
    }

    // ****************************************************************************
    // * THIS SHOULD BE USED/CALLED BY "client.java" !!!
    // *
    // ****************************************************************************
    public void eraseHelper(int x, int y) {
        g2.setColor(Color.white);
        g2.fillRect(x - eraserSize / 2, y - eraserSize / 2, eraserSize, eraserSize);
        g2.setColor(Color.black);
        repaint();
    }

    protected void paintComponent(Graphics g) {
        if (image == null) {
            // image to draw null ==> we create
            image = createImage(getSize().width, getSize().height);
            g2 = (Graphics2D) image.getGraphics();
            // enable antialiasing
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // clear draw area
            clear();
        }

        g.drawImage(image, 0, 0, null);
    }

    // now we create exposed methods

    // Clear all drawings
    public void clear() {

        if (g2 != null) {
            g2.setPaint(Color.white);
            // draw white on entire draw area to clear
            g2.fillRect(0, 0, getSize().width, getSize().height);
            g2.setPaint(Color.black);
            repaint();
        }
    }

    // Get eraser status
    public void setEraserMode(Boolean eraserMode) {
        this.eraserMode = eraserMode;

        if (eraserMode) {
            // To use eraser, simply set paint color to background color.
            g2.setPaint(getBackground());

        } else {
            // Else, change paint color and paint as normal.
            g2.setPaint(Color.black);
        }
    }

}