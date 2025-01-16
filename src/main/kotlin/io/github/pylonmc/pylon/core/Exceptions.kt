package io.github.pylonmc.pylon.core

import org.bukkit.NamespacedKey

class NotRegisteredException(id: String) : Exception("'$id' is not registered")
class AlreadyRegisteredException(id: NamespacedKey) : Exception("'$id' is already registered")
class MissingPlaceConstructorException(id: String) : Exception("'$id' is missing a place constructor")
class MissingLoadConstructorException(id: String) : Exception("'$id' is missing a load constructor")
