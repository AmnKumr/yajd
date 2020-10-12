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

import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class RollbackIterator<E> extends BufferedIterator<E> {
    private Deque<E> rollback = null;

    RollbackIterator(Iterator<E> raw_iterator) {
        super(raw_iterator);
    }

    @Override
    public E next() {
        var e = super.next();
        if (rollback != null) {
            rollback.addLast(e);
        }
        return e;
    }

    Optional<?> tryProcess(@NotNull Supplier<Optional<?>> f) {
        return tryProcess(new ArrayDeque<>(), f);
    }

    Optional<?> tryProcess(@NotNull Deque<E> processed, @NotNull Supplier<Optional<?>> f) {
        assert rollback == null; // We don't support nested rollbacks.
        rollback = processed;
        var result = f.get();
        if (result.isEmpty()) {
            while (!rollback.isEmpty()) {
                putBack(rollback.removeLast());
            }
            rollback = null;
        }
        return result;
    }
    Optional<?> tryProcess(@NotNull Function<RollbackIterator<E>, Optional<?>> f) {
        return tryProcess(new ArrayDeque<>(), f);
    }

    Optional<?> tryProcess(@NotNull Deque<E> processed,
                           @NotNull Function<RollbackIterator<E>, Optional<?>> f) {
        assert rollback == null; // We don't support nested rollbacks.
        rollback = processed;
        var result = f.apply(this);
        if (result.isEmpty()) {
            while (!rollback.isEmpty()) {
                putBack(rollback.removeLast());
            }
            rollback = null;
        }
        return result;
    }
}
