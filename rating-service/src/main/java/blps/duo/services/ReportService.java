package blps.duo.services;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final StatisticService statisticService;

    public String getSmallReportByRaceIdAndOffset(Long raceId, int offset) {
        return null;
    }
}
