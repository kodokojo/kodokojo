package io.kodokojo.brick;

import org.apache.commons.lang.StringUtils;

import static org.apache.commons.lang.StringUtils.isBlank;

public class DefaultBrickUrlFactory implements BrickUrlFactory {

    public static final String DOMAIN_FORMAT_WITHOUT_ENTITY = "%s-%s.%s";

    public static final String DOMAIN_FORMAT_WITH_ENTITY = "%s-%s-%s.%s";

    private final String baseDomainename;

    public DefaultBrickUrlFactory(String baseDomainename) {
        if (isBlank(baseDomainename)) {
            throw new IllegalArgumentException("baseDomainename must be defined.");
        }
        this.baseDomainename = baseDomainename;
    }

    @Override
    public String forgeUrl(String entity, String projectName, String brickType) {
        if (isBlank(projectName)) {
            throw new IllegalArgumentException("projectName must be defined.");
        }
        if (isBlank(brickType)) {
            throw new IllegalArgumentException("brickName must be defined.");
        }
        if (StringUtils.isBlank(entity)) {
            return String.format(DOMAIN_FORMAT_WITHOUT_ENTITY, brickType.toLowerCase(), projectName.toLowerCase(), baseDomainename);
        } else {
            return String.format(DOMAIN_FORMAT_WITH_ENTITY, brickType.toLowerCase(), projectName.toLowerCase(), entity.toLowerCase(), baseDomainename);
        }
    }
}
