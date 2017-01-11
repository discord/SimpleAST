package com.discord.simpleast.core.utils;

import com.discord.simpleast.core.node.Node;

import java.util.Collection;

public class ASTUtils {
    public static void traversePostOrder(final Collection<? extends Node> ast, final NodeProcessor nodeProcessor) {
        for (final Node node : ast) {
            traversePostOrderSubtree(node, nodeProcessor);
        }
    }

    private static void traversePostOrderSubtree(final Node node, final NodeProcessor nodeProcessor) {
        if (node.hasChildren()) {
            final Iterable<Node> children = node.getChildren();
            for (final Node child : children) {
                traversePostOrderSubtree(child, nodeProcessor);
            }
        }

        nodeProcessor.processNode(node);
    }
}
