-- 00-create-pizza-shop.sql
-- User and Role Management

-- Table for users (local and social login)
CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,     -- Unique user ID
    firstname VARCHAR(50) NOT NULL,           -- First name (remove full_name)
    lastname VARCHAR(50) NOT NULL,            -- Last name
    password VARCHAR(255) NOT NULL,           -- Hashed password
    email VARCHAR(100) NOT NULL UNIQUE,       -- User email
    phone VARCHAR(20),                        -- User phone number
    status VARCHAR(20) DEFAULT 'ACTIVE',      -- Account status
    provider VARCHAR(30),                     -- Social login provider (e.g., 'google', 'facebook')
    provider_id VARCHAR(100),                 -- Social login provider user id
    verification_token VARCHAR(128),          -- Inline email verification token
    verification_expires_at TIMESTAMP NULL,   -- Inline token expiry time
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Creation timestamp
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- Last update timestamp
    is_deleted BOOLEAN DEFAULT FALSE          -- Soft delete flag
);

-- Table for user roles
CREATE TABLE IF NOT EXISTS roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT, -- Unique role ID
    name VARCHAR(50) NOT NULL UNIQUE,     -- Role name (e.g., Customer, Admin)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Creation timestamp
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- Last update timestamp
    is_deleted BOOLEAN DEFAULT FALSE      -- Soft delete flag
);

-- Table for user-role mapping (no audit/soft delete fields, ON DELETE CASCADE)
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL,                  -- Reference to user
    role_id BIGINT NOT NULL,                  -- Reference to role
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- Table for user addresses
CREATE TABLE IF NOT EXISTS addresses (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,     -- Unique address ID
    user_id BIGINT NOT NULL,                  -- Reference to user
    address_line VARCHAR(255) NOT NULL,       -- Street address
    city VARCHAR(100),                        -- City
    district VARCHAR(100),                    -- District
    phone VARCHAR(20),                        -- Contact phone for delivery
    is_default BOOLEAN DEFAULT FALSE,         -- Is this the default address?
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Creation timestamp
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- Last update timestamp
    is_deleted BOOLEAN DEFAULT FALSE,         -- Soft delete flag
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Table for discounts/coupons
CREATE TABLE IF NOT EXISTS discounts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,     -- Unique discount ID
    code VARCHAR(50) NOT NULL UNIQUE,         -- Discount code
    description VARCHAR(255),                 -- Description
    discount_type VARCHAR(20),                -- Type (e.g., PERCENT, AMOUNT)
    value DECIMAL(10,2),                      -- Discount value
    valid_from DATE,                          -- Start date
    valid_to DATE,                            -- End date
    quantity INT DEFAULT 0,                   -- Number of uses left
    is_active BOOLEAN DEFAULT TRUE,           -- Is the discount active?
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Creation timestamp
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- Last update timestamp
    is_deleted BOOLEAN DEFAULT FALSE          -- Soft delete flag
);

-- Table for food categories
CREATE TABLE IF NOT EXISTS food_categories (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,     -- Unique category ID
    name VARCHAR(50) NOT NULL UNIQUE,         -- Category name (e.g., pizza, pasta, drink)
    description VARCHAR(255),                 -- Description of the category
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Creation timestamp
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- Last update timestamp
    is_deleted BOOLEAN DEFAULT FALSE          -- Soft delete flag
);

-- Table for food menu
CREATE TABLE IF NOT EXISTS foods (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,     -- Unique food ID
    name VARCHAR(100) NOT NULL,               -- Food name
    description TEXT,                         -- Description
    base_price DECIMAL(10,2) NOT NULL,        -- Base price
    image_url VARCHAR(255),                   -- Image URL
    category_id BIGINT NOT NULL,              -- Reference to food category
    is_active BOOLEAN DEFAULT TRUE,           -- Is the food available?
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Creation timestamp
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- Last update timestamp
    is_deleted BOOLEAN DEFAULT FALSE,         -- Soft delete flag
    FOREIGN KEY (category_id) REFERENCES food_categories(id) ON DELETE RESTRICT
);

-- Table for food sizes
CREATE TABLE IF NOT EXISTS food_sizes (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,     -- Unique size ID
    food_id BIGINT NOT NULL,                  -- Reference to food
    size VARCHAR(10) NOT NULL,                -- Size (S, M, L, etc.)
    price DECIMAL(10,2) NOT NULL,             -- Price for this size
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Creation timestamp
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- Last update timestamp
    is_deleted BOOLEAN DEFAULT FALSE,         -- Soft delete flag
    FOREIGN KEY (food_id) REFERENCES foods(id) ON DELETE CASCADE
);

