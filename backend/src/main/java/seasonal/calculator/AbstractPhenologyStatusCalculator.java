package seasonal.calculator;

import seasonal.domain.ObservationRecord;
import seasonal.enums.PhenologyStage;

import java.time.LocalDate;
import java.util.Objects;

public abstract class AbstractPhenologyStatusCalculator implements PhenologyStatusCalculator {
    /** ENDED 미기록 시 자동 종료까지의 유예 일수 */
    private static final int ASSUMED_END_DAYS = 7;

    @Override
    public final PhenologyStage calculate(LocalDate queryDate, ObservationRecord record) {
        Objects.requireNonNull(queryDate, "조회 날짜는 null일 수 없습니다.");
        Objects.requireNonNull(record, "관측 기록은 null일 수 없습니다.");

        if (queryDate.isBefore(record.getObservedDate())) {
            return PhenologyStage.BEFORE_START;
        }

        PhenologyStage stage = calculateObservedStage(queryDate, record);

        // ENDED가 끝내 기록되지 않은 경우: 마지막 관측일 + 7일 이후는 종료로 간주
        if (stage != PhenologyStage.ENDED
                && !queryDate.isBefore(record.getObservedDate().plusDays(ASSUMED_END_DAYS))) {
            return PhenologyStage.ENDED;
        }

        return stage;
    }

    protected abstract PhenologyStage calculateObservedStage(LocalDate queryDate, ObservationRecord record);
}
