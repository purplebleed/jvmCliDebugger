package excuter;

import com.sun.jdi.*;
import com.sun.tools.jdi.ClassTypeImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by leolin on 9/4/15.
 */
public abstract class Executer {

    protected ThreadReference threadReference = null;

    public abstract int getLineNumber();

    public abstract String getClassName();

    public abstract void execute(Map<String, Value> values);

    public Object valueToObject(Value v){
        Object value = null;
        if(v instanceof ArrayReference) {
            ArrayReference arrayReference = (ArrayReference) v;
            Object[] values = new Object[arrayReference.length()];
            for(int i = 0; i < arrayReference.length(); i++){
                values[i] = valueToObject(arrayReference.getValue(i));
            }
            value = values;
        }else if(v instanceof BooleanValue){
            BooleanValue booleanValue = (BooleanValue)v;
            value = booleanValue.value();
        }else if(v instanceof ByteValue){
            ByteValue byteValue = (ByteValue)v;
            value = byteValue.value();
        }else if(v instanceof CharValue){
            CharValue charValue = (CharValue)v;
            value = charValue.value();
        }else if(v instanceof DoubleValue){
            DoubleValue doubleValue = (DoubleValue)v;
            value = doubleValue.value();
        }else if(v instanceof FloatValue){
            FloatValue floatValue = (FloatValue)v;
            value = floatValue.value();
        }else if(v instanceof IntegerValue){
            IntegerValue integerValue = (IntegerValue)v;
            value = integerValue.value();
        }else if(v instanceof LongValue){
            LongValue longValue = (LongValue)v;
            value = longValue.value();
        }else if(v instanceof ShortValue){
            ShortValue shortValue = (ShortValue)v;
            value = shortValue.value();
        }else if(v instanceof StringReference){
            StringReference stringReference = (StringReference)v;
            value = stringReference.value();
        }else if(v instanceof ObjectReference){
            ObjectReference objectReference = (ObjectReference)v;
            if(((ClassType)objectReference.type()).superclass().name().equals("java.lang.Number"))
                value = objectToPrimitive(objectReference);
            else
                value = objectReference;
        }

        return value;
    }

    public Object objectToPrimitive(ObjectReference obj){
        Field field = obj.referenceType().fieldByName("value");
        if(field != null){
            Value v = obj.getValue(field);
            return valueToObject(v);
        }
        else
            return obj;
    }

    public Object getField(ObjectReference val, String fieldName){
        Field field = val.referenceType().fieldByName(fieldName);
        return valueToObject(val.getValue(field));
    }

    public Value runMethod(ObjectReference val,String methodName, List<Value> args) {
        ReferenceType t = val.referenceType();
        Method m2 = t.methodsByName(methodName).iterator().next();
        Value res = null;
        try {
            res = val.invokeMethod(threadReference, m2, args, 0);
        }catch (Exception e){
            e.printStackTrace();
        }

        return res;
    }

    public void setThread(ThreadReference thread){
        this.threadReference = thread;
    }
}
