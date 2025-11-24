package com.etd.reimbursement_management.dto;

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
public class ProcessReimbursementDTO {

    private Long requestProcessedByEmployeeId;

    private String status;

    private String remarks;

}
