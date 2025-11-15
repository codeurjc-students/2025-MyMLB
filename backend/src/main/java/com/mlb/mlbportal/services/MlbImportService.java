package com.mlb.mlbportal.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.mlb.mlbportal.dto.match.MatchDTO;
import com.mlb.mlbportal.dto.mlbApi.GameEntry;
import com.mlb.mlbportal.dto.mlbApi.ScheduleResponse;
import com.mlb.mlbportal.dto.team.TeamSummary;
import com.mlb.mlbportal.models.enums.MatchStatus;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class MlbImportService {
    private final RestTemplate restTemplate = new RestTemplate();
    private final TeamLookupService teamLookupService;

    public List<MatchDTO> getOfficialMatches(LocalDate start, LocalDate end) {

        String url = "https://statsapi.mlb.com/api/v1/schedule?sportId=1"
                + "&startDate=" + start
                + "&endDate=" + end;

        ScheduleResponse response = restTemplate.getForObject(url, ScheduleResponse.class);

        if (response == null || response.dates() == null)
            return List.of();

        return response.dates().stream()
                .flatMap(d -> d.games().stream())
                .map(this::toMatchDTO)
                .toList();
    }

    private MatchDTO toMatchDTO(GameEntry game) {

        int homeId = game.teams().home().team().id();
        int awayId = game.teams().away().team().id();

        TeamSummary home = teamLookupService.getTeamSummary(homeId);
        TeamSummary away = teamLookupService.getTeamSummary(awayId);

        LocalDateTime date = LocalDateTime.parse(game.gameDate().replace("Z", ""));

        MatchStatus status = convertStatus(game.status().detailedState());

        return new MatchDTO(
                home,
                away,
                game.teams().home().score() != null ? game.teams().home().score() : 0,
                game.teams().away().score() != null ? game.teams().away().score() : 0,
                date,
                status);
    }

    private MatchStatus convertStatus(String apiStatus) {
        return switch (apiStatus) {
            case "Scheduled" -> MatchStatus.SCHEDULED;
            case "In Progress" -> MatchStatus.IN_PROGRESS;
            case "Final" -> MatchStatus.FINISHED;
            default -> MatchStatus.SCHEDULED;
        };
    }
}