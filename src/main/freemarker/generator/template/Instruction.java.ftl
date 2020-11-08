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

<#function element_in_list element list>
    <#if list?size == 0>
        <#return false>
    <#elseif element == list[0]>
        <#return true>
    <#else>
        <#return element_in_list(element, list[1..])>
    </#if>
</#function>
<#function remove_adjacent_duplicates list>
    <#if list?size < 2>
        <#return list>
    <#elseif list[0] == list[1]>
        <#return remove_adjacent_duplicates(list[1..])>
    <#else>
        <#return list[0..0] + remove_adjacent_duplicates(list[1..])>
    </#if>
</#function>
<#function sort_and_remove_duplicates list>
    <#return remove_adjacent_duplicates(list?sort)>
</#function>
<#function same_argument_lists x y>
    <#if x?size == 0>
        <#if y?size == 0>
            <#return true>
        <#else>
            <#return false>
        </#if>
    <#elseif y?size == 0>
        <#return false>
    <#elseif x[0]?keep_after(":") == y[0]?keep_after(":")>
        <#return same_argument_lists(x[1..], y[1..])>
    <#else>
        <#return false>
    </#if>
</#function>
<#function replace_arguments arguments from to>
    <#if arguments?size == 0>
        <#return []>
    <#elseif from?size == 1>
        <#if arguments[0]?keep_after(":") == from[0]>
            <#return [arguments[0]?keep_before(":") + ":" + to[0]] +
                      replace_arguments(arguments[1..], from, to)>
        <#else>
            <#return arguments[0..0] + replace_arguments(arguments[1..], from, to)>
        </#if>
    <#else>
        <#return replace_arguments(
            replace_arguments(arguments, from[0..0], to[0..0]), from[1..], to[1..])>
    </#if>
</#function>
<#function absolute_address_operand arguments>
    <#list arguments as argument>
        <#if element_in_list(argument?keep_after(":"),
                             ["AbsMemory8", "AbsMemory16", "AbsMemory32", "x64AbsMemory"])>
            <#return true>
        </#if>
    </#list>
    <#return false>
</#function>
<#function address_operand arguments>
    <#list arguments as argument>
        <#if element_in_list(argument?keep_after(":"),
                             ["Memory0", "Memory8", "Memory16", "Memory32", "x64Memory"])>
            <#return true>
        </#if>
    </#list>
    <#return false>
</#function>
<#function address64_operand arguments>
    <#list arguments as argument>
        <#if argument?keep_after(":") == "x64AbsMemory" || argument?keep_after(":") == "x64Memory">
            <#return true>
        </#if>
    </#list>
    <#return false>
</#function>
<#function expand_address_arguments instructions>
    <#if instructions?size == 0>
        <#return []>
    <#else>
        <#if address_operand(instructions[0].arguments)>
            <#local result =
                [instructions[0] + {"arguments": replace_arguments(
                    instructions[0].arguments,
                    ["Memory0", "Memory8", "Memory16", "Memory32", "x64Memory"],
                    ["RIPAddress64/0", "RIPAddress64/8", "RIPAddress64/16", "RIPAddress64/32", "RIPAddress64/64"])}] +
                [instructions[0] + {"arguments": replace_arguments(
                    instructions[0].arguments,
                    ["Memory0", "Memory8", "Memory16", "Memory32", "x64Memory"],
                    ["GPAddress64/0", "GPAddress64/8", "GPAddress64/16", "GPAddress64/32", "GPAddress64/64"])}] +
                [instructions[0] + {"arguments": replace_arguments(
                    instructions[0].arguments,
                    ["Memory0", "Memory8", "Memory16", "Memory32", "x64Memory"],
                    ["EIPAddress32/0", "EIPAddress32/8", "EIPAddress32/16", "EIPAddress32/32", "EIPAddress32/64"])}] +
                [instructions[0] + {"arguments": replace_arguments(
                    instructions[0].arguments,
                    ["Memory0", "Memory8", "Memory16", "Memory32", "x64Memory"],
                    ["GPAddress32/0", "GPAddress32/8", "GPAddress32/16", "GPAddress32/32", "GPAddress32/64"])}]>
            <#if !address64_operand(instructions[0].arguments)>
                <#local result = result +
                    [instructions[0] + {"arguments": replace_arguments(
                        instructions[0].arguments,
                        ["Memory0", "Memory8", "Memory16", "Memory32"],
                        ["GPAddress16/0", "GPAddress16/8", "GPAddress16/16", "GPAddress16/32"])}]>
            </#if>
            <#return result + expand_native_arguments(instructions[1..])>
        <#elseif absolute_address_operand(instructions[0].arguments)>
            <#local result =
                [instructions[0] + {"arguments": replace_arguments(
                    instructions[0].arguments,
                    ["AbsMemory8", "AbsMemory16", "AbsMemory32", "x64AbsMemory"],
                    ["AbsoluteAddress64/8", "AbsoluteAddress64/16", "AbsoluteAddress64/32", "AbsoluteAddress64/64"])}] +
                [instructions[0] + {"arguments": replace_arguments(
                    instructions[0].arguments,
                    ["AbsMemory8", "AbsMemory16", "AbsMemory32", "x64AbsMemory"],
                    ["GPAddress32/8", "GPAddress32/16", "GPAddress32/32", "GPAddress32/64"])}]>
            <#if !address64_operand(instructions[0].arguments)>
                <#local result = result +
                    [instructions[0] + {"arguments": replace_arguments(
                    instructions[0].arguments,
                    ["AbsMemory8", "AbsMemory16", "AbsMemory32"],
                    ["GPAddress16/8", "GPAddress16/16", "GPAddress16/32"])}]>
            </#if>
            <#return result + expand_native_arguments(instructions[1..])>
        <#else>
            <#return instructions[0..0] + expand_native_arguments(instructions[1..])>
        </#if>
    </#if>
</#function>
<#function native_sized_operand arguments>
    <#list arguments as argument>
        <#if element_in_list(argument?keep_after(":"), ["GPRegisterNative", "MemoryNative"])>
            <#return true>
        </#if>
    </#list>
    <#return false>
</#function>
<#function split_opcodes keys names opcode_16_bit_prefix opcode_32_bit_prefix opcode_64_bit_prefix>
    <#if keys?size == 0>
        <#return {}>
    <#else>
        <#if opcode_16_bit_prefix == "SKIP">
            <#local opcode16 = "">
        <#else>
            <#local opcode16 = opcode_16_bit_prefix + names[keys[0]]>
        </#if>
        <#if opcode_32_bit_prefix == "SKIP">
            <#local opcode32 = "">
        <#else>
            <#local opcode32 = opcode_32_bit_prefix + names[keys[0]]>
        </#if>
        <#if opcode_64_bit_prefix == "SKIP" ||
             names[keys[0]] == "0x40" ||
             names[keys[0]] == "0x48">
            <#local opcode64 = "">
        <#else>
            <#if names[keys[0]]?starts_with("0xf2? 0xf3? ")>
                <#local opcode64 = "0x66? 0xf2? 0xf3? " + opcode_64_bit_prefix + names[keys[0]][12..]>
            <#elseif names[keys[0]]?starts_with("0xf2 ")>
                <#local opcode64 = "0x66? 0xf2 " + opcode_64_bit_prefix + names[keys[0]][5..]>
            <#elseif names[keys[0]]?starts_with("0xf3 ")>
                <#local opcode64 = "0x66? 0xf3 " + opcode_64_bit_prefix + names[keys[0]][5..]>
            <#else>
                <#local opcode64 = "0x66? " + opcode_64_bit_prefix + names[keys[0]]>
            </#if>
        </#if>
        <#return
            {keys[0]: opcode16 + "|" + opcode32 + "|" + opcode64} +
            split_opcodes(keys[1..],
                          names,
                          opcode_16_bit_prefix,
                          opcode_32_bit_prefix,
                          opcode_64_bit_prefix)>
    </#if>
</#function>
<#function expand_native_arguments instructions>
    <#if instructions?size == 0>
        <#return []>
    <#else>
        <#if native_sized_operand(instructions[0].arguments)>

            <#return
                expand_address_arguments(
                    [instructions[0] + {
                        "arguments": replace_arguments(
                            instructions[0].arguments,
                            ["GPRegisterNative", "AbsMemoryNative", "MemoryNative", "ImmNative64", "ImmNative"],
                            ["GPRegister16", "AbsMemory16", "Memory16", "Imm16", "Imm16"]),
                        "names": instructions[0].names + split_opcodes(
                            instructions[0].names?keys,
                            instructions[0].names,
                            "", "0x66 ", "SKIP")},
                    instructions[0] + {
                        "arguments": replace_arguments(
                            instructions[0].arguments,
                            ["GPRegisterNative", "AbsMemoryNative", "MemoryNative", "ImmNative64", "ImmNative"],
                            ["GPRegister32", "AbsMemory32", "Memory32", "Imm32", "Imm32"]),
                        "names": instructions[0].names + split_opcodes(
                            instructions[0].names?keys,
                            instructions[0].names,
                            "0x66 ", "", "SKIP")},
                    instructions[0] + {
                        "arguments": replace_arguments(
                            instructions[0].arguments,
                            ["GPRegisterNative", "AbsMemoryNative", "MemoryNative", "ImmNative64", "ImmNative"],
                            ["GPRegister64", "x64AbsMemory", "x64Memory", "Imm64", "Imm32"]),
                        "names": instructions[0].names + split_opcodes(
                            instructions[0].names?keys,
                            instructions[0].names,
                            "SKIP", "SKIP", "rexw ")}]) +
                expand_native_arguments(instructions[1..])>
        <#else>
            <#return expand_address_arguments([instructions[0]]) +
                     expand_native_arguments(instructions[1..])>
        </#if>
    </#if>
