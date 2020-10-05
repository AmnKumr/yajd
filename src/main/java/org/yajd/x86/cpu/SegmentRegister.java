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

public enum SegmentRegister {
    ES("es", 0),
    CS("cs", 1),
    SS("ss", 2),
    DS("ds", 3),
    FS("fs", 4),
    GS("gs", 5),
    SEG6("?", 6), // There are only 6 segment registers in x86 architecture.
    SEG7("?", 7); // But certain operation allow 8. Add 2 "extra" ones to handle that.

    private final static SegmentRegister[] registers = {ES, CS, SS, DS, FS, GS, SEG6, SEG7};
    private final String name;
    private final int index;

    SegmentRegister(String name, int index) {
        this.name = name;
        this.index = index;
    }

    @Contract(pure = true)
    public static SegmentRegister of(int index) {
        return registers[index];
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
            return result.when(SegmentRegister.this);
        }
    }
}
