package com.etd.reimbursement_management.service.classes;

import com.etd.reimbursement_management.client.AccountManagementClient;
import com.etd.reimbursement_management.client.TravelPlannerClient;
import com.etd.reimbursement_management.dao.ReimbursementRequsetRepo;
import com.etd.reimbursement_management.dao.ReimbursementTypeRepo;
import com.etd.reimbursement_management.dto.ProcessReimbursementDTO;
import com.etd.reimbursement_management.dto.ReimbursementRequestDTO;
import com.etd.reimbursement_management.dto.ReimbursementResponseDTO;
import com.etd.reimbursement_management.entity.ReimbursementRequest;
import com.etd.reimbursement_management.entity.ReimbursementType;
import com.etd.reimbursement_management.exception.BadRequestException;
import com.etd.reimbursement_management.exception.DocumentSizeLimitExceededException;
import com.etd.reimbursement_management.exception.IllegalArgumentException;
import com.etd.reimbursement_management.exception.NotFoundException;
import com.etd.reimbursement_management.mapper.ReimbursementRequestMapper;
import com.etd.reimbursement_management.service.interfaces.ReimbursementRequestService;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Date;
import java.util.List;
import java.util.Locale;

import static com.etd.reimbursement_management.constant.AppConstant.APPROVED;
import static com.etd.reimbursement_management.constant.AppConstant.EMPTY_REMARKS;
import static com.etd.reimbursement_management.constant.AppConstant.EXCEED_BUDGET_LIMIT_FOR_REIMBURSEMENT;
import static com.etd.reimbursement_management.constant.AppConstant.FOOD;
import static com.etd.reimbursement_management.constant.AppConstant.FROM_DATE;
import static com.etd.reimbursement_management.constant.AppConstant.INVALID_FOOD_WATER_INVOICE_AMOUNT;
import static com.etd.reimbursement_management.constant.AppConstant.INVALID_INVOICE_DATE;
import static com.etd.reimbursement_management.constant.AppConstant.INVALID_LAUNDRY_INVOICE_AMOUNT;
import static com.etd.reimbursement_management.constant.AppConstant.INVALID_LOCAL_TRAVEL_INVOICE_AMOUNT;
import static com.etd.reimbursement_management.constant.AppConstant.INVALID_PDF_FORMAT;
import static com.etd.reimbursement_management.constant.AppConstant.INVALID_REQUEST_EMPLOYEE_ID;
import static com.etd.reimbursement_management.constant.AppConstant.INVALID_STATUS;
import static com.etd.reimbursement_management.constant.AppConstant.INVALID_TRAVEL_DESK_EXEC_ID;
import static com.etd.reimbursement_management.constant.AppConstant.INVOICE_AMOUNT;
import static com.etd.reimbursement_management.constant.AppConstant.INVOICE_DATE;
import static com.etd.reimbursement_management.constant.AppConstant.LAUNDRY;
import static com.etd.reimbursement_management.constant.AppConstant.LOCAL_TRAVEL;
import static com.etd.reimbursement_management.constant.AppConstant.PDF_FILE;
import static com.etd.reimbursement_management.constant.AppConstant.PDF_SIZE_EXCEED;
import static com.etd.reimbursement_management.constant.AppConstant.RAISED_BY_EMPLOYEE_ID;
import static com.etd.reimbursement_management.constant.AppConstant.REIMBURSEMENT_ID;
import static com.etd.reimbursement_management.constant.AppConstant.REIMBURSEMENT_NOT_FOUND;
import static com.etd.reimbursement_management.constant.AppConstant.REIMBURSEMENT_NOT_FOUND_FOR_TRAVEL_REQUEST_ID;
import static com.etd.reimbursement_management.constant.AppConstant.REIMBURSEMENT_REQUEST_ALREADY_PROCESSED;
import static com.etd.reimbursement_management.constant.AppConstant.REIMBURSEMENT_TYPE_ID;
import static com.etd.reimbursement_management.constant.AppConstant.REIMBURSEMENT_TYPE_NOT_FOUND;
import static com.etd.reimbursement_management.constant.AppConstant.REJECTED;
import static com.etd.reimbursement_management.constant.AppConstant.REMARKS;
import static com.etd.reimbursement_management.constant.AppConstant.REQUEST_PROCESSED_BY_EMP_ID;
import static com.etd.reimbursement_management.constant.AppConstant.REQUEST_RAISED_BY_EMP_ID;
import static com.etd.reimbursement_management.constant.AppConstant.ROLE;
import static com.etd.reimbursement_management.constant.AppConstant.STATUS;
import static com.etd.reimbursement_management.constant.AppConstant.TO_DATE;
import static com.etd.reimbursement_management.constant.AppConstant.TRAVEL_DESK_EXE;
import static com.etd.reimbursement_management.constant.AppConstant.TRAVEL_REQUEST_ID;
import static com.etd.reimbursement_management.constant.AppConstant.TRAVEL_REQUEST_NOT_FOUND;
import static com.etd.reimbursement_management.constant.AppConstant.UNDERSCORE;
import static com.etd.reimbursement_management.constant.AppConstant.WATER;

