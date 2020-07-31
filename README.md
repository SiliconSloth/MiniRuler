**This branch contains an old version of MiniRuler that actually plays the game. The latest developments, including the timeline viewer, are only available on the `master` brnach.**

MiniRuler is a rule-based AI that plays the top-down survival game [Minicraft](https://en.wikipedia.org/wiki/Minicraft).
It doesn't use any fancy machine learning, but rather features a large number of
hard-coded game-specific rules that tell the agent how to respond to changes in
the environment. This project was mainly intended to teach myself about rule-based
AI, and the pros and cons of this type of programming. MiniRuler is not complete,
and only plays the game up to the point of crafting some tools and avoiding monsters, however it has given me valuable insights into the potential power of rule-based programming.

## Running the Software

MiniRuler is built using Gradle. You can run various parts of the project as follows:
* To run Minicraft by itself, use `./gradlew game:run`
* To run the MiniRuler AI, use `./gradlew main:run`

## The Rule Engine

MiniRuler's rule engine is a vital component of the system, which allows the definition of rules that fire when there are facts in the global fact base which match the conditions of the rule, usually adding more facts to the fact base. These rules are written in a pure-Kotlin DSL, with syntax similar to the example below:

```kotlin
// Give a new robot to all children who don't already have a toy.
rule {
    val child by find<Person> { age < 10 }
    not<Ownership> { owner == child && item is Toy }

    fire {
        val toy = RobotToy()
        insert(toy)
        insert(Ownership(child, toy))
    }
}
```

These rules are written in pure Kotlin, with the help of concise custom syntax enabled by Kotlin's elegant language features. This means that unlike other rule engines that employ a completely separate custom DSL, you can easily integrate rules into normal code files and interoperate with imperative code. All your IDE's fancy code inspection features will work out the box too!

MiniRuler's rule engine conceptually fires many rules in parallel, so with continued optimizations it could become a highly performant way to execute complex AI software. The unstructured nature of a rule-based program makes it easy to define a large number of loosely related behaviours that share data, without struggling with constant restructuring as the program grows.

## Acknowledgements

MiniRuler includes a modified version of the source code of [Minicraft](https://en.wikipedia.org/wiki/Minicraft), created by Markus Persson.