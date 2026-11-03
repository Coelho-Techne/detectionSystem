package com.coelhotechne.detection_system.security.api.controller;

import com.coelhotechne.detection_system.security.api.dto.PasswordChangeRequest;
import com.coelhotechne.detection_system.security.api.dto.UserCreateRequest;
import com.coelhotechne.detection_system.security.api.dto.UserResponse;
import com.coelhotechne.detection_system.security.api.dto.UserUpdateRequest;
import com.coelhotechne.detection_system.security.application.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@Tag(name = "User")
@RequestMapping(value = "/api/v1/user",produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@PreAuthorize(UserAccess.ONLY_ADM)
public class UserController {

    private final UserService service;

    @GetMapping
    public ResponseEntity<Page<UserResponse>>pageListUser(
            @PageableDefault(size = 50,sort = "email",direction = Sort.Direction.ASC)
            Pageable pageable
    ){
        return ResponseEntity.ok(service.listUserPage(pageable));
    }
    @GetMapping(value = "/all")
    public ResponseEntity<List<UserResponse>>listUser(){return ResponseEntity.ok(service.listUser());}

    @GetMapping(value = "/{id}")
    public ResponseEntity<UserResponse>findId(@PathVariable UUID id){
        return ResponseEntity.ok(service.getUserId(id));
    }
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponse>createUser(@PathVariable @Valid UserCreateRequest request){
        UserResponse created = service.createUser(request);
        return ResponseEntity.created(URI.create("/api/v1/user"+created.uuid())).body(created);
    }
    @PostMapping(value = "/{id}/password",consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void>resetPassword(@PathVariable UUID id, @Valid @RequestBody PasswordChangeRequest request){
        service.resetPassword(id,request);
        return ResponseEntity.noContent().build();
    }
    @PostMapping(value = "/{id}",consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponse>updateUser(@PathVariable UUID id, @Valid @RequestBody UserUpdateRequest request){
        return ResponseEntity.ok(service.updateUser(id,request));
    }



}
