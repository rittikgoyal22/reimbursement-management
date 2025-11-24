package com.etd.reimbursement_management.mapper;

import com.etd.reimbursement_management.dto.ReimbursementTypeResponseDTO;
import com.etd.reimbursement_management.entity.ReimbursementType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReimbursementTypeMapper {

    private final Logger logger = LoggerFactory.getLogger(ReimbursementTypeMapper.class);

    public List<ReimbursementTypeResponseDTO> mapListOfReimbursementTypeToReimbursementTypeResponseDTO(List<ReimbursementType> reimbursementTypes) {
        logger.info("Inside Reimbursement Type Mapper :: mapListOfReimbursementTypeToReimbursementTypeResponseDTO");
        return reimbursementTypes.stream().map(this::mapReimbursementTypeToReimbursementTypeResponseDTO).toList();
    }

    public ReimbursementTypeResponseDTO mapReimbursementTypeToReimbursementTypeResponseDTO(ReimbursementType reimbursementType)
    {
        logger.info("Inside Reimbursement Type Mapper :: mapReimbursementTypeToReimbursementTypeResponseDTO");
        return ReimbursementTypeResponseDTO
                .builder()
                .id(reimbursementType.getId())
                .type(reimbursementType.getType())
                .build();
    }

}
