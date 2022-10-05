/*******************************************************************************
 * Copyright (c) 2021, 2022 IBM Corp. and others
 *
 * This program and the accompanying materials are made available under
 * the terms of the Eclipse Public License 2.0 which accompanies this
 * distribution and is available at https://www.eclipse.org/legal/epl-2.0/
 * or the Apache License, Version 2.0 which accompanies this distribution and
 * is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * This Source Code may also be made available under the following
 * Secondary Licenses when the conditions for such availability set
 * forth in the Eclipse Public License, v. 2.0 are satisfied: GNU
 * General Public License, version 2 with the GNU Classpath
 * Exception [1] and GNU General Public License, version 2 with the
 * OpenJDK Assembly Exception [2].
 *
 * [1] https://www.gnu.org/software/classpath/license.html
 * [2] http://openjdk.java.net/legal/assembly-exception.html
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0 OR GPL-2.0 WITH Classpath-exception-2.0 OR LicenseRef-GPL-2.0 WITH Assembly-exception
 *******************************************************************************/
package org.openj9.test.jep419.upcall;

import org.testng.annotations.Test;
import org.testng.Assert;
import org.testng.AssertJUnit;

import java.lang.invoke.MethodHandle;
import jdk.incubator.foreign.CLinker;
import jdk.incubator.foreign.FunctionDescriptor;
import jdk.incubator.foreign.MemoryAddress;
import jdk.incubator.foreign.MemoryLayout;
import jdk.incubator.foreign.MemorySegment;
import jdk.incubator.foreign.NativeSymbol;
import jdk.incubator.foreign.ResourceScope;
import jdk.incubator.foreign.SegmentAllocator;
import jdk.incubator.foreign.SymbolLookup;
import jdk.incubator.foreign.ValueLayout;
import static jdk.incubator.foreign.ValueLayout.*;

/**
 * Test cases for JEP 419: Foreign Linker API (Second Incubator) intended for
 * the situations when the multiple primitive specific upcalls happen within
 * the same resource scope or from different resource scopes.
 */
@Test(groups = { "level.sanity" })
public class MultiUpcallMHTests {
	private static CLinker clinker = CLinker.systemCLinker();

	static {
		System.loadLibrary("clinkerffitests");
	}
	private static final SymbolLookup nativeLibLookup = SymbolLookup.loaderLookup();

	@Test
	public void test_addTwoBoolsWithOrByUpcallMH_SameScope() throws Throwable {
		FunctionDescriptor fd = FunctionDescriptor.of(JAVA_BOOLEAN, JAVA_BOOLEAN, JAVA_BOOLEAN, ADDRESS);
		NativeSymbol functionSymbol = nativeLibLookup.lookup("add2BoolsWithOrByUpcallMH").get();
		MethodHandle mh = clinker.downcallHandle(functionSymbol, fd);

		try (ResourceScope scope = ResourceScope.newConfinedScope()) {
			NativeSymbol upcallFuncAddr1 = clinker.upcallStub(UpcallMethodHandles.MH_add2BoolsWithOr,
					FunctionDescriptor.of(JAVA_BOOLEAN, JAVA_BOOLEAN, JAVA_BOOLEAN), scope);
			boolean result = (boolean)mh.invoke(true, false, upcallFuncAddr1);
			Assert.assertEquals(result, true);

			NativeSymbol upcallFuncAddr2 = clinker.upcallStub(UpcallMethodHandles.MH_add2BoolsWithOr,
					FunctionDescriptor.of(JAVA_BOOLEAN, JAVA_BOOLEAN, JAVA_BOOLEAN), scope);
			result = (boolean)mh.invoke(true, false, upcallFuncAddr2);
			Assert.assertEquals(result, true);

			NativeSymbol upcallFuncAddr3 = clinker.upcallStub(UpcallMethodHandles.MH_add2BoolsWithOr,
					FunctionDescriptor.of(JAVA_BOOLEAN, JAVA_BOOLEAN, JAVA_BOOLEAN), scope);
			result = (boolean)mh.invoke(true, false, upcallFuncAddr3);
			Assert.assertEquals(result, true);
		}
	}

