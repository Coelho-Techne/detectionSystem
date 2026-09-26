package com.coelhotechne.detection_system.cam.api.controller;

import com.coelhotechne.detection_system.cam.api.dto.fixed.CamFixedRequest;
import com.coelhotechne.detection_system.cam.api.dto.fixed.CamFixedResponse;
import com.coelhotechne.detection_system.cam.application.fixed.CamFixedService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "CamFixed")
@CrossOrigin(origins = "http://localhost:5173") // ou o IP/host da UI em produção
@RequestMapping(value = "/api/v1/cam-fixed",produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class CamFixedController {

    private final CamFixedService service;

    @Operation(summary = "Lista câmeras fixas cadastradas, paginado")
    @PreAuthorize(CamAccess.READ)
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<CamFixedResponse>> findCamFixedPageList(Pageable pageable) {
        return ResponseEntity.ok(service.findCamList(pageable));
    }

    @Operation(summary = "Lista câmeras fixas cadastradas")
    @PreAuthorize(CamAccess.READ)
    @GetMapping(value = "/all", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<CamFixedResponse>>findCamFixedList(){
        return ResponseEntity.status(HttpStatus.OK).body(service.findCamList());
    }
    @Operation(summary = "Busca uma camera fixa por ID")
    @PreAuthorize(CamAccess.READ)
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CamFixedResponse>findCamFixedId(@PathVariable UUID id){
        return ResponseEntity.status(HttpStatus.OK).body(service.findCamId(id));
    }
    @Operation(summary = "Cadastra uma nova câmera fixa")
    @PreAuthorize(CamAccess.EDIT)
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE,consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CamFixedResponse>createCamFixed(@Valid @RequestBody CamFixedRequest camFixedRequest){
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createCam(camFixedRequest));
    }
    @Operation(summary = "Atualiza o cadastro de uma câmera fixa existente")
    @PreAuthorize(CamAccess.EDIT)
    @PutMapping(value = "/{id}",produces = MediaType.APPLICATION_JSON_VALUE,
    consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CamFixedResponse>updateCamFixed(@PathVariable UUID id, @RequestBody @Valid CamFixedRequest camFixedRequest){
        return ResponseEntity.status(HttpStatus.OK).body(service.updateCam(id,camFixedRequest));
    }
    @Operation(summary = "Remove o cadastro de uma câmera fixa")
    @PreAuthorize(CamAccess.ONLY_ADM)
    @DeleteMapping(value = "/{id}")
    public ResponseEntity<CamFixedResponse>deleteCamFixed(@PathVariable UUID id){
        return ResponseEntity.status(HttpStatus.OK).body(service.deleteCam(id));
    }
}
