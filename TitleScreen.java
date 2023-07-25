import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TitleScreen extends JFrame {

    public TitleScreen() {
        // this sets the text on top of the window
        setTitle("TODO: Still need to name the game");
        // set the opening screen to be the same size as the game
        setSize(600, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // creates a new jpanel for our entry screen
        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(new BorderLayout());

        // this puts a label on the actual panel. Will probably put a fancy title here
        JLabel titleLabel = new JLabel("TODO: Still need to name the game");
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titlePanel.add(titleLabel, BorderLayout.NORTH);

        // creates a start game button
        JButton startButton = new JButton("Start Game");
        // use this to change the size of it
        startButton.setPreferredSize(new Dimension(200, 100));
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // start the game if pressed
                startGame();
            }
        });
        // keeps the button centred
        titlePanel.add(startButton, BorderLayout.CENTER);

        // creates a button that will display the rules
        JButton rulesButton = new JButton("Rules");
        // sets size of the button
        rulesButton.setPreferredSize(new Dimension(200, 50));
        rulesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // when clicked will show the rules from the rules method
                showRules();
            }
        });
        // will put this button under our start game button
        titlePanel.add(rulesButton, BorderLayout.SOUTH);

        add(titlePanel);
        setVisible(true);
    }

    
    private void startGame() {
        // closes the title screen and goes to our drawing_board
        dispose(); 
        SwingUtilities.invokeLater(() -> new drawing_board().show()); 
    }

    private void showRules() {
        String rules = "TODO: IDK what the actual rules are yet. Add it in later"; 
        // displays a pane that has the rules on it
        JOptionPane.showMessageDialog(this, rules, "Game Rules", JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new TitleScreen();
        });
    }
}
