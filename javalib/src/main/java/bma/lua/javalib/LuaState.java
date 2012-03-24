/*
 * $Id: LuaState.java,v 1.11 2007-09-17 19:28:40 thiago Exp $
 * Copyright (C) 2003-2007 Kepler Project.
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 * IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
 * CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 * SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package bma.lua.javalib;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * 对应Lua_State对象
 * 
 * @author guanzhong
 * 
 */
public class LuaState implements LuaConst {

	private final static String LUAJAVA_LIB = "luajni-1.0";

	/**
	 * Opens the library containing the luajava API
	 */
	static {
		System.loadLibrary(LUAJAVA_LIB);
	}

	public static class NativeData {

		private long data;
		private int type;
		private long destroy;

		public boolean equals(Object other) {
			if (other == null)
				return false;
			if (other == this)
				return true;
			if (NativeData.class != other.getClass())
				return false;
			return data == ((NativeData) other).data;
		}

		public long getData() {
			return data;
		}

		public int getType() {
			return type;
		}

		protected void close() {
			destroy = data = type = 0;
		}

		@Override
		public String toString() {
			return type + ":0x" + Long.toHexString(data);
		}

		@Override
		protected void finalize() throws Throwable {
			if (this.destroy != 0) {
				_destroy(data, type, destroy);
				destroy = data = type = 0;
			}
			super.finalize();
		}

	}

	/********************* Lua Native Interface *************************/
	private native NativeData _open(int stateId);

	private static native void _destroy(long data, int type, long destroy);

	private native void _close(NativeData ptr);

	private native int _apiX(NativeData luaState, int api, int p1, int p2,
			int p3);

	private native Object _xapi(NativeData luaState, int api, Object p1,
			Object p2, Object p3);

	private native void _timeout(NativeData luaState, boolean begin,
			int timeoutSec);

	private static int c_function(int stateId, Object obj) {
		try {
			LuaState L = LuaStateManager.getState(stateId);
			if (L == null) {
				return -1;
			}
			if (obj instanceof LuaFunction) {
				LuaFunction f = (LuaFunction) obj;
				return f.execute(L);
			}
			return -2;
		} catch (Exception e) {
			StringWriter r = new StringWriter(1024);
			PrintWriter w = new PrintWriter(r);
			e.printStackTrace(w);
			w.close();
			throw new RuntimeException(r.toString());
		}
	}

	private static String c_loader(int stateId, Object loader, Object data,
			String src) {
		try {
			if (loader == null) {
				if (data instanceof String) {
					return (String) data;
				}
			}
			if (loader instanceof LuaLoader) {
				LuaLoader l = (LuaLoader) loader;
				return l.load(data, src);
			}
			return null;
		} catch (Exception e) {
			return null;
		}
	}

	/********************* end Lua Native Interface *********************/

	private NativeData luaState;

	private int stateId;

	/**
	 * Constructor to instance a new LuaState and initialize it with LuaJava's
	 * functions
	 * 
	 * @param stateId
	 */
	protected LuaState(int stateId) {
		luaState = _open(stateId);
		this.stateId = stateId;
	}

	/**
	 * Closes state and removes the object from the LuaStateFactory
	 */
	public void close() {
		if (this.luaState != null) {
			LuaStateManager.removeState(stateId);
			_close(luaState);
			this.luaState = null;
		}
	}

	@Override
	public String toString() {
		return "LuaState[" + stateId + "," + luaState + "]";
	}

	/**
	 * Returns <code>true</code> if state is closed.
	 */
	public boolean isOpen() {
		return luaState != null;
	}

	/**
	 * Return the long representing the LuaState pointer
	 * 
	 * @return long
	 */
	public long getNativePtr() {
		return (luaState != null) ? luaState.getData() : 0;
	}

	// ***************** API *************************
	public int luaAbsindex(int idx) {
		return _apiX(luaState, 1, idx, 0, 0);
	}

	public void luaOpAdd(int idx) {
		_apiX(luaState, 2, 0, 0, 0);
	}

	public void luaOpSub(int idx) {
		_apiX(luaState, 2, 1, 0, 0);
	}

	public void luaOpMul(int idx) {
		_apiX(luaState, 2, 2, 0, 0);
	}

	public void luaOpDiv(int idx) {
		_apiX(luaState, 2, 3, 0, 0);
	}

	public void luaOpMod(int idx) {
		_apiX(luaState, 2, 4, 0, 0);
	}

