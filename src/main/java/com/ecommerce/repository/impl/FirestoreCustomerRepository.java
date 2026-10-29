package com.ecommerce.repository.impl;

import com.ecommerce.model.Address;
import com.ecommerce.model.Customer;
import com.ecommerce.repository.CustomerRepository;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class FirestoreCustomerRepository implements CustomerRepository {

    private static final String COLLECTION_NAME = "customers";
    private final CollectionReference collection;

    public FirestoreCustomerRepository(Firestore db) {
        if (db == null) {
            throw new IllegalArgumentException("Firestore instance cannot be null");
        }
        this.collection = db.collection(COLLECTION_NAME);
    }

    @Override
    public void save(Customer customer) {
        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("id", customer.getId());
        data.put("name", customer.getName());
        data.put("email", customer.getEmail());
        data.put("phone", customer.getPhone());

        // Serialize addresses into list of maps
        List<Map<String, String>> addressList = new ArrayList<>();
        for (Address addr : customer.getAddresses()) {
            Map<String, String> addrMap = new HashMap<>();
            addrMap.put("street", addr.getStreet());
            addrMap.put("city", addr.getCity());
            addrMap.put("state", addr.getState());
            addrMap.put("zipCode", addr.getZipCode());
            addressList.add(addrMap);
        }
        data.put("addresses", addressList);

        try {
            collection.document(customer.getId()).set(data).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to save customer to Firestore: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<Customer> findById(String id) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        try {
            DocumentSnapshot doc = collection.document(id.trim()).get().get();
            if (!doc.exists()) {
                return Optional.empty();
            }
            return Optional.of(mapToCustomer(doc));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to read customer from Firestore: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Customer> findAll() {
        try {
            List<QueryDocumentSnapshot> docs = collection.get().get().getDocuments();
            List<Customer> list = new ArrayList<>();
            for (QueryDocumentSnapshot doc : docs) {
                list.add(mapToCustomer(doc));
            }
            return list;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to fetch all customers: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteById(String id) {
        if (id == null || id.isBlank()) {
            return false;
        }
        try {
            if (!existsById(id)) {
                return false;
            }
            collection.document(id.trim()).delete().get();
            return true;
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to delete customer: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean existsById(String id) {
        return findById(id).isPresent();
    }

    @Override
    public Optional<Customer> findByEmail(String email) {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }
        try {
            List<QueryDocumentSnapshot> docs = collection.whereEqualTo("email", email.trim().toLowerCase()).get().get().getDocuments();
            if (docs.isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(mapToCustomer(docs.get(0)));
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to query customer by email: " + e.getMessage(), e);
        }
    }

    @SuppressWarnings("unchecked")
    private Customer mapToCustomer(DocumentSnapshot doc) {
        String id = doc.getString("id");
        String name = doc.getString("name");
        String email = doc.getString("email");
        String phone = doc.getString("phone");

        Customer customer = new Customer(id, name, email, phone);

        List<Map<String, String>> addressList = (List<Map<String, String>>) doc.get("addresses");
        if (addressList != null) {
            for (Map<String, String> addrMap : addressList) {
                customer.addAddress(new Address(
                        addrMap.get("street"),
                        addrMap.get("city"),
                        addrMap.get("state"),
                        addrMap.get("zipCode")
                ));
            }
        }

        return customer;
    }
}
