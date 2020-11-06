package org.yajd.x86.cpu;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class Imm64 implements Argument {
    private final long value;

    public Imm64(long value) {
        this.value = value;
    }

    public long getValue() {
        return value;
    }

    @Override
    public <Type> Type process(@NotNull Result<Type> result) {
        return result.when(this);
    }
}
