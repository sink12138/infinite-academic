package com.buaa.academic.model.exception;

/**
 * Base custom exception model for the whole project.
 */
public class AcademicException extends Exception {

    protected String message;

    public AcademicException() {}

    public AcademicException(String message) {
        this.message = message;
    }

    public AcademicException(ExceptionType type) {
        this.message = type.getMessage();
    }

    @Override
    public String getMessage() {
        return this.message;
    }

}
