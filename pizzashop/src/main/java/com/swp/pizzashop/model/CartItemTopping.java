package com.swp.pizzashop.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "cart_item_toppings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemTopping {

    @EmbeddedId
    private CartItemToppingId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("cartItemId")
    @JoinColumn(name = "cart_item_id", nullable = false)
    private CartItem cartItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("toppingId")
    @JoinColumn(name = "topping_id", nullable = false)
    private Topping topping;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price; // snapshot at time of add

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CartItemToppingId implements Serializable {
        @Column(name = "cart_item_id")
        private Long cartItemId;
        @Column(name = "topping_id")
        private Long toppingId;
    }
}

