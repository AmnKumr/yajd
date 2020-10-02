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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class InstructionTest {
    static Stream<Arguments> TwoArgumentInstructions() {
        return Stream.of(
                arguments(new Instruction.MovReg8Reg8(GPRegister8.AL, GPRegister8.CL),
                        GPRegister8.AL, GPRegister8.CL),
                arguments(new Instruction.MovReg16Reg16(GPRegister16.AX, GPRegister16.CX),
                        GPRegister16.AX, GPRegister16.CX),
                arguments(new Instruction.MovReg32Reg32(GPRegister32.EAX, GPRegister32.ECX),
                        GPRegister32.EAX, GPRegister32.ECX),
                arguments(new Instruction.MovReg64Reg64(GPRegister64.RAX, GPRegister64.RCX),
                        GPRegister64.RAX, GPRegister64.RCX));
    }

    @ParameterizedTest
    @MethodSource("TwoArgumentInstructions")
    @DisplayName("Two argument instructions test")
    void testTwoArgumentInstructions(Instruction instruction, Object argument0, Object argument1)
            throws Exception {
        var arg0 = instruction.getClass().getMethod("getArg0").invoke(instruction);
        var arg1 = instruction.getClass().getMethod("getArg1").invoke(instruction);
        assertEquals(arg0, argument0);
        assertEquals(arg1, argument1);
    }

    @Test
    @DisplayName("MovReg8Reg8 test")
    void testMovReg8Reg8() {
        Instruction instruction = new Instruction.MovReg8Reg8(GPRegister8.AL, GPRegister8.CL);
        var arguments = instruction.getArguments();
        assertEquals(GPRegister8.AL, arguments[0].process(new Argument.Result<GPRegister8>() {
            @Override
            public GPRegister8 when(@NotNull GPRegister8 argument) {
                return argument;
            }
        }));
        assertEquals(GPRegister8.CL, arguments[1].process(new Argument.Result<GPRegister8>() {
            @Override
            public GPRegister8 when(@NotNull GPRegister8 argument) {
                return argument;
            }
        }));
    }

    @Test
    @DisplayName("MovReg16Reg16 test")
    void testMovReg16Reg16() {
        Instruction instruction = new Instruction.MovReg16Reg16(GPRegister16.AX, GPRegister16.CX);
        var arguments = instruction.getArguments();
        assertEquals(GPRegister16.AX, arguments[0].process(new Argument.Result<GPRegister16>() {
            @Override
            public GPRegister16 when(@NotNull GPRegister16 argument) {
                return argument;
            }
        }));
        assertEquals(GPRegister16.CX, arguments[1].process(new Argument.Result<GPRegister16>() {
            @Override
            public GPRegister16 when(@NotNull GPRegister16 argument) {
                return argument;
            }
        }));
    }

    @Test
    @DisplayName("MovReg32Reg32 test")
    void testMovReg32Reg32() {
        Instruction instruction = new Instruction.MovReg32Reg32(GPRegister32.EAX, GPRegister32.ECX);
        var arguments = instruction.getArguments();
        assertEquals(GPRegister32.EAX, arguments[0].process(new Argument.Result<GPRegister32>() {
            @Override
            public GPRegister32 when(@NotNull GPRegister32 argument) {
                return argument;
            }
        }));
        assertEquals(GPRegister32.ECX, arguments[1].process(new Argument.Result<GPRegister32>() {
            @Override
            public GPRegister32 when(@NotNull GPRegister32 argument) {
                return argument;
            }
        }));
    }

    @Test
    @DisplayName("MovReg64Reg64 test")
    void testMovReg64Reg64() {
        Instruction instruction = new Instruction.MovReg64Reg64(GPRegister64.RAX, GPRegister64.RCX);
        var arguments = instruction.getArguments();
        assertEquals(GPRegister64.RAX, arguments[0].process(new Argument.Result<GPRegister64>() {
            @Override
            public GPRegister64 when(@NotNull GPRegister64 argument) {
                return argument;
            }
        }));
        assertEquals(GPRegister64.RCX, arguments[1].process(new Argument.Result<GPRegister64>() {
            @Override
            public GPRegister64 when(@NotNull GPRegister64 argument) {
                return argument;
            }
        }));
    }
}
