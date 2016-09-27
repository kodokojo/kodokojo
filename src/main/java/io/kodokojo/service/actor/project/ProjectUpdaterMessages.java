package io.kodokojo.service.actor.project;

import io.kodokojo.model.Project;
import io.kodokojo.model.User;
import io.kodokojo.service.actor.message.UserRequestMessage;

public interface ProjectUpdaterMessages {

    class ProjectUpdateMsg extends UserRequestMessage {

        final Project project;

        public ProjectUpdateMsg(User requester, Project project) {
            super(requester);
            if (project == null) {
                throw new IllegalArgumentException("project must be defined.");
            }
            this.project = project;
        }
    }

    class ProjectUpdateResultMsg extends UserRequestMessage {

        private final Project project;

        public ProjectUpdateResultMsg(User requester, Project project) {
            super(requester);
            if (project == null) {
                throw new IllegalArgumentException("project must be defined.");
            }
            this.project = project;
        }

        public Project getProject() {
            return project;
        }
    }

    class ProjectUpdateNotAuthoriseMsg extends UserRequestMessage {

        private final Project project;

        public ProjectUpdateNotAuthoriseMsg(User requester, Project project) {
            super(requester);
            if (project == null) {
                throw new IllegalArgumentException("project must be defined.");
            }
            this.project = project;
        }

        public Project getProject() {
            return project;
        }
    }
}
