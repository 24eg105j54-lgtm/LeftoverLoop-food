package com.leftoverloop.service;

import com.leftoverloop.dto.ImpactStatsDto;
import com.leftoverloop.entity.DonationStatus;
import com.leftoverloop.entity.FoodDonation;
import com.leftoverloop.entity.FoodRequest;
import com.leftoverloop.entity.RequestStatus;
import com.leftoverloop.repository.FoodDonationRepository;
import com.leftoverloop.repository.FoodRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ImpactService {

    @Autowired
    private FoodDonationRepository donationRepository;

    @Autowired
    private FoodRequestRepository requestRepository;

    @Autowired
    private FoodDonationService donationService;

    public ImpactStatsDto getImpactStats() {
        donationService.updateExpiredDonationsStatus();

        List<FoodDonation> allDonations = donationRepository.findAll();
        List<FoodRequest> allRequests = requestRepository.findAll();

        long totalDonationsCount = allDonations.size();
        long totalFoodDonatedUnits = allDonations.stream().mapToLong(FoodDonation::getInitialQuantity).sum();

        long totalFoodCollectedUnits = allRequests.stream()
                .filter(r -> r.getStatus() == RequestStatus.COLLECTED)
                .mapToLong(FoodRequest::getRequestedQuantity)
                .sum();

        long completedDistributionsCount = allRequests.stream()
                .filter(r -> r.getStatus() == RequestStatus.COLLECTED)
                .count();

        long pendingRequestsCount = allRequests.stream()
                .filter(r -> r.getStatus() == RequestStatus.PENDING)
                .count();

        long expiredDonationsCount = allDonations.stream()
                .filter(d -> d.getStatus() == DonationStatus.EXPIRED || (d.getExpiryDate() != null && d.getExpiryDate().isBefore(LocalDateTime.now())))
                .count();

        long activeDonationsCount = allDonations.stream()
                .filter(d -> (d.getStatus() == DonationStatus.AVAILABLE || d.getStatus() == DonationStatus.PARTIALLY_RESERVED)
                          && d.getExpiryDate() != null
                          && d.getExpiryDate().isAfter(LocalDateTime.now()))
                .count();

        long remainingAvailableUnits = allDonations.stream()
                .filter(d -> d.getExpiryDate() != null && d.getExpiryDate().isAfter(LocalDateTime.now()))
                .mapToLong(d -> donationService.calculateRemainingQuantity(d.getId(), d.getInitialQuantity()))
                .sum();

        // Documented Calculation Formula:
        // 1 collected food unit/portion = ~2.5 people served (nutritional baseline per meal pack)
        long peopleServed = (long) (totalFoodCollectedUnits * 2.5);

        // 1 collected unit = ~0.75 kg of food rescued from landfill
        double foodSavedKg = Math.round(totalFoodCollectedUnits * 0.75 * 100.0) / 100.0;

        // 1 kg food waste avoided = ~2.5 kg CO2 equivalent emissions prevented
        double co2SavedKg = Math.round(foodSavedKg * 2.5 * 100.0) / 100.0;

        ImpactStatsDto dto = new ImpactStatsDto();
        dto.setTotalDonationsCount(totalDonationsCount);
        dto.setTotalFoodDonatedUnits(totalFoodDonatedUnits);
        dto.setTotalFoodCollectedUnits(totalFoodCollectedUnits);
        dto.setRemainingAvailableFoodUnits(remainingAvailableUnits);
        dto.setCompletedDistributionsCount(completedDistributionsCount);
        dto.setPendingRequestsCount(pendingRequestsCount);
        dto.setActiveDonationsCount(activeDonationsCount);
        dto.setExpiredDonationsCount(expiredDonationsCount);
        dto.setPeopleServed(peopleServed);
        dto.setFoodSavedKg(foodSavedKg);
        dto.setCo2SavedKg(co2SavedKg);

        return dto;
    }

    public String generateImpactCsv() {
        ImpactStatsDto stats = getImpactStats();
        StringBuilder csv = new StringBuilder();
        csv.append("Metric,Value,Unit/Details\n");
        csv.append("Total Food Listings,").append(stats.getTotalDonationsCount()).append(",Count\n");
        csv.append("Total Food Donated,").append(stats.getTotalFoodDonatedUnits()).append(",Units/Packets\n");
        csv.append("Total Food Rescued & Collected,").append(stats.getTotalFoodCollectedUnits()).append(",Units/Packets\n");
        csv.append("Remaining Available Food,").append(stats.getRemainingAvailableFoodUnits()).append(",Units/Packets\n");
        csv.append("Completed Distributions,").append(stats.getCompletedDistributionsCount()).append(",Fulfilled Requests\n");
        csv.append("People Served,").append(stats.getPeopleServed()).append(",Individuals Nourished\n");
        csv.append("Food Saved from Landfill,").append(stats.getFoodSavedKg()).append(",Kilograms (kg)\n");
        csv.append("CO2 Emissions Prevented,").append(stats.getCo2SavedKg()).append(",Kilograms CO2e\n");
        return csv.toString();
    }
}
