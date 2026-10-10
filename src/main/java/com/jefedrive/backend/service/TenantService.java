
package com.jefedrive.backend.service;

import com.jefedrive.backend.entity.Tenant;
import com.jefedrive.backend.repository.TenantRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TenantService {

    private final TenantRepository tenantRepository;

    public TenantService(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    public List<Tenant> getAllTenants() {
        return tenantRepository.findAll();
    }

    public Tenant getTenantById(Long id) {
        return tenantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tenant not found"));
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
        tenantRepository.delete(tenant);
    }
}

