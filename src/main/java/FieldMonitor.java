import com.sun.jdi.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.BreakpointRequest;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequestManager;
import excuter.ExampleExecuter;
import excuter.Executer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by leolin on 9/3/15.
 */
public class FieldMonitor {

    public static void main(String[] args) throws IOException, InterruptedException {
        // connect
        int port = Integer.valueOf(args[0]);
        VirtualMachine vm = new VMAcquirer().connect(port);
        Executer executer = new ExampleExecuter();

        // set watch field on already loaded classes
        List<ReferenceType> allReferenceType = vm.allClasses();
        List<ReferenceType> referenceTypes = new ArrayList<ReferenceType>();
        for(ReferenceType referenceType: allReferenceType){
            if(referenceType.name().contains(executer.getClassName())){
                referenceTypes.add(referenceType);
            }
        }
        for (ReferenceType refType : referenceTypes) {
            addLineWatch(vm, refType, executer.getLineNumber());
        }

        // resume the vm
        vm.resume();

        // process events
        EventQueue eventQueue = vm.eventQueue();
        while (true) {
            EventSet eventSet = eventQueue.remove();
            for (Event event : eventSet) {
                if (event instanceof VMDeathEvent || event instanceof VMDisconnectEvent) {
                    // exit
                    return;
                } else if (event instanceof BreakpointEvent){
                    BreakpointEvent breakpointEvent = (BreakpointEvent) event;
                    try {
                        breakpointEvent.thread().suspend();
                        StackFrame stackFrame = breakpointEvent.thread().frames().get(0);
                        List<LocalVariable> localVariables = stackFrame.visibleVariables();
                        List<Field> fields = stackFrame.thisObject().referenceType().fields();
                        Map<String, Value> values = new HashMap<String, Value>();
                        if(localVariables != null) {
                            for(LocalVariable localVariable: localVariables) {
                                Value v = stackFrame.getValue(localVariable);
                                values.put(localVariable.name(),v);
                            }
                        }
                        if(fields != null) {
                            for(Field field: fields) {
                                Value v = stackFrame.thisObject().getValue(field);
                                values.put(field.name(), v);
                            }
                        }
                        executer.setThread(breakpointEvent.thread());
                        executer.execute(values);
                        breakpointEvent.thread().resume();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
            eventSet.resume();
        }
    }

    /** Watch all classes of name "Test" */
    private static void addClassWatch(VirtualMachine vm, String className) {
        EventRequestManager erm = vm.eventRequestManager();
        ClassPrepareRequest classPrepareRequest = erm.createClassPrepareRequest();
        classPrepareRequest.addClassFilter(className);
        classPrepareRequest.setEnabled(true);
    }

    private static void addLineWatch(VirtualMachine vm, ReferenceType refType, int line) {
        EventRequestManager erm = vm.eventRequestManager();
        try {
            List<Location> location = refType.allLineLocations();
            List<Location> locations = refType.locationsOfLine(line);
            if(locations.size() > 0) {
                BreakpointRequest breakpointEvent = erm.createBreakpointRequest(locations.get(0));
                breakpointEvent.setEnabled(true);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}