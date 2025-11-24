package com.etd.reimbursement_management.dao;

import com.etd.reimbursement_management.entity.ReimbursementRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReimbursementRequsetRepo extends JpaRepository<ReimbursementRequest, Long> {

    @Query("SELECT rr from ReimbursementRequest rr where rr.travelRequestId = :travelRequestId")
    List<ReimbursementRequest> findByTravelRequestId(Long travelRequestId);

}
