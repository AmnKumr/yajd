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

package org.yajd;

import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.jetbrains.annotations.NotNull;
import org.yajd.x86.cpu.Argument;
import org.yajd.x86.cpu.GPAddress16;
import org.yajd.x86.cpu.GPRegister16;
import org.yajd.x86.cpu.GPRegister32;
import org.yajd.x86.cpu.GPRegister64;
import org.yajd.x86.cpu.GPRegister8;
import org.yajd.x86.cpu.Imm16;
import org.yajd.x86.cpu.Imm32;
import org.yajd.x86.cpu.Imm64;
import org.yajd.x86.cpu.Imm8;
import org.yajd.x86.cpu.Instruction;
import org.yajd.x86.cpu.InstructionIterator;
import org.yajd.x86.cpu.Rel16;
import org.yajd.x86.cpu.Rel32;
import org.yajd.x86.cpu.Rel8;
import org.yajd.x86.cpu.SegmentRegister;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Optional;
import java.util.Queue;

public class SimpleDisassembler {
    public static void disassembleFile(long start_address, Instruction.Mode mode, String file_name) throws IOException {
        byte[] data = Files.readAllBytes(Paths.get(file_name));
        Instruction[] instructions = new Instruction[data.length];
        Queue<Integer> target_positions = new ArrayDeque<>();
        target_positions.add(0);
        while (!target_positions.isEmpty()) {
            int position = target_positions.remove();
            if (position < 0 || position >= data.length || instructions[position] != null) {
                continue;
            }
            var iterator = new ByteIterator(data, position);
            var instruction_iterator = new InstructionIterator(mode, iterator);
            decoding:
            while (instruction_iterator.hasNext()) {
                var instruction = instruction_iterator.next();
                for (int i = 0; i < instruction.getBytes().length; ++i) {
                    if (instructions[position + i] != null) {
                        break decoding;
                    }
                }
                for (int i = 0; i < instruction.getBytes().length; ++i) {
                    instructions[position + i] = instruction;
                }
                position += instruction.getBytes().length;
                int instruction_end = position; // Need to copy it to access from inner class.
                Optional<Integer> new_position = instruction.process(new Instruction.Result<Optional<Integer>>() {
                    @Override
                    public Optional<Integer> when(Instruction instruction) {
                        return Optional.empty();
                    }

                    @Override
                    public Optional<Integer> when(Instruction.Jcc instruction) {
                        Argument[] arguments = instruction.getArguments();
                        if (arguments.length != 1) {
                            return Optional.empty();
                        }
                        return arguments[0].process(new Argument.Result<Optional<Integer>>() {
                            @Override
                            public Optional<Integer> when(@NotNull Argument argument) {
                                return Optional.empty();
                            }

                            @Override
                            public Optional<Integer> when(@NotNull Rel8 argument) {
                                return Optional.of((instruction_end + argument.getValue()) % 0x10000);
                            }

                            @Override
                            public Optional<Integer> when(@NotNull Rel16 argument) {
                                return Optional.of((instruction_end + argument.getValue()) % 0x10000);
                            }

                            @Override
                            public Optional<Integer> when(@NotNull Rel32 argument) {
                                return Optional.of(instruction_end + argument.getValue());
                            }
                        });
                    }

                    @Override
                    public Optional<Integer> when(Instruction.CallRel16 instruction) {
                        return Optional.of((instruction_end + instruction.getArg0().getValue()) % 0x10000);
                    }

                    @Override
                    public Optional<Integer> when(Instruction.CallRel32 instruction) {
                        return Optional.of(instruction_end + instruction.getArg0().getValue());
                    }
                });
                if (new_position.isPresent()) {
                    target_positions.add(new_position.get());
                }
                if (instruction.process(new Instruction.Result<Boolean>() {
                    @Override
                    public Boolean when(Instruction instruction) {
                        return false;
                    }

                    @Override
                    public Boolean when(Instruction.Jcc instruction) {
                        return instruction.getCondition().isEmpty();
                    }

                    @Override
                    public Boolean when(Instruction.Ret instruction) {
                        return true;
                    }

                    @Override
                    public Boolean when(Instruction.RetImm16 instruction) {
                        return true;
                    }

                    @Override
                    public Boolean when(Instruction.RetFar instruction) {
                        return true;
                    }

                    @Override
                    public Boolean when(Instruction.RetFarImm16 instruction) {
                        return true;
                    }

                    @Override
                    public Boolean when(Instruction.Iret instruction) {
                        return true;
                    }

                    @Override
                    public Boolean when(Instruction.Iretd instruction) {
                        return true;
                    }

                    @Override
                    public Boolean when(Instruction.Iretq instruction) {
                        return true;
                    }
                })) {
                    break;
                }
            }
        }

        for (int position = 0; position < data.length; ) {
            if (instructions[position] != null) {
                Instruction instruction = instructions[position];
                System.out.printf("%s\n", InstructionToString(instructions[position], position + start_address));
                position += instruction.getBytes().length;
            } else {
                int ascii_chunk_len = 1;
                while (ascii_chunk_len < 8) {
                    if (position + ascii_chunk_len >= data.length || instructions[position + ascii_chunk_len] != null) {
                        break;
                    }
                    ++ascii_chunk_len;
                }
                System.out.printf("%4x  ", position + start_address);
                for (int i = 0; i < ascii_chunk_len; ++i) {
                    System.out.printf("%02x ", data[position + i]);
                }
                System.out.print("   ".repeat(8 - ascii_chunk_len));
                System.out.print("  \"");
                for (int i = 0; i < ascii_chunk_len; ++i) {
                    byte c = data[position + i];
                    switch (c) {
                        case '\'':
                            System.out.print("\\'");
                            break;
                        case '"':
                            System.out.print("\\\"");
                            break;
                        case '\b':
                            System.out.print("\\b");
                            break;
                        case '\f':
                            System.out.print("\\f");
                            break;
                        case '\n':
                            System.out.print("\\n");
                            break;
                        case '\r':
                            System.out.print("\\r");
                            break;
                        case '\t':
                            System.out.print("\\t");
                            break;
                        default:
                            if (' ' <= c && c <= '~') {
                                System.out.print((char)c);
                            } else {
                                System.out.printf("\\%03o", c & 0xff);
                            }
                        }
                }
                System.out.println("\"");
                position += ascii_chunk_len;
            }
        }
    }

