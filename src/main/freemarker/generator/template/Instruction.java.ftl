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

<#assign ADDR16_DATA32 = 0>
<#assign ADDR16_DATA16 = 1>
<#assign ADDR32_DATA32 = 2>
<#assign ADDR32_DATA16 = 3>
<#assign ADDR64_DATA32 = 4>
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
<#function normalize_prefixes prefixes opcodes>
    <#if 0 < prefixes?size && prefixes[0] == "">
        <#return normalize_prefixes(prefixes[1..], opcodes)>
    <#elseif 0 < opcodes?size && element_in_list(opcodes[0]?keep_before("?"), ["0x66", "0x67", "0xf2", "0xf3", "rexw"])>
        <#return normalize_prefixes(prefixes + [opcodes[0]], opcodes[1..])>
    <#elseif element_in_list("/p", prefixes)>
        <#return (prefixes?filter(prefix -> prefix != "/p")?sort +
                  opcodes?map(opcode -> opcode?replace("/m", "/p")))?join(" ")>
    <#else>
        <#return (prefixes?sort + opcodes)?join(" ")>
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
<#function far_address_operand arguments>
    <#list arguments as argument>
        <#if element_in_list(argument?keep_after(":"),
                             ["Memory16Memory16", "Memory16Memory32"])>
            <#return true>
        </#if>
    </#list>
    <#return false>
</#function>
<#function rm_register_operand arguments>
    <#list arguments as argument>
        <#if element_in_list(argument, ["Rm:GPRegister8", "Rm:GPRegister16", "Rm:GPRegister32", "Rm:GPRegister64"])>
            <#return true>
        </#if>
    </#list>
    <#return false>
</#function>
<#function x86_64_operand arguments>
    <#list arguments as argument>
        <#if argument?keep_after(":") == "x64AbsMemory" || argument?keep_after(":") == "x64Memory">
            <#return true>
        </#if>
    </#list>
    <#return false>
</#function>
<#function split_opcodes_by_address_size keys names opcode_16_bit_prefix opcode_32_bit_prefix opcode_64_bit_prefix>
    <#if keys?size == 0>
        <#return {}>
    <#else>
        <#if names[keys[0]]?contains("|")>
            <#local opcodes = names[keys[0]]?split("|")>
            <#if opcodes?size == 2>
                <#local opcodes = [opcodes[0], opcodes[1], opcodes[0], opcodes[1], opcodes[0]]>
            <#elseif opcodes?size == 3>
                <#local opcodes = [opcodes[0], opcodes[0], opcodes[1], opcodes[1], opcodes[2]]>
            </#if>
        <#else>
            <#local opcodes = [names[keys[0]], names[keys[0]], names[keys[0]], names[keys[0]], names[keys[0]]]>
        </#if>
        <#local addr16_data32_opcode = "">
        <#local addr16_data16_opcode = "">
        <#if opcode_16_bit_prefix != "SKIP">
            <#if opcodes[ADDR16_DATA32] != "">
                <#local addr16_data32_opcode = normalize_prefixes(
                    opcode_16_bit_prefix?split(" "), opcodes[ADDR16_DATA32]?split(" "))>
            </#if>
            <#if opcodes[ADDR16_DATA16] != "">
                <#local addr16_data16_opcode = normalize_prefixes(
                    opcode_16_bit_prefix?split(" "), opcodes[ADDR16_DATA16]?split(" "))>
            </#if>
        </#if>
        <#local addr32_data32_opcode = "">
        <#local addr32_data16_opcode = "">
        <#if opcode_32_bit_prefix != "SKIP">
            <#if opcodes[ADDR32_DATA32] != "">
                <#local addr32_data32_opcode = normalize_prefixes(
                    opcode_32_bit_prefix?split(" "), opcodes[ADDR32_DATA32]?split(" "))>
            </#if>
            <#if opcodes[ADDR32_DATA16] != "">
                <#local addr32_data16_opcode = normalize_prefixes(
                    opcode_32_bit_prefix?split(" "), opcodes[ADDR32_DATA16]?split(" "))>
            </#if>
        </#if>
        <#local addr64_data32_opcode = "">
        <#if opcode_64_bit_prefix != "SKIP">
            <#if opcodes[ADDR64_DATA32] != "">
                <#local addr64_data32_opcode = normalize_prefixes(
                    opcode_64_bit_prefix?split(" "), opcodes[ADDR64_DATA32]?split(" "))>
            <#elseif opcodes[ADDR32_DATA32] != "">
                <#local addr64_data32_opcode = normalize_prefixes(
                    opcode_64_bit_prefix?split(" "), opcodes[ADDR32_DATA32]?split(" "))>
            </#if>
        </#if>
        <#return
            {keys[0]: addr16_data32_opcode + "|" +
                      addr16_data16_opcode + "|" +
                      addr32_data32_opcode + "|" +
                      addr32_data16_opcode + "|" +
                      addr64_data32_opcode} +
            split_opcodes_by_address_size(keys[1..],
                names,
                opcode_16_bit_prefix,
                opcode_32_bit_prefix,
                opcode_64_bit_prefix)>
    </#if>
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
                    ["RIPAddress64/0", "RIPAddress64/8", "RIPAddress64/16", "RIPAddress64/32", "RIPAddress64/64"]),
                "names": instructions[0].names + split_opcodes_by_address_size(
                    instructions[0].names?keys,
                    instructions[0].names,
                    "SKIP", "SKIP", "/p")}] +
                [instructions[0] + {"arguments": replace_arguments(
                    instructions[0].arguments,
                    ["Memory0", "Memory8", "Memory16", "Memory32", "x64Memory"],
                    ["GPAddress64/0", "GPAddress64/8", "GPAddress64/16", "GPAddress64/32", "GPAddress64/64"]),
                "names": instructions[0].names + split_opcodes_by_address_size(
                    instructions[0].names?keys,
                    instructions[0].names,
                    "SKIP", "SKIP", "")}] +
                [instructions[0] + {"arguments": replace_arguments(
                    instructions[0].arguments,
                    ["Memory0", "Memory8", "Memory16", "Memory32", "x64Memory"],
                    ["EIPAddress32/0", "EIPAddress32/8", "EIPAddress32/16", "EIPAddress32/32", "EIPAddress32/64"]),
                "names": instructions[0].names + split_opcodes_by_address_size(
                    instructions[0].names?keys,
                    instructions[0].names,
                    "SKIP", "SKIP", "0x67 /p")}] +
                [instructions[0] + {"arguments": replace_arguments(
                    instructions[0].arguments,
                    ["Memory0", "Memory8", "Memory16Memory16", "Memory16Memory32", "Memory16", "Memory32", "x64Memory"],
                    ["GPAddress32/0", "GPAddress32/8", "GPAddress32/16+16", "GPAddress32/16+32", "GPAddress32/16", "GPAddress32/32", "GPAddress32/64"]),
                "names": instructions[0].names + split_opcodes_by_address_size(
                    instructions[0].names?keys,
                    instructions[0].names,
                    "0x67", "", "0x67")}]>
            <#if !x86_64_operand(instructions[0].arguments)>
                <#local result = result +
                    [instructions[0] + {"arguments": replace_arguments(
                        instructions[0].arguments,
                        ["Memory0", "Memory8", "Memory16Memory16", "Memory16Memory32", "Memory16", "Memory32"],
                        ["GPAddress16/0", "GPAddress16/8", "GPAddress16/16+16", "GPAddress16/16+32", "GPAddress16/16", "GPAddress16/32"]),
                    "names": instructions[0].names + split_opcodes_by_address_size(
                        instructions[0].names?keys,
                        instructions[0].names,
                        "", "0x67", "SKIP")}]>
            </#if>
            <#return result + expand_address_arguments(instructions[1..])>
        <#elseif far_address_operand(instructions[0].arguments)>
            <#local result =
                [instructions[0] + {"arguments": replace_arguments(
                    instructions[0].arguments,
                    ["Memory16Memory16", "Memory16Memory32"],
                    ["GPAddress32/16+16", "GPAddress32/16+32"]),
                "names": instructions[0].names + split_opcodes_by_address_size(
                    instructions[0].names?keys,
                    instructions[0].names,
                    "0x67", "", "SKIP")}] +
                [instructions[0] + {"arguments": replace_arguments(
                    instructions[0].arguments,
                    ["Memory16Memory16", "Memory16Memory32"],
                    ["GPAddress16/16+16", "GPAddress16/16+32"]),
                "names": instructions[0].names + split_opcodes_by_address_size(
                    instructions[0].names?keys,
                    instructions[0].names,
                    "", "0x67", "SKIP")}]>
            <#return result + expand_address_arguments(instructions[1..])>
        <#elseif absolute_address_operand(instructions[0].arguments)>
            <#local result =
                [instructions[0] + {"arguments": replace_arguments(
                    instructions[0].arguments,
                    ["AbsMemory8", "AbsMemory16", "AbsMemory32", "x64AbsMemory"],
                    ["AbsoluteAddress64/8", "AbsoluteAddress64/16", "AbsoluteAddress64/32", "AbsoluteAddress64/64"]),
                "names": instructions[0].names + split_opcodes_by_address_size(
                    instructions[0].names?keys,
                    instructions[0].names,
                    "SKIP", "SKIP", "")}] +
                [instructions[0] + {"arguments": replace_arguments(
                    instructions[0].arguments,
                    ["AbsMemory8", "AbsMemory16", "AbsMemory32", "x64AbsMemory"],
                    ["GPAddress32/8", "GPAddress32/16", "GPAddress32/32", "GPAddress32/64"]),
                "names": instructions[0].names + split_opcodes_by_address_size(
                    instructions[0].names?keys,
                    instructions[0].names,
                    "0x67", "", "0x67")}]>
            <#if !x86_64_operand(instructions[0].arguments)>
                <#local result = result +
                    [instructions[0] + {"arguments": replace_arguments(
                        instructions[0].arguments,
                        ["AbsMemory8", "AbsMemory16", "AbsMemory32"],
                        ["GPAddress16/8", "GPAddress16/16", "GPAddress16/32"]),
                    "names": instructions[0].names + split_opcodes_by_address_size(
                        instructions[0].names?keys,
                        instructions[0].names,
                    "", "0x67", "SKIP")}]>
            </#if>
            <#return result + expand_address_arguments(instructions[1..])>
        <#elseif rm_register_operand(instructions[0].arguments)>
            <#return [instructions[0] + {"names": split_opcodes_by_address_size(
                                                      instructions[0].names?keys,
                                                      instructions[0].names,
                                                      "0x67", "0x67", "0x67")}] +
                     instructions[0..0] + expand_address_arguments(instructions[1..])>
        <#else>
            <#return instructions[0..0] + expand_address_arguments(instructions[1..])>
        </#if>
    </#if>
