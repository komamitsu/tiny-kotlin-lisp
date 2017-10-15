# tiny-kotlin-lisp
[<img src="https://travis-ci.org/komamitsu/tiny-kotlin-lisp.svg?branch=master"/>](https://travis-ci.org/komamitsu/tiny-kotlin-lisp)

Tiny Lisp compiler / interpreter written in Kotlin

## Supported keywords

- `car`
- `cdr`
- `cons`
- `if`
- `+`
- `-`
- `*`
- `/`
- `=`
- `/=`
- `<`
- `<=`
- `>`
- `>=`
- `defun`
- `setq`
- `print`

## Usage

Run this command to get an executable Jar file.

```
$ ./gradlew build
```

### Compiler mode

This mode compiles a specified Lisp code to Java class files and generates a Jar file from them. It doesn't support interaction mode (REPL)

You can use this mode by specifying `compile`. 

#### Compile and execute a Jar file

```
$ java -jar build/libs/tiny-kotlin-lisp-1.0-SNAPSHOT.jar -i ~/tmp/fib_37.lisp compile
39088169
```

If you want to check a generated Java source code, specify `-s` option.

```
$ java -jar build/libs/tiny-kotlin-lisp-1.0-SNAPSHOT.jar -i ~/tmp/fib_37.lisp -s GeneratedJavaCode.java compile
39088169

$ head -20 GeneratedJavaCode.java
package org.komamitsu.tinylisp;

public class Runner {
public static Node fib(
    Node n
)
{
{
Node var1 = null;
// Pushed a var name (defun): var1
// Entering 'if'
Node var2 = null;
// Pushed a var name (if: cond): var2
{
Node var3 = null;
// Pushed a var name (evalAndReturn): var3
{
// Entering '<='
Node var4 = null;
// Pushed a var name (declareVar): var4
```

#### Just build a Jar file

You can get a built Jar file w/o executing it by specifying `-o` option. With this option, `-s` option above is also available.

```
$ java -jar build/libs/tiny-kotlin-lisp-1.0-SNAPSHOT.jar -i ~/tmp/fib_37.lisp -o CompiledLisp.jar compile
$ java -jar CompiledLisp.jar
39088169
```

### Interpreter mode

This mode just interprets a Lisp code.

You can use this mode by specifying `interpret`. 


#### REPL

```
$ java -jar build/libs/tiny-kotlin-lisp-1.0-SNAPSHOT.jar interpret
```

```
> (defun fib (n) (if (<= n 1) 1 (+ (fib (- n 1)) (fib (- n 2)))))

> (print (fib 37))
39088169
```

#### Evaluate a specified Lisp code

```
$ java -jar build/libs/tiny-kotlin-lisp-1.0-SNAPSHOT.jar -i ~/tmp/fib_37.lisp interpret
39088169
```


