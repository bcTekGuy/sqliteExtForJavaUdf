

#include <jni.h>
#include <sqlite3.h>
#include <cstdlib>
#include <cstring>
#include "jniSqliteExtForJavaUdf.h"

typedef struct struct_udfFnctInfo {
  const char* sName;
  int nArg;
} UdfFnctInfo;

typedef struct struct_jniJavaClass{
  jclass jClass;
  jmethodID jmInit;
  jmethodID jmGetValue;
} JavaClassInfo;

class JniVariableClass{
public:
//********************************************************************
  JniVariableClass(JNIEnv *jenvIn, jobject joFnctManagerIn, jobjectArray jaaFnctSignaturesIn){
    this->jenv=jenvIn;
    this->joFnctManager=jenv->NewGlobalRef(joFnctManagerIn);
    this->jcFnctManager=(jclass)jenv->NewGlobalRef(jenv->GetObjectClass(this->joFnctManager));
    this->jmFnctManager_callback=jenv->GetMethodID(jcFnctManager, "callback","(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/Object;");
    this->clsObject.jClass=(jclass)jenv->NewGlobalRef(jenv->FindClass("java/lang/Object"));
    this->clsBoolean.jClass=(jclass)jenv->NewGlobalRef(jenv->FindClass("java/lang/Boolean"));
    this->clsDouble.jClass=(jclass)jenv->NewGlobalRef(jenv->FindClass("java/lang/Double"));
    this->clsInteger.jClass=(jclass)jenv->NewGlobalRef(jenv->FindClass("java/lang/Integer"));
    this->clsString.jClass=(jclass)jenv->NewGlobalRef(jenv->FindClass("java/lang/String"));
    this->clsBoolean.jmGetValue=jenv->GetMethodID(this->clsBoolean.jClass, "booleanValue", "()Z");
    this->clsDouble.jmInit=jenv->GetMethodID(this->clsDouble.jClass, "<init>", "(D)V");
    this->clsDouble.jmGetValue=jenv->GetMethodID(this->clsDouble.jClass, "doubleValue", "()D");
    this->clsInteger.jmInit=jenv->GetMethodID(this->clsInteger.jClass, "<init>", "(I)V");
    this->clsInteger.jmGetValue=jenv->GetMethodID(this->clsInteger.jClass, "intValue", "()I");

    this->nUdfFncts=jenv->GetArrayLength(jaaFnctSignaturesIn);
    this->aUdfFnctInfo=new UdfFnctInfo[nUdfFncts];
    for (int iTemp=0; iTemp<nUdfFncts; iTemp++){
      jobjectArray jaFnctSignatures=(jobjectArray)jenv->GetObjectArrayElement(jaaFnctSignaturesIn,iTemp);
      jstring jsFnctName=(jstring)jenv->GetObjectArrayElement(jaFnctSignatures,0);
      jobject joNumArg=jenv->GetObjectArrayElement(jaFnctSignatures,1);
      int nLenFnctName=jenv->GetStringUTFLength(jsFnctName);
      this->aUdfFnctInfo[iTemp].nArg=jenv->CallIntMethod(joNumArg,this->clsInteger.jmGetValue);
      char* sFnctNameStoreTemp=(char *)calloc(nLenFnctName+1,sizeof(char));
      const char* zFnctNameTemp=jenv->GetStringUTFChars(jsFnctName,0);
      strcpy(sFnctNameStoreTemp,zFnctNameTemp);
      jenv->ReleaseStringUTFChars(jsFnctName,zFnctNameTemp);
      this->aUdfFnctInfo[iTemp].sName=sFnctNameStoreTemp;
    }
  }

  JNIEnv *jenv;
  jclass jcFnctManager;
  jobject joFnctManager;
  jmethodID jmFnctManager_callback;
  JavaClassInfo clsObject;
  JavaClassInfo clsBoolean;
  JavaClassInfo clsDouble;
  JavaClassInfo clsInteger;
  JavaClassInfo clsString;
  int nUdfFncts;
  UdfFnctInfo* aUdfFnctInfo;
};

JniVariableClass* g_jniVariableClassTemp=NULL;

