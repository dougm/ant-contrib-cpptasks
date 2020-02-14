/*
 *
 * Copyright 2002-2007 The Ant-Contrib project
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package net.sf.antcontrib.cpptasks.hp;

import net.sf.antcontrib.cpptasks.compiler.AbstractProcessor;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test HP aCC compiler adapter
 */
// TODO Since aCCCompiler extends GccCompatibleCCompiler, this test
// should probably extend TestGccCompatibleCCompiler.
public class TestaCCCompiler {
    @Test
    public void testBidC() {
        aCCCompiler compiler = aCCCompiler.getInstance();
        assertEquals(AbstractProcessor.DEFAULT_PROCESS_BID, compiler.bid("foo.c"));
    }

    @Test
    public void testBidCpp() {
        aCCCompiler compiler = aCCCompiler.getInstance();
        assertEquals(AbstractProcessor.DEFAULT_PROCESS_BID, compiler.bid("foo.C"));
    }

    @Test
    public void testBidCpp2() {
        aCCCompiler compiler = aCCCompiler.getInstance();
        assertEquals(AbstractProcessor.DEFAULT_PROCESS_BID, compiler.bid("foo.cc"));
    }

    @Test
    public void testBidCpp3() {
        aCCCompiler compiler = aCCCompiler.getInstance();
        assertEquals(AbstractProcessor.DEFAULT_PROCESS_BID, compiler.bid("foo.CC"));
    }

    @Test
    public void testBidCpp4() {
        aCCCompiler compiler = aCCCompiler.getInstance();
        assertEquals(AbstractProcessor.DEFAULT_PROCESS_BID, compiler.bid("foo.cxx"));
    }

    @Test
    public void testBidCpp5() {
        aCCCompiler compiler = aCCCompiler.getInstance();
        assertEquals(AbstractProcessor.DEFAULT_PROCESS_BID, compiler.bid("foo.CXX"));
    }

    @Test
    public void testBidCpp6() {
        aCCCompiler compiler = aCCCompiler.getInstance();
        assertEquals(AbstractProcessor.DEFAULT_PROCESS_BID, compiler.bid("foo.cpp"));
    }

    @Test
    public void testBidCpp7() {
        aCCCompiler compiler = aCCCompiler.getInstance();
        assertEquals(AbstractProcessor.DEFAULT_PROCESS_BID, compiler.bid("foo.CPP"));
    }

    @Test
    public void testBidCpp8() {
        aCCCompiler compiler = aCCCompiler.getInstance();
        assertEquals(AbstractProcessor.DEFAULT_PROCESS_BID, compiler.bid("foo.c++"));
    }

    @Test
    public void testBidCpp9() {
        aCCCompiler compiler = aCCCompiler.getInstance();
        assertEquals(AbstractProcessor.DEFAULT_PROCESS_BID, compiler.bid("foo.C++"));
    }

    @Test
    public void testBidPreprocessed() {
        aCCCompiler compiler = aCCCompiler.getInstance();
        assertEquals(AbstractProcessor.DEFAULT_PROCESS_BID, compiler.bid("foo.i"));
    }

    @Test
    public void testBidAssembly() {
        aCCCompiler compiler = aCCCompiler.getInstance();
        assertEquals(AbstractProcessor.DEFAULT_PROCESS_BID, compiler.bid("foo.s"));
    }
}
