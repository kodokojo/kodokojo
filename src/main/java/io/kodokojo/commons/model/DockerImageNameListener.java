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