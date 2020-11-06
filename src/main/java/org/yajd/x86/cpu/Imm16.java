package org.yajd.x86.cpu;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public class Imm16 implements Argument {
    private final short value;

    public Imm16(short value) {
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
