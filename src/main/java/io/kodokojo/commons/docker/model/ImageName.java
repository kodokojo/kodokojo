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

import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.List;

public class ImageName {

    public static final String DEFAULT_NAMESPACE = "library";

    public static final String TAG_LATEST = "latest";

    private final String repository;

    private final String namespace;

    private final String name;

    private final String tag;

    private final List<String> tags;

    public ImageName(String repository, String namespace, String name, String tag){
        if (StringUtils.isNotBlank(repository)) {
            this.repository = repository.toLowerCase();
        } else {
            this.repository = null;
        }
        if (StringUtils.isBlank(namespace)) {
            this.namespace = DEFAULT_NAMESPACE;
        } else {
            this.namespace = namespace.toLowerCase();
        }
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("name  must be defined.");
        }
        this.name = name.toLowerCase();
        if (StringUtils.isBlank(tag)) {
            this.tag = TAG_LATEST;
        } else {
            this.tag = tag.toLowerCase();
        }
        this.tags = Collections.singletonList(this.tag);
    }

    public ImageName(String namespace, String name, String tag) {
        this(null, namespace, name, tag);
    }

    public ImageName(String repository, String namespace, String name, List<String> tags) {
        this.repository = repository;
        if (StringUtils.isBlank(namespace)) {
            this.namespace = DEFAULT_NAMESPACE;
        } else {
            this.namespace = namespace;
        }
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("name  must be defined.");
        }
        if (tags  == null) {
            throw new IllegalArgumentException("tags must be defined.");
        }
        this.name = name;
        this.tag = null;
        this.tags = tags;

    }

    public ImageName(String namespace, String name, List<String> tags) {
        this(null, namespace, name, tags);
    }

    public ImageName(String namespace, String name) {
        this(namespace, name, Collections.emptyList());
    }

    public ImageName(String name) {
        this(DEFAULT_NAMESPACE, name);
    }

    public boolean isFullyQualified() {
        return StringUtils.isNotBlank(tag);
    }

    public boolean isRootImage() {
        return DEFAULT_NAMESPACE.equals(namespace);
    }

    public String getFullyQualifiedName() {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotBlank(repository)) {
            sb.append(repository).append("/");
        }
        if (StringUtils.isNotBlank(namespace)) {
            sb.append(namespace).append("/");
        }
        sb.append(name);
        if (StringUtils.isNotBlank(tag)) {
            sb.append(":").append(tag);
        }
        return sb.toString();
    }

    public String getShortNameWithoutTag() {
        StringBuilder sb = new StringBuilder();
        if (!DEFAULT_NAMESPACE.equals(namespace)) {
            sb.append(namespace).append("/");
        }
        sb.append(name);
        return sb.toString();
    }

    public String getShortName() {
        StringBuilder sb = new StringBuilder();
        sb.append(getShortNameWithoutTag());
        if (isFullyQualified()) {
            sb.append(":").append(tag);
        }
        return sb.toString();
    }

    public String getDockerImageName() {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotBlank(repository)) {
            sb.append(repository).append("/");
        }
        sb.append(getShortName());
        return sb.toString();
    }

    public String getRepository() {
        return repository;
    }

    public String getNamespace() {
        return namespace;
    }

    public String getName() {
        return name;
    }

    public String getTag() {
        return tag;
    }

    public List<String> getTags() {
        return tags;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ImageName imageName = (ImageName) o;

        if (repository != null ? !repository.equals(imageName.repository) : imageName.repository != null) return false;
        if (!namespace.equals(imageName.namespace)) return false;
        if (!name.equals(imageName.name)) return false;
        return tag != null ? tag.equals(imageName.tag) : imageName.tag == null ;

    }

    @Override
    public int hashCode() {
        int result = repository != null ? repository.hashCode() : 0;
        result = 31 * result + namespace.hashCode();
        result = 31 * result + name.hashCode();
        result = 31 * result + (tag != null ? tag.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ImageName{" +
                "repository='" + repository + '\'' +
                ", namespace='" + namespace + '\'' +
                ", name='" + name + '\'' +
                ", tag='" + tag + '\'' +
                ", tags=" + tags +
                '}';
    }
}

