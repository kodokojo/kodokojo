package io.kodokojo.project.launcher;

public interface ConfigurerFactory<C,L> {

    ProjectConfigurer<C,L> create();

}
