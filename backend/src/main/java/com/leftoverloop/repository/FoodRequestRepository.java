package com.leftoverloop.repository;

import com.leftoverloop.entity.FoodRequest;
import com.leftoverloop.entity.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FoodRequestRepository extends JpaRepository<FoodRequest, Long> {

    List<FoodRequest> findByDonationIdOrderByRequestedAtDesc(Long donationId);

    List<FoodRequest> findByReceiverIdOrderByRequestedAtDesc(Long receiverId);

    @Query("SELECT r FROM FoodRequest r WHERE r.donation.donor.id = :donorId ORDER BY r.requestedAt DESC")
    List<FoodRequest> findIncomingRequestsForDonor(@Param("donorId") Long donorId);

    @Query("SELECT r FROM FoodRequest r WHERE r.donation.id = :donationId AND r.status IN :statuses")
    List<FoodRequest> findByDonationIdAndStatusIn(@Param("donationId") Long donationId, @Param("statuses") List<RequestStatus> statuses);

    @Query("SELECT COALESCE(SUM(r.requestedQuantity), 0) FROM FoodRequest r WHERE r.donation.id = :donationId AND r.status IN :statuses")
    Integer sumReservedQuantityForDonation(@Param("donationId") Long donationId, @Param("statuses") List<RequestStatus> statuses);

    Optional<FoodRequest> findByCollectionCode(String collectionCode);

    List<FoodRequest> findByStatus(RequestStatus status);
}
