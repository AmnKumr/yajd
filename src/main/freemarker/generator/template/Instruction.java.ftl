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
            <#return [arguments[0]] + replace_arguments(arguments[1..], from, to)>
        </#if>
    <#else>
        <#return replace_arguments(
            replace_arguments(arguments, [from[0]], [to[0]]), from[1..], to[1..])>
    </#if>
</#function>
<#function address_operand arguments>
    <#list arguments as argument>
        <#if element_in_list(argument?keep_after(":"),
                             ["Memory8", "Memory16", "Memory32", "x64Memory"])>
            <#return true>
        </#if>
    </#list>
    <#return false>
</#function>
<#function address64_operand arguments>
    <#list arguments as argument>
        <#if argument?keep_after(":") == "x64Memory">
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
                    ["Memory8", "Memory16", "Memory32", "x64Memory"],
                    ["EIPAddress32/8", "EIPAddress32/16", "EIPAddress32/32", "EIPAddress32/64"])}] +
                [instructions[0] + {"arguments": replace_arguments(
                    instructions[0].arguments,
                    ["Memory8", "Memory16", "Memory32", "x64Memory"],
                    ["GPAddress32/8", "GPAddress32/16", "GPAddress32/32", "GPAddress32/64"])}] +
                [instructions[0] + {"arguments": replace_arguments(
                    instructions[0].arguments,
                    ["Memory8", "Memory16", "Memory32", "x64Memory"],
                    ["RIPAddress64/8", "RIPAddress64/16", "RIPAddress64/32", "RIPAddress64/64"])}] +
                [instructions[0] + {"arguments": replace_arguments(
                    instructions[0].arguments,
                    ["Memory8", "Memory16", "Memory32", "x64Memory"],
                    ["GPAddress64/8", "GPAddress64/16", "GPAddress64/32", "GPAddress64/64"])}]>
            <#if !address64_operand(instructions[0].arguments)>
                <#local result = result +
                    [instructions[0] + {"arguments": replace_arguments(
                        instructions[0].arguments,
                        ["Memory8", "Memory16", "Memory32"],
                        ["GPAddress16/8", "GPAddress16/16", "GPAddress16/32"])}]>
            </#if>
            <#return result + expand_native_arguments(instructions[1..])>
        <#else>
            <#return [instructions[0]] + expand_native_arguments(instructions[1..])>
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
            <#local opcode64 = opcode_64_bit_prefix + names[keys[0]]>
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
                            ["GPRegisterNative", "MemoryNative"],
                            ["GPRegister16", "Memory16"]),
                        "names": instructions[0].names + split_opcodes(
                            instructions[0].names?keys,
                            instructions[0].names,
                            "", "0x66 ", "0x66 ")},
                    instructions[0] + {
                        "arguments": replace_arguments(
                            instructions[0].arguments,
                            ["GPRegisterNative", "MemoryNative"],
                            ["GPRegister32", "Memory32"]),
                        "names": instructions[0].names + split_opcodes(
                            instructions[0].names?keys,
                            instructions[0].names,
                            "0x66 ", "", "")},
                    instructions[0] + {
                        "arguments": replace_arguments(
                            instructions[0].arguments,
                            ["GPRegisterNative", "MemoryNative"],
                            ["GPRegister64", "x64Memory"]),
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
            <#return expand_native_arguments([instructions[0]]) +
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
            <#return [new_names[0]] + filter_out_instructions(existing_names, new_names[1..])>
        <#else>
            <#return filter_out_instructions(existing_names, new_names[1..])>
        </#if>
    <#else>
        <#return filter_out_instructions(existing_names[0],
                     filter_out_instructions(existing_names[1..], new_names))>
    </#if>