</#function>
<#function native_sized_operand arguments>
    <#list arguments as argument>
        <#if element_in_list(argument?keep_after(":"), ["GPRegisterNative", "MemoryNative", "ImmNative", "RelNative"])>
            <#return true>
        </#if>
    </#list>
    <#return false>
</#function>
<#function split_opcodes_by_data_operand_size keys names opcode_16_bit_prefix opcode_32_bit_prefix opcode_64_bit_prefix>
    <#if keys?size == 0>
        <#return {}>
    <#else>
        <#if opcode_16_bit_prefix == "SKIP">
            <#local opcode16 = "">
        <#else>
            <#local opcode16 = normalize_prefixes(opcode_16_bit_prefix?split(" "), names[keys[0]]?split(" "))>
        </#if>
        <#if opcode_32_bit_prefix == "SKIP">
            <#local opcode32 = "">
        <#else>
            <#local opcode32 = normalize_prefixes(opcode_32_bit_prefix?split(" "), names[keys[0]]?split(" "))>
        </#if>
        <#if opcode_64_bit_prefix == "SKIP" ||
             names[keys[0]] == "0x40" ||
             names[keys[0]] == "0x48">
            <#local opcode64 = "">
        <#else>
            <#local opcode64 = normalize_prefixes(opcode_64_bit_prefix?split(" "), names[keys[0]]?split(" "))>
        </#if>
        <#return
            {keys[0]: opcode32 + "|" + opcode16 + "|" + opcode32 + "|" + opcode16 + "|" + opcode64} +
            split_opcodes_by_data_operand_size(keys[1..],
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
                            ["GPRegisterNative", "AbsMemoryNative", "MemoryNative", "ImmNative64", "ImmNative", "RelNative"],
                            ["GPRegister16", "AbsMemory16", "Memory16", "Imm16", "Imm16", "Rel16"]),
                        "names": instructions[0].names + split_opcodes_by_data_operand_size(
                            instructions[0].names?keys,
                            instructions[0].names,
                            "", "0x66", "0x66")},
                    instructions[0] + {
                        "arguments": replace_arguments(
                            instructions[0].arguments,
                            ["GPRegisterNative", "AbsMemoryNative", "MemoryNative", "ImmNative64", "ImmNative", "RelNative"],
                            ["GPRegister32", "AbsMemory32", "Memory32", "Imm32", "Imm32", "Rel32"]),
                        "names": instructions[0].names + split_opcodes_by_data_operand_size(
                            instructions[0].names?keys,
                            instructions[0].names,
                            "0x66", "", "")},
                    instructions[0] + {
                        "arguments": replace_arguments(
                            instructions[0].arguments,
                            ["GPRegisterNative", "AbsMemoryNative", "MemoryNative", "ImmNative64", "ImmNative", "RelNative"],
                            ["GPRegister64", "x64AbsMemory", "x64Memory", "Imm64", "Imm32", "Rel32"]),
                        "names": instructions[0].names + split_opcodes_by_data_operand_size(
                            instructions[0].names?keys,
                            instructions[0].names,
                            "SKIP", "SKIP", "0x66? rexw")}]) +
                expand_native_arguments(instructions[1..])>
        <#else>
            <#return expand_address_arguments(instructions[0..0]) + expand_native_arguments(instructions[1..])>
        </#if>
    </#if>
</#function>
<#function memory_register_operand arguments>
    <#list arguments as argument>
        <#if element_in_list(argument?keep_after(":"),
                             ["GPRegister8/Memory8", "GPRegister16/Memory16", "GPRegister32/Memory32", "GPRegister64/x64Memory", "GPRegisterNative/MemoryNative"])>
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
                            ["GPRegister8/Memory8", "GPRegister16/Memory16", "GPRegister32/Memory32", "GPRegister64/x64Memory", "GPRegisterNative/MemoryNative"],
                            ["GPRegister8", "GPRegister16", "GPRegister32", "GPRegister64", "GPRegisterNative"]),
                        "names": instructions[0].names + regmem_opcodes(
                            instructions[0].names?keys,
                            instructions[0].names, "/r")}] +
                    [instructions[0] + {
                        "arguments": replace_arguments(
                            instructions[0].arguments,
                            ["GPRegister8/Memory8", "GPRegister16/Memory16", "GPRegister32/Memory32", "GPRegister64/x64Memory", "GPRegisterNative/MemoryNative"],
                            ["Memory8", "Memory16", "Memory32", "x64Memory", "MemoryNative"]),
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
        name?keep_before("/")?capitalize?replace(" ", "")}<#list arguments as argument><#if argument_to_class_name[argument?keep_after(":")]??>${
        argument_to_class_name[argument?keep_after(":")]
    }<#else>QQX${argument?keep_after(":")}X</#if></#list></#local>
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
        <#-- combination of 0xf2 and 0xf3 prefixes is never valid-->
        <#if opcode?keep_before("?")?contains("0xf2 0xf3")>
            <#return opcode_optional_prefixes_expand(
                         opcode?keep_before("?")?ensure_starts_with(" ")?keep_before_last(" ") + opcode?keep_after("?"))>
        <#else>
            <#return opcode_optional_prefixes_expand(
                         opcode?keep_before("?")?ensure_starts_with(" ")?keep_before_last(" ") + opcode?keep_after("?")) +
                     opcode_optional_prefixes_expand(
                         opcode?keep_before("?") + opcode?keep_after("?"))>
        </#if>
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
                    <#local split_opcode = instruction_opcode?split("|")>
                    <#if split_opcode?size == 2>
                       <#local split_opcode =
                           [split_opcode[0], split_opcode[1], split_opcode[0], split_opcode[1], split_opcode[0]]>
                    <#elseif split_opcode?size == 3>
                        <#local split_opcode =
                           [split_opcode[0], split_opcode[0], split_opcode[1], split_opcode[1], split_opcode[2]]>
                    </#if>
                    <#local opcode = split_opcode[opcodes_variant]>
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
            " \"0x67 0x90\": { \"name\": \"Nop\", \"arguments\": [] }," +
            " \"0x66 0x67 0x90\": { \"name\": \"Nop\", \"arguments\": [] }," +
            " \"0xf2 0x90\": { \"name\": \"Nop\", \"arguments\": [] }," +
            " \"0x66 0xf2 0x90\": { \"name\": \"Nop\", \"arguments\": [] }," +
            " \"0x67 0xf2 0x90\": { \"name\": \"Nop\", \"arguments\": [] }," +
            " \"0x66 0x67 0xf2 0x90\": { \"name\": \"Nop\", \"arguments\": [] }," +
            " \"0xf3 0x90\": { \"name\": \"Pause\", \"arguments\": [] }," +
            " \"0x66 0xf3 0x90\": { \"name\": \"Pause\", \"arguments\": [] }," +
            " \"0x67 0xf3 0x90\": { \"name\": \"Pause\", \"arguments\": [] }," +
            " \"0x66 0x67 0xf3 0x90\": { \"name\": \"Pause\", \"arguments\": [] }}"
        )?eval>
    <#return opcodes_map_text>
</#function>
<#function prefix_opcode_map opcode_map variable_name_suffix>
    Note: freemarker documentation says quite explicitly: “Note that hash concatenation is not to
          be used for many repeated concatenations, like for adding items to a hash inside a loop”.
    That′s why we periodoclly turn map into a string and then using eval to make a map again.
    We also are doing two passes: first to collect correct values for the result in global variables,
    second to generate the hash. This is done to reduce memory usage. Note: we couldn't use local
    variables here due to peculiarity of freemarker's interpret function.
    <#local prefix_map>
        <#local sep = "">
        <#list opcode_map?keys as opcode>
            <#list 0..<opcode?split(" ")?size-1 as opcode_piece>
                <#local opcode_prefix = opcode?split(" ")[0..opcode_piece]>
                <#local opcode_prefix_name =
                    "opcode_prefix_" + variable_name_suffix + opcode_prefix?join("_")?replace("/", "_")?replace("+", "_")>
                <#local opcode_suffix_name>" ${opcode?split(" ")[opcode_piece+1..]?join(" ")}"</#local>
                ${sep}"${opcode_prefix?join(" ")}" :
                <@(
                    "<#assign ${opcode_prefix_name}>" +
                        "[${opcode_suffix_name}]" +
                        "<#if ${opcode_prefix_name}??>" +
                            r"+ ${" + "${opcode_prefix_name}}" +
                        "</#if>" +
                    "</#assign>" +
                    "true")?interpret />
                    <#local sep = ",">
            </#list>
        </#list>
    </#local>
    <#local prefix_map = "{${prefix_map}}"?eval>
    <#local prefix_map>
        <#list prefix_map?keys as opcode_prefix>
            <#local opcode_prefix_name =
                "opcode_prefix_" + variable_name_suffix + opcode_prefix?replace(" ", "_")?replace("/", "_")?replace("+", "_")>
            <@"<#assign opcode_prefix_data = ${opcode_prefix_name}>"?interpret />
            "${opcode_prefix}" : ${opcode_prefix_data}<#sep>,
        </#list>
    </#local>
    <#return "{${prefix_map}}"?eval>
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
<#assign opcode_maps = [generate_opcodes_map(ADDR16_DATA32, "x64", expanded_instruction_classes_list),
                        generate_opcodes_map(ADDR16_DATA16, "x64", expanded_instruction_classes_list),
                        generate_opcodes_map(ADDR32_DATA32, "x64", expanded_instruction_classes_list),
                        generate_opcodes_map(ADDR32_DATA16, "x64", expanded_instruction_classes_list),
                        generate_opcodes_map(ADDR64_DATA32, "x32", expanded_instruction_classes_list)]>