	@Test
	public void test_addTwoBoolsWithOrByUpcallMH_DiffScope() throws Throwable {
		FunctionDescriptor fd = FunctionDescriptor.of(JAVA_BOOLEAN, JAVA_BOOLEAN, JAVA_BOOLEAN, ADDRESS);
		NativeSymbol functionSymbol = nativeLibLookup.lookup("add2BoolsWithOrByUpcallMH").get();
		MethodHandle mh = clinker.downcallHandle(functionSymbol, fd);

		try (ResourceScope scope = ResourceScope.newConfinedScope()) {
			NativeSymbol upcallFuncAddr = clinker.upcallStub(UpcallMethodHandles.MH_add2BoolsWithOr,
					FunctionDescriptor.of(JAVA_BOOLEAN, JAVA_BOOLEAN, JAVA_BOOLEAN), scope);
			boolean result = (boolean)mh.invoke(true, false, upcallFuncAddr);
			Assert.assertEquals(result, true);
		}

		try (ResourceScope scope = ResourceScope.newConfinedScope()) {
			NativeSymbol upcallFuncAddr = clinker.upcallStub(UpcallMethodHandles.MH_add2BoolsWithOr,
					FunctionDescriptor.of(JAVA_BOOLEAN, JAVA_BOOLEAN, JAVA_BOOLEAN), scope);
			boolean result = (boolean)mh.invoke(true, false, upcallFuncAddr);
			Assert.assertEquals(result, true);
		}

		try (ResourceScope scope = ResourceScope.newConfinedScope()) {
			NativeSymbol upcallFuncAddr = clinker.upcallStub(UpcallMethodHandles.MH_add2BoolsWithOr,
					FunctionDescriptor.of(JAVA_BOOLEAN, JAVA_BOOLEAN, JAVA_BOOLEAN), scope);
			boolean result = (boolean)mh.invoke(true, false, upcallFuncAddr);
			Assert.assertEquals(result, true);
		}
	}

	@Test
	public void test_createNewCharFromCharAndCharFromPointerByUpcallMH_SameScope() throws Throwable {
		FunctionDescriptor fd = FunctionDescriptor.of(JAVA_CHAR, ADDRESS, JAVA_CHAR, ADDRESS);
		NativeSymbol functionSymbol = nativeLibLookup.lookup("createNewCharFromCharAndCharFromPointerByUpcallMH").get();
		MethodHandle mh = clinker.downcallHandle(functionSymbol, fd);

		try (ResourceScope scope = ResourceScope.newConfinedScope()) {
			SegmentAllocator allocator = SegmentAllocator.nativeAllocator(scope);

			NativeSymbol upcallFuncAddr1 = clinker.upcallStub(UpcallMethodHandles.MH_createNewCharFromCharAndCharFromPointer,
					FunctionDescriptor.of(JAVA_CHAR, ADDRESS, JAVA_CHAR), scope);
			MemorySegment charSegmt1 = allocator.allocate(JAVA_CHAR, 'B');
			char result = (char)mh.invoke(charSegmt1, 'D', upcallFuncAddr1);
			Assert.assertEquals(result, 'C');

			NativeSymbol upcallFuncAddr2 = clinker.upcallStub(UpcallMethodHandles.MH_createNewCharFromCharAndCharFromPointer,
					FunctionDescriptor.of(JAVA_CHAR, ADDRESS, JAVA_CHAR), scope);
			MemorySegment charSegmt2 = allocator.allocate(JAVA_CHAR, 'B');
			result = (char)mh.invoke(charSegmt2, 'D', upcallFuncAddr2);
			Assert.assertEquals(result, 'C');

			NativeSymbol upcallFuncAddr3 = clinker.upcallStub(UpcallMethodHandles.MH_createNewCharFromCharAndCharFromPointer,
					FunctionDescriptor.of(JAVA_CHAR, ADDRESS, JAVA_CHAR), scope);
			MemorySegment charSegmt3 = allocator.allocate(JAVA_CHAR, 'B');
			result = (char)mh.invoke(charSegmt3, 'D', upcallFuncAddr3);
			Assert.assertEquals(result, 'C');
		}
	}

	@Test
	public void test_createNewCharFromCharAndCharFromPointerByUpcallMH_DiffScope() throws Throwable {
		FunctionDescriptor fd = FunctionDescriptor.of(JAVA_CHAR, ADDRESS, JAVA_CHAR, ADDRESS);
		NativeSymbol functionSymbol = nativeLibLookup.lookup("createNewCharFromCharAndCharFromPointerByUpcallMH").get();
		MethodHandle mh = clinker.downcallHandle(functionSymbol, fd);

		try (ResourceScope scope = ResourceScope.newConfinedScope()) {
			NativeSymbol upcallFuncAddr1 = clinker.upcallStub(UpcallMethodHandles.MH_createNewCharFromCharAndCharFromPointer,
					FunctionDescriptor.of(JAVA_CHAR, ADDRESS, JAVA_CHAR), scope);
			SegmentAllocator allocator = SegmentAllocator.nativeAllocator(scope);
			MemorySegment charSegmt = allocator.allocate(JAVA_CHAR, 'B');
			char result = (char)mh.invoke(charSegmt, 'D', upcallFuncAddr1);
			Assert.assertEquals(result, 'C');
		}

		try (ResourceScope scope = ResourceScope.newConfinedScope()) {
			NativeSymbol upcallFuncAddr1 = clinker.upcallStub(UpcallMethodHandles.MH_createNewCharFromCharAndCharFromPointer,
					FunctionDescriptor.of(JAVA_CHAR, ADDRESS, JAVA_CHAR), scope);
			SegmentAllocator allocator = SegmentAllocator.nativeAllocator(scope);
			MemorySegment charSegmt = allocator.allocate(JAVA_CHAR, 'B');
			char result = (char)mh.invoke(charSegmt, 'D', upcallFuncAddr1);
			Assert.assertEquals(result, 'C');
		}

		try (ResourceScope scope = ResourceScope.newConfinedScope()) {
			NativeSymbol upcallFuncAddr1 = clinker.upcallStub(UpcallMethodHandles.MH_createNewCharFromCharAndCharFromPointer,
					FunctionDescriptor.of(JAVA_CHAR, ADDRESS, JAVA_CHAR), scope);
			SegmentAllocator allocator = SegmentAllocator.nativeAllocator(scope);
			MemorySegment charSegmt = allocator.allocate(JAVA_CHAR, 'B');
			char result = (char)mh.invoke(charSegmt, 'D', upcallFuncAddr1);
			Assert.assertEquals(result, 'C');
		}
	}

