package com.ecommerce.model;

public class Address {

    private final String street;
    private final String city;
    private final String state;
    private final String zipCode;

    public Address(String street, String city, String state, String zipCode) {
        if (street == null || street.isBlank()) {
            throw new IllegalArgumentException("Street cannot be empty");
        }
        if (city == null || city.isBlank()) {
            throw new IllegalArgumentException("City cannot be empty");
        }
        if (state == null || state.isBlank()) {
            throw new IllegalArgumentException("State cannot be empty");
        }
        if (zipCode == null || zipCode.isBlank()) {
            throw new IllegalArgumentException("Zip code cannot be empty");
        }

        this.street = street.trim();
        this.city = city.trim();
        this.state = state.trim();
        this.zipCode = zipCode.trim();
    }

    public String getStreet() {
        return street;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    public String getZipCode() {
        return zipCode;
    }

    @Override
    public String toString() {
        return String.format("%s, %s, %s - %s", street, city, state, zipCode);
    }
}
