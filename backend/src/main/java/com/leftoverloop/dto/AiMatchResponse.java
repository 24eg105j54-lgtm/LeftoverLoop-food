package com.leftoverloop.dto;

import java.util.List;

public class AiMatchResponse {
    private String modelUsed;
    private boolean liveCloudAi;
    private String summaryRationale;
    private List<RecommendedItem> recommendations;

    public AiMatchResponse() {}

    public AiMatchResponse(String modelUsed, boolean liveCloudAi, String summaryRationale, List<RecommendedItem> recommendations) {
        this.modelUsed = modelUsed;
        this.liveCloudAi = liveCloudAi;
        this.summaryRationale = summaryRationale;
        this.recommendations = recommendations;
    }

    public static class RecommendedItem {
        private FoodDonationDto donation;
        private double matchScore; // 0.0 to 100.0
        private String matchReason;
        private String urgencyLevel; // HIGH, MEDIUM, NORMAL

        public RecommendedItem() {}

        public RecommendedItem(FoodDonationDto donation, double matchScore, String matchReason, String urgencyLevel) {
            this.donation = donation;
            this.matchScore = matchScore;
            this.matchReason = matchReason;
            this.urgencyLevel = urgencyLevel;
        }

        public FoodDonationDto getDonation() { return donation; }
        public void setDonation(FoodDonationDto donation) { this.donation = donation; }

        public double getMatchScore() { return matchScore; }
        public void setMatchScore(double matchScore) { this.matchScore = matchScore; }

        public String getMatchReason() { return matchReason; }
        public void setMatchReason(String matchReason) { this.matchReason = matchReason; }

        public String getUrgencyLevel() { return urgencyLevel; }
        public void setUrgencyLevel(String urgencyLevel) { this.urgencyLevel = urgencyLevel; }
    }

    public String getModelUsed() { return modelUsed; }
    public void setModelUsed(String modelUsed) { this.modelUsed = modelUsed; }

    public boolean isLiveCloudAi() { return liveCloudAi; }
    public void setLiveCloudAi(boolean liveCloudAi) { this.liveCloudAi = liveCloudAi; }

    public String getSummaryRationale() { return summaryRationale; }
    public void setSummaryRationale(String summaryRationale) { this.summaryRationale = summaryRationale; }

    public List<RecommendedItem> getRecommendations() { return recommendations; }
    public void setRecommendations(List<RecommendedItem> recommendations) { this.recommendations = recommendations; }
}
