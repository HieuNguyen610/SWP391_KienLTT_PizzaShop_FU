package com.swp.pizzashop.service;

import java.util.List;

public interface OrderStatsService {
    List<Object[]> countByStatus();
}