<#-- We need to know if it makes sense to continue to parse instruction after certain “prefix”
     part. For example PF2IW have opcode “0x0f 0x0f 0x1c” where 0x1c part comes in place of
     immediate. We need to ensure parsing would stop at “0x0f 0x0f”.

     But in some cases prefix is also a valid instruction by itself. E.g. CMPPS have opcode
     “0x0f 0xc2” yet CMPEQPS have opcode “0x0f 0xc2 0x00” and we need to handle both of these. -->
<#assign prefix_opcode_maps = [prefix_opcode_map(opcode_maps[ADDR16_DATA32], "ADDR16_DATA32"),
                               prefix_opcode_map(opcode_maps[ADDR16_DATA16], "ADDR16_DATA16"),
                               prefix_opcode_map(opcode_maps[ADDR32_DATA32], "ADDR32_DATA32"),
                               prefix_opcode_map(opcode_maps[ADDR32_DATA16], "ADDR32_DATA16"),
                               prefix_opcode_map(opcode_maps[ADDR64_DATA32], "ADDR64_DATA32")]>
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
<#macro condition_from_instruction_suffix instruction_suffix>
${{"a":"Condition.Above",
   "ae":"Condition.AboveOrEqual",
   "b":"Condition.Below",
   "be":"Condition.BelowOrEqual",
   "cxz":"Condition.CxZero",
   "e":"Condition.Equal",
   "ecxz":"Condition.EcxZero",
   "g":"Condition.Greater",
   "ge":"Condition.GreaterOrEqual",
   "l":"Condition.Less",
   "le":"Condition.LessOrEqual",
   "mp":"null",
   "ne":"Condition.NotEqual",
   "no":"Condition.NotOverflow",
   "np":"Condition.NotParity",
   "ns":"Condition.NotSign",
   "o":"Condition.Overflow",
   "p":"Condition.Parity",
   "rcxz":"Condition.RcxZero",
   "s":"Condition.Sign"}[instruction_suffix]}</#macro
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
<#macro implicit_address type reg16bit reg32bit reg64bit segment instruction>
    <#list instruction.arguments as argument>
        <#if argument?starts_with(type + "GPAddress16")
            >new GPAddress16(${segment}, null, GPRegister16.${reg16bit}, (short)0)<#elseif
        argument?starts_with(type + "GPAddress32")
            >new GPAddress32(${segment}, null, GPRegister32.${reg32bit}, ScaleFactor.X1, 0)<#elseif
        argument?starts_with(type + "GPAddress64")
            >new GPAddress64(${segment}, null, GPRegister64.${reg64bit}, ScaleFactor.X1, 0)</#if
        ></#list
    ></#macro
>
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
<#macro parse_immediate_address indent instruction>
    <#list instruction.arguments as argument>
        <#if argument?starts_with("Imm0:GPAddress16/")>
${indent}var immediate_argument0 = parseShort(it);
${indent}if (immediate_argument0.isEmpty()) {
${indent}    return Optional.empty();
${indent}}
${indent}var immediate_address = new GPAddress16(final_segment, null, null, immediate_argument0.get());
        <#elseif argument?starts_with("Imm0:GPAddress32/")>
${indent}var immediate_argument0 = parseInteger(it);
${indent}if (immediate_argument0.isEmpty()) {
${indent}    return Optional.empty();
${indent}}
${indent}var immediate_address = new GPAddress32(final_segment, null, null, ScaleFactor.X1, immediate_argument0.get());
        <#elseif argument?starts_with("Imm0:AbsoluteAddress64/")>
${indent}var immediate_argument0 = parseLong(it);
${indent}if (immediate_argument0.isEmpty()) {
${indent}    return Optional.empty();
${indent}}
${indent}var immediate_address = new AbsoluteAddress64(final_segment, immediate_argument0.get());
        </#if>
    </#list>
</#macro>
<#function has_immediate_argument0 instruction>
    <#list instruction.arguments as argument>
        <#if argument?starts_with("Imm0:Imm") || argument?starts_with("Imm0:Rel")>
            <#return true>
        </#if>
    </#list>
    <#return false>
</#function>
<#macro immediate_argument0 instruction>
    <#list instruction.arguments as argument>
        <#if argument?starts_with("Imm0:")
        >${{"Imm8": "parseByte(it)",
            "Imm16": "parseShort(it)",
            "Imm32": "parseInteger(it)",
            "Imm64": "parseLong(it)",
            "Rel8": "parseByte(it)",
            "Rel16": "parseShort(it)",
            "Rel32": "parseInteger(it)"}[argument?keep_after(":")]}</#if
        >
    </#list>
</#macro>
<#function has_immediate_argument1 instruction>
    <#list instruction.arguments as argument>
        <#if argument?starts_with("Imm1:Imm") || argument?starts_with("Imm1:Rel:")>
            <#return true>
        </#if>
    </#list>
    <#return false>
