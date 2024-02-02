import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;

public class ProductSellingApp {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/ProductSellinApp";
    private static final String CREATE_TABLE_QUERY = "CREATE TABLE IF NOT EXISTS products (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, price REAL)";
    private static final String INSERT_PRODUCT_QUERY = "INSERT INTO products (name, price) VALUES (?, ?)";
    private static final String SELECT_PRODUCTS_QUERY = "SELECT * FROM ProductSellingApp";

    public static void main(String[] args) {
        createDatabase();

        SwingUtilities.invokeLater(() -> {
            LoginFrame loginFrame = new LoginFrame();
            loginFrame.setVisible(true);
        });
    }

    private static void createDatabase() {
        try (Connection connection = DriverManager.getConnection(DB_URL);
             PreparedStatement statement = connection.prepareStatement(CREATE_TABLE_QUERY)) {
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    static class LoginFrame extends JFrame {
        private JTextField usernameField;
        private JPasswordField passwordField;

        public LoginFrame() {
            setTitle("Login");
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setSize(300, 150);
            setLocationRelativeTo(null);

            usernameField = new JTextField(20);
            passwordField = new JPasswordField(20);
            JButton loginButton = new JButton("Login");

            loginButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String username = usernameField.getText();
                    char[] password = passwordField.getPassword();
                    // Validate username and password (you can check against a database)
                    // For simplicity, let's consider a hardcoded username and password
                    if ("admin".equals(username) && "admin123".equals(new String(password))) {
                        dispose(); // Close the login frame
                        showProductFrame();
                    } else {
                        JOptionPane.showMessageDialog(LoginFrame.this, "Invalid username or password");
                    }
                }
            });

            JPanel panel = new JPanel(new GridLayout(3, 2));
            panel.add(new JLabel("Username:"));
            panel.add(usernameField);
            panel.add(new JLabel("Password:"));
            panel.add(passwordField);
            panel.add(new JLabel());
            panel.add(loginButton);

            add(panel);
        }

        private void showProductFrame() {
            ProductFrame productFrame = new ProductFrame();
            productFrame.setVisible(true);
        }
    }

    static class ProductFrame extends JFrame {
        private DefaultListModel<String> productListModel;
        private JList<String> productList;

        public ProductFrame() {
            setTitle("Product Selling App");
            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setSize(400, 300);
            setLocationRelativeTo(null);

            productListModel = new DefaultListModel<>();
            productList = new JList<>(productListModel);

            refreshProductList();

            JButton addProductButton = new JButton("Add Product");
            addProductButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    String productName = JOptionPane.showInputDialog(ProductFrame.this, "Enter product name:");
                    if (productName != null && !productName.trim().isEmpty()) {
                        String priceStr = JOptionPane.showInputDialog(ProductFrame.this, "Enter product price:");
                        try {
                            double price = Double.parseDouble(priceStr);
                            addProduct(productName, price);
                            refreshProductList();
                        } catch (NumberFormatException ex) {
                            JOptionPane.showMessageDialog(ProductFrame.this, "Invalid price format");
                        }
                    } else {
                        JOptionPane.showMessageDialog(ProductFrame.this, "Invalid product name");
                    }
                }
            });

            JPanel panel = new JPanel(new BorderLayout());
            panel.add(new JScrollPane(productList), BorderLayout.CENTER);
            panel.add(addProductButton, BorderLayout.SOUTH);

            add(panel);
        }

        private void addProduct(String name, double price) {
            try (Connection connection = DriverManager.getConnection(DB_URL);
                 PreparedStatement statement = connection.prepareStatement(INSERT_PRODUCT_QUERY)) {
                statement.setString(1, name);
                statement.setDouble(2, price);
                statement.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        private void refreshProductList() {
            productListModel.clear();
            try (Connection connection = DriverManager.getConnection(DB_URL);
                 PreparedStatement statement = connection.prepareStatement(SELECT_PRODUCTS_QUERY);
                 ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    String productName = resultSet.getString("name");
                    double price = resultSet.getDouble("price");
                    productListModel.addElement(productName + " - $" + price);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}