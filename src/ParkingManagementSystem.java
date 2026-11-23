import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ParkingManagementSystem extends JFrame {

    private JPanel mainPanel;
    private JPanel page1;
    private JPanel page3;
    private JButton dashboardButton;
    private JPanel mainContentPanel;
    private JTextField username;
    private JLabel login;
    private JPasswordField password;
    private JButton loginButton;
    private JCheckBox rememberMeCheckBox;
    private JButton forgotPasswordButton;
    private JLabel incorrectPassword;
    private JButton somethingButton;
    private JButton newAccount;
    private JPanel page2;
    private JTextField textField1;
    private JPasswordField passwordField1;
    private JPasswordField passwordField2;
    private JButton signUpButton;
    private JScrollPane dashBoard;
    private JPanel dashboardContent;
    private JButton backButton;
    private JLabel date;
    private JLabel time;


    private JPanel[] pages;
    private int currentIndex = 0;

    public ParkingManagementSystem() {
        pages = new JPanel[]{page1, page2, page3};
        showPage(currentIndex);

        newAccount.addActionListener(e -> showPage(1));
        loginButton.addActionListener(e -> showPage(2));

        signUpButton.addActionListener(e -> showPage(0));
        backButton.addActionListener(e -> showPage(0));
        makeHoverButton(newAccount);
    }

    private void makeHoverButton(JButton button) {

        // Default appearance
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);

        button.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseEntered(MouseEvent e) {
                button.setOpaque(true);
                button.setContentAreaFilled(true);
                button.setBackground(new Color(180, 180, 180));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setOpaque(false);
                button.setContentAreaFilled(false);
            }
        });
    }

    private void showPage(int index) {

        currentIndex = index;

        for (int i = 0; i < pages.length; i++) {
            pages[i].setVisible(i == index);
        }

        // Highlight New Account on Sign Up page
        newAccount.setOpaque(index == 1);
        newAccount.setContentAreaFilled(index == 1);

        if (index == 1) {
            newAccount.setBackground(new Color(180, 180, 180));
        }
    }



    public static void main(String[] args) {


        JFrame frame = new JFrame("Parking Management");

        ParkingManagementSystem parkingSystem =
                new ParkingManagementSystem();

        frame.setContentPane(parkingSystem.mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 500);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}