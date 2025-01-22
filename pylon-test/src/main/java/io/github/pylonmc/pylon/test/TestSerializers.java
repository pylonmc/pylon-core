package io.github.pylonmc.pylon.test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class TestSerializers {
    public Boolean testAllSerializers() throws InvocationTargetException, IllegalAccessException {
        Method[] methods = getClass().getMethods();
            for (int i = 0; i < methods.length; i++) {
                if (!(Boolean) methods[i].invoke(this)) return false;
            }
        return true;
    }
}


/*package io.github.pylonmc.pylon.test

class TestSerializers {
    companion object {
        @JvmStatic
        fun testAllSerializers(): Boolean {
            try {
                for (method in this.javaClass.methods) {
                    if (method.name != "testAllSerializers" && !(method.invoke(null, null) as Boolean)) {
                        return false
                    }
                }
                return true
            } catch (e: Exception) {
                return false
            }
        }
        // Serializer tests will be added here in a future PR
    }
}*/