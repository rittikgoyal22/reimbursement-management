package com.etd.reimbursement_management.service.classes;

import com.etd.reimbursement_management.dao.ReimbursementTypeRepo;
import com.etd.reimbursement_management.dto.ReimbursementTypeResponseDTO;
import com.etd.reimbursement_management.entity.ReimbursementType;
import com.etd.reimbursement_management.mapper.ReimbursementTypeMapper;
import com.etd.reimbursement_management.service.interfaces.ReimbursementTypeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReimbursementTypeServiceImpl implements ReimbursementTypeService {

    private final Logger logger = LoggerFactory.getLogger(ReimbursementTypeServiceImpl.class);
    private final ReimbursementTypeRepo reimbursementTypeRepo;
    private final ReimbursementTypeMapper reimbursementTypeMapper;

    public ReimbursementTypeServiceImpl(ReimbursementTypeRepo reimbursementTypeRepo, ReimbursementTypeMapper reimbursementTypeMapper) {
        this.reimbursementTypeRepo = reimbursementTypeRepo;
        this.reimbursementTypeMapper = reimbursementTypeMapper;
    }

    @Override
    public List<ReimbursementTypeResponseDTO> getAllReimbursementType() {
        logger.info("Inside Reimbursement Type Service :: getAllReimbursementType");
        List<ReimbursementType> reimbursementTypes = reimbursementTypeRepo.findAll();
        return reimbursementTypeMapper.mapListOfReimbursementTypeToReimbursementTypeResponseDTO(reimbursementTypes);
    }

}