</#function>
<#macro immediate_argument1 instruction>
    <#list instruction.arguments as argument>
        <#if argument?starts_with("Imm1:")
        >${{"Imm8": "parseByte(it)",
            "Imm16": "parseShort(it)",
            "Imm32": "parseInteger(it)",
            "Imm64": "parseLong(it)",
            "Rel8": "parseByte(it)",
            "Rel16": "parseShort(it)",
            "Rel32": "parseInteger(it)"}[argument?keep_after(":")]}</#if
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
<#macro rm_register_argument instruction modrm_var rex_prefix>
    <#list instruction.arguments as argument>
        <#if argument?starts_with("Rm:")
            >${argument?keep_after(":")
                }.of((${modrm_var} & 0b000_000_111) | ((${rex_prefix} & 0b0000_0_0_0_1) << 3)<#if
                argument?keep_after(":") == "GPRegister8">, ${rex_prefix} != 0</#if>)</#if
        >
    </#list>
</#macro>
<#macro rm_memory_argument instruction segment rex_prefix it>
    <#list instruction.arguments as argument>
        <#if argument?starts_with("Rm:GPAddress16/")
            >parseGPAddress16(${segment}, ${it})<#elseif
        argument?starts_with("Rm:GPAddress32/")
            >parseGPAddress32(${segment}, ${rex_prefix}, ${it})<#elseif
        argument?starts_with("Rm:GPAddress64/")
            >parseGPAddress64(${segment}, ${rex_prefix}, ${it})</#if
    >
    </#list>
</#macro>
<#macro parse_immediate_and_implicit_aruments indent instruction>
    <#if has_implicit_argument("AX:", instruction)>
${indent}var implicit_argument_ax = <@implicit_argument "AX:" "AL" "AX" "EAX" "RAX" instruction/>;
    </#if>
    <#if has_implicit_argument("BX:", instruction)>
        ${indent}var implicit_address_bx = <@implicit_address "BX:" "BX" "EBX" "RBX" "final_segment" instruction/>;
    </#if>
    <#if has_implicit_argument("CS:", instruction)>
${indent}var implicit_argument_cs = SegmentRegister.CS;
    </#if>
    <#if has_implicit_argument("CX:", instruction)>
${indent}var implicit_argument_cx = <@implicit_argument "CX:" "CL" "CX" "ECX" "RCX" instruction/>;
    </#if>
    <#if has_implicit_argument("DI:", instruction)>
${indent}var implicit_address_di = <@implicit_address "DI:" "DI" "EDI" "RDI" "final_segment" instruction/>;
    </#if>
    <#if has_implicit_argument("DS:", instruction)>
${indent}var implicit_argument_ds = SegmentRegister.DS;
    </#if>
    <#if has_implicit_argument("DX:", instruction)>
${indent}var implicit_argument_dx = <@implicit_argument "DX:" "DL" "DX" "EDX" "RDX" instruction/>;
    </#if>
    <#if has_implicit_argument("ES:", instruction)>
${indent}var implicit_argument_es = SegmentRegister.ES;
    </#if>
    <#if has_implicit_argument("FS:", instruction)>
${indent}var implicit_argument_fs = SegmentRegister.FS;
    </#if>
    <#if has_implicit_argument("GS:", instruction)>
${indent}var implicit_argument_gs = SegmentRegister.GS;
    </#if>
    <#if has_implicit_argument("SI:", instruction)>
${indent}var implicit_address_si = <@implicit_address "SI:" "SI" "ESI" "RSI" "SegmentRegister.ES" instruction/>;
    </#if>
    <#if has_implicit_argument("SS:", instruction)>
${indent}var implicit_argument_ss = SegmentRegister.SS;
    </#if>
    <#if has_immediate_argument0(instruction)>
${indent}var immediate_argument0 = <@immediate_argument0 instruction/>;
${indent}if (immediate_argument0.isEmpty()) {
${indent}    return Optional.empty();
${indent}}
        <#if has_immediate_argument1(instruction)>
${indent}var immediate_argument1 = <@immediate_argument1 instruction/>;
${indent}if (immediate_argument1.isEmpty()) {
${indent}    return Optional.empty();
${indent}}
        </#if>
    </#if>
</#macro>
<#macro make_instruction instruction>
Optional.of(new ${instruction.name}(<#list instruction.arguments as argument
    ><#if argument?starts_with("Imm0:") && argument?contains("Address")>immediate_address<#elseif
            argument?starts_with("Imm0:") && argument?contains("Rel")
        >new ${argument?keep_after(":")}(immediate_argument0.get())<#else
        >${{"AX": "implicit_argument_ax",
            "BX": "implicit_address_bx",
            "CS": "implicit_argument_cs",
            "CX": "implicit_argument_cx",
            "DI": "implicit_address_di",
            "DS": "implicit_argument_ds",
            "DX": "implicit_argument_dx",
            "ES": "implicit_argument_es",
            "FS": "implicit_argument_fs",
            "GS": "implicit_argument_gs",
            "Imm0": "immediate_argument0.get()",
            "Imm1": "immediate_argument1.get()",
            "Op": "opcode_argument",
            "Reg": "reg_argument",
            "Rm" : "rm_argument",
            "SI": "implicit_address_si",
            "SS": "implicit_argument_ss"}[argument?keep_before(":")]}</#if
        >, </#list
    >toPrimitive(deque.toArray(empty_byte_array))))</#macro
>
<#macro select_insruction_by_immediate indent mode opcode>
    <#if prefix_opcode_maps[mode][opcode]??>
        <#if prefix_opcode_maps[mode][opcode]?size == 1>
            <#local suffix = prefix_opcode_maps[mode][opcode][0]>
${indent}if (immediate_argument0.get() == <@value_to_byte suffix[suffix?length-2..]/>) {
${indent}    return <@make_instruction opcode_maps[mode][opcode + suffix]/>;
${indent}}
        <#else>
${indent}switch (immediate_argument0.get()) {
        <#list prefix_opcode_maps[mode][opcode] as suffix>
${indent}    case <@value_to_byte suffix[suffix?length-2..]/>:
${indent}        return <@make_instruction opcode_maps[mode][opcode + suffix]/>;
        </#list>
${indent}}
        </#if>
   </#if>
</#macro>
<#function adjust_instruction_name prefix_under_test instruction_name>
     <#if prefix_under_test == "addr">
         <#if instruction_name?contains("ecxz")>
             <#return instruction_name?replace("ecxz", "cxz")>
          <#elseif instruction_name?contains("cxz")>
              <#return instruction_name?replace("rcxz", "cxz")?replace("cxz", "ecxz")>
         <#elseif instruction_name?contains("Addr32")>
             <#return instruction_name?replace("Addr32", "Addr16")>
         <#else>
             <#return instruction_name?
                      replace("AbsAddr64", "Addr32")?
                      replace("RIPAddr64", "EIPAddr32")?
                      replace("Addr64", "Addr32")?
                      replace("Addr16", "Addr32")>
         </#if>
     <#elseif prefix_under_test == "oprd">
         <#if instruction_name?contains("Reg32") || instruction_name?contains("Mem32") || instruction_name?contains("Imm16Imm32")>
             <#return instruction_name?
                      replace("Reg32", "Reg16")?
                      replace("Mem32", "Mem16")?
                      replace("Imm32", "Imm16")>
         <#else>
             <#return instruction_name?
                      replace("Reg16", "Reg32")?
                      replace("Mem16", "Mem32")?
                      replace("Imm16", "Imm32")?
                      replace("Imm32Imm32", "Imm16Imm32")>
         </#if>
     <#else>
         <#return instruction_name>
     </#if>
</#function>
<#function adjust_instruction_arguments prefix_under_test instruction_arguments>
    <#if instruction_arguments?size == 0>
         <#return []>
    </#if>
    <#if prefix_under_test == "addr">
        <#if instruction_arguments[0]?contains(":GPAddress32/")>
            <#return [instruction_arguments[0]?replace(":GPAddress32/", ":GPAddress16/")] +
                     adjust_instruction_arguments(prefix_under_test, instruction_arguments[1..])>
        <#else>
            <#return [instruction_arguments[0]?
                      replace(":GPAddress16/", ":GPAddress32/")?
                      replace(":AbsoluteAddress64/", ":GPAddress32/")?
                      replace(":RIPAddress64/", ":EIPAddress32/")?
                      replace(":GPAddress64/", ":GPAddress32/")] +
                     adjust_instruction_arguments(prefix_under_test, instruction_arguments[1..])>
        </#if>
    <#elseif prefix_under_test == "oprd">
        <#if instruction_arguments[0]?ends_with("/32")>
            <#return [instruction_arguments[0]?replace("/32", "/16")] +
                     adjust_instruction_arguments(prefix_under_test, instruction_arguments[1..])>
        <#elseif instruction_arguments[0]?ends_with("/16")>
            <#return [instruction_arguments[0]?replace("/16", "/32")] +
                     adjust_instruction_arguments(prefix_under_test, instruction_arguments[1..])>
        <#elseif instruction_arguments[0]?ends_with(":GPRegister32")>
            <#return [instruction_arguments[0]?replace(":GPRegister32", ":GPRegister16")] +
                     adjust_instruction_arguments(prefix_under_test, instruction_arguments[1..])>
        <#elseif instruction_arguments[0]?ends_with(":GPRegister16")>
            <#return [instruction_arguments[0]?replace(":GPRegister16", ":GPRegister32")] +
                     adjust_instruction_arguments(prefix_under_test, instruction_arguments[1..])>
        <#elseif instruction_arguments[0]?ends_with(":Imm32")>
            <#return [instruction_arguments[0]?replace(":Imm32", ":Imm16")] +
                     adjust_instruction_arguments(prefix_under_test, instruction_arguments[1..])>
        <#elseif instruction_arguments[0]?ends_with(":Imm16")>
            <#if 1 < instruction_arguments?size && instruction_arguments[1]?ends_with(":Imm32")>
                <#return instruction_arguments[0..0] + [instruction_arguments[1]?replace(":Imm32", ":Imm16")] +
                         adjust_instruction_arguments(prefix_under_test, instruction_arguments[2..])>
            <#elseif 1 < instruction_arguments?size && instruction_arguments[1]?ends_with(":Imm16")>
                <#return instruction_arguments[0..0] + [instruction_arguments[1]?replace(":Imm16", ":Imm32")] +
                adjust_instruction_arguments(prefix_under_test, instruction_arguments[2..])>
            </#if>
            <#return [instruction_arguments[0]?replace(":Imm16", ":Imm32")] +
                     adjust_instruction_arguments(prefix_under_test, instruction_arguments[1..])>
        <#else>
            <#return instruction_arguments[0..0] + adjust_instruction_arguments(prefix_under_test, instruction_arguments[1..])>
        </#if>
    <#else>
        <#return instruction_arguments>
    </#if>
