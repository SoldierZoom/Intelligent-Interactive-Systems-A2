package ac.uk.iisa.claimcheck.ui;

import javax.swing.*;
import java.net.URL;

public class GUI extends JFrame {
    private JPanel mainPanel;
    private JButton submitButton;
    private JLabel responseLabel;
    private JTextArea responseTextArea;

    public GUI() {
        setTitle("ClaimCheck test");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(mainPanel);
        pack();
        setSize(900, 600);
        setLocationRelativeTo(null);
        URL logoUrl = getClass().getResource("/images/logo.png");
        setIconImage(new ImageIcon(logoUrl).getImage());
    }
}
