package com.ecommerce.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Customer {

    private final String id;
    private final String name;
    private final String email;
    private String phone;
    private final List<Address> addresses;

    public Customer(String id, String name, String email, String phone) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Customer ID cannot be empty");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Customer Name cannot be empty");
        }
        if (email == null || email.isBlank() || !email.contains("@")) {
            throw new IllegalArgumentException("A valid email is required");
        }
        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("Phone number cannot be empty");
        }

        this.id = id.trim();
        this.name = name.trim();
        this.email = email.trim();
        this.phone = phone.trim();
        this.addresses = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            throw new IllegalArgumentException("Phone number cannot be empty");
        }
        this.phone = phone.trim();
    }

    // Encapsulation: controlled way to add an address
    public void addAddress(Address address) {
        if (address == null) {
            throw new IllegalArgumentException("Address cannot be null");
        }
        this.addresses.add(address);
    }

    // Encapsulation: provides read-only access to prevent external tampering
    public List<Address> getAddresses() {
        return Collections.unmodifiableList(addresses);
    }

    @Override
    public String toString() {
        return String.format("Customer[ID=%s, Name=%s, Email=%s, Phone=%s, AddressesCount=%d]",
                id, name, email, phone, addresses.size());
    }
}
