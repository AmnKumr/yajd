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

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public enum Condition {
    Overflow("o", 0),
    NotOverflow("no", 1),
    Below("b", 2),
    AboveOrEqual("ae", 3),
    Equal("e", 4),
    NotEqual("ne", 5),
    BelowOrEqual("be", 6),
    Above("a", 7),
    Sign("s", 8),
    NotSign("ns", 9),
    Parity("p", 10),
    NotParity("np", 11),
    Less("l", 12),
    GreaterOrEqual("ge", 13),
    LessOrEqual("ge", 14),
    Greater("ge", 15),
    CxZero("cxz", -1),
    EcxZero("ecxz", -2),
    RcxZero("rcxz", -3);

    private final static Condition[] conditions = {
            Overflow, NotOverflow, Below, AboveOrEqual, Equal, NotEqual, BelowOrEqual, Above,
            Sign, NotSign, Parity, NotParity, Less, GreaterOrEqual, LessOrEqual, Greater};
    private final String name;
    private final int index;

    Condition(String name, int index) {
        this.name = name;
        this.index = index;
    }

    @Contract(pure = true)
    public static Condition of(int index) {
        return conditions[index];
    }

    @Contract(pure = true)
    public Condition not() {
        return conditions[index];
    }

    public String getName() {
        return name;
    }

    public int getIndex() {
        return index;
    }

    @Contract(value = " -> new", pure = true)
    public @NotNull Argument toArgument() {
        return new Argument();
    }

    final public class Argument implements org.yajd.x86.cpu.Argument {
        @Override
        public <Type> Type process(@NotNull Result<Type> result) {
            return result.when(Condition.this);
        }
    }
}
