package siliconsloth.miniruler.engine

open class Filter<T>(val predicate: (T) -> Boolean)