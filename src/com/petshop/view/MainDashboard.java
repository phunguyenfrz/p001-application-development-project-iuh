package com.petshop.view;

import com.petshop.model.User;
import com.petshop.util.SessionManager;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;

public class MainDashboard extends JFrame {
    public MainDashboard() {
        User user = SessionManager.getCurrentUser();
        setTitle("PetShop Management | Trang chính");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 560);
        setLocationRelativeTo(null);
        buildUi(user);
    }

    private void buildUi(User user) {
        JPanel root = new JPanel(new BorderLayout(18, 18));
        root.setBorder(BorderFactory.createEmptyBorder(22, 22, 22, 22));

        JPanel header = new JPanel(new BorderLayout());
        JLabel title = new JLabel("Quản lý cửa hàng thú cưng");
        title.setFont(new Font("SansSerif", Font.BOLD, 25));
        title.setForeground(new Color(35, 78, 112));
        header.add(title, BorderLayout.WEST);

        JLabel account = new JLabel(user.getFullName() + " | " + user.getRole().getDisplayName());
        account.setFont(new Font("SansSerif", Font.PLAIN, 14));
        header.add(account, BorderLayout.EAST);
        root.add(header, BorderLayout.NORTH);

        JPanel menu = new JPanel(new GridLayout(2, 3, 14, 14));
        addPlaceholderButton(menu, "Quản lý sản phẩm");
        addPlaceholderButton(menu, "Quản lý thú cưng");
        addPlaceholderButton(menu, "Bán hàng");
        addPlaceholderButton(menu, "Khách hàng");
        addPlaceholderButton(menu, "Báo cáo thống kê");

        if (user.getRole() == User.Role.OWNER) {
            addPlaceholderButton(menu, "Quản lý nhân viên");
        } else {
            JButton locked = new JButton("Quản lý nhân viên (Chủ cửa hàng)");
            locked.setEnabled(false);
            menu.add(locked);
        }
        root.add(menu, BorderLayout.CENTER);

        JLabel status = new JLabel("Chọn một phân hệ để bắt đầu phát triển chức năng.");
        status.setForeground(Color.DARK_GRAY);
        root.add(status, BorderLayout.SOUTH);

        JButton logoutButton = new JButton("Đăng xuất");
        logoutButton.addActionListener(event -> logout());
        header.add(logoutButton, BorderLayout.CENTER);
        setContentPane(root);
    }

    private void addPlaceholderButton(JPanel menu, String label) {
        JButton button = new JButton(label);
        button.setFont(new Font("SansSerif", Font.PLAIN, 15));
        button.addActionListener(event -> JOptionPane.showMessageDialog(
                this,
                "Phân hệ này sẽ được phát triển ở bước tiếp theo.",
                label,
                JOptionPane.INFORMATION_MESSAGE));
        menu.add(button);
    }

    private void logout() {
        SessionManager.logout();
        dispose();
        new LoginFrame().setVisible(true);
    }
}
