package io.github.pylonmc.pylon.core

class SchemaNotFoundException(id: String) : Exception("Schema '$id' not found")
class NotRegisteredException(id: String) : Exception("'$id' is not registered")
class AlreadyRegisteredException(id: String) : Exception("'$id' is already registered")
class MissingPlaceConstructorException(id: String) : Exception("'$id' is missing a place constructor")
class MissingLoadConstructorException(id: String) : Exception("'$id' is missing a load constructor")
