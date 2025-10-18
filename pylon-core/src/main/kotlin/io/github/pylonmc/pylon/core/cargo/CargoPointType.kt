package io.github.pylonmc.pylon.core.cargo

enum class CargoPointType {
    /**
     * Input to the attached machine
     */
    INPUT,

    /**
     * Output from the attached machine
     */
    OUTPUT,

    /**
     * Corner (cargo ducts cannot have intersections)
     */
    CORNER,
}