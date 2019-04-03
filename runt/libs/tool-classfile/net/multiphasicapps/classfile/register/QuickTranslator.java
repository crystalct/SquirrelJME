// -*- Mode: Java; indent-tabs-mode: t; tab-width: 4 -*-
// ---------------------------------------------------------------------------
// Multi-Phasic Applications: SquirrelJME
//     Copyright (C) Stephanie Gawroriski <xer@multiphasicapps.net>
// ---------------------------------------------------------------------------
// SquirrelJME is under the GNU General Public License v3+, or later.
// See license.mkd for licensing and copyright information.
// ---------------------------------------------------------------------------

package net.multiphasicapps.classfile.register;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.multiphasicapps.classfile.ByteCode;
import net.multiphasicapps.classfile.ClassName;
import net.multiphasicapps.classfile.ConstantValue;
import net.multiphasicapps.classfile.ConstantValueNumber;
import net.multiphasicapps.classfile.ExceptionHandler;
import net.multiphasicapps.classfile.ExceptionHandlerTable;
import net.multiphasicapps.classfile.FieldDescriptor;
import net.multiphasicapps.classfile.FieldReference;
import net.multiphasicapps.classfile.Instruction;
import net.multiphasicapps.classfile.InstructionIndex;
import net.multiphasicapps.classfile.InstructionJumpTarget;
import net.multiphasicapps.classfile.InstructionJumpTargets;
import net.multiphasicapps.classfile.JavaType;
import net.multiphasicapps.classfile.MethodDescriptor;
import net.multiphasicapps.classfile.MethodHandle;
import net.multiphasicapps.classfile.MethodName;
import net.multiphasicapps.classfile.MethodReference;
import net.multiphasicapps.classfile.PrimitiveType;
import net.multiphasicapps.classfile.StackMapTable;
import net.multiphasicapps.classfile.StackMapTableState;

/**
 * This is a translator which is designed to run as quick as possible while
 * translating all of the instructions.
 *
 * @since 2019/04/03
 */
