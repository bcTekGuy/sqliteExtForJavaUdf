
cmake_minimum_required(VERSION 3.4.1)

include_directories(${CMAKE_SOURCE_DIR}/../../../../vendor/sqlite/src)

include_directories(C:/GitTemp/art/runtime)
include_directories(C:/GitTemp/art/libartbase)
include_directories(C:/GitTemp/core/base/include)

add_library( SqliteExtForJavaUdf SHARED jniSqliteExtForJavaUdf.cpp)

target_link_libraries( SqliteExtForJavaUdf ${CMAKE_SOURCE_DIR}/../../../../vendor/sqlite/jni/${ANDROID_ABI}/libsqliteX.so) #Extracted from aar
#target_link_libraries( SqliteExtForJavaUdf ${CMAKE_SOURCE_DIR}/../../../../vendor/sqlite/jni/${ANDROID_ABI}/libsqlite.so) #Extracted from aar

#add_library( sqlite3 SHARED IMPORTED)
#set_target_properties (sqlite3 PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/../../../../vendor/sqlite/jni/${ANDROID_ABI}/libsqliteX.so) #Extracted from aar
#target_link_libraries(SqliteExtForJavaUdf sqlite3)