	@Test
	public void test_addByteAndByteFromNativePtrByUpcallMH_SameScope() throws Throwable {
		FunctionDescriptor fd = FunctionDescriptor.of(JAVA_BYTE, JAVA_BYTE, ADDRESS);
		NativeSymbol functionSymbol = nativeLibLookup.lookup("addByteAndByteFromNativePtrByUpcallMH").get();
		MethodHandle mh = clinker.downcallHandle(functionSymbol, fd);

		try (ResourceScope scope = ResourceScope.newConfinedScope()) {
			NativeSymbol upcallFuncAddr1 = clinker.upcallStub(UpcallMethodHandles.MH_addByteAndByteFromPointer,
					FunctionDescriptor.of(JAVA_BYTE, JAVA_BYTE, ADDRESS), scope);
			byte result = (byte)mh.invoke((byte)33, upcallFuncAddr1);
			Assert.assertEquals(result, (byte)88);

			NativeSymbol upcallFuncAddr2 = clinker.upcallStub(UpcallMethodHandles.MH_addByteAndByteFromPointer,
					FunctionDescriptor.of(JAVA_BYTE, JAVA_BYTE, ADDRESS), scope);
			result = (byte)mh.invoke((byte)33, upcallFuncAddr2);
			Assert.assertEquals(result, (byte)88);

			NativeSymbol upcallFuncAddr3 = clinker.upcallStub(UpcallMethodHandles.MH_addByteAndByteFromPointer,
					FunctionDescriptor.of(JAVA_BYTE, JAVA_BYTE, ADDRESS), scope);
			result = (byte)mh.invoke((byte)33, upcallFuncAddr3);
			Assert.assertEquals(result, (byte)88);
		}
	}

	@Test
	public void test_addByteAndByteFromNativePtrByUpcallMH_DiffScope() throws Throwable {
		FunctionDescriptor fd = FunctionDescriptor.of(JAVA_BYTE, JAVA_BYTE, ADDRESS);
		NativeSymbol functionSymbol = nativeLibLookup.lookup("addByteAndByteFromNativePtrByUpcallMH").get();
		MethodHandle mh = clinker.downcallHandle(functionSymbol, fd);

		try (ResourceScope scope = ResourceScope.newConfinedScope()) {
			NativeSymbol upcallFuncAddr = clinker.upcallStub(UpcallMethodHandles.MH_addByteAndByteFromPointer,
					FunctionDescriptor.of(JAVA_BYTE, JAVA_BYTE, ADDRESS), scope);
			byte result = (byte)mh.invoke((byte)33, upcallFuncAddr);
			Assert.assertEquals(result, (byte)88);
		}

		try (ResourceScope scope = ResourceScope.newConfinedScope()) {
			NativeSymbol upcallFuncAddr = clinker.upcallStub(UpcallMethodHandles.MH_addByteAndByteFromPointer,
					FunctionDescriptor.of(JAVA_BYTE, JAVA_BYTE, ADDRESS), scope);
			byte result = (byte)mh.invoke((byte)33, upcallFuncAddr);
			Assert.assertEquals(result, (byte)88);
		}

		try (ResourceScope scope = ResourceScope.newConfinedScope()) {
			NativeSymbol upcallFuncAddr = clinker.upcallStub(UpcallMethodHandles.MH_addByteAndByteFromPointer,
					FunctionDescriptor.of(JAVA_BYTE, JAVA_BYTE, ADDRESS), scope);
			byte result = (byte)mh.invoke((byte)33, upcallFuncAddr);
			Assert.assertEquals(result, (byte)88);
		}
	}

