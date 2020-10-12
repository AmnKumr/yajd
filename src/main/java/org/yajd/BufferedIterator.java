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

import java.util.Iterator;
import java.util.Stack;

public class BufferedIterator<E> implements Iterator<E> {
    private Stack<E> buffer;
    final private Iterator<E> raw_iterator;

    BufferedIterator(Iterator<E> raw_iterator) {
        this.buffer = new Stack<E>();
        this.raw_iterator = raw_iterator;
    }

    @Override
    public boolean hasNext() {
        if (!buffer.isEmpty()) {
            return true;
        }
        return raw_iterator.hasNext();
    }

    @Override
    public E next() {
        if (!buffer.isEmpty()) {
            return buffer.pop();
        }
        return raw_iterator.next();
    }

    public void putBack(E e) {
        buffer.push(e);
    }
}
