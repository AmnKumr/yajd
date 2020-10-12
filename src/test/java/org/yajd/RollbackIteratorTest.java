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

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

public class RollbackIteratorTest {
    @Test
    @DisplayName("Pass-through test")
    void PassthroughTest() {
        RollbackIterator<Integer> it = new RollbackIterator<>(
                Arrays.asList(new Integer[]{1, 2, 3}).iterator());
        assertTrue(it.hasNext());
        assertEquals(1, it.next());
        assertTrue(it.hasNext());
        assertEquals(2, it.next());
        assertTrue(it.hasNext());
        assertEquals(3, it.next());
        assertFalse(it.hasNext());
    }

    @Test
    @DisplayName("Rollback test")
    void RollbackTest() {
        RollbackIterator<Integer> it = new RollbackIterator<>(
                Arrays.asList(new Integer[]{1, 2, 3}).iterator());
        it.tryProcess(() -> {
            assertTrue(it.hasNext());
            assertEquals(1, it.next());
            assertTrue(it.hasNext());
            assertEquals(2, it.next());
            return Optional.empty();
        });
        assertTrue(it.hasNext());
        assertEquals(1, it.next());
        it.tryProcess(() -> {
            assertTrue(it.hasNext());
            assertEquals(2, it.next());
            assertTrue(it.hasNext());
            assertEquals(3, it.next());
            return Optional.empty();
        });
        assertTrue(it.hasNext());
        assertEquals(2, it.next());
        assertTrue(it.hasNext());
        assertEquals(3, it.next());
        assertFalse(it.hasNext());
    }
}
