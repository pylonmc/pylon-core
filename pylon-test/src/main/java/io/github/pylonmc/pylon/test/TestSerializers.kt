package io.github.pylonmc.pylon.test

class TestSerializers {
    companion object {
        @JvmStatic
        fun testAllSerializers(): Boolean {
            try {
                for (method in this.javaClass.methods) {
                    if (method.name != "TestAllSerializers" && !(method.invoke(null, null) as Boolean)) {
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
}