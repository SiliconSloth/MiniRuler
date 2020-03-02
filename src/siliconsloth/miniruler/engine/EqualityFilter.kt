package siliconsloth.miniruler.engine

class EqualityFilter<T: Any>(val target: () -> T): Filter<T>({
    it == target()
}) {
}