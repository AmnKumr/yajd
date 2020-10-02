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

public enum GPRegister8 {
    AH("ah", 4, false),
    CH("ch", 5, false),
    DH("dh", 6, false),
    BH("dh", 7, false),
    AL("ax", 0, true),
    CL("ax", 1, true),
    DL("ax", 2, true),
    BL("ax", 3, true),
    SPL("spl", 4, true),
    BPL("bpl", 5, true),
    SIL("sil", 6, true),
    DIL("dil", 7, true),
    R8B("r8b", 8, true),
    R9B("r9b", 9, true),
    R10B("r10b", 10, true),
    R11B("r11b", 11, true),
    R12B("r12b", 12, true),
    R13B("r13b", 13, true),
    R14B("r14b", 14, true),
    R15B("r15b", 15, true);

    private final static GPRegister8[] rex_registers = {
            AL, CL, DL, BL, SPL, BPL, SIL, DIL, R8B, R9B, R10B, R11B, R12B, R13B, R14B, R15B};
    private final static GPRegister8[] nonrex_registers = {AL, CL, DL, BL, AH, CH, DH, BH};
    private final String name;
    private final int index;
    private final boolean rex_compatible;

    GPRegister8(String name, int index, boolean rex_compatible) {
        this.name = name;
        this.index = index;
        this.rex_compatible = rex_compatible;
    }

    @Contract(pure = true)
    public static GPRegister8 of(int index) {
        return rex_registers[index];
    }

    @Contract(pure = true)
    public static GPRegister8 of(@NotNull GPRegister16 r) {
        return rex_registers[r.getIndex()];
    }

    @Contract(pure = true)
    public static GPRegister8 of(@NotNull GPRegister32 r) {
        return rex_registers[r.getIndex()];
    }

    @Contract(pure = true)
    public static GPRegister8 of(@NotNull GPRegister64 r) {
        return rex_registers[r.getIndex()];
    }

    @Contract(pure = true)
    public static GPRegister8 of(int index, boolean rex) {
        if (rex) {
            return rex_registers[index];
        } else {
            return nonrex_registers[index];
        }
    }

    public String getName() {
        return name;
    }

    public int getIndex() {
        return index;
    }

    public boolean getRexCompatible() {
        return rex_compatible;
    }

    @Contract(value = " -> new", pure = true)
    public @NotNull Argument toArgument() {
        return new Argument();
    }

    final public class Argument implements org.yajd.x86.cpu.Argument {
        @Override
        public <Type> Type process(@NotNull Result<Type> result) {
            return result.when(GPRegister8.this);
        }
    }
}
