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
import org.yajd.RollbackIterator;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class InstructionTest {
    static @NotNull Stream<Arguments> decIncEncodings() {
        return Stream.of(
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x48}, "DecReg16", GPRegister16.AX),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x49}, "DecReg16", GPRegister16.CX),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x4a}, "DecReg16", GPRegister16.DX),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x4b}, "DecReg16", GPRegister16.BX),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x4c}, "DecReg16", GPRegister16.SP),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x4d}, "DecReg16", GPRegister16.BP),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x4e}, "DecReg16", GPRegister16.SI),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x4f}, "DecReg16", GPRegister16.DI),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x66, 0x48}, "DecReg32", GPRegister32.EAX),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{0x48}, "DecReg32", GPRegister32.EAX),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{0x66, 0x48}, "DecReg16", GPRegister16.AX),
                arguments(Instruction.Mode.ADDR32_DATA16, new Byte[]{0x48}, "DecReg16", GPRegister16.AX),
                arguments(Instruction.Mode.ADDR32_DATA16, new Byte[]{0x66, 0x48}, "DecReg32", GPRegister32.EAX),
                arguments(Instruction.Mode.ADDR32_DATA32, new Byte[]{0x48}, "DecReg32", GPRegister32.EAX),
                arguments(Instruction.Mode.ADDR32_DATA32, new Byte[]{0x66, 0x48}, "DecReg16", GPRegister16.AX),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x40}, "IncReg16", GPRegister16.AX),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x41}, "IncReg16", GPRegister16.CX),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x42}, "IncReg16", GPRegister16.DX),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x43}, "IncReg16", GPRegister16.BX),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x44}, "IncReg16", GPRegister16.SP),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x45}, "IncReg16", GPRegister16.BP),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x46}, "IncReg16", GPRegister16.SI),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x47}, "IncReg16", GPRegister16.DI),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x66, 0x40}, "IncReg32", GPRegister32.EAX),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{0x40}, "IncReg32", GPRegister32.EAX),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{0x66, 0x40}, "IncReg16", GPRegister16.AX),
                arguments(Instruction.Mode.ADDR32_DATA16, new Byte[]{0x40}, "IncReg16", GPRegister16.AX),
                arguments(Instruction.Mode.ADDR32_DATA16, new Byte[]{0x66, 0x40}, "IncReg32", GPRegister32.EAX),
                arguments(Instruction.Mode.ADDR32_DATA32, new Byte[]{0x40}, "IncReg32", GPRegister32.EAX),
                arguments(Instruction.Mode.ADDR32_DATA32, new Byte[]{0x66, 0x40}, "IncReg16", GPRegister16.AX));
    }

    static @NotNull Stream<Arguments> nopEncodings() {
        return Stream.of(
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte)0x90}, "Nop"),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{(byte)0x90}, "Nop"),
                arguments(Instruction.Mode.ADDR32_DATA16, new Byte[]{(byte)0x90}, "Nop"),
                arguments(Instruction.Mode.ADDR32_DATA32, new Byte[]{(byte)0x90}, "Nop"),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{(byte)0x90}, "Nop"),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x40, (byte)0x90}, "Nop"),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x42, (byte)0x90}, "Nop"),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x44, (byte)0x90}, "Nop"),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x46, (byte)0x90}, "Nop"));
    }

    static @NotNull Stream<Arguments> twoArgumentRegisterInstructions() {
        return Stream.of(arguments(new Instruction.AddReg8Reg8(GPRegister8.AL, GPRegister8.CL, null),
                GPRegister8.AL, GPRegister8.CL),
                arguments(new Instruction.AddReg16Reg16(GPRegister16.AX, GPRegister16.CX, null),
                        GPRegister16.AX, GPRegister16.CX),
                arguments(new Instruction.AddReg32Reg32(GPRegister32.EAX, GPRegister32.ECX, null),
                        GPRegister32.EAX, GPRegister32.ECX),
                arguments(new Instruction.AddReg64Reg64(GPRegister64.RAX, GPRegister64.RCX, null),
                        GPRegister64.RAX, GPRegister64.RCX),
                arguments(new Instruction.MovReg8Reg8(GPRegister8.AL, GPRegister8.CL, null),
                        GPRegister8.AL, GPRegister8.CL),
                arguments(new Instruction.MovReg16Reg16(GPRegister16.AX, GPRegister16.CX, null),
                        GPRegister16.AX, GPRegister16.CX),
                arguments(new Instruction.MovReg32Reg32(GPRegister32.EAX, GPRegister32.ECX, null),
                        GPRegister32.EAX, GPRegister32.ECX),
                arguments(new Instruction.MovReg64Reg64(GPRegister64.RAX, GPRegister64.RCX, null),
                        GPRegister64.RAX, GPRegister64.RCX),
                arguments(new Instruction.SubReg8Reg8(GPRegister8.AL, GPRegister8.CL, null),
                        GPRegister8.AL, GPRegister8.CL),
                arguments(new Instruction.SubReg16Reg16(GPRegister16.AX, GPRegister16.CX, null),
                        GPRegister16.AX, GPRegister16.CX),
                arguments(new Instruction.SubReg32Reg32(GPRegister32.EAX, GPRegister32.ECX, null),
                        GPRegister32.EAX, GPRegister32.ECX),
                arguments(new Instruction.SubReg64Reg64(GPRegister64.RAX, GPRegister64.RCX, null),
                        GPRegister64.RAX, GPRegister64.RCX));
    }

    static @NotNull Stream<Arguments> twoArgumentAddressInstructions() {
        return Stream.of(
                arguments(new Instruction.AddReg8Addr16Mem8(GPRegister8.AL,
                                new GPAddress16(Optional.of(SegmentRegister.ES), Optional.of(GPRegister16.BX),
                                        Optional.of(GPRegister16.DI), (short) 1),
                                null),
                        GPRegister8.AL, SegmentRegister.ES, GPRegister16.BX, GPRegister16.DI, null, (short) 1,
                        (short) 8),
                arguments(
                        new Instruction.AddReg16Addr32Mem16(GPRegister16.BX,
                                new GPAddress32(Optional.of(SegmentRegister.CS), Optional.of(GPRegister32.EAX),
                                        Optional.of(GPRegister32.ECX), ScaleFactor.X1, 2),
                                null),
                        GPRegister16.BX, SegmentRegister.CS, GPRegister32.EAX, GPRegister32.ECX, ScaleFactor.X1,
                        2, (short) 16),
                arguments(
                        new Instruction.AddReg32Addr64Mem32(GPRegister32.ECX,
                                new GPAddress64(Optional.of(SegmentRegister.SS), Optional.of(GPRegister64.RDX),
                                        Optional.of(GPRegister64.RSI), ScaleFactor.X2, 3),
                                null),
                        GPRegister32.ECX, SegmentRegister.SS, GPRegister64.RDX, GPRegister64.RSI,
                        ScaleFactor.X2, 3, (short) 32),
                arguments(
                        new Instruction.AddReg64EIPAddr32Mem64(GPRegister64.RDX, new EIPAddress32(4), null),
                        GPRegister64.RDX, null, null, null, null, 4, (short) 64),
                arguments(new Instruction.AddReg8RIPAddr64Mem8(GPRegister8.AH, new RIPAddress64(5), null),
                        GPRegister8.AH, null, null, null, null, 5, (short) 8));
    }

    private static Stream<Arguments> xchgAccumulatorRegEncodings() {
        return Stream.of(
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte) 0x91}, "XchgReg16Reg16", GPRegister16.AX, GPRegister16.CX),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte) 0x92}, "XchgReg16Reg16", GPRegister16.AX, GPRegister16.DX),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte) 0x93}, "XchgReg16Reg16", GPRegister16.AX, GPRegister16.BX),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte) 0x94}, "XchgReg16Reg16", GPRegister16.AX, GPRegister16.SP),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte) 0x95}, "XchgReg16Reg16", GPRegister16.AX, GPRegister16.BP),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte) 0x96}, "XchgReg16Reg16", GPRegister16.AX, GPRegister16.SI),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte) 0x97}, "XchgReg16Reg16", GPRegister16.AX, GPRegister16.DI),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x66, (byte) 0x90}, "XchgReg32Reg32", GPRegister32.EAX, GPRegister32.EAX),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x66, (byte) 0x91}, "XchgReg32Reg32", GPRegister32.EAX, GPRegister32.ECX),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x66, (byte) 0x92}, "XchgReg32Reg32", GPRegister32.EAX, GPRegister32.EDX),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x66, (byte) 0x93}, "XchgReg32Reg32", GPRegister32.EAX, GPRegister32.EBX),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x66, (byte) 0x94}, "XchgReg32Reg32", GPRegister32.EAX, GPRegister32.ESP),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x66, (byte) 0x95}, "XchgReg32Reg32", GPRegister32.EAX, GPRegister32.EBP),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x66, (byte) 0x96}, "XchgReg32Reg32", GPRegister32.EAX, GPRegister32.ESI),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x66, (byte) 0x97}, "XchgReg32Reg32", GPRegister32.EAX, GPRegister32.EDI),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{(byte) 0x91}, "XchgReg32Reg32", GPRegister32.EAX, GPRegister32.ECX),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{(byte) 0x92}, "XchgReg32Reg32", GPRegister32.EAX, GPRegister32.EDX),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{(byte) 0x93}, "XchgReg32Reg32", GPRegister32.EAX, GPRegister32.EBX),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{(byte) 0x94}, "XchgReg32Reg32", GPRegister32.EAX, GPRegister32.ESP),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{(byte) 0x95}, "XchgReg32Reg32", GPRegister32.EAX, GPRegister32.EBP),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{(byte) 0x96}, "XchgReg32Reg32", GPRegister32.EAX, GPRegister32.ESI),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{(byte) 0x97}, "XchgReg32Reg32", GPRegister32.EAX, GPRegister32.EDI),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{0x66, (byte) 0x90}, "XchgReg16Reg16", GPRegister16.AX, GPRegister16.AX),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{0x66, (byte) 0x91}, "XchgReg16Reg16", GPRegister16.AX, GPRegister16.CX),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{0x66, (byte) 0x92}, "XchgReg16Reg16", GPRegister16.AX, GPRegister16.DX),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{0x66, (byte) 0x93}, "XchgReg16Reg16", GPRegister16.AX, GPRegister16.BX),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{0x66, (byte) 0x94}, "XchgReg16Reg16", GPRegister16.AX, GPRegister16.SP),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{0x66, (byte) 0x95}, "XchgReg16Reg16", GPRegister16.AX, GPRegister16.BP),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{0x66, (byte) 0x96}, "XchgReg16Reg16", GPRegister16.AX, GPRegister16.SI),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{0x66, (byte) 0x97}, "XchgReg16Reg16", GPRegister16.AX, GPRegister16.DI),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{(byte) 0x91}, "XchgReg32Reg32", GPRegister32.EAX, GPRegister32.ECX),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{(byte) 0x92}, "XchgReg32Reg32", GPRegister32.EAX, GPRegister32.EDX),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{(byte) 0x93}, "XchgReg32Reg32", GPRegister32.EAX, GPRegister32.EBX),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{(byte) 0x94}, "XchgReg32Reg32", GPRegister32.EAX, GPRegister32.ESP),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{(byte) 0x95}, "XchgReg32Reg32", GPRegister32.EAX, GPRegister32.EBP),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{(byte) 0x96}, "XchgReg32Reg32", GPRegister32.EAX, GPRegister32.ESI),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{(byte) 0x97}, "XchgReg32Reg32", GPRegister32.EAX, GPRegister32.EDI),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x66, (byte) 0x90}, "XchgReg16Reg16", GPRegister16.AX, GPRegister16.AX),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x66, (byte) 0x91}, "XchgReg16Reg16", GPRegister16.AX, GPRegister16.CX),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x66, (byte) 0x92}, "XchgReg16Reg16", GPRegister16.AX, GPRegister16.DX),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x66, (byte) 0x93}, "XchgReg16Reg16", GPRegister16.AX, GPRegister16.BX),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x66, (byte) 0x94}, "XchgReg16Reg16", GPRegister16.AX, GPRegister16.SP),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x66, (byte) 0x95}, "XchgReg16Reg16", GPRegister16.AX, GPRegister16.BP),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x66, (byte) 0x96}, "XchgReg16Reg16", GPRegister16.AX, GPRegister16.SI),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x66, (byte) 0x97}, "XchgReg16Reg16", GPRegister16.AX, GPRegister16.DI),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x48, (byte) 0x91}, "XchgReg64Reg64", GPRegister64.RAX, GPRegister64.RCX),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x48, (byte) 0x92}, "XchgReg64Reg64", GPRegister64.RAX, GPRegister64.RDX),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x48, (byte) 0x93}, "XchgReg64Reg64", GPRegister64.RAX, GPRegister64.RBX),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x48, (byte) 0x94}, "XchgReg64Reg64", GPRegister64.RAX, GPRegister64.RSP),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x48, (byte) 0x95}, "XchgReg64Reg64", GPRegister64.RAX, GPRegister64.RBP),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x48, (byte) 0x96}, "XchgReg64Reg64", GPRegister64.RAX, GPRegister64.RSI),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x48, (byte) 0x97}, "XchgReg64Reg64", GPRegister64.RAX, GPRegister64.RDI),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x41, (byte) 0x90}, "XchgReg32Reg32", GPRegister32.EAX, GPRegister32.R8D),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x41, (byte) 0x91}, "XchgReg32Reg32", GPRegister32.EAX, GPRegister32.R9D),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x41, (byte) 0x92}, "XchgReg32Reg32", GPRegister32.EAX, GPRegister32.R10D),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x41, (byte) 0x93}, "XchgReg32Reg32", GPRegister32.EAX, GPRegister32.R11D),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x41, (byte) 0x94}, "XchgReg32Reg32", GPRegister32.EAX, GPRegister32.R12D),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x41, (byte) 0x95}, "XchgReg32Reg32", GPRegister32.EAX, GPRegister32.R13D),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x41, (byte) 0x96}, "XchgReg32Reg32", GPRegister32.EAX, GPRegister32.R14D),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x41, (byte) 0x97}, "XchgReg32Reg32", GPRegister32.EAX, GPRegister32.R15D),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x66, 0x41, (byte) 0x90}, "XchgReg16Reg16", GPRegister16.AX, GPRegister16.R8W),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x66, 0x41, (byte) 0x91}, "XchgReg16Reg16", GPRegister16.AX, GPRegister16.R9W),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x66, 0x41, (byte) 0x92}, "XchgReg16Reg16", GPRegister16.AX, GPRegister16.R10W),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x66, 0x41, (byte) 0x93}, "XchgReg16Reg16", GPRegister16.AX, GPRegister16.R11W),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x66, 0x41, (byte) 0x94}, "XchgReg16Reg16", GPRegister16.AX, GPRegister16.R12W),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x66, 0x41, (byte) 0x95}, "XchgReg16Reg16", GPRegister16.AX, GPRegister16.R13W),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x66, 0x41, (byte) 0x96}, "XchgReg16Reg16", GPRegister16.AX, GPRegister16.R14W),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x66, 0x41, (byte) 0x97}, "XchgReg16Reg16", GPRegister16.AX, GPRegister16.R15W),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x49, (byte) 0x90}, "XchgReg64Reg64", GPRegister64.RAX, GPRegister64.R8),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x49, (byte) 0x91}, "XchgReg64Reg64", GPRegister64.RAX, GPRegister64.R9),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x49, (byte) 0x92}, "XchgReg64Reg64", GPRegister64.RAX, GPRegister64.R10),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x49, (byte) 0x93}, "XchgReg64Reg64", GPRegister64.RAX, GPRegister64.R11),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x49, (byte) 0x94}, "XchgReg64Reg64", GPRegister64.RAX, GPRegister64.R12),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x49, (byte) 0x95}, "XchgReg64Reg64", GPRegister64.RAX, GPRegister64.R13),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x49, (byte) 0x96}, "XchgReg64Reg64", GPRegister64.RAX, GPRegister64.R14),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x49, (byte) 0x97}, "XchgReg64Reg64", GPRegister64.RAX, GPRegister64.R15));
    }

    @ParameterizedTest
    @MethodSource("decIncEncodings")
    @DisplayName("Dec/Inc parse test")
    void testParseInc(@NotNull Instruction.Mode mode, Byte[] opcodes, String name, Object register) {
        Optional<Instruction> instruction =
                Instruction.parse(mode, new RollbackIterator<>(Arrays.asList(opcodes).iterator()));
        assertEquals(
                "org.yajd.x86.cpu.Instruction$" + name,
                instruction.get().getClass().getName());
        var arguments = instruction.get().getArguments();
        assertEquals(register, arguments[0].process(new Argument.Result<Object>() {
            @Override
            public GPRegister16 when(@NotNull GPRegister16 argument) {
                return argument;
            }

            @Override
            public GPRegister32 when(@NotNull GPRegister32 argument) {
                return argument;
            }
        }));
    }

    @Test
    @DisplayName("MovReg8Reg8 test")
    void testMovReg8Reg8() {
        Instruction instruction = new Instruction.MovReg8Reg8(GPRegister8.AL, GPRegister8.CL, null);
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
        Instruction instruction = new Instruction.MovReg16Reg16(GPRegister16.AX, GPRegister16.CX, null);
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
        Instruction instruction =
                new Instruction.MovReg32Reg32(GPRegister32.EAX, GPRegister32.ECX, null);
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
        Instruction instruction =
                new Instruction.MovReg64Reg64(GPRegister64.RAX, GPRegister64.RCX, null);
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

    @ParameterizedTest
    @MethodSource("nopEncodings")
    @DisplayName("Nop parse test")
    void testParseNop(@NotNull Instruction.Mode mode, Byte[] opcodes, String name) {
        Optional<Instruction> instruction =
                Instruction.parse(mode, new RollbackIterator<>(Arrays.asList(opcodes).iterator()));
        assertEquals(
                "org.yajd.x86.cpu.Instruction$" + name,
                instruction.get().getClass().getName());
        var arguments = instruction.get().getArguments();
        assertEquals(0, arguments.length);
    }

    @ParameterizedTest
    @MethodSource("xchgAccumulatorRegEncodings")
    @DisplayName("Xchg Accumulator with Register parse test")
    void testParseXchgAccumulatorRegister(@NotNull Instruction.Mode mode, Byte[] opcodes, String name, Object register1, Object register2) {
        Optional<Instruction> instruction =
                Instruction.parse(mode, new RollbackIterator<>(Arrays.asList(opcodes).iterator()));
        assertEquals(
                "org.yajd.x86.cpu.Instruction$" + name,
                instruction.get().getClass().getName());
        var arguments = instruction.get().getArguments();
        var GetResult = new Argument.Result<Object>() {
            @Override
            public GPRegister16 when(@NotNull GPRegister16 argument) {
                return argument;
            }

            @Override
            public GPRegister32 when(@NotNull GPRegister32 argument) {
                return argument;
            }

            @Override
            public GPRegister64 when(@NotNull GPRegister64 argument) {
                return argument;
            }
        };
        assertEquals(register1, arguments[0].process(GetResult));
        assertEquals(register2, arguments[1].process(GetResult));
    }

    @ParameterizedTest
    @MethodSource("twoArgumentRegisterInstructions")
    @DisplayName("Two argument register instructions test")
    void testTwoArgumentRegisterInstructions(
            @NotNull Instruction instruction, Object argument0, Object argument1) throws Exception {
        var arg0 = instruction.getClass().getMethod("getArg0").invoke(instruction);
        var arg1 = instruction.getClass().getMethod("getArg1").invoke(instruction);
        assertEquals(argument0, arg0);
        assertEquals(argument1, arg1);
    }

    @ParameterizedTest
    @MethodSource("twoArgumentAddressInstructions")
    @DisplayName("Two argument address instructions test")
    void testTwoArgumentAddressInstructions(@NotNull Instruction instruction, Object argument0,
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
}