@Service
public class ReimbursementRequestServiceImpl implements ReimbursementRequestService {

    private final Logger logger = LoggerFactory.getLogger(ReimbursementRequestServiceImpl.class);
    private final ReimbursementRequsetRepo reimbursementRequsetRepo;
    private final ReimbursementRequestMapper reimbursementRequestMapper;
    private final MessageSource messageSource;
    private final TravelPlannerClient travelPlannerClient;
    private final ReimbursementTypeRepo reimbursementTypeRepo;
    private final AccountManagementClient accountManagementClient;
    private final String uploadDir;

    public ReimbursementRequestServiceImpl(ReimbursementRequsetRepo reimbursementRequsetRepo,
                                           ReimbursementRequestMapper reimbursementRequestMapper,
                                           MessageSource messageSource,
                                           TravelPlannerClient travelPlannerClient,
                                           ReimbursementTypeRepo reimbursementTypeRepo,
                                           AccountManagementClient accountManagementClient,
                                           @Value("${app.upload.dir}") String uploadDir) {
        this.reimbursementRequsetRepo = reimbursementRequsetRepo;
        this.reimbursementRequestMapper = reimbursementRequestMapper;
        this.messageSource = messageSource;
        this.travelPlannerClient = travelPlannerClient;
        this.reimbursementTypeRepo = reimbursementTypeRepo;
        this.accountManagementClient = accountManagementClient;
        this.uploadDir = uploadDir;
    }

    private static final long MAX_PDF_SIZE = 262144;
    private static final List<String> VALID_STATUSES = List.of(APPROVED, REJECTED);

    @Override
    public List<ReimbursementResponseDTO> getAllReimbursementsByTravelRequestId(Long travelRequestId) {
        logger.info("Inside Reimbursement Request Service Impl :: getAllReimbursementsByTravelRequestId");
        List<ReimbursementRequest> reimbursementsList = reimbursementRequsetRepo.findByTravelRequestId(travelRequestId);
        if (ObjectUtils.isEmpty(reimbursementsList)) {
            throw new NotFoundException(
                    messageSource.getMessage(REIMBURSEMENT_NOT_FOUND_FOR_TRAVEL_REQUEST_ID, new Object[]{travelRequestId}, Locale.ENGLISH),
                    TRAVEL_REQUEST_ID);
        }
        return reimbursementRequestMapper.mapListOfReimbursementToListOfReimbursementResponseDTO(reimbursementsList);
    }

    @Override
    public ReimbursementResponseDTO getReimbursementByReimbursementId(Long reimbursementId) {
        logger.info("Inside Reimbursement Request Service Impl :: getReimbursementByReimbursementId");
        ReimbursementRequest reimbursement = reimbursementRequsetRepo.findById(reimbursementId)
                .orElseThrow(() -> new NotFoundException(
                        messageSource.getMessage(REIMBURSEMENT_NOT_FOUND, new Object[]{reimbursementId}, Locale.ENGLISH),
                        REIMBURSEMENT_ID));
        return reimbursementRequestMapper.mapReimbursementToReimbursementResponseDTO(reimbursement);
    }

