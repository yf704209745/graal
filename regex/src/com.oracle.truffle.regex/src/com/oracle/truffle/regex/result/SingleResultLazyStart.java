/*
 * Copyright (c) 2018, 2019, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * The Universal Permissive License (UPL), Version 1.0
 *
 * Subject to the condition set forth below, permission is hereby granted to any
 * person obtaining a copy of this software, associated documentation and/or
 * data (collectively the "Software"), free of charge and under any and all
 * copyright rights in the Software, and any and all patent rights owned or
 * freely licensable by each licensor hereunder covering either (i) the
 * unmodified Software as contributed to or provided by such licensor, or (ii)
 * the Larger Works (as defined below), to deal in both
 *
 * (a) the Software, and
 *
 * (b) any piece of software and/or hardware listed in the lrgrwrks.txt file if
 * one is included with the Software each a "Larger Work" to which the Software
 * is contributed by such licensors),
 *
 * without restriction, including without limitation the rights to copy, create
 * derivative works of, display, perform, and distribute the Software and make,
 * use, sell, offer for sale, import, export, have made, and have sold the
 * Software and the Larger Work(s), and to sublicense the foregoing rights on
 * either these or other terms.
 *
 * This license is subject to the following condition:
 *
 * The above copyright notice and either this complete permission notice or at a
 * minimum a reference to the UPL must be included in all copies or substantial
 * portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.oracle.truffle.regex.result;

import com.oracle.truffle.api.CallTarget;
import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;

public final class SingleResultLazyStart extends LazyResult {

    private int start = -1;
    private final CallTarget findStartCallTarget;

    public SingleResultLazyStart(Object input, int fromIndex, int end, CallTarget findStartCallTarget) {
        super(input, fromIndex, end);
        this.findStartCallTarget = findStartCallTarget;
    }

    @Override
    public int getStart(int groupNumber) {
        return groupNumber == 0 ? start : -1;
    }

    @Override
    public int getEnd(int groupNumber) {
        return groupNumber == 0 ? getEnd() : -1;
    }

    public boolean isStartCalculated() {
        return start != -1;
    }

    public int getStart() {
        return start;
    }

    public CallTarget getFindStartCallTarget() {
        return findStartCallTarget;
    }

    public Object[] createArgsFindStart() {
        return new Object[]{getInput(), getEnd() - 1, getFromIndex()};
    }

    public void applyFindStartResult(int findStartResult) {
        this.start = findStartResult + 1;
    }

    /**
     * Forces evaluation of this lazy regex result. Do not use this method on any fast paths, use
     * {@link com.oracle.truffle.regex.result.RegexResultGetStartNode} instead!
     */
    @TruffleBoundary
    @Override
    public void debugForceEvaluation() {
        if (!isStartCalculated()) {
            applyFindStartResult((int) findStartCallTarget.call(createArgsFindStart()));
        }
    }

    @TruffleBoundary
    @Override
    public String toString() {
        if (!isStartCalculated()) {
            debugForceEvaluation();
        }
        return "[" + start + ", " + getEnd() + "]";
    }
}
