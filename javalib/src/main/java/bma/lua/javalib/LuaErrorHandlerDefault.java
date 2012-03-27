package bma.lua.javalib;

public class LuaErrorHandlerDefault implements LuaErrorHandler, LuaConst {

	public static final LuaErrorHandlerDefault INSTANCE = new LuaErrorHandlerDefault();

	@Override
	public void handleJavaException(LuaState L, Throwable t) {
		String s = t.getMessage();
		if (s == null || s.isEmpty()) {
			s = t.getClass().getName();
		}
		L.luaPushstring(s);
	}

	@Override
	public LuaException handleLuaError(LuaState L, int type, int errorRet) {
		String s = L.luaTostring(-1);
		L.luaPop(1);
		return new LuaException(s);
	}

	@Override
	public int buildLuaError(LuaState L) {
		return -1;
	}
}
