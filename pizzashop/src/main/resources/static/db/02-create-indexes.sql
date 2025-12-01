-- sql
-- 02-create-indexes.sql
-- Indexes for pizza shop slim schema

-- USERS
CREATE INDEX idx_users_provider_provider_id ON users (provider, provider_id);
CREATE INDEX idx_users_is_deleted ON users (is_deleted);
CREATE INDEX idx_users_is_verified ON users (is_verified);
CREATE INDEX idx_users_created_at ON users (created_at);

-- USER_ROLES
CREATE INDEX idx_user_roles_role_id ON user_roles (role_id);

-- ADDRESSES
CREATE INDEX idx_addresses_user_id ON addresses (user_id);
CREATE INDEX idx_addresses_is_default ON addresses (is_default);

-- FOOD CATEGORIES
CREATE INDEX idx_food_categories_name ON food_categories (name);

-- FOODS
CREATE INDEX idx_foods_category_id ON foods (category_id);
CREATE INDEX idx_foods_is_active ON foods (is_active);
CREATE INDEX idx_foods_name ON foods (name);

-- FOOD SIZES
CREATE INDEX idx_food_sizes_food_id ON food_sizes (food_id);

-- CARTS
CREATE INDEX idx_carts_user_id ON carts (user_id);

-- CART ITEMS
CREATE INDEX idx_cart_items_cart_id ON cart_items (cart_id);
CREATE INDEX idx_cart_items_food_id ON cart_items (food_id);
CREATE INDEX idx_cart_items_size_id ON cart_items (size_id);

-- ORDERS
CREATE INDEX idx_orders_user_id ON orders (user_id);
CREATE INDEX idx_orders_status ON orders (status);
CREATE INDEX idx_orders_delivery_address_id ON orders (delivery_address_id);
CREATE INDEX idx_orders_order_time ON orders (order_time);
CREATE INDEX idx_orders_user_status ON orders (user_id, status);

-- ORDER ITEMS
CREATE INDEX idx_order_items_order_id ON order_items (order_id);
CREATE INDEX idx_order_items_food_id ON order_items (food_id);

-- PAYMENTS
CREATE INDEX idx_payments_order_id ON payments (order_id);
CREATE INDEX idx_payments_transaction_id ON payments (transaction_id);

-- PASSWORD RESET TOKENS
CREATE INDEX idx_password_reset_tokens_user_id ON password_reset_tokens (user_id);
CREATE INDEX idx_password_reset_tokens_token ON password_reset_tokens (token);
CREATE INDEX idx_password_reset_tokens_expires_at ON password_reset_tokens (expires_at);
