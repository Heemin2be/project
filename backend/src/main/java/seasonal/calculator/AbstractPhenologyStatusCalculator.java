package seasonal.calculator;

import seasonal.domain.ObservationRecord;
import seasonal.enums.PhenologyStage;

import java.time.LocalDate;
import java.util.Objects;

public abstract class AbstractPhenologyStatusCalculator implements PhenologyStatusCalculator {
    @Override
    public final PhenologyStage calculate(LocalDate queryDate, ObservationRecord record) {
        Objects.requireNonNull(queryDate, "조회 날짜는 null일 수 없습니다.");
        Objects.requireNonNull(record, "관측 기록은 null일 수 없습니다.");

        if (queryDate.isBefore(record.getObservedDate())) {
            return PhenologyStage.BEFORE_START;
        }
        return calculateObservedStage(queryDate, record);
    }

    protected abstract PhenologyStage calculateObservedStage(LocalDate queryDate, ObservationRecord record);
}
