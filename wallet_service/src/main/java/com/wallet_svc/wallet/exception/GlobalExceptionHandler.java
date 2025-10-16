package com.wallet_svc.wallet.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.wallet_svc.wallet.dto.response.ApiResponse;

import lombok.extern.slf4j.Slf4j;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

	@ExceptionHandler(value = Exception.class)
	ResponseEntity<ApiResponse<String>> handlingRuntimeException(Exception exception) {
		log.error("Exception: ", exception);
		ApiResponse<String> apiResponse = new ApiResponse<>();

		apiResponse.setCode(ErrorCode.UNCATEGORIZED_EXCEPTION.getCode());
		apiResponse.setMessage(ErrorCode.UNCATEGORIZED_EXCEPTION.getMessage());

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(apiResponse);
	}

	@ExceptionHandler(value = AppException.class)
	ResponseEntity<ApiResponse<String>> handlingAppException(AppException exception) {
		ErrorCode errorCode = exception.getErrorCode();
		ApiResponse<String> apiResponse = new ApiResponse<>();

		apiResponse.setCode(errorCode.getCode());
		apiResponse.setMessage(errorCode.getMessage());

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
	}

	@ExceptionHandler(value = MethodArgumentNotValidException.class)
	ResponseEntity<ApiResponse<String>> handlingValidation(MethodArgumentNotValidException exception) {
		String enumKey = exception.getFieldError().getDefaultMessage();

		ErrorCode errorCode = ErrorCode.INVALID_KEY;

		try {
			errorCode = ErrorCode.valueOf(enumKey);
		} catch (IllegalArgumentException e) {
			log.error("Invalid error code: {}", enumKey);
		}

		ApiResponse<String> apiResponse = new ApiResponse<>();

		apiResponse.setCode(errorCode.getCode());
		apiResponse.setMessage(errorCode.getMessage());

		return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);
	}
}