//********************************************************************
//Breakpoints in this function cause signalFaults (SIG33)
void javaCallbackStub(sqlite3_context *context, int argc, sqlite3_value **argv){
  JniVariableClass* jniVariableClassTemp= static_cast<JniVariableClass*>(sqlite3_user_data(context));
  JNIEnv *jenv=jniVariableClassTemp->jenv;
  const char* zFnctName=context->pFunc->zName;
  jstring jsFnctName=jenv->NewStringUTF(zFnctName);
  jobjectArray jarrArgsTemp=NULL;
  if (argc!=0){
    jarrArgsTemp=jenv->NewObjectArray(argc,jniVariableClassTemp->clsObject.jClass,NULL);
    for (int iTemp=0; iTemp<argc; iTemp++) {
      sqlite3_value *value = argv[iTemp];
      jobject joTemp = NULL;
      switch (sqlite3_value_type(value)) {
        case SQLITE_INTEGER:
          joTemp = jenv->NewObject(jniVariableClassTemp->clsInteger.jClass, jniVariableClassTemp->clsInteger.jmInit, sqlite3_value_int(value));
          break;
        case SQLITE_FLOAT:
          joTemp = jenv->NewObject(jniVariableClassTemp->clsDouble.jClass, jniVariableClassTemp->clsDouble.jmInit, sqlite3_value_double(value));
          break;
        case SQLITE_TEXT:
          joTemp = jenv->NewStringUTF((char *)sqlite3_value_text(value));
          break;
        case SQLITE_BLOB: //TODO: Add code to handle blob type
          break;
        case SQLITE_NULL:
        default:
          break;
      }
      if (joTemp != NULL) {
        jenv->SetObjectArrayElement(jarrArgsTemp, iTemp, joTemp);
        jenv->DeleteLocalRef(joTemp);
      }
    }
  }
  jobject joReturn=jenv->CallObjectMethod(jniVariableClassTemp->joFnctManager,jniVariableClassTemp->jmFnctManager_callback,jsFnctName,jarrArgsTemp);
  jenv->DeleteLocalRef(jsFnctName);
  jenv->DeleteLocalRef(jarrArgsTemp);

  if (joReturn==NULL) {
    sqlite3_result_null(context);
  }
  else {
    if (jenv->IsInstanceOf(joReturn, jniVariableClassTemp->clsBoolean.jClass)) {
      bool bTemp = jenv->CallBooleanMethod(joReturn, jniVariableClassTemp->clsBoolean.jmGetValue);
      sqlite3_result_int(context, bTemp?1:0);
    }
    else if (jenv->IsInstanceOf(joReturn, jniVariableClassTemp->clsInteger.jClass)) {
      int nTemp = jenv->CallIntMethod(joReturn, jniVariableClassTemp->clsInteger.jmGetValue);
      sqlite3_result_int(context, nTemp);
    }
    else if (jenv->IsInstanceOf(joReturn, jniVariableClassTemp->clsDouble.jClass)) {
      double dTemp = jenv->CallDoubleMethod(joReturn, jniVariableClassTemp->clsDouble.jmGetValue);
      sqlite3_result_double(context, dTemp);
    }
    else if (jenv->IsInstanceOf(joReturn, jniVariableClassTemp->clsString.jClass)) {
      jstring jsTemp = (jstring) joReturn;
      const char *sTemp = jenv->GetStringUTFChars(jsTemp, NULL);
      sqlite3_result_text(context, sTemp, -1, SQLITE_TRANSIENT);
      jenv->ReleaseStringUTFChars(jsTemp, sTemp);
    }
  }
}

//********************************************************************
int loadSqliteExtention(sqlite3 *db, char **pzErrMsg, const sqlite3_api_routines *pApi) {
  // deterministic = function will always return the same result given the same inputs within a single SQL statement
  int eTextRep=SQLITE_UTF8 | SQLITE_DETERMINISTIC;  //Text encoding for input parameters
  void *pApp=g_jniVariableClassTemp; //arbitrary pointer accessible for passing data to function
  void (*xStepNotUsed)(sqlite3_context*,int,sqlite3_value**)=0;
  void (*xFinalNotUsed)(sqlite3_context*)=0;
  void(*xDestroyNotUsed)(void*)=0;
  for (int iTemp=0; iTemp<g_jniVariableClassTemp->nUdfFncts; iTemp++) {
    UdfFnctInfo* fnctInfoTemp=&g_jniVariableClassTemp->aUdfFnctInfo[iTemp];
    sqlite3_create_function_v2(db, fnctInfoTemp->sName, fnctInfoTemp->nArg, eTextRep, pApp, &javaCallbackStub, xStepNotUsed, xFinalNotUsed, xDestroyNotUsed);
  }
  return SQLITE_OK_LOAD_PERMANENTLY; //SQLITE_OK;
  //An extension for which the initialization function returns SQLITE_OK_LOAD_PERMANENTLY continues to exist in memory after the database connection closes.
  //However, the extension is not automatically registered with subsequent database connections
  //sqlite3_auto_extension registers extension to be automatically loaded on subsequent db openings
}

//********************************************************************
extern "C" JNIEXPORT jint JNICALL Java_lib_sqliteextforjavaudf_SQLiteUdfFnctManager_nativeInitUdfJavaFncts
 (JNIEnv *jenv, jclass clazz, jobject fnctManager, jobjectArray aaFnctSignatures)
{
  g_jniVariableClassTemp=new JniVariableClass(jenv,fnctManager,aaFnctSignatures);
  jint nReturn=sqlite3_auto_extension( (void(*)(void))&loadSqliteExtention);
  return nReturn;
}

