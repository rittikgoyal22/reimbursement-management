package com.etd.reimbursement_management.service.interfaces;

import com.etd.reimbursement_management.dto.ProcessReimbursementDTO;
import com.etd.reimbursement_management.dto.ReimbursementRequestDTO;
import com.etd.reimbursement_management.dto.ReimbursementResponseDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ReimbursementRequestService {

    List<ReimbursementResponseDTO> getAllReimbursementsByTravelRequestId(Long travelRequestId);

    ReimbursementResponseDTO getReimbursementByReimbursementId(Long reimbursementId);

    ReimbursementResponseDTO addReimbursement(ReimbursementRequestDTO reimbursementRequestDTO, MultipartFile pdfFile);

    ReimbursementResponseDTO processReimbursement(Long reimbursementId, ProcessReimbursementDTO processReimbursementDTO);

    List<ReimbursementResponseDTO> getMyReimbursements();

}
