
package com.jefedrive.backend.service;

import com.jefedrive.backend.entity.Tenant;
import com.jefedrive.backend.repository.TenantRepository;
import org.springframework.stereotype.Service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.jefedrive.backend.repository.RentalRepository;

@Service
public class TenantService {

    private final TenantRepository tenantRepository;

    private final RentalRepository rentalRepository;

    public TenantService(
            TenantRepository tenantRepository,
            RentalRepository rentalRepository) {
        this.tenantRepository = tenantRepository;
        this.rentalRepository = rentalRepository;
    }

    public List<Tenant> getAllTenants() {
        return tenantRepository.findAll();
    }

    public Tenant getTenantById(Long id) {
        return tenantRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Tenant not found with id: " + id
                ));
    }

    public Tenant createTenant(Tenant tenant) {
        return tenantRepository.save(tenant);
    }

    public Tenant updateTenant(Long id, Tenant tenantDetails) {
        Tenant tenant = getTenantById(id);

        tenant.setFirstName(tenantDetails.getFirstName());
        tenant.setLastName(tenantDetails.getLastName());
        tenant.setEmail(tenantDetails.getEmail());
        tenant.setPhone(tenantDetails.getPhone());
        tenant.setNationalId(tenantDetails.getNationalId());
        tenant.setDriverLicense(tenantDetails.getDriverLicense());
        tenant.setDriverLicenseExpiry(tenantDetails.getDriverLicenseExpiry());
        tenant.setDateOfBirth(tenantDetails.getDateOfBirth());
        tenant.setAddress(tenantDetails.getAddress());

        return tenantRepository.save(tenant);
    }

    public void deleteTenant(Long id) {
        Tenant tenant = getTenantById(id);

        if (rentalRepository.existsByTenantId(id)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cannot delete a tenant who has rental history"
            );
        }

        tenantRepository.delete(tenant);
    }
}

