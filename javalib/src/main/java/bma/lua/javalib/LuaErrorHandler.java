package bma.lua.javalib;

public interface LuaErrorHandler {

	public final static int ERR_TYPE_CALL = 0;
	public final static int ERR_TYPE_LOAD = 1;

	/**
	 * 把Java的异常转换为Lua的错误表示，然后LuaState.push
	 * 
	 * @param L
	 * @param t
	 */
	public void handleJavaException(LuaState L, Throwable t);

	/**
	 * 把Lua的错误数据转换为Java异常
	 * 
	 * @param L
	 * @param type
	 * @param errorRet
	 * @return
	 */
	public LuaException handleLuaError(LuaState L, int type, int errorRet);

	/**
	 * Lua的ErrorHandler<br/>
	 * 返回负数表示不处理
	 * 
	 * @param L
	 * @return
	 */
	public int buildLuaError(LuaState L);
}
