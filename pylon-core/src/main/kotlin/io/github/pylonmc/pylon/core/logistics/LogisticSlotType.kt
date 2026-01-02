package io.github.pylonmc.pylon.core.logistics

enum class LogisticSlotType {
    /**
     * Input to the attached machine
     */
    INPUT,

    /**
     * Output from the attached machine
     */
    OUTPUT,

    /**
     * Both input to and output from the attached machine
     */
    BOTH,
}