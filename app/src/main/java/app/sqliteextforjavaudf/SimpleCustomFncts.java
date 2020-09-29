package app.sqliteextforjavaudf;

import lib.sqliteextforjavaudf.SQLiteUdfFnctManager;

import android.database.Cursor;
//Native - built in
//import android.database.sqlite.SQLiteDatabase;
//import android.database.SQLException;
//import android.database.DatabaseErrorHandler;

//Latest - downloaded from sqlite.org
import org.sqlite.database.sqlite.SQLiteDatabase;
//import org.sqlite.database.SQLException;
//import org.sqlite.database.DatabaseErrorHandler;

public class SimpleCustomFncts {

  static class CustomFnct_Add extends SQLiteUdfFnctManager.CustomFnct {

    CustomFnct_Add() {
      super("Adder", -1);
    }

    public Object callback(Object[] argsFromDatabase) {
      int nReturn = 0;
      if (argsFromDatabase != null) {
        for (Object oTemp : argsFromDatabase) {
          if (oTemp instanceof Integer)
            nReturn += ((Integer) oTemp).intValue();
          else if (oTemp instanceof Long)
            nReturn += ((Long) oTemp).intValue();
          else if (oTemp instanceof String) {
            try {
              nReturn += Integer.parseInt((String) oTemp);
            } catch (NumberFormatException ex) {
            }
          }
        }
      }
      return (nReturn);
    }
  }

  static class CustomFnct_Subtract extends SQLiteUdfFnctManager.CustomFnct {

    CustomFnct_Subtract(int nArgs) {
      super("Subtract", nArgs);
    }

    public Object callback(Object[] argsFromDatabase) {
      int nReturn = 0;
      if (argsFromDatabase != null) {
        for (int iTemp = 0; iTemp < argsFromDatabase.length; iTemp++) {
          Object oTemp = argsFromDatabase[iTemp];
          int nTemp = 0;
          if (oTemp instanceof Integer)
            nTemp = ((Integer) oTemp).intValue();
          else if (oTemp instanceof Long)
            nTemp = ((Long) oTemp).intValue();
          else if (oTemp instanceof String) {
            try {
              nTemp = Integer.parseInt((String) oTemp);
            } catch (NumberFormatException ex) {
            }
          }
          if (iTemp == 0)
            nReturn = nTemp;
          else
            nReturn -= nTemp;
        }
      }
      return (nReturn);
    }
  }

  //This is not a useful example, just designed to show an overloaded function. - Will call main version unless all values used to call function are Integers
  static class CustomFnct_DivideInt extends SQLiteUdfFnctManager.CustomFnct {
    CustomFnct_DivideInt() {
      super("Divide", 2, new Class[]{Integer.class,Integer.class});
    }

    public Object callback(Object[] argsFromDatabase) {
      int nNumerator = ((Integer) argsFromDatabase[0]).intValue();
      int nDenominator = ((Integer) argsFromDatabase[1]).intValue();
      return (nNumerator == 0 || nDenominator == 0) ? 0 : nNumerator / nDenominator;
    }
  }

  static class CustomFnct_Divide extends SQLiteUdfFnctManager.CustomFnct {

    CustomFnct_Divide() {
      super("Divide", 2);
    }

    public Object callback(Object[] argsFromDatabase) {
      double dNumerator = 0;
      double dDenominator = 0;
      if (argsFromDatabase != null) {
        for (int iTemp = 0; iTemp < 2 && iTemp < argsFromDatabase.length; iTemp++) {
          Object oTemp = argsFromDatabase[iTemp];
          double dTemp = 0;
          if (oTemp instanceof Double)
            dTemp=((Double) oTemp).doubleValue();
          else if (oTemp instanceof Integer)
            dTemp = ((Integer) oTemp).doubleValue();
          else if (oTemp instanceof Long)
            dTemp = ((Long) oTemp).doubleValue();
          else if (oTemp instanceof String) {
            try {
              dTemp = Double.parseDouble((String) oTemp);
            } catch (NumberFormatException ex) {
            }
          }
          if (iTemp == 0)
            dNumerator = dTemp;
          else
            dDenominator = dTemp;
        }
      }
      return (dNumerator == 0 || dDenominator == 0) ? 0 : dNumerator / dDenominator;
    }
  }


