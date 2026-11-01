package app.exceptions.handler;

import app.exceptions.InvalidTokenException;
import app.exceptions.InvalidUuidException;
import app.exceptions.UserAlreadyExistsException;
import app.exceptions.UserNotFoundException;
import io.grpc.Status;
import org.springframework.grpc.server.advice.GrpcAdvice;
import org.springframework.grpc.server.advice.GrpcExceptionHandler;

@GrpcAdvice
public class GlobalExceptionHandler {

    @GrpcExceptionHandler(UserNotFoundException.class)
    public Status handleUserNotFoundException(UserNotFoundException ex) {
        return Status.NOT_FOUND
            .withDescription(ex.getMessage());
    }

    @GrpcExceptionHandler(InvalidUuidException.class)
    public Status handleInvalidUuidException(InvalidUuidException ex) {
        return Status.INVALID_ARGUMENT
            .withDescription(ex.getMessage());
    }

    @GrpcExceptionHandler(InvalidTokenException.class)
    public Status handleInvalidTokenException(InvalidTokenException ex) {

        return Status.UNAUTHENTICATED.withDescription(ex.getMessage());
    }

    @GrpcExceptionHandler(UserAlreadyExistsException.class)
    public Status handleUserAlreadyExistsException(UserAlreadyExistsException ex) {
        return Status.ALREADY_EXISTS.withDescription(ex.getMessage());
    }

    @GrpcExceptionHandler(Exception.class)
    public Status handleException(Exception ex) {
        return Status.INTERNAL.withDescription(ex.getMessage());
    }
}
