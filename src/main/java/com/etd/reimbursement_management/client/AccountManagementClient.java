package com.etd.reimbursement_management.client;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "account-management", url = "${account.management.service.base_url}" + "${account.management.service.url}")
public interface AccountManagementClient {

    @GetMapping("employees/{id}")
    ObjectNode getEmployeeById(@PathVariable("id") Long id);

}
