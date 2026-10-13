package com.leftoverloop;

import com.leftoverloop.dto.FoodDonationDto;
import com.leftoverloop.dto.UserDto;
import com.leftoverloop.entity.Role;
import com.leftoverloop.entity.User;
import com.leftoverloop.repository.UserRepository;
import com.leftoverloop.service.AdminService;
import com.leftoverloop.service.FoodDonationService;
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
public class AdminServiceTest {

    @Autowired
    private AdminService adminService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FoodDonationService donationService;

    @Test
    void testAdminUserManagementAndAuditing() {
        User user = new User();
        user.setFullName("Audit Target");
        user.setEmail("target@user.com");
        user.setPassword("secret");
        user.setRole(Role.ROLE_DONOR);
        user.setActive(true);
        user = userRepository.save(user);

        // Toggle user status
        UserDto toggled = adminService.toggleUserStatus(user.getId());
        assertFalse(toggled.isActive());

        // Create sample donation with donor role and audit
        FoodDonationDto dto = new FoodDonationDto();
        dto.setTitle("Audit Food");
        dto.setCategory("Dairy");
        dto.setInitialQuantity(15);
        dto.setUnit("cartons");
        dto.setExpiryDate(LocalDateTime.now().plusDays(2));
        dto.setLocation("Warehouse A");
        donationService.createDonation(dto, user);

        List<FoodDonationDto> allDonations = adminService.getAllDonations();
        assertTrue(allDonations.size() >= 1);

        // Update user role to RECEIVER
        UserDto updatedRole = adminService.updateUserRole(user.getId(), Role.ROLE_RECEIVER);
        assertEquals(Role.ROLE_RECEIVER, updatedRole.getRole());
    }
}
