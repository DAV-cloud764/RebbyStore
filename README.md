# RebbyStore

A full-stack e-commerce platform for **RebbyStore**, a wig store based in **Ubungo Riverside, Dar es Salaam, Tanzania**.

RebbyStore provides a customer storefront for browsing and purchasing wigs and an administrative platform for managing products, inventory, purchases, orders, customers, suppliers, and operational reporting.

The project is currently implemented as a full-stack MVP with a React frontend and Spring Boot backend backed by PostgreSQL.

---

## Project Status

> **Status: Full-stack MVP**

The application currently includes:

- Customer storefront
- Administrative dashboard
- Database-backed product, customer, order, inventory, supplier, and purchase management
- JWT authentication
- Role-based authorization
- Inventory tracking and stock movement history
- Purchase receiving and automatic stock-in
- Order lifecycle management
- Product image management
- Reporting endpoints
- API rate limiting
- CORS configuration
- Global API exception handling
- Application logging
- Spring Boot Actuator monitoring

### Current limitations

- Payment method is currently **Cash on Delivery**
- No online payment gateway has been integrated yet
- Checkout and protected management APIs require authentication
- Delivery is currently limited to the configured business rules below

---

## Features

### Customer Storefront

- Responsive Bento Grid homepage
- Product browsing and discovery
- Product search
- Category filtering
- Hair type filtering
- Texture filtering
- Length filtering
- Price filtering
- Product sorting
- Product details
- Product image gallery
- Wishlist
- Shopping cart
- Quantity controls
- Stock-aware purchasing
- Checkout
- Cash on Delivery
- Order confirmation

### Admin Panel

- Dashboard
- Product management
- Add and edit products
- Product image management
- Inventory overview
- Stock-in
- Stock-out
- Stock adjustments
- Inventory movement history
- Low-stock monitoring
- Purchase management
- Supplier management
- Order management
- Order status management
- Customer management
- Reporting and operational summaries
- Store settings

---

## Business Rules

### Delivery

Orders with a subtotal of **TSh 200,000 or more** qualify for free delivery.

Orders below **TSh 200,000** have a flat **TSh 5,000** delivery fee.

### Payment

The currently supported payment method is:

**Cash on Delivery (COD)**

### Order Lifecycle

Orders follow the following lifecycle:

```text
PENDING
   ↓
CONFIRMED
   ↓
PROCESSING
   ↓
READY_FOR_DELIVERY
   ↓
DELIVERED