	@Test
	public void test_addShortAndShortFromPtr_RetPtr_ByUpcallMH_SameScope() throws Throwable {
		FunctionDescriptor fd = FunctionDescriptor.of(ADDRESS, ADDRESS, JAVA_SHORT, ADDRESS);
		NativeSymbol functionSymbol = nativeLibLookup.lookup("addShortAndShortFromPtr_RetPtr_ByUpcallMH").get();
		MethodHandle mh = clinker.downcallHandle(functionSymbol, fd);

		try (ResourceScope scope = ResourceScope.newConfinedScope()) {
			SegmentAllocator allocator = SegmentAllocator.nativeAllocator(scope);

			NativeSymbol upcallFuncAddr1 = clinker.upcallStub(UpcallMethodHandles.MH_addShortAndShortFromPtr_RetPtr,
					FunctionDescriptor.of(ADDRESS, ADDRESS, JAVA_SHORT), scope);
			MemorySegment shortSegmt1 = allocator.allocate(JAVA_SHORT, (short)444);
			MemoryAddress resultAddr1 = (MemoryAddress)mh.invoke(shortSegmt1, (short)555, upcallFuncAddr1);
			Assert.assertEquals(resultAddr1.get(JAVA_SHORT, 0), (short)999);

			NativeSymbol upcallFuncAddr2 = clinker.upcallStub(UpcallMethodHandles.MH_addShortAndShortFromPtr_RetPtr,
					FunctionDescriptor.of(ADDRESS, ADDRESS, JAVA_SHORT), scope);
			MemorySegment shortSegmt2 = allocator.allocate(JAVA_SHORT, (short)444);
			MemoryAddress resultAddr2 = (MemoryAddress)mh.invoke(shortSegmt2, (short)555, upcallFuncAddr2);
			Assert.assertEquals(resultAddr2.get(JAVA_SHORT, 0), (short)999);

			NativeSymbol upcallFuncAddr3 = clinker.upcallStub(UpcallMethodHandles.MH_addShortAndShortFromPtr_RetPtr,
					FunctionDescriptor.of(ADDRESS, ADDRESS, JAVA_SHORT), scope);
			MemorySegment shortSegmt3 = allocator.allocate(JAVA_SHORT, (short)444);
			MemoryAddress resultAddr3 = (MemoryAddress)mh.invoke(shortSegmt3, (short)555, upcallFuncAddr3);
			Assert.assertEquals(resultAddr3.get(JAVA_SHORT, 0), (short)999);
		}
	}

	@Test
	public void test_addShortAndShortFromPtr_RetPtr_ByUpcallMH_DiffScope() throws Throwable {
		FunctionDescriptor fd = FunctionDescriptor.of(ADDRESS, ADDRESS, JAVA_SHORT, ADDRESS);
		NativeSymbol functionSymbol = nativeLibLookup.lookup("addShortAndShortFromPtr_RetPtr_ByUpcallMH").get();
		MethodHandle mh = clinker.downcallHandle(functionSymbol, fd);

		try (ResourceScope scope = ResourceScope.newConfinedScope()) {
			NativeSymbol upcallFuncAddr = clinker.upcallStub(UpcallMethodHandles.MH_addShortAndShortFromPtr_RetPtr,
					FunctionDescriptor.of(ADDRESS, ADDRESS, JAVA_SHORT), scope);
			SegmentAllocator allocator = SegmentAllocator.nativeAllocator(scope);
			MemorySegment shortSegmt = allocator.allocate(JAVA_SHORT, (short)444);
			MemoryAddress resultAddr = (MemoryAddress)mh.invoke(shortSegmt, (short)555, upcallFuncAddr);
			Assert.assertEquals(resultAddr.get(JAVA_SHORT, 0), (short)999);
		}

		try (ResourceScope scope = ResourceScope.newConfinedScope()) {
			NativeSymbol upcallFuncAddr = clinker.upcallStub(UpcallMethodHandles.MH_addShortAndShortFromPtr_RetPtr,
					FunctionDescriptor.of(ADDRESS, ADDRESS, JAVA_SHORT), scope);
			SegmentAllocator allocator = SegmentAllocator.nativeAllocator(scope);
			MemorySegment shortSegmt = allocator.allocate(JAVA_SHORT, (short)444);
			MemoryAddress resultAddr = (MemoryAddress)mh.invoke(shortSegmt, (short)555, upcallFuncAddr);
			Assert.assertEquals(resultAddr.get(JAVA_SHORT, 0), (short)999);
		}

		try (ResourceScope scope = ResourceScope.newConfinedScope()) {
			NativeSymbol upcallFuncAddr = clinker.upcallStub(UpcallMethodHandles.MH_addShortAndShortFromPtr_RetPtr,
					FunctionDescriptor.of(ADDRESS, ADDRESS, JAVA_SHORT), scope);
			SegmentAllocator allocator = SegmentAllocator.nativeAllocator(scope);
			MemorySegment shortSegmt = allocator.allocate(JAVA_SHORT, (short)444);
			MemoryAddress resultAddr = (MemoryAddress)mh.invoke(shortSegmt, (short)555, upcallFuncAddr);
			Assert.assertEquals(resultAddr.get(JAVA_SHORT, 0), (short)999);
		}
	}

