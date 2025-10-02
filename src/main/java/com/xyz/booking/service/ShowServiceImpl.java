package com.xyz.booking.service;

import com.xyz.booking.dto.ShowDTO;
import com.xyz.booking.dto.request.ShowRequestDTO;
import com.xyz.booking.entity.Show;
import com.xyz.booking.exception.ShowHasFutureBookingsException;
import com.xyz.booking.exception.ShowNotFoundException;
import com.xyz.booking.mapper.ShowMapper;
import com.xyz.booking.repository.BookingRepository;
import com.xyz.booking.repository.MovieRepository;
import com.xyz.booking.repository.ShowRepository;
import com.xyz.booking.repository.TheatreRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

/**
 * Implementation of {@link ShowService} that provides CRUD operations
 * and business rules for managing movie shows.
 *
 * <p>This service is responsible for:
 * <ul>
 *   <li>Creating new shows for movies and theatres</li>
 *   <li>Updating existing shows (only if no future bookings exist)</li>
 *   <li>Deleting shows (only if no future bookings exist)</li>
 *   <li>Fetching single or multiple shows</li>
 *   <li>Finding shows by movie, city, and date</li>
 * </ul>
 *
 * <p>Business rules enforced:
 * <ul>
 *   <li>Shows with future bookings cannot be updated or deleted.</li>
 *   <li>Shows must be associated with a valid Movie and Theatre.</li>
 * </ul>
 *
 * <p>Security considerations:
 * <ul>
 *   <li>RBAC (Role-Based Access Control) handled at controller level with Spring Security annotations.</li>
 *   <li>ABAC (Attribute-Based Access Control) should be enforced here:
 *       e.g., validate that the authenticated tenant/partner owns the theatre linked to the show.</li>
 * </ul>
 */
@Slf4j
@Service
public class ShowServiceImpl implements ShowService {

    private final ShowRepository showRepository;
    private final MovieRepository movieRepository;
    private final TheatreRepository theatreRepository;
    private final BookingRepository bookingRepository;
    private final ShowMapper showMapper;

    public ShowServiceImpl(ShowRepository showRepository,
                           MovieRepository movieRepository,
                           TheatreRepository theatreRepository,
                           BookingRepository bookingRepository,
                           ShowMapper showMapper) {
        this.showRepository = showRepository;
        this.movieRepository = movieRepository;
        this.theatreRepository = theatreRepository;
        this.bookingRepository = bookingRepository;
        this.showMapper = showMapper;
    }

    /**
     * Finds shows by movie title, city, and date.
     *
     * Input Sanitization Required:
     * - Validate that movieTitle and cityName are not null/empty.
     * - Ensure date is not null and is in valid range.
     *
     * ABAC Consideration:
     * - Usually not enforced here since shows may be public.
     * - If tenants should only see their own shows, filter by tenantId.
     *
     * @param movieTitle title of the movie
     * @param cityName   name of the city
     * @param date       selected date
     * @return list of matching shows as DTOs
     */
    @Override
    public List<ShowDTO> findShowsByMovieAndCityAndDate(String movieTitle, String cityName, LocalDate date) {
        log.info("Finding shows for movie='{}', city='{}', date={}", movieTitle, cityName, date);

        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        List<Show> shows = showRepository.findByMovie_TitleIgnoreCaseAndTheatre_City_NameIgnoreCaseAndShowTimeBetween(
                movieTitle, cityName, startOfDay, endOfDay
        );

        log.debug("Found {} shows for movie='{}' in city='{}' on date={}",
                shows.size(), movieTitle, cityName, date);

        return showMapper.toDtoList(shows);
    }

    /**
     * Creates a new show for a movie and theatre.
     *
     * Input Sanitization Required:
     * - Validate request is not null.
     * - Ensure movieId and theatreId are positive numbers.
     * - Validate showTime is not null and not in the past.
     *
     * ABAC Required:
     * - Ensure authenticated tenantId == theatre.tenantId before creating.
     *
     * @param request show request containing movieId, theatreId, and showTime
     * @return the created show as DTO
     */
    @Override
    public ShowDTO createShow(ShowRequestDTO request) {
        log.info("Creating show for movieId={}, theatreId={}, showTime={}",
                request.getMovieId(), request.getTheatreId(), request.getShowTime());

        Show show = showMapper.toEntity(request);

        show.setMovie(movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> {
                    log.error("Movie not found with id={}", request.getMovieId());
                    return new EntityNotFoundException("Movie not found");
                }));

        show.setTheatre(theatreRepository.findById(request.getTheatreId())
                .orElseThrow(() -> {
                    log.error("Theatre not found with id={}", request.getTheatreId());
                    return new EntityNotFoundException("Theatre not found");
                }));

