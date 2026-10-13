package com.leftoverloop.repository;

import com.leftoverloop.entity.FoodDonation;
import com.leftoverloop.entity.DonationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FoodDonationRepository extends JpaRepository<FoodDonation, Long> {

    List<FoodDonation> findByDonorIdOrderByCreatedAtDesc(Long donorId);

    List<FoodDonation> findByStatusInOrderByExpiryDateAsc(List<DonationStatus> statuses);

    @Query("SELECT f FROM FoodDonation f WHERE f.status IN :statuses AND f.expiryDate > :now ORDER BY f.expiryDate ASC")
    List<FoodDonation> findActiveDonations(@Param("statuses") List<DonationStatus> statuses, @Param("now") LocalDateTime now);

    @Query("SELECT f FROM FoodDonation f WHERE f.expiryDate <= :now AND f.status NOT IN ('EXPIRED', 'COMPLETED', 'CANCELLED')")
    List<FoodDonation> findNewlyExpiredDonations(@Param("now") LocalDateTime now);

    List<FoodDonation> findByDonorRoleOrderByCreatedAtDesc(com.leftoverloop.entity.Role role);
}