	@Test
	public void test_addTwoIntsByUpcallMH_SameScope() throws Throwable {
		FunctionDescriptor fd = FunctionDescriptor.of(JAVA_INT, JAVA_INT, JAVA_INT, ADDRESS);
		NativeSymbol functionSymbol = nativeLibLookup.lookup("add2IntsByUpcallMH").get();
		MethodHandle mh = clinker.downcallHandle(functionSymbol, fd);

		try (ResourceScope scope = ResourceScope.newConfinedScope()) {
			NativeSymbol upcallFuncAddr1 = clinker.upcallStub(UpcallMethodHandles.MH_add2Ints,
					FunctionDescriptor.of(JAVA_INT, JAVA_INT, JAVA_INT), scope);
			int result = (int)mh.invoke(111112, 111123, upcallFuncAddr1);
			Assert.assertEquals(result, 222235);

			NativeSymbol upcallFuncAddr2 = clinker.upcallStub(UpcallMethodHandles.MH_add2Ints,
					FunctionDescriptor.of(JAVA_INT, JAVA_INT, JAVA_INT), scope);
			result = (int)mh.invoke(111112, 111123, upcallFuncAddr2);
			Assert.assertEquals(result, 222235);

			NativeSymbol upcallFuncAddr3 = clinker.upcallStub(UpcallMethodHandles.MH_add2Ints,
					FunctionDescriptor.of(JAVA_INT, JAVA_INT, JAVA_INT), scope);
			result = (int)mh.invoke(111112, 111123, upcallFuncAddr3);
			Assert.assertEquals(result, 222235);
		}
	}

	@Test
	public void test_addTwoIntsByUpcallMH_DiffScope() throws Throwable {
		FunctionDescriptor fd = FunctionDescriptor.of(JAVA_INT, JAVA_INT, JAVA_INT, ADDRESS);
		NativeSymbol functionSymbol = nativeLibLookup.lookup("add2IntsByUpcallMH").get();
		MethodHandle mh = clinker.downcallHandle(functionSymbol, fd);

		try (ResourceScope scope = ResourceScope.newConfinedScope()) {
			NativeSymbol upcallFuncAddr = clinker.upcallStub(UpcallMethodHandles.MH_add2Ints,
					FunctionDescriptor.of(JAVA_INT, JAVA_INT, JAVA_INT), scope);
			int result = (int)mh.invoke(111112, 111123, upcallFuncAddr);
			Assert.assertEquals(result, 222235);
		}

		try (ResourceScope scope = ResourceScope.newConfinedScope()) {
			NativeSymbol upcallFuncAddr = clinker.upcallStub(UpcallMethodHandles.MH_add2Ints,
					FunctionDescriptor.of(JAVA_INT, JAVA_INT, JAVA_INT), scope);
			int result = (int)mh.invoke(111112, 111123, upcallFuncAddr);
			Assert.assertEquals(result, 222235);
		}

		try (ResourceScope scope = ResourceScope.newConfinedScope()) {
			NativeSymbol upcallFuncAddr = clinker.upcallStub(UpcallMethodHandles.MH_add2Ints,
					FunctionDescriptor.of(JAVA_INT, JAVA_INT, JAVA_INT), scope);
			int result = (int)mh.invoke(111112, 111123, upcallFuncAddr);
			Assert.assertEquals(result, 222235);
		}
	}

	@Test
	public void test_addTwoIntsReturnVoidByUpcallMH_SameScope() throws Throwable {
		FunctionDescriptor fd = FunctionDescriptor.ofVoid(JAVA_INT, JAVA_INT, ADDRESS);
		NativeSymbol functionSymbol = nativeLibLookup.lookup("add2IntsReturnVoidByUpcallMH").get();
		MethodHandle mh = clinker.downcallHandle(functionSymbol, fd);

		try (ResourceScope scope = ResourceScope.newConfinedScope()) {
			NativeSymbol upcallFuncAddr1 = clinker.upcallStub(UpcallMethodHandles.MH_add2IntsReturnVoid,
					FunctionDescriptor.ofVoid(JAVA_INT, JAVA_INT), scope);
			mh.invoke(111454, 111398, upcallFuncAddr1);

			NativeSymbol upcallFuncAddr2 = clinker.upcallStub(UpcallMethodHandles.MH_add2IntsReturnVoid,
					FunctionDescriptor.ofVoid(JAVA_INT, JAVA_INT), scope);
			mh.invoke(111454, 111398, upcallFuncAddr2);

			NativeSymbol upcallFuncAddr3 = clinker.upcallStub(UpcallMethodHandles.MH_add2IntsReturnVoid,
					FunctionDescriptor.ofVoid(JAVA_INT, JAVA_INT), scope);
			mh.invoke(111454, 111398, upcallFuncAddr3);
		}
	}

