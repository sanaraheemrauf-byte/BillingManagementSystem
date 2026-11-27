package oops;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;

public class BillingManagementSystem extends JFrame {

  
	JTextField customerField, productField, quantityField, priceField;
    JLabel totalLabel;
    JTable table;
    DefaultTableModel model;

    Connection con;

    // DATABASE CONNECTION
    public BillingManagementSystem() {
        connectDatabase();

        setTitle("Billing Management System");
        setSize(850, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(new Color(238, 242, 247));

        // HEADING
        JLabel heading = new JLabel(
            "BILLING MANAGEMENT SYSTEM",
            JLabel.CENTER
        );

        heading.setFont(new Font("Arial", Font.BOLD, 22));
        heading.setForeground(Color.WHITE);
        heading.setBackground(new Color(23, 54, 93));
        heading.setOpaque(true);
        heading.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        add(heading, BorderLayout.NORTH);

        // INPUT FORM
        JPanel form = new JPanel(new GridLayout(4, 4, 10, 12));
        form.setBackground(Color.WHITE);
        form.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        customerField = new JTextField();
        productField = new JTextField();
        quantityField = new JTextField();
        priceField = new JTextField();

        form.add(new JLabel("Customer Name:"));
        form.add(customerField);

        form.add(new JLabel("Product Name:"));
        form.add(productField);

        form.add(new JLabel("Quantity:"));
        form.add(quantityField);

        form.add(new JLabel("Price (Rs.):"));
        form.add(priceField);

        JLabel totalTitle = new JLabel("Total Amount:");
        totalTitle.setFont(new Font("Arial", Font.BOLD, 14));

        totalLabel = new JLabel("Rs. 0.00");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 16));
        totalLabel.setForeground(new Color(22, 134, 91));

        form.add(totalTitle);
        form.add(totalLabel);

        JButton calculateButton = new JButton("Calculate Total");
        JButton saveButton = new JButton("Save Bill");
        JButton clearButton = new JButton("Clear");

        form.add(calculateButton);
        form.add(saveButton);
        form.add(clearButton);

        // BUTTON ACTIONS
        calculateButton.addActionListener(e -> calculateTotal());
        saveButton.addActionListener(e -> saveBill());
        clearButton.addActionListener(e -> clearFields());

        JPanel centerPanel = new JPanel(new BorderLayout(10, 10));
        centerPanel.setBackground(new Color(238, 242, 247));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 0, 20));

        centerPanel.add(form, BorderLayout.NORTH);

        // BILL RECORDS TABLE
        String[] columns = {
            "ID", "Customer", "Product",
            "Quantity", "Price", "Total", "Date"
        };

        model = new DefaultTableModel(columns, 0);
        table = new JTable(model);
        table.setRowHeight(25);

        JScrollPane scrollPane = new JScrollPane(table);

        JLabel tableHeading = new JLabel("Saved Billing Records");
        tableHeading.setFont(new Font("Arial", Font.BOLD, 16));

        JPanel tablePanel = new JPanel(new BorderLayout(5, 5));
        tablePanel.setBackground(Color.WHITE);
        tablePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        tablePanel.add(tableHeading, BorderLayout.NORTH);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        centerPanel.add(tablePanel, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);
       connectDatabase();
        showBills();

        setVisible(true);
    }

    // DATABASE CONNECTION
    void connectDatabase() {
        try {
            con = DriverManager.getConnection("jdbc:sqlite:billing.db");

            Statement stmt = con.createStatement();

            stmt.executeUpdate(
                "CREATE TABLE IF NOT EXISTS bills (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "customer TEXT," +
                "product TEXT," +
                "quantity INTEGER," +
                "price REAL," +
                "total REAL," +
                "bill_date TEXT)"
            );

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    // CALCULATE TOTAL
    void calculateTotal() {
        try {
            int quantity = Integer.parseInt(quantityField.getText());
            double price = Double.parseDouble(priceField.getText());

            if (quantity <= 0 || price < 0) {
                JOptionPane.showMessageDialog(
                    this, "Invalid quantity or price"
                );
                return;
            }

            double total = quantity * price;
            totalLabel.setText(String.format("Rs. %.2f", total));

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(
                this, "Enter valid quantity and price"
            );
        }
    }

    // SAVE BILL
    void saveBill() {
        String customer = customerField.getText().trim();
        String product = productField.getText().trim();

        try {
            int quantity = Integer.parseInt(quantityField.getText());
            double price = Double.parseDouble(priceField.getText());

            if (customer.isEmpty() || product.isEmpty()) {
                JOptionPane.showMessageDialog(
                    this, "Please enter customer and product name"
                );
                return;
            }

            if (quantity <= 0 || price < 0) {
                JOptionPane.showMessageDialog(
                    this, "Invalid quantity or price"
                );
                return;
            }

            double total = quantity * price;

            String sql = "INSERT INTO bills " +
                "(customer, product, quantity, price, total, bill_date) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, customer);
            ps.setString(2, product);
            ps.setInt(3, quantity);
            ps.setDouble(4, price);
            ps.setDouble(5, total);
            ps.setString(6, LocalDate.now().toString());

            ps.executeUpdate();

            JOptionPane.showMessageDialog(
                this, "Bill saved successfully!"
            );

            showBills();
            clearFields();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(
                this, "Enter valid quantity and price"
            );
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    // DISPLAY BILLS
    void showBills() {
        model.setRowCount(0);

        try {
            Statement stmt = con.createStatement();

            ResultSet rs = stmt.executeQuery(
                "SELECT * FROM bills ORDER BY id DESC"
            );

            while (rs.next()) {
                model.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("customer"),
                    rs.getString("product"),
                    rs.getInt("quantity"),
                    rs.getDouble("price"),
                    rs.getDouble("total"),
                    rs.getString("bill_date")
                });
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }
    }

    // CLEAR FIELDS
    void clearFields() {
        customerField.setText("");
        productField.setText("");
        quantityField.setText("");
        priceField.setText("");
        totalLabel.setText("Rs. 0.00");
    }

    // MAIN METHOD
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() ->
            new BillingManagementSystem()
        );
    }
}


	


