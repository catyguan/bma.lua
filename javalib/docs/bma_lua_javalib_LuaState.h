/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class bma_lua_javalib_LuaState */

#ifndef _Included_bma_lua_javalib_LuaState
#define _Included_bma_lua_javalib_LuaState
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     bma_lua_javalib_LuaState
 * Method:    _open
 * Signature: (I)Lbma/lua/javalib/LuaState/NativeData;
 */
JNIEXPORT jobject JNICALL Java_bma_lua_javalib_LuaState__1open
  (JNIEnv *, jobject, jint);

/*
 * Class:     bma_lua_javalib_LuaState
 * Method:    _destroy
 * Signature: (JIJ)V
 */
JNIEXPORT void JNICALL Java_bma_lua_javalib_LuaState__1destroy
  (JNIEnv *, jclass, jlong, jint, jlong);

/*
 * Class:     bma_lua_javalib_LuaState
 * Method:    _close
 * Signature: (Lbma/lua/javalib/LuaState/NativeData;)V
 */
JNIEXPORT void JNICALL Java_bma_lua_javalib_LuaState__1close
  (JNIEnv *, jobject, jobject);

/*
 * Class:     bma_lua_javalib_LuaState
 * Method:    _apiX
 * Signature: (Lbma/lua/javalib/LuaState/NativeData;IIII)I
 */
JNIEXPORT jint JNICALL Java_bma_lua_javalib_LuaState__1apiX
  (JNIEnv *, jobject, jobject, jint, jint, jint, jint);

/*
 * Class:     bma_lua_javalib_LuaState
 * Method:    _xapi
 * Signature: (Lbma/lua/javalib/LuaState/NativeData;ILjava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;
 */
JNIEXPORT jobject JNICALL Java_bma_lua_javalib_LuaState__1xapi
  (JNIEnv *, jobject, jobject, jint, jobject, jobject, jobject);

/*
 * Class:     bma_lua_javalib_LuaState
 * Method:    _timeout
 * Signature: (Lbma/lua/javalib/LuaState/NativeData;ZI)V
 */
JNIEXPORT void JNICALL Java_bma_lua_javalib_LuaState__1timeout
  (JNIEnv *, jobject, jobject, jboolean, jint);

#ifdef __cplusplus
}
#endif
#endif