    public static String InstructionToString(@NotNull Instruction instruction, long position) {
        StringBuilder result = new StringBuilder(String.format("%4x  ", position));
        int bytes_left = 8;
        for (var current_byte : instruction.getBytes()) {
            result.append(String.format("%02x ", current_byte));
            bytes_left--;
        }
        result.append("   ".repeat(Math.max(0, bytes_left + 1)));
        Argument[] arguments = instruction.getArguments();
        if (arguments.length > 0) {
            result.append(String.format("%-8s", instruction.getName()));
            String[] arguments_text = new String[arguments.length];
            for (int i = 0; i < arguments.length; ++i) {
                arguments_text[i] = arguments[i].process(new Argument.Result<String>() {
                    @Override
                    public String when(@NotNull Argument argument) {
                        return argument.toString();
                    }

                    @Override
                    public String when(@NotNull Imm8 imm8) {
                        return String.format("0x%x", imm8.getValue());
                    }

                    @Override
                    public String when(@NotNull Imm16 imm16) {
                        return String.format("0x%x", imm16.getValue());
                    }

                    @Override
                    public String when(@NotNull Imm32 imm32) {
                        return String.format("0x%x", imm32.getValue());
                    }

                    @Override
                    public String when(@NotNull Imm64 imm64) {
                        return String.format("0x%x", imm64.getValue());
                    }

                    @Override
                    public String when(@NotNull Rel8 rel8) {
                        return String.format("0x%x", (position + instruction.getBytes().length + rel8.getValue()) % 0x10000);
                    }

                    @Override
                    public String when(@NotNull Rel16 rel16) {
                        return String.format("0x%x", (position + instruction.getBytes().length + rel16.getValue()) % 0x10000);
                    }

                    @Override
                    public String when(@NotNull Rel32 rel32) {
                        return String.format("0x%x", (position + instruction.getBytes().length + rel32.getValue()) % 0x100000000L);
                    }

                    @Override
                    public String when(@NotNull GPRegister8 register) {
                        return register.getName();
                    }

                    @Override
                    public String when(@NotNull GPRegister16 register) {
                        return register.getName();
                    }

                    @Override
                    public String when(@NotNull GPRegister32 register) {
                        return register.getName();
                    }

                    @Override
                    public String when(@NotNull GPRegister64 register) {
                        return register.getName();
                    }

                    @Override
                    public String when(@NotNull SegmentRegister register) {
                        return register.getName();
                    }

                    @Override
                    public String when(@NotNull GPAddress16 address16) {
                        StringBuilder result = new StringBuilder();
                        if (address16.getSize() == 8) {
                            result.append("BYTE PTR ");
                        } else if (address16.getSize() == 16) {
                            result.append("WORD PTR ");
                        } else {
                            result.append("PTR ");
                        }
                        Optional<SegmentRegister> segment = address16.getSegment();
                        if (segment.isPresent()) {
                            result.append(segment.get().getName());
                            result.append(":");
                        }
                        result.append("[");
                        Optional<GPRegister16> base = address16.getBase();
                        Optional<GPRegister16> index = address16.getIndex();
                        short disp = address16.getDisp();
                        ArrayList<String> components = new ArrayList<>();
                        if (base.isPresent()) {
                            components.add(base.get().getName());
                        }
                        if (index.isPresent()) {
                            components.add(index.get().getName());
                        }
                        if (disp != 0) {
                            components.add(String.format("0x%x", disp));
                        }
                        result.append(String.join(" + ", components));
                        result.append("]");
                        return result.toString();
                    }
                });
            }
            result.append(String.join(", ", arguments_text));
        } else {
            result.append(instruction.getName());
        }
        return result.toString();
    }

