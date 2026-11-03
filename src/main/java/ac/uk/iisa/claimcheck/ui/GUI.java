package ac.uk.iisa.claimcheck.ui;

import ac.uk.iisa.claimcheck.service.ClaimChecker;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

public class GUI extends JFrame {
    private JPanel mainPanel;
    private JPanel inputPanel;
    private JPanel actionsPanel;
    private JLabel titleLabel;
    private JLabel subtitleLabel;
    private JLabel inputLabel;
    private JLabel hintLabel;
    private JLabel resultsLabel;
    private JLabel sourcesLabel;
    private JTextArea inputTextArea;
    private JTextArea resultsTextArea;
    private JTextArea sourcesTextArea;
    private JButton checkButton;

    private static final String INPUT_HINT =
            "Paste the AI-generated answer here. You’ll be able to inspect its claims and supporting evidence.";
    private static final String SOURCES_HINT =
            "Paste the source passages claims should be checked against. Include each source title or link.";

    public GUI() {
        setTitle("ClaimCheck");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setContentPane(mainPanel);
        setMinimumSize(new Dimension(760, 700));
        setSize(940, 800);
        setLocationRelativeTo(null);

        URL logoUrl = getClass().getResource("/images/logo.png");
        if (logoUrl != null) {
            setIconImage(new ImageIcon(logoUrl).getImage());
        }

        styleInterface();
        installPlaceholders();
        checkButton.addActionListener(event -> handleCheckResponse());
    }

    private void styleInterface() {
        Color page = new Color(0x2c2c2c);
        Color surface = new Color(56, 56, 56);
        Color ink = new Color(242, 244, 247);
        Color muted = new Color(185, 190, 199);
        Color blue = new Color(62, 103, 218);
        Color blueHover = new Color(45, 83, 192);
        Color line = new Color(82, 82, 82);

        Font normal = new Font("Trebuchet MS", Font.PLAIN, 14);

        mainPanel.setBackground(page);
        inputPanel.setBackground(page);
        actionsPanel.setBackground(page);

        titleLabel.setFont(normal.deriveFont(Font.BOLD, 28f));
        titleLabel.setForeground(ink);
        subtitleLabel.setFont(normal.deriveFont(Font.PLAIN, 15f));
        subtitleLabel.setForeground(muted);
        inputLabel.setFont(normal.deriveFont(Font.BOLD, 16f));
        inputLabel.setForeground(ink);
        resultsLabel.setFont(normal.deriveFont(Font.BOLD, 16f));
        resultsLabel.setForeground(ink);
        sourcesLabel.setFont(normal.deriveFont(Font.BOLD, 16f));
        sourcesLabel.setForeground(ink);
        hintLabel.setFont(normal.deriveFont(Font.PLAIN, 13f));
        hintLabel.setForeground(muted);

        inputTextArea.setFont(normal.deriveFont(Font.PLAIN, 15f));
        inputTextArea.setBackground(surface);
        inputTextArea.setCaretColor(ink);
        inputTextArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(line),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)));
        inputTextArea.setMargin(new Insets(0, 0, 0, 0));

        resultsTextArea.setFont(normal.deriveFont(Font.PLAIN, 14f));
        resultsTextArea.setBackground(surface);
        resultsTextArea.setCaretColor(ink);
        resultsTextArea.setForeground(muted);
        resultsTextArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(line),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)));

        sourcesTextArea.setFont(normal.deriveFont(Font.PLAIN, 14f));
        sourcesTextArea.setBackground(surface);
        sourcesTextArea.setForeground(muted);
        sourcesTextArea.setCaretColor(ink);
        sourcesTextArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(line),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)));

        checkButton.setFont(normal.deriveFont(Font.BOLD, 15f));
        checkButton.setForeground(Color.WHITE);
        checkButton.setBackground(blue);
        checkButton.setOpaque(true);
        checkButton.setBorderPainted(false);
        checkButton.setFocusPainted(false);
        checkButton.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));
        checkButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        checkButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent event) {
                checkButton.setBackground(blueHover);
            }

            @Override
            public void mouseExited(MouseEvent event) {
                checkButton.setBackground(blue);
            }
        });
    }

    private void installPlaceholders() {
        installPlaceholder(inputTextArea, INPUT_HINT);
        installPlaceholder(sourcesTextArea, SOURCES_HINT);
    }

    private void installPlaceholder(JTextArea textArea, String placeholder) {
        Color hintColor = new Color(165, 170, 179);
        Color textColor = new Color(242, 244, 247);
        textArea.setText(placeholder);
        textArea.setForeground(hintColor);

        textArea.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent event) {
                if (textArea.getText().equals(placeholder)) {
                    textArea.setText("");
                    textArea.setForeground(textColor);
                }
            }

            @Override
            public void focusLost(FocusEvent event) {
                if (textArea.getText().isBlank()) {
                    textArea.setText(placeholder);
                    textArea.setForeground(hintColor);
                }
            }
        });
    }

    private void handleCheckResponse() {
        String response = inputTextArea.getText().trim();
        if (response.isEmpty() || response.equals(INPUT_HINT)) {
            hintLabel.setText("Paste an AI response above to get started.");
            return;
        }

        String sources = sourcesTextArea.getText().trim();
        if (sources.isEmpty() || sources.equals(SOURCES_HINT)) {
            hintLabel.setText("Add source passages before checking this response.");
            sourcesTextArea.requestFocusInWindow();
            return;
        }


        hintLabel.setText("Response and source passages entered.");
        resultsTextArea.setForeground(new Color(185, 190, 199));
        resultsTextArea.setText("Your response is ready for review. Claim extraction and evidence matching are not connected yet.");

        ClaimChecker checker = new ClaimChecker();
        String result = checker.check(response, sources);
        resultsTextArea.setText(result);
    }
}
