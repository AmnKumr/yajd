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
<#function same_lists x y>
    <#if x?size == 0>
        <#if y?size == 0>
            <#return true>
        <#else>
            <#return false>
        </#if>
    <#elseif y?size == 0>
        <#return false>
    <#elseif x[0] == y[0]>
        <#return same_lists(x[1..], y[1..])>
    <#else>
        <#return false>
    </#if>
</#function>
<#function replace_arguments arguments from to>
    <#if arguments?size == 0>
        <#return []>
    <#elseif from?size == 1>
        <#if arguments[0] == from[0]>
            <#return [to[0]] + replace_arguments(arguments[1..], from, to)>
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
        <#if element_in_list(argument, ["Memory8", "Memory16", "Memory32", "Memory64"])>
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
            <#return
                [instructions[0] + {"arguments": replace_arguments(
                    instructions[0].arguments,
                    ["Memory8", "Memory16", "Memory32", "Memory64"],
                    ["GPAddress16/8", "GPAddress16/16", "GPAddress16/32", "GPAddress16/64"])}] +
                [instructions[0] + {"arguments": replace_arguments(
                    instructions[0].arguments,
                    ["Memory8", "Memory16", "Memory32", "Memory64"],
                    ["GPAddress32/8", "GPAddress32/16", "GPAddress32/32", "GPAddress32/64"])}] +
                [instructions[0] + {"arguments": replace_arguments(
                    instructions[0].arguments,
                    ["Memory8", "Memory16", "Memory32", "Memory64"],
                    ["EIPAddress32/8", "EIPAddress32/16", "EIPAddress32/32", "EIPAddress32/64"])}] +
                [instructions[0] + {"arguments": replace_arguments(
                    instructions[0].arguments,
                    ["Memory8", "Memory16", "Memory32", "Memory64"],
                    ["GPAddress64/8", "GPAddress64/16", "GPAddress64/32", "GPAddress64/64"])}] +
                [instructions[0] + {"arguments": replace_arguments(
                    instructions[0].arguments,
                    ["Memory8", "Memory16", "Memory32", "Memory64"],
                    ["RIPAddress64/8", "RIPAddress64/16", "RIPAddress64/32", "RIPAddress64/64"])}] +
                expand_native_arguments(instructions[1..])>
        <#else>
            <#return [instructions[0]] + expand_native_arguments(instructions[1..])>
        </#if>
    </#if>
</#function>
<#function native_sized_operand arguments>
    <#list arguments as argument>
        <#if argument == "GPRegisterNative" || argument == "MemoryNative">
            <#return true>
        </#if>
    </#list>
    <#return false>
</#function>
<#function expand_native_arguments instructions>
    <#if instructions?size == 0>
        <#return []>
    <#else>
        <#if native_sized_operand(instructions[0].arguments)>
            <#return
                expand_address_arguments(
                    [instructions[0] + {"arguments": replace_arguments(
                        instructions[0].arguments,
                        ["GPRegisterNative", "MemoryNative"],
                        ["GPRegister16", "Memory16"])}] +
                    [instructions[0] + {"arguments": replace_arguments(
                        instructions[0].arguments,
                        ["GPRegisterNative", "MemoryNative"],
                        ["GPRegister32", "Memory32"])}] +
                    [instructions[0] + {"arguments": replace_arguments(
                        instructions[0].arguments,
                        ["GPRegisterNative", "MemoryNative"],
                        ["GPRegister64", "Memory64"])}]) +
                expand_native_arguments(instructions[1..])>
        <#else>
            <#return expand_address_arguments([instructions[0]]) +
                     expand_native_arguments(instructions[1..])>
        </#if>
    </#if>
</#function>
<#function memory_register_operand arguments>
    <#list arguments as argument>
        <#if argument == "GPRegister8/Memory8" ||
             argument == "GPRegisterNative/MemoryNative">
            <#return true>
        </#if>
    </#list>
    <#return false>
