#include <stdio.h>
#include <stdlib.h>
#include <time.h>
#include <string.h>
#include "luajni.h"
#include "lua.h"
#include "lualib.h"
#include "lauxlib.h"

/* Constant that is used to index the JNI Environment */
#define LUAJAVA_JNIENV		"__JAVA_JNIEnv"
/* Defines the lua State Index Property Name */
#define LUAJAVA_STATEID		"__JAVA_StateId"
/* Defines the lua JavaObject flag */
#define LUAJAVA_OBJECT			"__JAVA_Object"

static lua_State * getStateFromJObj( JNIEnv * env , jlong cptr );

static void pushJNIEnv( JNIEnv * env , lua_State * L );

static JNIEnv * getEnvFromState( lua_State * L );

static jclass    cls_LuaState				= NULL;
static jmethodID mid_LuaState__function		= NULL;
static jmethodID mid_LuaState__loader		= NULL;
static jmethodID mid_LuaState__error		= NULL;

static jclass    cls_Number					= NULL;
static jmethodID mid_Number__intValue		= NULL;
static jmethodID mid_Number__doubleValue	= NULL;

static jclass    cls_Integer				= NULL;
static jmethodID mid_Integer__c				= NULL;

static jclass    cls_Long					= NULL;
static jmethodID mid_Long__c				= NULL;

static jclass    cls_Double					= NULL;
static jmethodID mid_Double__c				= NULL;

static jclass	 cls_Exception				= NULL;
static jmethodID mid_Exception__getMessage	= NULL;

static jclass    cls_LuaDebug					= NULL;
static jmethodID mid_LuaDebug__int				= NULL;
static jmethodID mid_LuaDebug__str				= NULL;

void initJNICache(JNIEnv* env)
{
	if(cls_LuaState)return;

	cls_Number = ( *env )->FindClass( env , "java/lang/Number" );
	mid_Number__intValue = ( *env )->GetMethodID( env , cls_Number, "intValue" , "()I" );
	mid_Number__doubleValue = ( *env )->GetMethodID( env , cls_Number, "doubleValue" , "()D" );

	cls_Double = ( *env )->FindClass( env , "java/lang/Double" );
	mid_Double__c = ( *env )->GetMethodID( env , cls_Double, "<init>" , "(D)V" );

	cls_LuaState = ( *env )->FindClass( env , "bma/lua/javalib/LuaState" );
	mid_LuaState__function = ( *env )->GetStaticMethodID( env , cls_LuaState, "c_function" , "(ILjava/lang/Object;)I" );
	mid_LuaState__loader = ( *env )->GetStaticMethodID( env , cls_LuaState, "c_loader" , "(ILjava/lang/Object;Ljava/lang/Object;Ljava/lang/String;)Ljava/lang/String;" );
	mid_LuaState__error = ( *env )->GetStaticMethodID( env , cls_LuaState, "c_error" , "(I)I" );

	cls_Exception = ( *env )->FindClass( env , "java/lang/Exception" );
	mid_Exception__getMessage = ( *env )->GetMethodID( env , cls_Exception, "getMessage" , "()Ljava/lang/String;" );

	cls_Integer = ( *env )->FindClass( env , "java/lang/Integer" );
	mid_Integer__c = ( *env )->GetMethodID( env , cls_Integer, "<init>" , "(I)V" );

	cls_Long = ( *env )->FindClass( env , "java/lang/Long" );
	mid_Long__c = ( *env )->GetMethodID( env , cls_Long, "<init>" , "(J)V" );

	cls_LuaDebug = ( *env )->FindClass( env , "bma/lua/javalib/LuaDebug" );
	mid_LuaDebug__int = ( *env )->GetMethodID( env , cls_LuaDebug, "setInt" , "(II)V" );
	mid_LuaDebug__str = ( *env )->GetMethodID( env , cls_LuaDebug, "setStr" , "(ILjava/lang/String;)V" );
}

