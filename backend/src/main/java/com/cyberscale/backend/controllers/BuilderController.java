package com.cyberscale.backend.controllers;

import com.cyberscale.backend.dto.builder.TopologyRequest;
import com.cyberscale.backend.services.BuilderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/builder")
@CrossOrigin(origins = "*")
public class BuilderController {

    private final BuilderService builderService;

    public BuilderController(BuilderService builderService) {
        this.builderService = builderService;
    }

    @PostMapping("/deploy")
    public ResponseEntity<?> deployNetwork(@RequestBody TopologyRequest request) {
        try {
            String containerId = builderService.deployTopology(request);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "deploymentId", containerId,
                "message", "Environnement déployé avec succès"
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false, 
                "error", e.getMessage()
            ));
        }
    }
}