    @Override
    @Transactional
    public ReimbursementResponseDTO addReimbursement(ReimbursementRequestDTO dto, MultipartFile pdfFile) {
        logger.info("Inside Reimbursement Request Service Impl :: addReimbursement");

        // 1. Validate PDF (size + content-type)
        validatePdfFile(pdfFile);

        // 2. Validate reimbursement type exists
        ReimbursementType reimbursementType = findReimbursementType(dto.getReimbursementTypeId());

        // 3. Validate travel request exists and employee ID matches — do this before budget checks
        ObjectNode travelRequest = getTravelRequest(dto.getTravelRequestId());
        validateTravelRequestEmployeeId(travelRequest.get(RAISED_BY_EMPLOYEE_ID).asLong(), dto.getRequestRaisedByEmployeeId());

        // 4. Validate invoice date is within travel request date range
        validateInvoiceDate(dto.getInvoiceDate(), travelRequest);

        // 5. Budget validation against previous requests for the same date
        List<ReimbursementRequest> previousRequests = reimbursementRequsetRepo.findByTravelRequestId(dto.getTravelRequestId());
        long prevFoodWater = calculatePreviousAmount(previousRequests, dto, FOOD, WATER);
        long prevLaundry = calculatePreviousAmount(previousRequests, dto, LAUNDRY);
        long prevLocalTravel = calculatePreviousAmount(previousRequests, dto, LOCAL_TRAVEL);
        validateBudgetAndAmount(dto, reimbursementType, prevFoodWater, prevLaundry, prevLocalTravel);

        // 6. Save DB record and PDF file atomically
        String uniqueFileName = System.currentTimeMillis() + UNDERSCORE + pdfFile.getOriginalFilename();
        ReimbursementRequest reimbursement = reimbursementRequestMapper.mapReimbursementRequestDtoToReimbursement(dto, uniqueFileName, reimbursementType);
        ReimbursementRequest saved = reimbursementRequsetRepo.save(reimbursement);
        savePdfFile(pdfFile, uniqueFileName);

        return reimbursementRequestMapper.mapReimbursementToReimbursementResponseDTO(saved);
    }

    @Override
    @Transactional
    public ReimbursementResponseDTO processReimbursement(Long reimbursementId, ProcessReimbursementDTO processReimbursementDTO) {
        logger.info("Inside Reimbursement Request Service Impl :: processReimbursement");

        String status = processReimbursementDTO.getStatus();
        String remarks = processReimbursementDTO.getRemarks();

        // 1. Validate status and remarks before hitting the DB
        validateStatusAndRemarks(status, remarks);

        // 2. Fetch the reimbursement request
        ReimbursementRequest reimbursementRequest = reimbursementRequsetRepo.findById(reimbursementId)
                .orElseThrow(() -> new NotFoundException(
                        messageSource.getMessage(REIMBURSEMENT_NOT_FOUND, new Object[]{reimbursementId}, Locale.ENGLISH),
                        REIMBURSEMENT_ID));

        // 3. Guard against processing an already-processed request
        if (VALID_STATUSES.contains(reimbursementRequest.getStatus())) {
            String processedOn = reimbursementRequest.getRequestProcessedOn() != null
                    ? reimbursementRequest.getRequestProcessedOn().toString().substring(0, 10)
                    : "unknown date";
            throw new IllegalArgumentException(
                    messageSource.getMessage(REIMBURSEMENT_REQUEST_ALREADY_PROCESSED, new Object[]{processedOn}, Locale.ENGLISH),
                    STATUS);
        }

        // 4. Validate processor is a TravelDeskExe
        validateRole(processReimbursementDTO.getRequestProcessedByEmployeeId());

        ReimbursementRequest reimbursement = reimbursementRequestMapper.mapProcessedRequestToReimbursement(processReimbursementDTO, reimbursementRequest);
        ReimbursementRequest savedReimbursement = reimbursementRequsetRepo.save(reimbursement);
        return reimbursementRequestMapper.mapReimbursementToReimbursementResponseDTO(savedReimbursement);
    }

