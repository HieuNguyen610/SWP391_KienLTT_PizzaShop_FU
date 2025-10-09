package com.swp.pizzashop.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckoutResponse {
    private String sessionId;
    private String url;
    private String status; // e.g., CREATED
}