int isJavaObject( lua_State * L , int idx )
{
	if ( !lua_isuserdata( L , idx ) )return 0;
	if ( lua_getmetatable( L , idx ) == 0 )return 0;

	lua_pushstring( L , LUAJAVA_OBJECT );
	lua_rawget( L , -2 );

	if (lua_isnil( L, -1 ))
	{
		lua_pop( L , 2 );
		return 0;
	}
	lua_pop( L , 2 );
	return 1;
}

int getLuaStateId(lua_State* L) {
	int r = 0;

	lua_pushstring( L , LUAJAVA_STATEID );
	lua_rawget( L , LUA_REGISTRYINDEX );

	if ( !lua_isnumber( L , -1 ) )
	{
		lua_pushstring( L , "Impossible to identify luaState id." );
		lua_error( L );
	}
	
	r = lua_tointeger( L , -1 );
	lua_pop(L,1);
	return r;
}

int javaInt(JNIEnv* env, jobject p1,int def) {
	if(p1==NULL)return def;
	return (jint) ( *env )->CallIntMethod(env, p1,mid_Number__intValue);
}

int java_function_call( lua_State * L )
{
	jobject * obj;
	int ret,stateId;
	JNIEnv * env;
   
	if ( !isJavaObject( L , 1 ) )
	{
		lua_pushstring( L , "Not a java Function." );
		lua_error( L );
	}

	obj = lua_touserdata( L , 1 );

	/* Gets the JNI Environment */
	env = getEnvFromState( L );
	if ( env == NULL )
	{
		lua_pushstring( L , "Invalid JNI Environment." );
		lua_error( L );
	}

	stateId = getLuaStateId( L );

	ret = ( *env )->CallStaticIntMethod( env , cls_LuaState, mid_LuaState__function , stateId , *obj);

	if(ret==-1) {
		lua_pushstring( L , "Invalid LuaStateId." );
		lua_error( L );
	}
	if(ret==-2) {
		lua_pushstring( L , "Invalid LuaFunction." );
		lua_error( L );
	}
	if(ret==-3) {
		lua_error( L );
	}

	return ret;
}

int java_function_errhandler(lua_State* L) {

	int ret,stateId;
	JNIEnv * env;
   
	/* Gets the JNI Environment */
	env = getEnvFromState( L );
	ret = -1;
	if ( env != NULL )
	{
		stateId = getLuaStateId( L );
		ret = ( *env )->CallStaticIntMethod( env , cls_LuaState, mid_LuaState__error , stateId );	
	}
	if(ret<0) {
		if(lua_gettop(L)>0) {
			const char *msg = lua_tostring(L, 1);
			if (msg) {
				luaL_traceback(L, L, msg, 1);
			} else {
				lua_pushvalue(L,-1);
			}
			return 1;
		}
		lua_pushliteral(L, "(no error message)");
		return 1;
	}
	return ret;
}

int java_function_gc( lua_State * L )
{
   jobject * obj;
   JNIEnv * env;

   if ( !isJavaObject( L , 1 ) )
   {
      return 0;
   }

   obj = ( jobject * ) lua_touserdata( L , 1 );

   /* Gets the JNI Environment */
   env = getEnvFromState( L );
   if ( env == NULL )
   {
      lua_pushstring( L , "Invalid JNI Environment." );
      lua_error( L );
   }

   ( *env )->DeleteGlobalRef( env , *obj );

   return 0;
}

typedef struct JLoadF {
  jobject loader;
  jobject data;
  jobject source;
  const char* buffer;
  jobject ret;
} JLoadF;

static const char* java_function_loader(lua_State *L, void *ud, size_t *size) {
	int stateId;
	JNIEnv * env;
	jobject r = NULL;	
	JLoadF *lf = (JLoadF *)ud;

	if(lf->buffer!=NULL) {
		return NULL;
	}

	/* Gets the JNI Environment */
	env = getEnvFromState( L );
	if ( env == NULL )
	{
		return NULL;
	}

	stateId = getLuaStateId( L );

	r = ( *env )->CallStaticObjectMethod( env , cls_LuaState, mid_LuaState__loader , stateId , lf->loader,lf->data,lf->source);
	if(r == NULL) {
		return NULL;
	}

	*size = ( *env )->GetStringUTFLength(env, r);
	lf->buffer = ( *env )->GetStringUTFChars( env, r , NULL );
	lf->ret = r;
	
	return lf->buffer;
}

