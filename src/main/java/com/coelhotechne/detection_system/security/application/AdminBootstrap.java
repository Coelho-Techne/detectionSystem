package com.coelhotechne.detection_system.security.application;

import com.coelhotechne.detection_system.security.api.dto.UserCreateRequest;
import com.coelhotechne.detection_system.security.domain.enums.Role;
import com.coelhotechne.detection_system.security.infrastructure.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class AdminBootstrap implements ApplicationRunner {

    private final UserRepository repository;
    private final UserService service;
    private final String email;
    private final String password;

    public AdminBootstrap(UserRepository repository,
                          UserService service,
                          @Value("${app.bootstrap.admin-email:}")
                          String email,
                          @Value("${app.bootstrap.admin-password:}")
                          String password){
        this.repository = repository;
        this.service = service;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (repository.count()>0){
            return;
        }

        if (email == null || email.isBlank() || password==null || password.isBlank()){
            log.warn("No users and no app.bootstrap.admin-email/admin-password ser: nobody can log in ");
            return;
        }
        service.createUser(new UserCreateRequest(email,password, Role.ADMIN,"Administrator"));
        log.warn("Bootstrap ADMIN {} created. Change its password now (POST /api/v1/auth/password)",email);
    }
}
