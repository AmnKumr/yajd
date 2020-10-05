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

public enum GPRegister32 {
    EAX("eax", 0),
    ECX("ecx", 1),
    EDX("edx", 2),
    EBX("ebx", 3),
    ESP("esp", 4),
    EBP("ebp", 5),
    ESI("esi", 6),
    EDI("edi", 7),
    R8D("r8d", 8),
    R9D("r9d", 9),
    R10D("r10d", 10),
    R11D("r11d", 11),
    R12D("r12d", 12),
    R13D("r13d", 13),
    R14D("r14d", 14),
    R15D("r15d", 15),
    EIZ("eiz", -1); // EIZ can be used in 32-bit address.

    private final static GPRegister32[] registers = {
            EAX, ECX, EDX, EBX, ESP, EBP, ESI, EDI, R8D, R9D, R10D, R11D, R12D, R13D, R14D, R15D};
    private final String name;
    private final int index;

    GPRegister32(String name, int index) {
        this.name = name;
        this.index = index;
    }

    @Contract(pure = true)
    public static GPRegister32 of(int index) {
        return registers[index];
    }

    @Contract(pure = true)
    public static GPRegister32 of(@NotNull GPRegister8 r) {
        assert r.getRexCompatible();
        return registers[r.getIndex()];
    }

    @Contract(pure = true)
    public static GPRegister32 of(@NotNull GPRegister16 r) {
        return registers[r.getIndex()];
    }

    @Contract(pure = true)
    public static GPRegister32 of(@NotNull GPRegister64 r) {
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
            return result.when(GPRegister32.this);
        }
    }
}
