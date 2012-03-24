package bma.lua.javalib;

public interface LuaFunction {

	public int execute(LuaState L) throws LuaException;
}
