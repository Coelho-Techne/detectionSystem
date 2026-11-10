package com.coelhotechne.detection_system.cam.application.auth;

import com.coelhotechne.detection_system.cam.api.dto.auth.CamAuthResponse;
import com.coelhotechne.detection_system.cam.application.auth.enums.CamAuthMessage;
import com.coelhotechne.detection_system.cam.domain.base.BaseCam;
import com.coelhotechne.detection_system.cam.exceptions.CamAuthenticationException;
import com.coelhotechne.detection_system.cam.exceptions.CamNotFoundException;
import com.coelhotechne.detection_system.cam.infrastructure.CamRepository;
import com.coelhotechne.detection_system.globalClass.security.AccessKeyGenerator;
import com.coelhotechne.detection_system.globalClass.security.AccessKeyHasher;
import com.coelhotechne.detection_system.globalClass.security.IssuedAccessKeyResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CamAuthServiceImpl implements CamAuthService {

    private final CamRepository camRepository;
    private final AccessKeyHasher hasher;
    private final AccessKeyGenerator keyGenerator;
    private final Clock clock;

    @Override
    @Transactional
    public CamAuthResponse authenticate(UUID camId, String presentedAccessKey) {
        Optional<BaseCam> maybeCam = camRepository.findById(camId);
        if (maybeCam.isEmpty()|| !hasher.matches(presentedAccessKey,maybeCam.get().getAccessKey())) {
            return new CamAuthResponse(camId, CamAuthMessage.NEGADO, null);
        }
        Instant now = clock.instant();
        maybeCam.get().recordCommunication(now);
        return new CamAuthResponse(camId, CamAuthMessage.AUTORIZADO, now);
    }

    @Override
    public void requireValidAccessKey(UUID camId, String presentedAccessKey) {
        CamAuthResponse response = authenticate(camId, presentedAccessKey);
        if (response.message() != CamAuthMessage.AUTORIZADO) {
            throw new CamAuthenticationException(camId);
        }
    }

    @Override
    @Transactional
    public IssuedAccessKeyResponse issueAccessKey(UUID camId) {
        BaseCam cam = camRepository.findById(camId).orElseThrow(()->
                new CamNotFoundException(camId.toString(),"Camera not found!"));
        boolean rotation = cam.getAccessKey()!=null;
        String rawKey =keyGenerator.generate();
        cam.issueAccessKey(hasher.hash(rawKey));
        log.info("Access key {} for camera {} ",rotation ? "rotated" : "issued",camId);
        return new IssuedAccessKeyResponse(camId,rawKey,clock.instant());
    }
}