</#function>
<#function prefix_is_affecting_encoding modes opcode prefix_under_test must_be_present_prefixes must_be_absent_prefixes>
    <#local byte_prefix_under_test = {
        "": "",
        "addr": "0x67",
        "oprd": "0x66",
        "0x66": "0x66",
        "0x67": "0x67",
        "0xf2": "0xf2",
        "0xf3": "0xf3",
        "rexw": "rexw"
    }[prefix_under_test]>
    <#local prefixes_list =
        ["0x66 ", "0x67 ", "0x66 0x67 ",
         "0xf2 ", "0x66 0xf2 ", "0x67 0xf2 ", "0x66 0x67 0xf2 ",
         "0xf3 ", "0x66 0xf3 ", "0x67 0xf3 ", "0x66 0x67 0xf3 ",
         "rexw ", "0x66 rexw ", "0x67 rexw ", "0x66 0x67 rexw ",
         "0xf2 rexw ", "0x66 0xf2 rexw ", "0x67 0xf2 rexw ", "0x66 0x67 0xf2 rexw ",
         "0xf3 rexw ", "0x66 0xf3 rexw ", "0x67 0xf3 rexw ", "0x66 0x67 0xf3 rexw "]>
    <#list must_be_present_prefixes as must_be_present_prefix>
        <#local prefixes_list = prefixes_list?filter(x -> x?contains(must_be_present_prefix + " "))>
    </#list>
    <#list must_be_absent_prefixes as must_be_absent_prefix>
        <#local prefixes_list = prefixes_list?filter(x -> !x?contains(must_be_absent_prefix + " "))>
    </#list>
    <#local suffixes_list = [""]>
    <#list prefixes_list as prefixes>
        <#list modes as mode>
            <#if prefix_opcode_maps[mode][prefixes + opcode]??>
                <#local suffixes_list = suffixes_list + prefix_opcode_maps[mode][prefixes + opcode]>
            </#if>
        </#list>
    </#list>
    <#local suffixes_list = sort_and_remove_duplicates(suffixes_list)>
    <#local prefixes_list = prefixes_list?filter(x -> x?contains(byte_prefix_under_test + " "))>
    <#list sort_and_remove_duplicates(suffixes_list) as suffixes>
        <#list prefixes_list as prefixes>
            <#list modes as mode>
                <#local opcodes_with_prefix = prefixes + opcode + suffixes>
                <#if prefix_under_test == "">
                    <#local opcodes_no_prefix = opcodes_with_prefix>
                <#else>
                    <#local opcodes_no_prefix = prefixes?replace(byte_prefix_under_test + " ", "") + opcode + suffixes>
                </#if>
                <#if opcode_maps[mode][opcodes_with_prefix]?? != opcode_maps[mode][opcodes_no_prefix]??>
                    <#return true>
                </#if>
                <#if opcode_maps[mode][opcodes_with_prefix]??>
                    <#if opcode_maps[mode][opcodes_with_prefix].name !=
                             adjust_instruction_name(prefix_under_test, opcode_maps[mode][opcodes_no_prefix].name) ||
                         !same_argument_lists(opcode_maps[mode][opcodes_with_prefix].arguments,
                                              adjust_instruction_arguments(
                                                  prefix_under_test, opcode_maps[mode][opcodes_no_prefix].arguments))>
                            <#return true>
                    </#if>
                </#if>
                <#-- 2nd row: RIP/EIP-addressing is unique x86-64 feature, just ignore these for now -->
                <#if (prefix_under_test == "" || prefix_under_test == "addr" || prefix_under_test == "oprd") &&
                     (mode != ADDR64_DATA32 || !opcodes_with_prefix?contains("/p"))>
                    <#if opcode_maps[mode][opcodes_with_prefix]?? != opcode_maps[modes[0]][opcodes_with_prefix]??>
                        <#return true>
                    </#if>
                    <#if opcode_maps[mode][opcodes_no_prefix]?? != opcode_maps[modes[0]][opcodes_no_prefix]??>
                        <#return true>
                    </#if>
                    <#if opcode_maps[mode][opcodes_with_prefix]??>
                        <#if prefix_under_test == "addr">
                            <#-- Only verify inter-mode settings if "canonical" modes 2/3 are included.
                                 We only use 0..3 and 4..4 cases, thus it's enough. -->
                            <#if element_in_list(ADDR32_DATA32, modes) && element_in_list(ADDR32_DATA16, modes)>
                                <#if ((mode / 2) % 10) != 1>
                                    <#if opcode_maps[mode][opcodes_with_prefix].name !=
                                             opcode_maps[ADDR32_DATA32 + mode % 2][opcodes_no_prefix].name ||
                                         !same_argument_lists(opcode_maps[mode][opcodes_with_prefix].arguments,
                                                              opcode_maps[ADDR32_DATA32 + mode % 2][opcodes_no_prefix].arguments)>
                                        <#return true>
                                    </#if>
                                <#else>
                                    <#if opcode_maps[mode][opcodes_with_prefix].name !=
                                             opcode_maps[ADDR32_DATA32 + mode % 2][opcodes_with_prefix].name ||
                                         !same_argument_lists(opcode_maps[mode][opcodes_with_prefix].arguments,
                                                              opcode_maps[ADDR32_DATA32 + mode % 2][opcodes_with_prefix].arguments)>
                                        <#return true>
                                    </#if>
                                </#if>
                            </#if>
                        <#elseif prefix_under_test == "oprd">
                            <#if mode == modes[0] + 1>
                                <#if opcode_maps[modes[0]][opcodes_no_prefix].name !=
                                         opcode_maps[mode][opcodes_with_prefix].name ||
                                     !same_argument_lists(opcode_maps[modes[0]][opcodes_no_prefix].arguments,
                                         opcode_maps[mode][opcodes_with_prefix].arguments)>
                                    <#return true>
                                </#if>
                            <#elseif mode == modes[0] + 2>
                                <#if opcode_maps[modes[0]][opcodes_with_prefix].name !=
                                         adjust_instruction_name("addr", opcode_maps[mode][opcodes_with_prefix].name) ||
                                     !same_argument_lists(opcode_maps[modes[0]][opcodes_with_prefix].arguments,
                                         adjust_instruction_arguments("addr", opcode_maps[mode][opcodes_with_prefix].arguments))>
                                    <#return true>
                                </#if>
                            <#elseif mode == modes[0] + 3>
                                <#if opcode_maps[modes[0]][opcodes_no_prefix].name !=
                                         adjust_instruction_name("addr", opcode_maps[mode][opcodes_with_prefix].name) ||
                                     !same_argument_lists(opcode_maps[modes[0]][opcodes_no_prefix].arguments,
                                         adjust_instruction_arguments("addr", opcode_maps[mode][opcodes_with_prefix].arguments))>
                                    <#return true>
                                </#if>
                            <#elseif mode == modes[0] + 4>
                                <#if adjust_instruction_name("addr", opcode_maps[modes[0]][opcodes_with_prefix].name) !=
                                         adjust_instruction_name("addr", opcode_maps[mode][opcodes_with_prefix].name) ||
                                     !same_argument_lists(
                                         adjust_instruction_arguments("addr", opcode_maps[modes[0]][opcodes_with_prefix].arguments),
                                         adjust_instruction_arguments("addr", opcode_maps[mode][opcodes_with_prefix].arguments))>
                                    <#return true>
                                </#if>
                            </#if>
                        <#else><#-- prefix_under_test == "" -->
                            <#if mode == ADDR64_DATA32>
                                <#-- 64bit mode differs in address size from any other mode thus we are comparing it to
                                     ADDR32_DATA32 with changed address size -->
                                <#if opcode_maps[modes[0]][opcodes_with_prefix].name !=
                                         adjust_instruction_name("addr", opcode_maps[4][opcodes_with_prefix].name) ||
                                     !same_argument_lists(opcode_maps[modes[0]][opcodes_with_prefix].arguments,
                                         adjust_instruction_arguments("addr", opcode_maps[4][opcodes_with_prefix].arguments))>
                                    <#return true>
                                </#if>
                            <#else>
                                <#if opcode_maps[modes[0]][opcodes_with_prefix].name !=
                                         opcode_maps[mode][opcodes_with_prefix].name ||
                                     !same_argument_lists(opcode_maps[modes[0]][opcodes_with_prefix].arguments,
                                                          opcode_maps[mode][opcodes_with_prefix].arguments)>
                                    <#return true>
                                </#if>
                            </#if>
                        </#if> 
                    </#if>
                </#if>
            </#list>
        </#list>
    </#list>
    <#return false>
</#function>
<#macro mode_selection indent, modes, prefixes_and_opcode, x66_prefix_flavor, x67_prefix_flavor>
    <#if x66_prefix_flavor == "selector" && x67_prefix_flavor == "selector">
        <#local mode_to_modestring = [
            "mode == Mode.ADDR16_DATA32",
            "mode == Mode.ADDR16_DATA16",
            "mode == Mode.ADDR32_DATA32",
            "mode == Mode.ADDR16_DATA32",
            "mode == Mode.ADDR64_DATA32"
        ]>
    <#elseif x66_prefix_flavor == "selector" && x67_prefix_flavor == "address">
        <#local mode_to_modestring = [
            "Mode.operandSize(mode) == 32 && (Mode.addressSize(mode) == 16 ^ final_x67_prefix)",
            "Mode.operandSize(mode) == 16 && (Mode.addressSize(mode) == 16 ^ final_x67_prefix)",
            "Mode.operandSize(mode) == 32 && (Mode.addressSize(mode) == 32 ^ final_x67_prefix)",
            "Mode.operandSize(mode) == 16 && (Mode.addressSize(mode) == 32 ^ final_x67_prefix)",
            "Mode.operandSize(mode) == 32 && (Mode.addressSize(mode) == 64 ^ final_x67_prefix)"
        ]>
    <#elseif x66_prefix_flavor == "selector" && x67_prefix_flavor == "ignored">
        <#local mode_to_modestring = [
            "Mode.operandSize(mode) == 32",
            "Mode.operandSize(mode) == 16",
            "Mode.operandSize(mode) == 32",
            "Mode.operandSize(mode) == 16",
            "Mode.operandSize(mode) == 32"
        ]>
    <#elseif x66_prefix_flavor == "operand" && x67_prefix_flavor == "address">
        <#local mode_to_modestring = [
            "(Mode.operandSize(mode) == 32 ^ final_x66_prefix) && (Mode.addressSize(mode) == 16 ^ final_x67_prefix)",
            "(Mode.operandSize(mode) == 16 ^ final_x66_prefix) && (Mode.addressSize(mode) == 16 ^ final_x67_prefix)",
            "(Mode.operandSize(mode) == 32 ^ final_x66_prefix) && (Mode.addressSize(mode) == 32 ^ final_x67_prefix)",
            "(Mode.operandSize(mode) == 16 ^ final_x66_prefix) && (Mode.addressSize(mode) == 32 ^ final_x67_prefix)",
            "(Mode.operandSize(mode) == 32 ^ final_x66_prefix) && (Mode.addressSize(mode) == 64 ^ final_x67_prefix)"
        ]>
    <#elseif x66_prefix_flavor == "operand" && x67_prefix_flavor == "selector">
        <#local mode_to_modestring = [
        "(Mode.operandSize(mode) == 32 ^ final_x66_prefix) && Mode.addressSize(mode) == 16",
        "(Mode.operandSize(mode) == 16 ^ final_x66_prefix) && Mode.addressSize(mode) == 16",
        "(Mode.operandSize(mode) == 32 ^ final_x66_prefix) && Mode.addressSize(mode) == 32",
        "(Mode.operandSize(mode) == 16 ^ final_x66_prefix) && Mode.addressSize(mode) == 32",
        "(Mode.operandSize(mode) == 32 ^ final_x66_prefix) && Mode.addressSize(mode) == 64"
        ]>
    <#elseif x66_prefix_flavor == "operand" && x67_prefix_flavor == "ignored">
        <#local mode_to_modestring = [
            "Mode.operandSize(mode) == 32 ^ final_x66_prefix",
            "Mode.operandSize(mode) == 16 ^ final_x66_prefix",
            "Mode.operandSize(mode) == 32 ^ final_x66_prefix",
            "Mode.operandSize(mode) == 16 ^ final_x66_prefix",
            "Mode.operandSize(mode) == 32 ^ final_x66_prefix"
        ]>
    <#elseif x66_prefix_flavor == "ignored" && x67_prefix_flavor == "address">
        <#local mode_to_modestring = [
            "Mode.addressSize(mode) == 16 ^ final_x67_prefix",
            "Mode.addressSize(mode) == 16 ^ final_x67_prefix",
            "Mode.addressSize(mode) == 32 ^ final_x67_prefix",
            "Mode.addressSize(mode) == 32 ^ final_x67_prefix",
            "Mode.addressSize(mode) == 64 ^ final_x67_prefix"
        ]>
    <#elseif x66_prefix_flavor == "ignored" && x67_prefix_flavor == "selector">
        <#local mode_to_modestring = [
            "Mode.addressSize(mode) == 16",
            "Mode.addressSize(mode) == 16",
            "Mode.addressSize(mode) == 32",
            "Mode.addressSize(mode) == 32",
            "Mode.addressSize(mode) == 64"
        ]>
    </#if>
    <#if modes?size == 1>
        <#if modes[0] == ADDR64_DATA32 && x66_prefix_flavor == "operand" && x67_prefix_flavor == "address">
${indent}if (final_x66_prefix) {
${indent}    if (final_x67_prefix) {
<#nested indent + "        " modes[0] "0x66 0x67 " + prefixes_and_opcode>
${indent}    } else {
<#nested indent + "        " modes[0] "0x66 " + prefixes_and_opcode>
${indent}    }
${indent}} else {
${indent}    if (final_x67_prefix) {
<#nested indent + "        " modes[0] "0x67 " + prefixes_and_opcode>
${indent}    } else {
${indent}    }
<#nested indent + "        " modes[0] prefixes_and_opcode>
${indent}}
        <#elseif modes[0] == ADDR64_DATA32 && x66_prefix_flavor == "ignored" && x67_prefix_flavor == "address">
