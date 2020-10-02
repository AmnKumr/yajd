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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ArgumentTest {
    @Test
    @DisplayName("Test visitor with default implementation")
    void testArgumentDefaultImplementation() {
        var arguments = new Argument[]{
                GPRegister8.AL.toArgument(),
                GPRegister32.EAX.toArgument()};
        int arguments_count = 0;
        for (var argument : arguments) {
            arguments_count += argument.process(new Argument.Result<Integer>() {
                @Override
                public Integer when(@NotNull Argument argument) {
                    return 1;
                }
            });
        }
        assertEquals(2, arguments_count);
    }

    @Test
    @DisplayName("Test visitor with specific implementation")
    void testArgumentSpecificImplementation() {
        var arguments = new Argument[]{
                GPRegister8.AL.toArgument(),
                GPRegister32.EAX.toArgument()};
        List<GPRegister8> arguments_8bit = new ArrayList<>();
        for (var argument : arguments) {
            arguments_8bit.add(argument.process(new Argument.Result<GPRegister8>() {
                @Override
                public GPRegister8 when(@NotNull GPRegister8 argument) {
                    return argument;
                }
            }));
        }
        assertArrayEquals(new GPRegister8[]{GPRegister8.AL, null}, arguments_8bit.toArray());
    }
}
