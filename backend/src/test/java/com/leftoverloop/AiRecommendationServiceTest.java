package com.leftoverloop;

import com.leftoverloop.dto.AiMatchRequest;
import com.leftoverloop.dto.AiMatchResponse;
import com.leftoverloop.dto.FoodDonationDto;
import com.leftoverloop.entity.Role;
import com.leftoverloop.entity.User;
import com.leftoverloop.repository.UserRepository;
import com.leftoverloop.service.AiRecommendationService;
import com.leftoverloop.service.FoodDonationService;
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
public class AiRecommendationServiceTest {

    @Autowired
    private AiRecommendationService aiService;

    @Autowired
    private FoodDonationService donationService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void testAiRecommendationEngine() {
        User donor = new User();
        donor.setFullName("Catering Pro");
        donor.setEmail("pro@catering.com");
        donor.setPassword("pass");
        donor.setRole(Role.ROLE_DONOR);
        donor = userRepository.save(donor);

        FoodDonationDto d1 = new FoodDonationDto();
        d1.setTitle("Hot Soup & Bread");
        d1.setCategory("Prepared Meals");
        d1.setInitialQuantity(30);
        d1.setUnit("bowls");
        d1.setExpiryDate(LocalDateTime.now().plusHours(3)); // Urgent expiry
        d1.setLocation("North Street");
        donationService.createDonation(d1, donor);

        FoodDonationDto d2 = new FoodDonationDto();
        d2.setTitle("Canned Beans");
        d2.setCategory("Packaged Goods");
        d2.setInitialQuantity(50);
        d2.setUnit("cans");
        d2.setExpiryDate(LocalDateTime.now().plusDays(30));
        d2.setLocation("South Street");
        donationService.createDonation(d2, donor);

        AiMatchRequest req = new AiMatchRequest();
        req.setPreferredCategory("Prepared Meals");
        req.setLocation("North Street");
        req.setRequestedQuantity(10);
        req.setReceiverType("SHELTER");

        AiMatchResponse response = aiService.getRecommendations(req);

        assertNotNull(response);
        assertNotNull(response.getModelUsed());
        assertFalse(response.getRecommendations().isEmpty());

        // Top match should be the Prepared Meals item with North Street and urgent expiry
        AiMatchResponse.RecommendedItem topMatch = response.getRecommendations().get(0);
        assertEquals("Prepared Meals", topMatch.getDonation().getCategory());
        assertTrue(topMatch.getMatchScore() > 70.0);
        assertNotNull(topMatch.getMatchReason());
    }
}
