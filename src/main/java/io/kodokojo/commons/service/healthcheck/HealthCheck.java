package io.kodokojo.commons.service.healthcheck;

import java.io.Serializable;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang.StringUtils.isBlank;

public class HealthCheck implements Serializable {

    private final String name;

    private final long startDate;

    private final long endDate;

    private final State state;

    private final String detail;

    private HealthCheck(String name, long startDate, long endDate, State state, String detail) {
        if (isBlank(name)) {
            throw new IllegalArgumentException("name must be defined.");
        }
        requireNonNull(state, "state must be defined.");
        if (endDate < startDate) {
            throw new IllegalArgumentException("Start date must be before End date.");
        }

        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.state = state;
        this.detail = detail;
    }

    public String getName() {
        return name;
    }

    public long getStartDate() {
        return startDate;
    }

    public long getEndDate() {
        return endDate;
    }

    public State getState() {
        return state;
    }

    public String getDetail() {
        return detail;
    }

    @Override
    public String toString() {
        return "HealthCheck{" +
                "name='" + name + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", state=" + state +
                ", detail='" + detail + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HealthCheck that = (HealthCheck) o;

        if (startDate != that.startDate) return false;
        if (endDate != that.endDate) return false;
        if (!name.equals(that.name)) return false;
        return state == that.state;
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + (int) (startDate ^ (startDate >>> 32));
        result = 31 * result + (int) (endDate ^ (endDate >>> 32));
        result = 31 * result + state.hashCode();
        return result;
    }

    public enum State {
        OK,
        FAIL
    }

    public static class Builder {

        private String name;

        private long startDate;

        private long endDate;

        private State state;

        private String detail;

        public Builder() {
            startDate = System.currentTimeMillis();
        }

        public Builder(HealthCheck toCopy) {
            this.name = toCopy.name;
            this.startDate = toCopy.startDate;
            this.state = toCopy.state;
            this.detail = toCopy.detail;
            startDate = System.currentTimeMillis();
        }

        public HealthCheck build() {
            if (endDate <= 0) {
                endDate = System.currentTimeMillis();
            }
            return new HealthCheck(name, startDate, endDate, state, detail);
        }

        public Builder setName(String name) {
            this.name = name;
            return this;
        }

        public Builder setStartDate(long startDate) {
            this.startDate = startDate;
            return this;
        }

        public Builder setEndDate(long endDate) {
            this.endDate = endDate;
            return this;
        }

        public Builder setState(State state) {
            this.state = state;
            return this;
        }

        public Builder setDetail(String detail) {
            this.detail = detail;
            return this;
        }
    }

}
