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

import io.kodokojo.commons.utils.DockerImageNameBaseListener;
import io.kodokojo.commons.utils.DockerImageNameLexer;
import io.kodokojo.commons.utils.DockerImageNameParser;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.misc.NotNull;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

public class StringToImageNameConverter implements Function<String, ImageName> {

    private static final Logger LOGGER = LoggerFactory.getLogger(StringToImageNameConverter.class);

    public static ImageName convert(String input) {
        StringToImageNameConverter converter = new StringToImageNameConverter();
        return converter.apply(input);
    }

    @Override
    public ImageName apply(String input) {
        if (StringUtils.isBlank(input)) {
            throw new IllegalArgumentException("input  must be defined.");
        }
        input = input.trim();
        if (!input.endsWith("\n")) {
            input += "\n";
        }
        try {
            ANTLRInputStream antlrInputStream = new ANTLRInputStream(input);
            DockerImageNameLexer lexer = new DockerImageNameLexer(antlrInputStream);
            lexer.removeErrorListeners();
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            DockerImageNameParser parser = new DockerImageNameParser(tokens);
            DockerImageAntlrListener listener = new DockerImageAntlrListener();
            parser.addParseListener(listener);
            parser.removeErrorListeners();
            parser.imageName();


            return listener.getImageName();
        } catch (RecognitionException e) {
            LOGGER.debug("Unable to parse following image name '{}' : {}", input , e);
            return null;
        } catch (RuntimeException e ) {

            LOGGER.debug("Unable to parse following image name '{}' : {}", input, e);
            return null;
        }
    }

    private class DockerImageAntlrListener extends DockerImageNameBaseListener {

        private ImageNameBuilder builder = new ImageNameBuilder();

        @Override
        public void exitNamespace(@NotNull DockerImageNameParser.NamespaceContext ctx) {
            builder.setNamespace(ctx.getText());
        }

        @Override
        public void exitName(@NotNull DockerImageNameParser.NameContext ctx) {
            builder.setName(ctx.getText());
        }

        @Override
        public void exitTag(@NotNull DockerImageNameParser.TagContext ctx) {
            builder.setTag(ctx.getText());
        }

        @Override
        public void exitRepository(@NotNull DockerImageNameParser.RepositoryContext ctx) {
            builder.setRepository(ctx.getText());
        }

        public ImageName getImageName() {
            return builder.build();
        }
    }
}
