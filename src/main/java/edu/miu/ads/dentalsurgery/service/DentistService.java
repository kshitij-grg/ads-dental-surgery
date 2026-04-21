package edu.miu.ads.dentalsurgery.service;

import java.util.List;

import edu.miu.ads.dentalsurgery.dto.DentistRequest;
import edu.miu.ads.dentalsurgery.dto.DentistResponse;
import edu.miu.ads.dentalsurgery.exception.ResourceNotFoundException;
import edu.miu.ads.dentalsurgery.mapper.DentistMapper;
import edu.miu.ads.dentalsurgery.repository.DentistRepository;
import org.springframework.stereotype.Service;

@Service
public class DentistService {

    private final DentistRepository dentistRepository;
    private final DentistMapper dentistMapper;

    public DentistService(DentistRepository dentistRepository, DentistMapper dentistMapper) {
        this.dentistRepository = dentistRepository;
        this.dentistMapper = dentistMapper;
    }

    public List<DentistResponse> findAll() {
        return dentistRepository.findAll()
                .stream()
                .map(dentistMapper::toResponse)
                .toList();
    }

    public DentistResponse findById(Long id) {
        return dentistRepository.findById(id)
                .map(dentistMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Dentist not found: " + id));
    }

    public DentistResponse create(DentistRequest request) {
        dentistRepository.findByLicenseNumber(request.licenseNumber())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("License number already exists: " + request.licenseNumber());
                });

        return dentistMapper.toResponse(dentistRepository.save(dentistMapper.toEntity(request)));
    }

    public DentistResponse update(Long id, DentistRequest request) {
        var dentist = dentistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Dentist not found: " + id));

        dentistRepository.findByLicenseNumber(request.licenseNumber())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("License number already exists: " + request.licenseNumber());
                });

        dentistMapper.updateEntity(dentist, request);
        return dentistMapper.toResponse(dentistRepository.save(dentist));
    }
}
