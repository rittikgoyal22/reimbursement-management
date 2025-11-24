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
public class ReimbursementRequestDTO {

    private Long travelRequestId;

    private Long requestRaisedByEmployeeId;

    private Long reimbursementTypeId;

    private String invoiceNo;

    private Date invoiceDate;

    private Long invoiceAmount;

}
