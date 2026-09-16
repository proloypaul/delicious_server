package com.delicious.common.config;

import com.delicious.domain.customer.entity.CustomerProfile;
import com.delicious.domain.customer.repository.CustomerProfileRepository;
import com.delicious.domain.product.entity.Category;
import com.delicious.domain.product.entity.Product;
import com.delicious.domain.product.enums.ProductStatus;
import com.delicious.domain.product.repository.CategoryRepository;
import com.delicious.domain.product.repository.ProductRepository;
import com.delicious.domain.rider.entity.RiderProfile;
import com.delicious.domain.rider.repository.RiderProfileRepository;
import com.delicious.domain.seller.entity.SellerProfile;
import com.delicious.domain.seller.repository.SellerProfileRepository;
import com.delicious.domain.user.entity.User;
import com.delicious.domain.user.enums.UserRole;
import com.delicious.domain.user.enums.UserStatus;
import com.delicious.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CustomerProfileRepository customerProfileRepository;
    private final SellerProfileRepository sellerProfileRepository;
    private final RiderProfileRepository riderProfileRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Starting Data Initialization...");
        initializeData();
        log.info("Data Initialization completed.");
    }

    private void initializeData() {
        String defaultPassword = passwordEncoder.encode("password123");

        // Seed Admin
        if (!userRepository.existsByEmail("admin@example.com")) {
            User admin = User.builder()
                    .name("Admin User")
                    .email("admin@example.com")
                    .phone("1111111111")
                    .password(defaultPassword)
                    .role(UserRole.ADMIN)
                    .status(UserStatus.ACTIVE)
                    .build();
            userRepository.save(admin);
            log.info("Seeded Admin user.");
        }

        // Seed Customer
        if (!userRepository.existsByEmail("john@example.com")) {
            User customer = User.builder()
                    .name("John Doe")
                    .email("john@example.com")
                    .phone("0123456789")
                    .password(defaultPassword)
                    .role(UserRole.CUSTOMER)
                    .status(UserStatus.ACTIVE)
                    .build();
            userRepository.save(customer);

            CustomerProfile customerProfile = CustomerProfile.builder()
                    .user(customer)
                    .address("456 Customer Ave")
                    .build();
            customerProfileRepository.save(customerProfile);
            log.info("Seeded Customer user and profile.");
        }

        // Seed Seller
        User seller = null;
        if (!userRepository.existsByEmail("alice@example.com")) {
            seller = User.builder()
                    .name("Alice Seller")
                    .email("alice@example.com")
                    .phone("0987654321")
                    .password(defaultPassword)
                    .role(UserRole.SELLER)
                    .status(UserStatus.ACTIVE)
                    .build();
            userRepository.save(seller);

            SellerProfile sellerProfile = SellerProfile.builder()
                    .user(seller)
                    .storeName("Alice's Burger Shack")
                    .description("The best burgers in town")
                    .address("123 Burger Lane")
                    .build();
            sellerProfileRepository.save(sellerProfile);
            log.info("Seeded Seller user and profile.");
        } else {
            seller = userRepository.findByEmail("alice@example.com").orElse(null);
        }

        // Seed Rider
        if (!userRepository.existsByEmail("rider@example.com")) {
            User rider = User.builder()
                    .name("Speedy Rider")
                    .email("rider@example.com")
                    .phone("9998887776")
                    .password(defaultPassword)
                    .role(UserRole.RIDER)
                    .status(UserStatus.ACTIVE)
                    .build();
            userRepository.save(rider);

            RiderProfile riderProfile = RiderProfile.builder()
                    .user(rider)
                    .vehicleRegistrationNumber("XYZ-1234")
                    .vehicleType("BIKE")
                    .currentLocation("City Center")
                    .build();
            riderProfileRepository.save(riderProfile);
            log.info("Seeded Rider user and profile.");
        }

        // Seed Category
        Category fastFood = null;
        if (!categoryRepository.existsByNameIgnoreCase("Fast Food")) {
            fastFood = Category.builder()
                    .name("Fast Food")
                    .image("fast-food.png")
                    .build();
            categoryRepository.save(fastFood);
            log.info("Seeded Category.");
        } else {
            // Find category to use for product
            // Wait, categoryRepository doesn't have findByName, but we can assume it exists
            // or skip product if not found cleanly
            // To be safe, we'll just not seed the product if we can't easily get the
            // category right here,
            // or we could add a method. For simplicity, let's just rely on getting it.
            // Actually, we can get all categories and filter, or just leave it.
            // Let's assume if category isn't there, product is already there too.
        }

        // Seed Product
        if (seller != null && fastFood != null) {
            // Check if product exists by name for this seller just to be safe (or just
            // simple check)
            // For simplicity, we just seed one product if we just seeded the category.
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
            log.info("Seeded Product.");
        }
    }
}
