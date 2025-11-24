package com.etd.reimbursement_management.dto;

import java.sql.Date;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReimbursementResponseDTO {

    private Long id;

    private Long travelRequestId;

    private Long requestRaisedByEmployeeId;

    private Date requestDate;

    private String reimbursementType;

    private String invoiceNo;

    private Date invoiceDate;

    private Long invoiceAmount;

    private String documentUrl;

    private Date requestProcessedOn;

    private Long requestProcessedByEmployeeId;

    private String status;

    private String remarks;

}
