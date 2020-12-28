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

import org.jetbrains.annotations.NotNull;
import org.yajd.RollbackIterator;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class InstructionIterator implements Iterator<Instruction> {
    RollbackIterator<Byte> iterator;
    Instruction.Mode mode;
    Instruction instruction = null;

    public InstructionIterator(Instruction.Mode mode, @NotNull RollbackIterator<Byte> iterator) {
        this.mode = mode;
        this.iterator = iterator;
    }

    public InstructionIterator(Instruction.Mode mode, @NotNull Iterator<Byte> iterator) {
        this(mode, new RollbackIterator<Byte>(iterator));
    }

    @Override
    public boolean hasNext() {
        if (instruction != null) {
            return true;
        }
        var parsed_instruction = Instruction.parse(mode, iterator);
        if (parsed_instruction.isEmpty()) {
            if (!iterator.hasNext()) {
                return false;
            } else {
                instruction = new Instruction.BadInstruction(new byte[]{iterator.next()});
            }
        } else {
            instruction = parsed_instruction.get();
        }
        return true;
    }

    @Override
    public Instruction next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        var saved_instruction = instruction;
        instruction = null;
        return saved_instruction;
    }
}
