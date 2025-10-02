package com.xyz.booking.mapper;

import com.xyz.booking.dto.ShowDTO;

import com.xyz.booking.dto.request.ShowRequestDTO;
import com.xyz.booking.entity.Show;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ShowMapper {

    // ✅ Entity -> Response DTO
    @Mapping(source = "id", target = "showId")
    @Mapping(source = "movie.title", target = "movieTitle")
    @Mapping(source = "movie.language", target = "language")
    @Mapping(source = "movie.genre", target = "genre")
    @Mapping(source = "theatre.name", target = "theatreName")
    @Mapping(source = "theatre.city.name", target = "cityName")
    ShowDTO toDto(Show show);

    List<ShowDTO> toDtoList(List<Show> shows);

    // Request DTO -> Entity (for create/update)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "movie", ignore = true)   // set in service
    @Mapping(target = "theatre", ignore = true) // set in service
    Show toEntity(ShowRequestDTO request);
}
