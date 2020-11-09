package org.yajd.x86.cpu;

import org.jetbrains.annotations.NotNull;

public class Rel8 implements Argument {
    private final byte value;

    public Rel8(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return value;
    }

    @Override
    public <Type> Type process(@NotNull Result<Type> result) {
        return result.when(this);
    }
}
