package com.etd.reimbursement_management.controller;

import com.etd.reimbursement_management.dto.ProcessReimbursementDTO;
import com.etd.reimbursement_management.dto.ReimbursementRequestDTO;
import com.etd.reimbursement_management.dto.ReimbursementResponseDTO;
import com.etd.reimbursement_management.service.interfaces.ReimbursementRequestService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("api/reimbursements")
@CrossOrigin
public class ReimbursementRequestController {

    private final Logger logger = LoggerFactory.getLogger(ReimbursementRequestController.class);
    private final ReimbursementRequestService reimbursementRequestService;

    public ReimbursementRequestController(ReimbursementRequestService reimbursementRequestService) {
        this.reimbursementRequestService = reimbursementRequestService;
    }

    @GetMapping("/my")
    public ResponseEntity<List<ReimbursementResponseDTO>> getMyReimbursements() {
        logger.info("Inside Reimbursement Request Controller :: getMyReimbursements");
        List<ReimbursementResponseDTO> reimbursements = reimbursementRequestService.getMyReimbursements();
        return ResponseEntity.ok(reimbursements);
    }

    @GetMapping("/{travelRequestId}/requests")
    public ResponseEntity<List<ReimbursementResponseDTO>> getAllReimbursementsByTravelRequestId(@PathVariable("travelRequestId") Long travelRequestId)
    {
        logger.info("Inside Reimbursement Request Controller :: getAllReimbursementsByTravelRequestId");
        List<ReimbursementResponseDTO> reimbursements = reimbursementRequestService.getAllReimbursementsByTravelRequestId(travelRequestId);
        return ResponseEntity.ok(reimbursements);
    }

    @GetMapping("/{reimbursementId}")
    public ResponseEntity<ReimbursementResponseDTO> getReimbursementByReimbursementId(@PathVariable("reimbursementId") Long reimbursementId)
    {
        logger.info("Inside Reimbursement Request Controller :: getReimbursementByReimbursementId");
        ReimbursementResponseDTO reimbursement = reimbursementRequestService.getReimbursementByReimbursementId(reimbursementId);
        return ResponseEntity.ok(reimbursement);
    }

    @PostMapping(path = "/add", consumes = "multipart/form-data")
    public ResponseEntity<ReimbursementResponseDTO> addReimbursement(@RequestPart("reimbursementRequestDTO") ReimbursementRequestDTO reimbursementRequestDTO, @RequestPart("pdfFile") MultipartFile pdfFile)
    {
        logger.info("Inside Reimbursement Request Controller :: addReimbursement");
        ReimbursementResponseDTO reimbursement = reimbursementRequestService.addReimbursement(reimbursementRequestDTO, pdfFile);
        return ResponseEntity.ok(reimbursement);
    }

    @PutMapping("/{reimbursementId}/process")
    public ResponseEntity<ReimbursementResponseDTO> processReimbursement(@PathVariable("reimbursementId") Long reimbursementId, @RequestBody ProcessReimbursementDTO processReimbursementDTO)
    {
        logger.info("Inside Reimbursement Request Controller :: processReimbursement");
        ReimbursementResponseDTO reimbursement = reimbursementRequestService.processReimbursement(reimbursementId,processReimbursementDTO);
        return ResponseEntity.ok(reimbursement);
    }

}
