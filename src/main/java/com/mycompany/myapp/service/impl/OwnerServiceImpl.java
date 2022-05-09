package com.mycompany.myapp.service.impl;

import com.mycompany.myapp.domain.Owner;
import com.mycompany.myapp.repository.OwnerRepository;
import com.mycompany.myapp.service.OwnerService;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link Owner}.
 */
@Service
@Transactional
public class OwnerServiceImpl implements OwnerService {

    private final Logger log = LoggerFactory.getLogger(OwnerServiceImpl.class);

    private final OwnerRepository ownerRepository;

    public OwnerServiceImpl(OwnerRepository ownerRepository) {
        this.ownerRepository = ownerRepository;
    }

    @Override
    public Owner save(Owner owner) {
        log.debug("Request to save Owner : {}", owner);
        return ownerRepository.save(owner);
    }

    @Override
    public Owner update(Owner owner) {
        log.debug("Request to save Owner : {}", owner);
        return ownerRepository.save(owner);
    }

    @Override
    public Optional<Owner> partialUpdate(Owner owner) {
        log.debug("Request to partially update Owner : {}", owner);

        return ownerRepository
            .findById(owner.getId())
            .map(existingOwner -> {
                if (owner.getFirstName() != null) {
                    existingOwner.setFirstName(owner.getFirstName());
                }
                if (owner.getLastName() != null) {
                    existingOwner.setLastName(owner.getLastName());
                }
                if (owner.getEmail() != null) {
                    existingOwner.setEmail(owner.getEmail());
                }

                return existingOwner;
            })
            .map(ownerRepository::save);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Owner> findAll() {
        log.debug("Request to get all Owners");
        return ownerRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Owner> findOne(Long id) {
        log.debug("Request to get Owner : {}", id);
        return ownerRepository.findById(id);
    }

    @Override
    public void delete(Long id) {
        log.debug("Request to delete Owner : {}", id);
        ownerRepository.deleteById(id);
    }
}
