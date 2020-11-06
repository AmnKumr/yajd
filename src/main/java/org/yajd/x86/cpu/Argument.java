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

public interface Argument {
    <Type> Type process(@NotNull Result<Type> result);

    interface Result<Type> {
        default Type when(@NotNull Argument argument) {
            return null;
        }

        default Type when(@NotNull GPRegister8 argument) {
            return when(argument.toArgument());
        }

        default Type when(@NotNull GPRegister16 argument) {
            return when(argument.toArgument());
        }

        default Type when(@NotNull GPRegister32 argument) {
            return when(argument.toArgument());
        }

        default Type when(@NotNull GPRegister64 argument) {
            return when(argument.toArgument());
        }

        default Type when(@NotNull SegmentRegister argument) {
            return when(argument.toArgument());
        }

        default Type when(@NotNull Imm8 argumet) {
            return when((Argument)argumet);
        }

        default Type when(@NotNull Imm16 argumet) {
            return when((Argument)argumet);
        }

        default Type when(@NotNull Imm32 argumet) {
            return when((Argument)argumet);
        }

        default Type when(@NotNull Imm64 argumet) {
            return when((Argument)argumet);
        }

        default Type when(@NotNull GPAddress16 argument) {
            return when(argument.toArgument());
        }

        default Type when(@NotNull GPAddress32 argument) {
            return when(argument.toArgument());
        }

        default Type when(@NotNull EIPAddress32 argument) {
            return when(argument.toArgument());
        }

        default Type when(@NotNull GPAddress64 argument) {
            return when(argument.toArgument());
        }

        default Type when(@NotNull RIPAddress64 argument) {
            return when(argument.toArgument());
        }
    }
}
