package com.coelhotechne.detection_system.security.application;

import com.coelhotechne.detection_system.security.api.dto.PasswordChangeRequest;
import com.coelhotechne.detection_system.security.api.dto.UserCreateRequest;
import com.coelhotechne.detection_system.security.api.dto.UserResponse;
import com.coelhotechne.detection_system.security.api.dto.UserUpdateRequest;
import com.coelhotechne.detection_system.security.domain.User;
import com.coelhotechne.detection_system.security.domain.enums.Role;
import com.coelhotechne.detection_system.security.exceptions.UserConflictException;
import com.coelhotechne.detection_system.security.exceptions.UserEmailNotFoundException;
import com.coelhotechne.detection_system.security.exceptions.UserNotFoundException;
import com.coelhotechne.detection_system.security.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Log4j2
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository repository;
    private final PasswordEncoder encoder;



    @Transactional(readOnly = true)
    public Page<UserResponse>listUserPage(Pageable pageable){
        return repository.findAll(pageable).map(UserResponse::from);
    }
    @Transactional(readOnly = true)
    public List<UserResponse> listUser(){
        return repository.findAll().stream().map(UserResponse::from).toList();
    }
    @Transactional
    public UserResponse getUserId(UUID uuid){
        return UserResponse.from(requireUser(uuid));
    }
    @Transactional
    public UserResponse createUser(UserCreateRequest request){
        String email = normalize(request.email());
        if (repository.existsByEmail(email)){
            throw new UserConflictException(HttpStatus.CONFLICT,"E-mail already registered!");
        }
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(encoder.encode(request.password()));
        user.setRole(request.role());
        user.setFullName(request.fullName());
        user.setEnabled(true);
        User saved = repository.save(user);
        log.info("User with e-mail: {} created with role: {}",email,request.role());
        return UserResponse.from(saved);
    }
    @Transactional
    public UserResponse updateUser(UUID uuid, UserUpdateRequest request){
        User user = requireUser(uuid);
        boolean losesAdmin = user.getRole() ==
                Role.ADMIN && user.isEnabled() &&
                ((request.role() !=null && request.role() !=Role.ADMIN)
                        || Boolean.FALSE.equals(request.enabled()));

        if (losesAdmin && repository.countByRoleAndEnableTrue(Role.ADMIN)<=1){
            throw new UserConflictException(HttpStatus.CONFLICT,"Cannot remove or disable the last active ADMIN");
        }
        if (request.role()!=null)user.setRole(request.role());
        if (request.enabled()!=null)user.setEnabled(request.enabled());
        if (request.fullName()!=null)user.setFullName(request.fullName());
        return UserResponse.from(repository.saveAndFlush(user));
    }
    @Transactional(readOnly = true)
    public UserResponse me(String email) {
        return UserResponse.from(requireEmail(email));
    }
    @Transactional
    public void changeOwnPassword(String email, PasswordChangeRequest request){
        User user = requireEmail(email);
        if (request.currentPassword() == null || !encoder.matches(request.currentPassword(),user.getPasswordHash())){
            throw new UserConflictException(HttpStatus.UNPROCESSABLE_CONTENT,"Current password does not match");
        }
        user.setPasswordHash(encoder.encode(request.newPassword()));
    }
    @Transactional
    public void resetPassword(UUID uuid, PasswordChangeRequest request){
        User user = requireUser(uuid);
        user.setPasswordHash(encoder.encode(request.newPassword()));
        log.info("Password reset for user with e-mail: {}",user.getEmail());
    }

    private User requireUser(UUID uuid){
        return repository.findById(uuid)
                .orElseThrow(()-> new UserNotFoundException(uuid," User ID not found"));
    }
    static String normalize(String email){
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private User requireEmail(String email){
        return repository.findByEmail(email).orElseThrow(()-> new UserEmailNotFoundException(email,"E-mail not found!"));
    }
}
