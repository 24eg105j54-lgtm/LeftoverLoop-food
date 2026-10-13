package com.leftoverloop.service;

import com.leftoverloop.dto.FoodDonationDto;
import com.leftoverloop.dto.FoodRequestDto;
import com.leftoverloop.dto.UserDto;
import com.leftoverloop.entity.Role;
import com.leftoverloop.entity.User;
import com.leftoverloop.exception.BadRequestException;
import com.leftoverloop.exception.ResourceNotFoundException;
import com.leftoverloop.repository.FoodDonationRepository;
import com.leftoverloop.repository.FoodRequestRepository;
import com.leftoverloop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private FoodDonationRepository donationRepository;

    @Autowired
    private FoodRequestRepository requestRepository;

    @Autowired
    private FoodDonationService donationService;

    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserDto::new)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserDto toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        user.setActive(!user.isActive());
        User saved = userRepository.save(user);
        return new UserDto(saved);
    }

    @Transactional
    public UserDto updateUserRole(Long userId, Role newRole) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        user.setRole(newRole);
        User saved = userRepository.save(user);
        return new UserDto(saved);
    }

    public List<FoodDonationDto> getAllDonations() {
        donationService.updateExpiredDonationsStatus();
        return donationRepository.findAll().stream()
                .map(donationService::convertToDto)
                .collect(Collectors.toList());
    }

    public List<FoodRequestDto> getAllRequests() {
        return requestRepository.findAll().stream()
                .map(FoodRequestDto::new)
                .collect(Collectors.toList());
    }
}
