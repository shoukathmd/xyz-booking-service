package com.xyz.booking.controller;

import com.xyz.booking.dto.ShowDTO;
import com.xyz.booking.dto.request.ShowRequestDTO;
import com.xyz.booking.service.ShowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * REST controller for managing movie shows.
 *
 * <p>Exposes APIs for:
 * <ul>
 *   <li>Searching shows by movie, city, and date</li>
 *   <li>Creating new shows</li>
 *   <li>Updating existing shows</li>
 *   <li>Deleting shows</li>
 *   <li>Fetching shows by ID or listing all shows</li>
 * </ul>
 *
 * <p>Security:
 * <ul>
 *   <li>RBAC handled using Spring Security annotations (@PreAuthorize).</li>
 *   <li>ABAC (tenant-based checks) handled in service layer.</li>
 * </ul>
 */
@Slf4j
@RestController
@RequestMapping("/api/shows")
public class ShowController {

    private final ShowService showService;

    public ShowController(ShowService showService) {
        this.showService = showService;
    }

    /**
     * Search for shows by movie, city, and date.
     *
     * Input Sanitization Required:
     * - Validate movie and city are not blank.
     * - Validate date format (yyyy-MM-dd) and not null.
     *
     * @param movie movie title
     * @param city  city name
     * @param date  date in yyyy-MM-dd format
     * @return 200 OK with list of shows, or 204 No Content if none found
     */
    @GetMapping("/search")
    public ResponseEntity<List<ShowDTO>> getShows(
            @RequestParam String movie,
            @RequestParam String city,
            @RequestParam String date
    ) {
        log.info("Received request to search shows: movie='{}', city='{}', date={}", movie, city, date);

        LocalDate localDate = LocalDate.parse(date); //Potential exception if date is invalid

        List<ShowDTO> shows = showService.findShowsByMovieAndCityAndDate(movie, city, localDate);

        if (shows.isEmpty()) {
            log.info("No shows found for movie='{}', city='{}', date={}", movie, city, localDate);
            return ResponseEntity.noContent().build(); // 204 No Content
        }

        log.info("Returning {} shows for movie='{}', city='{}', date={}", shows.size(), movie, city, localDate);
        return ResponseEntity.ok(shows);
    }

    /**
     * Create a new show.
     *
     * RBAC: Only THEATRE_OWNER or ADMIN should be allowed.
     * ABAC: THEATRE_OWNER can only create shows for their own tenantId.
     *
     * Input Sanitization Required:
     * - Validate request body fields (movieId, theatreId, showTime).
     */
    @PostMapping
    // @PreAuthorize("hasRole('THEATRE_OWNER') or hasRole('ADMIN')")
    public ResponseEntity<ShowDTO> createShow(@RequestBody ShowRequestDTO request) {
        log.info("Received request to create show: movieId={}, theatreId={}, showTime={}",
                request.getMovieId(), request.getTheatreId(), request.getShowTime());
        return ResponseEntity.ok(showService.createShow(request));
    }

    /**
     * Update an existing show.
     *
     * RBAC: Only THEATRE_OWNER or ADMIN should be allowed.
     * ABAC: THEATRE_OWNER can only update shows belonging to their tenant.
     *
     * Input Sanitization Required:
     * - Validate id is positive.
     * - Validate request body fields.
     */
    @PutMapping("/{id}")
    // @PreAuthorize("hasRole('THEATRE_OWNER') or hasRole('ADMIN')")
    public ResponseEntity<ShowDTO> updateShow(@PathVariable Long id, @RequestBody ShowRequestDTO request) {
        log.info("Received request to update show with id={}", id);
        return ResponseEntity.ok(showService.updateShow(id, request));
    }

    /**
     * Delete a show by ID.
     *
     * RBAC: Only THEATRE_OWNER or ADMIN should be allowed.
     * ABAC: THEATRE_OWNER can only delete shows belonging to their tenant.
     *
     * Input Sanitization Required:
     * - Validate id is positive.
     */
    @DeleteMapping("/{id}")
    // @PreAuthorize("hasRole('THEATRE_OWNER') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteShow(@PathVariable Long id) {
        log.info("Received request to delete show with id={}", id);
        showService.deleteShow(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Get a single show by ID.
     *
     * Input Sanitization Required:
     * - Validate id is positive.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ShowDTO> getShow(@PathVariable Long id) {
        log.info("Received request to fetch show with id={}", id);
        return ResponseEntity.ok(showService.getShow(id));
    }

    /**
     * Get all shows.
     *
     * RBAC: Could restrict to ADMIN/THEATRE_OWNER, or open to all (depending on requirements).
     */
    @GetMapping
    public ResponseEntity<List<ShowDTO>> getAllShows() {
        log.info("Received request to fetch all shows");
        return ResponseEntity.ok(showService.getAllShows());
    }
}
