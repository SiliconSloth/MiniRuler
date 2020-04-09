package siliconsloth.miniruler.planner

/**
 * A variable in the planning problem of type T.
 *
 * @param initializeDomain returns the full domain of the variable (e.g. all positive integers)
 */
class Variable<T>(val initializeDomain: () -> Domain<T> = { AnyValue() })