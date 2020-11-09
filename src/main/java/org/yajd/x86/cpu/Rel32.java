package org.yajd.x86.cpu;

import org.jetbrains.annotations.NotNull;

public class Rel32 implements Argument {
    private final int value;

    public Rel32(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public <Type> Type process(@NotNull Result<Type> result) {
        return result.when(this);
    }
}
