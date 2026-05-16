package com.delicious.common.config;

import com.delicious.domain.product.entity.Category;
import com.delicious.domain.product.entity.Product;
import com.delicious.domain.product.enums.ProductStatus;
import com.delicious.domain.product.repository.CategoryRepository;
import com.delicious.domain.product.repository.ProductRepository;
import com.delicious.domain.user.entity.User;
import com.delicious.domain.user.enums.UserRole;
import com.delicious.domain.user.enums.UserStatus;
import com.delicious.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            initializeData();
        }
    }

    private void initializeData() {
        // Create a Customer
        User customer = User.builder()
                .name("John Doe")
                .email("john@example.com")
                .phone("0123456789")
                .password(passwordEncoder.encode("password123"))
                .role(UserRole.CUSTOMER)
                .status(UserStatus.ACTIVE)
                .build();
        userRepository.save(customer);

        // Create a Seller
        User seller = User.builder()
                .name("Alice Seller")
                .email("alice@example.com")
                .phone("0987654321")
                .password(passwordEncoder.encode("password123"))
                .role(UserRole.SELLER)
                .status(UserStatus.ACTIVE)
                .build();
        userRepository.save(seller);

        // Create a Category
        Category fastFood = Category.builder()
                .name("Fast Food")
                .image("fast-food.png")
                .build();
        categoryRepository.save(fastFood);

        // Create a Product
        Product burger = Product.builder()
                .foodName("Double Cheese Burger")
                .description("Delicious double cheese burger with special sauce")
                .price(new BigDecimal("5.99"))
                .makingTime(15)
                .status(ProductStatus.APPROVED)
                .sellerId(seller.getId())
                .category(fastFood)
                .build();
        productRepository.save(burger);

        System.out.println("Sample data initialized successfully.");
    }
}
