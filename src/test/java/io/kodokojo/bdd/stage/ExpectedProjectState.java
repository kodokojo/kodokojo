package io.kodokojo.bdd.stage;

import org.apache.commons.lang.StringUtils;

import java.util.List;

public class ExpectedProjectState {

    private final List<String> stackNamePresents;

    private final List<String> brickNamePresents;

    public ExpectedProjectState(List<String> stackNamePresents, List<String> brickNamePresents) {
        this.stackNamePresents = stackNamePresents;
        this.brickNamePresents = brickNamePresents;
    }

    public List<String> getStackNamePresents() {
        return stackNamePresents;
    }

    public List<String> getBrickNamePresents() {
        return brickNamePresents;
    }

    @Override
    public String toString() {
        return StringUtils.join(brickNamePresents, ",");
    }
}