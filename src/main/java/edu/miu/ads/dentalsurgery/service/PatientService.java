package edu.miu.ads.dentalsurgery.service;

import java.util.List;

import edu.miu.ads.dentalsurgery.dto.PatientRequest;
import edu.miu.ads.dentalsurgery.dto.PatientResponse;
import edu.miu.ads.dentalsurgery.exception.ResourceNotFoundException;
import edu.miu.ads.dentalsurgery.mapper.PatientMapper;
import edu.miu.ads.dentalsurgery.repository.PatientRepository;
import org.springframework.stereotype.Service;

@Service
public class PatientService {

    private final PatientRepository patientRepository;
    private final PatientMapper patientMapper;

    public PatientService(PatientRepository patientRepository, PatientMapper patientMapper) {
        this.patientRepository = patientRepository;
        this.patientMapper = patientMapper;
    }

    public List<PatientResponse> findAll() {
        return patientRepository.findAll()
                .stream()
                .map(patientMapper::toResponse)
                .toList();
    }

    public PatientResponse findById(Long id) {
        return patientRepository.findById(id)
                .map(patientMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found: " + id));
    }

    public PatientResponse create(PatientRequest request) {
        return patientMapper.toResponse(patientRepository.save(patientMapper.toEntity(request)));
    }

    public PatientResponse update(Long id, PatientRequest request) {
        var patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found: " + id));

        patientMapper.updateEntity(patient, request);
        return patientMapper.toResponse(patientRepository.save(patient));
    }
}
