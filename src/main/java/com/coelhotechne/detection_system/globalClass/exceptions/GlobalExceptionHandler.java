package com.coelhotechne.detection_system.globalClass.exceptions;

import lombok.extern.log4j.Log4j2;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.*;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Log4j2
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    @Override
    protected ResponseEntity<Object> handleErrorResponseException(
            ErrorResponseException ex,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        if (status.is5xxServerError()) {
            log.error("{} - {}", status, ex.getBody().getDetail(), ex);
        }
        return super.handleErrorResponseException(ex, headers, status, request);
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ProblemDetail handleOptimisticLock(ObjectOptimisticLockingFailureException e){
    ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT
            ,"The resource was modified concurrently; reload and try again.");
    pd.setTitle("Current Modification");
    return pd;
    }

    // Rede de segurança: invariante barrada no @PrePersist/@PreUpdate
    // (só deveria disparar se algum caminho pular as validações do service, ex. ZoneService.requireZone())
    @ExceptionHandler({TransactionSystemException.class, InvalidDataAccessApiUsageException.class})
    public ProblemDetail handlePersistenceFailure(RuntimeException ex) {
        Throwable root = NestedExceptionUtils.getMostSpecificCause(ex);
        if (root instanceof DomainInvariantViolationException violationException) {
            ProblemDetail pd = ProblemDetail.forStatusAndDetail(
                    HttpStatus.UNPROCESSABLE_ENTITY, violationException.getMessage());
            return pd;
        }
        log.error("Unexpected persistence failure", ex);
        ProblemDetail pd = ProblemDetail
                .forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Unexpected error while saving the resource.");
        pd.setTitle("Persistence failure");
        return pd;
    }
}
