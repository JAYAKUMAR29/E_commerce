package com.ecommerce.model;

import java.math.BigDecimal;

public class Product {
    
    private final String id;
    private final String name;
    private final Category category;
    private final String sellerId;

    private BigDecimal price;
    private int stock;
    private boolean active;

    public Product(String id, String name, Category category, String sellerId, BigDecimal price, int stock)
    {
        if(id == null || id.isBlank())
        {
            throw new IllegalArgumentException("Product ID is required");
        }
        if(name == null || name.isBlank())
        {
            throw new IllegalArgumentException("Product Name is required");
        }
        if(category == null)
        {
            throw new IllegalArgumentException("Category is required");
        }
        if(sellerId == null || sellerId.isBlank())
        {
            throw new IllegalArgumentException("SellerID is required");
        }
        if(price == null || price.compareTo(BigDecimal.ZERO)<=0)
        {
            throw new IllegalArgumentException("Price is required");
        }
        if(stock < 0)
        {
            throw new IllegalArgumentException("Stock is required");
        }
        this.id = id;
        this.name = name;
        this.category = category;
        this.sellerId = sellerId;
        this.price = price;
        this.stock = stock;
        this.active = true;
    }
    public String getId()
    {
        return id;
    }
    public String getName()
    {
        return name;
    }
    public Category getCategory()
    {
        return category;
    }
    public String getSellerId()
    {
        return sellerId;
    }
    public BigDecimal getPrice()
    {
        return price;
    }
    public int getStock()
    {
        return stock;
    }
    public boolean isActive()
    {
        return active;
    }
    public boolean isOutofStock()
    {
        return stock == 0;
    }

    public void reduceStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity to reduce must be greater than zero");
        }
        if (quantity > this.stock) {
            throw new IllegalStateException("Insufficient stock. Available: " + this.stock + ", requested: " + quantity);
        }
        this.stock -= quantity;
    }

    public void increaseStock(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity to add must be greater than zero");
        }
        this.stock += quantity;
    }

    public void updatePrice(BigDecimal newPrice) {
        if (newPrice == null || newPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Price must be greater than zero");
        }
        this.price = newPrice;
    }

    @Override
    public String toString() {
        return String.format("Product[ID=%s, Name=%s, Category=%s, Price=%s, Stock=%d, Active=%b]",
                id, name, category, price, stock, active);
    }
}
