/*
 * Copyright (c) 2021-2026 Deepesh Patel
 * SPDX-License-Identifier: Apache-2.0
 */
package io.github.deepeshpatel.jnumbertools.generator.product.conditional;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

    /**
     * Immutable AVL map used for dimension-pair relations. Updates copy only the search path, while
     * iteration remains in ascending key order so model construction is deterministic.
     */
final class PersistentRelationMap extends AbstractMap<Long, Relation> {
        private static final PersistentRelationMap EMPTY = new PersistentRelationMap(null);
        private final RelationNode root;
        private transient Set<Entry<Long, Relation>> entries;

        private PersistentRelationMap(RelationNode root) {
            this.root = root;
        }

        static PersistentRelationMap empty() {
            return EMPTY;
        }

        PersistentRelationMap with(long key, Relation value) {
            return new PersistentRelationMap(put(root, key, value));
        }

        @Override
        public Relation get(Object key) {
            if (!(key instanceof Long)) return null;
            long target = (Long) key;
            RelationNode node = root;
            while (node != null) {
                if (target == node.key) return node.value;
                node = target < node.key ? node.left : node.right;
            }
            return null;
        }

        @Override
        public boolean containsKey(Object key) {
            if (!(key instanceof Long)) return false;
            long target = (Long) key;
            RelationNode node = root;
            while (node != null) {
                if (target == node.key) return true;
                node = target < node.key ? node.left : node.right;
            }
            return false;
        }

        @Override
        public int size() {
            return size(root);
        }

        @Override
        public Set<Entry<Long, Relation>> entrySet() {
            if (entries == null) {
                entries = new AbstractSet<>() {
                    @Override public int size() { return PersistentRelationMap.this.size(); }

                    @Override
                    public Iterator<Entry<Long, Relation>> iterator() {
                        return new Iterator<>() {
                            private final ArrayDeque<RelationNode> stack = new ArrayDeque<>();
                            { pushLeft(root); }

                            private void pushLeft(RelationNode node) {
                                while (node != null) {
                                    stack.push(node);
                                    node = node.left;
                                }
                            }

                            @Override public boolean hasNext() { return !stack.isEmpty(); }

                            @Override
                            public Entry<Long, Relation> next() {
                                if (stack.isEmpty()) throw new NoSuchElementException();
                                RelationNode node = stack.pop();
                                pushLeft(node.right);
                                return new SimpleImmutableEntry<>(node.key, node.value);
                            }
                        };
                    }
                };
            }
            return entries;
        }

        private static RelationNode put(RelationNode node, long key, Relation value) {
            if (node == null) return new RelationNode(key, value, null, null);
            if (key == node.key) return new RelationNode(key, value, node.left, node.right);
            RelationNode updated = key < node.key
                    ? new RelationNode(node.key, node.value, put(node.left, key, value), node.right)
                    : new RelationNode(node.key, node.value, node.left, put(node.right, key, value));
            return balance(updated);
        }

        private static RelationNode balance(RelationNode node) {
            int factor = height(node.left) - height(node.right);
            if (factor > 1) {
                if (height(node.left.left) < height(node.left.right)) {
                    return rotateRight(new RelationNode(node.key, node.value,
                            rotateLeft(node.left), node.right));
                }
                return rotateRight(node);
            }
            if (factor < -1) {
                if (height(node.right.right) < height(node.right.left)) {
                    return rotateLeft(new RelationNode(node.key, node.value,
                            node.left, rotateRight(node.right)));
                }
                return rotateLeft(node);
            }
            return node;
        }

        private static RelationNode rotateRight(RelationNode node) {
            RelationNode pivot = node.left;
            RelationNode right = new RelationNode(node.key, node.value, pivot.right, node.right);
            return new RelationNode(pivot.key, pivot.value, pivot.left, right);
        }

        private static RelationNode rotateLeft(RelationNode node) {
            RelationNode pivot = node.right;
            RelationNode left = new RelationNode(node.key, node.value, node.left, pivot.left);
            return new RelationNode(pivot.key, pivot.value, left, pivot.right);
        }

        private static int height(RelationNode node) { return node == null ? 0 : node.height; }
        private static int size(RelationNode node) { return node == null ? 0 : node.size; }

    private static final class RelationNode {
        final long key;
        final Relation value;
        final RelationNode left;
        final RelationNode right;
        final int height;
        final int size;

        RelationNode(long key, Relation value, RelationNode left, RelationNode right) {
            this.key = key;
            this.value = value;
            this.left = left;
            this.right = right;
            this.height = 1 + Math.max(left == null ? 0 : left.height, right == null ? 0 : right.height);
            this.size = 1 + (left == null ? 0 : left.size) + (right == null ? 0 : right.size);
        }
    }
}
