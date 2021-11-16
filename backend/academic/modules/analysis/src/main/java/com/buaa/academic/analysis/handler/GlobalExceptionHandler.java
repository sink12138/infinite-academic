package com.buaa.academic.analysis.handler;

import com.buaa.academic.model.exception.AcademicException;
import com.buaa.academic.model.exception.ExceptionType;
import com.buaa.academic.model.web.Result;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.ServletException;
import javax.validation.ValidationException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AcademicException.class)
    @ResponseBody
    public Result<Void> handleCustomException(AcademicException exception) {
        return new Result<Void>().withFailure(exception.getMessage());
    }

    @ExceptionHandler({
            HttpMessageConversionException.class,
            MethodArgumentNotValidException.class,
            ValidationException.class,
            TypeMismatchException.class,
            ServletException.class
    })
    @ResponseBody
    public Result<Void> handleParamException() {
        return new Result<Void>().withFailure(ExceptionType.INVALID_PARAM);
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Result<Void> handleOtherException(Exception exception) {
        exception.printStackTrace();
        return new Result<Void>().withFailure(ExceptionType.INTERNAL_SERVER_ERROR);
    }

}
