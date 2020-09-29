# sqliteExtForJavaUdf
Sqlite Extension For Custom / User Defined Functions - Functions to be written in Java 

The purpose of this project is to add Custom / UDF Functions to Sqlite where the functions are written in Java.

There is a class (SQLiteCustomFunction.java) in the Android bindings. As the callback does not return any value, the functions it can support are quite limited.

In Android, restrictions have been placed on using the built in Sqlite library:


It is not possible to call the built in library directly from the jni / ndk.

	The native sqlite3* database pointer is stored in:
		struct	SQLiteConnection {
			sqlite3* const db;
		}
	The native pointer to the struct SQLiteConnection is passed to java and stored in variable: 
		// The native SQLiteConnection pointer.  (FOR INTERNAL USE ONLY)
		private long mConnectionPtr;
		
	There does not appear to be a way of accessing mConnectionPtr.
	
On the built in version as well as the default Android build of Sqlite, the ability to use extensions is included by default.

However, again by default, the extensions appear to be enabled for loading the extention via the C-API but not by SQL function:

If the source is compiled with the ablitity to load extensions, there are two ways to enable / disable the loading of extensions 

	int sqlite3_enable_load_extension(sqlite3 *db, int onoff); //For both the C-API sqlite3_load_extension() and the SQL function load_extension()

	or 

	int sqlite3_db_config(sqlite3*, int op, ...);
	sqlite3_db_config(db, SQLITE_DBCONFIG_ENABLE_LOAD_EXTENSION, int onoff, int* returnValueCanBeNull); //Only enables the C-API however disables both C-API and SQL function


You will notice that both of these methods require access to the sqlite3* which we do not have for any database opened through the "SQLite Android Bindings"

There is still an option available, the "auto" extension.

This is an option that allows adding an extension to sqlite itself that is then automatically loaded any time a subsequent database is opened.

As access to the built in shared library is restricted and cannot be loaded in jni, the auto extension method can only be used with a non-native version (aka the version downloaded from SQLite).

When compiling from source code, there are many options available.  However my goal was to be able to use the aar without modification as provide by Sqlite.

Note: Sqlite Version 3.33.0 was the version used to when project initially completed.