</#function>
<#function memory_register_operand arguments>
    <#list arguments as argument>
        <#if element_in_list(argument?keep_after(":"),
                             ["GPRegister8/Memory8", "GPRegisterNative/MemoryNative"])>
            <#return true>
        </#if>
    </#list>
    <#return false>
</#function>
<#function regmem_opcodes keys names reg_mem_marker>
    <#if keys?size == 0>
        <#return {}>
    <#else>
        <#if names[keys[0]]?contains("/")>
            <#local opcode =
                names[keys[0]]?keep_before_last("/") +
                reg_mem_marker +
                " /" +
                names[keys[0]]?keep_after_last("/")>
        <#elseif names[keys[0]]?contains("+")>
            <#local opcode =
            names[keys[0]]?keep_before_last("+") +
            reg_mem_marker +
            " +" +
            names[keys[0]]?keep_after_last("+")>
        <#else>
            <#local opcode = names[keys[0]] + " " + reg_mem_marker>
        </#if>
        <#return {keys[0]: opcode} + regmem_opcodes(keys[1..], names, reg_mem_marker)>
    </#if>
</#function>
<#function expand_memory_register_arguments instructions>
    <#if instructions?size == 0>
        <#return []>
    <#else>
        <#if memory_register_operand(instructions[0].arguments)>
            <#return
                expand_native_arguments(
                    [instructions[0] + {
                        "arguments": replace_arguments(
                            instructions[0].arguments,
                            ["GPRegister8/Memory8", "GPRegisterNative/MemoryNative"],
                            ["GPRegister8", "GPRegisterNative"]),
                        "names": instructions[0].names + regmem_opcodes(
                            instructions[0].names?keys,
                            instructions[0].names, "/r")}] +
                    [instructions[0] + {
                        "arguments": replace_arguments(
                            instructions[0].arguments,
                            ["GPRegister8/Memory8", "GPRegisterNative/MemoryNative"],
                            ["Memory8", "MemoryNative"]),
                        "names": instructions[0].names + regmem_opcodes(
                        instructions[0].names?keys,
                        instructions[0].names, "/m")}]) +
                expand_memory_register_arguments(instructions[1..])>
        <#else>
            <#return expand_native_arguments(instructions[0..0]) +
                     expand_memory_register_arguments(instructions[1..])>
        </#if>
    </#if>
</#function>
<#function filter_out_instructions existing_names new_names>
    <#if existing_names?size == 0>
        <#return new_names>
    <#elseif new_names?size == 0>
        <#return []>
    <#elseif existing_names?size == 1>
        <#if existing_names[0]?filter(x -> x == new_names[0])?size == 0>
            <#return new_names[0..0] + filter_out_instructions(existing_names, new_names[1..])>
        <#else>
            <#return filter_out_instructions(existing_names, new_names[1..])>
        </#if>
    <#else>
        <#return filter_out_instructions(existing_names[0..0],
                     filter_out_instructions(existing_names[1..], new_names))>
    </#if>
</#function>
<#function merge_instruction merged_instructions original_instructions>
    <#if original_instructions?size == 0>
        <#return merged_instructions>
    <#elseif merged_instructions?filter(
                 x -> same_argument_lists(x.arguments, original_instructions[0].arguments))?size == 0>
        <#return merge_instruction(merged_instructions + [
            original_instructions[0] + {
                "names": sort_and_remove_duplicates(original_instructions[0].names?keys?map(x -> x?keep_before("/")))}],
            original_instructions[1..])>
    <#else>
        <#local filtered_names = filter_out_instructions(merged_instructions?filter(
                    x -> same_argument_lists(x.arguments, original_instructions[0].arguments))?map(
                    x -> x.names), original_instructions[0].names?keys?map(x -> x?keep_before("/")))>
        <#if filtered_names?size != 0>
            <#return merge_instruction(merged_instructions + [
                 original_instructions[0] + {"names": sort_and_remove_duplicates(filtered_names)}],
                 original_instructions[1..])>
        <#else>
            <#return merge_instruction(merged_instructions, original_instructions[1..])>
        </#if>
    </#if>
</#function>
<#function expand_and_merge_instructions instructions>
    <#return merge_instruction([], expand_memory_register_arguments(instructions))>
</#function>
<#function classname name arguments>
    <#local result>${
        name?keep_before("/")?capitalize?replace(" ", "")}<#list arguments as argument>${
        argument_to_class_name[argument?keep_after(":")]
    }</#list></#local>
    <#return result>
</#function>
<#function operand_in_opcode arguments>
    <#if arguments?size == 0>
        <#return false>
    <#elseif arguments[0]?starts_with("Op:")>
        <#return true>
    <#else>
        <#return operand_in_opcode(arguments[1..])>
    </#if>
</#function>
<#function opcode_optional_prefixes_expand opcode>
    <#if opcode?contains("?")>
        <#return opcode_optional_prefixes_expand(
                     opcode?keep_before("?")?ensure_starts_with(" ")?keep_before_last(" ") + opcode?keep_after("?")) +
                 opcode_optional_prefixes_expand(
                     opcode?keep_before("?") + opcode?keep_after("?"))>
    <#else>
        <#return [opcode?trim]>
    </#if>
</#function>
<#function generate_opcodes_map opcodes_variant skip_suffix expanded_instruction_classes_list>
    Note: freemarker documentation says quite explicitly: “Note that hash concatenation is not to
          be used for many repeated concatenations, like for adding items to a hash inside a loop”.
    That′s why we first are making a string and then using eval to make a map.
    <#assign opcodes_map_text>
        <#list expanded_instruction_classes_list as instruction_class>
            <#list instruction_class.names as instruction_name, instruction_opcode>
                <#if instruction_opcode?contains("|")>
                    <#if instruction_opcode?split("|")[opcodes_variant] != "">
                        <#local opcode = instruction_opcode?split("|")[opcodes_variant]>
                    <#else>
                        <#local opcode = "">
                    </#if>
                <#else>
                    <#local opcode = instruction_opcode>
                </#if>
                <#if !instruction_name?ends_with("/"+skip_suffix) && opcode != "">
                    <#list opcode_optional_prefixes_expand(opcode) as opcode>
                        <#if operand_in_opcode(instruction_class.arguments)>
                            <#if opcode?ends_with("0")>
                                <#local suffixes = ["0", "1", "2", "3", "4", "5", "6", "7"]>
                            <#else>
                                <#local suffixes = ["8", "9", "a", "b", "c", "d", "e", "f"]>
                            </#if>
                            <#list suffixes as suffix>
                                "${opcode[0..<(opcode?length-1)]}${suffix}" : {
                                    "name" : "${classname(instruction_name, instruction_class.arguments)}",
                                <#if instruction_class.arguments?size == 0>
                                    "arguments" : []
                                <#else>
                                    "arguments" : ["${instruction_class.arguments?join("\", \"")}"]
                                </#if>
                                },
                            </#list>
                        <#else>
                            "${opcode}" : {
                                "name" : "${classname(instruction_name, instruction_class.arguments)}",
                            <#if instruction_class.arguments?size == 0>
                                "arguments" : []
                            <#else>
                                "arguments" : ["${instruction_class.arguments?join("\", \"")}"]
                            </#if>
                            },
                        </#if>
                    </#list>
                </#if>
            </#list>
        </#list>
    </#assign>
    Evaluate Nop last to make sure it would be choosen over “xchg %ax, %ax”
    <#assign opcodes_map_text = (
            "{${opcodes_map_text}" +
            " \"0x90\": { \"name\": \"Nop\", \"arguments\": [] }," +
            " \"0x66 0x90\": { \"name\": \"Nop\", \"arguments\": [] }," +
            " \"0xf2 0x90\": { \"name\": \"Nop\", \"arguments\": [] }," +
            " \"0x66 0xf2 0x90\": { \"name\": \"Nop\", \"arguments\": [] }," +
            " \"0xf3 0x90\": { \"name\": \"Pause\", \"arguments\": [] }," +
            " \"0x66 0xf3 0x90\": { \"name\": \"Pause\", \"arguments\": [] }}"
        )?eval>
    <#return opcodes_map_text>
