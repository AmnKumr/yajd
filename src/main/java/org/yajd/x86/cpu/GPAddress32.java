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

public class GPAddress32 {
    private final Optional<SegmentRegister> segment;
    private final Optional<GPRegister32> base;
    private final Optional<GPRegister32> index;
    // Note: scale may be always present even if there are no index.
    // It does not chage semantic, but it affects encoding.
    private final ScaleFactor scale;
    private final int disp;
    private final short size; // Memory operand size, 0 if not specified (e.g. lea).

    @Contract(pure = true)
    GPAddress32(Optional<SegmentRegister> segment, Optional<GPRegister32> base,
                Optional<GPRegister32> index, ScaleFactor scale, int disp) {
        this.segment = segment;
        this.base = base;
        this.index = index;
        this.scale = scale;
        this.disp = disp;
        this.size = 0;
    }

    @Contract(pure = true)
    GPAddress32(@NotNull GPAddress32 addr, short size) {
        this.segment = addr.segment;
        this.base = addr.base;
        this.index = addr.index;
        this.scale = addr.scale;
        this.disp = addr.disp;
        this.size = size;
    }

    public Optional<SegmentRegister> getSegment() {
        return segment;
    }

    public Optional<GPRegister32> getBase() {
        return base;
    }

    public Optional<GPRegister32> getIndex() {
        return index;
    }

    public ScaleFactor getScale() {
        return scale;
    }

    public int getDisp() {
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
            return result.when(GPAddress32.this);
        }
    }
}
