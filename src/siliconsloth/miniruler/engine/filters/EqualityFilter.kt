package siliconsloth.miniruler.engine.filters

class EqualityFilter<T: Any>(val target: () -> T): Filter<T>({
    it == target()
}) {
}