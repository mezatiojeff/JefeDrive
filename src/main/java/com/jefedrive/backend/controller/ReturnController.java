package com.jefedrive.backend.controller;

import com.jefedrive.backend.entity.Return;
import com.jefedrive.backend.service.ReturnService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/returns")
public class ReturnController {

    private final ReturnService returnService;

    public ReturnController(ReturnService returnService) {
        this.returnService = returnService;
    }

    @PostMapping
    public Return processReturn(
            @RequestParam Long rentalId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime actualReturnDate,
            @RequestParam BigDecimal mileage,
            @RequestParam Boolean hasDamage,
            @RequestParam(required = false) String observations) {

        return returnService.processReturn(
                rentalId,
                actualReturnDate,
                mileage,
                hasDamage,
                observations
        );
    }

    @GetMapping
    public List<Return> getAllReturns() {
        return returnService.getAllReturns();
    }

    @GetMapping("/{id}")
    public Return getReturnById(@PathVariable Long id) {
        return returnService.getReturnById(id);
    }

    @PostMapping("/{returnId}/damages")
    public com.jefedrive.backend.entity.Damage recordDamage(
            @PathVariable Long returnId,
            @RequestParam String description,
            @RequestParam BigDecimal cost) {

        return returnService.recordDamage(returnId, description, cost);
    }

    @PutMapping("/{returnId}/complete")
    public Return completeReturn(@PathVariable Long returnId) {
        return returnService.completeReturn(returnId);
    }
}
