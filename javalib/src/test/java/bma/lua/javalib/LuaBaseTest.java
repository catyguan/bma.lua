package bma.lua.javalib;

import org.junit.Test;

import bma.lua.javalib.test.FunctionTest;

public class LuaBaseTest {

	@Test
	public void base_OpenClose() {
		LuaState L = LuaStateManager.newState();
		System.out.println(L);
		L.close();
	}

	@Test
	public void base_Api() {
		LuaState L = LuaStateManager.newState();
		System.out.println(L);
		Object v = null;

		v = L.luaAbsindex(-1);
		v = L.luaCheckStack(1);

		L.luaPushboolean(true);
		v = L.luaToboolean(-1);
		L.luaPop(1);

		L.luaPushinteger(100);
		v = L.luaTointeger(1);
		v = L.luaTostring(1);
		L.luaPop(1);

		L.luaPushnumber(100.3);
		v = L.luaTostring(1);
		L.luaPop(1);

		L.luaPushFunction(new FunctionTest());
		int t = L.luaType(1);
		v = L.luaTypename(t);
		L.luaPushstring("world");
		// L.luaPushstring("");
		L.luaPcall(1, 1, 0);
		v = L.luaTostring(-1);
		L.luaPop(1);

		L.luaLOpenlibs();
		if (L.luaLLoadfile("test.lua") > 0) {
			v = L.luaTostring(-1);
			L.luaPop(1);
		} else {
			v = "load success execute it";
			int x = L.luaPcall(0, LuaState.LUA_MULTRET, 0);
			if (x > 0)
				L.luaPop(x);
		}

		v = L.luaLoad(null, "print(\"java loader\");", "testLoader");
		// L.luaPop(1);
		L.luaCall(0, 0);
		Exception e;
		
		System.out.println(v);
		L.close();
	}

}
