// Frontend Integration Layer: Bridges React UI with Backend Services / Persistence

const INITIAL_PRODUCTS = [
  {
    id: "P101",
    name: "Sony WH-1000XM5 Headphones",
    category: "ELECTRONIC",
    sellerId: "S101",
    price: 24999.00,
    stock: 10,
    rating: 4.8,
    reviewsCount: 128,
    image: "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=600&auto=format&fit=crop&q=80",
    description: "Industry-leading noise canceling with two processors and eight microphones for unprecedented noise cancellation."
  },
  {
    id: "P102",
    name: "Effective Java (3rd Edition)",
    category: "BOOKS",
    sellerId: "S102",
    price: 850.00,
    stock: 20,
    rating: 4.9,
    reviewsCount: 312,
    image: "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=600&auto=format&fit=crop&q=80",
    description: "The definitive best-practices guide to programming with modern Java by Joshua Bloch."
  },
  {
    id: "P103",
    name: "Keychron K2 Mechanical Keyboard",
    category: "ELECTRONIC",
    sellerId: "S101",
    price: 6499.00,
    stock: 5,
    rating: 4.7,
    reviewsCount: 89,
    image: "https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=600&auto=format&fit=crop&q=80",
    description: "Wireless mechanical keyboard with Gateron switches, Mac/Windows compatibility, and RGB backlighting."
  },
  {
    id: "P104",
    name: "Nike Air Zoom Running Shoes",
    category: "FASHION",
    sellerId: "S103",
    price: 7999.00,
    stock: 8,
    rating: 4.6,
    reviewsCount: 64,
    image: "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=600&auto=format&fit=crop&q=80",
    description: "Lightweight, responsive road running shoes engineered for comfort and maximum speed."
  },
  {
    id: "P105",
    name: "Ergonomic Mesh Office Chair",
    category: "HOME",
    sellerId: "S104",
    price: 12499.00,
    stock: 4,
    rating: 4.5,
    reviewsCount: 42,
    image: "https://images.unsplash.com/photo-1580481077195-c322b28c89c8?w=600&auto=format&fit=crop&q=80",
    description: "High-back executive ergonomic chair with adaptive lumbar support, 3D armrests, and breathable mesh."
  }
];

const INITIAL_COUPONS = [
  { code: "WELCOME10", type: "PERCENTAGE", percentage: 10, minOrder: 1000, maxDiscount: 1500, active: true },
  { code: "MEGA500", type: "FLAT", discountAmount: 500, minOrder: 5000, active: true },
  { code: "FLAT100", type: "FLAT", discountAmount: 100, minOrder: 1000, active: true }
];

export const INITIAL_CUSTOMER = {
  id: "C101",
  name: "Arun Kumar",
  email: "arun@example.com",
  phone: "9876543210",
  addresses: [
    { id: "A1", street: "77 MG Road", city: "Bengaluru", state: "Karnataka", zipCode: "560001" },
    { id: "A2", street: "42 Anna Salai", city: "Chennai", state: "Tamil Nadu", zipCode: "600002" }
  ]
};

// Storage keys
const STORAGE_PRODUCTS = "ecommerce_products";
const STORAGE_ORDERS = "ecommerce_orders";
const STORAGE_COUPONS = "ecommerce_coupons";

const load = (key, fallback) => {
  const data = localStorage.getItem(key);
  return data ? JSON.parse(data) : fallback;
};

const save = (key, data) => {
  localStorage.setItem(key, JSON.stringify(data));
};

export const api = {
  // --- Products ---
  getProducts: () => load(STORAGE_PRODUCTS, INITIAL_PRODUCTS),

  saveProduct: (product) => {
    const products = load(STORAGE_PRODUCTS, INITIAL_PRODUCTS);
    const existingIndex = products.findIndex(p => p.id === product.id);
    if (existingIndex >= 0) {
      products[existingIndex] = product;
    } else {
      products.unshift(product);
    }
    save(STORAGE_PRODUCTS, products);
    return product;
  },

  restockProduct: (productId, quantity) => {
    const products = load(STORAGE_PRODUCTS, INITIAL_PRODUCTS);
    const p = products.find(prod => prod.id === productId);
    if (!p) throw new Error("Product not found");
    p.stock += quantity;
    save(STORAGE_PRODUCTS, products);
    return p;
  },

  // --- Coupons ---
  getCoupons: () => load(STORAGE_COUPONS, INITIAL_COUPONS),

  validateCoupon: (code, subtotal) => {
    if (!code) return { valid: false, discount: 0 };
    const coupons = load(STORAGE_COUPONS, INITIAL_COUPONS);
    const c = coupons.find(coupon => coupon.code.toUpperCase() === code.trim().toUpperCase());
    if (!c || !c.active) {
      return { valid: false, message: "Invalid or inactive coupon code.", discount: 0 };
    }
    if (subtotal < c.minOrder) {
      return { valid: false, message: `Minimum order of ₹${c.minOrder} required.`, discount: 0 };
    }

    let discount = 0;
    if (c.type === "PERCENTAGE") {
      discount = (subtotal * c.percentage) / 100;
      if (c.maxDiscount && discount > c.maxDiscount) {
        discount = c.maxDiscount;
      }
    } else {
      discount = Math.min(c.discountAmount, subtotal);
    }

    return { valid: true, discount, coupon: c };
  },

  // --- Orders ---
  getOrders: () => load(STORAGE_ORDERS, []),

  placeOrder: ({ customerId, items, shippingAddress, couponCode, subtotal, discount, finalAmount }) => {
    const products = load(STORAGE_PRODUCTS, INITIAL_PRODUCTS);

    // 1. Verify stock & reduce inventory
    items.forEach(item => {
      const p = products.find(prod => prod.id === item.productId);
      if (!p || p.stock < item.quantity) {
        throw new Error(`Insufficient stock for ${item.name}`);
      }
      p.stock -= item.quantity;
    });
    save(STORAGE_PRODUCTS, products);

    // 2. Create Order
    const newOrder = {
      orderId: "ORD-" + Math.random().toString(36).substring(2, 9).toUpperCase(),
      customerId,
      orderDate: new Date().toISOString(),
      status: "PLACED",
      items,
      shippingAddress,
      couponCode,
      subtotal,
      discount,
      finalAmount
    };

    const orders = load(STORAGE_ORDERS, []);
    orders.unshift(newOrder);
    save(STORAGE_ORDERS, orders);

    return newOrder;
  },

  updateOrderStatus: (orderId, newStatus) => {
    const orders = load(STORAGE_ORDERS, []);
    const order = orders.find(o => o.orderId === orderId);
    if (!order) throw new Error("Order not found");

    // If cancelled, restore stock
    if (newStatus === "CANCELLED" && order.status !== "CANCELLED") {
      const products = load(STORAGE_PRODUCTS, INITIAL_PRODUCTS);
      order.items.forEach(item => {
        const p = products.find(prod => prod.id === item.productId);
        if (p) p.stock += item.quantity;
      });
      save(STORAGE_PRODUCTS, products);
    }

    order.status = newStatus;
    save(STORAGE_ORDERS, orders);
    return order;
  }
};
