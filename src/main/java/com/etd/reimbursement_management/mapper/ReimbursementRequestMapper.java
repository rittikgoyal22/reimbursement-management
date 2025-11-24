package com.etd.reimbursement_management.mapper;

import com.etd.reimbursement_management.dto.ProcessReimbursementDTO;
import com.etd.reimbursement_management.dto.ReimbursementRequestDTO;
import com.etd.reimbursement_management.dto.ReimbursementResponseDTO;
import com.etd.reimbursement_management.entity.ReimbursementRequest;
import com.etd.reimbursement_management.entity.ReimbursementType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.Date;
import java.util.List;

import static com.etd.reimbursement_management.constant.AppConstant.NEW;

@Component
public class ReimbursementRequestMapper {

    private final Logger logger = LoggerFactory.getLogger(ReimbursementRequestMapper.class);

    public List<ReimbursementResponseDTO> mapListOfReimbursementToListOfReimbursementResponseDTO(List<ReimbursementRequest> reimbursementList) {
        logger.info("Inside Reimbursement Request Mapper :: mapListOfReimbursementToListOfReimbursementRequestDTO");
        return reimbursementList.stream().map(this::mapReimbursementToReimbursementResponseDTO).toList();
    }

    public ReimbursementResponseDTO mapReimbursementToReimbursementResponseDTO(ReimbursementRequest reimbursement) {
        logger.info("Inside Reimbursement Request Mapper :: mapReimbursementToReimbursementRequestDTO");
        return ReimbursementResponseDTO
                .builder()
                .id(reimbursement.getId())
                .travelRequestId(reimbursement.getTravelRequestId())
                .requestRaisedByEmployeeId(reimbursement.getRequestRaisedByEmployeeId())
                .requestDate(reimbursement.getRequestDate())
                .reimbursementType(reimbursement.getReimbursementType().getType())
                .invoiceNo(reimbursement.getInvoiceNo())
                .invoiceDate(reimbursement.getInvoiceDate())
                .invoiceAmount(reimbursement.getInvoiceAmount())
                .documentUrl(reimbursement.getDocumentUrl())
                .requestProcessedOn(reimbursement.getRequestProcessedOn())
                .requestProcessedByEmployeeId(reimbursement.getRequestProcessedByEmployeeId())
                .status(reimbursement.getStatus())
                .remarks(reimbursement.getRemarks())
                .build();
    }

    public ReimbursementRequest mapReimbursementRequestDtoToReimbursement(ReimbursementRequestDTO reimbursementRequestDTO, String uniqueFileName, ReimbursementType reimbursementType) {
        logger.info("Inside Reimbursement Request Mapper :: mapReimbursementRequestDtoToReimbursement");
        return ReimbursementRequest
                .builder()
                .travelRequestId(reimbursementRequestDTO.getTravelRequestId())
                .requestRaisedByEmployeeId(reimbursementRequestDTO.getRequestRaisedByEmployeeId())
                .requestDate(new Date(System.currentTimeMillis()))
                .reimbursementType(reimbursementType)
                .invoiceNo(reimbursementRequestDTO.getInvoiceNo())
                .invoiceDate(reimbursementRequestDTO.getInvoiceDate())
                .invoiceAmount(reimbursementRequestDTO.getInvoiceAmount())
                .documentUrl(uniqueFileName)
                .status(NEW)
                .build();
    }

    public ReimbursementRequest mapProcessedRequestToReimbursement(ProcessReimbursementDTO processReimbursementDTO, ReimbursementRequest reimbursementRequest)
    {
        logger.info("Inside Reimbursement Request Mapper :: mapProcessedRequestToReimbursement");
        return ReimbursementRequest
                .builder()
                .id(reimbursementRequest.getId())
                .travelRequestId(reimbursementRequest.getTravelRequestId())
                .requestRaisedByEmployeeId(reimbursementRequest.getRequestRaisedByEmployeeId())
                .requestDate(reimbursementRequest.getRequestDate())
                .reimbursementType(reimbursementRequest.getReimbursementType())
                .invoiceNo(reimbursementRequest.getInvoiceNo())
                .invoiceDate(reimbursementRequest.getInvoiceDate())
                .invoiceAmount(reimbursementRequest.getInvoiceAmount())
                .documentUrl(reimbursementRequest.getDocumentUrl())
                .status(processReimbursementDTO.getStatus())
                .requestProcessedOn(new Date(System.currentTimeMillis()))
                .requestProcessedByEmployeeId(processReimbursementDTO.getRequestProcessedByEmployeeId())
                .remarks(processReimbursementDTO.getRemarks())
                .build();
    }

}
