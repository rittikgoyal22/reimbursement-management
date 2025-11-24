package com.etd.reimbursement_management.controller;

import com.etd.reimbursement_management.dto.ReimbursementTypeResponseDTO;
import com.etd.reimbursement_management.service.interfaces.ReimbursementTypeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/reimbursements/types")
public class ReimbursementTypeController {

    private final Logger logger = LoggerFactory.getLogger(ReimbursementTypeController.class);
    private final ReimbursementTypeService reimbursementTypeService;

    public ReimbursementTypeController(ReimbursementTypeService reimbursementTypeService) {
        this.reimbursementTypeService = reimbursementTypeService;
    }

    @GetMapping()
    public ResponseEntity<List<ReimbursementTypeResponseDTO>> getAllReimbursementType()
    {
        logger.info("Inside Reimbursement Controller :: getAllReimbursementType");
        List<ReimbursementTypeResponseDTO> reimbursementTypes = reimbursementTypeService.getAllReimbursementType();
        return ResponseEntity.ok(reimbursementTypes);
    }

}
