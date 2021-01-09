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
import org.yajd.x86.cpu.Instruction;
import org.yajd.x86.cpu.InstructionIterator;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class SimpleDisassembler {
    public static void disassembleFile(long start_address, Instruction.Mode mode, String file_name) throws IOException {
        var file = new FileInputStream(file_name);
        var iterator = new Iterator<Byte>() {
            int next_byte = -1;

            @Override
            public boolean hasNext() {
                if (next_byte != -1) {
                    return true;
                }
                try {
                    next_byte = file.read();
                } catch (IOException e) {
                    throw new NoSuchElementException();
                }
                return next_byte != -1;
            }

            @Override
            public Byte next() {
                if (next_byte != -1) {
                    var saved_byte = next_byte;
                    next_byte = -1;
                    return (byte) saved_byte;
                }
                try {
                    var new_byte = file.read();
                    if (new_byte == -1) {
                        throw new NoSuchElementException();
                    }
                    return (byte) new_byte;
                } catch (IOException e) {
                    throw new NoSuchElementException();
                }
            }
        };
        long position = start_address;
        var instruction_iterator = new InstructionIterator(mode, iterator);
        while (instruction_iterator.hasNext()) {
            var instruction = instruction_iterator.next();
            int bytes_left = 8;
            System.out.printf("%4x   ", position);
            for (var current_byte : instruction.getBytes()) {
                System.out.printf("%02x ", current_byte);
                bytes_left--;
                position++;
            }
            for (int byte_count = 0; byte_count <= bytes_left; byte_count++) {
                System.out.print("   ");
            }
            System.out.printf("%s\n", instruction.getName());
        }
        file.close();
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
}