-- Table for toppings
CREATE TABLE IF NOT EXISTS toppings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,     -- Unique topping ID
    name VARCHAR(100) NOT NULL,               -- Topping name
    price DECIMAL(10,2) NOT NULL,             -- Price for topping
    is_active BOOLEAN DEFAULT TRUE,           -- Is the topping available?
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Creation timestamp
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- Last update timestamp
    is_deleted BOOLEAN DEFAULT FALSE          -- Soft delete flag
);

-- Table for default toppings per food (mapping, no audit/soft delete fields, ON DELETE CASCADE)
CREATE TABLE IF NOT EXISTS food_toppings_map (
    food_id BIGINT NOT NULL,                  -- Reference to food
    topping_id BIGINT NOT NULL,               -- Reference to topping
    PRIMARY KEY (food_id, topping_id),
    FOREIGN KEY (food_id) REFERENCES foods(id) ON DELETE CASCADE,
    FOREIGN KEY (topping_id) REFERENCES toppings(id) ON DELETE CASCADE
);

-- Table for ingredients
CREATE TABLE IF NOT EXISTS ingredients (
                                           id BIGINT PRIMARY KEY AUTO_INCREMENT,     -- Unique ingredient ID
                                           name VARCHAR(100) NOT NULL,               -- Ingredient name
                                           unit VARCHAR(20),                         -- Unit (e.g., g, ml)
                                           quantity_in_stock DECIMAL(10,2) DEFAULT 0,-- Current stock
                                           min_stock DECIMAL(10,2) DEFAULT 0,        -- Minimum stock
                                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Creation timestamp
                                           updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- Last update timestamp
                                           is_deleted BOOLEAN DEFAULT FALSE          -- Soft delete flag
);

-- Table for food-ingredient mapping (no audit/soft delete fields, ON DELETE CASCADE)
CREATE TABLE IF NOT EXISTS food_ingredients (
    food_id BIGINT NOT NULL,                  -- Reference to food
    ingredient_id BIGINT NOT NULL,            -- Reference to ingredient
    amount DECIMAL(10,2) NOT NULL,            -- Amount of ingredient
    PRIMARY KEY (food_id, ingredient_id),
    FOREIGN KEY (food_id) REFERENCES foods(id) ON DELETE CASCADE,
    FOREIGN KEY (ingredient_id) REFERENCES ingredients(id) ON DELETE CASCADE
);

-- Table for shopping carts
CREATE TABLE IF NOT EXISTS carts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,     -- Unique cart ID
    user_id BIGINT NOT NULL,                  -- Reference to user
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Creation timestamp
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- Last update timestamp
    is_deleted BOOLEAN DEFAULT FALSE,         -- Soft delete flag
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

-- Table for items in a cart
CREATE TABLE IF NOT EXISTS cart_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,     -- Unique cart item ID
    cart_id BIGINT NOT NULL,                  -- Reference to cart
    food_id BIGINT NOT NULL,                  -- Reference to food
    size_id BIGINT NOT NULL,                  -- Reference to food size
    quantity INT NOT NULL,                    -- Quantity
    price DECIMAL(10,2) NOT NULL,             -- Price
    notes VARCHAR(255),                       -- Notes
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Creation timestamp
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- Last update timestamp
    is_deleted BOOLEAN DEFAULT FALSE,         -- Soft delete flag
    FOREIGN KEY (cart_id) REFERENCES carts(id) ON DELETE CASCADE,
    FOREIGN KEY (food_id) REFERENCES foods(id) ON DELETE CASCADE,
    FOREIGN KEY (size_id) REFERENCES food_sizes(id) ON DELETE CASCADE
);

-- Table for toppings on cart items (mapping, no audit/soft delete fields, ON DELETE CASCADE)
CREATE TABLE IF NOT EXISTS cart_item_toppings (
    cart_item_id BIGINT NOT NULL,             -- Reference to cart item
    topping_id BIGINT NOT NULL,               -- Reference to topping
    price DECIMAL(10,2) NOT NULL DEFAULT 0,   -- Price of topping at time of adding to cart
    PRIMARY KEY (cart_item_id, topping_id),
    FOREIGN KEY (cart_item_id) REFERENCES cart_items(id) ON DELETE CASCADE,
    FOREIGN KEY (topping_id) REFERENCES toppings(id) ON DELETE CASCADE
);

