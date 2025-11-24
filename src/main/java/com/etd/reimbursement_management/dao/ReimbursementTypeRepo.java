package com.etd.reimbursement_management.dao;

import com.etd.reimbursement_management.entity.ReimbursementType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReimbursementTypeRepo extends JpaRepository<ReimbursementType, Long> {

}
