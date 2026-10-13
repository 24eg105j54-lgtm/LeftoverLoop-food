package com.leftoverloop;

import com.leftoverloop.dto.FoodDonationDto;
import com.leftoverloop.dto.FoodRequestDto;
import com.leftoverloop.dto.ImpactStatsDto;
import com.leftoverloop.entity.Role;
import com.leftoverloop.entity.User;
import com.leftoverloop.repository.UserRepository;
import com.leftoverloop.service.FoodDonationService;
import com.leftoverloop.service.FoodRequestService;
import com.leftoverloop.service.ImpactService;
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
public class ImpactServiceTest {

    @Autowired
    private ImpactService impactService;

    @Autowired
    private FoodDonationService donationService;

    @Autowired
    private FoodRequestService requestService;

    @Autowired
    private UserRepository userRepository;

    @Test
    void testImpactStatsAndCsvGeneration() {
        User donor = new User();
        donor.setFullName("Bakery Owner");
        donor.setEmail("bakery@shop.com");
        donor.setPassword("pass");
        donor.setRole(Role.ROLE_DONOR);
        donor = userRepository.save(donor);

        User receiver = new User();
        receiver.setFullName("Shelter Rep");
        receiver.setEmail("shelter@rep.org");
        receiver.setPassword("pass");
        receiver.setRole(Role.ROLE_RECEIVER);
        receiver = userRepository.save(receiver);

        FoodDonationDto dto = new FoodDonationDto();
        dto.setTitle("Artisan Breads");
        dto.setCategory("Bakery");
        dto.setInitialQuantity(30);
        dto.setUnit("loaves");
        dto.setExpiryDate(LocalDateTime.now().plusHours(24));
        dto.setLocation("Downtown");

        FoodDonationDto donation = donationService.createDonation(dto, donor);
        FoodRequestDto req = requestService.createRequest(donation.getId(), 20, "Bread for breakfast", receiver);

        // Complete collection
        requestService.verifyAndCompleteCollection(req.getCollectionCode(), donor);

        ImpactStatsDto stats = impactService.getImpactStats();
        assertTrue(stats.getTotalDonationsCount() >= 1);
        assertTrue(stats.getTotalFoodCollectedUnits() >= 20);
        assertTrue(stats.getPeopleServed() >= 50); // 20 * 2.5 = 50 people served
        assertTrue(stats.getFoodSavedKg() > 0);

        String csv = impactService.generateImpactCsv();
        assertTrue(csv.contains("Total Food Rescued & Collected"));
        assertTrue(csv.contains("People Served"));
    }
}