	public void luaOpPow(int idx) {
		_apiX(luaState, 2, 5, 0, 0);
	}

	public void luaOpUnm(int idx) {
		_apiX(luaState, 2, 6, 0, 0);
	}

	public void luaCall(int nargs, int nresult) {
		_apiX(luaState, 3, nargs, nresult, 0);
	}

	public boolean luaCheckStack(int extra) {
		return _apiX(luaState, 4, extra, 0, 0) != 0;
	}

	public boolean luaOpEq(int idx1, int idx2) {
		return _apiX(luaState, 5, idx1, idx2, 0) != 0;
	}

	public boolean luaOpLt(int idx1, int idx2) {
		return _apiX(luaState, 5, idx1, idx2, 1) != 0;
	}

	public boolean luaOpLe(int idx1, int idx2) {
		return _apiX(luaState, 5, idx1, idx2, 2) != 0;
	}

	public void luaConcat(int n) {
		_apiX(luaState, 6, n, 0, 0);
	}

	public void luaCopy(int from, int to) {
		_apiX(luaState, 7, from, to, 0);
	}

	public void luaCreatetable(int narr, int nrec) {
		_apiX(luaState, 8, narr, nrec, 0);
	}

	public int luaGc(int what, int data) {
		return _apiX(luaState, 10, what, data, 0);
	}

	public void luaGetfield(int index, String k) {
		// 11
		if (k == null)
			k = "";
		_xapi(luaState, 11, index, k, null);
	}

	public void luaGetglobal(String name) {
		// 12
		if (name == null)
			name = "";
		_xapi(luaState, 12, name, null, null);
	}

	public boolean luaGetmetatable(int index) {
		return _apiX(luaState, 13, index, 0, 0) != 0;
	}

	public void luaGettable(int index) {
		_apiX(luaState, 14, index, 0, 0);
	}

	public int luaGettop() {
		return _apiX(luaState, 15, 0, 0, 0);
	}

	public void luaGetuservalue(int index) {
		_apiX(luaState, 16, index, 0, 0);
	}

	public void luaInsert(int index) {
		_apiX(luaState, 17, index, 0, 0);
	}

	public boolean luaIsboolean(int index) {
		return _apiX(luaState, 18, index, 0, 0) == LUA_TBOOLEAN;
	}

	public boolean luaIscfunction(int index) {
		return _apiX(luaState, 19, index, 0, 0) != 0;
	}

	public boolean luaIslightuserdata(int index) {
		return _apiX(luaState, 18, index, 0, 0) == LUA_TLIGHTUSERDATA;
	}

	public boolean luaIsnil(int index) {
		return _apiX(luaState, 18, index, 0, 0) == LUA_TNIL;
	}

	public boolean luaIsnone(int index) {
		return _apiX(luaState, 18, index, 0, 0) == LUA_TNONE;
	}

	public boolean luaIsnoneornil(int index) {
		return _apiX(luaState, 18, index, 0, 0) <= 0;
	}

	public boolean luaIsnumber(int index) {
		return _apiX(luaState, 18, index, 0, 0) == LUA_TNUMBER;
	}

	public boolean luaIsstring(int index) {
		return _apiX(luaState, 18, index, 0, 0) == LUA_TSTRING;
	}

	public boolean luaIstable(int index) {
		return _apiX(luaState, 18, index, 0, 0) == LUA_TTABLE;
	}

	public boolean luaIsthread(int index) {
		return _apiX(luaState, 18, index, 0, 0) == LUA_TTHREAD;
	}

	public boolean luaIsuserdata(int index) {
		return _apiX(luaState, 18, index, 0, 0) == LUA_TUSERDATA;
	}

	public void luaLen(int index) {
		_apiX(luaState, 20, index, 0, 0);
	}

	public void luaNewtable() {
		luaCreatetable(0, 0);
	}

	public void luaNewuserdata(int size) {
		_apiX(luaState, 21, size, 0, 0);
	}

	public int luaNext(int index) {
		return _apiX(luaState, 22, index, 0, 0);
	}

	public int luaPcall(int nargs, int nresults, int msgh) {
		return _apiX(luaState, 23, nargs, nresults, msgh);
	}

	public void luaPop(int n) {
		_apiX(luaState, 24, n, 0, 0);
	}

	public void luaPushboolean(boolean v) {
		_apiX(luaState, 25, v ? 1 : 0, 0, 0);
	}

	public void luaPushinteger(int v) {
		_apiX(luaState, 26, v, 0, 0);
	}