/***************************************************************************
*
*  Function: getStateFromJObj
*  ****/

lua_State * getStateFromJObj( JNIEnv * env , jlong cptr )
{
	return ( lua_State * ) cptr;
}

/***************************************************************************
*
*  Function: luaJavaFunctionCall
*  ****/

JNIEnv * getEnvFromState( lua_State * L )
{
	JNIEnv ** udEnv;

	lua_pushstring( L , LUAJAVA_JNIENV );
	lua_rawget( L , LUA_REGISTRYINDEX );

	if ( !lua_isuserdata( L , -1 ) )
	{
		lua_pop( L , 1 );
		return NULL;
	}

	udEnv = ( JNIEnv ** ) lua_touserdata( L , -1 );

	lua_pop( L , 1 );

	return * udEnv;
}


/***************************************************************************
*
*  Function: pushJNIEnv
*  ****/

void pushJNIEnv( JNIEnv * env , lua_State * L )
{
	JNIEnv ** udEnv;

	lua_pushstring( L , LUAJAVA_JNIENV );
	lua_rawget( L , LUA_REGISTRYINDEX );

	if ( !lua_isnil( L , -1 ) )
	{
		udEnv = ( JNIEnv ** ) lua_touserdata( L , -1 );
		*udEnv = env;
		lua_pop( L , 1 );
	}
	else
	{
		lua_pop( L , 1 );
		udEnv = ( JNIEnv ** ) lua_newuserdata( L , sizeof( JNIEnv * ) );
		*udEnv = env;

		lua_pushstring( L , LUAJAVA_JNIENV );
		lua_insert( L , -2 );
		lua_rawset( L , LUA_REGISTRYINDEX );
	}
}

/*********************** LUA API FUNCTIONS ******************************/

/************************************************************************
*   JNI Called function
************************************************************************/

static void clockHook(lua_State *L, lua_Debug *ar) {
	clock_t* ptr;
	clock_t sp;

	JNIEnv* env = getEnvFromState( L );
	if(env==NULL)return;	

	lua_getglobal(L,"_hversion");
	ptr = (clock_t*) lua_touserdata(L,-1);
	lua_pop(L,1);
	if(ptr==NULL) {
		luaL_error(L,"execute timeout");
	}
	
	sp = clock() - (*ptr);
	if(sp >= (*(ptr+1))) {
		lua_sethook(L,clockHook,LUA_MASKCOUNT,1);
		luaL_error(L,"execute timeout in %f seconds",(lua_Number)(sp/1000));
	}
}

JNIEXPORT jobject JNICALL Java_bma_lua_javalib_LuaState__1getDebug
  (JNIEnv * env, jobject jThis, jlong jobj, jstring what, jint level)
{
	jobject obj;
	lua_Debug debug;
	lua_State* L = getStateFromJObj( env , jobj);	
	
	memset(&debug,0,sizeof(debug));
	if(!lua_getstack(L,level,&debug)) {
		return NULL;
	}
	if(what!=NULL) {
		const char* k = ( *env )->GetStringUTFChars( env, what , NULL );
		lua_getinfo( L ,k, &debug);
		( *env )->ReleaseStringUTFChars( env , what, k );
	}

	
	obj = ( *env )->AllocObject(env,cls_LuaDebug);
	if(obj==NULL) {
		return NULL;
	}

	( *env )->CallVoidMethod(env,obj,mid_LuaDebug__int,1,debug.event);
	( *env )->CallVoidMethod(env,obj,mid_LuaDebug__int,6,debug.currentline);
	( *env )->CallVoidMethod(env,obj,mid_LuaDebug__int,7,debug.linedefined);
	( *env )->CallVoidMethod(env,obj,mid_LuaDebug__int,8,debug.lastlinedefined);
	( *env )->CallVoidMethod(env,obj,mid_LuaDebug__int,9,debug.nups);
	( *env )->CallVoidMethod(env,obj,mid_LuaDebug__int,10,debug.nparams);
	( *env )->CallVoidMethod(env,obj,mid_LuaDebug__int,11,debug.isvararg);
	( *env )->CallVoidMethod(env,obj,mid_LuaDebug__int,12,debug.istailcall);
	if(debug.name) {
		( *env )->CallVoidMethod(env,obj,mid_LuaDebug__str,2,
			 ( *env )->NewStringUTF( env , debug.name));		
	}
	if(debug.namewhat) {
		( *env )->CallVoidMethod(env,obj,mid_LuaDebug__str,3,
			 ( *env )->NewStringUTF( env , debug.namewhat));		
	}
	if(debug.what) {
		( *env )->CallVoidMethod(env,obj,mid_LuaDebug__str,4,
			 ( *env )->NewStringUTF( env , debug.what));		
	}
	if(debug.source) {
		( *env )->CallVoidMethod(env,obj,mid_LuaDebug__str,5,
			 ( *env )->NewStringUTF( env , debug.source));
	}
	if(debug.short_src) {
		( *env )->CallVoidMethod(env,obj,mid_LuaDebug__str,13,
			 ( *env )->NewStringUTF( env , debug.short_src));		
	}

	return obj;
}

