package com.hussain.workflow_orchestrator.web;

import static com.hussain.workflow_orchestrator.constant.WorkflowRoutes.TRIGGER_BY_NAME;

import com.hussain.workflow_orchestrator.service.WorkflowTriggerService;
import com.hussain.workflow_orchestrator.service.WorkflowTriggerService.WorkflowNotFoundException;
import com.hussain.workflow_orchestrator.web.dto.TriggerResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WorkflowTriggerController {

    private final WorkflowTriggerService service;

    public WorkflowTriggerController(WorkflowTriggerService service) {
        this.service = service;
    }

    @PostMapping(TRIGGER_BY_NAME)
    public ResponseEntity<TriggerResponse> triggerByName(@PathVariable String workflowName) {
        if (!StringUtils.hasText(workflowName)) {
            // Path variable can't really be blank, but defensive
            throw new WorkflowNotFoundException(workflowName);
        }
        TriggerResponse response = service.triggerByName(workflowName);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
    }
}