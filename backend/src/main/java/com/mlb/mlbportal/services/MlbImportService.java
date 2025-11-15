package com.mlb.mlbportal.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.mlb.mlbportal.dto.match.MatchDTO;
import com.mlb.mlbportal.dto.mlbApi.GameEntry;
import com.mlb.mlbportal.dto.mlbApi.ScheduleResponse;
import com.mlb.mlbportal.dto.team.TeamSummary;
import com.mlb.mlbportal.handler.notFound.TeamNotFoundException;
import com.mlb.mlbportal.models.Match;
import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.models.enums.MatchStatus;
import com.mlb.mlbportal.repositories.MatchRepository;
import com.mlb.mlbportal.repositories.TeamRepository;
import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class MlbImportService {
    private final RestTemplate restTemplate = new RestTemplate();
    private final TeamLookupService teamLookupService;
    private final MatchRepository matchRepository;
    private final TeamRepository teamRepository;

    public List<MatchDTO> getOfficialMatches(LocalDate start, LocalDate end) {
        String url = "https://statsapi.mlb.com/api/v1/schedule?sportId=1" + "&startDate=" + start + "&endDate=" + end;
        ScheduleResponse response = this.restTemplate.getForObject(url, ScheduleResponse.class);
        if (response == null || response.dates() == null)
            return List.of();
        List<MatchDTO> result = response.dates().stream().flatMap(d -> d.games().stream()).map(this::toMatchDTO)
                .toList();
        List<MatchDTO> savedMatches = result.stream().filter(this::isMlbTeamMatch).map(dto -> {
            Team homeEntity = this.teamRepository.findByName(dto.homeTeam().name())
                    .orElseThrow(TeamNotFoundException::new);
            Team awayEntity = this.teamRepository.findByName(dto.awayTeam().name())
                    .orElseThrow(TeamNotFoundException::new);
            Match match = new Match(awayEntity, homeEntity, dto.awayScore(), dto.homeScore(), dto.date(), dto.status());
            this.matchRepository.save(match);
            return dto;
        }).toList();
        return savedMatches;
    }

    private boolean isMlbTeamMatch(MatchDTO dto) {
        boolean homeExists = this.teamRepository.findByName(dto.homeTeam().name()).isPresent();
        boolean awayExists = this.teamRepository.findByName(dto.awayTeam().name()).isPresent();
        return homeExists && awayExists;
    }

    private MatchDTO toMatchDTO(GameEntry game) {
        int homeId = game.teams().home().team().id();
        int awayId = game.teams().away().team().id();
        TeamSummary home = teamLookupService.getTeamSummary(homeId);
        TeamSummary away = teamLookupService.getTeamSummary(awayId);
        LocalDateTime date = LocalDateTime.parse(game.gameDate().replace("Z", ""));
        MatchStatus status = convertStatus(game.status().detailedState());
        return new MatchDTO(home, away, Objects.requireNonNullElse(game.teams().home().score(), 0),
                Objects.requireNonNullElse(game.teams().away().score(), 0), date, status);
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