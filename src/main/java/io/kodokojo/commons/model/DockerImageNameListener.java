/**
 * Kodo Kojo - Software factory done right
 * Copyright © 2017 Kodo Kojo (infos@kodokojo.io)
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
// Generated from io/kodokojo/commons/utils/DockerImageName.g4 by ANTLR 4.3
package io.kodokojo.commons.model;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link DockerImageNameParser}.
 */
public interface DockerImageNameListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link DockerImageNameParser#imageName}.
	 * @param ctx the parse tree
	 */
	void enterImageName(@NotNull DockerImageNameParser.ImageNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DockerImageNameParser#imageName}.
	 * @param ctx the parse tree
	 */
	void exitImageName(@NotNull DockerImageNameParser.ImageNameContext ctx);

	/**
	 * Enter a parse tree produced by {@link DockerImageNameParser#namespace}.
	 * @param ctx the parse tree
	 */
	void enterNamespace(@NotNull DockerImageNameParser.NamespaceContext ctx);
	/**
	 * Exit a parse tree produced by {@link DockerImageNameParser#namespace}.
	 * @param ctx the parse tree
	 */
	void exitNamespace(@NotNull DockerImageNameParser.NamespaceContext ctx);

	/**
	 * Enter a parse tree produced by {@link DockerImageNameParser#name}.
	 * @param ctx the parse tree
	 */
	void enterName(@NotNull DockerImageNameParser.NameContext ctx);
	/**
	 * Exit a parse tree produced by {@link DockerImageNameParser#name}.
	 * @param ctx the parse tree
	 */
	void exitName(@NotNull DockerImageNameParser.NameContext ctx);

	/**
	 * Enter a parse tree produced by {@link DockerImageNameParser#tag}.
	 * @param ctx the parse tree
	 */
	void enterTag(@NotNull DockerImageNameParser.TagContext ctx);
	/**
	 * Exit a parse tree produced by {@link DockerImageNameParser#tag}.
	 * @param ctx the parse tree
	 */
	void exitTag(@NotNull DockerImageNameParser.TagContext ctx);

	/**
	 * Enter a parse tree produced by {@link DockerImageNameParser#repository}.
	 * @param ctx the parse tree
	 */
	void enterRepository(@NotNull DockerImageNameParser.RepositoryContext ctx);
	/**
	 * Exit a parse tree produced by {@link DockerImageNameParser#repository}.
	 * @param ctx the parse tree
	 */
	void exitRepository(@NotNull DockerImageNameParser.RepositoryContext ctx);
}