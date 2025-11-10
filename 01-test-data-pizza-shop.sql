-- 01-test-data-pizza-shop.sql
-- Roles
INSERT INTO roles (name) VALUES ('Customer'), ('Cashier'), ('Chef'), ('Manager'), ('Admin');

-- Users
INSERT INTO users (firstname, lastname, password, email, phone, status) VALUES
('Alice', 'Customer', 'password1', 'alice@example.com', '0123456789', 'ACTIVE'),
('Bob', 'Cashier', 'password2', 'bob@example.com', '0123456790', 'ACTIVE'),
('Carol', 'Chef', 'password3', 'carol@example.com', '0123456791', 'ACTIVE'),
('Dave', 'Manager', 'password4', 'dave@example.com', '0123456792', 'ACTIVE'),
('Eve', 'Admin', 'password5', 'eve@example.com', '0123456793', 'ACTIVE');

-- User Roles
INSERT INTO user_roles (user_id, role_id) VALUES
(15, 1), -- Alice: Customer
(16, 2), -- Bob: Cashier
(17, 3), -- Carol: Chef
(18, 4), -- Dave: Manager
(19, 5); -- Eve: Admin

-- Addresses
INSERT INTO addresses (user_id, address_line, city, district, phone, is_default) VALUES
(15, '123 Pizza St', 'PizzaCity', 'Central', '0123456789', TRUE);

-- Food Categories
INSERT INTO food_categories (name) VALUES
('Pizza'),
('Pasta'),
('Drink');

-- Foods
INSERT INTO foods (name, description, base_price, image_url, category_id) VALUES
('Margherita', 'Classic cheese and tomato', 5.00, 'margherita.jpg', 1),
('Pepperoni', 'Pepperoni and cheese', 6.50, 'pepperoni.jpg', 1),
('Veggie', 'Vegetables and cheese', 6.00, 'veggie.jpg', 1),
('Spaghetti Carbonara', 'Pasta with bacon and creamy sauce', 7.50, 'carbonara.jpg', 2),
('Lemonade', 'Fresh lemonade drink', 2.00, 'lemonade.jpg', 3),
('Fried Chicken', 'Crispy fried chicken', 4.50, 'fried_chicken.jpg', 1);


-- Food Sizes
INSERT INTO food_sizes (food_id, size, price) VALUES
(1, 'S', 5.00), (1, 'M', 7.00), (1, 'L', 9.00),
(2, 'S', 6.50), (2, 'M', 8.50), (2, 'L', 10.50)
;

-- Toppings
INSERT INTO toppings (name, price) VALUES
('Extra Cheese', 1.00),
('Mushrooms', 0.80),
('Olives', 0.70),
('Pepperoni', 1.20);

-- Food Default Toppings
INSERT INTO food_toppings_map (food_id, topping_id) VALUES
(1, 1), -- Margherita: Extra Cheese
(2, 4), -- Pepperoni: Pepperoni
(3, 2), (3, 3); -- Veggie: Mushrooms, Olives

-- Ingredients
INSERT INTO ingredients (name, unit, quantity_in_stock, min_stock) VALUES
('Cheese', 'g', 10000, 1000),
('Tomato Sauce', 'ml', 8000, 800),
('Pepperoni', 'g', 5000, 500),
('Mushrooms', 'g', 3000, 300),
('Olives', 'g', 2000, 200);

-- Food Ingredients
INSERT INTO food_ingredients (food_id, ingredient_id, amount) VALUES
(1, 1, 100), (1, 2, 50),
(2, 1, 100), (2, 2, 50), (2, 3, 80),
(3, 1, 100), (3, 2, 50), (3, 4, 60), (3, 5, 40),
(4, 1, 50), (4, 2, 30),
(5, 2, 10);

-- Discounts
INSERT INTO discounts (code, description, discount_type, value, valid_from, valid_to, is_active) VALUES
('WELCOME10', '10% off for new customers', 'PERCENT', 10, '2025-01-01', '2025-12-31', TRUE),
('FREESHIP', 'Free shipping', 'AMOUNT', 2, '2025-01-01', '2025-12-31', TRUE);

-- Orders
INSERT INTO orders (user_id, order_time, status, total_price, payment_method, delivery_address_id, discount_id) VALUES
(1, NOW(), 'PLACED', 16.00, 'CREDIT_CARD', 1, 1),
(2, NOW(), 'PLACED', 9.50, 'CASH', 1, NULL);

-- Order Items
INSERT INTO order_items (order_id, food_id, size_id, quantity, price, notes) VALUES
(1, 2, 5, 1, 8.50, 'No onions'),
(1, 1, 2, 1, 7.00, ''),
(2, 4, 10, 1, 7.50, 'Extra bacon'),
(2, 5, 11, 1, 2.00, 'No ice');

-- Order Item Toppings
INSERT INTO order_item_toppings (order_item_id, topping_id) VALUES
(1, 1), -- Extra Cheese on Pepperoni
(2, 2); -- Mushrooms on Margherita

-- Order Status History
INSERT INTO order_status_history (order_id, status, note) VALUES
(1, 'PLACED', 'Order placed by customer');

-- Payments
INSERT INTO payments (order_id, payment_type, amount, status, transaction_id, paid_at) VALUES
(1, 'CREDIT_CARD', 16.00, 'PAID', 'TXN123456', NOW());

-- Reviews
INSERT INTO reviews (user_id, order_id, food_id, rating, comment) VALUES
(1, 1, 2, 5, 'Delicious Pepperoni!'),
(1, 1, 1, 4, 'Classic Margherita, very good.'),
(2, 2, 4, 5, 'Best Carbonara!'),
(2, 2, 5, 3, 'Lemonade was okay.');

-- Invoices
INSERT INTO invoices (order_id, total, status, file_url) VALUES
(1, 16.00, 'ISSUED', 'invoice-1.pdf');