	public void luaPushstring(String s) {
		// 27
		if (s == null) {
			luaPushnil();
		} else {
			_xapi(luaState, 27, s, null, null);
		}
	}

	public void luaPushnil() {
		_apiX(luaState, 28, 0, 0, 0);
	}

	public void luaPushnumber(Number v) {
		// 29
		if (v == null) {
			luaPushnil();
		} else {
			_xapi(luaState, 29, v, null, null);
		}
	}

	public void luaPushvalue(int index) {
		_apiX(luaState, 30, index, 0, 0);
	}

	public boolean luaRawequal(int index1, int index2) {
		return _apiX(luaState, 31, index1, index2, 0) != 0;
	}

	public void luaRawget(int index) {
		_apiX(luaState, 32, index, 0, 0);
	}

	public void luaRawgeti(int index, int n) {
		_apiX(luaState, 33, index, n, 0);
	}

	public int luaRawlen(int index) {
		return _apiX(luaState, 34, index, 0, 0);
	}

	public void luaRawset(int index) {
		_apiX(luaState, 35, index, 0, 0);
	}

	public void luaRawseti(int index, int n) {
		_apiX(luaState, 36, index, n, 0);
	}

	public void luaRemove(int index) {
		_apiX(luaState, 37, index, 0, 0);
	}

	public void luaReplace(int index) {
		_apiX(luaState, 38, index, 0, 0);
	}

	public void luaSetfield(int index, String k) {
		// 39
		if (k == null)
			k = "";
		_xapi(luaState, 39, index, k, null);
	}

	public void luaSetglobal(String k) {
		// 40
		if (k == null)
			k = "";
		_xapi(luaState, 40, k, null, null);
	}

	public void luaSetmetatable(int index) {
		_apiX(luaState, 41, index, 0, 0);
	}

	public void luaSettop(int index) {
		_apiX(luaState, 42, index, 0, 0);
	}

	public void luaSetuservalue(int index) {
		_apiX(luaState, 43, index, 0, 0);
	}

	public boolean luaToboolean(int index) {
		return _apiX(luaState, 44, index, 0, 0) != 0;
	}

	public int luaTointeger(int index) {
		return _apiX(luaState, 45, index, 0, 0);
	}

	public int luaTointegerx(int index, int def) {
		return _apiX(luaState, 46, index, def, 0);
	}

	public Number luaTonumber(int index) {
		// 47
		Object v = _xapi(luaState, 47, index, null, null);
		if (v == null)
			return null;
		if (v instanceof Number) {
			return (Number) v;
		}
		return null;
	}

	public String luaTostring(int index) {
		// 49
		Object v = _xapi(luaState, 49, index, null, null);
		if (v == null)
			return null;
		if (v instanceof String) {
			return (String) v;
		}
		return v.toString();
	}

	public NativeData luaTouserdata(int index) {
		// 50
		Object v = _xapi(luaState, 50, index, null, null);
		if (v == null)
			return null;
		if (v instanceof NativeData) {
			return (NativeData) v;
		}
		return null;
	}

	public int luaType(int index) {
		return _apiX(luaState, 51, index, 0, 0);
	}

	public String luaTypename(int index) {
		// 52
		Object v = _xapi(luaState, 52, index, null, null);
		if (v == null)
			return null;
		if (v instanceof String) {
			return (String) v;
		}
		return v.toString();
	}

	// 补充
	public void luaPushFunction(LuaFunction fun) {
		// 53
		if (fun == null)
			return;
		_xapi(luaState, 53, fun, null, null);
	}

	public int luaLoad(LuaLoader loader, Object data, String source) {
		Object v = _xapi(luaState, 54, loader, data, source);
		if (v != null) {
			if (v instanceof Integer) {
				Integer r = (Integer) v;
				return r.intValue();
			}
		}
		return LUA_ERRERR;
	}

	// 4.9 – The Debug Interface

	// 5 – The Auxiliary Library
	public void luaLOpenlibs() {
		// 5001
		_apiX(luaState, 5001, 0, 0, 0);
	}

	public int luaLLoadfile(String fileName) {
		// 5002
		Object v = _xapi(luaState, 5002, fileName, null, null);
		if (v != null) {
			if (v instanceof Integer) {
				Integer r = (Integer) v;
				return r.intValue();
			}
		}
		return LUA_ERRERR;
	}

	// helper
	public void setExecuteTimeout(boolean begin, int timeoutSec) {
		_timeout(luaState, begin, timeoutSec);
	}
}
