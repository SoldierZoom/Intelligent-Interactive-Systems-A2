package ac.uk.iisa.claimcheck;

import ac.uk.iisa.claimcheck.ui.GUI;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GUI window = new GUI();
            window.setVisible(true);
        });
    }
}