    public static void help(Options options) {
        var formatter = new HelpFormatter();
        formatter.printHelp("SimpleDecoder [OPTION]... [FILE]...", options);
    }

    public static void main(String[] args) {
        var options = new Options();
        options.addOption(
                Option
                        .builder("h")
                        .longOpt("help")
                        .desc("print this message")
                        .build());
        options.addOption(
                Option
                        .builder("M")
                        .longOpt("mode")
                        .desc("disassembler mode: i8086, i386, or x86-64")
                        .hasArg()
                        .argName("8086_MODE")
                        .build());
        options.addOption(
                Option
                        .builder("S")
                        .longOpt("start-address")
                        .desc("start-address: default is 0x100 (as for COM file)")
                        .hasArg()
                        .argName("START_ADDRESS")
                        .build());
        CommandLineParser parser = new DefaultParser();
        try {
            var command_line = parser.parse(options, args);

            if (command_line.hasOption("help")) {
                help(options);
                return;
            }

            var mode = Instruction.Mode.ADDR16_DATA16;
            var mode_option = command_line.getOptionValue("mode");
            if (mode_option != null) {
                switch (mode_option) {
                    case "i8086":
                        mode = Instruction.Mode.ADDR16_DATA16;
                        break;
                    case "i386":
                        mode = Instruction.Mode.ADDR32_DATA32;
                        break;
                    case "x86-64":
                        mode = Instruction.Mode.ADDR64_DATA32;
                        break;
                    default:
                        throw new ParseException("Unrecognized x86 mode: " + mode_option);
                }
            }

            long start_address = 0x100;
            var start_option = command_line.getOptionValue("start-address");
            if (start_option != null) {
                if (start_option.startsWith("0x")) {
                    start_address = Long.parseLong(start_option.substring(2), 16);
                } else if (start_option.startsWith("0")) {
                    start_address = Long.parseLong(start_option.substring(1), 8);
                } else {
                    start_address = Long.parseLong(start_option);
                }
            }

            var files = command_line.getArgs();
            if (files.length == 0) {
                help(options);
            } else {
                for (var file_name : files) {
                    disassembleFile(start_address, mode, file_name);
                }
            }
        } catch (ParseException | IOException exp) {
            System.err.println(exp.getMessage());

            help(options);
        }
    }

    private static class ByteIterator implements Iterator<Byte> {
        private final byte[] data;
        int position;

        public ByteIterator(byte[] data, int position) {
            this.data = data;
            this.position = position;
        }

        @Override
        public boolean hasNext() {
            return position < data.length;
        }

        @Override
        public Byte next() {
            return data[position++];
        }
    }
}