</#function>
<#function merge_instruction merged_instructions original_instructions>
    <#if original_instructions?size == 0>
        <#return merged_instructions>
    <#elseif merged_instructions?filter(
                 x -> same_argument_lists(x.arguments, original_instructions[0].arguments))?size == 0>
        <#return merge_instruction(merged_instructions + [
            original_instructions[0] + {"names": original_instructions[0].names?keys}],
            original_instructions[1..])>
    <#else>
        <#local filtered_names = filter_out_instructions(merged_instructions?filter(
                    x -> same_argument_lists(x.arguments, original_instructions[0].arguments))?map(
                    x -> x.names), original_instructions[0].names?keys)>
        <#if filtered_names?size != 0>
            <#return merge_instruction(merged_instructions + [
                 original_instructions[0] + {"names": filtered_names}],
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
        name?capitalize}<#list arguments as argument>${
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
<#function generate_opcodes_map opcodes_variant expanded_instruction_classes_list>
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
                <#if opcode != "">
                    <#if operand_in_opcode(instruction_class.arguments)>
                        <#if opcode?ends_with("0")>
                            <#local suffixes = ["0", "1", "2", "3", "4", "5", "6", "7"]>
                        <#else>
                            <#local suffixes = ["8", "9", "a", "b", "c", "d", "e", "f"]>
                        </#if>
                        <#list suffixes as suffix>
                            "${opcode[0..<(opcode?length-1)]}${suffix}" : {
                                "name" : "${classname(instruction_name, instruction_class.arguments)}",
                                "arguments" : ["${instruction_class.arguments?join("\", \"")}"]
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
                </#if>
            </#list>
        </#list>
    </#assign>
    Evaluate Nop last to make sure it would be choosen over “xchg %ax, %ax”
    <#assign opcodes_map_text =
        "{${opcodes_map_text}\"0x90\": { \"name\": \"Nop\", \"arguments\": [] }}"?eval>
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
<#assign opcode_map_16 = generate_opcodes_map(0, expanded_instruction_classes_list)>
<#assign opcode_map_32 = generate_opcodes_map(1, expanded_instruction_classes_list)>
<#assign opcode_map_64 = generate_opcodes_map(2, expanded_instruction_classes_list)>
<#-- We need to know if it makes sense to continue to parse instruction after certain “prefix”
     part. For example PF2IW have opcode “0x0f 0x0f 0x1c” where 0x1c part comes in place of
     immediate. We need to ensure parsing would stop at “0x0f 0x0f”.

     But in some cases prefix is also a valid instruction by itself. E.g. CMPPS have opcode
     “0x0f 0xc2” yet CMPEQPS have opcode “0x0f 0xc2 0x00” and we need to handle both of these. -->
<#assign prefix_opcode_map_16 = prefix_opcode_map(opcode_map_16)>
<#assign prefix_opcode_map_32 = prefix_opcode_map(opcode_map_32)>
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
<#function value_to_byte value>
    <#-- Note: freemarker doesn't have a lexicogrphical comparison for strings… use hash instead… -->
    <#return {
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
    }[value[0]] + value>
</#function>
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
                }.of((${opcode_var} & 0b00_000_111) | ((${rex_prefix} & 0b0000_0_0_0_1) << 3))</#if
        >
    </#list>
</#macro>
<#function has_implicit_argument instruction>
    <#list instruction.arguments as argument>
        <#if argument?starts_with("AX:")>
            <#return true>
        </#if>
    </#list>
    <#return false>
</#function>
<#macro implicit_argument instruction>
    <#list instruction.arguments as argument>
        <#if argument?starts_with("AX:")
            >${{"GPRegister8": "GPRegister8.AL",
                "GPRegister16": "GPRegister16.AX",
                "GPRegister32": "GPRegister32.EAX",
                "GPRegister64": "GPRegister64.RAX"}[argument?keep_after(":")]}</#if
        >
    </#list>
</#macro>
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
<#macro make_instruction_with_name instruction_name instruction>
Optional.of(new ${instruction_name}(<#list instruction.arguments as argument
    >${{"AX": "implicit_argument",
        "Op": "opcode_argument",
        "Reg": "reg_argument",
        "Rm" : "rm_argument"}[argument?keep_before(":")]}, </#list
    >toPrimitive(deque.toArray(empty_byte_array))))</#macro
>
<#macro make_instruction instruction>
<@make_instruction_with_name instruction.name instruction/>
</#macro>
<#macro make_instruction_addr16 instruction>
    <@make_instruction_with_name instruction.name?replace("Addr32", "Addr16")?replace("Addr64", "Addr16") instruction/>
</#macro>
<#macro make_instruction_addr32 instruction>
    <@make_instruction_with_name instruction.name?replace("Addr16", "Addr32")?replace("Addr64", "Addr32") instruction/>
