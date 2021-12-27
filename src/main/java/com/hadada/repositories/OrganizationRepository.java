package com.hadada.repositories;

import com.hadada.modal.Organization;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface OrganizationRepository extends CrudRepository<Organization, Long> {
    List<Organization> findAll();
    List<Organization> findByOfficialEmail(String officialEmail);
    List<Organization> findByOrganizationKey(Long organizationKey);
}