${indent}if (final_x67_prefix) {
<#nested indent + "    " modes[0] "0x67 " + prefixes_and_opcode>
${indent}} else {
<#nested indent + "    " modes[0] prefixes_and_opcode>
${indent}}
        <#elseif modes[0] == ADDR64_DATA32 && x66_prefix_flavor == "operand" && x67_prefix_flavor == "ignored">
${indent}if (final_x66_prefix) {
<#nested indent + "    " modes[0] "0x66 " + prefixes_and_opcode>
${indent}} else {
<#nested indent + "    " modes[0] prefixes_and_opcode>
${indent}}
        <#else>
<#nested indent modes[0] prefixes_and_opcode>
        </#if>
    <#else>
        <#local first_case = true>
        <#list modes as mode>
            <#if first_case>
${indent}if (${mode_to_modestring[mode]}) {
                <#local first_case = false>
            <#elseif mode?has_next>
${indent}} else if (${mode_to_modestring[mode]}) {
            <#else>
${indent}} else {
            </#if>
<#nested indent + "    " mode prefixes_and_opcode>
        </#list>
${indent}}
    </#if>
</#macro>
<#macro parse_instruction_info_after_opcode indent, modes, prefixes_and_opcode, x66_prefix_flavor, x67_prefix_flavor>
    <#local reg_mode = false>
    <#local memory_mode = false>
    <#local no_modrm_mode = false>
    <#list modes as mode>
        <#if opcode_maps[mode][prefixes_and_opcode]??>
            <#local no_modrm_mode = true>
        </#if>
        <#if opcode_maps[mode][prefixes_and_opcode + " /r"]?? || prefix_opcode_maps[mode][prefixes_and_opcode + " /r"]??>
            <#local reg_mode = true>
        </#if>
        <#if opcode_maps[mode][prefixes_and_opcode + " /m"]?? || prefix_opcode_maps[mode][prefixes_and_opcode + " /m"]??>
            <#local memory_mode = true>
        </#if>
    </#list>
    <#local active_modes_memory = modes?filter(mode -> (mode != ADDR16_DATA16 || x66_prefix_flavor != "ignored") &&
                                                       (mode != ADDR32_DATA16 || x66_prefix_flavor != "ignored") &&
                                                       (mode < ADDR32_DATA32 || x67_prefix_flavor != "ignored"))>
    <#local active_modes_no_memory = active_modes_memory?filter(mode -> mode < ADDR32_DATA32 || x67_prefix_flavor != "address")>
    <#if active_modes_memory?size == 0>
        <#if modes?size == 1 && modes[0] == ADDR64_DATA32>
            <#local active_modes_memory = [ADDR64_DATA32]>
        <#else>
            ${["Mode selection error"] + 1}
        </#if>
    </#if>
    <#if active_modes_no_memory?size == 0>
        <#if modes?size == 1 && modes[0] == ADDR64_DATA32>
            <#local active_modes_no_memory = [ADDR64_DATA32]>
        <#else>
            ${["Mode selection error"] + 1}
        </#if>
    </#if>
    <#if no_modrm_mode>
        <@mode_selection indent active_modes_memory prefixes_and_opcode x66_prefix_flavor x67_prefix_flavor ;
                         indent, mode, prefixes_and_opcode>
            <#local instruction = opcode_maps[mode][prefixes_and_opcode]>
            <#if argument_in_opcode(instruction)>
${indent}var opcode_argument = <@argument_from_opcode instruction "opcode" "final_rex_prefix"/>;
                <@parse_immediate_and_implicit_aruments indent instruction/>
${indent}return <@make_instruction instruction/>;
            <#else>
                <#if has_immediate_address(instruction)>
                    <#local instruction = opcode_maps[mode][prefixes_and_opcode]>
                    <@parse_immediate_and_implicit_aruments indent instruction/>
                    <@parse_immediate_address indent instruction/>
${indent}return <@make_instruction instruction/>;
                <#else>
                <#-- Nop is unique instruction where REW.B affects encoding: if there are REX.B prefix then 0x90 is not
                     interpreted as NOP, but as Xchg. Note: objdump erroneously decodes, e.g., 0x40 0x90 as xchg - but that
                     one is actually NOP. -->
                    <#if prefixes_and_opcode?ends_with("0x90")>
${indent}if ((final_rex_prefix & 0b0000_0_0_0_1) == 0) {
${indent}    return <@make_instruction instruction/>;
${indent}} else {
                        <@parse_instruction_info_after_opcode
                            indent + "    " modes prefixes_and_opcode?replace("0x90", "0x97") "operand" "ignored"/>
${indent}}
                    <#else>
                        <@parse_immediate_and_implicit_aruments indent instruction/>
                        <@select_insruction_by_immediate indent mode prefixes_and_opcode/>
${indent}return <@make_instruction instruction/>;
                    </#if>
                </#if>
            </#if>
        </@mode_selection>
    <#else>
        <#if reg_mode>
            <#local suffixes = [" /r"]>
        <#else>
            <#local suffixes = []>
        </#if>
        <#if memory_mode>
            <#local suffixes = suffixes + [" /m"]>
        </#if>
        <#list suffixes as suffix>
            <#local x67_prefix_flavor_version = x67_prefix_flavor>
            <#if suffix == " /r">
                <#local active_modes = active_modes_no_memory>
                <#if x67_prefix_flavor == "address">
                    <#local x67_prefix_flavor_version = "ignored">
                </#if>
                <#if suffixes?size == 2>
                    <#local indent = indent + "    ">
${indent[0..<indent?length-4]}if ((it.peek() & 0b11_000_000) == 0b11_000_000) {
               <#else>
${indent}if ((it.peek() & 0b11_000_000) != 0b11_000_000) {
${indent}    return Optional.empty();
${indent}}
               </#if>
            <#else><#-- suffix == " /m"-->
                <#local active_modes = active_modes_memory>
                <#if suffixes?size == 2>
${indent[0..<indent?length-4]}} else {
                <#else>
${indent}if ((it.peek() & 0b11_000_000) == 0b11_000_000) {
${indent}    return Optional.empty();
${indent}}
                </#if>
            </#if>
            <@mode_selection indent active_modes prefixes_and_opcode + suffix x66_prefix_flavor x67_prefix_flavor_version ;
                             indent, mode, prefixes_and_opcode>
                <#if prefix_opcode_maps[mode][prefixes_and_opcode]??>
                    <#local extensions = sort_and_remove_duplicates(prefix_opcode_maps[mode][prefixes_and_opcode])>
${indent}byte opcode_extension = (byte)(it.peek() & 0b00_111_000);
${indent}switch (opcode_extension) {
                    <#local indеnt = indent + "        ">
                <#else>
                    <#local indеnt = indent>
                    <#local extensions = [""]>
                </#if>
                <#list extensions as extension>
                    <#local instruction = opcode_maps[mode][prefixes_and_opcode + extension]>
                    <#if 1 < extensions?size || extensions[0] != "">
${indеnt[0..<indеnt?length-4]}case 0b00_${{"/0": "000",
                                           "/1": "001",
                                           "/2": "010",
                                           "/3": "011",
                                           "/4": "100",
                                           "/5": "101",
                                           "/6": "110",
                                           "/7": "111"}[extension[1..]?keep_before(" ")]}_000: {
                    <#else>
${indеnt}var reg_argument = <@reg_argument instruction "it.peek()" "final_rex_prefix"/>;
                    </#if>
                    <#if suffix == " /m" && 1 < mode && element_in_list(4, active_modes)>
${indеnt}if (mode == Mode.ADDR64_DATA32 && (it.peek() & 0b11_000_111) == 0b00_000_101) {
${indеnt}    it.next();
${indеnt}    var parse_result = parseInteger(it);
${indеnt}    if (parse_result.isEmpty()) {
${indеnt}        return Optional.empty();
${indеnt}    }
                        <#if mode == ADDR32_DATA32>
                            <#local xip_instruction =
                                opcode_maps[4][normalize_prefixes(["0x67", "/p"], (prefixes_and_opcode + extension)?split(" "))]>
${indеnt}    var rm_argument = new EIPAddress32(final_segment, parse_result.get());
                        <#elseif mode == ADDR32_DATA16>
                            <#local xip_instruction =
                                opcode_maps[4][normalize_prefixes(["0x66", "0x67", "/p"], (prefixes_and_opcode + extension)?split(" "))]>
${indеnt}    var rm_argument = new EIPAddress32(final_segment, parse_result.get());
                        <#else>
                            <#local xip_instruction =
                                opcode_maps[mode][normalize_prefixes(["/p"], (prefixes_and_opcode + extension)?split(" "))]>
                            <#if prefixes_and_opcode?contains("0x67 ")>
${indеnt}    var rm_argument = new EIPAddress32(final_segment, parse_result.get());
                            <#else>
${indеnt}    var rm_argument = new RIPAddress64(final_segment, parse_result.get());
                            </#if>
                        </#if>
                        <@parse_immediate_and_implicit_aruments indеnt + "    " xip_instruction/>
                        <@select_insruction_by_immediate indеnt + "    " mode prefixes_and_opcode + extension/>
${indеnt}    return <@make_instruction xip_instruction/>;
${indеnt}}
                    </#if>
                    <#if suffix == " /m">
${indеnt}var parse_result = <@rm_memory_argument instruction "final_segment" "final_rex_prefix" "it"/>;
${indеnt}if (parse_result.isEmpty()) {
${indеnt}    return Optional.empty();
${indеnt}}
${indеnt}var rm_argument = parse_result.get();
                    <#else>
${indеnt}var rm_argument = <@rm_register_argument instruction "it.next()" "final_rex_prefix"/>;
                    </#if>
                    <@parse_immediate_and_implicit_aruments indеnt instruction/>
                    <@select_insruction_by_immediate indеnt mode prefixes_and_opcode + extension/>
${indеnt}return <@make_instruction instruction/>;
                    <#if 1 < extensions?size || extensions[0] != "">
${indеnt[0..<indеnt?length-4]}}
                    </#if>
                </#list>
                <#if 1 < extensions?size || extensions[0] != "">
                    <#local indent = indеnt[0..<indеnt?length-8]>
${indent}    default:
${indent}        return Optional.empty();
${indent}}
                </#if>
            </@mode_selection>
        </#list>
                <#if suffixes?size == 2>
${indent[0..<indent?length-4]}}
        </#if>
    </#if>
