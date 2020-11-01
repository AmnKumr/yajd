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
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte) 0x90}, "Nop"),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{(byte) 0x90}, "Nop"),
                arguments(Instruction.Mode.ADDR32_DATA16, new Byte[]{(byte) 0x90}, "Nop"),
                arguments(Instruction.Mode.ADDR32_DATA32, new Byte[]{(byte) 0x90}, "Nop"),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{(byte) 0x90}, "Nop"),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x40, (byte) 0x90}, "Nop"),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x42, (byte) 0x90}, "Nop"),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x44, (byte) 0x90}, "Nop"),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x46, (byte) 0x90}, "Nop"));
    }

    static @NotNull Stream<Arguments> twoArgumentRegisterEncoding() {
        return Stream.of(
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x00, (byte) 0xd1}, "AddReg8Reg8", GPRegister8.CL, GPRegister8.DL),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x01, (byte) 0xd1}, "AddReg16Reg16", GPRegister16.CX, GPRegister16.DX),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x66, 0x01, (byte) 0xd1}, "AddReg32Reg32", GPRegister32.ECX, GPRegister32.EDX),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x48, 0x01, (byte) 0xd1}, "AddReg64Reg64", GPRegister64.RCX, GPRegister64.RDX),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x02, (byte) 0xca}, "AddReg8Reg8", GPRegister8.CL, GPRegister8.DL),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x03, (byte) 0xca}, "AddReg16Reg16", GPRegister16.CX, GPRegister16.DX),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x66, 0x03, (byte) 0xca}, "AddReg32Reg32", GPRegister32.ECX, GPRegister32.EDX),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x48, 0x03, (byte) 0xca}, "AddReg64Reg64", GPRegister64.RCX, GPRegister64.RDX),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte)0x88, (byte) 0xd1}, "MovReg8Reg8", GPRegister8.CL, GPRegister8.DL),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte)0x89, (byte) 0xd1}, "MovReg16Reg16", GPRegister16.CX, GPRegister16.DX),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x66, (byte)0x89, (byte) 0xd1}, "MovReg32Reg32", GPRegister32.ECX, GPRegister32.EDX),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x48, (byte)0x89, (byte) 0xd1}, "MovReg64Reg64", GPRegister64.RCX, GPRegister64.RDX),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x2a, (byte) 0xca}, "SubReg8Reg8", GPRegister8.CL, GPRegister8.DL),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x2b, (byte) 0xca}, "SubReg16Reg16", GPRegister16.CX, GPRegister16.DX),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x66, 0x2b, (byte) 0xca}, "SubReg32Reg32", GPRegister32.ECX, GPRegister32.EDX),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x48, 0x2b, (byte) 0xca}, "SubReg64Reg64", GPRegister64.RCX, GPRegister64.RDX),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x28, (byte) 0xd1}, "SubReg8Reg8", GPRegister8.CL, GPRegister8.DL),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x29, (byte) 0xd1}, "SubReg16Reg16", GPRegister16.CX, GPRegister16.DX),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x66, 0x29, (byte) 0xd1}, "SubReg32Reg32", GPRegister32.ECX, GPRegister32.EDX),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x48, 0x29, (byte) 0xd1}, "SubReg64Reg64", GPRegister64.RCX, GPRegister64.RDX),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x2a, (byte) 0xca}, "SubReg8Reg8", GPRegister8.CL, GPRegister8.DL),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x2b, (byte) 0xca}, "SubReg16Reg16", GPRegister16.CX, GPRegister16.DX),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x66, 0x2b, (byte) 0xca}, "SubReg32Reg32", GPRegister32.ECX, GPRegister32.EDX),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x48, 0x2b, (byte) 0xca}, "SubReg64Reg64", GPRegister64.RCX, GPRegister64.RDX));
    }

    static @NotNull Stream<Arguments> twoArgumentAddressEncoding() {
        return Stream.of(
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x26, 0x02, 0x49, 0x01}, "AddReg8Addr16Mem8",
                        GPRegister8.CL, "GPAddress16", SegmentRegister.ES, GPRegister16.BX, GPRegister16.DI, null, (short)1, (short)8),
                arguments(Instruction.Mode.ADDR32_DATA32, new Byte[]{0x2e, 0x66, 0x03, 0x5c, 0x08, 0x02}, "AddReg16Addr32Mem16",
                        GPRegister16.BX, "GPAddress32", SegmentRegister.CS, GPRegister32.EAX, GPRegister32.ECX, ScaleFactor.X1, 2, (short) 16),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x36, 0x03, 0x4c, 0x72, 0x03}, "AddReg32Addr64Mem32",
                        GPRegister32.ECX, "GPAddress64", SegmentRegister.SS, GPRegister64.RDX, GPRegister64.RSI, ScaleFactor.X2, 3, (short) 32),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x3e, 0x67, 0x48, 0x03, 0x15, 0x04, 0x00, 0x00, 0x00}, "AddReg64EIPAddr32Mem64",
                        GPRegister64.RDX, "EIPAddress32", SegmentRegister.DS, null, null, null, 4, (short) 64),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x64, 0x02, 0x25, 0x05, 0x00, 0x00, 0x00}, "AddReg8RIPAddr64Mem8",
                        GPRegister8.AH, "RIPAddress64", SegmentRegister.FS, null, null, null, 5, (short) 8));
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
    @MethodSource("twoArgumentAddressEncoding")
    @DisplayName("Two argument address instructions test")
    void testParseTwoArgumentAddressInstructions(
            @NotNull Instruction.Mode mode, Byte[] opcodes, String name, Object register,
            String addr_name, SegmentRegister segment, Object base, Object index, Object scale, Object disp, short size)
            throws Exception {
        Optional<Instruction> instruction =
                Instruction.parse(mode, new RollbackIterator<>(Arrays.asList(opcodes).iterator()));
        assertEquals(
                "org.yajd.x86.cpu.Instruction$" + name,
                instruction.get().getClass().getName());
        var arguments = instruction.get().getArguments();
        assertEquals(register, arguments[0].process(new Argument.Result<Object>() {
            @Override
            public GPRegister8 when(@NotNull GPRegister8 argument) {
                return argument;
            }

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
        }));
        var address = arguments[1].process(new Argument.Result<Object>() {
            @Override
            public GPAddress16 when(@NotNull GPAddress16 argument) {
                return argument;
            }

            @Override
            public GPAddress32 when(@NotNull GPAddress32 argument) {
                return argument;
            }

            @Override
            public EIPAddress32 when(@NotNull EIPAddress32 argument) {
                return argument;
            }

            @Override
            public GPAddress64 when(@NotNull GPAddress64 argument) {
                return argument;
            }

            @Override
            public RIPAddress64 when(@NotNull RIPAddress64 argument) {
                return argument;
            }
        });
        assertEquals(
                "org.yajd.x86.cpu." + addr_name,
                address.getClass().getName());
        if (segment != null) {
            assertEquals(Optional.of(segment), address.getClass().getMethod("getSegment").invoke(address));
        }
        if (base != null) {
            assertEquals(Optional.of(base), address.getClass().getMethod("getBase").invoke(address));
        }
        if (index != null) {
            assertEquals(Optional.of(index), address.getClass().getMethod("getIndex").invoke(address));
        }
        if (scale != null) {
            assertEquals(scale, address.getClass().getMethod("getScale").invoke(address));
        }
        assertEquals(disp, address.getClass().getMethod("getDisp").invoke(address));
        assertEquals(size, address.getClass().getMethod("getSize").invoke(address));
    }

    @ParameterizedTest
    @MethodSource("twoArgumentRegisterEncoding")
    @DisplayName("Two argument register instructions test")
    void testParseTwoArgumentRegisterInstructions(
            @NotNull Instruction.Mode mode, Byte[] opcodes, String name, Object register1, Object register2) {
        Optional<Instruction> instruction =
                Instruction.parse(mode, new RollbackIterator<>(Arrays.asList(opcodes).iterator()));
        assertEquals(
                "org.yajd.x86.cpu.Instruction$" + name,
                instruction.get().getClass().getName());
        var arguments = instruction.get().getArguments();
        var GetResult = new Argument.Result<Object>() {
            @Override
            public GPRegister8 when(@NotNull GPRegister8 argument) {
                return argument;
            }

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
}
