package bma.lua.javalib.test;

import bma.lua.javalib.LuaException;
import bma.lua.javalib.LuaFunction;
import bma.lua.javalib.LuaState;

public class FunctionTest implements LuaFunction {

	@Override
	public int execute(LuaState L) throws LuaException {
		String s = L.luaGettop() > 0 ? L.luaTostring(-1) : null;
		if (s == null || s.isEmpty()) {
			throw new NullPointerException("message is empty");
		}
		System.out.println("hello " + s + " from " + L);
		L.luaPushinteger(12);
		return 1;
	}

}
