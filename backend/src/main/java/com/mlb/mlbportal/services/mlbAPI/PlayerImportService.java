package com.mlb.mlbportal.services.mlbAPI;

import java.util.List;
import java.util.Objects;

import javax.naming.ServiceUnavailableException;

import com.mlb.mlbportal.models.player.Player;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.mlb.mlbportal.dto.mlbapi.player.PlayerDetailInfo;
import com.mlb.mlbportal.dto.mlbapi.player.PlayerResponse;
import com.mlb.mlbportal.dto.mlbapi.player.RosterEntry;
import com.mlb.mlbportal.dto.mlbapi.player.RosterResponse;
import com.mlb.mlbportal.dto.mlbapi.player.StatData;
import com.mlb.mlbportal.models.Team;
import com.mlb.mlbportal.models.enums.PitcherPositions;
import com.mlb.mlbportal.models.enums.PlayerPositions;
import com.mlb.mlbportal.models.others.PictureInfo;
import com.mlb.mlbportal.models.player.Pitcher;
import com.mlb.mlbportal.models.player.PositionPlayer;
import com.mlb.mlbportal.repositories.TeamRepository;
import com.mlb.mlbportal.repositories.player.PitcherRepository;
import com.mlb.mlbportal.repositories.player.PositionPlayerRepository;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class PlayerImportService {
    private final RestTemplate restTemplate = new RestTemplate();
    private final TeamRepository teamRepository;
    private final PitcherRepository pitcherRepository;
    private final PositionPlayerRepository positionPlayerRepository;

    /**
     * Main method of the service.
     * Obtain the roster from each team, and then obtain the stats for each player.
     */
    @Transactional
    @CircuitBreaker(name = "playersFromStatsAPI", fallbackMethod = "fallbackGetTeamsRoster")
    @Retry(name = "playersFromStatsAPI")
    public void getTeamRoster() {
        List<Team> teams = this.teamRepository.findAll();

        for (Team team : teams) {
            String url = "https://statsapi.mlb.com/api/v1/teams/" + team.getStatsApiId() + "/roster";
            RosterResponse response = this.restTemplate.getForObject(url, RosterResponse.class);
            if (response != null && response.roster() != null) {
                for (RosterEntry entry : response.roster()) {
                    try {
                        this.getPlayerStatsFromAPI(entry.person().id(), team);
                    }
                    catch (Exception ex) {
                        log.warn("Error fetching the data of player id: {} with the team : {}: {}", entry.person().id(), team.getName(), ex.getMessage());
                    }
                }
            }
        }
        log.info("Rosters and Stats successfully retrieved and stored in the database");
    }

    /**
     * Obtain the stats for each player. The stats are different depending on the type of player (position or pitcher)
     *
     * @param personId stat API ID for the player.
     * @param team of the player.
     */
    @CircuitBreaker(name = "playerStatsFromAPI", fallbackMethod = "fallbackGetPlayerStats")
    @Retry(name = "playerStatsFromAPI")
    private void getPlayerStatsFromAPI(int personId, Team team) {
        String url = "https://statsapi.mlb.com/api/v1/people/" + personId + "?hydrate=stats(group=[hitting,pitching],type=[season])";
        PlayerResponse response = this.restTemplate.getForObject(url, PlayerResponse.class);
        if (response == null || response.people().isEmpty()) {
            return;
        }
        PlayerDetailInfo player = response.people().getFirst();
        StatData data = this.getData(player);

        if ("1".equals(player.primaryPosition().code())) {
            this.savePitcher(player, data, team);
        }
        else {
            this.savePositionPlayer(player, data, team);
        }
    }

    /**
     * Obtain the player's picture from the API, and if the player does not have any (or any error occur) it sets a default one.
     *
     * @param id stat API ID for the player.
     *
     * @return the picture obtained from the API.
     */
    @CircuitBreaker(name = "playerPictureFromAPI", fallbackMethod = "fallbackGetPlayerPicture")
    @Retry(name = "playerPictureFromAPI")
    private PictureInfo savePlayerPicture(int id) {
        String url = "https://img.mlbstatic.com/mlb-photos/image/upload/d_people:generic:headshot:67:current.png/v1/people/"
                + id + "/headshot/67/current";

        PictureInfo pictureInfo = new PictureInfo();
        pictureInfo.setUrl(url);
        return pictureInfo;
    }

    @SuppressWarnings("unused")
    public void fallbackGetTeamsRoster(Throwable throwable) {
        log.error("Error REAL de la API: {}", throwable.getMessage());
        log.error("playersFromStatsAPI: Error obtaining the roster");
    }

    @SuppressWarnings("unused")
    public void fallbackGetPlayerStats(int personId, Team  team, Throwable throwable) throws ServiceUnavailableException {
        if (this.positionPlayerRepository.findByStatsApiId(personId).isEmpty() || this.pitcherRepository.findByStatsApiId(personId).isEmpty()) {
            log.warn("playerStatsFromAPI: Error obtaining the stats of the player whose stat API ID is: {}", personId);
        }
    }

    @SuppressWarnings("unused")
    public PictureInfo fallbackGetPlayerPicture(int id, Throwable throwable)  {
        log.warn("playerPictureFromAPI: Cannot get the picture for the player with statsApiId: {}", id);
        PictureInfo defaultPicture = new PictureInfo();
        defaultPicture.setUrl("https://img.mlbstatic.com/mlb-photos/image/upload/v1/people/generic/headshot/67/current");
        return defaultPicture;
    }

    private StatData getData(PlayerDetailInfo playerDetailInfo) {
        if (playerDetailInfo.stats() != null && !playerDetailInfo.stats().isEmpty()) {
            return playerDetailInfo.stats().getFirst().splits().getFirst().stat();
        }
        return null;
    }

    /**
     * Map the position player's data obtained from the API, into the one that this application manages and store it in the database.
     *
     * @param playerDetailInfo player's info
     * @param data stats of the player.
     * @param team of the player.
     */
    private void savePositionPlayer(PlayerDetailInfo playerDetailInfo, StatData data, Team team) {
        PositionPlayer positionPlayer = this.positionPlayerRepository.findByName(playerDetailInfo.fullName()).orElse(new PositionPlayer());
        this.saveCommonData(positionPlayer, playerDetailInfo, team);
        try {
            String posAbbreviation = playerDetailInfo.primaryPosition().abbreviation();
            positionPlayer.setPosition(PlayerPositions.fromLabel(posAbbreviation));
        }
        catch (IllegalArgumentException ex) {
            log.error("Unknown Position Player Position: {}", playerDetailInfo.primaryPosition().abbreviation());
        }

        if (data != null) {
            positionPlayer.createWithStats(
                    Objects.requireNonNullElse(data.atBats(), 0),
                    Objects.requireNonNullElse(data.baseOnBalls(), 0),
                    Objects.requireNonNullElse(data.hits(), 0),
                    Objects.requireNonNullElse(data.doubles(), 0),
                    Objects.requireNonNullElse(data.triples(), 0),
                    Objects.requireNonNullElse(data.homeRuns(), 0),
                    Objects.requireNonNullElse(data.rbi(), 0),
                    Objects.requireNonNullElse(data.avg(), 0.000),
                    Objects.requireNonNullElse(data.ops(), 0.000),
                    Objects.requireNonNullElse(data.obp(), 0.000),
                    Objects.requireNonNullElse(data.slg(), 0.000)
            );
        }
        else {
            positionPlayer.createWithNoStats();
        }
        this.positionPlayerRepository.save(positionPlayer);
    }

    /**
     * Map the pitcher's data obtained from the API, into the one that this application manages and store it in the database.
     *
     * @param playerDetailInfo player's info
     * @param data stats of the player.
     * @param team of the player.
     */
    private void savePitcher(PlayerDetailInfo playerDetailInfo, StatData data, Team team) {
        Pitcher pitcher = this.pitcherRepository.findByName(playerDetailInfo.fullName()).orElse(new Pitcher());
        this.saveCommonData(pitcher, playerDetailInfo, team);
        try {
            String posAbbreviation = playerDetailInfo.primaryPosition().abbreviation();
            pitcher.setPosition(PitcherPositions.fromLabel(posAbbreviation));
        }
        catch (IllegalArgumentException ex) {
            log.error("Unknown Pitcher Position: {}", playerDetailInfo.primaryPosition().abbreviation());
        }

        if (data != null) {
            pitcher.createWithStats(
                    Objects.requireNonNullElse(data.gamesPlayed(), 0),
                    Objects.requireNonNullElse(data.wins(), 0),
                    Objects.requireNonNullElse(data.losses(), 0),
                    Double.parseDouble(data.inningsPitched()),
                    Objects.requireNonNullElse(data.strikeOuts(), 0),
                    Objects.requireNonNullElse(data.baseOnBalls(), 0),
                    Objects.requireNonNullElse(data.hits(), 0),
                    Objects.requireNonNullElse(data.runs(), 0),
                    Objects.requireNonNullElse(data.saves(), 0),
                    Objects.requireNonNullElse(data.saveOpportunities(), 0),
                    Objects.requireNonNullElse(data.era(), 0.000),
                    Objects.requireNonNullElse(data.whip(), 0.000)
            );
        }
        else {
            pitcher.createWithNoStats();
        }
        this.pitcherRepository.save(pitcher);
    }

    /**
     * Auxiliary method that manages common data for players.
     *
     * @param player siad player.
     * @param data of said player.
     * @param team of said player.
     */
    public <T extends Player> void saveCommonData(T player, PlayerDetailInfo data, Team team) {
        player.setStatsApiId(data.id());
        player.setName(data.fullName());
        player.setPlayerNumber(this.parsePlayerNumber(data.primaryNumber()));
        player.setTeam(team);
        player.setPicture(this.savePlayerPicture(data.id()));
    }

    /**
     * Auxiliary method that do a save parser for the player's number
     *
     * @param number to parse.
     *
     * @return the number converted to integer.
     */
    private int parsePlayerNumber(String number) {
        try {
            return (number != null && !number.isEmpty()) ? Integer.parseInt(number) : 0;
        }
        catch (NumberFormatException e) {
            return 0;
        }
    }
}