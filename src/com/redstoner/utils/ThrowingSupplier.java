package com.redstoner.utils;

/**
 * A supplier with a throws declaration.
 * Once again, I have more solid alternatives, but if you want it in your utils... be my guest :D
 *
 * @param <T> The type of object computed by this supplier.
 */
@FunctionalInterface
public interface ThrowingSupplier<T> {
    T get() throws Throwable;
}