    // ─── Private helpers ────────────────────────────────────────────────────────

    private void validatePdfFile(MultipartFile pdfFile) {
        if (pdfFile.getSize() > MAX_PDF_SIZE) {
            throw new DocumentSizeLimitExceededException(
                    messageSource.getMessage(PDF_SIZE_EXCEED, null, Locale.ENGLISH));
        }
        String contentType = pdfFile.getContentType();
        if (contentType == null || !contentType.equals("application/pdf")) {
            throw new BadRequestException(
                    messageSource.getMessage(INVALID_PDF_FORMAT, null, Locale.ENGLISH), PDF_FILE);
        }
    }

    private ReimbursementType findReimbursementType(Long typeId) {
        return reimbursementTypeRepo.findById(typeId)
                .orElseThrow(() -> new IllegalArgumentException(
                        messageSource.getMessage(REIMBURSEMENT_TYPE_NOT_FOUND, new Object[]{typeId}, Locale.ENGLISH),
                        REIMBURSEMENT_TYPE_ID));
    }

    private ObjectNode getTravelRequest(Long travelRequestId) {
        ObjectNode travelRequest;
        try {
            travelRequest = travelPlannerClient.getTravelRequestDetailByTravelRequestId(travelRequestId);
        } catch (Exception ex) {
            logger.warn("Failed to fetch travel request {}: {}", travelRequestId, ex.getMessage());
            throw new IllegalArgumentException(
                    messageSource.getMessage(TRAVEL_REQUEST_NOT_FOUND, new Object[]{travelRequestId}, Locale.ENGLISH),
                    TRAVEL_REQUEST_ID);
        }
        if (ObjectUtils.isEmpty(travelRequest)) {
            throw new IllegalArgumentException(
                    messageSource.getMessage(TRAVEL_REQUEST_NOT_FOUND, new Object[]{travelRequestId}, Locale.ENGLISH),
                    TRAVEL_REQUEST_ID);
        }
        return travelRequest;
    }

    private void validateTravelRequestEmployeeId(Long employeeIdInTravelRequest, Long requestRaisedByEmployeeId) {
        if (!employeeIdInTravelRequest.equals(requestRaisedByEmployeeId)) {
            logger.warn("Employee ID mismatch: travel request raised by {}, but reimbursement submitted by {}",
                    employeeIdInTravelRequest, requestRaisedByEmployeeId);
            throw new IllegalArgumentException(
                    messageSource.getMessage(INVALID_REQUEST_EMPLOYEE_ID, null, Locale.ENGLISH),
                    REQUEST_RAISED_BY_EMP_ID);
        }
    }

    private void validateInvoiceDate(Date invoiceDate, ObjectNode travelRequest) {
        Date from = Date.valueOf(travelRequest.get(FROM_DATE).asText().substring(0, 10));
        Date to = Date.valueOf(travelRequest.get(TO_DATE).asText().substring(0, 10));
        if (invoiceDate.before(from) || invoiceDate.after(to)) {
            throw new IllegalArgumentException(
                    messageSource.getMessage(
                            INVALID_INVOICE_DATE,
                            new Object[]{invoiceDate.toString().substring(0, 10),
                                    from.toString().substring(0, 10),
                                    to.toString().substring(0, 10)},
                            Locale.ENGLISH),
                    INVOICE_DATE);
        }
    }

    private long calculatePreviousAmount(List<ReimbursementRequest> previousRequests, ReimbursementRequestDTO dto, String... types) {
        long total = 0L;
        for (ReimbursementRequest req : previousRequests) {
            boolean isSameDate = req.getInvoiceDate().toString().substring(0, 10)
                    .equals(dto.getInvoiceDate().toString().substring(0, 10));
            if (isSameDate) {
                String type = req.getReimbursementType().getType();
                for (String t : types) {
                    if (type.equals(t)) {
                        total += req.getInvoiceAmount();
                    }
                }
            }
        }
        return total;
    }