  static class CustomFnct_CustomMessage extends SQLiteUdfFnctManager.CustomFnct {
    String sCustomMessage;

    CustomFnct_CustomMessage(String sCustomMessageIn) {
      super("CustomMessage", 0);
      this.sCustomMessage = sCustomMessageIn;
    }

    public Object callback(Object[] argsFromDatabase) {
      return (this.sCustomMessage);
    }
  }

  static class CustomFnct_IsEven extends SQLiteUdfFnctManager.CustomFnct {
    String sCustomMessage;

    CustomFnct_IsEven() {
      super("IsEven", 1);
    }

    public Object callback(Object[] argsFromDatabase) {
      boolean bReturn=false;
      if (argsFromDatabase[0] instanceof Integer)
        bReturn=((Integer)argsFromDatabase[0]).intValue()%2==0;
      return (bReturn);
    }
  }


  static void registerCustomFncts() {
    SQLiteUdfFnctManager fnctManager = new SQLiteUdfFnctManager();
    fnctManager.addFnct(new CustomFnct_Add());
    fnctManager.addFnct(new CustomFnct_Subtract(2));
    fnctManager.addFnct(new CustomFnct_DivideInt());
    fnctManager.addFnct(new CustomFnct_Divide());
    fnctManager.addFnct(new CustomFnct_CustomMessage("Top of the world!!"));
    fnctManager.addFnct(new CustomFnct_IsEven());
    fnctManager.registerJavaFncts();
  }

  static String testCustomFncts(String sPathDatabase) {
    String sReturn=null;
    if (sPathDatabase != null) {
      String sqlTest=null;
      registerCustomFncts();
      SQLiteDatabase db3 = SQLiteDatabase.openOrCreateDatabase(sPathDatabase, null, null);
      sqlTest="CREATE TABLE IF NOT EXISTS T_Test (id INTEGER PRIMARY KEY AUTOINCREMENT, colA integer, colB integer)";
      db3.execSQL(sqlTest);
      sReturn="Table Created: "+ sqlTest+"\n";
      sqlTest="INSERT INTO T_Test (ColA, ColB) VALUES (10,13)";
      db3.execSQL(sqlTest);
      sReturn+="\nRow added: "+ sqlTest;

      String sTestName=null;
      Cursor cursor;
      int iTest=0;

      while (iTest>=0){
        switch (iTest){
          case 0:
            sTestName="Add";
            sqlTest="Select adder(ColA,ColB,ColA,ColB), ColA, ColB FROM T_Test";
            break;
          case 1:
            sTestName="Subtract";
            sqlTest="Select subtract(ColB,ColA), ColA, ColB FROM T_Test";
            break;
          case 2:
            sTestName="CustomMessage";
            sqlTest="Select customMessage()";
            break;
          case 3:
            sTestName="IsEven";
            sqlTest="Select isEven(ColA) FROM T_Test";
            break;
          case 4:
            sTestName="Divide (int)";
            sqlTest="Select divide(ColB,ColA) FROM T_Test";
            break;
          case 5:
            sTestName="Divide (float)";
            sqlTest="Select divide(CAST(ColB AS FLOAT) ,CAST(ColA AS FLOAT)) FROM T_Test";
            break;
          case 6:
            sTestName="Divide (too few parameters)";
            sqlTest="Select divide(ColB) FROM T_Test";
            break;
          default:
            iTest=-1;
        }
        if (iTest!=-1){
          iTest++;
          sReturn+="\n\nTest "+iTest+": "+sTestName+"    Result: ";
          try {
            cursor = db3.rawQuery(sqlTest, null);
            sReturn += cursor.moveToFirst() ? cursor.getString(0) : "Did not return anything";
//          while (cursor.moveToNext())
//            sReturn += "\n"+cursor.getString(0);
            cursor.close();
          } catch (Exception e) {
            sReturn+="Failed";
          }
          sReturn+="\nSQL: "+sqlTest;
        }
      }
      db3.close();
    }
    return sReturn;
  }

}