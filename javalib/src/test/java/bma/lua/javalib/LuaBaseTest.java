package bma.lua.javalib;

import org.junit.Test;

import bma.common.langutil.core.ToStringUtil;
import bma.lua.javalib.test.FunctionTest;

public class LuaBaseTest {

	@Test
	public void base_OpenClose() {
		LuaState L = LuaStateManager.newState();
		System.out.println(L);
		L.close();
	}

	@Test
	public void base_Api() throws Exception {
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

		try {
			L.luaPushFunction(new FunctionTest());
			int t = L.luaType(1);
			v = L.luaTypename(t);
			L.luaPushstring("world");
			// L.luaPushstring("");
			if (L.luaPcall(1, 1) != 0) {
				v = L.luaTostring(-1);
				System.out.println("error -- \n" + v);
			}
			L.luaPop(1);
		} catch (LuaException e) {
			System.out.println("call fail");
			e.printStackTrace(System.out);
		}

		L.luaLOpenlibs();
		if (L.luaLLoadfile("test.lua") > 0) {
			v = L.luaTostring(-1);
			L.luaPop(1);
		} else {
			v = "load success execute it";
			int x = L.luaPcall(0, LuaState.LUA_MULTRET);
			if (x > 0)
				L.luaPop(x);
		}

		v = L.luaLoad(null, "print(\"java loader 中文\");", "testLoader");
		L.luaCall(0, 0);

		try {
			v = L.luaLoad(null, "error(\"error raise by lua\");",
					"@/testLoader2.lua");
			L.luaCall(0, 0);
		} catch (Exception e) {
			System.out.println("lua error");
			e.printStackTrace(System.out);
		}

		try {
			L.luaPushFunction(new FunctionTest());
			L.luaSetglobal("f");
			v = L.luaLoad(null, "f();", "@/testLoader3.lua");
			L.luaCall(0, 0);
		} catch (Exception e) {
			System.out.println("lua/java error");
			e.printStackTrace(System.out);
		}

		System.out.println(v);
		L.close();
	}

	@Test
	public void debug_Api() throws Exception {
		LuaState L = LuaStateManager.newState();
		System.out.println(L);
		L.luaRegister("f", new LuaFunction() {

			@Override
			public int execute(LuaState L) throws LuaException {
				for (int i = 0;; i++) {
					Object v = L.getDebug("*", i);
					if (v != null) {
						System.out.println(ToStringUtil.fieldReflect(v));
					} else {
						System.out.println("<null>");
						break;
					}
				}
				return 0;
			}
		});
		L.luaLoad(null, "local a = function()\n f();\n end\na();",
				"=debug_Api_1");
		L.luaPcall(0, 0);
		L.close();
	}

	@Test
	public void base_Userdata() throws Exception {
		LuaState L = LuaStateManager.newState();
		System.out.println(L);

		L.luaNewuserdata(4);
		long ptr = L.luaTouserdata(-1);
		byte[] buf = new byte[] { 1, 2, 3, 4 };
		L.setUserData(ptr, 0, buf, 0, 4);
		byte[] r = L.getUserData(ptr, 0, 4);
		for (int i = 0; i < r.length; i++) {
			System.out.println(r[i]);
		}
		L.close();
	}
}
