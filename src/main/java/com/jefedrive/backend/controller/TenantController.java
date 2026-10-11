
package com.jefedrive.backend.controller;

import com.jefedrive.backend.entity.Tenant;
import com.jefedrive.backend.service.TenantService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tenants")
public class TenantController {

    private final TenantService tenantService;

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @GetMapping
    public List<Tenant> getAllTenants() {
        return tenantService.getAllTenants();
    }

    @GetMapping("/{id}")
    public Tenant getTenantById(@PathVariable Long id) {
        return tenantService.getTenantById(id);
    }

    @PostMapping
    public Tenant createTenant(@Valid @RequestBody Tenant tenant) {
        return tenantService.createTenant(tenant);
    }

    @PutMapping("/{id}")
    public Tenant updateTenant(
            @PathVariable Long id,
            @Valid @RequestBody Tenant tenant) {
        return tenantService.updateTenant(id, tenant);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTenant(@PathVariable Long id) {
        tenantService.deleteTenant(id);
        return ResponseEntity.noContent().build();
    }
}

