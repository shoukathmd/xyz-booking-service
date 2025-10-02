package com.xyz.booking.service;

import com.xyz.booking.dto.ShowDTO;
import com.xyz.booking.dto.request.ShowRequestDTO;
import com.xyz.booking.entity.Movie;
import com.xyz.booking.entity.Show;
import com.xyz.booking.entity.Theatre;
import com.xyz.booking.exception.ShowHasFutureBookingsException;
import com.xyz.booking.exception.ShowNotFoundException;
import com.xyz.booking.mapper.ShowMapper;
import com.xyz.booking.repository.BookingRepository;
import com.xyz.booking.repository.MovieRepository;
import com.xyz.booking.repository.ShowRepository;
import com.xyz.booking.repository.TheatreRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ShowServiceImplTest {

    @Mock
    private ShowRepository showRepository;

    @Mock
    private MovieRepository movieRepository;

    @Mock
    private TheatreRepository theatreRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ShowMapper showMapper;

    @InjectMocks
    private ShowServiceImpl showService;

    private Show show;
    private Movie movie;
    private Theatre theatre;
    private ShowRequestDTO requestDTO;
    private ShowDTO responseDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        movie = new Movie();
        movie.setId(1L);
        movie.setTitle("Inception");

        theatre = new Theatre();
        theatre.setId(1L);
        theatre.setName("PVR");

        show = Show.builder()
                .id(1L)
                .movie(movie)
                .theatre(theatre)
                .showTime(LocalDateTime.now().plusDays(1))
                .build();

        requestDTO = ShowRequestDTO.builder()
                .movieId(1L)
                .theatreId(1L)
                .showTime(LocalDateTime.now().plusDays(1))
                .build();

        responseDTO = ShowDTO.builder()
                .showId(1L)
                .movieTitle("Inception")
                .theatreName("PVR")
                .showTime(requestDTO.getShowTime())
                .build();
    }

    @Test
    void testCreateShow_Success() {
        when(showMapper.toEntity(requestDTO)).thenReturn(new Show());
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(theatreRepository.findById(1L)).thenReturn(Optional.of(theatre));
        when(showRepository.save(any(Show.class))).thenReturn(show);
        when(showMapper.toDto(show)).thenReturn(responseDTO);

        ShowDTO result = showService.createShow(requestDTO);

        assertNotNull(result);
        assertEquals("Inception", result.getMovieTitle());
        verify(showRepository, times(1)).save(any(Show.class));
    }

    @Test
    void testCreateShow_MovieNotFound() {
        when(showMapper.toEntity(requestDTO)).thenReturn(new Show());
        when(movieRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> showService.createShow(requestDTO));
    }

    @Test
    void testUpdateShow_Success() {
        when(bookingRepository.existsByShow_IdAndShow_ShowTimeAfter(eq(1L), any(LocalDateTime.class))).thenReturn(false);
        when(showRepository.findById(1L)).thenReturn(Optional.of(show));
        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));
        when(theatreRepository.findById(1L)).thenReturn(Optional.of(theatre));
        when(showRepository.save(any(Show.class))).thenReturn(show);
        when(showMapper.toDto(show)).thenReturn(responseDTO);

        ShowDTO result = showService.updateShow(1L, requestDTO);

        assertNotNull(result);
        assertEquals("Inception", result.getMovieTitle());
    }

    @Test
    void testUpdateShow_FutureBookingsExist() {
        when(bookingRepository.existsByShow_IdAndShow_ShowTimeAfter(eq(1L), any(LocalDateTime.class))).thenReturn(true);

        assertThrows(ShowHasFutureBookingsException.class, () -> showService.updateShow(1L, requestDTO));
    }

    @Test
    void testDeleteShow_Success() {
        when(bookingRepository.existsByShow_IdAndShow_ShowTimeAfter(eq(1L), any(LocalDateTime.class))).thenReturn(false);
        when(showRepository.findById(1L)).thenReturn(Optional.of(show));

        showService.deleteShow(1L);

        verify(showRepository, times(1)).delete(show);
    }

    @Test
    void testDeleteShow_FutureBookingsExist() {
        when(bookingRepository.existsByShow_IdAndShow_ShowTimeAfter(eq(1L), any(LocalDateTime.class))).thenReturn(true);

        assertThrows(ShowHasFutureBookingsException.class, () -> showService.deleteShow(1L));
    }

    @Test
    void testGetShow_Success() {
        when(showRepository.findById(1L)).thenReturn(Optional.of(show));
        when(showMapper.toDto(show)).thenReturn(responseDTO);

        ShowDTO result = showService.getShow(1L);

        assertNotNull(result);
        assertEquals("Inception", result.getMovieTitle());
    }

    @Test
    void testGetShow_NotFound() {
        when(showRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ShowNotFoundException.class, () -> showService.getShow(1L));
    }

    @Test
    void testGetAllShows() {
        when(showRepository.findAll()).thenReturn(List.of(show));
        when(showMapper.toDtoList(anyList())).thenReturn(List.of(responseDTO));

        List<ShowDTO> results = showService.getAllShows();

        assertEquals(1, results.size());
        assertEquals("Inception", results.get(0).getMovieTitle());
    }

    @Test
    void testFindShowsByMovieAndCityAndDate() {
        when(showRepository.findByMovie_TitleIgnoreCaseAndTheatre_City_NameIgnoreCaseAndShowTimeBetween(
                eq("Inception"), eq("Hyderabad"), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of(show));

        when(showMapper.toDtoList(anyList())).thenReturn(List.of(responseDTO));

        List<ShowDTO> results = showService.findShowsByMovieAndCityAndDate("Inception", "Hyderabad", LocalDate.now());

        assertEquals(1, results.size());
        assertEquals("Inception", results.get(0).getMovieTitle());
    }
}
