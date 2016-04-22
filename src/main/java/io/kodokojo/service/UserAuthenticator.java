package io.kodokojo.service;

/*
 * #%L
 * project-manager
 * %%
 * Copyright (C) 2016 Kodo-kojo
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import io.kodokojo.model.User;
import io.kodokojo.service.user.Credential;

/**
 * Extract {@link User} from a {@link Credential}.
 * @param <T>
 */
public interface UserAuthenticator<T extends Credential> {

    /**
     * Check if credentials match with a User.
     * @param credentials The credentials to test.
     * @return The user which match credentials, <code>null</code> else.
     */
    User authenticate(T credentials);

}
