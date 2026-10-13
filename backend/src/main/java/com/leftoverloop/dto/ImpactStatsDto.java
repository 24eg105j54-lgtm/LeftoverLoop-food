package com.leftoverloop.dto;

public class ImpactStatsDto {
    private long totalDonationsCount;
    private long totalFoodDonatedUnits;
    private long totalFoodCollectedUnits;
    private long remainingAvailableFoodUnits;
    private long completedDistributionsCount;
    private long pendingRequestsCount;
    private long activeDonationsCount;
    private long expiredDonationsCount;
    private long peopleServed;
    private double foodSavedKg;
    private double co2SavedKg;

    public ImpactStatsDto() {}

    public long getTotalDonationsCount() { return totalDonationsCount; }
    public void setTotalDonationsCount(long totalDonationsCount) { this.totalDonationsCount = totalDonationsCount; }

    public long getTotalFoodDonatedUnits() { return totalFoodDonatedUnits; }
    public void setTotalFoodDonatedUnits(long totalFoodDonatedUnits) { this.totalFoodDonatedUnits = totalFoodDonatedUnits; }

    public long getTotalFoodCollectedUnits() { return totalFoodCollectedUnits; }
    public void setTotalFoodCollectedUnits(long totalFoodCollectedUnits) { this.totalFoodCollectedUnits = totalFoodCollectedUnits; }

    public long getRemainingAvailableFoodUnits() { return remainingAvailableFoodUnits; }
    public void setRemainingAvailableFoodUnits(long remainingAvailableFoodUnits) { this.remainingAvailableFoodUnits = remainingAvailableFoodUnits; }

    public long getCompletedDistributionsCount() { return completedDistributionsCount; }
    public void setCompletedDistributionsCount(long completedDistributionsCount) { this.completedDistributionsCount = completedDistributionsCount; }

    public long getPendingRequestsCount() { return pendingRequestsCount; }
    public void setPendingRequestsCount(long pendingRequestsCount) { this.pendingRequestsCount = pendingRequestsCount; }

    public long getActiveDonationsCount() { return activeDonationsCount; }
    public void setActiveDonationsCount(long activeDonationsCount) { this.activeDonationsCount = activeDonationsCount; }

    public long getExpiredDonationsCount() { return expiredDonationsCount; }
    public void setExpiredDonationsCount(long expiredDonationsCount) { this.expiredDonationsCount = expiredDonationsCount; }

    public long getPeopleServed() { return peopleServed; }
    public void setPeopleServed(long peopleServed) { this.peopleServed = peopleServed; }

    public double getFoodSavedKg() { return foodSavedKg; }
    public void setFoodSavedKg(double foodSavedKg) { this.foodSavedKg = foodSavedKg; }

    public double getCo2SavedKg() { return co2SavedKg; }
    public void setCo2SavedKg(double co2SavedKg) { this.co2SavedKg = co2SavedKg; }
}
