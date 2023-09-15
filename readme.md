# Combinator

A simple combinator parser library for Java >=11.


## Features


### Lexing 

```java
Cursor text = new Cursor("hello 1234");

lex("hello").parse(text);               // returns "hello"

lex("hello").parse(text);               // failure

lex(" ").parse(text);                   // returns " "

lex("\\d+").parse(text);                // returns "1234"
```


### Combinators 


Concatenation and sequencing

```java
Combinator website = 
           lex("\w+").
           then(lex("\\.")).
           then(lex("\w+"));

website.parse(new Cursor("google.com")); // returns the pair ("google", (".", "com"))


Combinator numbers = 
           several(
               lex("\\d+"), 
               lex(",")
           );
           
numbers.parse(new Cursor("1"));         // returns the list ["1"]
numbers.parse(new Cursor("1,2,3"));     // returns the list ["1", "2", "3"]


// use all(A, B, C, ...) as a shorthand for A.then(B).then(C)...

Combinator oneTwoThree =
           all(
               lex("1"), 
               lex("2"), 
               lex("3")
           );

oneTwoThree.parse(new Cursor("12"));   // failure
oneTwoThree.parse(new Cursor("123"));  // returns the pair ("1", "2", "3")    
```

Choice

```java
Choice numberOrWord = 
       lex("\\d+").
       or(lex("\\w+");


numberOrWord.parse("pizza"); // returns "pizza"
numberOrWord.parse("1337");  // returns "1337"
numberOrWord.parse("???");   // failure


// use any(A, B, C, ...) as a shorthand for A.or(B).or(C)...

Combinator weather = 
           any(
               lex("rain"), 
               lex("sun"), 
               lex("snow")
           );

several(
    weather, 
    lex(" ")
)
.parse(new Cursor("rain sun")); // returns the list ["rain", "sun"]
```

Omission

```java
// Use skip() to omit tokens from the parse result

Combinator website2 = 
           lex("\\w+").
           skip(lex("\\.")).
           lex("\\w+");

website2.parse("google.com"); // returns the pair ("google", "com")
```


### Evaluation

This library supports evaluation of parse results via the `to()` combinator.

```java
Combinator number = 
           lex("\\d+").
           to(Integer::parseInt);

Combinator add = 
           lex("\\+");

Combinator addition =
           all(
               number,
               skip(add),
               number
           ).
           to(
               (a, b) -> a + b
           );
           
addition.parse("1+3");   // returns the integer 4
addition.parse("10+12"); // returns the integer 22
```
