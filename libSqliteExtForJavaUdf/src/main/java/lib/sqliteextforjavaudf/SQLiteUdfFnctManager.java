
package lib.sqliteextforjavaudf;

 import java.util.HashMap;
 import java.util.ArrayList;
 import java.util.Map;

public class SQLiteUdfFnctManager {
  static native int nativeInitUdfJavaFncts(SQLiteUdfFnctManager fnctManager, Object[][] aaFnctSignatures);

  static {
    System.loadLibrary("sqliteX");
    System.loadLibrary("SqliteExtForJavaUdf");
  }

  final ArrayList<CustomFnct> arrCustomFnct;

  public SQLiteUdfFnctManager(){
    arrCustomFnct=new ArrayList<>();
  }

  public boolean addFnct(CustomFnct fnctToAdd){
    boolean bReturn=false;
    boolean bDuplicateFunctionSignature=false;
    CustomFnctSignature fnctSignatureAdd=fnctToAdd.fnctSignature;

    if (fnctSignatureAdd.sFnctName!=null) {
      for (int iTemp = 0; iTemp < arrCustomFnct.size() && !bDuplicateFunctionSignature; iTemp++) {
        CustomFnctSignature fnctSignatureTemp = arrCustomFnct.get(iTemp).fnctSignature;
        if (fnctSignatureAdd.sFnctName.equalsIgnoreCase(fnctSignatureTemp.sFnctName) && (fnctSignatureTemp.nArgs == fnctSignatureAdd.nArgs)) {
          bDuplicateFunctionSignature = true;
          if (((fnctSignatureAdd.aClassTypes == null) && (fnctSignatureTemp.aClassTypes != null)) ||
           ((fnctSignatureAdd.aClassTypes != null) && (fnctSignatureTemp.aClassTypes == null)))
          {
            bDuplicateFunctionSignature = false;
          }
          else if ((fnctSignatureAdd.aClassTypes != null) && (fnctSignatureTemp.aClassTypes != null) && (fnctSignatureAdd.aClassTypes.length == fnctSignatureTemp.aClassTypes.length)) {
            for (int iClassTemp = 0; iClassTemp < fnctSignatureAdd.aClassTypes.length && bDuplicateFunctionSignature; iClassTemp++) {
              if (fnctSignatureAdd.aClassTypes[iClassTemp] != fnctSignatureTemp.aClassTypes[iClassTemp])
                bDuplicateFunctionSignature = false;
            }
          }
          if (bDuplicateFunctionSignature) {
            arrCustomFnct.set(iTemp, fnctToAdd); //Update to current function
            bReturn = true;
          }
        }
      }
      if (!bDuplicateFunctionSignature){
        arrCustomFnct.add(fnctToAdd);
        bReturn=true;
      }
    }
    return bReturn;
  }

  public int registerJavaFncts(){
    HashMap<String,Integer> mapFncts=new HashMap<String,Integer>();
    for (int iTemp=0; iTemp<arrCustomFnct.size(); iTemp++){
      CustomFnctSignature fnctSignature=arrCustomFnct.get(iTemp).fnctSignature;
      int nArgs=fnctSignature.nArgs;
      String sFnctName=fnctSignature.sFnctName.toLowerCase();
      if (mapFncts.containsKey(sFnctName) && (mapFncts.get(sFnctName).intValue()!=-1))
        nArgs=-1;
      mapFncts.put(sFnctName,nArgs);
    }
    Object[][] aaFnctSignatures=new Object[mapFncts.size()][2];
    int iTemp=0;
    for (Map.Entry<String, Integer> entryTemp : mapFncts.entrySet()) {
      aaFnctSignatures[iTemp][0] = entryTemp.getKey();
      aaFnctSignatures[iTemp][1] = entryTemp.getValue();
      iTemp++;
    }
    int nReturn= nativeInitUdfJavaFncts(this,aaFnctSignatures);
    return nReturn;
  }

  @SuppressWarnings("unused") //Called from native
  public Object callback(String sFnctName, Object[] argsFromDatabase){
    Object oReturn=null;
    CustomFnct customFnct=null;
    int nArgs=argsFromDatabase==null?0:argsFromDatabase.length;
    for (int iTemp=0; iTemp<this.arrCustomFnct.size(); iTemp++){
      CustomFnct fnctTemp=this.arrCustomFnct.get(iTemp);
      CustomFnctSignature fnctSigTemp=fnctTemp.fnctSignature;
      if (fnctSigTemp.sFnctName.equalsIgnoreCase(sFnctName)){
        if (nArgs==fnctSigTemp.nArgs){
          boolean bSuccess=true;
          if (fnctSigTemp.aClassTypes==null){
            if ((customFnct!=null) && (customFnct.fnctSignature.aClassTypes!=null))
              bSuccess=false;
          }
          else {
            for (int iClass = 0; iClass < fnctSigTemp.aClassTypes.length; iClass++) {
              if (argsFromDatabase[iClass] != null && (fnctSigTemp.aClassTypes[iClass] != argsFromDatabase[iClass].getClass()))
                bSuccess = false;
            }
          }
          if (bSuccess)
            customFnct=fnctTemp;
        }
        else if ((fnctSigTemp.nArgs==-1) && (customFnct==null)){
          customFnct=fnctTemp;
        }
      }
    }
    if (customFnct!=null) {
      try {
        oReturn = customFnct.callback(argsFromDatabase);
      }
      catch (Exception e){}
    }
    return oReturn;
    //  return (customFnct==null)?null:customFnct.callback(argsFromDatabase);
  }

  static public class CustomFnctSignature{
    final String sFnctName;
    final int nArgs;
    final Class[] aClassTypes;
    public CustomFnctSignature(String sFnctNameIn, int nArgsIn, Class[] aClassTypesIn){
      this.sFnctName=sFnctNameIn;
      this.nArgs=nArgsIn;
      this.aClassTypes=aClassTypesIn;
    }
    public CustomFnctSignature(String sFnctNameIn, int nArgsIn){
      this(sFnctNameIn,nArgsIn,null);
    }
    public CustomFnctSignature(String sFnctNameIn){
      this(sFnctNameIn,-1,null);
    }
  }

  static public abstract class CustomFnct{
    final CustomFnctSignature fnctSignature;

    public CustomFnct(String sFnctNameIn, int nArgsIn, Class[] aClassTypesIn){
      this.fnctSignature=new CustomFnctSignature(sFnctNameIn, nArgsIn, aClassTypesIn);
    }
    public CustomFnct(String sFnctNameIn, int nArgsIn){
      this(sFnctNameIn,nArgsIn,null);
    }
    public CustomFnct(String sFnctNameIn){
      this(sFnctNameIn,-1,null);
    }

    public abstract Object callback(Object[] argsFromDatabase);
  }

}