</#macro>
<#macro select_instruction_form indent modes opcode>
    <#if element_in_list(4, modes) && prefix_is_affecting_encoding(4..4, opcode, "rexw", [], [])>
        <#local rexw_prefixes_list = [[["rexw"], []], [[], ["rexw"]]]>
        <#local indent = indent + "    ">
    <#else>
        <#local rexw_prefixes_list = [[[], ["rexw"]]]>
    </#if>
    <#list rexw_prefixes_list as rexw_prefixes>
        <#local norexw_modes = modes>
        <#if 1 < rexw_prefixes_list?size>
            <#if 0 < rexw_prefixes[0]?size>
                <#local norexw_modes = 4..4>
${indent}if ((final_rex_prefix & 0b000_1_0_0_0) != 0) {
            </#if>
            <#if 0 < rexw_prefixes[1]?size>
${indent}} else {
            </#if>
        </#if>
        <#if prefix_is_affecting_encoding(norexw_modes, opcode, "0xf2" rexw_prefixes[0], rexw_prefixes[1]) ||
             prefix_is_affecting_encoding(norexw_modes, opcode, "0xf3" rexw_prefixes[0], rexw_prefixes[1])>
            <#local f2f3_prefixes_list = [[["0xf3"] + rexw_prefixes[0], ["0xf2"] + rexw_prefixes[1]],
                                          [["0xf2"] + rexw_prefixes[0], ["0xf3"] + rexw_prefixes[1]],
                                          [rexw_prefixes[0], ["0xf2", "0xf3"] + rexw_prefixes[1]]]>
            <#local indent = indent + "    ">
        <#else>
            <#local f2f3_prefixes_list = [[rexw_prefixes[0], ["0xf2", "0xf3"] + rexw_prefixes[1]]]>
        </#if>
        <#list f2f3_prefixes_list as f2f3_prefixes>
            <#if 1 < f2f3_prefixes_list?size>
                <#if 0 < f2f3_prefixes[1]?size && f2f3_prefixes[1][0] == "0xf2">
                    <#if f2f3_prefixes[1]?size < 2 || f2f3_prefixes[1][1] != "0xf3">
${indent}if (final_xf2_xf3_prefix == (byte)0xf3) {
                    <#else>
${indent}} else {
                    </#if>
                <#else>
${indent}} else if (final_xf2_xf3_prefix == (byte)0xf2) {
                </#if>
            </#if>
            <#if prefix_is_affecting_encoding(norexw_modes, opcode, "0x67", f2f3_prefixes[0], f2f3_prefixes[1])>
                <#if prefix_is_affecting_encoding(norexw_modes, opcode, "addr", f2f3_prefixes[0], f2f3_prefixes[1])>
                    <#local x67_prefix_flavor = "selector">
                <#else>
                    <#local x67_prefix_flavor = "address">
                    <#if prefix_is_affecting_encoding(norexw_modes, opcode + " /r", "addr", f2f3_prefixes[0], f2f3_prefixes[1])>
                        ${["Address opcode affects non-memory configuration"] + 0}
                    </#if>
                </#if>
            <#else>
               <#-- Verfiy that if prefix acts as address size prefix then it moves instructions between address
                    modes in the predictable way.  X86 doesn't have instructions where it doesn't work thus we
                    don't know how to handle these. -->
               <#if element_in_list(ADDR16_DATA32, norexw_modes) &&
                    element_in_list(ADDR32_DATA32, norexw_modes) &&
                     prefix_is_affecting_encoding([ADDR16_DATA32, ADDR32_DATA32], opcode, "", f2f3_prefixes[0], f2f3_prefixes[1])>
                   ${["Not supported combination of modes"] + 0}
               </#if>
                <#if element_in_list(ADDR16_DATA16, norexw_modes) &&
                     element_in_list(ADDR32_DATA16, norexw_modes) &&
                     prefix_is_affecting_encoding([ADDR16_DATA16, ADDR32_DATA16], opcode, "", f2f3_prefixes[0], f2f3_prefixes[1])>
                    ${["Not supported combination of modes"] + 0}
                </#if>
                <#if element_in_list(ADDR32_DATA32, norexw_modes) &&
                     element_in_list(ADDR64_DATA32, norexw_modes) &&
                     prefix_is_affecting_encoding([ADDR32_DATA32, ADDR64_DATA32], opcode, "", f2f3_prefixes[0], f2f3_prefixes[1])>
                    ${["Not supported combination of modes"] + 0}
                </#if>
                <#local x67_prefix_flavor = "ignored">
            </#if>
            <#if x67_prefix_flavor == "selector">
                <#local x67_prefixes_list = [[["0x67"] + f2f3_prefixes[0], f2f3_prefixes[1]],
                                             [f2f3_prefixes[0], ["0x67"] + f2f3_prefixes[1]]]>
                <#local indent = indent + "    ">
            <#else>
                <#local x67_prefixes_list = [[f2f3_prefixes[0],  ["0x67"] + f2f3_prefixes[1]]]>
            </#if>
            <#list x67_prefixes_list as x67_prefixes>
                <#if 1 < x67_prefixes_list?size>
                    <#if element_in_list("0x67", x67_prefixes[0])>
${indent}if (final_x67_prefix) {
                    <#else>
${indent}} else {
                    </#if>
                </#if>
                <#if prefix_is_affecting_encoding(norexw_modes, opcode, "0x66", x67_prefixes[0], x67_prefixes[1])>
                    <#if prefix_is_affecting_encoding(norexw_modes, opcode, "oprd", x67_prefixes[0], x67_prefixes[1])>
                        <#local x66_prefix_flavor = "selector">
                    <#else>
                        <#local x66_prefix_flavor = "operand">
                    </#if>
                <#else>
                    <#-- Verfiy that if prefix acts as address size prefix then it moves instructions between address
                         modes in the predictable way.  X86 doesn't have instructions where it doesn't work thus we
                         don't know how to handle these. -->
                    <#if element_in_list(ADDR16_DATA32, norexw_modes) &&
                         element_in_list(ADDR16_DATA16, norexw_modes) &&
                         prefix_is_affecting_encoding([ADDR16_DATA32, ADDR16_DATA16], opcode, "", x67_prefixes[0], x67_prefixes[1])>
                        ${["Not supported combination of modes"] + 0}
                    </#if>
                    <#if element_in_list(ADDR32_DATA32, norexw_modes) &&
                         element_in_list(ADDR32_DATA16, norexw_modes) &&
                         prefix_is_affecting_encoding([ADDR32_DATA32, ADDR32_DATA16], opcode, "", x67_prefixes[0], x67_prefixes[1])>
                        ${["Not supported combination of modes"] + 0}
                    </#if>
                    <#if element_in_list(ADDR32_DATA32, norexw_modes) &&
                         element_in_list(ADDR64_DATA32, norexw_modes) &&
                         prefix_is_affecting_encoding([ADDR32_DATA32, ADDR64_DATA32], opcode, "", x67_prefixes[0], x67_prefixes[1])>
                        <#list x67_prefixes[1] as xx>${xx}</#list>
                        ${["Not supported combination of modes"] + 0}
                    </#if>
                    <#local x66_prefix_flavor = "ignored">
                </#if>
                <#if x66_prefix_flavor == "selector">
                    <#local x66_prefixes_list = [[["0x66"] + x67_prefixes[0], x67_prefixes[1]],
                                                 [x67_prefixes[0], ["0x66"] + x67_prefixes[1]]]>
                    <#local indent = indent + "    ">
                <#else>
                    <#local x66_prefixes_list = [[x67_prefixes[0],  ["0x66"] + x67_prefixes[1]]]>
                </#if>
                <#list x66_prefixes_list as x66_prefixes>
                    <#if 1 < x66_prefixes_list?size>
                        <#if element_in_list("0x66", x66_prefixes[0])>
${indent}if (final_x66_prefix) {
                        <#else>
${indent}} else {
                        </#if>
                    </#if>
                    <#local prefixes_and_opcode = (x66_prefixes[0]?sort + [opcode])?join(" ")>
                    <@parse_instruction_info_after_opcode
                        indent + "    " norexw_modes prefixes_and_opcode x66_prefix_flavor x67_prefix_flavor/>
                </#list>
                <#if 1 < x66_prefixes_list?size>
${indent}}
                    <#local indent = indent[0..<indent?length-4]>
                </#if>
            </#list>
            <#if 1 < x67_prefixes_list?size>
${indent}}
                <#local indent = indent[0..<indent?length-4]>
            </#if>
        </#list>
        <#if 1 < f2f3_prefixes_list?size>
${indent}}
            <#local indent = indent[0..<indent?length-4]>
        </#if>
    </#list>
    <#if 1 < rexw_prefixes_list?size>
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
    @NotNull byte[] getBytes();

    @Contract(pure = true)
    @NotNull Argument[] getArguments();

    <Type> Type process(@NotNull Result<Type> result);

    interface Result<Type> {
        default Type when(Instruction argument) {
            return null;
        }
        default Type when(Jcc argument) {
            return when((Instruction) argument);
        }
        default Type when(BadInstruction argument) {
            return when((Instruction) argument);
        }
<#list merged_instruction_classes_list as instruction_class>
    <#list instruction_class.names as instruction_name>
        default Type when(${classname(instruction_name, instruction_class.arguments)} argument) {
        <#if instruction_name?starts_with("j")>
            return when((Jcc) argument);
        <#else>
            return when((Instruction) argument);
        </#if>
        }
    </#list>
</#list>
    }

    abstract class Jcc implements Instruction {
        final private Condition condition;

        @Contract(pure = true)
        Jcc(Condition condition) {
            this.condition = condition;
        }

        public Optional<Condition> getCondition() {
            return Optional.ofNullable(condition);
        }
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
            return "(bad)";
        }

        @Contract(pure = true)
        public @NotNull Argument[] getArguments() {
            return new Argument[0];
        }

        public @NotNull byte[] getBytes() {
            return bytes;
        }
    }

