package com.etd.reimbursement_management.config;

import com.etd.reimbursement_management.dao.ReimbursementTypeRepo;
import com.etd.reimbursement_management.entity.ReimbursementType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements ApplicationRunner {

    private final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    private final ReimbursementTypeRepo reimbursementTypeRepo;

    public DataInitializer(ReimbursementTypeRepo reimbursementTypeRepo) {
        this.reimbursementTypeRepo = reimbursementTypeRepo;
    }

    @Override
    public void run(ApplicationArguments args) {
        seedReimbursementTypes();
    }

    private void seedReimbursementTypes() {
        if (reimbursementTypeRepo.count() > 0) {
            logger.info("DataInitializer :: Reimbursement types already seeded, skipping.");
            return;
        }
        List<String> typeNames = List.of("Food", "Water", "Laundry", "LocalTravel");
        typeNames.forEach(name ->
                reimbursementTypeRepo.save(ReimbursementType.builder().type(name).build())
        );
        logger.info("DataInitializer :: Seeded {} reimbursement types.", typeNames.size());
    }
}
