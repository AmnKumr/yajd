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

import java.util.Optional;

// Note: only one x86-64 instruction “mov to/from accumulator with absolute address” may use 64-bit value as an actual
// address. And it's usually best idea to handle it separately because it's very limited.
//
// In 16-bit mode and 32-bit mode it's Ok to just consider than instruction as “short form” of regular mov, but that
// doesn't work for 64-bit.
public class AbsoluteAddress64 {
    private final SegmentRegister segment;
    private final long disp;
    private final short size; // Memory operand size, 0 if not specified (e.g. lea).

    @Contract(pure = true)
    AbsoluteAddress64(SegmentRegister segment, long disp) {
        this.segment = segment;
        this.disp = disp;
        this.size = 0;
    }

    @Contract(pure = true)
    AbsoluteAddress64(@NotNull AbsoluteAddress64 addr, short size) {
        this.segment = addr.segment;
        this.disp = addr.disp;
        this.size = size;
    }

    public Optional<SegmentRegister> getSegment() {
        return Optional.ofNullable(segment);
    }

    public long getDisp() {
        return disp;
    }

    public short getSize() {
        return size;
    }

    @Contract(value = " -> new", pure = true)
    public @NotNull Argument toArgument() {
        return new Argument();
    }

    final public class Argument implements org.yajd.x86.cpu.Argument {
        @Override
        public <Type> Type process(@NotNull Result<Type> result) {
            return result.when(AbsoluteAddress64.this);
        }
    }
}
