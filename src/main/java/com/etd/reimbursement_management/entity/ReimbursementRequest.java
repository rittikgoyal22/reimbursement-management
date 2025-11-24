package com.etd.reimbursement_management.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Date;

@Entity
@Table(name = "reimbursement_requests")
@Builder
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReimbursementRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "travel_request_id")
    private Long travelRequestId;

    @Column(name = "request_raised_by_employee_id")
    private Long requestRaisedByEmployeeId;

    @Column(name = "request_date")
    private Date requestDate;

    @ManyToOne
    @JoinColumn(name="reimbursement_type_id", referencedColumnName = "id")
    private ReimbursementType reimbursementType;

    @Column(name = "invoice_no")
    private String invoiceNo;

    @Column(name = "invoice_date")
    private Date invoiceDate;

    @Column(name = "invoice_amount")
    private Long invoiceAmount;

    @Column(name = "document_url")
    private String documentUrl;

    @Column(name = "request_processed_on")
    private Date requestProcessedOn;

    @Column(name = "request_processed_by_employee_id")
    private Long requestProcessedByEmployeeId;

    @Column(name = "status")
    private String status;

    @Column(name = "remarks")
    private String remarks;

}
