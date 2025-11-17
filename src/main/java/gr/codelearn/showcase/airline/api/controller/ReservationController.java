package gr.codelearn.showcase.airline.api.controller;

import gr.codelearn.showcase.airline.api.resource.mapper.ReservationMapper;
import gr.codelearn.showcase.airline.api.resource.request.CreateReservationResource;
import gr.codelearn.showcase.airline.api.resource.response.ReservationResource;
import gr.codelearn.showcase.airline.api.transfer.ApiResponse;
import gr.codelearn.showcase.airline.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/reservations")
public class ReservationController {
	private final ReservationService service;
	private final ReservationMapper mapper;

	@PostMapping
	public ResponseEntity<ApiResponse<ReservationResource>> reserve(@RequestBody CreateReservationResource resource) {
		var reservation = service.reserve(
				resource.flightId(),
				resource.customerEmail(),
				resource.seatClass(),
				resource.seatNumber()
										 );
		return ResponseEntity.ok(ApiResponse.<ReservationResource>builder()
											.data(mapper.toResource(reservation))
											.build());
	}

	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<ReservationResource>> get(@PathVariable Long id) {
		return ResponseEntity.of(
				service.get(id)
					   .map(mapper::toResource)
					   .map(resource -> ApiResponse.<ReservationResource>builder()
												   .data(resource)
												   .build()));
	}

	@PostMapping(value = "/{id}", headers = "action=confirm")
	public ResponseEntity<ApiResponse<ReservationResource>> confirm(@PathVariable Long id) {
		return ResponseEntity.ok(ApiResponse.<ReservationResource>builder()
											.data(mapper.toResource(service.confirm(id)))
											.build());
	}

	@PostMapping(value = "/{id}", headers = "action=cancel")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void cancel(@PathVariable Long id) {
		service.cancel(id);
	}
}