</#function>
<#function prefix_opcode_map opcode_map>
    <#assign prefix_map>
        <#list opcode_map?keys as opcode_key>
            <#list 0..<opcode_key?split(" ")?size-1 as opcode_piece>
                "${opcode_key?split(" ")[0..opcode_piece]?join(" ")}" : true,
            </#list>
        </#list>
    </#assign>
    <#return "{${prefix_map}\"0x90\": true}"?eval>
</#function>
<#-- Expand instruction:
       GPRegisterNative becomes GPRegisterNative16, 32, or 64 depending on prefix -->
<#assign expanded_instruction_classes_list = expand_memory_register_arguments(instructions)>
<#-- Merge expanded insructions:
       Some instructions have more than one encoding, but map to a single Instruction class -->
<#assign merged_instruction_classes_list = merge_instruction([], expanded_instruction_classes_list)>
<#-- Opcode maps. Certain instructions use 0x66 to specify “opcode extensions” (e.g. SSE),
     certain instructions use it to specify instruction width — even SSE instructions, e.g. CRC32.

     The simplest way to handle that is not have separate opcode maps for 16bit data width,
     32bit data width and ADDR64_DATA32 (which also have 32bit data width by default, but
     supports 64bit data width with REX.W prefix bit). -->
<#assign opcode_map_16_x32 = generate_opcodes_map(0, "x64", expanded_instruction_classes_list)>
<#assign opcode_map_16_x64 = generate_opcodes_map(0, "x32", expanded_instruction_classes_list)>
<#assign opcode_map_32_x32 = generate_opcodes_map(1, "x64", expanded_instruction_classes_list)>
<#assign opcode_map_32_x64 = generate_opcodes_map(1, "x32", expanded_instruction_classes_list)>
<#assign opcode_map_64 = generate_opcodes_map(2, "x32", expanded_instruction_classes_list)>
<#-- We need to know if it makes sense to continue to parse instruction after certain “prefix”
     part. For example PF2IW have opcode “0x0f 0x0f 0x1c” where 0x1c part comes in place of
     immediate. We need to ensure parsing would stop at “0x0f 0x0f”.

     But in some cases prefix is also a valid instruction by itself. E.g. CMPPS have opcode
     “0x0f 0xc2” yet CMPEQPS have opcode “0x0f 0xc2 0x00” and we need to handle both of these. -->
<#assign prefix_opcode_map_16_x32 = prefix_opcode_map(opcode_map_16_x32)>
<#assign prefix_opcode_map_16_x64 = prefix_opcode_map(opcode_map_16_x64)>
<#assign prefix_opcode_map_32_x32 = prefix_opcode_map(opcode_map_32_x32)>
<#assign prefix_opcode_map_32_x64 = prefix_opcode_map(opcode_map_32_x64)>
<#assign prefix_opcode_map_64 = prefix_opcode_map(opcode_map_64)>
<#-- Opcodes are typically one-byte on x86. To process these we need a list of all possible
     byte values.  But freemarker doesn't make it easy to create one. -->
<#assign byte_vaues = [
    "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "0a", "0b", "0c", "0d", "0e", "0f",
    "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "1a", "1b", "1c", "1d", "1e", "1f",
    "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "2a", "2b", "2c", "2d", "2e", "2f",
    "30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "3a", "3b", "3c", "3d", "3e", "3f",
    "40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "4a", "4b", "4c", "4d", "4e", "4f",
    "50", "51", "52", "53", "54", "55", "56", "57", "58", "59", "5a", "5b", "5c", "5d", "5e", "5f",
    "60", "61", "62", "63", "64", "65", "66", "67", "68", "69", "6a", "6b", "6c", "6d", "6e", "6f",
    "70", "71", "72", "73", "74", "75", "76", "77", "78", "79", "7a", "7b", "7c", "7d", "7e", "7f",
    "80", "81", "82", "83", "84", "85", "86", "87", "88", "89", "8a", "8b", "8c", "8d", "8e", "8f",
    "90", "91", "92", "93", "94", "95", "96", "97", "98", "99", "9a", "9b", "9c", "9d", "9e", "9f",
    "a0", "a1", "a2", "a3", "a4", "a5", "a6", "a7", "a8", "a9", "aa", "ab", "ac", "ad", "ae", "af",
    "b0", "b1", "b2", "b3", "b4", "b5", "b6", "b7", "b8", "b9", "ba", "bb", "bc", "bd", "be", "bf",
    "c0", "c1", "c2", "c3", "c4", "c5", "c6", "c7", "c8", "c9", "ca", "cb", "cc", "cd", "ce", "cf",
    "d0", "d1", "d2", "d3", "d4", "d5", "d6", "d7", "d8", "d9", "da", "db", "dc", "dd", "de", "df",
    "e0", "e1", "e2", "e3", "e4", "e5", "e6", "e7", "e8", "e9", "ea", "eb", "ec", "ed", "ee", "ef",
    "f0", "f1", "f2", "f3", "f4", "f5", "f6", "f7", "f8", "f9", "fa", "fb", "fc", "fd", "fe", "ff"
]>
<#macro value_to_byte value>
<#-- Note: freemarker doesn't have a lexicogrphical comparison for strings… use hash instead… -->${{
    '0':"0x",
    '1':"0x",
    '2':"0x",
    '3':"0x",
    '4':"0x",
    '5':"0x",
    '6':"0x",
    '7':"0x",
    '8':"(byte)0x",
    '9':"(byte)0x",
    'a':"(byte)0x",
    'b':"(byte)0x",
    'c':"(byte)0x",
    'd':"(byte)0x",
    'e':"(byte)0x",
    'f':"(byte)0x"
    }[value[0]] + value}</#macro
>
<#function argument_in_opcode instruction>
    <#list instruction.arguments as argument>
        <#if argument?starts_with("Op:")>
            <#return true>
        </#if>
    </#list>
    <#return false>
