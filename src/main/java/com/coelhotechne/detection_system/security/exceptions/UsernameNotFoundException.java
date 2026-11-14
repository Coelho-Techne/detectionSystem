package com.coelhotechne.detection_system.security.exceptions;

import lombok.Getter;
import org.jspecify.annotations.Nullable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.core.AuthenticationException;

@Getter
public class UsernameNotFoundException extends AuthenticationException{
    private static final long serialVersionUID = 1410688585992297006L;
    private static final String DEFAULT_USER_NOT_FOUND_MESSAGE = "User not found";
    private final @Nullable String name;

    public UsernameNotFoundException(String msg) {
        super(msg);
        this.name = null;
    }

    public UsernameNotFoundException(String msg, Throwable cause) {
        super(msg, cause);
        this.name = null;
    }

    private UsernameNotFoundException(String msg, String name) {
        super(msg);
        this.name = name;
    }

    private UsernameNotFoundException(String msg, String name, Throwable cause) {
        super(msg, cause);
        this.name = name;
    }

    public static UsernameNotFoundException fromUsername(String username) {
        return new UsernameNotFoundException("User not found", username);
    }

    public static UsernameNotFoundException fromUsername(String username, Throwable cause) {
        return new UsernameNotFoundException("user not found", username, cause);
    }

    public @Nullable String getName() {
        return this.name;
    }

    private static ProblemDetail buildProblemDetail(String name,String msg){
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,msg);
        pd.setTitle("user not found");
        pd.setProperty("name",name);
        return pd;
    }
}
