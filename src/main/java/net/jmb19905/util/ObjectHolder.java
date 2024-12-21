package net.jmb19905.util;

import java.util.function.Consumer;
import java.util.function.Function;

public class ObjectHolder<T> {
    private T value;
    private boolean locked;

    public ObjectHolder(T def) {
        this.value = def;
        this.locked = false;
    }

    public T getValue() {
        return value;
    }

    public void updateValue(Function<T, T> mutator) {
        this.setValue(mutator.apply(this.value));
    }

    public void changeValue(Consumer<T> consumer) {
        consumer.accept(value);
    }

    public ObjectHolder<T> setValue(T newValue) {
        if (!locked)
            this.value = newValue;
        return this;
    }
    public boolean isLocked() {
        return this.locked;
    }
    public ObjectHolder<T> lock() {
        this.locked = true;
        return this;
    }
}