<#list merged_instruction_classes_list as instruction_class>
    <#list instruction_class.names as instruction_name>
    final class ${classname(instruction_name, instruction_class.arguments)} <#if
            instruction_name?starts_with("j")>extends Jcc<#else>implements Instruction</#if> {
        final private byte[] bytes;
        <#list instruction_class.arguments as argument>
        final private ${argumeng_name_to_type_name[argument?keep_after(":")]} arg${argument?index};
        </#list>

        public ${classname(instruction_name, instruction_class.arguments)}(<#list
        instruction_class.arguments as argument>${
            argumeng_name_to_type_name[argument?keep_after(":")]} arg${argument?index}<#sep>, </#sep></#list
        ><#if instruction_class.arguments?size != 0>,</#if> byte[] bytes) {
        <#if instruction_name?starts_with("j")>
            super(<@condition_from_instruction_suffix instruction_name[1..]/>);
        </#if>
        <#list instruction_class.arguments as argument>
            <#if argument?contains("/")>
            this.arg${argument?index} = new ${argumeng_name_to_type_name[argument?keep_after(":")]}(arg${
                argument?index}, (short)(${argument?keep_after("/")}));
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
                > <#if argument?contains(":Imm")>new ${argument?keep_after(":")}(arg${argument?index})<#elseif
                      argument?contains(":Rel")>arg${argument?index}<#else
                      >arg${argument?index}.toArgument()</#if><#sep>, </#sep></#list>};
        }

        <#list instruction_class.arguments as argument>
        public ${argumeng_name_to_type_name[argument?keep_after(":")]} getArg${argument?index}() {
            return arg${argument?index};
        }

        </#list>
        public @NotNull byte[] getBytes() {
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
        ADDR16_DATA32,
        ADDR16_DATA16,
        ADDR32_DATA32,
        ADDR32_DATA16,
        ADDR64_DATA32;

        @Contract(pure = true)
        public static int addressSize(@NotNull Mode mode) {
            switch (mode) {
                case ADDR16_DATA32:
                case ADDR16_DATA16:
                    return 16;
                case ADDR32_DATA32:
                case ADDR32_DATA16:
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
<#assign valid_opcodes_list = []>
<#list byte_vaues?filter(x -> !element_in_list(x, ["66", "67", "f2", "f3"])) as opcode_value>
<#if opcode_maps[0]["0x" + opcode_value]?? &&
     argument_in_opcode(opcode_maps[0]["0x" + opcode_value]) && opcode_value[1] != '7' && opcode_value[1] != 'f'>
    <#assign valid_opcodes_list = valid_opcodes_list + [opcode_value]>
    <#continue>
</#if>
    <#assign valid_opcode_x32 = false>
    <#assign valid_opcode_x64 = false>
    <#assign valid_prefix_opcode_x32 = false>
    <#assign valid_prefix_opcode_x64 = false>
    <#assign x32_x64_difference = false>
    <#list 0..3 as map>
        <#list ["", "0x66 ", "0x67 ", "0x66 0x67 ",
                "0xf2 ", "0x66 0xf2 ", "0x67 0xf2 ", "0x66 0x67 0xf2 ",
                "0xf3 ", "0x66 0xf3 ", "0x67 0xf3 ", "0x66 0x67 0xf3 "] as opcode_prefix>
            <#assign suffix_list = [""]>
            <#list [ADDR16_DATA32, ADDR16_DATA16, ADDR32_DATA32, ADDR32_DATA16, ADDR64_DATA32] as anymap>
                <#if prefix_opcode_maps[anymap][opcode_prefix + "0x" + opcode_value]??>
                    <#assign suffix_list = suffix_list + prefix_opcode_maps[anymap][opcode_prefix + "0x" + opcode_value]>
                </#if>
            </#list>
            <#assign suffix_list = sort_and_remove_duplicates(suffix_list)>
            <#list suffix_list as opcode_suffix>
                <#if opcode_maps[map][opcode_prefix + "0x" + opcode_value + opcode_suffix]??>
                    <#if opcode_suffix == "">
                        <#assign valid_opcode_x32 = true>
                    <#else>
                        <#assign valid_prefix_opcode_x32 = true>
                    </#if>
                    <#-- Note: most instructions exist both in 16/32-bit and 64-bit modes, some exist only in one mode,
                         but only few one-byte opcode instructions are interpreted as different instructions depending
                         on the CPU mode-->
                    <#if map == 2 && opcode_maps[4][opcode_prefix + "0x" + opcode_value + opcode_suffix]??>
                        <#if opcode_suffix == "">
                            <#assign valid_opcode_x64 = true>
                        <#else>
                            <#assign valid_prefix_opcode_x64 = true>
                        </#if>
                        <#assign x32_instruction = opcode_maps[2][opcode_prefix + "0x" + opcode_value + opcode_suffix]>
                        <#assign x64_instruction = opcode_maps[4][opcode_prefix + "0x" + opcode_value + opcode_suffix]>
                        <#if x32_instruction.name != x64_instruction.name ||
                             !same_argument_lists(x32_instruction.arguments, x64_instruction.arguments)>
                            <#-- If it's memory instruction and difference is onlly address we would handle difference later -->
                            <#if opcode_suffix != " /m" ||
                                 (x32_instruction.name != x64_instruction.name?replace("Addr64", "Addr32") &&
                                  x32_instruction.name != x64_instruction.name?replace("Addr32", "Addr16"))>
                                <#assign x32_x64_difference = true>
                            </#if>
                        </#if>
                    </#if>
                <#elseif map == 2 && (opcode_maps[4][opcode_prefix + "0x" + opcode_value + opcode_suffix]?? ||
                                      opcode_maps[4][opcode_prefix + "rexw 0x" + opcode_value + opcode_suffix]??)>
                    <#if opcode_suffix == "">
                        <#assign valid_opcode_x64 = true>
                    <#else>
                        <#assign valid_prefix_opcode_x64 = true>
                    </#if>
                </#if>
            </#list>
        </#list>
    </#list>
    <#if !valid_opcode_x64 && !valid_opcode_x64>
        <#list 0..3 as map>
            <#list ["", "0x66 ", "0x67 ", "0x66 0x67 ",
                    "0xf2 ", "0x66 0xf2 ", "0x67 0xf2 ", "0x66 0x67 0xf2 ",
                    "0xf3 ", "0x66 0xf3 ", "0x67 0xf3 ", "0x66 0x67 0xf3 ",
                    "rexw ", "0x66 rexw ", "0x67 rexw ", "0x66 0x67 rexw "] as opcode_prefix>
                <#if prefix_opcode_maps[map][opcode_prefix + "0x" + opcode_value]??>
                    <#assign valid_prefix_opcode_x32 = true>
                </#if>
            </#list>
        </#list>
        <#list ["", "0x66 ", "0x67 ", "0x66 0x67 ",
                "0xf2 ", "0x66 0xf2 ", "0x67 0xf2 ", "0x66 0x67 0xf2 ",
                "0xf3 ", "0x66 0xf3 ", "0x67 0xf3 ", "0x66 0x67 0xf3 ",
                "rexw ", "0x66 rexw ", "0x67 rexw ", "0x66 0x67 rexw ",
                "0xf2 rexw ", "0x66 0xf2 rexw ", "0x67 0xf2 rexw ", "0x66 0x67 0xf2 rexw ",
                "0xf3 rexw ", "0x66 0xf3 rexw ", "0x67 0xf3 rexw ", "0x66 0x67 0xf3 rexw "] as opcode_prefix>
                <#if prefix_opcode_maps[4][opcode_prefix + "0x" + opcode_value]??>
                    <#assign valid_prefix_opcode_x64 = true>
                </#if>
        </#list>
    </#if>
    <#if valid_opcode_x32 || valid_prefix_opcode_x32 || valid_opcode_x64 || valid_prefix_opcode_x64>
        <#assign valid_opcodes_list = valid_opcodes_list + [opcode_value]>
            Supplier<Optional<Instruction>> parse_${opcode_value} = () -> {
        <#if !valid_opcode_x32 && !valid_prefix_opcode_x32>
                if (mode == Mode.ADDR64_DATA32) {
            <@select_instruction_form ""?left_pad(16) 4..4 "0x" + opcode_value/>
                }
                return Optional.empty();
        <#elseif !valid_opcode_x64 && !valid_prefix_opcode_x64>
                if (mode != Mode.ADDR64_DATA32) {
            <@select_instruction_form ""?left_pad(16) 0..3 "0x" + opcode_value/>
                }
                return Optional.empty();
        <#elseif x32_x64_difference>
                if (mode == Mode.ADDR64_DATA32) {
            <@select_instruction_form ""?left_pad(16) 4..4 "0x" + opcode_value/>
                } else {
            <@select_instruction_form ""?left_pad(16) 0..3 "0x" + opcode_value/>
                }
        <#else>
            <@select_instruction_form ""?left_pad(12) 0..4 "0x" + opcode_value/>
        </#if>
            };
    </#if>
</#list>
            switch (opcode) {
<#list valid_opcodes_list as opcode_value>
                case <@value_to_byte opcode_value/>:
<#if opcode_maps[0]["0x" + opcode_value]?? &&
     argument_in_opcode(opcode_maps[0]["0x" + opcode_value]) && opcode_value[1] != '7' && opcode_value[1] != 'f'>
                    /* fallthrough */
<#else>
                    return parse_${opcode_value}.get();
</#if>
</#list>
                default:
                    return Optional.empty();
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
            base = GPRegister${AddrSize}.of((modrm & 0b00_000_111) | ((rex & 0b0000_0_0_0_1) << 3));
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
