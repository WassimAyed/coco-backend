package tn.esprit.eventservice.service;

import tn.esprit.eventservice.dto.StatsDTO;

public interface IStatsService {
    StatsDTO getGlobalStats();
}