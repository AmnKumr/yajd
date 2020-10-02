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

public enum GPRegister64 {
    RAX("eax", 0),
    RCX("ecx", 1),
    RDX("edx", 2),
    RBX("ebx", 3),
    RSP("esp", 4),
    RBP("ebp", 5),
    RSI("esi", 6),
    RDI("edi", 7),
    R8("r8d", 8),
    R9("r9d", 9),
    R10("r10d", 10),
    R11("r11d", 11),
    R12("r12d", 12),
    R13("r13d", 13),
    R14("r14d", 14),
    R15("r15d", 15);

    private final static GPRegister64[] registers = {
            RAX, RCX, RDX, RBX, RSP, RBP, RSI, RDI, R8, R9, R10, R11, R12, R13, R14, R15};
    private final String name;
    private final int index;

    GPRegister64(String name, int index) {
        this.name = name;
        this.index = index;
    }

    @Contract(pure = true)
    public static GPRegister64 of(int index) {
        return registers[index];
    }

    @Contract(pure = true)
    public static GPRegister64 of(@NotNull GPRegister8 r) {
        assert r.getRexCompatible();
        return registers[r.getIndex()];
    }

    @Contract(pure = true)
    public static GPRegister64 of(@NotNull GPRegister16 r) {
        return registers[r.getIndex()];
    }

    @Contract(pure = true)
    public static GPRegister64 of(@NotNull GPRegister32 r) {
        return registers[r.getIndex()];
    }

    public String getName() {
        return name;
    }

    public int getIndex() {
        return index;
    }

    @Contract(value = " -> new", pure = true)
    public @NotNull Argument toArgument() {
        return new Argument();
    }

    final public class Argument implements org.yajd.x86.cpu.Argument {
        @Override
        public <Type> Type process(@NotNull Result<Type> result) {
            return result.when(GPRegister64.this);
        }
    }
}