</#function>
<#macro argument_from_opcode instruction opcode_var rex_prefix>
    <#list instruction.arguments as argument>
        <#if argument?starts_with("Op:")
            >${argument?keep_after(":")
                }.of((${opcode_var} & 0b00_000_111) | ((${rex_prefix} & 0b0000_0_0_0_1) << 3)<#if
                argument?keep_after(":") == "GPRegister8">, ${rex_prefix} != 0</#if>)</#if
        >
    </#list>
</#macro>
<#function has_implicit_argument type instruction>
    <#list instruction.arguments as argument>
        <#if argument?starts_with(type)>
            <#return true>
        </#if>
    </#list>
    <#return false>
</#function>
<#macro implicit_argument type reg8bit reg16bit reg32bit reg64bit instruction>
    <#list instruction.arguments as argument>
        <#if argument?starts_with(type)
            >${{"GPRegister8": "GPRegister8." + reg8bit,
                "GPRegister16": "GPRegister16." + reg16bit,
                "GPRegister32": "GPRegister32." + reg32bit,
                "GPRegister64": "GPRegister64." + reg64bit}[argument?keep_after(":")]}</#if
        >
    </#list>
</#macro>
<#function has_immediate_address instruction>
    <#list instruction.arguments as argument>
        <#if argument?starts_with("Imm0:") && argument?contains("Address")>
            <#return true>
        </#if>
    </#list>
    <#return false>
</#function>
<#function has_immediate_argument0 instruction>
    <#list instruction.arguments as argument>
        <#if argument?starts_with("Imm0:Imm")>
            <#return true>
        </#if>
    </#list>
    <#return false>
</#function>
<#macro immediate_argument instruction>
    <#list instruction.arguments as argument>
        <#if argument?starts_with("Imm0:") || argument?starts_with("Imm1:")
        >${{"Imm8": "parseByte(it)",
            "Imm16": "parseShort(it)",
            "Imm32": "parseInteger(it)",
            "Imm64": "parseLong(it)"}[argument?keep_after(":")]}</#if
        >
    </#list>
</#macro>
<#function has_immediate_argument1 instruction>
    <#list instruction.arguments as argument>
        <#if argument?starts_with("Imm1:Imm")>
            <#return true>
        </#if>
    </#list>
    <#return false>
</#function>
<#macro reg_argument instruction modrm_var rex_prefix>
    <#list instruction.arguments as argument>
        <#if argument?starts_with("Reg:")
            >${argument?keep_after(":")
                }.of(((${modrm_var} & 0b000_111_000) >> 3) | ((${rex_prefix} & 0b0000_0_1_0_0) << 1)<#if
                argument?keep_after(":") == "GPRegister8">, ${rex_prefix} != 0</#if>)</#if
        >
    </#list>
</#macro>
<#macro rm_argument instruction modrm_var rex_prefix>
    <#list instruction.arguments as argument>
        <#if argument?starts_with("Rm:")
            >${argument?keep_after(":")
                }.of((${modrm_var} & 0b000_000_111) | ((${rex_prefix} & 0b0000_0_0_0_1) << 3)<#if
                argument?keep_after(":") == "GPRegister8">, ${rex_prefix} != 0</#if>)</#if
        >
    </#list>
</#macro>
<#macro parse_immediate_and_implicit_aruments indent instruction>
    <#if has_implicit_argument("AX:", instruction)>
        ${indent}var implicit_argument_ax = <@implicit_argument "AX:" "AL" "AX" "EAX" "RAX" instruction/>;
    </#if>
    <#if has_implicit_argument("CX:", instruction)>
        ${indent}var implicit_argument_cx = <@implicit_argument "CX:" "CL" "CX" "ECX" "RCX" instruction/>;
    </#if>
    <#if has_implicit_argument("DX:", instruction)>
        ${indent}var implicit_argument_dx = <@implicit_argument "DX:" "DL" "DX" "EDX" "RDX" instruction/>;
    </#if>
    <#if has_immediate_argument0(instruction)>
        ${indent}var immediate_argument0 = <@immediate_argument instruction/>;
        ${indent}if (immediate_argument0.isEmpty()) {
        ${indent}    return Optional.empty();
        ${indent}}
        <#if has_immediate_argument1(instruction)>
        ${indent}var immediate_argument1 = <@immediate_argument instruction/>;
        ${indent}if (immediate_argument1.isEmpty()) {
        ${indent}    return Optional.empty();
        ${indent}}
        </#if>
    </#if>
</#macro>
<#macro make_instruction_with_name instruction_name instruction>
Optional.of(new ${instruction_name}(<#list instruction.arguments as argument
    ><#if argument?starts_with("Imm0:") && argument?contains("Address")>immediate_address<#else
        >${{"AX": "implicit_argument_ax",
            "CX": "implicit_argument_cx",
            "DX": "implicit_argument_dx",
            "Imm0": "immediate_argument0.get()",
            "Imm1": "immediate_argument1.get()",
            "Op": "opcode_argument",
            "Reg": "reg_argument",
            "Rm" : "rm_argument"}[argument?keep_before(":")]}</#if
        >, </#list
    >toPrimitive(deque.toArray(empty_byte_array))))</#macro
>
<#macro make_instruction target_addr instruction>
    <#if target_addr == "">
        <@make_instruction_with_name instruction.name instruction/>
    <#else>
        <@make_instruction_with_name
            instruction.name
                ?replace("Addr16", "𝓐𝓭𝓭𝓻")
                ?replace("Addr32", "𝓐𝓭𝓭𝓻")
                ?replace("Addr32", "𝓐𝓭𝓭𝓻")
                ?replace("Addr64", "𝓐𝓭𝓭𝓻")
                ?replace("𝓐𝓭𝓭𝓻", target_addr)
            instruction/>
    </#if>
</#macro>
<#macro select_insruction_by_immediate indent opcode target_addr>
    <#-- We only have refined instructions if opcode is both bpcode for instructions and prefix for refixned one -->
    <#if prefix_opcode_map_x32[opcode]??>
        <#local refined_instructions = 0>
        <#list byte_vaues as opcode_value>
            <#if opcode_map_x32[opcode + " +0x" + opcode_value]??>
                <#local refined_instructions = refined_instructions + 1>
            </#if>
        </#list>
        <#if 1 == refined_instructions>
            <#list byte_vaues as opcode_value>
                <#if opcode_map_x32[opcode + " +0x" + opcode_value]??>
                    <#local refined_instructions = refined_instructions + 1>
        ${indent}if (immediate_argument0.get() == <@value_to_byte opcode_value/>) {
        ${indent}    return <@make_instruction target_addr opcode_map_x32[opcode + " +0x" + opcode_value]/>;
        ${indent}}
                </#if>
            </#list>
        <#elseif 1 < refined_instructions>
        ${indent}switch (immediate_argument0.get()) {
            <#list byte_vaues as opcode_value>
                <#if opcode_map_x32[opcode + " +0x" + opcode_value]??>
                    <#local refined_instructions = refined_instructions + 1>
        ${indent}    case <@value_to_byte opcode_value/>:
        ${indent}        return <@make_instruction target_addr opcode_map_x32[opcode + " +0x" + opcode_value]/>;
                </#if>
            </#list>
        ${indent}}
        </#if>
    </#if>
</#macro>
<#macro parse_operand_and_return_instruction indent native_operand_size opcode instruction>
    <#if native_operand_size == 16>
        ${indent}if (final_x67_prefix) {
        ${indent}    var parse_result = parseGPAddress32(final_segment, final_rex_prefix, it);
        ${indent}    if (parse_result.isEmpty()) {
        ${indent}        return Optional.empty();
        ${indent}    }
        ${indent}    var rm_argument = parse_result.get();
            <@parse_immediate_and_implicit_aruments indent+"    " instruction/>
            <@select_insruction_by_immediate indent+"    " opcode "Addr32"/>
        ${indent}    return <@make_instruction "Addr32" instruction/>;
        ${indent}} else {
        ${indent}    var parse_result = parseGPAddress16(final_segment, it);
        ${indent}    if (parse_result.isEmpty()) {
        ${indent}        return Optional.empty();
        ${indent}    }
        ${indent}    var rm_argument = parse_result.get();
            <@parse_immediate_and_implicit_aruments indent+"    " instruction/>
            <@select_insruction_by_immediate indent+"    " opcode "Addr16"/>
        ${indent}    return <@make_instruction "Addr16" instruction/>;
        ${indent}}
    <#elseif native_operand_size == 32>
        ${indent}if (final_x67_prefix && (mode != Mode.ADDR64_DATA32)) {
        ${indent}    var parse_result = parseGPAddress16(final_segment, it);
        ${indent}    if (parse_result.isEmpty()) {
        ${indent}        return Optional.empty();
        ${indent}    }
        ${indent}    var rm_argument = parse_result.get();
            <@parse_immediate_and_implicit_aruments indent+"    " instruction/>
            <@select_insruction_by_immediate indent+"    " opcode "Addr16"/>
        ${indent}    return <@make_instruction "Addr16" instruction/>;
        ${indent}} else if (final_x67_prefix ^ (mode != Mode.ADDR64_DATA32)) {
        ${indent}    if (mode == Mode.ADDR64_DATA32) {
        ${indent}        if ((it.peek() & 0b11_000_111) == 0b00_000_101) {
        ${indent}            it.next();
        ${indent}            var parse_result = parseInteger(it);
        ${indent}            if (parse_result.isEmpty()) {
        ${indent}                return Optional.empty();
        ${indent}            }
        ${indent}            var rm_argument = new EIPAddress32(final_segment, parse_result.get());
                    <@parse_immediate_and_implicit_aruments indent+"            " instruction/>
                    <@select_insruction_by_immediate indent+"            " opcode "EIPAddr32"/>
        ${indent}            return <@make_instruction "EIPAddr32" instruction/>;
        ${indent}        }
        ${indent}    }
        ${indent}    var parse_result = parseGPAddress32(final_segment, final_rex_prefix, it);
        ${indent}    if (parse_result.isEmpty()) {
        ${indent}        return Optional.empty();
        ${indent}    }
        ${indent}    var rm_argument = parse_result.get();
            <@parse_immediate_and_implicit_aruments indent+"    " instruction/>
            <@select_insruction_by_immediate indent+"    " opcode "Addr32"/>
        ${indent}    return <@make_instruction "Addr32" instruction/>;
        ${indent}} else {
        ${indent}    if ((it.peek() & 0b11_000_111) == 0b00_000_101) {
        ${indent}        it.next();
        ${indent}        var parse_result = parseInteger(it);
        ${indent}        if (parse_result.isEmpty()) {
        ${indent}            return Optional.empty();
        ${indent}        }
        ${indent}        var rm_argument = new RIPAddress64(final_segment, parse_result.get());
                <@parse_immediate_and_implicit_aruments indent+"        " instruction/>
                <@select_insruction_by_immediate indent+"        " opcode "RIPAddr64"/>
        ${indent}        return <@make_instruction "RIPAddr64" instruction/>;
        ${indent}    }
        ${indent}    var parse_result = parseGPAddress64(final_segment, final_rex_prefix, it);
        ${indent}    if (parse_result.isEmpty()) {
        ${indent}        return Optional.empty();
        ${indent}    }
        ${indent}    var rm_argument = parse_result.get();
            <@parse_immediate_and_implicit_aruments indent+"    " instruction/>
            <@select_insruction_by_immediate indent+"    " opcode "Addr64"/>
        ${indent}    return <@make_instruction "Addr64" instruction/>;
        ${indent}}
     <#else><#-- native_operand_size == 64 -->
        ${indent}if (final_x67_prefix) {
        ${indent}    if ((it.peek() & 0b11_000_111) == 0b00_000_101) {
        ${indent}        it.next();
        ${indent}        var parse_result = parseInteger(it);
        ${indent}        if (parse_result.isEmpty()) {
        ${indent}            return Optional.empty();
        ${indent}        }
        ${indent}        var rm_argument = new EIPAddress32(final_segment, parse_result.get());
                <@parse_immediate_and_implicit_aruments indent+"        " instruction/>
                <@select_insruction_by_immediate indent+"        " opcode "EIPAddr32"/>
        ${indent}        return <@make_instruction "EIPAddr32" instruction/>;
        ${indent}    }
        ${indent}    var parse_result = parseGPAddress32(final_segment, final_rex_prefix, it);
        ${indent}    if (parse_result.isEmpty()) {
        ${indent}        return Optional.empty();
        ${indent}    }
        ${indent}    var rm_argument = parse_result.get();
            <@parse_immediate_and_implicit_aruments indent+"    " instruction/>
            <@select_insruction_by_immediate indent+"    " opcode "Addr32"/>
        ${indent}    return <@make_instruction "Addr32" instruction/>;
        ${indent}} else {
        ${indent}    if ((it.peek() & 0b11_000_111) == 0b00_000_101) {
        ${indent}        it.next();
        ${indent}        var parse_result = parseInteger(it);
        ${indent}        if (parse_result.isEmpty()) {
        ${indent}            return Optional.empty();
        ${indent}        }
        ${indent}        var rm_argument = new RIPAddress64(final_segment, parse_result.get());
                <@parse_immediate_and_implicit_aruments indent+"        " instruction/>
                <@select_insruction_by_immediate indent+"        " opcode "RIPAddr64"/>
        ${indent}        return <@make_instruction "RIPAddr64" instruction/>;
        ${indent}    }
        ${indent}    var parse_result = parseGPAddress64(final_segment, final_rex_prefix, it);
        ${indent}    if (parse_result.isEmpty()) {
        ${indent}        return Optional.empty();
        ${indent}    }
        ${indent}    var rm_argument = parse_result.get();
            <@parse_immediate_and_implicit_aruments indent+"    " instruction/>
            <@select_insruction_by_immediate indent+"    " opcode "Addr64"/>
        ${indent}    return <@make_instruction "Addr64" instruction/>;
        ${indent}}
    </#if>
</#macro>
package ${session.currentProject.model.groupId}.x86.cpu;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.yajd.BufferedIterator;
import org.yajd.RollbackIterator;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Optional;
import java.util.ServiceConfigurationError;
import java.util.function.Supplier;

import static org.apache.commons.lang3.ArrayUtils.toPrimitive;

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
        default Type when(BadInstruction argument) {
            return when((Instruction) argument);
        }
<#list merged_instruction_classes_list as instruction_class>
    <#list instruction_class.names as instruction_name>
        default Type when(${classname(instruction_name, instruction_class.arguments)} argument) {
            return when((Instruction) argument);
        }
    </#list>
</#list>
    }

    final class BadInstruction implements Instruction {
        final private byte[] bytes;
        public BadInstruction(byte[] bytes) {
            this.bytes = bytes;
        }

        @Override
        public <Type> Type process(@NotNull Result<Type> result) {
            return result.when(this);
        }

        @Contract(pure = true)
        public @NotNull String getName() {
            return "BAD";
        }

        @Contract(pure = true)
        public @NotNull Argument[] getArguments() {
            return new Argument[0];
        }

        byte[] getBytes() {
            return bytes;
        }
    }

<#list merged_instruction_classes_list as instruction_class>
    <#list instruction_class.names as instruction_name>
    final class ${classname(instruction_name, instruction_class.arguments)} implements Instruction {
        final private byte[] bytes;
        <#list instruction_class.arguments as argument>
        final private ${argumeng_name_to_type_name[argument?keep_after(":")]} arg${argument?index};
        </#list>

        public ${classname(instruction_name, instruction_class.arguments)}(<#list
        instruction_class.arguments as argument>${
            argumeng_name_to_type_name[argument?keep_after(":")]} arg${argument?index}<#sep>, </#sep></#list
        ><#if instruction_class.arguments?size != 0>,</#if> byte[] bytes) {
        <#list instruction_class.arguments as argument>
            <#if argument?contains("/")>
            this.arg${argument?index} = new ${argumeng_name_to_type_name[argument?keep_after(":")]}(arg${
                argument?index}, (short)${argument?keep_after("/")});
            <#else>
            this.arg${argument?index} = arg${argument?index};
            </#if>
        </#list>
            this.bytes = bytes;
        }

        @Override
        public <Type> Type process(@NotNull Result<Type> result) {
            return result.when(this);
        }

        @Contract(pure = true)
        public @NotNull String getName() {
            return "${instruction_name}";
        }

        public @NotNull Argument[] getArguments() {
            return new Argument[]{<#list instruction_class.arguments as argument
                > <#if argument?contains(":Imm")>new ${argument?keep_after(":")}(arg${argument?index})<#else
                  >arg${argument?index}.toArgument()</#if><#sep>, </#sep></#list>};
        }

        <#list instruction_class.arguments as argument>
        public ${argumeng_name_to_type_name[argument?keep_after(":")]} getArg${argument?index}() {
            return arg${argument?index};
        }

        </#list>

        byte[] getBytes() {
            return bytes;
        }
    }
    </#list>
</#list>

    // Note: it would make more sense to make these classes top-level ones, not nested,
    // but we couldn't do that because of Apache freemarker generator for maven limitation.

    // There are three possible address modes and two possible data modes, but ADDR64_DATA16
    // is not supported by x86 architecture.
    enum Mode {
        ADDR16_DATA16,
        ADDR16_DATA32,
        ADDR32_DATA16,
        ADDR32_DATA32,
        ADDR64_DATA32;

        @Contract(pure = true)
        public static int addressSize(@NotNull Mode mode) {
            switch (mode) {
                case ADDR16_DATA16:
                case ADDR16_DATA32:
                    return 16;
                case ADDR32_DATA16:
                case ADDR32_DATA32:
                    return 32;
                case ADDR64_DATA32:
                    return 64;
                default:
                    return 0;
            }
        }

        @Contract(pure = true)
        public static int operandSize(@NotNull Mode mode) {
            switch (mode) {
                case ADDR16_DATA16:
                case ADDR32_DATA16:
                    return 16;
                case ADDR16_DATA32:
                case ADDR32_DATA32:
                case ADDR64_DATA32:
                    return 32;
                default:
                    return 0;
            }
        }
    }

    static Optional<Instruction> parse(@NotNull Mode mode, @NotNull RollbackIterator<Byte> it) {
        Deque<Byte> deque = new ArrayDeque<Byte>();
        return it.tryProcess(deque, () -> {
            // If there are more than one segment prefix then only last one is used.
            SegmentRegister segment = null;
            // If there are two or more 0xf2/0xf3 prefixes then only last one is used.
            byte xf2_xf3_prefix = 0;
            // Prefixes 0x66, 0x67, or 0xf0 are either present or not.
            boolean x66_prefix = false;
            boolean x67_prefix = false;
            boolean lock_prefix = false;

            scan_prefix: for (; ; ) {
                if (!it.hasNext()) {
                    return Optional.empty();
                }
                switch (it.peek()) {
                    case 0x26:
                        segment = SegmentRegister.ES;
                        break;
                    case 0x2e:
                        segment = SegmentRegister.CS;
                        break;
                    case 0x36:
                        segment = SegmentRegister.SS;
                        break;
                    case 0x3e:
                        segment = SegmentRegister.DS;
                        break;
                    case 0x64:
                        segment = SegmentRegister.FS;
                        break;
                    case 0x65:
                        segment = SegmentRegister.GS;
                        break;
                    case 0x66:
                        x66_prefix = true;
                        break;
                    case 0x67:
                        x67_prefix = true;
                        break;
                    case (byte) 0xf0:
                        lock_prefix = true;
                        break;
                    case (byte) 0xf2:
                        xf2_xf3_prefix = (byte) 0xf2;
                        break;
                    case (byte) 0xf3:
                        xf2_xf3_prefix = (byte) 0xf3;
                        break;
                    default:
                        break scan_prefix;
                }
                // Skip prefix.
                it.next();
            }

            if (!it.hasNext()) {
                return Optional.empty();
            }

            byte rex_prefix = 0;
            if (mode == Mode.ADDR64_DATA32) {
                if (0x40 <= it.peek() && it.peek() <= 0x4f) {
                    rex_prefix = it.next();
                    if (!it.hasNext()) {
                        return Optional.empty();
                    }
                }
            }
            // Lambdas can only access final or effectively final variables.
            // Copy collected prefixes into final varaibles.
            final byte final_rex_prefix = rex_prefix;
            final SegmentRegister final_segment = segment;
            final byte final_xf2_xf3_prefix = xf2_xf3_prefix;
            final boolean final_x66_prefix = x66_prefix;
            final boolean final_x67_prefix = x67_prefix;
            final boolean final_lock_prefix = lock_prefix;
            final byte opcode = it.next();
<#list [16, 32, 64] as native_operand_size>
    <#if native_operand_size == 16>
        <#assign opcode_map_x32 = opcode_map_16_x32>
        <#assign opcode_map_x64 = opcode_map_16_x64>
        <#assign prefix_opcode_map_x32 = prefix_opcode_map_16_x32>
        <#assign prefix_opcode_map_x64 = prefix_opcode_map_16_x64>
    <#elseif native_operand_size == 32>
        <#assign opcode_map_x32 = opcode_map_32_x32>
        <#assign opcode_map_x64 = opcode_map_32_x64>
        <#assign prefix_opcode_map_x32 = prefix_opcode_map_32_x32>
        <#assign prefix_opcode_map_x64 = prefix_opcode_map_32_x64>
    <#else>
        <#assign opcode_map_x32 = opcode_map_64>
        <#assign opcode_map_x64 = opcode_map_64>
        <#assign prefix_opcode_map_x32 = prefix_opcode_map_64>
        <#assign prefix_opcode_map_x64 = prefix_opcode_map_64>
    </#if>
            Supplier<Optional<Instruction>> parse_${native_operand_size}bit_instruction = () -> {
    <#list ["", "0x66"] as X66Prefix>
        <#list ["", "0xf2", "0xf3"] as Xf2Xf3Prefix>
            <#if X66Prefix == "">
                <#if Xf2Xf3Prefix == "">
                    <#assign opcode_prefix></#assign>
                <#else>
                    <#assign opcode_prefix>${Xf2Xf3Prefix} </#assign>
                </#if>
            <#else>
                <#if Xf2Xf3Prefix == "">
                    <#assign opcode_prefix>${X66Prefix} </#assign>
                <#else>
                    <#assign opcode_prefix>${X66Prefix} ${Xf2Xf3Prefix} </#assign>
                </#if>
            </#if>
            <#if native_operand_size == 64>
                <#assign opcode_prefix>${opcode_prefix}rexw </#assign>
            </#if>
            <#assign opcode_prefix>${opcode_prefix}0x</#assign>
                Supplier<Optional<Instruction>> parse_${X66Prefix}_${Xf2Xf3Prefix}_instruction = () -> {
                    switch (opcode) {
            <#list byte_vaues?filter(x -> !element_in_list(x, ["66", "f2", "f3"])) as opcode_value>
                <#if opcode_map_x32[opcode_prefix + opcode_value]?? ||
                     opcode_map_x64[opcode_prefix + opcode_value]?? ||
                     prefix_opcode_map_x32[opcode_prefix + opcode_value]?? ||
                     prefix_opcode_map_x64[opcode_prefix + opcode_value]??>
                        case <@value_to_byte opcode_value/>:
                    <#-- Handle register operant in opcode -->
                    <#if opcode_map_x32[opcode_prefix + opcode_value]?? ||
                         opcode_map_x64[opcode_prefix + opcode_value]??>
                        <#if opcode_map_x32[opcode_prefix + opcode_value]??>
                            <#assign instruction = opcode_map_x32[opcode_prefix + opcode_value]>
                        <#else>
                            <#assign instruction = opcode_map_x64[opcode_prefix + opcode_value]>
                        </#if>
                        <#if argument_in_opcode(instruction)>
                            <#-- Instructions with operands in opcode would occupy case lines -->
                            <#if opcode_value[1] == '7' || opcode_value[1] == 'f'>
                            {
                            <#if !opcode_map_x32[opcode_prefix + opcode_value]??>
                                if (mode != Mode.ADDR64_DATA32) {
                                    return Optional.empty();
                                }
                            </#if>
                            <#if !opcode_map_x64[opcode_prefix + opcode_value]?? && native_operand_size != 16>
                                if (mode == Mode.ADDR64_DATA32) {
                                    return Optional.empty();
                                }
                            </#if>
                                var opcode_argument = <@argument_from_opcode instruction "opcode" "final_rex_prefix"/>;
                                <@parse_immediate_and_implicit_aruments ""?left_pad(24) instruction/>
                                return <@make_instruction "" instruction/>;
                            }
                            <#else>
                            /* fallthrough */
                            </#if>
                        <#else>
                            <#if element_in_list(opcode_prefix + opcode_value,
                                ["0x90", "0x66 0x90", "0xf2 0x90", "0xf3 0x90", "0x66 0xf2 0x90", "0x66 0xf3 0x90"])>
                            // If there are REX.B prefix then 0x90 is not interpreted as NOP, but as Xchg.
                            // Note: objdump erroneously decodes, e.g., 0x40 0x90 as xchg - but that one is actually NOP.
                            if ((final_rex_prefix & 0b0000_0_0_0_1) == 0)
                            </#if>
                            {
                            <#if !opcode_map_x32[opcode_prefix + opcode_value]??>
                                if (mode != Mode.ADDR64_DATA32) {
                                    return Optional.empty();
                                }
                            </#if>
                            <#if !opcode_map_x64[opcode_prefix + opcode_value]?? && native_operand_size != 16>
                                if (mode == Mode.ADDR64_DATA32) {
                                    return Optional.empty();
                                }
                            </#if>
                                <@parse_immediate_and_implicit_aruments ""?left_pad(24) instruction/>
                                <@select_insruction_by_immediate ""?left_pad(24) opcode_prefix + opcode_value ""/>
                            <#if has_immediate_address(instruction)>
                                <#if native_operand_size == 16>
                                if (final_x67_prefix) {
                                    var immediate_argument0 = parseInteger(it);
                                    if (immediate_argument0.isEmpty()) {
                                        return Optional.empty();
                                    }
                                    var immediate_address = new GPAddress32(
                                            final_segment, null, null, ScaleFactor.X1, immediate_argument0.get());
                                    return <@make_instruction "Addr32" instruction/>;
                                } else {
                                    var immediate_argument0 = parseShort(it);
                                    if (immediate_argument0.isEmpty()) {
                                        return Optional.empty();
                                    }
                                    var immediate_address = new GPAddress16(
                                            final_segment, null, null, immediate_argument0.get());
                                    return <@make_instruction "Addr16" instruction/>;
                                }
                                <#elseif native_operand_size == 32>
                                if (mode == Mode.ADDR64_DATA32) {
                                    if (final_x67_prefix) {
                                        var immediate_argument0 = parseInteger(it);
                                        if (immediate_argument0.isEmpty()) {
                                            return Optional.empty();
                                        }
                                        var immediate_address = new GPAddress32(
                                                final_segment, null, null, ScaleFactor.X1, immediate_argument0.get());
                                        return <@make_instruction "Addr32" instruction/>;
                                    } else {
                                        var immediate_argument0 = parseLong(it);
                                        if (immediate_argument0.isEmpty()) {
                                            return Optional.empty();
                                        }
                                        var immediate_address = new AbsoluteAddress64(
                                                final_segment, immediate_argument0.get());
                                        return <@make_instruction "AbsAddr64" instruction/>;
                                    }
                                } else {
                                    if (final_x67_prefix) {
                                        var immediate_argument0 = parseShort(it);
                                        if (immediate_argument0.isEmpty()) {
                                            return Optional.empty();
                                        }
                                        var immediate_address = new GPAddress16(
                                                final_segment, null, null, immediate_argument0.get());
                                        return <@make_instruction "Addr16" instruction/>;
                                    } else {
                                        var immediate_argument0 = parseInteger(it);
                                        if (immediate_argument0.isEmpty()) {
                                            return Optional.empty();
                                        }
                                        var immediate_address = new GPAddress32(
                                                final_segment, null, null, ScaleFactor.X1, immediate_argument0.get());
                                        return <@make_instruction "Addr32" instruction/>;
                                    }
                                }
                                <#else>
                                if (final_x67_prefix) {
                                    var immediate_argument0 = parseInteger(it);
                                    if (immediate_argument0.isEmpty()) {
                                        return Optional.empty();
                                    }
                                    var immediate_address = new GPAddress32(
                                            final_segment, null, null, ScaleFactor.X1, immediate_argument0.get());
                                    return <@make_instruction "Addr32" instruction/>;
                                } else {
                                    var immediate_argument0 = parseLong(it);
                                    if (immediate_argument0.isEmpty()) {
                                        return Optional.empty();
                                    }
                                    var immediate_address = new AbsoluteAddress64(
                                            final_segment, immediate_argument0.get());
                                    return <@make_instruction "AbsAddr64" instruction/>;
                                }
                                </#if>
                            <#else>
                                <#if opcode_map_x32[opcode_prefix + opcode_value]?? &&
                                     opcode_map_x64[opcode_prefix + opcode_value]?? &&
                                     (opcode_map_x32[opcode_prefix + opcode_value].name !=
                                      opcode_map_x64[opcode_prefix + opcode_value].name)>
                                if (mode == Mode.ADDR64_DATA32) {
                                    <#assign instruction = opcode_map_x64[opcode_prefix + opcode_value]>
                                    return <@make_instruction "" instruction/>;
                                } else {
                                    <#assign instruction = opcode_map_x32[opcode_prefix + opcode_value]>
                                    return <@make_instruction "" instruction/>;
                                }
                                <#else>
                                return <@make_instruction "" instruction/>;
                                </#if>
                            </#if>
                            }
                            <#if element_in_list(opcode_prefix + opcode_value,
                                ["0x90", "0x66 0x90", "0xf2 0x90", "0xf3 0x90", "0x66 0xf2 0x90", "0x66 0xf3 0x90"])>
                            /* fallthrough */
                            </#if>
                        </#if>
                    <#elseif opcode_map_x32[opcode_prefix + opcode_value + " /r"]?? ||
                             opcode_map_x64[opcode_prefix + opcode_value + " /r"]?? ||
                             opcode_map_x32[opcode_prefix + opcode_value + " /m"]?? ||
                             opcode_map_x64[opcode_prefix + opcode_value + " /m"]??>
                            {
                                if (!it.hasNext()) {
                                    return Optional.empty();
                                }
                        <#if (opcode_map_x32[opcode_prefix + opcode_value + " /r"]?? &&
                              opcode_map_x64[opcode_prefix + opcode_value + " /r"]?? &&
                              (opcode_map_x32[opcode_prefix + opcode_value + " /r"].name !=
                                  opcode_map_x64[opcode_prefix + opcode_value + " /r"].name)) ||
                             (opcode_map_x32[opcode_prefix + opcode_value + " /m"]?? &&
                              opcode_map_x64[opcode_prefix + opcode_value + " /m"]?? &&
                              (opcode_map_x32[opcode_prefix + opcode_value + " /m"].name !=
                                  opcode_map_x64[opcode_prefix + opcode_value + " /m"].name))>
                            <#assign x32_x64_difference = true>
                                if (mode != Mode.ADDR64_DATA32) {
                        <#else>
                            <#assign x32_x64_difference = false>
                        </#if>
                        <#if  opcode_map_x32[opcode_prefix + opcode_value + " /r"]??>
                            <#assign instruction = opcode_map_x32[opcode_prefix + opcode_value + " /r"]>
                        <#else>
                            <#assign instruction = opcode_map_x32[opcode_prefix + opcode_value + " /m"]>
                        </#if>
                                var reg_argument = <@reg_argument instruction "it.peek()" "final_rex_prefix"/>;
                        <#if opcode_map_x32[opcode_prefix + opcode_value + " /r"]??>
                                if ((it.peek() & 0b11_000_000) == 0b11_000_000) {
                                    var rm_argument = <@rm_argument instruction "it.next()" "final_rex_prefix"/>;
                                    <@parse_immediate_and_implicit_aruments ""?left_pad(36) instruction/>
                                    <@select_insruction_by_immediate
                                        ""?left_pad(36) opcode_prefix + opcode_value + "r" ""/>
                                    return <@make_instruction "" instruction/>;
                                }
                        </#if>
                        <#if opcode_map_x32[opcode_prefix + opcode_value + " /m"]??>
                            <#if opcode_map_x32[opcode_prefix + opcode_value + " /r"]??>
                                <#assign instruction = opcode_map_x32[opcode_prefix + opcode_value + " /m"]>
                                <#assign incomplete_instruction = false>
                                else {
                            <#else>
                                <#assign incomplete_instruction = true>
                                if ((it.peek() & 0b11_000_000) != 0b11_000_000) {
                            </#if>
                            <@parse_operand_and_return_instruction
                                ""?left_pad(28) native_operand_size opcode_prefix + opcode_value + " /m" instruction/>
                                }
                        <#else>
                            <#assign incomplete_instruction = true>
                        </#if>
                        <#if incomplete_instruction>
                                return Optional.empty();
                        </#if>
                        <#if x32_x64_difference>
                                } else {
                            <#if  opcode_map_x64[opcode_prefix + opcode_value + " /r"]??>
                                <#assign instruction = opcode_map_x64[opcode_prefix + opcode_value + " /r"]>
                            <#else>
                                <#assign instruction = opcode_map_x64[opcode_prefix + opcode_value + " /m"]>
                            </#if>
                            <#if opcode_map_x64[opcode_prefix + opcode_value + " /r"]??>
                                if ((it.peek() & 0b11_000_000) == 0b11_000_000) {
                                    var rm_argument = <@rm_argument instruction "it.next()" "final_rex_prefix"/>;
                                    <@parse_immediate_and_implicit_aruments ""?left_pad(36) instruction/>
                                    <@select_insruction_by_immediate
                                        ""?left_pad(36) opcode_prefix + opcode_value + "r" ""/>
                                    return <@make_instruction "" instruction/>;
                                }
                            </#if>
                            <#if opcode_map_x64[opcode_prefix + opcode_value + " /m"]??>
                                <#if opcode_map_x64[opcode_prefix + opcode_value + " /r"]??>
                                    <#assign instruction = opcode_map_x64[opcode_prefix + opcode_value + " /m"]>
                                    <#assign incomplete_instruction = false>
                                else {
                                <#else>
                                    <#assign incomplete_instruction = true>
                                if ((it.peek() & 0b11_000_000) != 0b11_000_000) {
                                </#if>
                                    <@parse_operand_and_return_instruction
                                        ""?left_pad(28) native_operand_size opcode_prefix + opcode_value + " /m" instruction/>
                                }
                            <#else>
                                <#assign incomplete_instruction = true>
                            </#if>
                            <#if incomplete_instruction>
                                return Optional.empty();
                            </#if>
                                }
                        </#if>
                            }
                    <#elseif prefix_opcode_map_x32[opcode_prefix + opcode_value + " /r"]?? ||
                             prefix_opcode_map_x64[opcode_prefix + opcode_value + " /r"]?? ||
                             prefix_opcode_map_x32[opcode_prefix + opcode_value + " /m"]?? ||
                             prefix_opcode_map_x64[opcode_prefix + opcode_value + " /m"]?? >
                            {
                                if (!it.hasNext()) {
                                    return Optional.empty();
                                }
                                Supplier<Optional<Instruction>> parse_extension = () -> {
                                    byte opcode_extension = (byte)(it.peek() & 0b00_111_000);
                        <#if prefix_opcode_map_x32[opcode_prefix + opcode_value + " /r"]?? ||
                             prefix_opcode_map_x64[opcode_prefix + opcode_value + " /r"]??>
                                    if ((it.peek() & 0b11_000_000) == 0b11_000_000) {
                                        switch (opcode_extension) {
                            <#list ["0:000", "1:001", "2:010", "3:011", "4:100", "5:101", "6:110", "7:111"]
                                   as opcode_extension>
                                <#assign full_opcode = opcode_prefix + opcode_value + " /r /" + opcode_extension?keep_before(":")>
                                <#if opcode_map_x32[full_opcode]?? || opcode_map_x64[full_opcode]??>
                                            case 0b00_${opcode_extension?keep_after(":")}_000 :
                                                {
                                    <#if !opcode_map_x64[full_opcode]??>
                                        <#if native_operand_size != 16>
                                                    if (mode == Mode.ADDR64_DATA32) {
                                                        return Optional.empty();
                                                    }
                                        </#if>
                                    <#else>
                                        <#assign instruction = opcode_map_x64[full_opcode]>
                                    </#if>
                                    <#if !opcode_map_x32[full_opcode]??>
                                                    if (mode != Mode.ADDR64_DATA32) {
                                                        return Optional.empty();
                                                    }
                                    <#else>
                                        <#assign instruction = opcode_map_x32[full_opcode]>
                                    </#if>
                                                    var rm_argument = <@rm_argument instruction "it.next()" "final_rex_prefix"/>;
                                                    <@parse_immediate_and_implicit_aruments ""?left_pad(40) instruction/>
                                                    <@select_insruction_by_immediate ""?left_pad(48) full_opcode ""/>
                                                    return <@make_instruction "" instruction/>;
                                                }
                                </#if>
                            </#list>
                                            default:
                                                return Optional.empty();
                                        }
                                    }
                        </#if>
                        <#if prefix_opcode_map_x32[opcode_prefix + opcode_value + " /m"]?? ||
                             prefix_opcode_map_x64[opcode_prefix + opcode_value + " /m"]??>
                            <#if prefix_opcode_map_x32[opcode_prefix + opcode_value + " /r"]?? ||
                                 prefix_opcode_map_x64[opcode_prefix + opcode_value + " /r"]??>
                                    else {
                            <#else>
                                    if ((it.peek() & 0b11_000_000) != 0b11_000_000) {
                            </#if>
                                        switch (opcode_extension) {
                            <#list ["0:000", "1:001", "2:010", "3:011", "4:100", "5:101", "6:110", "7:111"]
                                   as opcode_extension>
                                <#assign full_opcode = opcode_prefix + opcode_value + " /m /" + opcode_extension?keep_before(":")>
                                <#if opcode_map_x32[full_opcode]?? || opcode_map_x64[full_opcode]??>
                                            case 0b00_${opcode_extension?keep_after(":")}_000:
                                                {
                                    <#if !opcode_map_x64[full_opcode]??>
                                        <#if native_operand_size != 16>
                                                    if (mode == Mode.ADDR64_DATA32) {
                                                        return Optional.empty();
                                                    }
                                        </#if>
                                    <#else>
                                        <#assign instruction = opcode_map_x64[full_opcode]>
                                    </#if>
                                    <#if !opcode_map_x32[full_opcode]??>
                                                    if (mode != Mode.ADDR64_DATA32) {
                                                        return Optional.empty();
                                                    }
                                    <#else>
                                        <#assign instruction = opcode_map_x32[full_opcode]>
                                    </#if>
                                                    <@parse_operand_and_return_instruction
                                                        ""?left_pad(44) native_operand_size full_opcode instruction/>
                                                }
                                </#if>
                            </#list>
                                            default:
                                                return Optional.empty();
                                        }
                                    }
                        </#if>
                                };
                                return parse_extension.get();
                            }
                    </#if>
                </#if>
            </#list>
                        default:
                            return Optional.empty();
                    }
                };
        </#list>
    </#list>
                if (final_x66_prefix) {
                    if (final_xf2_xf3_prefix == (byte) 0xf2) {
                        return parse_0x66_0xf2_instruction.get();
                    } else if (final_xf2_xf3_prefix == (byte) 0xf3) {
                        return parse_0x66_0xf3_instruction.get();
                    } else {
                        return parse_0x66__instruction.get();
                    }
                } else {
                    if (final_xf2_xf3_prefix == (byte) 0xf2) {
                        return parse__0xf2_instruction.get();
                    } else if (final_xf2_xf3_prefix == (byte) 0xf3) {
                        return parse__0xf3_instruction.get();
                    } else {
                        return parse___instruction.get();
                    }
                }
            };
</#list>
            if ((rex_prefix & 0b000_1_0_0_0) != 0) {
                return parse_64bit_instruction.get();
            } else if (Mode.operandSize(mode) == 32) {
                return parse_32bit_instruction.get();
            } else {
                return parse_16bit_instruction.get();
            }
        });
    }

    // Deque toArray ABI needs an empty byte array.
    // Make one and keep it here to avoid calling new again and again.
    Byte[] empty_byte_array = new Byte[0];

    /* Note: GPAddress16 is not supported in ADDR64_DATA32 mode thus REX support is not needed */
    static Optional<GPAddress16> parseGPAddress16(SegmentRegister segment, @NotNull Iterator<Byte> it) {
        if (!it.hasNext()) {
            return Optional.empty();
        }
        byte modrm = it.next();
        assert (modrm & 0b11_000_000) != 0b11_000_000;
        if ((modrm & 0b11_000_111) == 0b00_000_110) {
            Optional<Short> disp16 = parseShort(it);
            if (disp16.isPresent()) {
                return Optional.of(new GPAddress16(segment, null, null, disp16.get()));
            }
            return Optional.empty();
        }
        short disp = 0;
        if ((modrm & 0b11_000_000) == 0b01_000_000) {
            if (!it.hasNext()) {
                return Optional.empty();
            }
            disp = it.next();
        } else if ((modrm & 0b11_000_000) == 0b10_000_000) {
            Optional<Short> disp16 = parseShort(it);
            if (disp16.isEmpty()) {
                return Optional.empty();
            }
            disp = disp16.get();
        }
        return Optional.of(new GPAddress16(
                segment, base_by_rm[modrm & 0b00_000_111], index_by_rm[modrm & 0b00_000_111], disp));
    }

    GPRegister16[] base_by_rm = new GPRegister16[]{
            GPRegister16.BX,
            GPRegister16.BX,
            GPRegister16.BP,
            GPRegister16.BP,
            null,
            null,
            GPRegister16.BP,
            GPRegister16.BX
    };

    GPRegister16[] index_by_rm = new GPRegister16[]{
            GPRegister16.SI,
            GPRegister16.DI,
            GPRegister16.SI,
            GPRegister16.DI,
            GPRegister16.SI,
            GPRegister16.DI,
            null,
            null
    };

<#list [32, 64] as AddrSize>
    static Optional<GPAddress${AddrSize}> parseGPAddress${AddrSize}(
            SegmentRegister segment, byte rex, @NotNull Iterator<Byte> it) {
        if (!it.hasNext()) {
            return Optional.empty();
        }
        byte modrm = it.next();
        assert (modrm & 0b11_000_000) != 0b11_000_000;
        GPRegister${AddrSize} base = null;
        GPRegister${AddrSize} index = null;
        ScaleFactor scale = ScaleFactor.X1;
        int disp = 0;
        if ((modrm & 0b11_000_111) == 0b00_000_101) {
            var disp32 = parseInteger(it);
            if (disp32.isEmpty()) {
                return Optional.empty();
            }
            disp = disp32.get();
        } else if ((modrm & 0b00_000_111) != 0b00_000_100) {
            base = GPRegister${AddrSize}.of((modrm & 0b00_000_111) | ((rex & 0b00_000_001) << 3));
            if ((modrm & 0b11_000_000) == 0b01_000_000) {
                if (!it.hasNext()) {
                    return Optional.empty();
                }
                disp = it.next();
            } else if ((modrm & 0b11_000_000) == 0b10_000_000) {
                var disp32 = parseInteger(it);
                if (disp32.isEmpty()) {
                    return Optional.empty();
                }
                disp = disp32.get();
            }
        } else if (!it.hasNext()) {
            return Optional.empty();
        } else {
            byte sib = it.next();
            scale = ScaleFactor.of((sib & 0b11_000_000) >>> 6);
            if (((modrm & 0b11_000_000) == 0b00_000_000) &&
                    ((sib & 0b00_000_111) == 0b00_000_101)) {
                var disp32 = parseInteger(it);
                if (disp32.isEmpty()) {
                    return Optional.empty();
                }
                disp = disp32.get();
            } else {
                base = GPRegister${AddrSize}.of((sib & 0b00_000_111) | ((rex & 0b0000_0_0_0_1) << 3));
                if ((modrm & 0b11_000_000) == 0b01_000_000) {
                    if (!it.hasNext()) {
                        return Optional.empty();
                    }
                    disp = it.next();
                } else if ((modrm & 0b11_000_000) == 0b10_000_000) {
                    var disp32 = parseInteger(it);
                    if (disp32.isEmpty()) {
                        return Optional.empty();
                    }
                    disp = disp32.get();
                }
            }
            index = GPRegister${AddrSize}.of(((sib & 0b00_111_000) >>> 3) | ((rex & 0b0000_0_0_1_0) << 2));
            if (index == GPRegister${AddrSize}.<#if AddrSize == 32>ESP<#else>RSP</#if>) {
                index = null;
            }
        }
        return Optional.of(new GPAddress${AddrSize}(segment, base, index, scale, disp));
    }
</#list>

    static Optional<Byte> parseByte(@NotNull Iterator<Byte> it) {
        if (!it.hasNext()) {
            return Optional.empty();
        }
        return Optional.of(it.next());
    }

    static Optional<Short> parseShort(@NotNull Iterator<Byte> it) {
        if (!it.hasNext()) {
            return Optional.empty();
        }
        byte low_byte = it.next();
        if (!it.hasNext()) {
            return Optional.empty();
        }
        byte high_byte = it.next();
        return Optional.of((short)((high_byte << 8) | low_byte));
    }

    static Optional<Integer> parseInteger(@NotNull Iterator<Byte> it) {
        if (!it.hasNext()) {
            return Optional.empty();
        }
        byte low_byte = it.next();
        if (!it.hasNext()) {
            return Optional.empty();
        }
        byte second_byte = it.next();
        if (!it.hasNext()) {
            return Optional.empty();
        }
        byte third_byte = it.next();
        if (!it.hasNext()) {
            return Optional.empty();
        }
        byte high_byte = it.next();
        return Optional.of((int)((high_byte << 24) |
                                 (third_byte << 16) |
                                 (second_byte << 8) |
                                 low_byte));
    }

    static Optional<Long> parseLong(@NotNull Iterator<Byte> it) {
        if (!it.hasNext()) {
            return Optional.empty();
        }
        byte low_byte = it.next();
        if (!it.hasNext()) {
            return Optional.empty();
        }
        byte second_byte = it.next();
        if (!it.hasNext()) {
            return Optional.empty();
        }
        byte third_byte = it.next();
        if (!it.hasNext()) {
            return Optional.empty();
        }
        byte fourth_byte = it.next();
        if (!it.hasNext()) {
            return Optional.empty();
        }
        byte fifth_byte = it.next();
        if (!it.hasNext()) {
            return Optional.empty();
        }
        byte sixth_byte = it.next();
        if (!it.hasNext()) {
            return Optional.empty();
        }
        byte seventh_byte = it.next();
        if (!it.hasNext()) {
            return Optional.empty();
        }
        byte high_byte = it.next();
        return Optional.of((((long)high_byte << 56L) |
                            ((long)seventh_byte << 48L) |
                            ((long)sixth_byte << 40L) |
                            ((long)fifth_byte << 32L) |
                            (fourth_byte << 24) |
                            (third_byte << 16) |
                            (second_byte << 8) |
                            low_byte));
    }
}
