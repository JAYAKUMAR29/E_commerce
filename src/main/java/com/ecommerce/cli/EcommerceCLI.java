package com.ecommerce.cli;

import com.ecommerce.model.Address;
import com.ecommerce.model.CartItem;
import com.ecommerce.model.Category;
import com.ecommerce.model.Customer;
import com.ecommerce.model.Order;
import com.ecommerce.model.OrderItem;
import com.ecommerce.model.Product;
import com.ecommerce.model.ShoppingCart;
import com.ecommerce.service.CartService;
import com.ecommerce.service.OrderService;
import com.ecommerce.service.ProductService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Scanner;

public class EcommerceCLI {

    private final ProductService productService;
    private final CartService cartService;
    private final OrderService orderService;
    private final Customer currentCustomer;
    private final Scanner scanner;

    public EcommerceCLI(ProductService productService,
                        CartService cartService,
                        OrderService orderService,
                        Customer currentCustomer) {
        this.productService = productService;
        this.cartService = cartService;
        this.orderService = orderService;
        this.currentCustomer = currentCustomer;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        System.out.println("\n========================================================");
        System.out.println("  WELCOME TO APEX COMMERCE CLI - LOGGED IN AS: " + currentCustomer.getName());
        System.out.println("========================================================");

        boolean running = true;
        while (running) {
            printMainMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> viewAllProducts();
                case "2" -> searchProducts();
                case "3" -> addToCart();
                case "4" -> viewCart();
                case "5" -> checkout();
                case "6" -> viewMyOrders();
                case "7" -> cancelAnOrder();
                case "8" -> sellerAddProduct();
                case "9" -> sellerRestockProduct();
                case "0" -> {
                    System.out.println("\nThank you for shopping with us. Goodbye!");
                    running = false;
                }
                default -> System.out.println("⚠️ Invalid choice. Please enter a valid number (0-9).");
            }
        }
    }

    private void printMainMenu() {
        System.out.println("\n---------------- MAIN MENU ----------------");
        System.out.println("1. Browse All Products");
        System.out.println("2. Search Products");
        System.out.println("3. Add Product to Cart");
        System.out.println("4. View Cart & Total");
        System.out.println("5. Checkout & Place Order");
        System.out.println("6. View My Orders");
        System.out.println("7. Cancel an Order");
        System.out.println("8. [Seller/Admin] Add New Product");
        System.out.println("9. [Seller/Admin] Restock Inventory");
        System.out.println("0. Exit");
        System.out.print("Select an option: ");
    }

    private void viewAllProducts() {
        System.out.println("\n--- Product Catalog ---");
        List<Product> products = productService.getAllProducts();
        if (products.isEmpty()) {
            System.out.println("No products available.");
            return;
        }
        for (Product p : products) {
            System.out.printf("[%s] %-30s | Category: %-12s | Price: ₹%-8s | Stock: %d\n",
                    p.getId(), p.getName(), p.getCategory(), p.getPrice(), p.getStock());
        }
    }

    private void searchProducts() {
        System.out.print("\nEnter product name or keyword: ");
        String keyword = scanner.nextLine();
        List<Product> results = productService.searchProducts(keyword);
        if (results.isEmpty()) {
            System.out.println("No matching products found for: \"" + keyword + "\"");
            return;
        }
        System.out.println("\n--- Search Results ---");
        for (Product p : results) {
            System.out.printf("[%s] %-30s | Price: ₹%-8s | Stock: %d\n",
                    p.getId(), p.getName(), p.getPrice(), p.getStock());
        }
    }

    private void addToCart() {
        System.out.print("\nEnter Product ID: ");
        String productId = scanner.nextLine().trim();
        System.out.print("Enter Quantity: ");
        try {
            int quantity = Integer.parseInt(scanner.nextLine().trim());
            cartService.addItemToCart(currentCustomer.getId(), productId, quantity);
            System.out.println("✓ Added to cart successfully!");
        } catch (NumberFormatException e) {
            System.out.println("⚠️ Quantity must be a valid number.");
        } catch (Exception e) {
            System.out.println("⚠️ Error: " + e.getMessage());
        }
    }

    private void viewCart() {
        ShoppingCart cart = cartService.getCart(currentCustomer.getId());
        System.out.println("\n--- Your Shopping Cart ---");
        if (cart.isEmpty()) {
            System.out.println("Your cart is empty.");
            return;
        }
        for (CartItem item : cart.getItems()) {
            System.out.printf("- %-28s x %d @ ₹%-8s = ₹%s\n",
                    item.getProduct().getName(), item.getQuantity(), item.getProduct().getPrice(), item.getSubtotal());
        }
        System.out.println("----------------------------------------------");
        System.out.println("Cart Total: ₹" + cart.getTotalAmount());
    }

    private void checkout() {
        ShoppingCart cart = cartService.getCart(currentCustomer.getId());
        if (cart.isEmpty()) {
            System.out.println("⚠️ Your cart is empty. Add products before checking out.");
            return;
        }

        viewCart();

        Address deliveryAddr;
        if (currentCustomer.getAddresses().isEmpty()) {
            System.out.print("\nEnter street address: ");
            String street = scanner.nextLine();
            System.out.print("Enter city: ");
            String city = scanner.nextLine();
            System.out.print("Enter state: ");
            String state = scanner.nextLine();
            System.out.print("Enter zipCode: ");
            String zip = scanner.nextLine();
            deliveryAddr = new Address(street, city, state, zip);
            currentCustomer.addAddress(deliveryAddr);
        } else {
            deliveryAddr = currentCustomer.getAddresses().get(0);
            System.out.println("\nShipping to: " + deliveryAddr);
        }

        System.out.print("Enter Coupon Code (or press Enter to skip): ");
        String coupon = scanner.nextLine().trim();
        if (coupon.isBlank()) {
            coupon = null;
        }

        try {
            Order order = orderService.checkout(currentCustomer.getId(), deliveryAddr, coupon);
            System.out.println("\n🎉 Order Placed Successfully!");
            System.out.println("Order ID        : " + order.getOrderId());
            System.out.println("Subtotal        : ₹" + order.getSubtotalAmount());
            System.out.println("Discount Applied: ₹" + order.getDiscountAmount() + (order.getCouponCode() != null ? " (" + order.getCouponCode() + ")" : ""));
            System.out.println("Final Paid      : ₹" + order.getFinalAmount());
            System.out.println("Order Status    : " + order.getStatus());
        } catch (Exception e) {
            System.out.println("⚠️ Checkout failed: " + e.getMessage());
        }
    }

    private void viewMyOrders() {
        List<Order> orders = orderService.getOrdersByCustomer(currentCustomer.getId());
        System.out.println("\n--- My Orders ---");
        if (orders.isEmpty()) {
            System.out.println("You haven't placed any orders yet.");
            return;
        }
        for (Order o : orders) {
            System.out.printf("[%s] Date: %s | Status: %-10s | Total: ₹%s\n",
                    o.getOrderId(), o.getOrderDate().toLocalDate(), o.getStatus(), o.getFinalAmount());
            for (OrderItem item : o.getItems()) {
                System.out.printf("   └─ %s x %d (₹%s)\n", item.getProductName(), item.getQuantity(), item.getSubtotal());
            }
        }
    }

    private void cancelAnOrder() {
        System.out.print("\nEnter Order ID to cancel: ");
        String orderId = scanner.nextLine().trim();
        try {
            orderService.cancelOrder(orderId);
            System.out.println("✓ Order " + orderId + " has been successfully cancelled and stock restored!");
        } catch (Exception e) {
            System.out.println("⚠️ Could not cancel order: " + e.getMessage());
        }
    }

    private void sellerAddProduct() {
        System.out.println("\n--- [Admin/Seller] Add Product ---");
        try {
            System.out.print("Enter Product ID (e.g. P104): ");
            String id = scanner.nextLine().trim();
            System.out.print("Enter Product Name: ");
            String name = scanner.nextLine().trim();

            System.out.println("Select Category: 1. ELECTRONIC  2. FASHION  3. HOME  4. GROCERY  5. BOOKS");
            System.out.print("Choice: ");
            String catChoice = scanner.nextLine().trim();
            Category cat = switch (catChoice) {
                case "1" -> Category.ELECTRONIC;
                case "2" -> Category.FASHION;
                case "3" -> Category.HOME;
                case "4" -> Category.GROCERY;
                case "5" -> Category.BOOKS;
                default -> throw new IllegalArgumentException("Invalid category choice.");
            };

            System.out.print("Enter Seller ID: ");
            String sellerId = scanner.nextLine().trim();
            System.out.print("Enter Price (e.g. 1999.00): ");
            BigDecimal price = new BigDecimal(scanner.nextLine().trim());
            System.out.print("Enter Initial Stock: ");
            int stock = Integer.parseInt(scanner.nextLine().trim());

            Product product = new Product(id, name, cat, sellerId, price, stock);
            productService.addProduct(product);
            System.out.println("✓ Product created and added to catalog!");
        } catch (Exception e) {
            System.out.println("⚠️ Failed to add product: " + e.getMessage());
        }
    }

    private void sellerRestockProduct() {
        System.out.println("\n--- [Admin/Seller] Restock Inventory ---");
        try {
            System.out.print("Enter Product ID: ");
            String id = scanner.nextLine().trim();
            System.out.print("Enter Quantity to Add: ");
            int qty = Integer.parseInt(scanner.nextLine().trim());
            productService.restock(id, qty);
            System.out.println("✓ Product restocked! Current stock: " + productService.getProductById(id).getStock());
        } catch (Exception e) {
            System.out.println("⚠️ Failed to restock: " + e.getMessage());
        }
    }
}
