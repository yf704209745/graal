/*
 * Copyright (c) 2013, 2019, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.oracle.svm.core.posix.headers;

import org.graalvm.nativeimage.Platforms;
import org.graalvm.nativeimage.c.CContext;
import org.graalvm.nativeimage.c.constant.CConstant;
import org.graalvm.nativeimage.c.function.CFunction;
import org.graalvm.nativeimage.c.function.CLibrary;
import org.graalvm.nativeimage.c.struct.CField;
import org.graalvm.nativeimage.c.struct.CStruct;
import org.graalvm.nativeimage.c.type.CCharPointer;
import org.graalvm.nativeimage.impl.InternalPlatform.DARWIN_JNI_AND_SUBSTITUTIONS;
import org.graalvm.nativeimage.impl.InternalPlatform.LINUX_JNI_AND_SUBSTITUTIONS;
import org.graalvm.word.Pointer;
import org.graalvm.word.PointerBase;
import org.graalvm.word.WordBase;

//Checkstyle: stop

/**
 * Definitions manually translated from the C header file dlfcn.h.
 */
@Platforms({DARWIN_JNI_AND_SUBSTITUTIONS.class, LINUX_JNI_AND_SUBSTITUTIONS.class})
@CContext(PosixDirectives.class)
@CLibrary("dl")
public class Dlfcn {

    @CConstant
    public static native int RTLD_LAZY();

    @CConstant
    public static native int RTLD_NOW();

    @CConstant
    public static native int RTLD_GLOBAL();

    @CConstant
    public static native int RTLD_LOCAL();

    @CConstant
    public static native PointerBase RTLD_DEFAULT();

    @CFunction
    public static native PointerBase dlopen(CCharPointer file, int mode);

    @CFunction
    public static native int dlclose(PointerBase handle);

    @CFunction
    public static native <T extends PointerBase> T dlsym(PointerBase handle, CCharPointer name);

    @CFunction
    public static native CCharPointer dlerror();

    @CStruct
    public interface Dl_info extends PointerBase {
        @CField
        CCharPointer dli_fname();

        @CField
        Pointer dli_fbase();

        @CField
        CCharPointer dli_sname();

        @CField
        Pointer dli_saddr();
    }

    @CFunction
    public static native int dladdr(WordBase address, Dl_info info);
}
