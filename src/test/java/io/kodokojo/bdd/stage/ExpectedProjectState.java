package io.kodokojo.bdd.stage;

import io.kodokojo.model.BrickType;
import org.apache.commons.lang.StringUtils;

import java.util.List;

public class ExpectedProjectState {

    private final List<String> stackNamePresents;

    private final List<BrickType> brickTypePresents;

    public ExpectedProjectState(List<String> stackNamePresents, List<BrickType> brickTypePresents) {
        this.stackNamePresents = stackNamePresents;
        this.brickTypePresents = brickTypePresents;
    }

    public List<String> getStackNamePresents() {
        return stackNamePresents;
    }

    public List<BrickType> getBrickTypePresents() {
        return brickTypePresents;
    }

    @Override
    public String toString() {
        return StringUtils.join(brickTypePresents, ",");
    }
}