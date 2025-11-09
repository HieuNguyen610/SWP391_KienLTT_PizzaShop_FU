package com.swp.pizzashop.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateSessionRequest {
    private String orderRef;
    private List<CartItemDto> cart;
}
