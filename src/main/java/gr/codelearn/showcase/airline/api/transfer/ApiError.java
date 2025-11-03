package gr.codelearn.showcase.airline.api.transfer;

public record ApiError(Integer status, String message, String path) {
}
