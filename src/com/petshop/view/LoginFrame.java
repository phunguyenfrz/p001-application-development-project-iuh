package com.petshop.view;

import com.petshop.controller.LoginController;
import com.petshop.model.User;
import com.petshop.util.SessionManager;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

public class LoginFrame extends JFrame {
    private final JTextField usernameField = new JTextField();
    private final JPasswordField passwordField = new JPasswordField();
    private final LoginController loginController = new LoginController();

    public LoginFrame() {
        setTitle("PetShop Management | Đăng nhập");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(460, 360);
        setLocationRelativeTo(null);
        setResizable(false);
        buildUi();
    }

    private void buildUi() {
        JPanel content = new JPanel(new BorderLayout(0, 18));
        content.setBorder(BorderFactory.createEmptyBorder(28, 40, 28, 40));

        JLabel title = new JLabel("PETSHOP MANAGEMENT", JLabel.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 24));
        title.setForeground(new Color(35, 78, 112));
        content.add(title, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(7, 5, 7, 5);
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.weightx = 1;

        addField(form, constraints, 0, "Tên đăng nhập", usernameField);
        addField(form, constraints, 1, "Mật khẩu", passwordField);

        JLabel hint = new JLabel("Tài khoản mẫu: owner / owner123 hoặc staff / staff123");
        hint.setFont(new Font("SansSerif", Font.PLAIN, 11));
        hint.setForeground(Color.GRAY);
        constraints.gridx = 0;
        constraints.gridy = 2;
        constraints.gridwidth = 2;
        form.add(hint, constraints);
        content.add(form, BorderLayout.CENTER);

        JButton loginButton = new JButton("Đăng nhập");
        loginButton.setPreferredSize(new Dimension(140, 38));
        loginButton.addActionListener(event -> login());
        passwordField.addActionListener(event -> login());

        JPanel actions = new JPanel();
        actions.add(loginButton);
        content.add(actions, BorderLayout.SOUTH);
        setContentPane(content);
    }

    private void addField(JPanel form, GridBagConstraints constraints, int row, String label, JTextField field) {
        constraints.gridx = 0;
        constraints.gridy = row;
        constraints.gridwidth = 1;
        constraints.weightx = 0;
        form.add(new JLabel(label + ":"), constraints);

        constraints.gridx = 1;
        constraints.weightx = 1;
        field.setPreferredSize(new Dimension(220, 30));
        form.add(field, constraints);
    }

    private void login() {
        User user = loginController.authenticate(usernameField.getText(), new String(passwordField.getPassword()));
        if (user == null) {
            JOptionPane.showMessageDialog(this, "Tên đăng nhập hoặc mật khẩu không đúng.", "Đăng nhập thất bại", JOptionPane.ERROR_MESSAGE);
            passwordField.selectAll();
            passwordField.requestFocusInWindow();
            return;
        }

        SessionManager.login(user);
        dispose();
        new MainDashboard().setVisible(true);
    }
}
