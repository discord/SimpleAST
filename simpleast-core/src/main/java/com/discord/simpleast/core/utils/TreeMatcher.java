package com.discord.simpleast.core.utils;

import android.text.style.CharacterStyle;

import com.discord.simpleast.core.node.Node;
import com.discord.simpleast.core.node.StyleNode;
import com.discord.simpleast.core.node.TextNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Compares two trees represented by {@link List}s of {@link Node}, primarily for testing
 * purposes.
 *
 * Clients can register {@link NodeMatcher}s to tell {@link TreeMatcher} how to
 * verify equality of their own custom nodes.
 */
public class TreeMatcher {

    private Map<Class, NodeMatcher> matchers = new HashMap<>();

    /**
     * @param tree1 first tree to consider.
     * @param tree2 second tree to consider.
     *
     * @return true if both trees are the same (same size, same classes of {@link Node},
     * and optionally that nodes match according to the provided matchers.
     */
    public boolean matches(final List<? extends Node> tree1, final List<? extends Node> tree2) {
        final List<Node> tree1PostOrder = new ArrayList<>();
        final List<Node> tree2PostOrder = new ArrayList<>();

        // Build a post-order walk of the first tree.
        ASTUtils.traversePostOrder(tree1, new NodeProcessor() {
            @Override
            public void processNode(Node node) {
                tree1PostOrder.add(node);
            }
        });

        // Build a post-order walk of the second tree.
        ASTUtils.traversePostOrder(tree2, new NodeProcessor() {
            @Override
            public void processNode(Node node) {
                tree2PostOrder.add(node);
            }
        });

        if (tree1PostOrder.size() != tree2PostOrder.size()) {
            return false;
        }

        // Proceed through the two walks step-by-step, comparing each
        // node along the way.
        for (int i = 0; i < tree1PostOrder.size(); i++) {
            final Node node1 = tree1PostOrder.get(i);
            final Node node2 = tree2PostOrder.get(i);

            if (node1.getClass() != node2.getClass()) {
                return false;
            }

            final Class clazz = node1.getClass();
            if (matchers.containsKey(clazz)) {
                if (!matchers.get(clazz).matches(node1, node2)) {
                    return false;
                }
            }
        }

        return true;
    }

    public void registerMatcher(final Class clazz, final NodeMatcher matcher) {
        matchers.put(clazz, matcher);
    }

    public void registerDefaultMatchers() {

        registerMatcher(TextNode.class, new TreeMatcher.NodeMatcher() {
            @Override
            public boolean matches(Node node1, Node node2) {
                final TextNode textNode1 = (TextNode) node1;
                final TextNode textNode2 = (TextNode) node2;

                return textNode1.getContent().equals(textNode2.getContent());
            }
        });

        registerMatcher(StyleNode.class, new TreeMatcher.NodeMatcher() {
            @Override
            public boolean matches(Node node1, Node node2) {
                final StyleNode styleNode1 = (StyleNode) node1;
                final StyleNode styleNode2 = (StyleNode) node2;

                final List<CharacterStyle> styles1 = styleNode1.getStyles();
                final List<CharacterStyle> styles2 = styleNode2.getStyles();

                if (styles1.size() != styles2.size()) {
                    return false;
                }

                for (int i = 0; i < styles1.size(); i++) {
                    final CharacterStyle style1 = styles1.get(i);
                    final CharacterStyle style2 = styles2.get(i);

                    if (style1.getClass() != style2.getClass()) {
                        return false;
                    }
                }

                return true;
            }
        });
    }

    interface NodeMatcher {
        boolean matches(Node node1, Node node2);
    }
}