	@Test
	public void test_addTwoIntsReturnVoidByUpcallMH_DiffScope() throws Throwable {
		FunctionDescriptor fd = FunctionDescriptor.ofVoid(JAVA_INT, JAVA_INT, ADDRESS);
		NativeSymbol functionSymbol = nativeLibLookup.lookup("add2IntsReturnVoidByUpcallMH").get();
		MethodHandle mh = clinker.downcallHandle(functionSymbol, fd);

		try (ResourceScope scope = ResourceScope.newConfinedScope()) {
			NativeSymbol upcallFuncAddr = clinker.upcallStub(UpcallMethodHandles.MH_add2IntsReturnVoid,
					FunctionDescriptor.ofVoid(JAVA_INT, JAVA_INT), scope);
			mh.invoke(111454, 111398, upcallFuncAddr);
		}

		try (ResourceScope scope = ResourceScope.newConfinedScope()) {
			NativeSymbol upcallFuncAddr = clinker.upcallStub(UpcallMethodHandles.MH_add2IntsReturnVoid,
					FunctionDescriptor.ofVoid(JAVA_INT, JAVA_INT), scope);
			mh.invoke(111454, 111398, upcallFuncAddr);
		}

		try (ResourceScope scope = ResourceScope.newConfinedScope()) {
			NativeSymbol upcallFuncAddr = clinker.upcallStub(UpcallMethodHandles.MH_add2IntsReturnVoid,
					FunctionDescriptor.ofVoid(JAVA_INT, JAVA_INT), scope);
			mh.invoke(111454, 111398, upcallFuncAddr);
		}
	}

	@Test
	public void test_addLongAndLongFromPointerByUpcallMH_SameScope() throws Throwable {
		FunctionDescriptor fd = FunctionDescriptor.of(JAVA_LONG, ADDRESS, JAVA_LONG, ADDRESS);
		NativeSymbol functionSymbol = nativeLibLookup.lookup("addLongAndLongFromPointerByUpcallMH").get();
		MethodHandle mh = clinker.downcallHandle(functionSymbol, fd);

		try (ResourceScope scope = ResourceScope.newConfinedScope()) {
			SegmentAllocator allocator = SegmentAllocator.nativeAllocator(scope);

			NativeSymbol upcallFuncAddr1 = clinker.upcallStub(UpcallMethodHandles.MH_addLongAndLongFromPointer,
					FunctionDescriptor.of(JAVA_LONG, ADDRESS, JAVA_LONG), scope);
			MemorySegment longSegmt1 = allocator.allocate(JAVA_LONG, 5742457424L);
			long result = (long)mh.invoke(longSegmt1, 6666698235L, upcallFuncAddr1);
			Assert.assertEquals(result, 12409155659L);

			NativeSymbol upcallFuncAddr2 = clinker.upcallStub(UpcallMethodHandles.MH_addLongAndLongFromPointer,
					FunctionDescriptor.of(JAVA_LONG, ADDRESS, JAVA_LONG), scope);
			MemorySegment longSegmt2 = allocator.allocate(JAVA_LONG, 5742457424L);
			result = (long)mh.invoke(longSegmt2, 6666698235L, upcallFuncAddr2);
			Assert.assertEquals(result, 12409155659L);

			NativeSymbol upcallFuncAddr3 = clinker.upcallStub(UpcallMethodHandles.MH_addLongAndLongFromPointer,
					FunctionDescriptor.of(JAVA_LONG, ADDRESS, JAVA_LONG), scope);
			MemorySegment longSegmt3 = allocator.allocate(JAVA_LONG, 5742457424L);
			result = (long)mh.invoke(longSegmt3, 6666698235L, upcallFuncAddr3);
			Assert.assertEquals(result, 12409155659L);
		}
	}

	@Test
	public void test_addLongAndLongFromPointerByUpcallMH_DiffScope() throws Throwable {
		FunctionDescriptor fd = FunctionDescriptor.of(JAVA_LONG, ADDRESS, JAVA_LONG, ADDRESS);
		NativeSymbol functionSymbol = nativeLibLookup.lookup("addLongAndLongFromPointerByUpcallMH").get();
		MethodHandle mh = clinker.downcallHandle(functionSymbol, fd);

		try (ResourceScope scope = ResourceScope.newConfinedScope()) {
			NativeSymbol upcallFuncAddr = clinker.upcallStub(UpcallMethodHandles.MH_addLongAndLongFromPointer,
					FunctionDescriptor.of(JAVA_LONG, ADDRESS, JAVA_LONG), scope);
			SegmentAllocator allocator = SegmentAllocator.nativeAllocator(scope);
			MemorySegment longSegmt = allocator.allocate(JAVA_LONG, 5742457424L);
			long result = (long)mh.invoke(longSegmt, 6666698235L, upcallFuncAddr);
			Assert.assertEquals(result, 12409155659L);
		}

		try (ResourceScope scope = ResourceScope.newConfinedScope()) {
			NativeSymbol upcallFuncAddr = clinker.upcallStub(UpcallMethodHandles.MH_addLongAndLongFromPointer,
					FunctionDescriptor.of(JAVA_LONG, ADDRESS, JAVA_LONG), scope);
			SegmentAllocator allocator = SegmentAllocator.nativeAllocator(scope);
			MemorySegment longSegmt = allocator.allocate(JAVA_LONG, 5742457424L);
			long result = (long)mh.invoke(longSegmt, 6666698235L, upcallFuncAddr);
			Assert.assertEquals(result, 12409155659L);
		}

		try (ResourceScope scope = ResourceScope.newConfinedScope()) {
			NativeSymbol upcallFuncAddr = clinker.upcallStub(UpcallMethodHandles.MH_addLongAndLongFromPointer,
					FunctionDescriptor.of(JAVA_LONG, ADDRESS, JAVA_LONG), scope);
			SegmentAllocator allocator = SegmentAllocator.nativeAllocator(scope);
			MemorySegment longSegmt = allocator.allocate(JAVA_LONG, 5742457424L);
			long result = (long)mh.invoke(longSegmt, 6666698235L, upcallFuncAddr);
			Assert.assertEquals(result, 12409155659L);
		}
	}

