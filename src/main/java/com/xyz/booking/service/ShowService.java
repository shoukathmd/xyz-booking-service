package com.xyz.booking.service;

import com.xyz.booking.dto.ShowDTO;
import com.xyz.booking.dto.request.ShowRequestDTO;


import java.time.LocalDate;
import java.util.List;

public interface ShowService {
    public List<ShowDTO> findShowsByMovieAndCityAndDate(String movieTitle, String cityName, LocalDate date);
    ShowDTO createShow(ShowRequestDTO request);
    ShowDTO updateShow(Long id, ShowRequestDTO request);
    void deleteShow(Long id);
    ShowDTO getShow(Long id);
    List<ShowDTO> getAllShows();
}

