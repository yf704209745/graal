/*
 * Copyright (c) 2019, 2019, Oracle and/or its affiliates. All rights reserved.
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
package com.oracle.svm.configure.trace;

import java.util.List;
import java.util.Map;

import com.oracle.svm.configure.config.ConfigurationMemberKind;
import com.oracle.svm.configure.config.ConfigurationMethod;
import com.oracle.svm.configure.config.TypeConfiguration;

import org.graalvm.compiler.phases.common.LazyValue;
import jdk.vm.ci.meta.MetaUtil;

class JniProcessor extends AbstractProcessor {
    private final TypeConfiguration configuration;
    private final TypeConfiguration reflectionConfiguration;
    private final AccessAdvisor advisor;

    JniProcessor(AccessAdvisor advisor, TypeConfiguration configuration, TypeConfiguration reflectionConfiguration) {
        this.advisor = advisor;
        this.configuration = configuration;
        this.reflectionConfiguration = reflectionConfiguration;
    }

    public TypeConfiguration getConfiguration() {
        return configuration;
    }

    @Override
    @SuppressWarnings("fallthrough")
    void processEntry(Map<String, ?> entry) {
        boolean invalidResult = Boolean.FALSE.equals(entry.get("result"));
        if (invalidResult) {
            return;
        }
        String function = (String) entry.get("function");
        String clazz = (String) entry.get("class");
        String declaringClass = (String) entry.get("declaring_class");
        String callerClass = (String) entry.get("caller_class");
        List<?> args = (List<?>) entry.get("args");
        LazyValue<String> callerClassLazyValue = new LazyValue<>(() -> callerClass);
        if (advisor.shouldIgnore(callerClassLazyValue)) {
            return;
        }
        String declaringClassOrClazz = (declaringClass != null) ? declaringClass : clazz;
        ConfigurationMemberKind memberKind = (declaringClass != null) ? ConfigurationMemberKind.DECLARED : ConfigurationMemberKind.PRESENT;
        TypeConfiguration config = configuration;
        switch (function) {
            case "DefineClass": {
                String name = singleElement(args);
                if (name.startsWith("com/sun/proxy/$Proxy")) {
                    break; // implementation detail of Proxy support
                }
                logWarning("Unsupported JNI function DefineClass used to load class " + name);
                break;
            }
            case "FindClass": {
                String name = singleElement(args);
                if (name.charAt(0) != '[') {
                    name = "L" + name + ";";
                }
                String qualifiedJavaName = MetaUtil.internalNameToJava(name, true, false);
                if (!advisor.shouldIgnoreJniClassLookup(new LazyValue<>(() -> qualifiedJavaName), callerClassLazyValue)) {
                    config.getOrCreateType(qualifiedJavaName);
                }
                break;
            }
            case "GetStaticMethodID":
            case "GetMethodID": {
                expectSize(args, 2);
                String name = (String) args.get(0);
                String signature = (String) args.get(1);
                if (!advisor.shouldIgnoreJniMethodLookup(new LazyValue<>(() -> clazz), new LazyValue<>(() -> name), new LazyValue<>(() -> signature), callerClassLazyValue)) {
                    config.getOrCreateType(declaringClassOrClazz).addMethod(name, signature, memberKind);
                }
                break;
            }
            case "GetFieldID":
            case "GetStaticFieldID": {
                expectSize(args, 2);
                String name = (String) args.get(0);
                config.getOrCreateType(declaringClassOrClazz).addField(name, memberKind, false, false);
                break;
            }
            case "ThrowNew": {
                expectSize(args, 1); // exception message, ignore
                String name = ConfigurationMethod.CONSTRUCTOR_NAME;
                String signature = "(Ljava/lang/String;)V";
                if (!advisor.shouldIgnoreJniMethodLookup(new LazyValue<>(() -> clazz), new LazyValue<>(() -> name), new LazyValue<>(() -> signature), callerClassLazyValue)) {
                    config.getOrCreateType(declaringClassOrClazz).addMethod(name, signature, memberKind);
                }
                break;
            }
            case "ToReflectedField":
                config = reflectionConfiguration; // fall through
            case "FromReflectedField": {
                expectSize(args, 1);
                String name = (String) args.get(0);
                config.getOrCreateType(declaringClassOrClazz).addField(name, memberKind, false, false);
                break;
            }
            case "ToReflectedMethod":
                config = reflectionConfiguration; // fall through
            case "FromReflectedMethod": {
                expectSize(args, 2);
                String name = (String) args.get(0);
                String signature = (String) args.get(1);
                config.getOrCreateType(declaringClassOrClazz).addMethod(name, signature, memberKind);
                break;
            }
        }
    }

}
