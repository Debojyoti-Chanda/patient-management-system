package com.pm.patient_service.controller;

import com.pm.patient_service.dto.PatientResponseDTO;
import com.pm.patient_service.service.PatientService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/patients") //http://localhost:4000/patients
public class PatientController {

    private final PatientService patientService;

    // When only 1 constructor is created @Autowired is automatically for the constructor
    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping
    public ResponseEntity<List<PatientResponseDTO>>  getPatients(){
        List<PatientResponseDTO> patients = patientService.getPatients();
        return ResponseEntity.ok().body(patients);
    }

}
