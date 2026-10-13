package com.leftoverloop;

import com.leftoverloop.dto.FoodDonationDto;
import com.leftoverloop.entity.DonationStatus;
import com.leftoverloop.entity.Role;
import com.leftoverloop.entity.User;
import com.leftoverloop.exception.BadRequestException;
import com.leftoverloop.repository.UserRepository;
import com.leftoverloop.service.FoodDonationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class FoodDonationServiceTest {

    @Autowired
    private FoodDonationService donationService;

    @Autowired
    private UserRepository userRepository;

    private User donorUser;

    @BeforeEach
    void setUp() {
        donorUser = new User();
        donorUser.setFullName("Chef Mario");
        donorUser.setEmail("mario@restaurant.com");
        donorUser.setPassword("hashedpass");
        donorUser.setRole(Role.ROLE_DONOR);
        donorUser.setPhone("9998887776");
        donorUser = userRepository.save(donorUser);
    }

    @Test
    void testCreateFoodDonationSuccess() {
        FoodDonationDto dto = new FoodDonationDto();
        dto.setTitle("Fresh Pasta Trays");
        dto.setCategory("Prepared Meals");
        dto.setDescription("20 portions of freshly cooked pasta");
        dto.setInitialQuantity(20);
        dto.setUnit("portions");
        dto.setPreparationTime(LocalDateTime.now().minusHours(1));
        dto.setExpiryDate(LocalDateTime.now().plusHours(8));
        dto.setLocation("Downtown Kitchen");
        dto.setPickupInstructions("Use back door");

        FoodDonationDto created = donationService.createDonation(dto, donorUser);

        assertNotNull(created.getId());
        assertEquals("Fresh Pasta Trays", created.getTitle());
        assertEquals(20, created.getInitialQuantity());
        assertEquals(20, created.getRemainingQuantity());
        assertEquals(DonationStatus.AVAILABLE, created.getStatus());
        assertFalse(created.isExpired());
    }

    @Test
    void testCreateFoodDonationWithPastExpiryFails() {
        FoodDonationDto dto = new FoodDonationDto();
        dto.setTitle("Expired Bread");
        dto.setCategory("Bakery");
        dto.setInitialQuantity(10);
        dto.setUnit("loaves");
        dto.setExpiryDate(LocalDateTime.now().minusHours(2));

        assertThrows(BadRequestException.class, () -> donationService.createDonation(dto, donorUser));
    }
}
