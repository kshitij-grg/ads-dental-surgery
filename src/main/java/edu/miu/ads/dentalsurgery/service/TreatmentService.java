package edu.miu.ads.dentalsurgery.service;

import java.util.List;

import edu.miu.ads.dentalsurgery.dto.TreatmentRequest;
import edu.miu.ads.dentalsurgery.dto.TreatmentResponse;
import edu.miu.ads.dentalsurgery.exception.ResourceNotFoundException;
import edu.miu.ads.dentalsurgery.mapper.TreatmentMapper;
import edu.miu.ads.dentalsurgery.repository.TreatmentRepository;
import org.springframework.stereotype.Service;

@Service
public class TreatmentService {

    private final TreatmentRepository treatmentRepository;
    private final TreatmentMapper treatmentMapper;

    public TreatmentService(TreatmentRepository treatmentRepository, TreatmentMapper treatmentMapper) {
        this.treatmentRepository = treatmentRepository;
        this.treatmentMapper = treatmentMapper;
    }

    public List<TreatmentResponse> findAll() {
        return treatmentRepository.findAll()
                .stream()
                .map(treatmentMapper::toResponse)
                .toList();
    }

    public TreatmentResponse findById(Long id) {
        return treatmentRepository.findById(id)
                .map(treatmentMapper::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Treatment not found: " + id));
    }

    public TreatmentResponse create(TreatmentRequest request) {
        treatmentRepository.findByCode(request.code())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Treatment code already exists: " + request.code());
                });

        return treatmentMapper.toResponse(treatmentRepository.save(treatmentMapper.toEntity(request)));
    }

    public TreatmentResponse update(Long id, TreatmentRequest request) {
        var treatment = treatmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Treatment not found: " + id));

        treatmentRepository.findByCode(request.code())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Treatment code already exists: " + request.code());
                });

        treatmentMapper.updateEntity(treatment, request);
        return treatmentMapper.toResponse(treatmentRepository.save(treatment));
    }
}
