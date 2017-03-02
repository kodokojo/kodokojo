package io.kodokojo.commons.service.repository.search;

import static org.apache.commons.lang.StringUtils.isBlank;

public class Criteria {

    private final String field;

    private final CriteriaOperator operator;

    private final String value;

    public Criteria(String field, CriteriaOperator operator, String value) {
        if (isBlank(field)) {
            throw new IllegalArgumentException("field must be defined.");
        }
        if (value == null) {
            throw new IllegalArgumentException("value must be defined.");
        }
        this.field = field;
        if (operator == null) {
            this.operator = CriteriaOperator.MATCH;
        } else {
            this.operator = operator;
        }
        this.value = value;
    }

    public Criteria(String field, String value) {
        this(field, null, value);
    }

    public String getField() {
        return field;
    }

    public CriteriaOperator getOperator() {
        return operator;
    }

    public String getValue() {
        return value;
    }

    public enum CriteriaOperator {
        MUST_BE,
        MATCH,
        COULD_BE,
        EXCLUDE
    }
}