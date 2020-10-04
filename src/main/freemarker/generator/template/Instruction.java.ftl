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
<#list instructions as instruction>
        default Type when(${instruction.class} argument) {
            return when((Instruction) argument);
        }
</#list>
    }

<#list instructions as instruction>
    final class ${instruction.class} implements Instruction {
<#list instruction.arguments as argument>
        final private ${argument} arg${argument?index};
</#list>

        public ${instruction.class}(
<#list instruction.arguments as argument>
            ${argument} arg${argument?index}<#sep>, </#sep>
</#list>
                ) {
<#list instruction.arguments as argument>
            this.arg${argument?index} = arg${argument?index};
</#list>
        }

        @Override
        public <Type> Type process(@NotNull Result<Type> result) {
            return result.when(this);
        }

        @Contract(pure = true)
        public @NotNull String getName() {
            return "${instruction.name}";
        }

        public Argument[] getArguments() {
            return new Argument[]{
<#list instruction.arguments as argument>
                arg${argument?index}.toArgument()<#sep>, </#sep>
</#list>
                        };
        }

<#list instruction.arguments as argument>
        public ${argument} getArg${argument?index}() {
            return arg${argument?index};
        }
</#list>
    }
</#list>
}
