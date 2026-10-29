package com.ecommerce;

import com.ecommerce.cli.EcommerceCLI;
import com.ecommerce.config.FirebaseConfig;
import com.ecommerce.model.Address;
import com.ecommerce.model.Category;
import com.ecommerce.model.Customer;
import com.ecommerce.model.FlatDiscountCoupon;
import com.ecommerce.model.PercentageCoupon;
import com.ecommerce.model.Product;
import com.ecommerce.repository.CouponRepository;
import com.ecommerce.repository.CustomerRepository;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.impl.FirestoreCouponRepository;
import com.ecommerce.repository.impl.FirestoreCustomerRepository;
import com.ecommerce.repository.impl.FirestoreOrderRepository;
import com.ecommerce.repository.impl.FirestoreProductRepository;
import com.ecommerce.repository.impl.InMemoryCouponRepository;
import com.ecommerce.repository.impl.InMemoryCustomerRepository;
import com.ecommerce.repository.impl.InMemoryOrderRepository;
import com.ecommerce.repository.impl.InMemoryProductRepository;
import com.ecommerce.service.CartService;
import com.ecommerce.service.OrderService;
import com.ecommerce.service.ProductService;
import com.google.cloud.firestore.Firestore;

import java.math.BigDecimal;

public class Main {

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("      APEX COMMERCE ENGINE - INITIALIZING        ");
        System.out.println("=================================================");

        // 1. Attempt to connect to Google Cloud Firestore (Phase 2)
        boolean firebaseConnected = FirebaseConfig.initialize("src/main/resources/firebase-key.json");

        ProductRepository productRepo;
        CustomerRepository customerRepo;
        OrderRepository orderRepo;
        CouponRepository couponRepo;

        if (firebaseConnected) {
            System.out.println("🔥 STORAGE MODE: Google Cloud Firestore Database");
            Firestore db = FirebaseConfig.getFirestore();
            productRepo = new FirestoreProductRepository(db);
            customerRepo = new FirestoreCustomerRepository(db);
            orderRepo = new FirestoreOrderRepository(db);
            couponRepo = new FirestoreCouponRepository(db);
        } else {
            System.out.println("💾 STORAGE MODE: In-Memory Storage (Fallback / Local Dev)");
            productRepo = new InMemoryProductRepository();
            customerRepo = new InMemoryCustomerRepository();
            orderRepo = new InMemoryOrderRepository();
            couponRepo = new InMemoryCouponRepository();
        }

        // 2. Initialize Services (Identical for both In-Memory and Firebase!)
        ProductService productService = new ProductService(productRepo);
        CartService cartService = new CartService(productRepo);
        OrderService orderService = new OrderService(orderRepo, productRepo, customerRepo, couponRepo, cartService);

        // 3. Seed Initial Catalog (if empty)
        if (productRepo.findAll().isEmpty()) {
            System.out.println("🌱 Seeding initial products...");
            productService.addProduct(new Product("P101", "Sony WH-1000XM5 Headphones", Category.ELECTRONIC, "S101", new BigDecimal("24999.00"), 10));
            productService.addProduct(new Product("P102", "Effective Java (3rd Edition)", Category.BOOKS, "S102", new BigDecimal("850.00"), 20));
            productService.addProduct(new Product("P103", "Keychron K2 Mechanical Keyboard", Category.ELECTRONIC, "S101", new BigDecimal("6499.00"), 5));
            productService.addProduct(new Product("P104", "Nike Air Zoom Running Shoes", Category.FASHION, "S103", new BigDecimal("7999.00"), 8));
            productService.addProduct(new Product("P105", "Ergonomic Office Chair", Category.HOME, "S104", new BigDecimal("12499.00"), 4));
        }

        // 4. Seed Default Customer (if not exists)
        Customer defaultCustomer;
        if (customerRepo.findById("C101").isEmpty()) {
            defaultCustomer = new Customer("C101", "Arun Kumar", "arun@example.com", "9876543210");
            defaultCustomer.addAddress(new Address("77 MG Road", "Bengaluru", "Karnataka", "560001"));
            customerRepo.save(defaultCustomer);
        } else {
            defaultCustomer = customerRepo.findById("C101").get();
        }

        // 5. Seed Discount Coupons (if empty)
        if (couponRepo.findAll().isEmpty()) {
            couponRepo.save(new PercentageCoupon("WELCOME10", new BigDecimal("1000.00"), 10.0, new BigDecimal("1500.00")));
            couponRepo.save(new FlatDiscountCoupon("MEGA500", new BigDecimal("5000.00"), new BigDecimal("500.00")));
            couponRepo.save(new FlatDiscountCoupon("FLAT100", new BigDecimal("1000.00"), new BigDecimal("100.00")));
        }

        System.out.println("=================================================\n");

        // 6. Launch CLI Presentation Layer
        EcommerceCLI cli = new EcommerceCLI(productService, cartService, orderService, defaultCustomer);
        cli.start();
    }
}
