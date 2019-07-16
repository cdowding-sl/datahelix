package com.scottlogic.deg.generator.fieldspecs.whitelist;

import com.scottlogic.deg.generator.utils.RandomNumberGenerator;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FrequencyWhitelist<T> implements Whitelist<T> {

    private static final FrequencyWhitelist<?> EMPTY = new FrequencyWhitelist<>(Collections.emptySet());

    private final Set<ElementFrequency<T>> underlyingSet;

    public FrequencyWhitelist(final Set<ElementFrequency<T>> underlyingSet) {
        if (underlyingSet.isEmpty()) {
            this.underlyingSet = underlyingSet;
        } else {
            if (underlyingSet.contains(null)) {
                throw new IllegalArgumentException("Whitelist should not contain null elements");
            }

            float total = underlyingSet.stream()
                .map(ElementFrequency::frequency)
                .reduce(0.0F, Float::sum);

            if (total == 0.0F) {
                throw new IllegalArgumentException("Total of frequency whitelists sum to zero.");
            }

            this.underlyingSet = underlyingSet.stream()
                .map(holder -> new ElementFrequency<>(holder.element(), holder.frequency() / total))
                .collect(Collectors.toSet());
        }
    }

    public static <T> FrequencyWhitelist<T> uniform(final Set<T> underlyingSet) {
        return new FrequencyWhitelist<>(
            underlyingSet.stream()
                .map(e -> new ElementFrequency<T>(e, 1.0F))
                .collect(Collectors.toSet()));
    }

    @SuppressWarnings("unchecked")
    public static <T> FrequencyWhitelist<T> empty() {
        return (FrequencyWhitelist<T>) EMPTY;
    }

    @Override
    public Set<T> set() {
        return underlyingSet.stream().map(ElementFrequency::element).collect(Collectors.toSet());
    }

    @Override
    public Set<ElementFrequency<T>> distributedSet() {
        return underlyingSet;
    }

    @Override
    public Stream<T> generate(RandomNumberGenerator source) {
        float random = (float) source.nextDouble(0.0D, 1.0D);

        return Stream.generate(() -> randomElement(random));
    }

    private T randomElement(float random) {
        for (ElementFrequency<T> element : underlyingSet) {
            random = random - element.frequency();
            if (random <= 0) {
                return element.element();
            }
        }
        throw new IllegalArgumentException("Iterated all values of the whitelist without picking one.");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FrequencyWhitelist<?> that = (FrequencyWhitelist<?>) o;
        return Objects.equals(underlyingSet, that.underlyingSet);
    }

    @Override
    public int hashCode() {
        return Objects.hash(underlyingSet);
    }

    @Override
    public String toString() {
        return "FrequencyWhitelist{" +
            "underlyingSet=" + underlyingSet +
            '}';
    }
}
