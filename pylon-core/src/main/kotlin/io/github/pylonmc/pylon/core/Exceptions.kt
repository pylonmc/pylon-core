package io.github.pylonmc.pylon.core

import org.bukkit.NamespacedKey

class SchemaNotFoundException(id: String) : Exception("Schema '$id' not found")
class NotRegisteredException(id: String) : Exception("'$id' is not registered")
class AlreadyRegisteredException(id: String) : Exception("'$id' is already registered")
class MissingPlaceConstructorException(key: NamespacedKey) : Exception("'$key' is missing a place constructor")
class MissingLoadConstructorException(key: NamespacedKey) : Exception("'$key' is missing a load constructor")