JNIEXPORT void JNICALL Java_bma_lua_javalib_LuaState__1timeout
  (JNIEnv * env, jobject jThis, jlong jobj, jboolean begin, jint timeout)
{
	clock_t* ptr;
	lua_State* L = getStateFromJObj( env , jobj);

	ptr = lua_newuserdata(L,sizeof(clock_t)*2);
	*ptr = begin?clock():0;
	ptr++;
	*ptr = timeout;
	lua_setglobal(L,"_hversion");
	
	if(begin) {
		lua_sethook(L,clockHook,LUA_MASKCOUNT,1000);		
	} else {
		lua_sethook(L,NULL,0,0);
	}
}

/************************************************************************
*   JNI Called function
************************************************************************/

JNIEXPORT jlong JNICALL Java_bma_lua_javalib_LuaState__1open
  (JNIEnv * env , jobject jobj, jint stateId)
{
	lua_State * L = NULL;

	initJNICache(env);
	L = luaL_newstate();
 
	lua_pushstring( L , LUAJAVA_STATEID );
	lua_pushnumber( L , (lua_Number)stateId );
	lua_settable( L , LUA_REGISTRYINDEX );

	pushJNIEnv(env, L);   

	return (jlong) L;
}

JNIEXPORT void JNICALL Java_bma_lua_javalib_LuaState__1destroy
  (JNIEnv * env, jclass cls, jlong data, jint type, jlong ctx)
{	
	if(type==0) {
		// lua_State* L;
		lua_close( (lua_State*) data );
	}
	if(type==1) {
		// ptr*
	}
}

JNIEXPORT void JNICALL Java_bma_lua_javalib_LuaState__1close
  (JNIEnv * env , jobject jobj , jlong cptr)
{
	lua_State * L = getStateFromJObj( env , cptr );

	lua_close( L );
}

JNIEXPORT void JNICALL Java_bma_lua_javalib_LuaState__1writeData
  (JNIEnv * env, jobject jThis, jlong userdata, jint dataPos, jbyteArray buf, jint bufPos, jint size)
{
	jbyte * ptr = ( *env)->GetByteArrayElements(env,buf,0);
	memcpy(((jbyte*)userdata)+dataPos,ptr+bufPos,size);
}

JNIEXPORT jbyteArray JNICALL Java_bma_lua_javalib_LuaState__1readData
  (JNIEnv * env, jobject jThis, jlong userdata, jint pos, jint size)
{
	jbyteArray r;
	r = (*env)->NewByteArray(env,size);
	(*env)->SetByteArrayRegion(env, r, pos, size, (const jbyte*) userdata);
	return r; 
}

