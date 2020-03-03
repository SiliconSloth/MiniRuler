package siliconsloth.miniruler.engine.filters

open class Filter<T>(val predicate: (T) -> Boolean)