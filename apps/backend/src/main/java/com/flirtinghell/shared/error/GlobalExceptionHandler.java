package com.flirtinghell.shared.error;

import java.util.Map;

import com.flirtinghell.shared.api.RequestIds;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidationError(
			MethodArgumentNotValidException exception,
			HttpServletRequest request
	) {
		String field = exception.getBindingResult().getFieldErrors().stream()
				.findFirst()
				.map(error -> error.getField())
				.orElse(null);
		Map<String, Object> details = field == null ? Map.of() : Map.of("field", field);
		return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "요청 값이 올바르지 않습니다.", request, details);
	}

	@ExceptionHandler(ResourceNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException exception, HttpServletRequest request) {
		return build(HttpStatus.NOT_FOUND, exception.code(), exception.getMessage(), request, Map.of());
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleUnexpectedError(Exception exception, HttpServletRequest request) {
		return build(HttpStatus.INTERNAL_SERVER_ERROR, "SERVER_ERROR", "서버 오류가 발생했습니다.", request, Map.of());
	}

	private ResponseEntity<ErrorResponse> build(
			HttpStatus status,
			String code,
			String message,
			HttpServletRequest request,
			Map<String, Object> details
	) {
		ErrorBody body = new ErrorBody(code, message, RequestIds.from(request), details);
		return ResponseEntity.status(status).body(new ErrorResponse(body));
	}
}
