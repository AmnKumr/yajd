package org.yajd.x86.cpu;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class Imm32 implements Argument {
    private final int value;

    public Imm32(int value) {
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
