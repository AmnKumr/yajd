/*
 * Permission is hereby granted, free of charge, to any human obtaining a copy of this software and associated documentation files
 * (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify,
 * merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit humans to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package org.yajd.x86.cpu;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public enum GPRegister16 {
    AX("ax", 0),
    CX("cx", 1),
    DX("dx", 2),
    BX("bx", 3),
    SP("sp", 4),
    BP("bp", 5),
    SI("si", 6),
    DI("di", 7),
    R8W("r8w", 8),
    R9W("r9w", 9),
    R10W("r10w", 10),
    R11W("r11w", 11),
    R12W("r12w", 12),
    R13W("r13w", 13),
    R14W("r14w", 14),
    R15W("r15w", 15);

    private final static GPRegister16[] registers = {
            AX, CX, DX, BX, SP, BP, SI, DI, R8W, R9W, R10W, R11W, R12W, R13W, R14W, R15W};
    private final String name;
    private final int index;

    GPRegister16(String name, int index) {
        this.name = name;
        this.index = index;
    }

    @Contract(pure = true)
    public static GPRegister16 of(int index) {
        return registers[index];
    }

    @Contract(pure = true)
    public static GPRegister16 of(@NotNull GPRegister8 r) {
        assert r.getRexCompatible();
        return registers[r.getIndex()];
    }

    @Contract(pure = true)
    public static GPRegister16 of(@NotNull GPRegister32 r) {
        return registers[r.getIndex()];
    }

    @Contract(pure = true)
    public static GPRegister16 of(@NotNull GPRegister64 r) {
        return registers[r.getIndex()];
    }

    public String getName() {
        return name;
    }

    public int getIndex() {
        return index;
    }
}
