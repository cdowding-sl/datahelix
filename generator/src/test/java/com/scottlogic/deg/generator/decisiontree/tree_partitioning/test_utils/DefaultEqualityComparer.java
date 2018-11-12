package com.scottlogic.deg.generator.decisiontree.tree_partitioning.test_utils;

public class DefaultEqualityComparer implements EqualityComparer {
    private final EqualityComparer collectionComparer;

    public DefaultEqualityComparer() {
        this.collectionComparer = new CollectionEqualityComparer(this);
    }

    public DefaultEqualityComparer(EqualityComparer collectionComparer) {
        this.collectionComparer = collectionComparer;
    }

    @Override
    public int getHashCode(Object item) {
        return item.hashCode();
    }

    @Override
    public boolean equals(Object item1, Object item2) {
        if (item1 == null && item2 == null)
            return true;
        if (item1 == null || item2 == null)
            return false;

        if (CollectionEqualityComparer.isCollection(item1) && CollectionEqualityComparer.isCollection(item2)) {
            return collectionComparer.equals(item1, item2);
        }

        return item1.equals(item2);
    }
}
