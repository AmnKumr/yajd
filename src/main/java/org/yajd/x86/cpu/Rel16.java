package org.yajd.x86.cpu;

import org.jetbrains.annotations.NotNull;

public class Rel16 implements Argument {
    private final short value;

    public Rel16(short value) {
        this.value = value;
    }

    public short getValue() {
        return value;
    }

    @Override
    public <Type> Type process(@NotNull Result<Type> result) {
        return result.when(this);
    }
}
