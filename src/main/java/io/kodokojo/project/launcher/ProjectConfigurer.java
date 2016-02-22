package io.kodokojo.project.launcher;

public interface ProjectConfigurer<E,R> {

    R configure(E entrypoint);
}
