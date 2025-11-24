package com.etd.reimbursement_management.client;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "travel-planner", url = "${travel.planner.service.base_url}"+"${travel.planner.service.url}")
public interface TravelPlannerClient {

    @GetMapping("travelrequests/{trid}")
    ObjectNode getTravelRequestDetailByTravelRequestId(@PathVariable("trid") Long trid);

}
