package com.swp.pizzashop.controller;

import com.swp.pizzashop.service.FoodService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class FoodController {

        private final FoodService foodService;
}