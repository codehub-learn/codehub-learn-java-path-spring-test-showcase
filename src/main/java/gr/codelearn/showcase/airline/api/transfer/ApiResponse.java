package gr.codelearn.showcase.airline.api.transfer;

import lombok.Builder;
import lombok.Value;

import java.io.Serializable;
import java.time.ZonedDateTime;
import java.util.UUID;

@Value
@Builder
public class ApiResponse<T> implements Serializable {
	String transactionId = UUID.randomUUID().toString().toUpperCase();
	ZonedDateTime createdAt = ZonedDateTime.now();
	T data;
	ApiError apiError;
}
