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

import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class InstructionTest {
    static Stream<Arguments> TwoArgumentRegisterInstructions() {
        return Stream.of(arguments(new Instruction.AddReg8Reg8(GPRegister8.AL, GPRegister8.CL),
                GPRegister8.AL, GPRegister8.CL),
                arguments(new Instruction.AddReg16Reg16(GPRegister16.AX, GPRegister16.CX), GPRegister16.AX,
                        GPRegister16.CX),
                arguments(new Instruction.AddReg32Reg32(GPRegister32.EAX, GPRegister32.ECX),
                        GPRegister32.EAX, GPRegister32.ECX),
                arguments(new Instruction.AddReg64Reg64(GPRegister64.RAX, GPRegister64.RCX),
                        GPRegister64.RAX, GPRegister64.RCX),
                arguments(new Instruction.MovReg8Reg8(GPRegister8.AL, GPRegister8.CL), GPRegister8.AL,
                        GPRegister8.CL),
                arguments(new Instruction.MovReg16Reg16(GPRegister16.AX, GPRegister16.CX), GPRegister16.AX,
                        GPRegister16.CX),
                arguments(new Instruction.MovReg32Reg32(GPRegister32.EAX, GPRegister32.ECX),
                        GPRegister32.EAX, GPRegister32.ECX),
                arguments(new Instruction.MovReg64Reg64(GPRegister64.RAX, GPRegister64.RCX),
                        GPRegister64.RAX, GPRegister64.RCX),
                arguments(new Instruction.SubReg8Reg8(GPRegister8.AL, GPRegister8.CL), GPRegister8.AL,
                        GPRegister8.CL),
                arguments(new Instruction.SubReg16Reg16(GPRegister16.AX, GPRegister16.CX), GPRegister16.AX,
                        GPRegister16.CX),
                arguments(new Instruction.SubReg32Reg32(GPRegister32.EAX, GPRegister32.ECX),
                        GPRegister32.EAX, GPRegister32.ECX),
                arguments(new Instruction.SubReg64Reg64(GPRegister64.RAX, GPRegister64.RCX),
                        GPRegister64.RAX, GPRegister64.RCX));
    }

    static Stream<Arguments> TwoArgumentAddressInstructions() {
        return Stream.of(
                arguments(new Instruction.AddReg8Addr16Mem8(GPRegister8.AL,
                                new GPAddress16(Optional.of(SegmentRegister.ES), Optional.of(GPRegister16.BX),
                                        Optional.of(GPRegister16.DI), (short) 1)),
                        GPRegister8.AL, SegmentRegister.ES, GPRegister16.BX, GPRegister16.DI, null, (short) 1,
                        (short) 8),
                arguments(
                        new Instruction.AddReg16Addr32Mem16(GPRegister16.BX,
                                new GPAddress32(Optional.of(SegmentRegister.CS), Optional.of(GPRegister32.EAX),
                                        Optional.of(GPRegister32.ECX), ScaleFactor.X1, 2)),
                        GPRegister16.BX, SegmentRegister.CS, GPRegister32.EAX, GPRegister32.ECX, ScaleFactor.X1,
                        2, (short) 16),
                arguments(
                        new Instruction.AddReg32Addr64Mem32(GPRegister32.ECX,
                                new GPAddress64(Optional.of(SegmentRegister.SS), Optional.of(GPRegister64.RDX),
                                        Optional.of(GPRegister64.RSI), ScaleFactor.X2, 3)),
                        GPRegister32.ECX, SegmentRegister.SS, GPRegister64.RDX, GPRegister64.RSI, ScaleFactor.X2,
                        3, (short) 32),
                arguments(new Instruction.AddReg64EIPAddr32Mem64(GPRegister64.RDX, new EIPAddress32(4)),
                        GPRegister64.RDX, null, null, null, null, 4, (short) 64),
                arguments(new Instruction.AddReg8RIPAddr64Mem8(GPRegister8.AH, new RIPAddress64(5)),
                        GPRegister8.AH, null, null, null, null, 5, (short) 8));
    }

    @ParameterizedTest
    @MethodSource("TwoArgumentRegisterInstructions")
    @DisplayName("Two argument register instructions test")
    void testTwoArgumentRegisterInstructions(
            Instruction instruction, Object argument0, Object argument1) throws Exception {
        var arg0 = instruction.getClass().getMethod("getArg0").invoke(instruction);
        var arg1 = instruction.getClass().getMethod("getArg1").invoke(instruction);
        assertEquals(argument0, arg0);
        assertEquals(argument1, arg1);
    }

    @ParameterizedTest
    @MethodSource("TwoArgumentAddressInstructions")
    @DisplayName("Two argument address instructions test")
    void testTwoArgumentAddressInstructions(Instruction instruction, Object argument0,
                                            SegmentRegister segment, Object base, Object index, Object scale, Object disp, short size)
            throws Exception {
        var arg0 = instruction.getClass().getMethod("getArg0").invoke(instruction);
        var arg1 = instruction.getClass().getMethod("getArg1").invoke(instruction);
        assertEquals(argument0, arg0);
        if (segment != null) {
            assertEquals(Optional.of(segment), arg1.getClass().getMethod("getSegment").invoke(arg1));
        }
        if (base != null) {
            assertEquals(Optional.of(base), arg1.getClass().getMethod("getBase").invoke(arg1));
        }
        if (index != null) {
            assertEquals(Optional.of(index), arg1.getClass().getMethod("getIndex").invoke(arg1));
        }
        if (scale != null) {
            assertEquals(scale, arg1.getClass().getMethod("getScale").invoke(arg1));
        }
        assertEquals(disp, arg1.getClass().getMethod("getDisp").invoke(arg1));
        assertEquals(size, arg1.getClass().getMethod("getSize").invoke(arg1));
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