public class QuickTranslator
	implements Translator
{
	/** The byte code to translate. */
	protected final ByteCode bytecode;
	
	/** Used to build register codes. */
	protected final RegisterCodeBuilder codebuilder =
		new RegisterCodeBuilder();
	
	/** Exception tracker. */
	protected final __ExceptionTracker__ exceptiontracker;
	
	/** Default field access type, to determine how fields are accessed. */
	protected final FieldAccessTime defaultfieldaccesstime;
	
	/** The stacks which have been recorded. */
	private final Map<Integer, JavaStackState> _stacks =
		new LinkedHashMap<>();
	
	/** The current state of the stack. */
	private JavaStackState _stack;
	
	/** The current address being processed. */
	private int _addr =
		-1;
	
	/**
	 * Converts the input byte code to a register based code.
	 *
	 * @param __bc The byte code to translate.
	 * @throws NullPointerException On null arguments.
	 * @since 2019/04/03
	 */
	public QuickTranslator(ByteCode __bc)
		throws NullPointerException
	{
		if (__bc == null)
			throw new NullPointerException("NARG");
		
		this.bytecode = __bc;
		this.exceptiontracker = new __ExceptionTracker__(__bc);
		this.defaultfieldaccesstime = ((__bc.isInstanceInitializer() ||
			__bc.isStaticInitializer()) ? FieldAccessTime.INITIALIZER :
			FieldAccessTime.NORMAL);
			
		// Load initial Java stack state from the initial stack map
		JavaStackState s;
		this._stack = (s = JavaStackState.of(__bc.stackMapTable().get(0),
			__bc.writtenLocals()));
		this._stacks.put(0, s);
	}
	
	/**
	 * {@inheritDoc}
	 * @since 2019/04/03
	 */
	@Override
	public RegisterCode convert()
	{
		ByteCode bytecode = this.bytecode;
		RegisterCodeBuilder codebuilder = this.codebuilder;
		Map<Integer, JavaStackState> stacks = this._stacks;
		
		// Process every instruction
		for (Instruction inst : bytecode)
		{
			// Translate to simple instruction for easier handling
			SimplifiedJavaInstruction sji =
				new SimplifiedJavaInstruction(inst);
			
			// Debug
			todo.DEBUG.note("Xlate %s (%s)", sji, inst);
			
			// Current processing this address
			int addr = inst.address();
			this._addr = addr;
			
			// {@squirreljme.error JC37 No recorded stack state for this
			// position. (The address to check)}
			JavaStackState stack = stacks.get(addr);
			if (stack == null)
				throw new IllegalArgumentException("JC37 " + addr);
			
			// Add label to refer to this instruction in the Java instruction
			// space
			codebuilder.label("java", addr);
			
			// Handle the operation
			switch (sji.operation())
			{
					// Load local variable to the stack
				case SimplifiedJavaInstruction.LOAD:
					this.__doLoad(sji.<JavaType>argument(0, JavaType.class),
						sji.intArgument(1));
					break;
				
					// Load constant
				case InstructionIndex.LDC:
					this.__doLdc(sji.<ConstantValue>argument(0,
						ConstantValue.class));
					break;
					
					// This literally does nothing so no output code needs to
					// be generated at all
				case InstructionIndex.NOP:
					break;
				
					// Not yet implemented
				default:
					throw new todo.OOPS(
						sji.toString() + "/" + inst.toString());
			}
			
			// After the operation a new stack is now used
			JavaStackState newstack = this._stack;
			
			// Set target stack states for destinations of this instruction
			// Calculate the exception state only if it is needed
			JavaStackState hypoex = null;
			InstructionJumpTargets ijt = inst.jumpTargets();
			if (ijt != null && !ijt.isEmpty())
				for (int i = 0, n = ijt.size(); i < n; i++)
				{
					int jta = ijt.get(i).target();
					
					// Lazily calculate the exception handler since it might
					// not always be needed
					boolean isexception = ijt.isException(i);
					if (isexception && hypoex == null)
						hypoex = newstack.doExceptionHandler(new JavaType(
							new ClassName("java/lang/Throwable"))).after();
					
					// The type of stack to target
					JavaStackState use = (isexception ? hypoex : newstack);
					
					// Is empty state
					JavaStackState dss = stacks.get(jta);
					if (dss == null)
						stacks.put(jta, use);
				}
		}
		
		if (true)
			throw new todo.TODO();
		
		// Build the final code
		return codebuilder.build();
	}
	
	/**
	 * Loads constant value onto the stack.
	 *
	 * @param __v The value to push.
	 * @throws NullPointerException On null arguments.
	 * @since 2019/04/03
	 */
	private final void __doLdc(ConstantValue __v)
		throws NullPointerException
	{
		if (__v == null)
			throw new NullPointerException("NARG");
		
		// Get push properties
		JavaType jt = __v.type().javaType();
		
		// Push to the stack this type
		JavaStackResult result = this._stack.doStack(0, jt);
		this._stack = result.after();
		
		// Generate instruction
		RegisterCodeBuilder codebuilder = this.codebuilder;
		switch (__v.type())
		{
			case INTEGER:
				codebuilder.add(RegisterOperationType.X32_CONST,
					(Integer)__v.boxedValue(),
					result.out(0).register);
				break;
				
			case FLOAT:
				codebuilder.add(RegisterOperationType.X32_CONST,
					Float.floatToRawIntBits((Float)__v.boxedValue()),
					result.out(0).register);
				break;
			
			case LONG:
				codebuilder.add(RegisterOperationType.X64_CONST,
					__v.boxedValue(),
					result.out(0).register);
				break;
				
			case DOUBLE:
				codebuilder.add(RegisterOperationType.X64_CONST,
					Double.doubleToRawLongBits((Double)__v.boxedValue()),
					result.out(0).register);
				break;
			
			case STRING:
			case CLASS:
				codebuilder.add(RegisterOperationType.LOAD_POOL_VALUE,
					__v.boxedValue(), result.out(0).register);
				codebuilder.add(RegisterOperationType.COUNT,
					result.out(0).register);
				break;
			
			default:
				throw new todo.OOPS();
		}
	}
	
	/**
	 * Loads from a local and puts to the stack.
	 *
	 * @param __jt The type to push.
	 * @param __from The source local.
	 * @throws NullPointerException On null arguments.
	 * @since 2019/04/03
	 */
	private final void __doLoad(JavaType __jt, int __from)
		throws NullPointerException
	{
		if (__jt == null)
			throw new NullPointerException("NARG");
		
		// Push to the stack
		JavaStackResult result = this._stack.doStack(0, __jt);
		this._stack = result.after();
		
		// Do the copy
		this.codebuilder.add(DataType.of(__jt).copyOperation(false),
			result.before().getLocal(__from).register,
			result.out(0).register);
	}
}

