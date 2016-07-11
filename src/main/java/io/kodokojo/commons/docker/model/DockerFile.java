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
package io.kodokojo.commons.docker.model;


public class DockerFile {

    private final ImageName imageName;

    private final ImageName from;

    private final String maintainer;

    public DockerFile(ImageName imageName, ImageName from, String maintainer) {
        if (imageName == null) {
            throw new IllegalArgumentException("imageName must be defined.");
        }
        this.imageName = imageName;
        this.from = from;
        this.maintainer = maintainer;
    }

    public DockerFile(ImageName imageName) {
        this(imageName,null,null);
    }

    public DockerFile(ImageName imageName, ImageName from) {
        this(imageName, from, null);
    }

    public DockerFile(String imageName) {
        this(new ImageName(imageName));
    }

    public ImageName getImageName() {
        return imageName;
    }

    public ImageName getFrom() {
        return from;
    }

    public String getMaintainer() {
        return maintainer;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DockerFile that = (DockerFile) o;

        return imageName.equals(that.imageName);

    }

    @Override
    public int hashCode() {
        return imageName.hashCode();
    }

    @Override
    public String toString() {
        return "DockerFile{" +
                "imageName=" + imageName +
                ", from=" + from +
                ", maintainer='" + maintainer + '\'' +
                '}';
    }

}

