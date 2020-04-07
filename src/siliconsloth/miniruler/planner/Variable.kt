package siliconsloth.miniruler.planner

class Variable<T>(val initializeDomain: () -> Domain<T> = { AnyValue() })