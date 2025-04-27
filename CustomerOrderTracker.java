import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

// Interfaces
interface Trackable {
    String getStatus();
}

interface Refundable {
    void processRefund();
}

// Customer class
class Customer {
    private String name;
    private String customerID;

    public Customer(String name, String customerID) {
        this.name = name;
        this.customerID = customerID;
    }

    public String getName() {
        return name;
    }

    public String getCustomerID() {
        return customerID;
    }
}

// OrderItem class
class OrderItem {
    private String productName;
    private int quantity;
    private double price;

    public OrderItem(String productName, int quantity, double price) throws IllegalArgumentException {
        if (quantity < 0 || price < 0) {
            throw new IllegalArgumentException("Quantity and price must be non-negative.");
        }
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
    }

    public String getProductName() {
        return productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }
}

// Order class
class Order implements Trackable, Refundable {
    private Customer customer;
    private List<OrderItem> items;
    private String status;

    public Order(Customer customer) {
        this.customer = customer;
        this.items = new ArrayList<>();
        this.status = "Completed";
    }

    public void addItem(OrderItem item) {
        items.add(item);
    }

    public Customer getCustomer() {
        return customer;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    @Override
    public String getStatus() {
        return status;
    }

    public void cancel() {
        this.status = "Cancelled";
    }

    @Override
    public void processRefund() {
        if (!status.equals("Cancelled")) {
            cancel();
            System.out.println("Refund processed for order of customer: " + customer.getName());
        }
    }

    public double getTotal() {
        return items.stream().mapToDouble(i -> i.getPrice() * i.getQuantity()).sum();
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Customer: ").append(customer.getName()).append(" (ID: ").append(customer.getCustomerID())
                .append(")\n");
        for (OrderItem item : items) {
            sb.append("- ").append(item.getProductName()).append(" x").append(item.getQuantity())
                    .append(" @ ").append(item.getPrice()).append("\n");
        }
        sb.append("Status: ").append(status).append("\nTotal: $").append(getTotal()).append("\n");
        return sb.toString();
    }
}

// OrderManager class
class OrderManager {
    private List<Order> orders = new ArrayList<>();

    public void addOrder(Order order) {
        orders.add(order);
    }

    public List<Order> getOrdersByCustomer(String customerName) {
        return orders.stream()
                .filter(o -> o.getCustomer().getName().equalsIgnoreCase(customerName))
                .collect(Collectors.toList());
    }

    public List<Order> getOrdersByStatus(String status) {
        return orders.stream()
                .filter(o -> o.getStatus().equalsIgnoreCase(status))
                .collect(Collectors.toList());
    }

    public void saveToFile(String filename) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(filename));
        for (Order order : orders) {
            writer.write(order.toString());
            writer.write("-------------------------\n");
        }
        writer.close();
    }

    public String getMostOrderedProduct() {
        Map<String, Integer> productCount = new HashMap<>();
        for (Order order : orders) {
            for (OrderItem item : order.getItems()) {
                productCount.put(item.getProductName(),
                        productCount.getOrDefault(item.getProductName(), 0) + item.getQuantity());
            }
        }

        return productCount.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue())
                .map(e -> e.getKey() + " (" + e.getValue() + " units)")
                .orElse("No products ordered");
    }
}

// Main Class
public class CustomerOrderTracker {
    private Scanner scanner = new Scanner(System.in);
    private OrderManager orderManager = new OrderManager();

    public CustomerOrderTracker() {
        while (true) {
            System.out.println("\n--- Customer Order Tracker ---");
            System.out.println("1. Add New Order");
            System.out.println("2. Cancel & Refund Order");
            System.out.println("3. View Customer Orders");
            System.out.println("4. Filter Orders by Status");
            System.out.println("5. Most Ordered Product");
            System.out.println("6. Save Orders to File");
            System.out.println("7. Exit");
            System.out.print("Choose an option: ");

            switch (scanner.nextLine()) {
                case "1":
                    addOrder();
                    break;
                case "2":
                    refundOrder();
                    break;
                case "3":
                    viewOrdersByCustomer();
                    break;
                case "4":
                    viewOrdersByStatus();
                    break;
                case "5":
                    System.out.println("Most ordered product: " + orderManager.getMostOrderedProduct());
                    break;
                case "6":
                    try {
                        orderManager.saveToFile("order_logs.txt");
                        System.out.println("Orders saved to order_logs.txt");
                    } catch (IOException e) {
                        System.out.println("Error saving file: " + e.getMessage());
                    }
                    break;
                case "7":
                    return;
                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    private void addOrder() {
        System.out.print("Enter customer name: ");
        String name = scanner.nextLine();
        System.out.print("Enter customer ID: ");
        String id = scanner.nextLine();
        Customer customer = new Customer(name, id);
        Order order = new Order(customer);

        while (true) {
            System.out.print("Enter product name (or 'done'): ");
            String product = scanner.nextLine();
            if (product.equalsIgnoreCase("done"))
                break;

            try {
                System.out.print("Quantity: ");
                int qty = Integer.parseInt(scanner.nextLine());
                System.out.print("Price: ");
                double price = Double.parseDouble(scanner.nextLine());

                OrderItem item = new OrderItem(product, qty, price);
                order.addItem(item);
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid input: " + e.getMessage());
            }
        }

        orderManager.addOrder(order);
        System.out.println("Order added successfully.");
    }

    private void refundOrder() {
        System.out.print("Enter customer name to refund order: ");
        String name = scanner.nextLine();
        List<Order> orders = orderManager.getOrdersByCustomer(name);
        if (orders.isEmpty()) {
            System.out.println("No orders found.");
            return;
        }

        for (int i = 0; i < orders.size(); i++) {
            System.out.println("\nOrder #" + (i + 1));
            System.out.println(orders.get(i));
        }

        System.out.print("Select order number to cancel: ");
        try {
            int index = Integer.parseInt(scanner.nextLine()) - 1;
            if (index >= 0 && index < orders.size()) {
                orders.get(index).processRefund();
                System.out.println("Order cancelled and refunded.");
            } else {
                System.out.println("Invalid order number.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
        }
    }

    private void viewOrdersByCustomer() {
        System.out.print("Enter customer name: ");
        String name = scanner.nextLine();
        List<Order> orders = orderManager.getOrdersByCustomer(name);
        if (orders.isEmpty()) {
            System.out.println("No orders found for this customer.");
            return;
        }
        for (Order o : orders) {
            System.out.println(o);
            System.out.println("-------------------------");
        }
    }

    private void viewOrdersByStatus() {
        System.out.print("Enter status (Completed/Cancelled): ");
        String status = scanner.nextLine();
        List<Order> orders = orderManager.getOrdersByStatus(status);
        if (orders.isEmpty()) {
            System.out.println("No orders with this status.");
            return;
        }
        for (Order o : orders) {
            System.out.println(o);
            System.out.println("-------------------------");
        }
    }

    public static void main(String[] args) {
        new CustomerOrderTracker(); // Constructor starts the app
    }
}