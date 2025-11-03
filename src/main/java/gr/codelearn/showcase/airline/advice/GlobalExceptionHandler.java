package gr.codelearn.showcase.airline.advice;

import gr.codelearn.showcase.airline.api.transfer.ApiError;
import gr.codelearn.showcase.airline.api.transfer.ApiResponse;
import gr.codelearn.showcase.airline.component.BaseComponent;
import gr.codelearn.showcase.airline.exception.BusinessException;
import gr.codelearn.showcase.airline.exception.NotFoundException;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.util.StringUtils;
import org.springframework.web.HttpMediaTypeException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.UnsatisfiedServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler extends BaseComponent {
	@Value("${spring.servlet.multipart.max-file-size}")
	private String maxFileSize;

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ApiResponse<?>> handleException(final Exception ex, final WebRequest request) {
		logger.error("Unexpected exception occurred. Please retry the operation.", ex);
		return new ResponseEntity<>(ApiResponse.builder()
											   .apiError(getApiError(ex, HttpStatus.INTERNAL_SERVER_ERROR, request,
																	 "Unexpected exception occurred. Please retry the operation."))
											   .build(), HttpStatus.INTERNAL_SERVER_ERROR);
	}

	@ExceptionHandler(NotFoundException.class)
	public ResponseEntity<ApiResponse<?>> handleException(final NotFoundException ex, final WebRequest request) {
		logger.error("Unable to find the selected resource.", ex);
		return new ResponseEntity<>(ApiResponse.builder().apiError(getApiError(ex, HttpStatus.FORBIDDEN, request)).build(),
									HttpStatus.FORBIDDEN);
	}

	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ApiResponse<?>> handleException(final BusinessException ex, final WebRequest request) {
		logger.error("Unable to complete the selected action.", ex);
		return new ResponseEntity<>(ApiResponse.builder().apiError(getApiError(ex, HttpStatus.FORBIDDEN, request)).build(),
									HttpStatus.FORBIDDEN);
	}

	@ExceptionHandler(ResourceAccessException.class)
	public ResponseEntity<ApiResponse<?>> handleException(final ResourceAccessException ex, final WebRequest request) {
		logger.error("Access to the specified resource was denied.", ex);
		return new ResponseEntity<>(ApiResponse.builder().apiError(getApiError(ex, HttpStatus.FORBIDDEN, request)).build(),
									HttpStatus.FORBIDDEN);
	}

	@ExceptionHandler(ConstraintViolationException.class)
	public ResponseEntity<ApiResponse<?>> handleException(final ConstraintViolationException ex, final WebRequest request) {
		logger.error("The submitted resource violates data constraints.", ex);
		return new ResponseEntity<>(ApiResponse.builder().apiError(getApiError(ex, HttpStatus.BAD_REQUEST, request)).build(),
									HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public ResponseEntity<ApiResponse<?>> handleException(final HttpRequestMethodNotSupportedException ex, final WebRequest request) {
		logger.error("The requested HTTP method is not supported.", ex);
		return new ResponseEntity<>(ApiResponse.builder().apiError(getApiError(ex, HttpStatus.METHOD_NOT_ALLOWED, request)).build(),
									HttpStatus.METHOD_NOT_ALLOWED);
	}

	@ExceptionHandler(DataAccessException.class)
	public final ResponseEntity<ApiResponse<?>> handleException(final DataAccessException ex, final WebRequest request) {
		var customMessage = "There was something wrong while interacting with the associated database.";
		logger.error(customMessage, ex);
		return new ResponseEntity<>(
				ApiResponse.builder().apiError(getApiError(ex, HttpStatus.NOT_ACCEPTABLE, request, customMessage)).build(),
				HttpStatus.NOT_ACCEPTABLE);
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public final ResponseEntity<ApiResponse<?>> handleException(final DataIntegrityViolationException ex, final WebRequest request) {
		var customMessage = """
							There was a constraint violation while interacting with the associated database.
							Make sure the action submitted does not violate business associations.
							E.g.
							Cannot delete a training edition associated with a training instance or supplier contracts.
							Cannot delete an account associated as instructor with a training edition or a training instance.
							""";
		logger.error("{}", customMessage, ex);

		return new ResponseEntity<>(
				ApiResponse.builder().apiError(getApiError(ex, HttpStatus.NOT_ACCEPTABLE, request, customMessage)).build(),
				HttpStatus.NOT_ACCEPTABLE);
	}

	@ExceptionHandler(NoResourceFoundException.class)
	protected ResponseEntity<ApiResponse<?>> handleException(final NoResourceFoundException ex, final WebRequest request) {
		logger.error("We could not find a matching resource, please try again.", ex);
		return new ResponseEntity<>(ApiResponse.builder()
											   .apiError(getApiError(ex, HttpStatus.NOT_FOUND, request,
																	 "We could not find a matching resource, please try again."))
											   .build(), HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(HttpMessageConversionException.class)
	protected ResponseEntity<ApiResponse<?>> handleException(final HttpMessageConversionException ex, final WebRequest request) {
		logger.error("Check again the structure and make sure that you are using the correct types.", ex);
		return new ResponseEntity<>(ApiResponse.builder()
											   .apiError(getApiError(ex, HttpStatus.NOT_FOUND, request,
																	 "Check again the structure and make sure that you are using the " +
																	 "correct types."))
											   .build(), HttpStatus.NOT_FOUND);
	}

	@ExceptionHandler(HttpMediaTypeException.class)
	protected ResponseEntity<ApiResponse<?>> handleException(final HttpMediaTypeException ex, final WebRequest request) {
		logger.error("Content type is not supported.", ex);
		return new ResponseEntity<>(ApiResponse.builder().apiError(getApiError(ex, HttpStatus.BAD_REQUEST, request)).build(),
									HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(UnsatisfiedServletRequestParameterException.class)
	public ResponseEntity<ApiResponse<?>> handleException(final UnsatisfiedServletRequestParameterException ex, final WebRequest request) {
		var customMessage = "The request is missing required parameters or the provided parameters do not match the expected criteria.";
		logger.error("{}", customMessage, ex);
		return new ResponseEntity<>(ApiResponse.builder().apiError(getApiError(ex, HttpStatus.BAD_REQUEST, request, customMessage)).build(),
									HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(MissingServletRequestParameterException.class)
	protected ResponseEntity<ApiResponse<?>> handleException(final MissingServletRequestParameterException ex, final WebRequest request) {
		logger.error("There was a parameter missing from incoming request.", ex);
		return new ResponseEntity<>(ApiResponse.builder().apiError(getApiError(ex, HttpStatus.BAD_REQUEST, request)).build(),
									HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	protected ResponseEntity<ApiResponse<?>> handleException(final HttpMessageNotReadableException ex, final WebRequest request) {
		var customMessage = "The submitted HTTP request could not be parsed.";
		logger.error(customMessage, ex);
		return new ResponseEntity<>(ApiResponse.builder().apiError(getApiError(ex, HttpStatus.BAD_REQUEST, request, customMessage)).build(),
									HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(MissingServletRequestPartException.class)
	protected ResponseEntity<ApiResponse<?>> handleException(final MissingServletRequestPartException ex, final WebRequest request) {
		var customMessage = "The submitted HTTP request is missing required parts.";
		logger.error(customMessage, ex);
		return new ResponseEntity<>(ApiResponse.builder().apiError(getApiError(ex, HttpStatus.BAD_REQUEST, request, customMessage)).build(),
									HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(MaxUploadSizeExceededException.class)
	protected ResponseEntity<ApiResponse<?>> handleException(final MaxUploadSizeExceededException ex, final WebRequest request) {
		var customMessage = "The submitted file exceeds the maximum upload size. Maximum upload size is " + maxFileSize;
		logger.error(customMessage, ex);
		return new ResponseEntity<>(ApiResponse.builder().apiError(getApiError(ex, HttpStatus.BAD_REQUEST, request, customMessage)).build(),
									HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	protected ResponseEntity<ApiResponse<?>> handleException(final MethodArgumentNotValidException ex, final WebRequest request) {
		logger.error("Method argument is invalid.", ex);
		return new ResponseEntity<>(ApiResponse.builder().apiError(getApiError(ex, HttpStatus.BAD_REQUEST, request)).build(),
									HttpStatus.BAD_REQUEST);
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ApiResponse<?>> handleException(final MethodArgumentTypeMismatchException ex, final WebRequest request) {
		var customMessage = "Method argument, although matched, is of wrong type or exceeds valid value range. " +
							"Make sure you are not using invalid dates and alphanumeric characters when a number is expected.";
		logger.error(customMessage, ex);
		return new ResponseEntity<>(ApiResponse.builder().apiError(getApiError(ex, HttpStatus.BAD_REQUEST, request, customMessage)).build(),
									HttpStatus.BAD_REQUEST);
	}

	private ApiError getApiError(final Exception ex, final HttpStatus status, final WebRequest request) {
		String path = request.getDescription(false);
		if (path.indexOf("uri=") == 0) {
			path = StringUtils.replace(path, "uri=", "");
		}
		return new ApiError(status.value(), ex.getMessage(), path);
	}

	private ApiError getApiError(final Exception ex, final HttpStatus status, final WebRequest request, String customMessage) {
		String path = request.getDescription(false);
		if (path.indexOf("uri=") == 0) {
			path = StringUtils.replace(path, "uri=", "");
		}
		return new ApiError(status.value(), customMessage != null ? customMessage : ex.getMessage(), path);
	}
}