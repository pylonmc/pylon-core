package io.github.pylonmc.pylon.core.util

import java.util.Random
import java.util.concurrent.ThreadLocalRandom

class WeightedSet<E> @JvmOverloads constructor(
    private val innerSet: MutableSet<WeightedElement<E>> = mutableSetOf()
) : AbstractMutableSet<WeightedSet.WeightedElement<E>>() {

    constructor(value: E, weight: Float) : this(mutableSetOf(WeightedElement(value, weight)))
    @SafeVarargs
    constructor(vararg values: Pair<E, Float>) : this(values.map { WeightedElement(it.first, it.second) }.toMutableSet())

    private var totalWeight: Float = innerSet.fold(0f) { acc, element -> acc + element.weight }

    override val size by innerSet::size

    val elements: Set<E>
        get() = innerSet.map { it.element }.toSet()

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

    override fun add(element: WeightedElement<E>): Boolean {
        if (innerSet.add(element)) {
            totalWeight += element.weight
            return true
        }
        return false
    }

    override fun iterator(): MutableIterator<WeightedElement<E>> {
        val innerIterator = innerSet.iterator()
        return object : MutableIterator<WeightedElement<E>> by innerIterator {
            private lateinit var lastItem: WeightedElement<E>

            override fun next(): WeightedElement<E> {
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
    data class WeightedElement<E>(val element: E, val weight: Float)
}