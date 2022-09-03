# Thinking in Ekko

Our goal in Ekko project, is having a full haskell-like language, with imports, lambdas, pattern matching, and even type
classes. And, said that, we need to model our language.

## Table of contents

- [Thinking in Ekko](#thinking-in-ekko)
  - [Language Features](#language-features)

## Language features

```haskell
data Either a b = Left a | Right b

data Maybe a = Nothing | Just a

data Person = Person {
  name : String,
  age : Int
}

external
println : String -> IO ()

module Person where
  -- omit the function type
  default = Person {
    name : "Carlos",
    age : 19
  }

  sayHello : Person -> IO ()
  sayHello (Person name age) =
    println "Hello, I'm $name, and i'm $age years old"

  -- or matching:
  -- sayHello : Person -> IO ()
  -- sayHello = \case
  --  (Person name age) -> println "Hello, I'm $name, and i'm $age years old"
  --
  -- or matching the parameter:
  -- sayHello : Person -> IO ()
  -- sayHello person = case person of
  --  (Person name age) -> println "Hello, I'm $name, and i'm $age years old"

people : [Person]
people = [
  Person.default,
  Person.default { name = "João" },
  Person.default { name = "Maria" }
]

main : IO ()
main = do
  traverse $ (\x -> sayHello x) <$> people
  -- or passing the lambda reference
  -- traverse $ sayHello <$> people

  println "hello, world"
```

But this model, is a little complex to start with, because, there are many features, like `type classes`(the `traverse`
function),
`lambdas`, `pattern matching`, `enums`, etc... And this is going to be hard to implement at the first view, so we will
simplify it.

```haskell
data Person = Person {
  name : String,
  age : Int
}

main : ()
main =
  let person = Person "Carlos" 19 in
  println person
```

And with this simple model, we will be incrementing throughout this article. This model does not have the following
features:

- interpolation
- type classes(instances, etc...)
- enums
- pattern matching
- externals
- do notation/monads
- pureness