	@Test
	public void test_addFloatAndFloatFromNativePtrByUpcallMH_SameScope() throws Throwable {
		FunctionDescriptor fd = FunctionDescriptor.of(JAVA_FLOAT, JAVA_FLOAT, ADDRESS);
		NativeSymbol functionSymbol = nativeLibLookup.lookup("addFloatAndFloatFromNativePtrByUpcallMH").get();
		MethodHandle mh = clinker.downcallHandle(functionSymbol, fd);

		try (ResourceScope scope = ResourceScope.newConfinedScope()) {
			NativeSymbol upcallFuncAddr1 = clinker.upcallStub(UpcallMethodHandles.MH_addFloatAndFloatFromPointer,
					FunctionDescriptor.of(JAVA_FLOAT, JAVA_FLOAT, ADDRESS), scope);
			float result = (float)mh.invoke(5.74F, upcallFuncAddr1);
			Assert.assertEquals(result, 12.53F, 0.01F);

			NativeSymbol upcallFuncAddr2 = clinker.upcallStub(UpcallMethodHandles.MH_addFloatAndFloatFromPointer,
					FunctionDescriptor.of(JAVA_FLOAT, JAVA_FLOAT, ADDRESS), scope);
			result = (float)mh.invoke(5.74F, upcallFuncAddr2);
			Assert.assertEquals(result, 12.53F, 0.01F);

			NativeSymbol upcallFuncAddr3 = clinker.upcallStub(UpcallMethodHandles.MH_addFloatAndFloatFromPointer,
					FunctionDescriptor.of(JAVA_FLOAT, JAVA_FLOAT, ADDRESS), scope);
			result = (float)mh.invoke(5.74F, upcallFuncAddr3);
			Assert.assertEquals(result, 12.53F, 0.01F);
		}
	}

	@Test
	public void test_addFloatAndFloatFromNativePtrByUpcallMH_DiffScope() throws Throwable {
		FunctionDescriptor fd = FunctionDescriptor.of(JAVA_FLOAT, JAVA_FLOAT, ADDRESS);
		NativeSymbol functionSymbol = nativeLibLookup.lookup("addFloatAndFloatFromNativePtrByUpcallMH").get();
		MethodHandle mh = clinker.downcallHandle(functionSymbol, fd);

		try (ResourceScope scope = ResourceScope.newConfinedScope()) {
			NativeSymbol upcallFuncAddr = clinker.upcallStub(UpcallMethodHandles.MH_addFloatAndFloatFromPointer,
					FunctionDescriptor.of(JAVA_FLOAT, JAVA_FLOAT, ADDRESS), scope);
			float result = (float)mh.invoke(5.74F, upcallFuncAddr);
			Assert.assertEquals(result, 12.53F, 0.01F);
		}

		try (ResourceScope scope = ResourceScope.newConfinedScope()) {
			NativeSymbol upcallFuncAddr = clinker.upcallStub(UpcallMethodHandles.MH_addFloatAndFloatFromPointer,
					FunctionDescriptor.of(JAVA_FLOAT, JAVA_FLOAT, ADDRESS), scope);
			float result = (float)mh.invoke(5.74F, upcallFuncAddr);
			Assert.assertEquals(result, 12.53F, 0.01F);
		}

		try (ResourceScope scope = ResourceScope.newConfinedScope()) {
			NativeSymbol upcallFuncAddr = clinker.upcallStub(UpcallMethodHandles.MH_addFloatAndFloatFromPointer,
					FunctionDescriptor.of(JAVA_FLOAT, JAVA_FLOAT, ADDRESS), scope);
			float result = (float)mh.invoke(5.74F, upcallFuncAddr);
			Assert.assertEquals(result, 12.53F, 0.01F);
		}
	}

