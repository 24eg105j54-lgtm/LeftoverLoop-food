package com.leftoverloop;

import com.leftoverloop.dto.FoodDonationDto;
import com.leftoverloop.dto.FoodRequestDto;
import com.leftoverloop.dto.RequestActionDto;
import com.leftoverloop.entity.*;
import com.leftoverloop.exception.BadRequestException;
import com.leftoverloop.repository.UserRepository;
import com.leftoverloop.service.FoodDonationService;
import com.leftoverloop.service.FoodRequestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class FoodRequestServiceTest {

    @Autowired
    private FoodRequestService requestService;

    @Autowired
    private FoodDonationService donationService;

    @Autowired
    private UserRepository userRepository;

    private User orgUser;
    private User receiverUser1;
    private User receiverUser2;
    private FoodDonationDto donationDto;

    @BeforeEach
    void setUp() {
        orgUser = new User();
        orgUser.setFullName("Hope Shelter Admin");
        orgUser.setEmail("hope@shelter.org");
        orgUser.setPassword("pass123");
        orgUser.setRole(Role.ROLE_ORGANIZATION);
        orgUser.setOrganizationName("Hope Food Bank");
        orgUser = userRepository.save(orgUser);

        receiverUser1 = new User();
        receiverUser1.setFullName("Receiver Alice");
        receiverUser1.setEmail("alice@community.org");
        receiverUser1.setPassword("pass123");
        receiverUser1.setRole(Role.ROLE_RECEIVER);
        receiverUser1 = userRepository.save(receiverUser1);

        receiverUser2 = new User();
        receiverUser2.setFullName("Receiver Bob");
        receiverUser2.setEmail("bob@community.org");
        receiverUser2.setPassword("pass123");
        receiverUser2.setRole(Role.ROLE_RECEIVER);
        receiverUser2 = userRepository.save(receiverUser2);

        FoodDonationDto dto = new FoodDonationDto();
        dto.setTitle("Bulk Meal Packets");
        dto.setCategory("Prepared Meals");
        dto.setInitialQuantity(20);
        dto.setUnit("packets");
        dto.setPreparationTime(LocalDateTime.now().minusHours(1));
        dto.setExpiryDate(LocalDateTime.now().plusHours(12));
        dto.setLocation("Central Pantry");
        dto.setPickupInstructions("Gate 2");

        donationDto = donationService.createDonation(dto, orgUser);
    }

    @Test
    void testPartialQuantityRequestAndRemainingCalculation() {
        // Request 10 packets out of 20
        FoodRequestDto req1 = requestService.createRequest(donationDto.getId(), 10, "For family shelter", receiverUser1);

        assertNotNull(req1.getId());
        assertEquals(10, req1.getRequestedQuantity());
        assertNotNull(req1.getCollectionCode());
        assertTrue(req1.getCollectionCode().startsWith("LL-"));

        // Verify remaining quantity is now 10
        FoodDonationDto updatedDonation = donationService.getDonationById(donationDto.getId());
        assertEquals(10, updatedDonation.getRemainingQuantity());
        assertEquals(DonationStatus.PARTIALLY_RESERVED, updatedDonation.getStatus());

        // Second receiver sees remaining 10 as AVAILABLE and requests 10
        FoodRequestDto req2 = requestService.createRequest(donationDto.getId(), 10, "For local youth center", receiverUser2);
        assertEquals(10, req2.getRequestedQuantity());

        // Verify remaining quantity is now 0 and status is RESERVED
        FoodDonationDto finalDonation = donationService.getDonationById(donationDto.getId());
        assertEquals(0, finalDonation.getRemainingQuantity());
        assertEquals(DonationStatus.RESERVED, finalDonation.getStatus());
    }

    @Test
    void testOverbookingPreventionFails() {
        // Request 12 packets out of 20
        requestService.createRequest(donationDto.getId(), 12, "Partial request", receiverUser1);

        // Attempting to request 10 packets when only 8 remain must throw BadRequestException
        BadRequestException ex = assertThrows(BadRequestException.class, () ->
                requestService.createRequest(donationDto.getId(), 10, "Overbooking request", receiverUser2)
        );

        assertTrue(ex.getMessage().contains("exceeds available remaining quantity"));
    }

    @Test
    void testOrganizationIncomingRequestsWorkflow() {
        // Receiver requests food posted by organization
        requestService.createRequest(donationDto.getId(), 5, "Community request", receiverUser1);

        // Organization checks incoming requests
        List<FoodRequestDto> incoming = requestService.getIncomingRequestsForUser(orgUser);
        assertEquals(1, incoming.size());
        assertEquals("Receiver Alice", incoming.get(0).getReceiverName());
    }

    @Test
    void testAcceptRejectAndCollectionVerificationWorkflow() {
        FoodRequestDto req = requestService.createRequest(donationDto.getId(), 5, "Need meals", receiverUser1);

        // Organization accepts request
        RequestActionDto acceptAction = new RequestActionDto(RequestStatus.ACCEPTED, "Approved for pickup between 2-4 PM");
        FoodRequestDto acceptedReq = requestService.updateRequestStatus(req.getId(), acceptAction, orgUser);
        assertEquals(RequestStatus.ACCEPTED, acceptedReq.getStatus());

        // Verify collection using code
        FoodRequestDto collectedReq = requestService.verifyAndCompleteCollection(acceptedReq.getCollectionCode(), orgUser);
        assertEquals(RequestStatus.COLLECTED, collectedReq.getStatus());
        assertNotNull(collectedReq.getCollectedAt());
    }

    @Test
    void testGetRequestByCodeForReceiptVerification() {
        FoodRequestDto created = requestService.createRequest(donationDto.getId(), 4, "Receipt check", receiverUser1);
        assertNotNull(created.getCollectionCode());

        // Receiver can fetch receipt by code
        FoodRequestDto byCodeReceiver = requestService.getRequestByCode(created.getCollectionCode(), receiverUser1);
        assertEquals(created.getId(), byCodeReceiver.getId());
        assertEquals("Bulk Meal Packets", byCodeReceiver.getDonationTitle());

        // Donor / Organization can also fetch receipt by code
        FoodRequestDto byCodeOrg = requestService.getRequestByCode(created.getCollectionCode(), orgUser);
        assertEquals(created.getId(), byCodeOrg.getId());
    }
}
