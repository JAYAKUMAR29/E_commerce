package com.ecommerce.service;

import com.ecommerce.model.Address;
import com.ecommerce.model.CartItem;
import com.ecommerce.model.Coupon;
import com.ecommerce.model.Order;
import com.ecommerce.model.OrderItem;
import com.ecommerce.model.OrderStatus;
import com.ecommerce.model.Product;
import com.ecommerce.model.ShoppingCart;
import com.ecommerce.repository.CouponRepository;
import com.ecommerce.repository.CustomerRepository;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final CouponRepository couponRepository;
    private final CartService cartService;

    public OrderService(OrderRepository orderRepository,
                        ProductRepository productRepository,
                        CustomerRepository customerRepository,
                        CouponRepository couponRepository,
                        CartService cartService) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.couponRepository = couponRepository;
        this.cartService = cartService;
    }

    // Checkout: converts customer's active cart into a confirmed order
    public synchronized Order checkout(String customerId, Address shippingAddress, String couponCode) {
        if (!customerRepository.existsById(customerId)) {
            throw new NoSuchElementException("Customer not found: " + customerId);
        }

        ShoppingCart cart = cartService.getCart(customerId);
        if (cart.isEmpty()) {
            throw new IllegalStateException("Cannot checkout: Shopping cart is empty.");
        }

        // 1. Validate that all items still have enough stock in repository
        for (CartItem item : cart.getItems()) {
            Product currentProduct = productRepository.findById(item.getProduct().getId())
                    .orElseThrow(() -> new NoSuchElementException("Product no longer available: " + item.getProduct().getId()));
            if (currentProduct.getStock() < item.getQuantity()) {
                throw new IllegalStateException("Insufficient stock for " + currentProduct.getName() +
                        ". Available: " + currentProduct.getStock() + ", in cart: " + item.getQuantity());
            }
        }

        // 2. Deduct inventory and build OrderItem snapshots
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem item : cart.getItems()) {
            Product product = productRepository.findById(item.getProduct().getId()).get();
            product.reduceStock(item.getQuantity());
            productRepository.save(product);

            orderItems.add(new OrderItem(
                    product.getId(),
                    product.getName(),
                    product.getPrice(),
                    item.getQuantity()
            ));
        }

        // 3. Resolve Coupon if provided
        Coupon coupon = null;
        if (couponCode != null && !couponCode.isBlank()) {
            coupon = couponRepository.findByCode(couponCode)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid coupon code: " + couponCode));
        }

        // 4. Generate order ID and construct Order
        String orderId = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Order order = new Order(orderId, customerId, shippingAddress, orderItems, coupon);

        // 5. Persist order and clear cart
        orderRepository.save(order);
        cartService.clearCart(customerId);

        return order;
    }

    public Order getOrderById(String orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("Order not found with ID: " + orderId));
    }

    public List<Order> getOrdersByCustomer(String customerId) {
        return orderRepository.findByCustomerId(customerId);
    }

    // Cancel order and restore product stock back to repository
    public synchronized void cancelOrder(String orderId) {
        Order order = getOrderById(orderId);
        order.cancel(); // Validates status transition internally

        // Restore inventory for each item
        for (OrderItem item : order.getItems()) {
            productRepository.findById(item.getProductId()).ifPresent(product -> {
                product.increaseStock(item.getQuantity());
                productRepository.save(product);
            });
        }

        orderRepository.save(order);
    }

    public void updateOrderStatus(String orderId, OrderStatus newStatus) {
        Order order = getOrderById(orderId);
        switch (newStatus) {
            case CONFIRMED -> order.confirm();
            case SHIPPED -> order.markShipped();
            case DELIVERED -> order.markDelivered();
            case CANCELLED -> cancelOrder(orderId);
            case RETURN_REQUESTED -> order.requestReturn();
            case RETURNED -> order.completeReturn();
            default -> throw new IllegalArgumentException("Unsupported status transition: " + newStatus);
        }
        orderRepository.save(order);
    }
}