    private void validateBudgetAndAmount(ReimbursementRequestDTO dto, ReimbursementType type,
                                         long prevFoodWater, long prevLaundry, long prevLocalTravel) {
        String t = type.getType();
        long amount = dto.getInvoiceAmount();
        switch (t) {
            case FOOD, WATER -> {
                validateAmountRange(amount, 1000, 1500, INVALID_FOOD_WATER_INVOICE_AMOUNT);
                validateDailyTotal(prevFoodWater, amount, 1500, dto.getInvoiceDate());
            }
            case LAUNDRY -> {
                validateAmountRange(amount, 250, 500, INVALID_LAUNDRY_INVOICE_AMOUNT);
                validateDailyTotal(prevLaundry, amount, 500, dto.getInvoiceDate());
            }
            case LOCAL_TRAVEL -> {
                validateAmountRange(amount, 0, 1000, INVALID_LOCAL_TRAVEL_INVOICE_AMOUNT);
                validateDailyTotal(prevLocalTravel, amount, 1000, dto.getInvoiceDate());
            }
            default -> logger.warn("Unknown reimbursement type: {}", t);
        }
    }

    private void validateAmountRange(long amount, long min, long max, String messageKey) {
        if (amount < min || amount > max) {
            throw new IllegalArgumentException(
                    messageSource.getMessage(messageKey, new Object[]{amount}, Locale.ENGLISH),
                    INVOICE_AMOUNT);
        }
    }

    private void validateDailyTotal(long previous, long current, long limit, Date invoiceDate) {
        if (previous + current > limit) {
            throw new IllegalArgumentException(
                    messageSource.getMessage(
                            EXCEED_BUDGET_LIMIT_FOR_REIMBURSEMENT,
                            new Object[]{invoiceDate.toString().substring(0, 10)},
                            Locale.ENGLISH),
                    INVOICE_AMOUNT);
        }
    }

    private void validateStatusAndRemarks(String status, String remarks) {
        if (!VALID_STATUSES.contains(status)) {
            throw new IllegalArgumentException(
                    messageSource.getMessage(INVALID_STATUS, null, Locale.ENGLISH), STATUS);
        }
        if (REJECTED.equals(status) && ObjectUtils.isEmpty(remarks)) {
            throw new IllegalArgumentException(
                    messageSource.getMessage(EMPTY_REMARKS, null, Locale.ENGLISH), REMARKS);
        }
    }

    private void validateRole(Long employeeId) {
        // Separate the Feign call from the role check so the catch block only
        // handles network/Feign failures — not the BadRequestException itself.
        String role;
        try {
            role = accountManagementClient.getEmployeeById(employeeId).get(ROLE).asText();
        } catch (Exception ex) {
            logger.warn("Failed to fetch employee {} from account-management: {}", employeeId, ex.getMessage());
            throw new BadRequestException(
                    messageSource.getMessage(INVALID_TRAVEL_DESK_EXEC_ID, null, Locale.ENGLISH),
                    REQUEST_PROCESSED_BY_EMP_ID);
        }
        if (ObjectUtils.isEmpty(role) || !TRAVEL_DESK_EXE.equals(role)) {
            logger.warn("Employee {} has role '{}' — not TravelDeskExe, cannot process reimbursement", employeeId, role);
            throw new BadRequestException(
                    messageSource.getMessage(INVALID_TRAVEL_DESK_EXEC_ID, null, Locale.ENGLISH),
                    REQUEST_PROCESSED_BY_EMP_ID);
        }
    }

    private void savePdfFile(MultipartFile pdfFile, String uniqueFileName) {
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            Path filePath = uploadPath.resolve(uniqueFileName);
            Files.write(filePath, pdfFile.getBytes());
        } catch (IOException e) {
            logger.error("Failed to save PDF file: {}", e.getMessage());
            throw new BadRequestException("Failed to save the uploaded PDF file.", PDF_FILE);
        }
    }
}
