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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

public class InstructionTest {
    static @NotNull Stream<Arguments> decIncMemoryEncodings() {
        return Stream.of(
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x26, (byte)0xff, 0x49, 0x01}, "DecAddr16Mem16",
                        "GPAddress16", SegmentRegister.ES, GPRegister16.BX, GPRegister16.DI, null, (short)1, (short)16),
                arguments(Instruction.Mode.ADDR32_DATA32, new Byte[]{0x2e, 0x66, (byte)0xff, 0x4c, 0x08, 0x02}, "DecAddr32Mem16",
                        "GPAddress32", SegmentRegister.CS, GPRegister32.EAX, GPRegister32.ECX, ScaleFactor.X1, 2, (short) 16),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x36, (byte)0xff, 0x4c, 0x72, 0x03}, "DecAddr64Mem32",
                        "GPAddress64", SegmentRegister.SS, GPRegister64.RDX, GPRegister64.RSI, ScaleFactor.X2, 3, (short) 32),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x3e, 0x67, 0x48, (byte)0xff, 0x0d, 0x04, 0x00, 0x00, 0x00}, "DecEIPAddr32Mem64",
                        "EIPAddress32", SegmentRegister.DS, null, null, null, 4, (short) 64),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x64, (byte)0xff, 0x0d, 0x05, 0x00, 0x00, 0x00}, "DecRIPAddr64Mem32",
                        "RIPAddress64", SegmentRegister.FS, null, null, null, 5, (short) 32),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x26, (byte)0xff, 0x41, 0x01}, "IncAddr16Mem16",
                        "GPAddress16", SegmentRegister.ES, GPRegister16.BX, GPRegister16.DI, null, (short)1, (short)16),
                arguments(Instruction.Mode.ADDR32_DATA32, new Byte[]{0x2e, 0x66, (byte)0xff, 0x44, 0x08, 0x02}, "IncAddr32Mem16",
                        "GPAddress32", SegmentRegister.CS, GPRegister32.EAX, GPRegister32.ECX, ScaleFactor.X1, 2, (short) 16),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x36, (byte)0xff, 0x44, 0x72, 0x03}, "IncAddr64Mem32",
                        "GPAddress64", SegmentRegister.SS, GPRegister64.RDX, GPRegister64.RSI, ScaleFactor.X2, 3, (short) 32),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x3e, 0x67, 0x48, (byte)0xff, 0x05, 0x04, 0x00, 0x00, 0x00}, "IncEIPAddr32Mem64",
                        "EIPAddress32", SegmentRegister.DS, null, null, null, 4, (short) 64),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x64, (byte)0xff, 0x05, 0x05, 0x00, 0x00, 0x00}, "IncRIPAddr64Mem32",
                        "RIPAddress64", SegmentRegister.FS, null, null, null, 5, (short) 32));
    }

    static @NotNull Stream<Arguments> decIncRegisterEncodings() {
        return Stream.of(
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x48}, "DecReg16", GPRegister16.AX),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte)0xff, (byte)0xc8}, "DecReg16", GPRegister16.AX),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{0x66, 0x48}, "DecReg16", GPRegister16.AX),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{0x66, (byte)0xff, (byte)0xc8}, "DecReg16", GPRegister16.AX),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x66, (byte)0xff, (byte)0xc8}, "DecReg16", GPRegister16.AX),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x49}, "DecReg16", GPRegister16.CX),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte)0xff, (byte)0xc9}, "DecReg16", GPRegister16.CX),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{0x66, 0x49}, "DecReg16", GPRegister16.CX),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{0x66, (byte)0xff, (byte)0xc9}, "DecReg16", GPRegister16.CX),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x66, (byte)0xff, (byte)0xc9}, "DecReg16", GPRegister16.CX),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x4a}, "DecReg16", GPRegister16.DX),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte)0xff, (byte)0xca}, "DecReg16", GPRegister16.DX),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{0x66, 0x4a}, "DecReg16", GPRegister16.DX),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{0x66, (byte)0xff, (byte)0xca}, "DecReg16", GPRegister16.DX),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x66, (byte)0xff, (byte)0xca}, "DecReg16", GPRegister16.DX),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x4b}, "DecReg16", GPRegister16.BX),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte)0xff, (byte)0xcb}, "DecReg16", GPRegister16.BX),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{0x66, 0x4b}, "DecReg16", GPRegister16.BX),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{0x66, (byte)0xff, (byte)0xcb}, "DecReg16", GPRegister16.BX),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x66, (byte)0xff, (byte)0xcb}, "DecReg16", GPRegister16.BX),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x4c}, "DecReg16", GPRegister16.SP),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte)0xff, (byte)0xcc}, "DecReg16", GPRegister16.SP),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{0x66, 0x4c}, "DecReg16", GPRegister16.SP),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{0x66, (byte)0xff, (byte)0xcc}, "DecReg16", GPRegister16.SP),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x66, (byte)0xff, (byte)0xcc}, "DecReg16", GPRegister16.SP),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x4d}, "DecReg16", GPRegister16.BP),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte)0xff, (byte)0xcd}, "DecReg16", GPRegister16.BP),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{0x66, 0x4d}, "DecReg16", GPRegister16.BP),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{0x66, (byte)0xff, (byte)0xcd}, "DecReg16", GPRegister16.BP),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x66, (byte)0xff, (byte)0xcd}, "DecReg16", GPRegister16.BP),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x4e}, "DecReg16", GPRegister16.SI),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte)0xff, (byte)0xce}, "DecReg16", GPRegister16.SI),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{0x66, 0x4e}, "DecReg16", GPRegister16.SI),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{0x66, (byte)0xff, (byte)0xce}, "DecReg16", GPRegister16.SI),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x66, (byte)0xff, (byte)0xce}, "DecReg16", GPRegister16.SI),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x4f}, "DecReg16", GPRegister16.DI),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte)0xff, (byte)0xcf}, "DecReg16", GPRegister16.DI),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{0x66, 0x4f}, "DecReg16", GPRegister16.DI),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{0x66, (byte)0xff, (byte)0xcf}, "DecReg16", GPRegister16.DI),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x66, (byte)0xff, (byte)0xcf}, "DecReg16", GPRegister16.DI),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x66, 0x41, (byte)0xff, (byte)0xc8}, "DecReg16", GPRegister16.R8W),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x66, 0x41, (byte)0xff, (byte)0xc9}, "DecReg16", GPRegister16.R9W),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x66, 0x41, (byte)0xff, (byte)0xca}, "DecReg16", GPRegister16.R10W),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x66, 0x41, (byte)0xff, (byte)0xcb}, "DecReg16", GPRegister16.R11W),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x66, 0x41, (byte)0xff, (byte)0xcc}, "DecReg16", GPRegister16.R12W),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x66, 0x41, (byte)0xff, (byte)0xcd}, "DecReg16", GPRegister16.R13W),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x66, 0x41, (byte)0xff, (byte)0xce}, "DecReg16", GPRegister16.R14W),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x66, 0x41, (byte)0xff, (byte)0xcf}, "DecReg16", GPRegister16.R15W),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x66, 0x48}, "DecReg32", GPRegister32.EAX),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x66, (byte)0xff, (byte)0xc8}, "DecReg32", GPRegister32.EAX),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{0x48}, "DecReg32", GPRegister32.EAX),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{(byte)0xff, (byte)0xc8}, "DecReg32", GPRegister32.EAX),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{(byte)0xff, (byte)0xc8}, "DecReg32", GPRegister32.EAX),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x66, 0x49}, "DecReg32", GPRegister32.ECX),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x66, (byte)0xff, (byte)0xc9}, "DecReg32", GPRegister32.ECX),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{0x49}, "DecReg32", GPRegister32.ECX),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{(byte)0xff, (byte)0xc9}, "DecReg32", GPRegister32.ECX),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{(byte)0xff, (byte)0xc9}, "DecReg32", GPRegister32.ECX),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x66, 0x4a}, "DecReg32", GPRegister32.EDX),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x66, (byte)0xff, (byte)0xca}, "DecReg32", GPRegister32.EDX),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{0x4a}, "DecReg32", GPRegister32.EDX),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{(byte)0xff, (byte)0xca}, "DecReg32", GPRegister32.EDX),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{(byte)0xff, (byte)0xca}, "DecReg32", GPRegister32.EDX),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x66, 0x4b}, "DecReg32", GPRegister32.EBX),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x66, (byte)0xff, (byte)0xcb}, "DecReg32", GPRegister32.EBX),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{0x4b}, "DecReg32", GPRegister32.EBX),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{(byte)0xff, (byte)0xcb}, "DecReg32", GPRegister32.EBX),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{(byte)0xff, (byte)0xcb}, "DecReg32", GPRegister32.EBX),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x66, 0x4c}, "DecReg32", GPRegister32.ESP),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x66, (byte)0xff, (byte)0xcc}, "DecReg32", GPRegister32.ESP),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{0x4c}, "DecReg32", GPRegister32.ESP),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{(byte)0xff, (byte)0xcc}, "DecReg32", GPRegister32.ESP),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{(byte)0xff, (byte)0xcc}, "DecReg32", GPRegister32.ESP),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x66, 0x4d}, "DecReg32", GPRegister32.EBP),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x66, (byte)0xff, (byte)0xcd}, "DecReg32", GPRegister32.EBP),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{0x4d}, "DecReg32", GPRegister32.EBP),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{(byte)0xff, (byte)0xcd}, "DecReg32", GPRegister32.EBP),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{(byte)0xff, (byte)0xcd}, "DecReg32", GPRegister32.EBP),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x66, 0x4e}, "DecReg32", GPRegister32.ESI),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x66, (byte)0xff, (byte)0xce}, "DecReg32", GPRegister32.ESI),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{0x4e}, "DecReg32", GPRegister32.ESI),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{(byte)0xff, (byte)0xce}, "DecReg32", GPRegister32.ESI),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{(byte)0xff, (byte)0xce}, "DecReg32", GPRegister32.ESI),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x66, 0x4f}, "DecReg32", GPRegister32.EDI),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x66, (byte)0xff, (byte)0xcf}, "DecReg32", GPRegister32.EDI),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{0x4f}, "DecReg32", GPRegister32.EDI),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{(byte)0xff, (byte)0xcf}, "DecReg32", GPRegister32.EDI),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{(byte)0xff, (byte)0xcf}, "DecReg32", GPRegister32.EDI),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x41, (byte)0xff, (byte)0xc8}, "DecReg32", GPRegister32.R8D),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x41, (byte)0xff, (byte)0xc9}, "DecReg32", GPRegister32.R9D),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x41, (byte)0xff, (byte)0xca}, "DecReg32", GPRegister32.R10D),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x41, (byte)0xff, (byte)0xcb}, "DecReg32", GPRegister32.R11D),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x41, (byte)0xff, (byte)0xcc}, "DecReg32", GPRegister32.R12D),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x41, (byte)0xff, (byte)0xcd}, "DecReg32", GPRegister32.R13D),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x41, (byte)0xff, (byte)0xce}, "DecReg32", GPRegister32.R14D),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x41, (byte)0xff, (byte)0xcf}, "DecReg32", GPRegister32.R15D),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x48, (byte)0xff, (byte)0xc8}, "DecReg64", GPRegister64.RAX),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x48, (byte)0xff, (byte)0xc9}, "DecReg64", GPRegister64.RCX),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x48, (byte)0xff, (byte)0xca}, "DecReg64", GPRegister64.RDX),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x48, (byte)0xff, (byte)0xcb}, "DecReg64", GPRegister64.RBX),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x48, (byte)0xff, (byte)0xcc}, "DecReg64", GPRegister64.RSP),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x48, (byte)0xff, (byte)0xcd}, "DecReg64", GPRegister64.RBP),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x48, (byte)0xff, (byte)0xce}, "DecReg64", GPRegister64.RSI),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x48, (byte)0xff, (byte)0xcf}, "DecReg64", GPRegister64.RDI),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x49, (byte)0xff, (byte)0xc8}, "DecReg64", GPRegister64.R8),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x49, (byte)0xff, (byte)0xc9}, "DecReg64", GPRegister64.R9),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x49, (byte)0xff, (byte)0xca}, "DecReg64", GPRegister64.R10),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x49, (byte)0xff, (byte)0xcb}, "DecReg64", GPRegister64.R11),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x49, (byte)0xff, (byte)0xcc}, "DecReg64", GPRegister64.R12),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x49, (byte)0xff, (byte)0xcd}, "DecReg64", GPRegister64.R13),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x49, (byte)0xff, (byte)0xce}, "DecReg64", GPRegister64.R14),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x49, (byte)0xff, (byte)0xcf}, "DecReg64", GPRegister64.R15),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x40}, "IncReg16", GPRegister16.AX),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte)0xff, (byte)0xc0}, "IncReg16", GPRegister16.AX),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{0x66, 0x40}, "IncReg16", GPRegister16.AX),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{0x66, (byte)0xff, (byte)0xc0}, "IncReg16", GPRegister16.AX),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x66, (byte)0xff, (byte)0xc0}, "IncReg16", GPRegister16.AX),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x41}, "IncReg16", GPRegister16.CX),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte)0xff, (byte)0xc1}, "IncReg16", GPRegister16.CX),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{0x66, 0x41}, "IncReg16", GPRegister16.CX),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{0x66, (byte)0xff, (byte)0xc1}, "IncReg16", GPRegister16.CX),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x66, (byte)0xff, (byte)0xc1}, "IncReg16", GPRegister16.CX),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x42}, "IncReg16", GPRegister16.DX),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte)0xff, (byte)0xc2}, "IncReg16", GPRegister16.DX),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{0x66, 0x42}, "IncReg16", GPRegister16.DX),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{0x66, (byte)0xff, (byte)0xc2}, "IncReg16", GPRegister16.DX),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x66, (byte)0xff, (byte)0xc2}, "IncReg16", GPRegister16.DX),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x43}, "IncReg16", GPRegister16.BX),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte)0xff, (byte)0xc3}, "IncReg16", GPRegister16.BX),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{0x66, 0x43}, "IncReg16", GPRegister16.BX),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{0x66, (byte)0xff, (byte)0xc3}, "IncReg16", GPRegister16.BX),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x66, (byte)0xff, (byte)0xc3}, "IncReg16", GPRegister16.BX),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x44}, "IncReg16", GPRegister16.SP),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte)0xff, (byte)0xc4}, "IncReg16", GPRegister16.SP),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{0x66, 0x44}, "IncReg16", GPRegister16.SP),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{0x66, (byte)0xff, (byte)0xc4}, "IncReg16", GPRegister16.SP),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x66, (byte)0xff, (byte)0xc4}, "IncReg16", GPRegister16.SP),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x45}, "IncReg16", GPRegister16.BP),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte)0xff, (byte)0xc5}, "IncReg16", GPRegister16.BP),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{0x66, 0x45}, "IncReg16", GPRegister16.BP),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{0x66, (byte)0xff, (byte)0xc5}, "IncReg16", GPRegister16.BP),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x66, (byte)0xff, (byte)0xc5}, "IncReg16", GPRegister16.BP),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x46}, "IncReg16", GPRegister16.SI),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte)0xff, (byte)0xc6}, "IncReg16", GPRegister16.SI),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{0x66, 0x46}, "IncReg16", GPRegister16.SI),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{0x66, (byte)0xff, (byte)0xc6}, "IncReg16", GPRegister16.SI),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x66, (byte)0xff, (byte)0xc6}, "IncReg16", GPRegister16.SI),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x47}, "IncReg16", GPRegister16.DI),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte)0xff, (byte)0xc7}, "IncReg16", GPRegister16.DI),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{0x66, 0x47}, "IncReg16", GPRegister16.DI),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{0x66, (byte)0xff, (byte)0xc7}, "IncReg16", GPRegister16.DI),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x66, (byte)0xff, (byte)0xc7}, "IncReg16", GPRegister16.DI),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x66, 0x41, (byte)0xff, (byte)0xc0}, "IncReg16", GPRegister16.R8W),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x66, 0x41, (byte)0xff, (byte)0xc1}, "IncReg16", GPRegister16.R9W),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x66, 0x41, (byte)0xff, (byte)0xc2}, "IncReg16", GPRegister16.R10W),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x66, 0x41, (byte)0xff, (byte)0xc3}, "IncReg16", GPRegister16.R11W),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x66, 0x41, (byte)0xff, (byte)0xc4}, "IncReg16", GPRegister16.R12W),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x66, 0x41, (byte)0xff, (byte)0xc5}, "IncReg16", GPRegister16.R13W),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x66, 0x41, (byte)0xff, (byte)0xc6}, "IncReg16", GPRegister16.R14W),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x66, 0x41, (byte)0xff, (byte)0xc7}, "IncReg16", GPRegister16.R15W),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x66, 0x40}, "IncReg32", GPRegister32.EAX),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x66, (byte)0xff, (byte)0xc0}, "IncReg32", GPRegister32.EAX),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{0x40}, "IncReg32", GPRegister32.EAX),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{(byte)0xff, (byte)0xc0}, "IncReg32", GPRegister32.EAX),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{(byte)0xff, (byte)0xc0}, "IncReg32", GPRegister32.EAX),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x66, 0x41}, "IncReg32", GPRegister32.ECX),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x66, (byte)0xff, (byte)0xc1}, "IncReg32", GPRegister32.ECX),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{0x41}, "IncReg32", GPRegister32.ECX),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{(byte)0xff, (byte)0xc1}, "IncReg32", GPRegister32.ECX),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{(byte)0xff, (byte)0xc1}, "IncReg32", GPRegister32.ECX),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x66, 0x42}, "IncReg32", GPRegister32.EDX),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x66, (byte)0xff, (byte)0xc2}, "IncReg32", GPRegister32.EDX),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{0x42}, "IncReg32", GPRegister32.EDX),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{(byte)0xff, (byte)0xc2}, "IncReg32", GPRegister32.EDX),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{(byte)0xff, (byte)0xc2}, "IncReg32", GPRegister32.EDX),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x66, 0x43}, "IncReg32", GPRegister32.EBX),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x66, (byte)0xff, (byte)0xc3}, "IncReg32", GPRegister32.EBX),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{0x43}, "IncReg32", GPRegister32.EBX),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{(byte)0xff, (byte)0xc3}, "IncReg32", GPRegister32.EBX),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{(byte)0xff, (byte)0xc3}, "IncReg32", GPRegister32.EBX),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x66, 0x44}, "IncReg32", GPRegister32.ESP),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x66, (byte)0xff, (byte)0xc4}, "IncReg32", GPRegister32.ESP),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{0x44}, "IncReg32", GPRegister32.ESP),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{(byte)0xff, (byte)0xc4}, "IncReg32", GPRegister32.ESP),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{(byte)0xff, (byte)0xc4}, "IncReg32", GPRegister32.ESP),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x66, 0x45}, "IncReg32", GPRegister32.EBP),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x66, (byte)0xff, (byte)0xc5}, "IncReg32", GPRegister32.EBP),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{0x45}, "IncReg32", GPRegister32.EBP),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{(byte)0xff, (byte)0xc5}, "IncReg32", GPRegister32.EBP),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{(byte)0xff, (byte)0xc5}, "IncReg32", GPRegister32.EBP),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x66, 0x46}, "IncReg32", GPRegister32.ESI),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x66, (byte)0xff, (byte)0xc6}, "IncReg32", GPRegister32.ESI),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{0x46}, "IncReg32", GPRegister32.ESI),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{(byte)0xff, (byte)0xc6}, "IncReg32", GPRegister32.ESI),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{(byte)0xff, (byte)0xc6}, "IncReg32", GPRegister32.ESI),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x66, 0x47}, "IncReg32", GPRegister32.EDI),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x66, (byte)0xff, (byte)0xc7}, "IncReg32", GPRegister32.EDI),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{0x47}, "IncReg32", GPRegister32.EDI),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{(byte)0xff, (byte)0xc7}, "IncReg32", GPRegister32.EDI),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{(byte)0xff, (byte)0xc7}, "IncReg32", GPRegister32.EDI),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x41, (byte)0xff, (byte)0xc0}, "IncReg32", GPRegister32.R8D),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x41, (byte)0xff, (byte)0xc1}, "IncReg32", GPRegister32.R9D),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x41, (byte)0xff, (byte)0xc2}, "IncReg32", GPRegister32.R10D),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x41, (byte)0xff, (byte)0xc3}, "IncReg32", GPRegister32.R11D),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x41, (byte)0xff, (byte)0xc4}, "IncReg32", GPRegister32.R12D),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x41, (byte)0xff, (byte)0xc5}, "IncReg32", GPRegister32.R13D),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x41, (byte)0xff, (byte)0xc6}, "IncReg32", GPRegister32.R14D),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x41, (byte)0xff, (byte)0xc7}, "IncReg32", GPRegister32.R15D),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x48, (byte)0xff, (byte)0xc0}, "IncReg64", GPRegister64.RAX),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x48, (byte)0xff, (byte)0xc1}, "IncReg64", GPRegister64.RCX),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x48, (byte)0xff, (byte)0xc2}, "IncReg64", GPRegister64.RDX),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x48, (byte)0xff, (byte)0xc3}, "IncReg64", GPRegister64.RBX),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x48, (byte)0xff, (byte)0xc4}, "IncReg64", GPRegister64.RSP),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x48, (byte)0xff, (byte)0xc5}, "IncReg64", GPRegister64.RBP),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x48, (byte)0xff, (byte)0xc6}, "IncReg64", GPRegister64.RSI),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x48, (byte)0xff, (byte)0xc7}, "IncReg64", GPRegister64.RDI),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x49, (byte)0xff, (byte)0xc0}, "IncReg64", GPRegister64.R8),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x49, (byte)0xff, (byte)0xc1}, "IncReg64", GPRegister64.R9),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x49, (byte)0xff, (byte)0xc2}, "IncReg64", GPRegister64.R10),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x49, (byte)0xff, (byte)0xc3}, "IncReg64", GPRegister64.R11),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x49, (byte)0xff, (byte)0xc4}, "IncReg64", GPRegister64.R12),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x49, (byte)0xff, (byte)0xc5}, "IncReg64", GPRegister64.R13),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x49, (byte)0xff, (byte)0xc6}, "IncReg64", GPRegister64.R14),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x49, (byte)0xff, (byte)0xc7}, "IncReg64", GPRegister64.R15));
    }

    static @NotNull Stream<Arguments> oneArgumentInstructions() {
        return Stream.of(
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte) 0x70, 0x01}, "JoRel8", Rel8.class, (byte) 1),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte) 0x71, 0x02}, "JnoRel8", Rel8.class, (byte) 2),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte) 0x72, 0x03}, "JbRel8", Rel8.class, (byte) 3),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte) 0x73, 0x04}, "JaeRel8", Rel8.class, (byte) 4),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte) 0x74, 0x05}, "JeRel8", Rel8.class, (byte) 5),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte) 0x75, 0x06}, "JneRel8", Rel8.class, (byte) 6),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte) 0x76, 0x07}, "JbeRel8", Rel8.class, (byte) 7),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte) 0x77, 0x08}, "JaRel8", Rel8.class, (byte) 8),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte) 0x78, 0x09}, "JsRel8", Rel8.class, (byte) 9),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte) 0x79, 0x0a}, "JnsRel8", Rel8.class, (byte) 10),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte) 0x7a, 0x0b}, "JpRel8", Rel8.class, (byte) 11),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte) 0x7b, 0x0c}, "JnpRel8", Rel8.class, (byte) 12),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte) 0x7c, 0x0d}, "JlRel8", Rel8.class, (byte) 13),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte) 0x7d, 0x0e}, "JgeRel8", Rel8.class, (byte) 14),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte) 0x7e, 0x0f}, "JleRel8", Rel8.class, (byte) 15),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte) 0x7f, 0x10}, "JgRel8", Rel8.class, (byte) 16),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte) 0xe3, 0x11}, "JcxzRel8", Rel8.class, (byte) 17),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x67, (byte) 0xe3, 0x12}, "JecxzRel8", Rel8.class, (byte) 18),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x66, (byte) 0xe3, 0x13}, "JcxzRel8", Rel8.class, (byte) 19),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x66, 0x67, (byte) 0xe3, 0x14}, "JecxzRel8", Rel8.class, (byte) 20),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{(byte) 0xe3, 0x15}, "JcxzRel8", Rel8.class, (byte) 21),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{0x67, (byte) 0xe3, 0x16}, "JecxzRel8", Rel8.class, (byte) 22),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{0x66, (byte) 0xe3, 0x17}, "JcxzRel8", Rel8.class, (byte) 23),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{0x66, 0x67, (byte) 0xe3, 0x18}, "JecxzRel8", Rel8.class, (byte) 24),
                arguments(Instruction.Mode.ADDR32_DATA16, new Byte[]{(byte) 0xe3, 0x19}, "JecxzRel8", Rel8.class, (byte) 25),
                arguments(Instruction.Mode.ADDR32_DATA16, new Byte[]{0x67, (byte) 0xe3, 0x1a}, "JcxzRel8", Rel8.class, (byte) 26),
                arguments(Instruction.Mode.ADDR32_DATA16, new Byte[]{0x66, (byte) 0xe3, 0x1b}, "JecxzRel8", Rel8.class, (byte) 27),
                arguments(Instruction.Mode.ADDR32_DATA16, new Byte[]{0x66, 0x67, (byte) 0xe3, 0x1c}, "JcxzRel8", Rel8.class, (byte) 28),
                arguments(Instruction.Mode.ADDR32_DATA32, new Byte[]{(byte) 0xe3, 0x1d}, "JecxzRel8", Rel8.class, (byte) 29),
                arguments(Instruction.Mode.ADDR32_DATA32, new Byte[]{0x67, (byte) 0xe3, 0x1e}, "JcxzRel8", Rel8.class, (byte) 30),
                arguments(Instruction.Mode.ADDR32_DATA32, new Byte[]{0x66, (byte) 0xe3, 0x1f}, "JecxzRel8", Rel8.class, (byte) 31),
                arguments(Instruction.Mode.ADDR32_DATA32, new Byte[]{0x66, 0x67, (byte) 0xe3, 0x20}, "JcxzRel8", Rel8.class, (byte) 32),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{(byte) 0xe3, 0x21}, "JrcxzRel8", Rel8.class, (byte) 33),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x67, (byte) 0xe3, 0x22}, "JecxzRel8", Rel8.class, (byte) 34),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x66, (byte) 0xe3, 0x23}, "JrcxzRel8", Rel8.class, (byte) 35),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x66, 0x67, (byte) 0xe3, 0x24}, "JecxzRel8", Rel8.class, (byte) 36),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x40, (byte) 0xe3, 0x25}, "JrcxzRel8", Rel8.class, (byte) 37),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x67, 0x40, (byte) 0xe3, 0x26}, "JecxzRel8", Rel8.class, (byte) 38),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x66, 0x40, (byte) 0xe3, 0x27}, "JrcxzRel8", Rel8.class, (byte) 39),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x66, 0x67, 0x40, (byte) 0xe3, 0x28}, "JecxzRel8", Rel8.class, (byte) 40),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x48, (byte) 0xe3, 0x29}, "JrcxzRel8", Rel8.class, (byte) 41),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x67, 0x48, (byte) 0xe3, 0x2a}, "JecxzRel8", Rel8.class, (byte) 42),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x66, 0x48, (byte) 0xe3, 0x2b}, "JrcxzRel8", Rel8.class, (byte) 43),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x66, 0x67, 0x48, (byte) 0xe3, 0x2c}, "JecxzRel8", Rel8.class, (byte) 44),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte) 0xeb, 0x2d}, "JmpRel8", Rel8.class, (byte) 45),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{(byte) 0xeb, 0x2e}, "JmpRel8", Rel8.class, (byte) 46),
                arguments(Instruction.Mode.ADDR32_DATA16, new Byte[]{(byte) 0xeb, 0x2f}, "JmpRel8", Rel8.class, (byte) 47),
                arguments(Instruction.Mode.ADDR32_DATA32, new Byte[]{(byte) 0xeb, 0x30}, "JmpRel8", Rel8.class, (byte) 48),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{(byte) 0xeb, 0x31}, "JmpRel8", Rel8.class, (byte) 49),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte) 0xe9, 0x32, 0x01}, "JmpRel16", Rel16.class, (short) 0x0132),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{(byte) 0xe9, 0x33, 0x03, 0x02, 0x01}, "JmpRel32", Rel32.class, 0x01020333),
                arguments(Instruction.Mode.ADDR32_DATA16, new Byte[]{(byte) 0xe9, 0x34, 0x01}, "JmpRel16", Rel16.class, (short) 0x0134),
                arguments(Instruction.Mode.ADDR32_DATA32, new Byte[]{(byte) 0xe9, 0x35, 0x03, 0x02, 0x01}, "JmpRel32", Rel32.class, 0x01020335),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{(byte) 0xe9, 0x36, 0x03, 0x02, 0x01}, "JmpRel32", Rel32.class, 0x01020336),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x66, (byte) 0xe9, 0x37, 0x03, 0x02, 0x01}, "JmpRel32", Rel32.class, 0x01020337),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{0x66, (byte) 0xe9, 0x38, 0x01}, "JmpRel16", Rel16.class, (short) 0x0138),
                arguments(Instruction.Mode.ADDR32_DATA16, new Byte[]{0x66, (byte) 0xe9, 0x39, 0x03, 0x02, 0x01}, "JmpRel32", Rel32.class, 0x01020339),
                arguments(Instruction.Mode.ADDR32_DATA32, new Byte[]{0x66, (byte) 0xe9, 0x3a, 0x01}, "JmpRel16", Rel16.class, (short) 0x013a),
                // Implement Intel mode for now: https://bugs.chromium.org/p/nativeclient/issues/detail?id=2578
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x66, (byte) 0xe9, 0x3b, 0x03, 0x02, 0x01}, "JmpRel32", Rel32.class, 0x0102033b));
    }

    static @NotNull Stream<Arguments> twoArgumentImmediateEncoding() {
        return Stream.of(
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte) 0xe4, 0x01}, "InReg8Imm8", GPRegister8.AL, (byte) 1),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte) 0xe5, 0x02}, "InReg16Imm8", GPRegister16.AX, (byte) 2),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x66, (byte) 0xe5, 0x03}, "InReg32Imm8", GPRegister32.EAX, (byte) 3),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte) 0xf2, (byte) 0xe5, 0x04}, "InReg16Imm8", GPRegister16.AX, (byte) 4),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte) 0xf3, (byte) 0xe5, 0x05}, "InReg16Imm8", GPRegister16.AX, (byte) 5),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x66, (byte) 0xf2, (byte) 0xe5, 0x06}, "InReg32Imm8", GPRegister32.EAX, (byte) 6),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x66, (byte) 0xf3, (byte) 0xe5, 0x07}, "InReg32Imm8", GPRegister32.EAX, (byte) 7),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{(byte) 0xe4, 0x08}, "InReg8Imm8", GPRegister8.AL, (byte) 8),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{(byte) 0xe5, 0x09}, "InReg32Imm8", GPRegister32.EAX, (byte) 9),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{0x66, (byte) 0xe5, 0x0a}, "InReg16Imm8", GPRegister16.AX, (byte) 10),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{(byte) 0xf2, (byte) 0xe5, 0x0b}, "InReg32Imm8", GPRegister32.EAX, (byte) 11),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{(byte) 0xf3, (byte) 0xe5, 0x0c}, "InReg32Imm8", GPRegister32.EAX, (byte) 12),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{0x66, (byte) 0xf2, (byte) 0xe5, 0x0d}, "InReg16Imm8", GPRegister16.AX, (byte) 13),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{0x66, (byte) 0xf3, (byte) 0xe5, 0x0e}, "InReg16Imm8", GPRegister16.AX, (byte) 14),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{(byte) 0xe4, 0x0f}, "InReg8Imm8", GPRegister8.AL, (byte) 15),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{(byte) 0xe5, 0x10}, "InReg32Imm8", GPRegister32.EAX, (byte) 16),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x66, (byte) 0xe5, 0x11}, "InReg16Imm8", GPRegister16.AX, (byte) 17),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{(byte) 0xf2, (byte) 0xe5, 0x12}, "InReg32Imm8", GPRegister32.EAX, (byte) 18),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{(byte) 0xf3, (byte) 0xe5, 0x13}, "InReg32Imm8", GPRegister32.EAX, (byte) 19),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x66, (byte) 0xf2, (byte) 0xe5, 0x14}, "InReg16Imm8", GPRegister16.AX, (byte) 20),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x66, (byte) 0xf3, (byte) 0xe5, 0x15}, "InReg16Imm8", GPRegister16.AX, (byte) 21),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x40, (byte) 0xe4, 0x0f}, "InReg8Imm8", GPRegister8.AL, (byte) 15),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x40, (byte) 0xe5, 0x10}, "InReg32Imm8", GPRegister32.EAX, (byte) 16),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x66, 0x40, (byte) 0xe5, 0x11}, "InReg16Imm8", GPRegister16.AX, (byte) 17),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{(byte) 0xf2, 0x40, (byte) 0xe5, 0x12}, "InReg32Imm8", GPRegister32.EAX, (byte) 18),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{(byte) 0xf3, 0x40, (byte) 0xe5, 0x13}, "InReg32Imm8", GPRegister32.EAX, (byte) 19),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x66, (byte) 0xf2, 0x40, (byte) 0xe5, 0x14}, "InReg16Imm8", GPRegister16.AX, (byte) 20),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x66, (byte) 0xf3, 0x40, (byte) 0xe5, 0x15}, "InReg16Imm8", GPRegister16.AX, (byte) 21),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x48, (byte) 0xe4, 0x0f}, "InReg8Imm8", GPRegister8.AL, (byte) 15),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x48, (byte) 0xe5, 0x10}, "InReg32Imm8", GPRegister32.EAX, (byte) 16),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x66, 0x48, (byte) 0xe5, 0x11}, "InReg32Imm8", GPRegister32.EAX, (byte) 17),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{(byte) 0xf2, 0x48, (byte) 0xe5, 0x12}, "InReg32Imm8", GPRegister32.EAX, (byte) 18),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{(byte) 0xf3, 0x48, (byte) 0xe5, 0x13}, "InReg32Imm8", GPRegister32.EAX, (byte) 19),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x66, (byte) 0xf2, 0x48, (byte) 0xe5, 0x14}, "InReg32Imm8", GPRegister32.EAX, (byte) 20),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x66, (byte) 0xf3, 0x48, (byte) 0xe5, 0x15}, "InReg32Imm8", GPRegister32.EAX, (byte) 21),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte) 0xb0, 0x01}, "MovReg8Imm8", GPRegister8.AL, (byte) 1),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte) 0xc6, (byte)0xc0, 0x02}, "MovReg8Imm8", GPRegister8.AL, (byte) 2),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte) 0xb1, 0x03}, "MovReg8Imm8", GPRegister8.CL, (byte) 3),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte) 0xc6, (byte)0xc1, 0x04}, "MovReg8Imm8", GPRegister8.CL, (byte) 4),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte) 0xb2, 0x05}, "MovReg8Imm8", GPRegister8.DL, (byte) 5),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte) 0xc6, (byte)0xc2, 0x06}, "MovReg8Imm8", GPRegister8.DL, (byte) 6),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte) 0xb3, 0x07}, "MovReg8Imm8", GPRegister8.BL, (byte) 7),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte) 0xc6, (byte)0xc3, 0x08}, "MovReg8Imm8", GPRegister8.BL, (byte) 8),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte) 0xb4, 0x09}, "MovReg8Imm8", GPRegister8.AH, (byte) 9),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte) 0xc6, (byte)0xc4, 0x0a}, "MovReg8Imm8", GPRegister8.AH, (byte) 10),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte) 0xb5, 0x0b}, "MovReg8Imm8", GPRegister8.CH, (byte) 11),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte) 0xc6, (byte)0xc5, 0x0c}, "MovReg8Imm8", GPRegister8.CH, (byte) 12),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte) 0xb6, 0x0d}, "MovReg8Imm8", GPRegister8.DH, (byte) 13),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte) 0xc6, (byte)0xc6, 0x0e}, "MovReg8Imm8", GPRegister8.DH, (byte) 14),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte) 0xb7, 0x0f}, "MovReg8Imm8", GPRegister8.BH, (byte) 15),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte) 0xc6, (byte)0xc7, 0x10}, "MovReg8Imm8", GPRegister8.BH, (byte) 16),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte) 0xb8, 0x01, 0x00}, "MovReg16Imm16", GPRegister16.AX, (short) 1),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte) 0xc7, (byte)0xc0, 0x02, 0x00}, "MovReg16Imm16", GPRegister16.AX, (short) 2),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte) 0xb9, 0x03, 0x00}, "MovReg16Imm16", GPRegister16.CX, (short) 3),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte) 0xc7, (byte)0xc1, 0x04, 0x00}, "MovReg16Imm16", GPRegister16.CX, (short) 4),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte) 0xba, 0x05, 0x00}, "MovReg16Imm16", GPRegister16.DX, (short) 5),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte) 0xc7, (byte)0xc2, 0x06, 0x00}, "MovReg16Imm16", GPRegister16.DX, (short) 6),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte) 0xbb, 0x07, 0x00}, "MovReg16Imm16", GPRegister16.BX, (short) 7),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte) 0xc7, (byte)0xc3, 0x08, 0x00}, "MovReg16Imm16", GPRegister16.BX, (short) 8),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte) 0xbc, 0x09, 0x00}, "MovReg16Imm16", GPRegister16.SP, (short) 9),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte) 0xc7, (byte)0xc4, 0x0a, 0x00}, "MovReg16Imm16", GPRegister16.SP, (short) 10),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte) 0xbd, 0x0b, 0x00}, "MovReg16Imm16", GPRegister16.BP, (short) 11),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte) 0xc7, (byte)0xc5, 0x0c, 0x00}, "MovReg16Imm16", GPRegister16.BP, (short) 12),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte) 0xbe, 0x0d, 0x00}, "MovReg16Imm16", GPRegister16.SI, (short) 13),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte) 0xc7, (byte)0xc6, 0x0e, 0x00}, "MovReg16Imm16", GPRegister16.SI, (short) 14),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte) 0xbf, 0x0f, 0x00}, "MovReg16Imm16", GPRegister16.DI, (short) 15),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte) 0xc7, (byte)0xc7, 0x10, 0x00}, "MovReg16Imm16", GPRegister16.DI, (short) 16),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{(byte) 0xb8, 0x01, 0x00, 0x00, 0x00}, "MovReg32Imm32", GPRegister32.EAX, 1),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{(byte) 0xc7, (byte)0xc0, 0x02, 0x00, 0x00, 0x00}, "MovReg32Imm32", GPRegister32.EAX, 2),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{(byte) 0xb9, 0x03, 0x00, 0x00, 0x00}, "MovReg32Imm32", GPRegister32.ECX, 3),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{(byte) 0xc7, (byte)0xc1, 0x04, 0x00, 0x00, 0x00}, "MovReg32Imm32", GPRegister32.ECX, 4),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{(byte) 0xba, 0x05, 0x00, 0x00, 0x00}, "MovReg32Imm32", GPRegister32.EDX, 5),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{(byte) 0xc7, (byte)0xc2, 0x06, 0x00, 0x00, 0x00}, "MovReg32Imm32", GPRegister32.EDX, 6),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{(byte) 0xbb, 0x07, 0x00, 0x00, 0x00}, "MovReg32Imm32", GPRegister32.EBX, 7),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{(byte) 0xc7, (byte)0xc3, 0x08, 0x00, 0x00, 0x00}, "MovReg32Imm32", GPRegister32.EBX, 8),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{(byte) 0xbc, 0x09, 0x00, 0x00, 0x00}, "MovReg32Imm32", GPRegister32.ESP, 9),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{(byte) 0xc7, (byte)0xc4, 0x0a, 0x00, 0x00, 0x00}, "MovReg32Imm32", GPRegister32.ESP, 10),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{(byte) 0xbd, 0x0b, 0x00, 0x00, 0x00}, "MovReg32Imm32", GPRegister32.EBP, 11),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{(byte) 0xc7, (byte)0xc5, 0x0c, 0x00, 0x00, 0x00}, "MovReg32Imm32", GPRegister32.EBP, 12),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{(byte) 0xbe, 0x0d, 0x00, 0x00, 0x00}, "MovReg32Imm32", GPRegister32.ESI, 13),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{(byte) 0xc7, (byte)0xc6, 0x0e, 0x00, 0x00, 0x00}, "MovReg32Imm32", GPRegister32.ESI, 14),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{(byte) 0xbf, 0x0f, 0x00, 0x00, 0x00}, "MovReg32Imm32", GPRegister32.EDI, 15),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{(byte) 0xc7, (byte)0xc7, 0x10, 0x00, 0x00, 0x00}, "MovReg32Imm32", GPRegister32.EDI, 16),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x48, (byte) 0xb8, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x0, 0x00}, "MovReg64Imm64", GPRegister64.RAX, 0x0000000000000001L),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x48, (byte) 0xc7, (byte)0xc0, 0x01, 0x00, 0x00, 0x00}, "MovReg64Imm32", GPRegister64.RAX, 1),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x48, (byte) 0xb9, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x0, 0x00}, "MovReg64Imm64", GPRegister64.RCX, 0x0000000000000100L),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x48, (byte) 0xc7, (byte)0xc1, 0x02, 0x00, 0x00, 0x00}, "MovReg64Imm32", GPRegister64.RCX, 2),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x48, (byte) 0xba, 0x00, 0x00, 0x01, 0x00, 0x00, 0x00, 0x0, 0x00}, "MovReg64Imm64", GPRegister64.RDX, 0x0000000000010000L),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x48, (byte) 0xc7, (byte)0xc2, 0x03, 0x00, 0x00, 0x00}, "MovReg64Imm32", GPRegister64.RDX, 3),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x48, (byte) 0xbb, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x0, 0x00}, "MovReg64Imm64", GPRegister64.RBX, 0x0000000001000000L),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x48, (byte) 0xc7, (byte)0xc3, 0x04, 0x00, 0x00, 0x00}, "MovReg64Imm32", GPRegister64.RBX, 4),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x48, (byte) 0xbc, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x0, 0x00}, "MovReg64Imm64", GPRegister64.RSP, 0x0000000100000000L),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x48, (byte) 0xc7, (byte)0xc4, 0x05, 0x00, 0x00, 0x00}, "MovReg64Imm32", GPRegister64.RSP, 5),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x48, (byte) 0xbd, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x0, 0x00}, "MovReg64Imm64", GPRegister64.RBP, 0x0000010000000000L),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x48, (byte) 0xc7, (byte)0xc5, 0x06, 0x00, 0x00, 0x00}, "MovReg64Imm32", GPRegister64.RBP, 6),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x48, (byte) 0xbe, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x1, 0x00}, "MovReg64Imm64", GPRegister64.RSI, 0x0001000000000000L),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x48, (byte) 0xc7, (byte)0xc6, 0x07, 0x00, 0x00, 0x00}, "MovReg64Imm32", GPRegister64.RSI, 7),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x48, (byte) 0xbf, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x0, 0x01}, "MovReg64Imm64", GPRegister64.RDI, 0x0100000000000000L),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x48, (byte) 0xc7, (byte)0xc7, 0x08, 0x00, 0x00, 0x00}, "MovReg64Imm32", GPRegister64.RDI, 8));
    }

    static @NotNull Stream<Arguments> twoArgumentMemoryEncoding() {
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

    private static @NotNull Stream<Arguments> xchgAccumulatorRegEncodings() {
        return Stream.of(
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte) 0x91}, "XchgReg16Reg16", GPRegister16.AX, GPRegister16.CX),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte) 0x92}, "XchgReg16Reg16", GPRegister16.AX, GPRegister16.DX),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte) 0x93}, "XchgReg16Reg16", GPRegister16.AX, GPRegister16.BX),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte) 0x94}, "XchgReg16Reg16", GPRegister16.AX, GPRegister16.SP),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte) 0x95}, "XchgReg16Reg16", GPRegister16.AX, GPRegister16.BP),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte) 0x96}, "XchgReg16Reg16", GPRegister16.AX, GPRegister16.SI),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte) 0x97}, "XchgReg16Reg16", GPRegister16.AX, GPRegister16.DI),
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
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x66, (byte) 0x91}, "XchgReg16Reg16", GPRegister16.AX, GPRegister16.CX),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x66, (byte) 0x92}, "XchgReg16Reg16", GPRegister16.AX, GPRegister16.DX),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x66, (byte) 0x93}, "XchgReg16Reg16", GPRegister16.AX, GPRegister16.BX),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x66, (byte) 0x94}, "XchgReg16Reg16", GPRegister16.AX, GPRegister16.SP),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x66, (byte) 0x95}, "XchgReg16Reg16", GPRegister16.AX, GPRegister16.BP),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x66, (byte) 0x96}, "XchgReg16Reg16", GPRegister16.AX, GPRegister16.SI),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x66, (byte) 0x97}, "XchgReg16Reg16", GPRegister16.AX, GPRegister16.DI),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x48, (byte) 0x90}, "XchgReg64Reg64", GPRegister64.RAX, GPRegister64.RAX),
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

    static @NotNull Stream<Arguments> zeroOperandEncodings() {
        return Stream.of(
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte) 0x98}, "Cbw"),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x66, (byte) 0x98}, "Cwde"),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{(byte) 0x98}, "Cwde"),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{0x66, (byte) 0x98}, "Cbw"),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{(byte) 0x98}, "Cwde"),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x66, (byte) 0x98}, "Cbw"),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x40, (byte) 0x98}, "Cwde"),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x48, (byte) 0x98}, "Cdqe"),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte) 0x99}, "Cwd"),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x66, (byte) 0x99}, "Cdq"),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{(byte) 0x99}, "Cdq"),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{0x66, (byte) 0x99}, "Cwd"),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{(byte) 0x99}, "Cdq"),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x66, (byte) 0x99}, "Cwd"),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x40, (byte) 0x99}, "Cdq"),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x48, (byte) 0x99}, "Cqo"),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte) 0x90}, "Nop"),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{(byte) 0x90}, "Nop"),
                arguments(Instruction.Mode.ADDR32_DATA16, new Byte[]{(byte) 0x90}, "Nop"),
                arguments(Instruction.Mode.ADDR32_DATA32, new Byte[]{(byte) 0x90}, "Nop"),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{(byte) 0x90}, "Nop"),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{0x66, (byte) 0x90}, "Nop"),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{0x66, (byte) 0x90}, "Nop"),
                arguments(Instruction.Mode.ADDR32_DATA16, new Byte[]{0x66, (byte) 0x90}, "Nop"),
                arguments(Instruction.Mode.ADDR32_DATA32, new Byte[]{0x66, (byte) 0x90}, "Nop"),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x66, (byte) 0x90}, "Nop"),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte) 0xf2, (byte) 0x90}, "Nop"),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{(byte) 0xf2, (byte) 0x90}, "Nop"),
                arguments(Instruction.Mode.ADDR32_DATA16, new Byte[]{(byte) 0xf2, (byte) 0x90}, "Nop"),
                arguments(Instruction.Mode.ADDR32_DATA32, new Byte[]{(byte) 0xf2, (byte) 0x90}, "Nop"),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{(byte) 0xf2, (byte) 0x90}, "Nop"),
                arguments(Instruction.Mode.ADDR16_DATA16, new Byte[]{(byte) 0xf3, (byte) 0x90}, "Pause"),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{(byte) 0xf3, (byte) 0x90}, "Pause"),
                arguments(Instruction.Mode.ADDR32_DATA16, new Byte[]{(byte) 0xf3, (byte) 0x90}, "Pause"),
                arguments(Instruction.Mode.ADDR32_DATA32, new Byte[]{(byte) 0xf3, (byte) 0x90}, "Pause"),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{(byte) 0xf3, (byte) 0x90}, "Pause"),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x40, (byte) 0x90}, "Nop"),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x42, (byte) 0x90}, "Nop"),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x44, (byte) 0x90}, "Nop"),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x46, (byte) 0x90}, "Nop"),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{(byte) 0xd5, 0x0a}, "Aad"),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{(byte) 0xd4, 0x0a}, "Aam"),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{(byte) 0xd4, 0x00}, null),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{(byte) 0xd4, 0x0a}, null),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{(byte) 0xd5, 0x00}, null),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{(byte) 0xd5, 0x0a}, null),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{0x66, (byte) 0xd5, 0x0a}, "Aad"),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{0x66, (byte) 0xd4, 0x0a}, "Aam"),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x66, (byte) 0xd5, 0x00}, null),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x66, (byte) 0xd5, 0x0a}, null),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x66, (byte) 0xd4, 0x00}, null),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x66, (byte) 0xd4, 0x0a}, null),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{(byte) 0xf2, (byte) 0xd5, 0x0a}, "Aad"),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{(byte) 0xf2, (byte) 0xd4, 0x0a}, "Aam"),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{(byte) 0xf2, (byte) 0xd5, 0x00}, null),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{(byte) 0xf2, (byte) 0xd5, 0x0a}, null),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{(byte) 0xf2, (byte) 0xd4, 0x00}, null),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{(byte) 0xf2, (byte) 0xd4, 0x0a}, null),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{(byte) 0xf3, (byte) 0xd5, 0x0a}, "Aad"),
                arguments(Instruction.Mode.ADDR16_DATA32, new Byte[]{(byte) 0xf3, (byte) 0xd4, 0x0a}, "Aam"),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{(byte) 0xf3, (byte) 0xd5, 0x00}, null),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{(byte) 0xf3, (byte) 0xd5, 0x0a}, null),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{(byte) 0xf3, (byte) 0xd4, 0x00}, null),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{(byte) 0xf3, (byte) 0xd4, 0x0a}, null),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x40, 0x40}, null),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x40, 0x41}, null),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x40, 0x42}, null),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x40, 0x43}, null),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x40, 0x44}, null),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x40, 0x45}, null),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x40, 0x46}, null),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x40, 0x47}, null),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x40, 0x48}, null),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x40, 0x49}, null),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x40, 0x4a}, null),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x40, 0x4b}, null),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x40, 0x4c}, null),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x40, 0x4d}, null),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x40, 0x4e}, null),
                arguments(Instruction.Mode.ADDR64_DATA32, new Byte[]{0x40, 0x4f}, null));
    }

    @ParameterizedTest
    @MethodSource("decIncRegisterEncodings")
    @DisplayName("Dec/Inc registers parse test")
    void testParseRegisterInc(@NotNull Instruction.Mode mode, Byte[] opcodes, String name, Object register) {
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

            @Override
            public GPRegister64 when(@NotNull GPRegister64 argument) {
                return argument;
            }
        }));
    }

    @ParameterizedTest
    @MethodSource("decIncMemoryEncodings")
    @DisplayName("Dec/Inc memory parse test")
    void testParseMemoryInc(
            @NotNull Instruction.Mode mode, Byte[] opcodes, String name,
            String addr_name, SegmentRegister segment, Object base, Object index, Object scale, Object disp, short size)
            throws Exception {
        Optional<Instruction> instruction =
                Instruction.parse(mode, new RollbackIterator<>(Arrays.asList(opcodes).iterator()));
        assertEquals(
                "org.yajd.x86.cpu.Instruction$" + name,
                instruction.get().getClass().getName());
        var arguments = instruction.get().getArguments();
        var address = arguments[0].process(new Argument.Result<Object>() {
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
    @MethodSource("oneArgumentInstructions")
    @DisplayName("One argument instruction parse test")
    void testParseOneArgumentInstructions(
            @NotNull Instruction.Mode mode, Byte[] opcodes, String name, Class<?> argument_class, Object argument) throws Exception {
        Optional<Instruction> instruction =
                Instruction.parse(mode, new RollbackIterator<>(Arrays.asList(opcodes).iterator()));
        assertEquals(
                "org.yajd.x86.cpu.Instruction$" + name,
                instruction.get().getClass().getName());
        var arguments = instruction.get().getArguments();
        var rel_argument = arguments[0].process(new Argument.Result<Boolean>() {
            @Override
            public Boolean when(@NotNull Argument argument) {
                return false;
            }

            @Override
            public Boolean when(@NotNull Rel8 argument) {
                return true;
            }

            @Override
            public Boolean when(@NotNull Rel16 argument) {
                return true;
            }

            @Override
            public Boolean when(@NotNull Rel32 argument) {
                return true;
            }
        });
        var actual = instruction.get().getClass().getMethod("getArg0").invoke(instruction.get());
        assertEquals(argument_class, actual.getClass());
        if (rel_argument) {
            assertEquals(argument, actual.getClass().getMethod("getValue").invoke(actual));
        } else {
            assertEquals(argument, actual);
        }
    }

    @ParameterizedTest
    @MethodSource("twoArgumentImmediateEncoding")
    @DisplayName("Two argument immediate instructions test")
    void testParseTwoArgumentImmediateInstructions(
            @NotNull Instruction.Mode mode, Byte[] opcodes, String name, Object register, Object immediate) {
        Optional<Instruction> instruction =
                Instruction.parse(mode, new RollbackIterator<>(Arrays.asList(opcodes).iterator()));
        assertEquals(
                "org.yajd.x86.cpu.Instruction$" + name,
                instruction.get().getClass().getName());
        var arguments = instruction.get().getArguments();
        var GetRegister = new Argument.Result<Object>() {
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
        var GetImmediate = new Argument.Result<Object>() {
            @Override
            public Byte when(@NotNull Imm8 argument) {
                return argument.getValue();
            }

            @Override
            public Short when(@NotNull Imm16 argument) {
                return argument.getValue();
            }

            @Override
            public Integer when(@NotNull Imm32 argument) {
                return argument.getValue();
            }

            @Override
            public Long when(@NotNull Imm64 argument) {
                return argument.getValue();
            }
        };
        assertEquals(register, arguments[0].process(GetRegister));
        assertEquals(immediate, arguments[1].process(GetImmediate));
    }

    @ParameterizedTest
    @MethodSource("twoArgumentMemoryEncoding")
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

    @ParameterizedTest
    @MethodSource("zeroOperandEncodings")
    @DisplayName("Zero-Operand Instructions Test")
    void testParseZeroOperandInstructions(@NotNull Instruction.Mode mode, Byte[] opcodes, String name) {
        Optional<Instruction> instruction =
                Instruction.parse(mode, new RollbackIterator<>(Arrays.asList(opcodes).iterator()));
        if (name != null) {
            assertTrue(instruction.isPresent());
            assertEquals(
                    "org.yajd.x86.cpu.Instruction$" + name,
                    instruction.get().getClass().getName());
            var arguments = instruction.get().getArguments();
            assertEquals(0, arguments.length);
        } else {
            assertFalse(instruction.isPresent());
        }
    }

}
