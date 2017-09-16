# tiny-kotlin-lisp
[<img src="https://travis-ci.org/komamitsu/tiny-kotlin-lisp.svg?branch=master"/>](https://travis-ci.org/komamitsu/tiny-kotlin-lisp)

Tiny Lisp interpreter written in Kotlin

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

### Interaction mode

```
$ ./gradlew run -q
```
or

```
$ ./gradlew build
$ java -jar build/libs/tiny-kotlin-lisp-1.0-SNAPSHOT.jar
```

```
> (defun fib (n) (if (<= n 1) 1 (+ (fib (- n 1)) (fib (- n 2)))))

> (print (fib 37))
39088169
```

### Batch mode

```
$ ./gradlew build
$ java -jar build/libs/tiny-kotlin-lisp-1.0-SNAPSHOT.jar -i ~/tmp/fib_37.lisp
39088169
```
