package blps.duo.services;

import blps.duo.model.ExpandedStatistic;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Comparator;


@Service
@RequiredArgsConstructor
public class ReportService {

    private final StatisticService statisticService;

    public Mono<String> getSmallReportByRaceIdAndOffset(Long raceId, Long offset) {

        return statisticService.getExpandedStatisticByRaceIdAndOffset(raceId, offset)
                .collectList()
                .map(list -> {
                    StringBuilder sb = new StringBuilder();

                    long likeCount;
                    long dislikeCount;
                    String mostActivePerson;
                    String mostPopularRecipe;
                    String mostUnpopularRecipe;

                    mostPopularRecipe = list.stream().max(Comparator.comparingDouble(ExpandedStatistic::getRecipeRating))
                            .map(ExpandedStatistic::getRecipeTitle).orElse("N/A");

                    mostUnpopularRecipe = list.stream().min(Comparator.comparingDouble(ExpandedStatistic::getRecipeRating))
                            .map(ExpandedStatistic::getRecipeTitle).orElse("N/A");

                    mostActivePerson = list.stream().max(Comparator.comparingLong(ExpandedStatistic::getPersonRatingCount))
                            .map(ExpandedStatistic::getPersonEmail).orElse("N/A");

                    likeCount = list.stream().filter(expandedStatistic -> expandedStatistic.getValue() > 0).count();
                    dislikeCount = list.size() - likeCount;

                    sb.append("Most Active Person: ").append(mostActivePerson).append("\n");
                    sb.append("Most Popular Recipe: ").append(mostPopularRecipe).append("\n");
                    sb.append("Most Unpopular Recipe: ").append(mostUnpopularRecipe).append("\n");
                    sb.append("Total Like Count: ").append(likeCount).append("\n");
                    sb.append("Total Dislike Count: ").append(dislikeCount).append("\n");

                    return sb.toString();
                });
    }
}
