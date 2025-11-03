package gr.codelearn.showcase.airline.api.resource.mapper;

import gr.codelearn.showcase.airline.api.resource.response.ReservationResource;
import gr.codelearn.showcase.airline.domain.Reservation;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", config = IgnoreUnmappedMapperConfig.class)
public interface ReservationMapper {
	@Mapping(target = "flightId", source = "flight.id")
	@Mapping(target = "customerEmail", source = "customer.email")
	ReservationResource toResource(Reservation reservation);
}
