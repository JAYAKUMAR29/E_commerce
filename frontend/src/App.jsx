import React, { useState, useEffect } from 'react';
import { api, INITIAL_CUSTOMER } from './services/api';
import { 
  ShoppingBag, 
  Search, 
  ShoppingCart, 
  Package, 
  ShieldCheck, 
  Plus, 
  Trash2, 
  CheckCircle, 
  Truck, 
  Star, 
  Clock, 
  X, 
  ArrowRight,
  RefreshCw,
  Tag
} from 'lucide-react';

export default function App() {
  // State
  const [view, setView] = useState('shop'); // 'shop' | 'orders' | 'admin'
  const [products, setProducts] = useState([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('ALL');
  const [cart, setCart] = useState([]);
  const [isCartOpen, setIsCartOpen] = useState(false);
  const [orders, setOrders] = useState([]);

  // Coupon state
  const [couponCode, setCouponCode] = useState('');
  const [appliedCoupon, setAppliedCoupon] = useState(null);
  const [couponError, setCouponError] = useState('');

  // Selected Product for Details Modal
  const [selectedProduct, setSelectedProduct] = useState(null);

  // Checkout Modal
  const [isCheckoutOpen, setIsCheckoutOpen] = useState(false);
  const [selectedAddress, setSelectedAddress] = useState(INITIAL_CUSTOMER.addresses[0]);
  const [paymentMethod, setPaymentMethod] = useState('UPI');

  // Admin New Product Form
  const [newProduct, setNewProduct] = useState({
    id: '',
    name: '',
    category: 'ELECTRONIC',
    sellerId: 'S101',
    price: '',
    stock: '',
    description: '',
    image: ''
  });

  // Load initial data
  useEffect(() => {
    refreshData();
  }, []);

  const refreshData = () => {
    setProducts(api.getProducts());
    setOrders(api.getOrders());
  };

  // Cart operations
  const addToCart = (product, qty = 1) => {
    setCart(prev => {
      const existing = prev.find(item => item.productId === product.id);
      if (existing) {
        if (existing.quantity + qty > product.stock) {
          alert(`Cannot add more than available stock (${product.stock})`);
          return prev;
        }
        return prev.map(item => item.productId === product.id 
          ? { ...item, quantity: item.quantity + qty } 
          : item
        );
      }
      return [...prev, {
        productId: product.id,
        name: product.name,
        price: product.price,
        image: product.image,
        quantity: qty,
        maxStock: product.stock
      }];
    });
    setIsCartOpen(true);
  };

  const updateCartQty = (productId, delta) => {
    setCart(prev => prev.map(item => {
      if (item.productId === productId) {
        const newQty = item.quantity + delta;
        if (newQty <= 0) return null;
        if (newQty > item.maxStock) {
          alert("Exceeds available stock");
          return item;
        }
        return { ...item, quantity: newQty };
      }
      return item;
    }).filter(Boolean));
  };

  const removeFromCart = (productId) => {
    setCart(prev => prev.filter(item => item.productId !== productId));
  };

  // Calculations
  const cartSubtotal = cart.reduce((sum, item) => sum + (item.price * item.quantity), 0);
  const cartDiscount = appliedCoupon ? appliedCoupon.discount : 0;
  const cartFinalTotal = Math.max(0, cartSubtotal - cartDiscount);

  // Apply Coupon
  const handleApplyCoupon = (e) => {
    e.preventDefault();
    setCouponError('');
    const result = api.validateCoupon(couponCode, cartSubtotal);
    if (result.valid) {
      setAppliedCoupon(result);
    } else {
      setAppliedCoupon(null);
      setCouponError(result.message || 'Invalid coupon code');
    }
  };

  // Place Order
  const handlePlaceOrder = () => {
    try {
      const newOrder = api.placeOrder({
        customerId: INITIAL_CUSTOMER.id,
        items: cart,
        shippingAddress: selectedAddress,
        couponCode: appliedCoupon?.coupon?.code || null,
        subtotal: cartSubtotal,
        discount: cartDiscount,
        finalAmount: cartFinalTotal
      });

      setCart([]);
      setAppliedCoupon(null);
      setCouponCode('');
      setIsCheckoutOpen(false);
      setIsCartOpen(false);
      refreshData();
      setView('orders');
      alert(`🎉 Order placed successfully! Order ID: ${newOrder.orderId}`);
    } catch (err) {
      alert("⚠️ Checkout failed: " + err.message);
    }
  };

  // Filter products
  const filteredProducts = products.filter(p => {
    const matchesCategory = selectedCategory === 'ALL' || p.category === selectedCategory;
    const matchesSearch = p.name.toLowerCase().includes(searchQuery.toLowerCase());
    return matchesCategory && matchesSearch;
  });

  return (
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column' }}>
      
      {/* 1. TOP NAVIGATION */}
      <header className="glass-panel" style={{
        position: 'sticky', top: 0, zIndex: 50, borderRadius: 0, borderTop: 'none', borderLeft: 'none', borderRight: 'none',
        padding: '14px 28px', display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: '20px'
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px', cursor: 'pointer' }} onClick={() => setView('shop')}>
          <div style={{ width: '40px', height: '40px', borderRadius: '10px', background: 'var(--accent-gradient)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <ShoppingBag size={22} color="white" />
          </div>
          <div>
            <h2 style={{ fontSize: '1.25rem', fontWeight: 800 }}>APEX<span className="gradient-text">COMMERCE</span></h2>
            <span style={{ fontSize: '0.7rem', color: 'var(--text-dim)', letterSpacing: '0.05em' }}>CORE JAVA • FIRESTORE • REACT</span>
          </div>
        </div>

        {/* Search Bar */}
        {view === 'shop' && (
          <div style={{ position: 'relative', flex: 1, maxWidth: '460px' }}>
            <Search size={18} style={{ position: 'absolute', left: '14px', top: '50%', transform: 'translateY(-50%)', color: 'var(--text-dim)' }} />
            <input 
              type="text" 
              className="input-field" 
              style={{ paddingLeft: '42px', borderRadius: 'var(--radius-full)' }}
              placeholder="Search products by name or keywords..." 
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
            />
          </div>
        )}

        {/* View Switcher & Cart */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '14px' }}>
          <button 
            className={view === 'shop' ? 'gradient-btn' : 'btn-secondary'}
            onClick={() => setView('shop')}
          >
            Catalog
          </button>
          
          <button 
            className={view === 'orders' ? 'gradient-btn' : 'btn-secondary'}
            onClick={() => { setView('orders'); refreshData(); }}
          >
            <Package size={18} />
            My Orders ({orders.length})
          </button>

          <button 
            className={view === 'admin' ? 'gradient-btn' : 'btn-secondary'}
            onClick={() => setView('admin')}
          >
            <ShieldCheck size={18} />
            Seller Portal
          </button>

          {/* Cart Trigger */}
          <button 
            className="btn-secondary" 
            style={{ position: 'relative', padding: '10px 16px' }}
            onClick={() => setIsCartOpen(true)}
          >
            <ShoppingCart size={20} />
            {cart.length > 0 && (
              <span style={{
                position: 'absolute', top: '-6px', right: '-6px', background: '#ec4899', color: 'white',
                borderRadius: '50%', width: '22px', height: '22px', display: 'flex', alignItems: 'center', justifyContent: 'center',
                fontSize: '0.75rem', fontWeight: 800, boxShadow: '0 0 10px #ec4899'
              }}>
                {cart.reduce((sum, i) => sum + i.quantity, 0)}
              </span>
            )}
          </button>
        </div>
      </header>

      {/* 2. BODY CONTENT */}
      <main style={{ flex: 1, padding: '32px 28px', maxWidth: '1400px', margin: '0 auto', width: '100%' }}>
        
        {/* VIEW 1: SHOPPING CATALOG */}
        {view === 'shop' && (
          <div className="animate-fade-in">
            {/* Category Filter Pills */}
            <div style={{ display: 'flex', gap: '10px', overflowX: 'auto', paddingBottom: '16px', marginBottom: '24px' }}>
              {['ALL', 'ELECTRONIC', 'FASHION', 'HOME', 'BOOKS', 'GROCERY'].map(cat => (
                <button
                  key={cat}
                  onClick={() => setSelectedCategory(cat)}
                  style={{
                    padding: '8px 18px',
                    borderRadius: 'var(--radius-full)',
                    border: '1px solid ' + (selectedCategory === cat ? 'var(--accent-primary)' : 'var(--border-subtle)'),
                    background: selectedCategory === cat ? 'var(--accent-primary)' : 'rgba(255, 255, 255, 0.04)',
                    color: 'white',
                    fontWeight: 600,
                    fontSize: '0.85rem',
                    cursor: 'pointer',
                    transition: 'all 0.2s ease'
                  }}
                >
                  {cat}
                </button>
              ))}
            </div>

            {/* Products Grid */}
            <div style={{
              display: 'grid',
              gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))',
              gap: '24px'
            }}>
              {filteredProducts.map(product => (
                <div 
                  key={product.id} 
                  className="glass-panel"
                  style={{
                    display: 'flex', flexDirection: 'column', overflow: 'hidden',
                    transition: 'transform 0.25s ease, border-color 0.25s ease',
                    cursor: 'pointer'
                  }}
                  onMouseEnter={(e) => e.currentTarget.style.transform = 'translateY(-4px)'}
                  onMouseLeave={(e) => e.currentTarget.style.transform = 'translateY(0)'}
                >
                  {/* Image banner */}
                  <div 
                    style={{ position: 'relative', height: '200px', background: '#1e293b' }}
                    onClick={() => setSelectedProduct(product)}
                  >
                    <img 
                      src={product.image} 
                      alt={product.name}
                      style={{ width: '100%', height: '100%', objectFit: 'cover' }}
                    />
                    <span className={`badge badge-${product.category.toLowerCase()}`} style={{ position: 'absolute', top: '12px', left: '12px' }}>
                      {product.category}
                    </span>
                    <span style={{
                      position: 'absolute', bottom: '12px', right: '12px', background: 'rgba(0,0,0,0.7)',
                      padding: '4px 8px', borderRadius: '6px', fontSize: '0.75rem', display: 'flex', alignItems: 'center', gap: '4px'
                    }}>
                      <Star size={13} color="#fbbf24" fill="#fbbf24" /> {product.rating} ({product.reviewsCount})
                    </span>
                  </div>

                  {/* Body */}
                  <div style={{ padding: '18px', display: 'flex', flexDirection: 'column', flex: 1, justifyContent: 'space-between' }}>
                    <div>
                      <h3 style={{ fontSize: '1.05rem', marginBottom: '8px', fontWeight: 600 }}>{product.name}</h3>
                      <p style={{ color: 'var(--text-muted)', fontSize: '0.85rem', lineHeight: '1.4', marginBottom: '16px' }}>
                        {product.description.substring(0, 75)}...
                      </p>
                    </div>

                    <div>
                      <div style={{ display: 'flex', alignItems: 'baseline', justifyContent: 'space-between', marginBottom: '14px' }}>
                        <div>
                          <span style={{ fontSize: '1.35rem', fontWeight: 800 }}>₹{product.price.toLocaleString()}</span>
                        </div>
                        <span style={{ fontSize: '0.8rem', color: product.stock > 0 ? 'var(--success)' : 'var(--danger)', fontWeight: 600 }}>
                          {product.stock > 0 ? `${product.stock} in stock` : 'Out of stock'}
                        </span>
                      </div>

                      <button 
                        className="gradient-btn"
                        style={{ width: '100%', justifyContent: 'center' }}
                        disabled={product.stock <= 0}
                        onClick={() => addToCart(product)}
                      >
                        <ShoppingCart size={16} />
                        Add to Cart
                      </button>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* VIEW 2: ORDERS TRACKING */}
        {view === 'orders' && (
          <div className="animate-fade-in" style={{ maxWidth: '900px', margin: '0 auto' }}>
            <h2 style={{ fontSize: '1.75rem', marginBottom: '24px' }}>My Orders & Tracking</h2>
            
            {orders.length === 0 ? (
              <div className="glass-panel" style={{ padding: '48px', textAlign: 'center' }}>
                <Package size={48} color="var(--text-dim)" style={{ marginBottom: '16px' }} />
                <h3>No Orders Placed Yet</h3>
                <p style={{ color: 'var(--text-muted)', marginTop: '8px', marginBottom: '20px' }}>Your completed purchases and delivery timelines will appear here.</p>
                <button className="gradient-btn" onClick={() => setView('shop')}>Browse Products</button>
              </div>
            ) : (
              orders.map(order => (
                <div key={order.orderId} className="glass-panel" style={{ padding: '24px', marginBottom: '20px' }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', borderBottom: '1px solid var(--border-subtle)', paddingBottom: '16px', marginBottom: '20px' }}>
                    <div>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                        <span style={{ fontWeight: 800, fontSize: '1.1rem' }}>{order.orderId}</span>
                        <span className={`badge ${order.status === 'DELIVERED' ? 'badge-success' : order.status === 'CANCELLED' ? 'badge-danger' : 'badge-warning'}`}>
                          {order.status}
                        </span>
                      </div>
                      <span style={{ fontSize: '0.8rem', color: 'var(--text-dim)', marginTop: '4px', display: 'block' }}>
                        Ordered on {new Date(order.orderDate).toLocaleDateString()} at {new Date(order.orderDate).toLocaleTimeString()}
                      </span>
                    </div>

                    <div style={{ textAlign: 'right' }}>
                      <span style={{ fontSize: '1.3rem', fontWeight: 800 }}>₹{order.finalAmount.toLocaleString()}</span>
                      {order.couponCode && (
                        <span style={{ display: 'block', fontSize: '0.75rem', color: 'var(--success)' }}>
                          Coupon {order.couponCode} applied (-₹{order.discount})
                        </span>
                      )}
                    </div>
                  </div>

                  {/* Status Timeline */}
                  <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', margin: '20px 0', position: 'relative' }}>
                    {['PLACED', 'CONFIRMED', 'SHIPPED', 'DELIVERED'].map((step, idx) => {
                      const isComplete = ['PLACED', 'CONFIRMED', 'SHIPPED', 'DELIVERED'].indexOf(order.status) >= idx;
                      const isCurrent = order.status === step;
                      return (
                        <div key={step} style={{ display: 'flex', flexDirection: 'column', alignItems: 'center', zIndex: 1 }}>
                          <div style={{
                            width: '32px', height: '32px', borderRadius: '50%',
                            background: isComplete ? 'var(--accent-primary)' : 'rgba(255,255,255,0.08)',
                            display: 'flex', alignItems: 'center', justifyContent: 'center',
                            border: isCurrent ? '2px solid white' : 'none',
                            boxShadow: isComplete ? '0 0 12px var(--accent-glow)' : 'none'
                          }}>
                            {isComplete ? <CheckCircle size={18} color="white" /> : <Clock size={16} color="var(--text-dim)" />}
                          </div>
                          <span style={{ fontSize: '0.75rem', marginTop: '6px', fontWeight: 600, color: isComplete ? 'white' : 'var(--text-dim)' }}>
                            {step}
                          </span>
                        </div>
                      );
                    })}
                  </div>

                  {/* Line Items */}
                  <div style={{ background: 'rgba(0,0,0,0.2)', padding: '16px', borderRadius: 'var(--radius-sm)', marginBottom: '16px' }}>
                    <h4 style={{ fontSize: '0.85rem', color: 'var(--text-dim)', marginBottom: '10px', textTransform: 'uppercase' }}>Items in this order</h4>
                    {order.items.map(item => (
                      <div key={item.productId} style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.9rem', marginBottom: '6px' }}>
                        <span>{item.name} <strong style={{ color: 'var(--accent-primary)' }}>× {item.quantity}</strong></span>
                        <span>₹{(item.price * item.quantity).toLocaleString()}</span>
                      </div>
                    ))}
                  </div>

                  {/* Action Buttons */}
                  <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '10px' }}>
                    {order.status !== 'CANCELLED' && order.status !== 'DELIVERED' && (
                      <button 
                        className="btn-secondary"
                        style={{ color: 'var(--danger)', borderColor: 'rgba(239, 68, 68, 0.3)' }}
                        onClick={() => {
                          if (confirm(`Cancel order ${order.orderId} and restore product inventory?`)) {
                            api.updateOrderStatus(order.orderId, 'CANCELLED');
                            refreshData();
                          }
                        }}
                      >
                        Cancel Order & Restore Stock
                      </button>
                    )}
                  </div>
                </div>
              ))
            )}
          </div>
        )}

        {/* VIEW 3: SELLER / ADMIN DASHBOARD */}
        {view === 'admin' && (
          <div className="animate-fade-in">
            <h2 style={{ fontSize: '1.75rem', marginBottom: '24px' }}>Seller & Inventory Control Center</h2>
            
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '24px' }}>
              
              {/* Form: Add New Product */}
              <div className="glass-panel" style={{ padding: '24px' }}>
                <h3 style={{ fontSize: '1.2rem', marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '8px' }}>
                  <Plus size={20} color="var(--accent-primary)" /> Add New Catalog Product
                </h3>
                
                <form onSubmit={(e) => {
                  e.preventDefault();
                  api.saveProduct({
                    id: newProduct.id || 'P' + Math.floor(100 + Math.random() * 900),
                    name: newProduct.name,
                    category: newProduct.category,
                    sellerId: 'S101',
                    price: parseFloat(newProduct.price),
                    stock: parseInt(newProduct.stock),
                    rating: 5.0,
                    reviewsCount: 1,
                    image: newProduct.image || 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=600&auto=format&fit=crop&q=80',
                    description: newProduct.description || 'Premium retail product.'
                  });
                  alert('✓ Product added successfully!');
                  setNewProduct({ id: '', name: '', category: 'ELECTRONIC', sellerId: 'S101', price: '', stock: '', description: '', image: '' });
                  refreshData();
                }}>
                  <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
                    <input 
                      type="text" 
                      placeholder="Product ID (e.g. P106)" 
                      className="input-field" 
                      required 
                      value={newProduct.id} 
                      onChange={e => setNewProduct({...newProduct, id: e.target.value})} 
                    />
                    <input 
                      type="text" 
                      placeholder="Product Name" 
                      className="input-field" 
                      required 
                      value={newProduct.name} 
                      onChange={e => setNewProduct({...newProduct, name: e.target.value})} 
                    />
                    <select 
                      className="input-field" 
                      value={newProduct.category} 
                      onChange={e => setNewProduct({...newProduct, category: e.target.value})}
                    >
                      <option value="ELECTRONIC">ELECTRONIC</option>
                      <option value="FASHION">FASHION</option>
                      <option value="HOME">HOME</option>
                      <option value="BOOKS">BOOKS</option>
                      <option value="GROCERY">GROCERY</option>
                    </select>
                    <input 
                      type="number" 
                      placeholder="Price (₹)" 
                      className="input-field" 
                      required 
                      value={newProduct.price} 
                      onChange={e => setNewProduct({...newProduct, price: e.target.value})} 
                    />
                    <input 
                      type="number" 
                      placeholder="Stock Inventory Units" 
                      className="input-field" 
                      required 
                      value={newProduct.stock} 
                      onChange={e => setNewProduct({...newProduct, stock: e.target.value})} 
                    />
                    <input 
                      type="url" 
                      placeholder="Image URL (Unsplash/Direct link)" 
                      className="input-field" 
                      value={newProduct.image} 
                      onChange={e => setNewProduct({...newProduct, image: e.target.value})} 
                    />
                    <textarea 
                      placeholder="Product Description" 
                      className="input-field" 
                      rows={3} 
                      value={newProduct.description} 
                      onChange={e => setNewProduct({...newProduct, description: e.target.value})} 
                    />
                    
                    <button type="submit" className="gradient-btn" style={{ justifyContent: 'center' }}>
                      Publish Product
                    </button>
                  </div>
                </form>
              </div>

              {/* Inventory Management Table */}
              <div className="glass-panel" style={{ padding: '24px' }}>
                <h3 style={{ fontSize: '1.2rem', marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '8px' }}>
                  <RefreshCw size={20} color="var(--accent-primary)" /> Inventory Restock Management
                </h3>

                <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                  {products.map(p => (
                    <div key={p.id} style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', padding: '12px', background: 'rgba(0,0,0,0.2)', borderRadius: 'var(--radius-sm)' }}>
                      <div>
                        <div style={{ fontWeight: 600 }}>{p.name}</div>
                        <span style={{ fontSize: '0.8rem', color: 'var(--text-dim)' }}>ID: {p.id} | Stock: {p.stock} units</span>
                      </div>

                      <div style={{ display: 'flex', gap: '8px' }}>
                        <button 
                          className="btn-secondary" 
                          style={{ padding: '6px 12px', fontSize: '0.8rem' }}
                          onClick={() => {
                            const add = prompt(`Add units to ${p.name}:`, '5');
                            if (add && !isNaN(add)) {
                              api.restockProduct(p.id, parseInt(add));
                              refreshData();
                            }
                          }}
                        >
                          + Restock
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              </div>

            </div>
          </div>
        )}

      </main>

      {/* 3. CART SLIDE-OUT DRAWER */}
      {isCartOpen && (
        <div style={{ position: 'fixed', inset: 0, zIndex: 100, display: 'flex', justifyContent: 'flex-end', background: 'rgba(0,0,0,0.6)', backdropFilter: 'blur(4px)' }}>
          <div className="glass-panel animate-fade-in" style={{ width: '100%', maxWidth: '420px', height: '100%', borderRadius: 0, display: 'flex', flexDirection: 'column', padding: '24px' }}>
            
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', borderBottom: '1px solid var(--border-subtle)', paddingBottom: '16px' }}>
              <h3 style={{ fontSize: '1.2rem', display: 'flex', alignItems: 'center', gap: '8px' }}>
                <ShoppingCart size={20} /> Your Shopping Cart ({cart.reduce((s, i) => s + i.quantity, 0)})
              </h3>
              <button className="btn-secondary" style={{ padding: '6px', borderRadius: '50%' }} onClick={() => setIsCartOpen(false)}>
                <X size={18} />
              </button>
            </div>

            {/* Items List */}
            <div style={{ flex: 1, overflowY: 'auto', padding: '16px 0', display: 'flex', flexDirection: 'column', gap: '14px' }}>
              {cart.length === 0 ? (
                <div style={{ textAlign: 'center', marginTop: '60px', color: 'var(--text-muted)' }}>
                  <ShoppingBag size={48} color="var(--text-dim)" style={{ marginBottom: '12px' }} />
                  <p>Your cart is empty.</p>
                </div>
              ) : (
                cart.map(item => (
                  <div key={item.productId} style={{ display: 'flex', gap: '12px', alignItems: 'center', padding: '12px', background: 'rgba(0,0,0,0.2)', borderRadius: 'var(--radius-sm)' }}>
                    <img src={item.image} alt={item.name} style={{ width: '56px', height: '56px', borderRadius: '6px', objectFit: 'cover' }} />
                    <div style={{ flex: 1 }}>
                      <div style={{ fontWeight: 600, fontSize: '0.9rem' }}>{item.name}</div>
                      <div style={{ fontSize: '0.85rem', color: 'var(--accent-primary)', fontWeight: 700 }}>₹{item.price.toLocaleString()}</div>
                      
                      {/* Quantity Stepper */}
                      <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginTop: '6px' }}>
                        <button className="btn-secondary" style={{ padding: '2px 8px' }} onClick={() => updateCartQty(item.productId, -1)}>-</button>
                        <span style={{ fontSize: '0.85rem', fontWeight: 700 }}>{item.quantity}</span>
                        <button className="btn-secondary" style={{ padding: '2px 8px' }} onClick={() => updateCartQty(item.productId, 1)}>+</button>
                      </div>
                    </div>
                    <button className="btn-secondary" style={{ padding: '6px', color: 'var(--danger)' }} onClick={() => removeFromCart(item.productId)}>
                      <Trash2 size={16} />
                    </button>
                  </div>
                ))
              )}
            </div>

            {/* Cart Footer */}
            {cart.length > 0 && (
              <div style={{ borderTop: '1px solid var(--border-subtle)', paddingTop: '16px' }}>
                
                {/* Coupon Box */}
                <form onSubmit={handleApplyCoupon} style={{ display: 'flex', gap: '8px', marginBottom: '12px' }}>
                  <input 
                    type="text" 
                    placeholder="Enter Coupon (e.g. WELCOME10)" 
                    className="input-field" 
                    value={couponCode} 
                    onChange={e => setCouponCode(e.target.value.toUpperCase())}
                  />
                  <button type="submit" className="btn-secondary">Apply</button>
                </form>
                {appliedCoupon && (
                  <div style={{ fontSize: '0.8rem', color: 'var(--success)', marginBottom: '8px' }}>
                    ✓ {appliedCoupon.coupon.code} applied (-₹{appliedCoupon.discount})
                  </div>
                )}
                {couponError && (
                  <div style={{ fontSize: '0.8rem', color: 'var(--danger)', marginBottom: '8px' }}>
                    ⚠️ {couponError}
                  </div>
                )}

                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '6px', fontSize: '0.9rem', color: 'var(--text-muted)' }}>
                  <span>Subtotal</span>
                  <span>₹{cartSubtotal.toLocaleString()}</span>
                </div>
                {cartDiscount > 0 && (
                  <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '6px', fontSize: '0.9rem', color: 'var(--success)' }}>
                    <span>Discount</span>
                    <span>-₹{cartDiscount.toLocaleString()}</span>
                  </div>
                )}
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '16px', fontSize: '1.2rem', fontWeight: 800 }}>
                  <span>Final Total</span>
                  <span>₹{cartFinalTotal.toLocaleString()}</span>
                </div>

                <button 
                  className="gradient-btn" 
                  style={{ width: '100%', justifyContent: 'center', padding: '14px' }}
                  onClick={() => setIsCheckoutOpen(true)}
                >
                  Proceed to Checkout <ArrowRight size={18} />
                </button>
              </div>
            )}

          </div>
        </div>
      )}

      {/* 4. CHECKOUT MODAL */}
      {isCheckoutOpen && (
        <div style={{ position: 'fixed', inset: 0, zIndex: 110, display: 'flex', alignItems: 'center', justifyContent: 'center', background: 'rgba(0,0,0,0.7)', backdropFilter: 'blur(6px)', padding: '20px' }}>
          <div className="glass-panel animate-fade-in" style={{ width: '100%', maxWidth: '520px', padding: '28px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
              <h3 style={{ fontSize: '1.3rem' }}>Final Checkout</h3>
              <button className="btn-secondary" style={{ padding: '6px' }} onClick={() => setIsCheckoutOpen(false)}>
                <X size={18} />
              </button>
            </div>

            {/* Address Selection */}
            <div style={{ marginBottom: '20px' }}>
              <label style={{ fontSize: '0.85rem', color: 'var(--text-dim)', textTransform: 'uppercase', fontWeight: 700, display: 'block', marginBottom: '8px' }}>
                Select Delivery Address
              </label>
              {INITIAL_CUSTOMER.addresses.map(addr => (
                <div 
                  key={addr.id}
                  onClick={() => setSelectedAddress(addr)}
                  style={{
                    padding: '12px',
                    borderRadius: 'var(--radius-sm)',
                    border: '1px solid ' + (selectedAddress.id === addr.id ? 'var(--accent-primary)' : 'var(--border-subtle)'),
                    background: selectedAddress.id === addr.id ? 'rgba(99, 102, 241, 0.1)' : 'rgba(0,0,0,0.2)',
                    marginBottom: '8px',
                    cursor: 'pointer'
                  }}
                >
                  <div style={{ fontWeight: 600 }}>{addr.street}</div>
                  <div style={{ fontSize: '0.8rem', color: 'var(--text-muted)' }}>{addr.city}, {addr.state} - {addr.zipCode}</div>
                </div>
              ))}
            </div>

            {/* Payment Method */}
            <div style={{ marginBottom: '24px' }}>
              <label style={{ fontSize: '0.85rem', color: 'var(--text-dim)', textTransform: 'uppercase', fontWeight: 700, display: 'block', marginBottom: '8px' }}>
                Payment Method
              </label>
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '10px' }}>
                {['UPI', 'CARD', 'COD'].map(m => (
                  <button
                    key={m}
                    className="btn-secondary"
                    style={{
                      justifyContent: 'center',
                      borderColor: paymentMethod === m ? 'var(--accent-primary)' : 'var(--border-subtle)',
                      background: paymentMethod === m ? 'rgba(99, 102, 241, 0.2)' : 'transparent'
                    }}
                    onClick={() => setPaymentMethod(m)}
                  >
                    {m}
                  </button>
                ))}
              </div>
            </div>

            {/* Summary */}
            <div style={{ background: 'rgba(0,0,0,0.3)', padding: '16px', borderRadius: 'var(--radius-sm)', marginBottom: '20px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '6px' }}>
                <span>Payable Amount:</span>
                <strong style={{ fontSize: '1.2rem', color: 'var(--accent-primary)' }}>₹{cartFinalTotal.toLocaleString()}</strong>
              </div>
              <span style={{ fontSize: '0.75rem', color: 'var(--text-dim)' }}>Zero contact delivery & free insured transit included.</span>
            </div>

            <button 
              className="gradient-btn" 
              style={{ width: '100%', justifyContent: 'center', padding: '14px' }}
              onClick={handlePlaceOrder}
            >
              Confirm & Pay ₹{cartFinalTotal.toLocaleString()}
            </button>
          </div>
        </div>
      )}

      {/* 5. PRODUCT DETAILS MODAL */}
      {selectedProduct && (
        <div style={{ position: 'fixed', inset: 0, zIndex: 110, display: 'flex', alignItems: 'center', justifyContent: 'center', background: 'rgba(0,0,0,0.7)', backdropFilter: 'blur(6px)', padding: '20px' }}>
          <div className="glass-panel animate-fade-in" style={{ width: '100%', maxWidth: '640px', overflow: 'hidden' }}>
            <div style={{ position: 'relative', height: '280px' }}>
              <img src={selectedProduct.image} alt={selectedProduct.name} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
              <button 
                className="btn-secondary" 
                style={{ position: 'absolute', top: '16px', right: '16px', borderRadius: '50%', padding: '6px', background: 'rgba(0,0,0,0.6)' }}
                onClick={() => setSelectedProduct(null)}
              >
                <X size={18} />
              </button>
            </div>
            <div style={{ padding: '24px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '12px' }}>
                <div>
                  <span className={`badge badge-${selectedProduct.category.toLowerCase()}`} style={{ marginBottom: '8px' }}>
                    {selectedProduct.category}
                  </span>
                  <h3 style={{ fontSize: '1.4rem', fontWeight: 700 }}>{selectedProduct.name}</h3>
                </div>
                <span style={{ fontSize: '1.6rem', fontWeight: 800, color: 'var(--accent-primary)' }}>₹{selectedProduct.price.toLocaleString()}</span>
              </div>

              <p style={{ color: 'var(--text-muted)', lineHeight: '1.6', marginBottom: '20px' }}>
                {selectedProduct.description}
              </p>

              <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
                <button 
                  className="gradient-btn"
                  style={{ flex: 1, justifyContent: 'center', padding: '12px' }}
                  disabled={selectedProduct.stock <= 0}
                  onClick={() => {
                    addToCart(selectedProduct);
                    setSelectedProduct(null);
                  }}
                >
                  <ShoppingCart size={18} /> Add to Cart
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

    </div>
  );
}
