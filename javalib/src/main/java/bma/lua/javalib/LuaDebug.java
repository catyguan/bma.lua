package bma.lua.javalib;

public class LuaDebug {

	private int event; // 1
	/**
	 * a reasonable name for the given function. <br/>
	 * Because functions in Lua are first-class values, they do not have a fixed
	 * name: <br/>
	 * some functions can be the value of multiple global variables, while
	 * others can be stored only in a table field. <br/>
	 * If it cannot find a name, then name is set to NULL.
	 */
	private String name; // 2
	/**
	 * explains the name field. <br/>
	 * The value of namewhat can be "global", "local", "method", "field",
	 * "upvalue", or "" (the empty string)<br/>
	 */
	private String nameWhat; // 3
	/**
	 * the string "Lua" if the function is a Lua function<br/>
	 * "C" if it is a C function<br/>
	 * "main" if it is the main part of a chunk.<br/>
	 */
	private String what; // 4
	/**
	 * the source of the chunk that created the function. <br/>
	 * If source starts with a '@', it means that the function was defined in a
	 * file where the file name follows the '@'. <br/>
	 * If source starts with a '=', the remainder of its contents describe the
	 * source in a user-dependent manner. <br/>
	 * Otherwise, the function was defined in a string where source is that
	 * string.
	 */
	private String source; // 5
	/**
	 * the current line where the given function is executing. <br/>
	 * When no line information is available, currentline is set to -1.
	 */
	private int currentLine; // 6
	/**
	 * the line number where the definition of the function starts.
	 */
	private int lineDefined; // 7
	/**
	 * the line number where the definition of the function ends.
	 */
	private int lastLineDefined; // 8
	/**
	 * number of upvalues
	 */
	private int upvalues; // 9
	/**
	 * number of parameters (always 0 for C functions).
	 */
	private int params; // 10

	/**
	 * true if the function is a vararg function (always true for C functions).
	 */
	private boolean vararg; // 11

	/**
	 * true if this function invocation was called by a tail call. <br/>
	 * In this case, the caller of this level is not in the stack.
	 */
	private boolean tailcall; // 12

	/**
	 * a "printable" version of source, to be used in error messages.
	 */
	private String shortSource; // 13

	public void setInt(int type, int v) {
		switch (type) {
		case 1:
			event = v;
			break;
		case 6:
			currentLine = v;
			break;
		case 7:
			lineDefined = v;
			break;
		case 8:
			lastLineDefined = v;
			break;
		case 9:
			upvalues = v;
			break;
		case 10:
			params = v;
			break;
		case 11:
			vararg = v != 0;
			break;
		case 12:
			tailcall = v != 0;
			break;
		}
	}

	public void setStr(int type, String s) {
		switch (type) {
		case 2:
			name = s;
			break;
		case 3:
			nameWhat = s;
			break;
		case 4:
			what = s;
			break;
		case 5:
			source = s;
			break;
		case 13:
			shortSource = s;
			break;
		}
	}

	public int getEvent() {
		return event;
	}

	public void setEvent(int event) {
		this.event = event;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNameWhat() {
		return nameWhat;
	}

	public void setNameWhat(String nameWhat) {
		this.nameWhat = nameWhat;
	}

	public String getWhat() {
		return what;
	}

	public void setWhat(String what) {
		this.what = what;
	}

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public int getCurrentLine() {
		return currentLine;
	}

	public void setCurrentLine(int currentLine) {
		this.currentLine = currentLine;
	}

	public int getLineDefined() {
		return lineDefined;
	}

	public void setLineDefined(int lineDefined) {
		this.lineDefined = lineDefined;
	}

	public int getLastLineDefined() {
		return lastLineDefined;
	}

	public void setLastLineDefined(int lastLineDefined) {
		this.lastLineDefined = lastLineDefined;
	}

	public int getUpvalues() {
		return upvalues;
	}

	public void setUpvalues(int upvalues) {
		this.upvalues = upvalues;
	}

	public int getParams() {
		return params;
	}

	public void setParams(int params) {
		this.params = params;
	}

	public boolean isVararg() {
		return vararg;
	}

	public void setVararg(boolean vararg) {
		this.vararg = vararg;
	}

	public boolean isTailcall() {
		return tailcall;
	}

	public void setTailcall(boolean tailcall) {
		this.tailcall = tailcall;
	}

	public String getShortSource() {
		return shortSource;
	}

	public void setShortSource(String shortSource) {
		this.shortSource = shortSource;
	}

}