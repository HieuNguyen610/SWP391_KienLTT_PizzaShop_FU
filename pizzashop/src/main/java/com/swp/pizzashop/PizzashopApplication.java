package com.swp.pizzashop;

import com.swp.pizzashop.model.Food;
import com.swp.pizzashop.model.FoodCategory;
import com.swp.pizzashop.repository.FoodCategoryRepository;
import com.swp.pizzashop.repository.FoodRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.math.BigDecimal;

@SpringBootApplication
public class PizzashopApplication {

    public static void main(String[] args) {
        SpringApplication.run(PizzashopApplication.class, args);
    }

    // Bean này sẽ chạy khi ứng dụng khởi động để thêm dữ liệu mẫu vào DB
    @Bean
    CommandLineRunner commandLineRunner(FoodRepository foodRepository, FoodCategoryRepository categoryRepository) {
        return args -> {
            // -- Tạo Category mẫu nếu chưa có --
            FoodCategory pizzaCategory = categoryRepository.findByName("Pizza");
            if (pizzaCategory == null) {
                pizzaCategory = new FoodCategory();
                pizzaCategory.setName("Pizza");
                categoryRepository.save(pizzaCategory);
                System.out.println("Sample Category 'Pizza' inserted!");
            }

            // -- Chỉ thêm Food mẫu nếu bảng foods đang trống --
            if (foodRepository.count() == 0) {
                // Tạo food 1
                Food p1 = new Food();
                p1.setName("Seafood Pizza Cocktail");
                p1.setDescription("Shrimp, crab, ham and Thousand Island dressing.");
                p1.setBasePrice(new BigDecimal("154000.00"));
                p1.setImageUrl("/images/seafoodpizza.jpg");
                p1.setCategory(pizzaCategory); // Gán category
                p1.setActive(true);

                // Tạo food 2
                Food p2 = new Food();
                p2.setName("Premium Seafood Pizza");
                p2.setDescription("Shrimp, crab, squid, clams and marinara sauce.");
                p2.setBasePrice(new BigDecimal("159000.00"));
                p2.setImageUrl("/images/preniumpizza.jpg");
                p2.setCategory(pizzaCategory);
                p2.setActive(true);

                // Lưu vào database
                foodRepository.save(p1);
                foodRepository.save(p2);
                System.out.println("Sample food data inserted!");
            }
        };
    }
}
