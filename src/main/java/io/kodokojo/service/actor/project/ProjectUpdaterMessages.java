package io.kodokojo.service.actor.project;

import io.kodokojo.model.Project;
import io.kodokojo.model.User;
import io.kodokojo.service.actor.message.UserRequestMessage;

import static java.util.Objects.requireNonNull;

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

    class ListAndUpdateUserToProjectMsg extends UserRequestMessage {

        private final User user;

        public ListAndUpdateUserToProjectMsg(User requester, User user) {
            super(requester);
            requireNonNull(user, "user must be defined.");
            this.user = user;
        }

        public User getUser() {
            return user;
        }
    }

    class ListAndUpdateUserToProjectResultMsg extends UserRequestMessage {

        private final ListAndUpdateUserToProjectMsg request;

        private final boolean success;

        public ListAndUpdateUserToProjectResultMsg(User requester, ListAndUpdateUserToProjectMsg request, boolean success) {
            super(requester);
            requireNonNull(request, "request must be defined.");
            this.request = request;
            this.success = success;
        }

        public ListAndUpdateUserToProjectMsg getRequest() {
            return request;
        }

        public boolean isSuccess() {
            return success;
        }
    }
}