JNIEXPORT jint JNICALL Java_bma_lua_javalib_LuaState__1apiX
   (JNIEnv * env, jobject jThis, jlong data, jint type, jint p1, jint p2, jint p3) {
	
	lua_State * L = getStateFromJObj( env , data );	
	switch(type) {
	case 1:
		return lua_absindex(L,p1);
	case 2:
		lua_arith(L,p1);
		return 0;
	case 3:
		lua_call(L,p1, p2);
		return 0;
	case 4:
		return lua_checkstack(L,p1);	
	case 5:
		return lua_compare(L,p1, p2, p3);
	case 6:
		lua_concat(L,p1);
		return 0;
	case 7:
		lua_copy(L,p1, p2);
		return 0;
	case 8:
		lua_createtable(L,p1,p2);
		return 0;
	case 9:
		return lua_error(L);
	case 10:
		return lua_gc(L,p1,p2);
	case 13:
		return lua_getmetatable(L,p1);
	case 14:
		lua_gettable(L,p1);
		return 0;
	case 15:
		return lua_gettop(L);
	case 16:
		lua_getuservalue(L,p1);
		return 0;
	case 17:
		lua_insert(L,p1);
		return 0;
	case 18:
		// isxxxxx
		return lua_type(L,p1);
	case 19:
		return lua_iscfunction(L,p2);
	case 20:
		lua_len(L,p1);
		return 0;
	case 21:
		lua_newuserdata(L,p1);
		return 0;
	case 22:
		return lua_next(L,p1);
	case 23:
		{
			int r = 0;
			int base = lua_gettop(L) - p1;  /* function index */
			lua_pushcfunction(L, &java_function_errhandler);  /* push traceback function */
			lua_insert(L, base);
			r = lua_pcall(L,p1,p2,base);
			lua_remove(L, base);
			return r;
		}
	case 24:
		lua_pop(L,p1);
		return 0;
	case 25:
		lua_pushboolean(L,p1);
		return 0;
	case 26:
		lua_pushinteger(L,p1);
		return 0;
	case 28:
		lua_pushnil(L);
		return 0;
	case 30:
		lua_pushvalue(L,p1);
		return 0;
	case 31:
		return lua_rawequal(L,p1,p2);
	case 32:
		lua_rawget(L,p1);
		return 0;
	case 33:
		lua_rawgeti(L,p1,p2);
		return 0;
	case 34:
		return lua_rawlen(L,p1);
	case 35:
		lua_rawset(L,p1);
		return 0;
	case 36:
		lua_rawseti(L,p1,p2);
		return 0;
	case 37:
		lua_remove(L,p1);
		return 0;
	case 38:
		lua_replace(L,p1);
		return 0;
	case 41:
		lua_setmetatable(L,p1);
		return 0;
	case 42:
		lua_settop(L,p1);
		return 0;
	case 43:
		lua_setuservalue(L,p1);
		return 0;
	case 44:
		return lua_toboolean(L,p1);
	case 45:
		return lua_tointeger(L,p1);
	case 46:
		{
			int isnum = 0;
			int r = lua_tointegerx(L,p1,&isnum);
			if(isnum) {
				return r;
			}
			return p2;
		}
	case 51:
		return lua_type(L,p1);
	case 5001:
		luaL_openlibs(L);
		return 0;
	default:
		return 0;
	}
}