-- Table for orders
CREATE TABLE IF NOT EXISTS orders (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,     -- Unique order ID
    user_id BIGINT NOT NULL,                  -- Reference to user
    order_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Order time
    status VARCHAR(30) NOT NULL,              -- Order status
    total_price DECIMAL(10,2) NOT NULL,       -- Total price
    payment_method VARCHAR(30),               -- Payment method
    delivery_address_id BIGINT,               -- Reference to address
    discount_id BIGINT,                       -- Reference to discount
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Creation timestamp
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- Last update timestamp
    is_deleted BOOLEAN DEFAULT FALSE,         -- Soft delete flag
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (delivery_address_id) REFERENCES addresses(id) ON DELETE SET NULL,
    FOREIGN KEY (discount_id) REFERENCES discounts(id) ON DELETE SET NULL
);

-- Table for items in an order
CREATE TABLE IF NOT EXISTS order_items (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,     -- Unique order item ID
    order_id BIGINT NOT NULL,                 -- Reference to order
    food_id BIGINT NOT NULL,                  -- Reference to food
    size_id BIGINT NOT NULL,                  -- Reference to food size
    quantity INT NOT NULL,                    -- Quantity
    price DECIMAL(10,2) NOT NULL,             -- Price
    notes VARCHAR(255),                       -- Notes
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Creation timestamp
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- Last update timestamp
    is_deleted BOOLEAN DEFAULT FALSE,         -- Soft delete flag
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (food_id) REFERENCES foods(id) ON DELETE CASCADE,
    FOREIGN KEY (size_id) REFERENCES food_sizes(id) ON DELETE CASCADE
);

-- Table for toppings on order items (mapping, no audit/soft delete fields, ON DELETE CASCADE)
CREATE TABLE IF NOT EXISTS order_item_toppings (
    order_item_id BIGINT NOT NULL,            -- Reference to order item
    topping_id BIGINT NOT NULL,               -- Reference to topping
    price DECIMAL(10,2) NOT NULL DEFAULT 0,   -- Price of topping at time of order
    PRIMARY KEY (order_item_id, topping_id),
    FOREIGN KEY (order_item_id) REFERENCES order_items(id) ON DELETE CASCADE,
    FOREIGN KEY (topping_id) REFERENCES toppings(id) ON DELETE CASCADE
);

-- Table for order status history
CREATE TABLE IF NOT EXISTS order_status_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,     -- Unique status history ID
    order_id BIGINT NOT NULL,                 -- Reference to order
    status VARCHAR(30) NOT NULL,              -- Status
    note VARCHAR(255),                        -- Note
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Creation timestamp
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- Last update timestamp
    is_deleted BOOLEAN DEFAULT FALSE,         -- Soft delete flag
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

-- Table for payments
CREATE TABLE IF NOT EXISTS payments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,     -- Unique payment ID
    order_id BIGINT NOT NULL,                 -- Reference to order
    payment_type VARCHAR(30),                 -- Payment type
    amount DECIMAL(10,2) NOT NULL,            -- Amount paid
    status VARCHAR(30),                       -- Payment status
    transaction_id VARCHAR(100),              -- Transaction ID
    paid_at TIMESTAMP,                        -- Payment time
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Creation timestamp
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- Last update timestamp
    is_deleted BOOLEAN DEFAULT FALSE,         -- Soft delete flag
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

-- Table for reviews
CREATE TABLE IF NOT EXISTS reviews (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,     -- Unique review ID
    user_id BIGINT NOT NULL,                  -- Reference to user
    order_id BIGINT NOT NULL,                 -- Reference to order
    food_id BIGINT NOT NULL,                  -- Reference to food
    rating INT NOT NULL,                      -- Rating
    comment VARCHAR(255),                     -- Review comment
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Creation timestamp
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- Last update timestamp
    is_deleted BOOLEAN DEFAULT FALSE,         -- Soft delete flag
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    FOREIGN KEY (food_id) REFERENCES foods(id) ON DELETE CASCADE
);

-- Table for invoices
CREATE TABLE IF NOT EXISTS invoices (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,     -- Unique invoice ID
    order_id BIGINT NOT NULL,                 -- Reference to order
    issued_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Issue timestamp
    total DECIMAL(10,2) NOT NULL,             -- Invoice total
    status VARCHAR(30),                       -- Invoice status
    file_url VARCHAR(255),                    -- Invoice file URL
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Creation timestamp
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, -- Last update timestamp
    is_deleted BOOLEAN DEFAULT FALSE,         -- Soft delete flag
    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
);

-- Table for password reset tokens (forgot password)
CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,     -- Unique token ID
    user_id BIGINT NOT NULL,                  -- Reference to user
    token VARCHAR(255) NOT NULL,              -- Reset token
    expires_at TIMESTAMP NOT NULL,            -- Expiry time
    used BOOLEAN DEFAULT FALSE,               -- Has the token been used?
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Creation timestamp
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);
