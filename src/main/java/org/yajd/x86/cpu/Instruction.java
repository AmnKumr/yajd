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

public interface Instruction {
    @Contract(pure = true)
    @NotNull String getName();

    @Contract(pure = true)
    @NotNull Argument[] getArguments();

    <Type> Type process(@NotNull Result<Type> result);

    interface Result<Type> {
        default Type when(Instruction argument) {
            return null;
        }

        default Type when(MovReg8Reg8 argument) {
            return when((Instruction) argument);
        }

        default Type when(MovReg16Reg16 argument) {
            return when((Instruction) argument);
        }

        default Type when(MovReg32Reg32 argument) {
            return when((Instruction) argument);
        }

        default Type when(MovReg64Reg64 argument) {
            return when((Instruction) argument);
        }
    }

    final class MovReg8Reg8 implements Instruction {
        final private GPRegister8 arg0;
        final private GPRegister8 arg1;

        public MovReg8Reg8(GPRegister8 arg0, GPRegister8 arg1) {
            this.arg0 = arg0;
            this.arg1 = arg1;
        }

        @Override
        public <Type> Type process(@NotNull Result<Type> result) {
            return result.when(this);
        }

        @Contract(pure = true)
        public @NotNull String getName() {
            return "mov";
        }

        public Argument[] getArguments() {
            return new Argument[]{arg0.toArgument(), arg1.toArgument()};
        }

        public GPRegister8 getArg0() {
            return arg0;
        }

        public GPRegister8 getArg1() {
            return arg1;
        }
    }

    final class MovReg16Reg16 implements Instruction {
        final private GPRegister16 arg0;
        final private GPRegister16 arg1;

        public MovReg16Reg16(GPRegister16 arg0, GPRegister16 arg1) {
            this.arg0 = arg0;
            this.arg1 = arg1;
        }

        @Override
        public <Type> Type process(@NotNull Result<Type> result) {
            return result.when(this);
        }

        @Contract(pure = true)
        public @NotNull String getName() {
            return "mov";
        }

        public Argument[] getArguments() {
            return new Argument[]{arg0.toArgument(), arg1.toArgument()};
        }

        public GPRegister16 getArg0() {
            return arg0;
        }

        public GPRegister16 getArg1() {
            return arg1;
        }
    }

    final class MovReg32Reg32 implements Instruction {
        final private GPRegister32 arg0;
        final private GPRegister32 arg1;

        public MovReg32Reg32(GPRegister32 arg0, GPRegister32 arg1) {
            this.arg0 = arg0;
            this.arg1 = arg1;
        }

        @Override
        public <Type> Type process(@NotNull Result<Type> result) {
            return result.when(this);
        }

        @Contract(pure = true)
        public @NotNull String getName() {
            return "mov";
        }

        public Argument[] getArguments() {
            return new Argument[]{arg0.toArgument(), arg1.toArgument()};
        }

        public GPRegister32 getArg0() {
            return arg0;
        }

        public GPRegister32 getArg1() {
            return arg1;
        }
    }

    final class MovReg64Reg64 implements Instruction {
        final private GPRegister64 arg0;
        final private GPRegister64 arg1;

        public MovReg64Reg64(GPRegister64 arg0, GPRegister64 arg1) {
            this.arg0 = arg0;
            this.arg1 = arg1;
        }

        @Override
        public <Type> Type process(@NotNull Result<Type> result) {
            return result.when(this);
        }

        @Contract(pure = true)
        public @NotNull String getName() {
            return "mov";
        }

        public Argument[] getArguments() {
            return new Argument[]{arg0.toArgument(), arg1.toArgument()};
        }

        public GPRegister64 getArg0() {
            return arg0;
        }

        public GPRegister64 getArg1() {
            return arg1;
        }
    }
}
