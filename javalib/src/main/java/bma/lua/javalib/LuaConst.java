package bma.lua.javalib;

public interface LuaConst {

	final public static Integer LUA_GLOBALSINDEX = new Integer(-10002);
	final public static Integer LUA_REGISTRYINDEX = new Integer(-10000);

	/**
	 * lua的数据类型
	 */
	final public static int LUA_TNONE = -1;
	final public static int LUA_TNIL = 0;
	final public static int LUA_TBOOLEAN = 1;
	final public static int LUA_TLIGHTUSERDATA = 2;
	final public static int LUA_TNUMBER = 3;
	final public static int LUA_TSTRING = 4;
	final public static int LUA_TTABLE = 5;
	final public static int LUA_TFUNCTION = 6;
	final public static int LUA_TUSERDATA = 7;
	final public static int LUA_TTHREAD = 8;

	/**
	 * Specifies that an unspecified (multiple) number of return arguments will
	 * be returned by a call.
	 */
	final public static int LUA_MULTRET = -1;

	/*
	 * error codes for `lua_load' and `lua_pcall'
	 */
	final public static int LUA_YIELD = 1;
	
	final public static int LUA_OK = 0;

	/** a runtime error. */
	final public static int LUA_ERRRUN = 2;

	/** syntax error during pre-compilation. */
	final public static int LUA_ERRSYNTAX = 3;

	/**
	 * memory allocation error. For such errors, Lua does not call the error
	 * handler function.
	 */
	final public static int LUA_ERRMEM = 4;

	/**
	 * error while running the error handler function.
	 */
	final public static int LUA_ERRERR = 5;

	/**
	 * GC
	 */
	final public static int LUA_GCSTOP = 0;
	final public static int LUA_GCRESTART = 1;
	final public static int LUA_GCCOLLECT = 2;
	final public static int LUA_GCCOUNT = 3;
	final public static int LUA_GCCOUNTB = 4;
	final public static int LUA_GCSTEP = 5;
	final public static int LUA_GCSETPAUSE = 6;
	final public static int LUA_GCSETSTEPMUL = 7;
}
