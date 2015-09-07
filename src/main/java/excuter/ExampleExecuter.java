package excuter;

import com.sun.jdi.*;

import java.util.Map;

/**
 * Created by leolin on 9/4/15.
 */
public class ExampleExecuter extends Executer {

    public int getLineNumber(){
        return 84;
    }

    public String getClassName(){
        return "Target Class";
    }

    public void execute(Map<String, Value> values){
        for(String name : values.keySet()){
            Value v = values.get(name);
            System.out.print(name + " ");
            System.out.println(v);
            if( name.equals("Some String variable")){
                String stringExample = (String)valueToObject(v);
                //Play with the string
            }else if(name.contains("Some Object variable")){
                //Ex: Class test with String field s and Long field l
                ObjectReference objExample = (ObjectReference)valueToObject(v);
                //Get Field String s in objExample
                String s = (String)getField(objExample, "s");
                //Get Field Long l in objExample
                Long l = (Long)getField(objExample,"l");
            }else if(name.contains("Some Array variable")){
                Object[] array = (Object[])valueToObject(v);
                //Cast this array to its original type
            }
        }
    }
}