JNIEXPORT jobject JNICALL Java_bma_lua_javalib_LuaState__1xapi
  (JNIEnv * env, jobject jThis, jlong data, jint api, jobject p1, jobject p2, jobject p3)
{
	lua_State * L = getStateFromJObj( env , data );	
	switch(api) {
	case 11:
		{
			int i = javaInt(env,p1,0);
			const char* k = ( *env )->GetStringUTFChars( env, p2 , NULL );
			lua_getfield( L ,i, k);
			( *env )->ReleaseStringUTFChars( env , p2, k );
			return NULL;
		}
	case 12:
		{
			const char* k = ( *env )->GetStringUTFChars( env, p1 , NULL );
			lua_getglobal( L , k);
			( *env )->ReleaseStringUTFChars( env , p1 , k );
			return NULL;
		}
	case 27:
		{
			const char* k = ( *env )->GetStringUTFChars( env, p1 , NULL );
			lua_pushstring ( L , k);
			( *env )->ReleaseStringUTFChars( env , p1 , k );
			return NULL;
		}
	case 29:
		{
			lua_Number n;
			n = (lua_Number) (jdouble) ( *env )->CallDoubleMethod(env, p1,mid_Number__doubleValue);
			lua_pushnumber(L,n);
			return NULL;
		}
	case 39:
		{
			int i = javaInt(env,p1,0);
			const char* k = ( *env )->GetStringUTFChars( env, p2 , NULL );
			lua_setfield( L ,i, k);
			( *env )->ReleaseStringUTFChars( env , p2, k );
			return NULL;
		}
	case 40:
		{
			const char* k = ( *env )->GetStringUTFChars( env, p1 , NULL );
			lua_setglobal( L, k);
			( *env )->ReleaseStringUTFChars( env , p1, k );
			return NULL;
		}
	case 47:
		{
			int isnum = 0;
			int i = javaInt(env, p1,0);
			lua_Number n = lua_tonumberx(L,i,&isnum);
			if(!isnum) {
				return NULL;
			}
			return ( *env) ->NewObject(env,cls_Double,mid_Double__c,(jdouble) n);
		}
	case 49:
		{
			int i = javaInt(env, p1,0);
			const char * r = NULL;
			r = lua_tostring(L,i);
			if(r) {
				return ( *env )->NewStringUTF( env , r);
			}
			return NULL;
		}
	case 50:
		{
			int i = javaInt(env, p1,0);
			void* data = lua_touserdata(L,i);
			if(data==NULL) {
				return NULL;
			}			
			return ( *env) ->NewObject(env,cls_Long,mid_Long__c,(jlong) data);
		}
	case 52:
		{
			int i = javaInt(env, p1,0);
			const char * r = NULL;
			r = lua_typename(L,i);
			if(r) {
				return ( *env )->NewStringUTF( env , r);
			}
			return NULL;
		}
	case 53:
		{
			jobject * userData , globalRef;
			globalRef = ( *env )->NewGlobalRef( env , p1 );
			userData = ( jobject * ) lua_newuserdata( L , sizeof( jobject ) );
			*userData = globalRef;
			/* Creates metatable */
			lua_newtable( L );

			/* pushes the __index metamethod */
			lua_pushstring( L , "__call" );
			lua_pushcfunction( L , &java_function_call);
			lua_rawset( L , -3 );

			/* pusher the __gc metamethod */
			lua_pushstring( L , "__gc" );
			lua_pushcfunction( L , &java_function_gc );
			lua_rawset( L , -3 );

			lua_pushstring( L , LUAJAVA_OBJECT );
			lua_pushboolean( L , 1 );
			lua_rawset( L , -3 );

			if ( lua_setmetatable( L , -2 ) == 0 )
			{
				( *env )->ThrowNew( env , ( *env )->FindClass( env , "org/keplerproject/luajava/LuaException" ) ,
                          "Index is not a java object" );
			}
			return NULL;
		}
	case 54:
		{
			int r;
			JLoadF f;
			const char* source = ( *env )->GetStringUTFChars( env, p3 , NULL );
			f.loader = p1;
			f.data = p2;
			f.source = p3;
			f.buffer = NULL;
			f.ret = NULL;
			r = lua_load( L, &java_function_loader,&f,source,NULL);
			( *env )->ReleaseStringUTFChars( env , p3, source );
			if(f.buffer!=NULL && f.ret!=NULL) {
				( *env )->ReleaseStringUTFChars( env , f.ret, f.buffer );
			}
			return ( *env) ->NewObject(env,cls_Integer,mid_Integer__c,(jint) r);
		}
	case 5002:
		{
			int r;
			const char* k = ( *env )->GetStringUTFChars( env, p1 , NULL );
			r = luaL_loadfile( L, k);
			( *env )->ReleaseStringUTFChars( env , p1, k );
			return ( *env) ->NewObject(env,cls_Integer,mid_Integer__c,(jint) r);
		}
	default:
		// luaL_argcheck
		return NULL;
	}
}