</#function>
<#function expand_memory_register_arguments instructions>
    <#if instructions?size == 0>
        <#return []>
    <#else>
        <#if memory_register_operand(instructions[0].arguments)>
            <#return
                expand_native_arguments(
                    [instructions[0] + {"arguments": replace_arguments(
                        instructions[0].arguments,
                        ["GPRegister8/Memory8", "GPRegisterNative/MemoryNative"],
                        ["GPRegister8", "GPRegisterNative"])}] +
                    [instructions[0] + {"arguments": replace_arguments(
                        instructions[0].arguments,
                        ["GPRegister8/Memory8", "GPRegisterNative/MemoryNative"],
                        ["Memory8", "MemoryNative"])}]) +
                expand_memory_register_arguments(instructions[1..])>
        <#else>
            <#return expand_native_arguments([instructions[0]]) +
                     expand_memory_register_arguments(instructions[1..])>
        </#if>
    </#if>
</#function>
<#function filter_out_instructions existing_names new_names>
    <#return []>
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
                 x -> same_lists(x.arguments, original_instructions[0].arguments))?size == 0>
        <#return merge_instruction(merged_instructions + [original_instructions[0]],
                                   original_instructions[1..])>
    <#else>
        <#local filtered_names = filter_out_instructions(merged_instructions?filter(
                    x -> same_lists(x.arguments, original_instructions[0].arguments))?map(
                    x -> x.names), original_instructions[0].names)>
        <#if filtered_names?size != 0>
            <#return merge_instruction(merged_instructions + [original_instructions[0] + {"names":
                         filtered_names}], original_instructions[1..])>
        <#else>
            <#return merge_instruction(merged_instructions, original_instructions[1..])>
        </#if>
    </#if>
</#function>
<#function expand_andmerge_instructions instructions>
    <#return merge_instruction([], expand_memory_register_arguments(instructions))>
</#function>
<#function classname name arguments>
    <#local result>${
        name?capitalize}<#list arguments as argument>${
        argument_to_class_name[argument]
    }</#list></#local>
    <#return result>
</#function>
package ${session.currentProject.model.groupId}.x86.cpu;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

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
<#list expand_andmerge_instructions(instructions) as instruction_class>
    <#list instruction_class.names as instruction_name>
        default Type when(${classname(instruction_name, instruction_class.arguments)} argument) {
            return when((Instruction) argument);
        }
    </#list>
</#list>
    }

<#list expand_andmerge_instructions(instructions) as instruction_class>
    <#list instruction_class.names as instruction_name>
    final class ${classname(instruction_name, instruction_class.arguments)} implements Instruction {
        <#list instruction_class.arguments as argument>
        final private ${argument?keep_before("/")} arg${argument?index};
        </#list>

        public ${classname(instruction_name, instruction_class.arguments)}(
        <#list instruction_class.arguments as argument>
            ${argument?keep_before("/")} arg${argument?index}<#sep>, </#sep>
        </#list>
                ) {
        <#list instruction_class.arguments as argument>
            <#if argument?contains("/")>
            this.arg${argument?index} = new ${argument?keep_before("/")}(
                arg${argument?index}, (short)${argument?keep_after("/")});
            <#else>
            this.arg${argument?index} = arg${argument?index};
            </#if>
        </#list>
        }

        @Override
        public <Type> Type process(@NotNull Result<Type> result) {
            return result.when(this);
        }

        @Contract(pure = true)
        public @NotNull String getName() {
            return "${instruction_name}";
        }

        public Argument[] getArguments() {
            return new Argument[]{
        <#list instruction_class.arguments as argument>
                arg${argument?index}.toArgument()<#sep>, </#sep>
        </#list>
                        };
        }

        <#list instruction_class.arguments as argument>
        public ${argument?keep_before("/")} getArg${argument?index}() {
            return arg${argument?index};
        }
        </#list>
    }
    </#list>
</#list>
}