        // 🔒 ABAC check (to be implemented):
        // Compare authenticatedTenantId with show.getTheatre().getTenantId()
        // If mismatch -> throw new AccessDeniedException("Not allowed to create show for this theatre");

        Show saved = showRepository.save(show);
        log.info("Show created successfully with id={}", saved.getId());

        return showMapper.toDto(saved);
    }

    /**
     * Updates an existing show.
     * <p>Throws {@link ShowHasFutureBookingsException} if the show has future bookings.</p>
     *
     * Input Sanitization Required:
     * - Validate id is not null and > 0.
     * - Validate request fields (movieId, theatreId, showTime) similar to create.
     *
     * ABAC Required:
     * - Ensure authenticated tenantId == theatre.tenantId before update.
     *
     * @param id      ID of the show
     * @param request updated show request
     * @return the updated show as DTO
     */
    @Override
    public ShowDTO updateShow(Long id, ShowRequestDTO request) {
        log.info("Updating show with id={}", id);

        boolean hasFutureBookings =
                bookingRepository.existsByShow_IdAndShow_ShowTimeAfter(id, LocalDateTime.now());

        if (hasFutureBookings) {
            log.warn("Cannot update show id={} because it has future bookings", id);
            throw new ShowHasFutureBookingsException(id);
        }

        Show show = showRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Show not found with id={}", id);
                    return new ShowNotFoundException(id);
                });

        // ABAC check (to be implemented):
        // Compare authenticatedTenantId with show.getTheatre().getTenantId()
        // If mismatch -> throw new AccessDeniedException("Not allowed to update this show");

        show.setMovie(movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new EntityNotFoundException("Movie not found")));
        show.setTheatre(theatreRepository.findById(request.getTheatreId())
                .orElseThrow(() -> new EntityNotFoundException("Theatre not found")));
        show.setShowTime(request.getShowTime());

        Show updated = showRepository.save(show);
        log.info("Show updated successfully with id={}", updated.getId());

        return showMapper.toDto(updated);
    }

    /**
     * Deletes a show.
     * <p>Throws {@link ShowHasFutureBookingsException} if the show has future bookings.</p>
     *
     * Input Sanitization Required:
     * - Validate id is not null and > 0.
     *
     * ABAC Required:
     * - Ensure authenticated tenantId == theatre.tenantId before delete.
     *
     * @param id ID of the show
     */
    @Override
    public void deleteShow(Long id) {
        log.info("Deleting show with id={}", id);

        boolean hasFutureBookings =
                bookingRepository.existsByShow_IdAndShow_ShowTimeAfter(id, LocalDateTime.now());

        if (hasFutureBookings) {
            log.warn("Cannot delete show id={} because it has future bookings", id);
            throw new ShowHasFutureBookingsException(id);
        }

        Show show = showRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Show not found with id={}", id);
                    return new ShowNotFoundException(id);
                });

        // ABAC check (to be implemented):
        // Compare authenticatedTenantId with show.getTheatre().getTenantId()
        // If mismatch -> throw new AccessDeniedException("Not allowed to delete this show");

        showRepository.delete(show);
        log.info("Show deleted successfully with id={}", id);
    }

    /**
     * Gets a single show by its ID.
     *
     * Input Sanitization Required:
     * - Validate id is not null and > 0.
     *
     * ABAC Consideration:
     * - Depending on requirements, either allow all tenants/customers
     *   OR enforce tenantId check for restricted access.
     *
     * @param id ID of the show
     * @return the show as DTO
     */
    @Override
    public ShowDTO getShow(Long id) {
        log.info("Fetching show with id={}", id);
        Show show = showRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Show not found with id={}", id);
                    return new ShowNotFoundException(id);
                });

        //  ABAC optional check:
        // If access should be tenant-restricted, compare authenticatedTenantId with show.getTheatre().getTenantId()

        log.debug("Fetched show with id={} successfully", id);
        return showMapper.toDto(show);
    }

    /**
     * Gets all shows.
     *
     * Input Sanitization Required:
     * - None (this fetches everything).
     *
     * ABAC Required:
     * - If strict tenant isolation is required, filter by authenticatedTenantId.
     *
     * @return list of all shows as DTOs
     */
    @Override
    public List<ShowDTO> getAllShows() {
        log.info("Fetching all shows");
        List<Show> shows = showRepository.findAll();
        log.debug("Found {} shows in total", shows.size());

        //  ABAC check:
        // If needed, filter: shows = shows.stream()
        //        .filter(s -> s.getTheatre().getTenantId().equals(authenticatedTenantId))
        //        .toList();

        return showMapper.toDtoList(shows);
    }
}
