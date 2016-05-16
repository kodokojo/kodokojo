/**
 * Kodo Kojo - Software factory done right
 * Copyright © 2016 Kodo Kojo (infos@kodokojo.io)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package io.kodokojo.brick;

public class BrickAlreadyExist extends Exception {

    private final String brickName;

    private final String projectName;

    public BrickAlreadyExist(String brickName, String projectName) {
        this.brickName = brickName;
        this.projectName = projectName;
    }

    public BrickAlreadyExist(String message, String brickName, String projectName) {
        super(message);
        this.brickName = brickName;
        this.projectName = projectName;
    }

    public BrickAlreadyExist(String message, Throwable cause, String brickName, String projectName) {
        super(message, cause);
        this.brickName = brickName;
        this.projectName = projectName;
    }

    public BrickAlreadyExist(Throwable cause, String brickName, String projectName) {
        super(cause);
        this.brickName = brickName;
        this.projectName = projectName;
    }

    public BrickAlreadyExist(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace, String brickName, String projectName) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.brickName = brickName;
        this.projectName = projectName;
    }

    public String getBrickName() {
        return brickName;
    }

    public String getProjectName() {
        return projectName;
    }
}