	@Test
	public void test_addDoubleAndDoubleFromPtr_RetPtr_ByUpcallMH_SameScope() throws Throwable {
		FunctionDescriptor fd = FunctionDescriptor.of(ADDRESS, ADDRESS, JAVA_DOUBLE, ADDRESS);
		NativeSymbol functionSymbol = nativeLibLookup.lookup("addDoubleAndDoubleFromPtr_RetPtr_ByUpcallMH").get();
		MethodHandle mh = clinker.downcallHandle(functionSymbol, fd);

		try (ResourceScope scope = ResourceScope.newConfinedScope()) {
			SegmentAllocator allocator = SegmentAllocator.nativeAllocator(scope);

			NativeSymbol upcallFuncAddr1 = clinker.upcallStub(UpcallMethodHandles.MH_addDoubleAndDoubleFromPtr_RetPtr,
					FunctionDescriptor.of(ADDRESS, ADDRESS, JAVA_DOUBLE), scope);
			MemorySegment doubleSegmt1 = allocator.allocate(JAVA_DOUBLE, 1159.748D);
			MemoryAddress resultAddr1 = (MemoryAddress)mh.invoke(doubleSegmt1, 1262.795D, upcallFuncAddr1);
			Assert.assertEquals(resultAddr1.get(JAVA_DOUBLE, 0), 2422.543D, 0.001D);

			NativeSymbol upcallFuncAddr2 = clinker.upcallStub(UpcallMethodHandles.MH_addDoubleAndDoubleFromPtr_RetPtr,
					FunctionDescriptor.of(ADDRESS, ADDRESS, JAVA_DOUBLE), scope);
			MemorySegment doubleSegmt2 = allocator.allocate(JAVA_DOUBLE, 1159.748D);
			MemoryAddress resultAddr2 = (MemoryAddress)mh.invoke(doubleSegmt2, 1262.795D, upcallFuncAddr2);
			Assert.assertEquals(resultAddr2.get(JAVA_DOUBLE, 0), 2422.543D, 0.001D);

			NativeSymbol upcallFuncAddr3 = clinker.upcallStub(UpcallMethodHandles.MH_addDoubleAndDoubleFromPtr_RetPtr,
					FunctionDescriptor.of(ADDRESS, ADDRESS, JAVA_DOUBLE), scope);
			MemorySegment doubleSegmt3 = allocator.allocate(JAVA_DOUBLE, 1159.748D);
			MemoryAddress resultAddr3 = (MemoryAddress)mh.invoke(doubleSegmt3, 1262.795D, upcallFuncAddr3);
			Assert.assertEquals(resultAddr3.get(JAVA_DOUBLE, 0), 2422.543D, 0.001D);
		}
	}

	@Test
	public void test_addDoubleAndDoubleFromPtr_RetPtr_ByUpcallMH_DiffScope() throws Throwable {
		FunctionDescriptor fd = FunctionDescriptor.of(ADDRESS, ADDRESS, JAVA_DOUBLE, ADDRESS);
		NativeSymbol functionSymbol = nativeLibLookup.lookup("addDoubleAndDoubleFromPtr_RetPtr_ByUpcallMH").get();
		MethodHandle mh = clinker.downcallHandle(functionSymbol, fd);

		try (ResourceScope scope = ResourceScope.newConfinedScope()) {
			NativeSymbol upcallFuncAddr = clinker.upcallStub(UpcallMethodHandles.MH_addDoubleAndDoubleFromPtr_RetPtr,
					FunctionDescriptor.of(ADDRESS, ADDRESS, JAVA_DOUBLE), scope);
			SegmentAllocator allocator = SegmentAllocator.nativeAllocator(scope);
			MemorySegment doubleSegmt = allocator.allocate(JAVA_DOUBLE, 1159.748D);
			MemoryAddress resultAddr = (MemoryAddress)mh.invoke(doubleSegmt, 1262.795D, upcallFuncAddr);
			Assert.assertEquals(resultAddr.get(JAVA_DOUBLE, 0), 2422.543D, 0.001D);
		}

		try (ResourceScope scope = ResourceScope.newConfinedScope()) {
			NativeSymbol upcallFuncAddr = clinker.upcallStub(UpcallMethodHandles.MH_addDoubleAndDoubleFromPtr_RetPtr,
					FunctionDescriptor.of(ADDRESS, ADDRESS, JAVA_DOUBLE), scope);
			SegmentAllocator allocator = SegmentAllocator.nativeAllocator(scope);
			MemorySegment doubleSegmt = allocator.allocate(JAVA_DOUBLE, 1159.748D);
			MemoryAddress resultAddr = (MemoryAddress)mh.invoke(doubleSegmt, 1262.795D, upcallFuncAddr);
			Assert.assertEquals(resultAddr.get(JAVA_DOUBLE, 0), 2422.543D, 0.001D);
		}

		try (ResourceScope scope = ResourceScope.newConfinedScope()) {
			NativeSymbol upcallFuncAddr = clinker.upcallStub(UpcallMethodHandles.MH_addDoubleAndDoubleFromPtr_RetPtr,
					FunctionDescriptor.of(ADDRESS, ADDRESS, JAVA_DOUBLE), scope);
			SegmentAllocator allocator = SegmentAllocator.nativeAllocator(scope);
			MemorySegment doubleSegmt = allocator.allocate(JAVA_DOUBLE, 1159.748D);
			MemoryAddress resultAddr = (MemoryAddress)mh.invoke(doubleSegmt, 1262.795D, upcallFuncAddr);
			Assert.assertEquals(resultAddr.get(JAVA_DOUBLE, 0), 2422.543D, 0.001D);
		}
	}
}
