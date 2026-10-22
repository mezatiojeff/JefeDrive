
package com.jefedrive.backend.controller;

import com.jefedrive.backend.entity.Repair;
import com.jefedrive.backend.service.RepairService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/repairs")
public class RepairController {

    private final RepairService repairService;

    public RepairController(RepairService repairService) {
        this.repairService = repairService;
    }

    @PostMapping
    public Repair createRepair(
            @RequestParam Long vehicleId,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate repairDate,
            @RequestParam String description,
            @RequestParam BigDecimal cost,
            @RequestParam(required = false) String notes) {

        return repairService.createRepair(
                vehicleId, repairDate, description, cost, notes);
    }

    @GetMapping
    public List<Repair> getAllRepairs() {
        return repairService.getAllRepairs();
    }

    @GetMapping("/vehicle/{vehicleId}")
    public List<Repair> getRepairsByVehicle(
            @PathVariable Long vehicleId) {

        return repairService.getRepairsByVehicle(vehicleId);
    }

    @PutMapping("/{repairId}/complete")
    public Repair completeRepair(@PathVariable Long repairId) {
        return repairService.completeRepair(repairId);
    }
}

