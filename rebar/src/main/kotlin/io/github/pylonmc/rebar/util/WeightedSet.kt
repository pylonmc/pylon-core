package io.github.pylonmc.rebar.util

import java.util.Random
import java.util.concurrent.ThreadLocalRandom

/**
 * A set containing elements with associated weights. You can retrieve either a specified subset
 * or a single element randomly, with the probability of each element being chosen proportional to its weight.
 *
 * For example, if you have elements A, B, and C with weights 0.25, 0.25, and 0.5 respectively,
 * element C will be chosen approximately 50% of the time, while A and B will each be chosen about 25% of the time.
 */
class WeightedSet<E> @JvmOverloads constructor(
    private val innerSet: MutableSet<Element<E>> = mutableSetOf()
) : AbstractMutableSet<WeightedSet.Element<E>>() {

    constructor(value: E, weight: Float) : this(mutableSetOf(Element(value, weight)))
    @SafeVarargs
    constructor(vararg values: Pair<E, Float>) : this(values.map { Element(it.first, it.second) }.toMutableSet())

    private var totalWeight: Float = innerSet.fold(0f) { acc, element -> acc + element.weight }

    override val size by innerSet::size

    val elements: Set<E>
        get() = innerSet.map { it.element }.toSet()

    /**
     * Returns a random subset of the specified size from the set, with selection probability based on weights.
     * The subset will contain unique elements and always be of the requested size.
     *
     * @param size The number of unique elements to select.
     * @param random An optional [Random] instance to use for selection. Defaults to [ThreadLocalRandom].
     * @throws IllegalArgumentException if [size] is negative or exceeds the number of elements in the set.
     */
    @JvmOverloads
    fun getRandomSubset(size: Int, random: Random = ThreadLocalRandom.current()): Set<E> {
        require(size >= 0) { "Size must be non-negative" }
        require(size <= this.size) { "Size must not exceed the number of elements in the set" }

        if (size == 0) return emptySet()
        if (size == this.size) return innerSet.map { it.element }.toSet()

        val selected = mutableSetOf<E>()
        val remaining = innerSet.toMutableList()
        var remainingWeight = totalWeight
        repeat(size) {
            val r = random.nextFloat(remainingWeight)
            var cumulativeWeight = 0.0
            var chosen = -1
            for (i in remaining.indices) {
                cumulativeWeight += remaining[i].weight
                if (r < cumulativeWeight) {
                    chosen = i
                    break
                }
            }
            if (chosen != -1) {
                selected.add(remaining[chosen].element)
                remainingWeight -= remaining[chosen].weight
                remaining.removeAt(chosen)
            }
        }
        return selected
    }

    /**
     * Returns a single random element from the set, with selection probability based on weights.
     * @param random An optional [Random] instance to use for selection. Defaults to [ThreadLocalRandom].
     * @throws IllegalArgumentException if the set is empty.
     */
    @JvmOverloads
    fun getRandom(random: Random = ThreadLocalRandom.current()): E {
        require(this.isNotEmpty()) { "Set must not be empty" }
        val r = random.nextFloat(totalWeight)
        var cumulativeWeight = 0.0
        for (element in innerSet) {
            cumulativeWeight += element.weight
            if (r < cumulativeWeight) {
                return element.element
            }
        }
        throw AssertionError("Should not reach here")
    }

    override fun add(element: Element<E>): Boolean {
        if (innerSet.add(element)) {
            totalWeight += element.weight
            return true
        }
        return false
    }

    override fun iterator(): MutableIterator<Element<E>> {
        val innerIterator = innerSet.iterator()
        return object : MutableIterator<Element<E>> by innerIterator {
            private lateinit var lastItem: Element<E>

            override fun next(): Element<E> {
                lastItem = innerIterator.next()
                return lastItem
            }

            override fun remove() {
                innerIterator.remove()
                totalWeight -= lastItem.weight
            }
        }
    }

    @JvmRecord
    data class Element<E>(val element: E, val weight: Float)
}