</#macro>
<#macro make_instruction_eip_addr32 instruction>
    <@make_instruction_with_name instruction.name?replace("Addr32", "EIPAddr32")?replace("Addr16", "EIPAddr32")?replace("Addr64", "EIPAddr32") instruction/>
</#macro>
<#macro make_instruction_addr64 instruction>
    <@make_instruction_with_name instruction.name?replace("Addr16", "Addr64")?replace("Addr32", "Addr64") instruction/>
</#macro>
<#macro make_instruction_rip_addr64 instruction>
    <@make_instruction_with_name instruction.name?replace("Addr64", "RIPAddr64")?replace("Addr16", "RIPAddr64")?replace("Addr32", "RIPAddr64") instruction/>
</#macro>
<#macro parse_operand_and_return_instruction indent native_operand_size instruction>
    <#if native_operand_size == 16>
        ${indent}if (final_x67_prefix) {
        ${indent}    var parse_result = parseGPAddress32(final_segment, final_rex_prefix, it);
        ${indent}    if (parse_result.isEmpty()) {
        ${indent}        return Optional.empty();
        ${indent}    }
        ${indent}    var rm_argument = parse_result.get();
        ${indent}    return <@make_instruction_addr32 instruction/>;
        ${indent}} else {
        ${indent}    var parse_result = parseGPAddress16(final_segment, it);
        ${indent}    if (parse_result.isEmpty()) {
        ${indent}        return Optional.empty();
        ${indent}    }
        ${indent}    var rm_argument = parse_result.get();
        ${indent}    return <@make_instruction_addr16 instruction/>;
        ${indent}}
    <#elseif native_operand_size == 32>
        ${indent}if (final_x67_prefix && (mode != Mode.ADDR64_DATA32)) {
        ${indent}    var parse_result = parseGPAddress16(final_segment, it);
        ${indent}    if (parse_result.isEmpty()) {
        ${indent}        return Optional.empty();
        ${indent}    }
        ${indent}    var rm_argument = parse_result.get();
        ${indent}    return <@make_instruction_addr16 instruction/>;
        ${indent}} else if (final_x67_prefix ^ (mode != Mode.ADDR64_DATA32)) {
        ${indent}    if (mode == Mode.ADDR64_DATA32) {
        ${indent}        if ((it.peek() & 0b11_000_111) == 0b00_000_101) {
        ${indent}            it.next();
        ${indent}            var parse_result = parseInteger(it);
        ${indent}            if (parse_result.isEmpty()) {
        ${indent}                return Optional.empty();
        ${indent}            }
        ${indent}            var rm_argument = new EIPAddress32(final_segment, parse_result.get());
        ${indent}            return <@make_instruction_eip_addr32 instruction/>;
        ${indent}        }
        ${indent}    }
        ${indent}    var parse_result = parseGPAddress32(final_segment, final_rex_prefix, it);
        ${indent}    if (parse_result.isEmpty()) {
        ${indent}        return Optional.empty();
        ${indent}    }
        ${indent}    var rm_argument = parse_result.get();
        ${indent}    return <@make_instruction_addr32 instruction/>;
        ${indent}} else {
        ${indent}    if ((it.peek() & 0b11_000_111) == 0b00_000_101) {
        ${indent}        it.next();
        ${indent}        var parse_result = parseInteger(it);
        ${indent}        if (parse_result.isEmpty()) {
        ${indent}            return Optional.empty();
        ${indent}        }
        ${indent}        var rm_argument = new RIPAddress64(final_segment, parse_result.get());
        ${indent}        return <@make_instruction_rip_addr64 instruction/>;
        ${indent}    }
        ${indent}    var parse_result = parseGPAddress64(final_segment, final_rex_prefix, it);
        ${indent}    if (parse_result.isEmpty()) {
        ${indent}        return Optional.empty();
        ${indent}    }
        ${indent}    var rm_argument = parse_result.get();
        ${indent}    return <@make_instruction_addr64 instruction/>;
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
        ${indent}        return <@make_instruction_eip_addr32 instruction/>;
        ${indent}    }
        ${indent}    var parse_result = parseGPAddress32(final_segment, final_rex_prefix, it);
        ${indent}    if (parse_result.isEmpty()) {
        ${indent}        return Optional.empty();
        ${indent}    }
        ${indent}    var rm_argument = parse_result.get();
        ${indent}    return <@make_instruction_addr32 instruction/>;
        ${indent}} else {
        ${indent}    if ((it.peek() & 0b11_000_111) == 0b00_000_101) {
        ${indent}        it.next();
        ${indent}        var parse_result = parseInteger(it);
        ${indent}        if (parse_result.isEmpty()) {
        ${indent}            return Optional.empty();
        ${indent}        }
        ${indent}        var rm_argument = new RIPAddress64(final_segment, parse_result.get());
        ${indent}        return <@make_instruction_rip_addr64 instruction/>;
        ${indent}    }
        ${indent}    var parse_result = parseGPAddress64(final_segment, final_rex_prefix, it);
        ${indent}    if (parse_result.isEmpty()) {
        ${indent}        return Optional.empty();
        ${indent}    }
        ${indent}    var rm_argument = parse_result.get();
        ${indent}    return <@make_instruction_addr64 instruction/>;
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
        final private ${argument?keep_after(":")?keep_before("/")} arg${argument?index};
        </#list>

        public ${classname(instruction_name, instruction_class.arguments)}(<#list
        instruction_class.arguments as argument>${
            argument?keep_after(":")?keep_before("/")} arg${argument?index}<#sep>, </#sep></#list
        ><#if instruction_class.arguments?size != 0>,</#if> byte[] bytes) {
        <#list instruction_class.arguments as argument>
            <#if argument?contains("/")>
            this.arg${argument?index} = new ${argument?keep_after(":")?keep_before("/")}(arg${
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
                >arg${argument?index}.toArgument()<#sep>, </#sep></#list>};
        }

        <#list instruction_class.arguments as argument>
        public ${argument?keep_after(":")?keep_before("/")} getArg${argument?index}() {
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
<#list [64, 32, 16] as NativeOperandSize>
    <@"<#assign opcode_map = opcode_map_${NativeOperandSize}>"?interpret />
    <@"<#assign prefix_opcode_map = prefix_opcode_map_${NativeOperandSize}>"?interpret />
            Supplier<Optional<Instruction>> parse_${NativeOperandSize}bit_instruction = () -> {
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
            <#if NativeOperandSize == 64>
                <#assign opcode_prefix>${opcode_prefix}rexw </#assign>
            </#if>
            <#assign opcode_prefix>${opcode_prefix}0x</#assign>
                Supplier<Optional<Instruction>> parse_${X66Prefix}_${Xf2Xf3Prefix}_instruction = () -> {
                    switch (opcode) {
            <#list byte_vaues?filter(x -> !element_in_list(x, ["66", "f2", "f3"])) as opcode_value>
                <#if opcode_map[opcode_prefix + opcode_value]?? ||
                     prefix_opcode_map[opcode_prefix + opcode_value]??>
                        case ${value_to_byte(opcode_value)}:
                    <#-- Handle register operant in opcode -->
                    <#if opcode_map[opcode_prefix + opcode_value]??>
                        <#assign instruction = opcode_map[opcode_prefix + opcode_value]>
                        <#if argument_in_opcode(instruction)>
                            <#-- Instructions with operands in opcode would occupy case lines -->
                            <#if opcode_value[1] == '7' || opcode_value[1] == 'f'>
                            {
                                var opcode_argument = <@argument_from_opcode
                                   instruction "opcode" "final_rex_prefix"/>;
                                <#if has_implicit_argument(instruction)>
                                var implicit_argument = <@implicit_argument instruction/>;
                                </#if>
                                return <@make_instruction instruction/>;
                            }
                            <#else>
                            /* fallthrough */
                            </#if>
                        <#else>
                            <#if opcode_prefix + opcode_value == "0x90">
                            // If there are REX.B prefix then 0x90 is not interpreted as NOP, but as Xchg.
                            // Note: objdump erroneously decodes, e.g., 0x40 0x90 as xchg - but that one is actually NOP.
                            if ((final_rex_prefix & 0b0000_0_0_0_1) == 0)
                            </#if>
                            {
                                <#if has_implicit_argument(instruction)>
                                var implicit_argument = <@implicit_argument instruction/>;
                                </#if>
                                return <@make_instruction instruction/>;
                            }
                            <#if opcode_prefix + opcode_value == "0x90">
                            /* fallthrough */
                            </#if>
                        </#if>
                    <#elseif opcode_map[opcode_prefix + opcode_value + " /r"]?? ||
                             opcode_map[opcode_prefix + opcode_value + " /m"]??>
                            {
                                if (!it.hasNext()) {
                                    return Optional.empty();
                                }
                        <#if opcode_map[opcode_prefix + opcode_value + " /r"]??>
                            <#assign instruction = opcode_map[opcode_prefix + opcode_value + " /r"]>
                        <#else>
                            <#assign instruction = opcode_map[opcode_prefix + opcode_value + " /m"]>
                        </#if>
                                var reg_argument = <@reg_argument instruction "it.peek()" "final_rex_prefix"/>;
                        <#if opcode_map[opcode_prefix + opcode_value + " /r"]??>
                                if ((it.peek() & 0b11_000_000) == 0b11_000_000) {
                                    var rm_argument = <@rm_argument instruction "it.next()" "final_rex_prefix"/>;
                                    return <@make_instruction instruction/>;
                                }
                        </#if>
                        <#if opcode_map[opcode_prefix + opcode_value + " /m"]??>
                            <#if opcode_map[opcode_prefix + opcode_value + " /r"]??>
                                <#assign instruction = opcode_map[opcode_prefix + opcode_value + " /m"]>
                                else {
                            <#else>
                                if ((it.peek() & 0b11_000_000) != 0b11_000_000) {
                            </#if>
                                    <@parse_operand_and_return_instruction ""?left_pad(28) NativeOperandSize instruction/>
                                }
                        </#if>
                            }
                    <#elseif prefix_opcode_map[opcode_prefix + opcode_value + " /r"]?? ||
                             prefix_opcode_map[opcode_prefix + opcode_value + " /m"]??>
                            {
                                if (!it.hasNext()) {
                                    return Optional.empty();
                                }
                                byte opcode_extension = (byte)(it.peek() & 0b00_111_000);
                        <#if prefix_opcode_map[opcode_prefix + opcode_value + " /r"]??>
                                if ((it.peek() & 0b11_000_000) == 0b11_000_000) {
                                    switch (opcode_extension) {
                            <#list ["0:000", "1:001", "2:010", "3:011", "4:100", "5:101", "6:110", "7:111"]
                            as opcode_extension>
                                <#assign full_opcode = opcode_prefix + opcode_value + " /r /" + opcode_extension?keep_before(":")>
                                <#if  opcode_map[full_opcode]??>
                                    <#assign instruction = opcode_map[full_opcode]>
                                        case 0b00_${opcode_extension?keep_after(":")}_000 :
                                            {
                                                var rm_argument = <@rm_argument instruction "it.next()" "final_rex_prefix"/>;
                                                return <@make_instruction instruction/>;
                                            }
                                </#if>
                            </#list>
                                        default:
                                            return Optional.empty();
                                    }
                                }
                        </#if>
                        <#if prefix_opcode_map[opcode_prefix + opcode_value + " /m"]??>
                            <#if prefix_opcode_map[opcode_prefix + opcode_value + " /r"]??>
                                else {
                            <#else>
                                if ((it.peek() & 0b11_000_000) != 0b11_000_000) {
                            </#if>
                                    switch (opcode_extension) {
                            <#list ["0:000", "1:001", "2:010", "3:011", "4:100", "5:101", "6:110", "7:111"]
                                   as opcode_extension>
                                <#assign full_opcode = opcode_prefix + opcode_value + " /m /" + opcode_extension?keep_before(":")>
                                    <#if  opcode_map[full_opcode]??>
                                        <#assign instruction = opcode_map[full_opcode]>
                                        case 0b00_${opcode_extension?keep_after(":")}_000:
                                            {
                                                <@parse_operand_and_return_instruction ""?left_pad(40) NativeOperandSize instruction/>
                                            }
                                        </#if>
                            </#list>
                                        default:
                                            return Optional.empty();
                                    }
                                }
                        </#if>
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
        return Optional.of((long)((high_byte << 56L) |
                                  (seventh_byte << 48L) |
                                  (sixth_byte << 40L) |
                                  (fifth_byte << 32L) |
                                  (fourth_byte << 24) |
                                  (third_byte << 16) |
                                  (second_byte << 8) |
                                  low_byte));
    }
}
