# Cocol SLR(0)

Provide an archive that follows Cocol grammar, then check if it's sintactically correct. Then, provide
an archive and lex it for token generation and parsing using an SLR parser.

## Video Demonstration

You can see a demonstration at:

```
https://youtu.be/RlzF9VBpuks
```

Note: video is in spanish.

### Prerequisites

Java SE8. SDK 8.

### Installing

Load the project to an IDE or copy the contents to a folder. 
Then compile the project from console or with IDE.

Note: Automated support for Jetbrains' IntelliJ (just load the project).

Main class is:

```
src\Run\Runnable.java
```

## Running the program

Load an archive with specifications, i.e., provided examples "SLR1.txt".
Then, load another test archive for token generation, such as "SLR1_RUN.txt".

## Program outputs

* For a successful analysis, program will prompt "exit code 0".
* If an error is detected, error will be logged in console, followed by "exit code 1".
* Program will output the recognized tokens, LR(0) automaton and the parsing process with its results.

## Authors

* **Gabriel Brolo** 

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details

## Acknowledgments

* Big shoutout to Guillaume Menard who's code helped me finish the infix to postfix converter:
https://gist.github.com/gmenard/6161825
* To Alfred Aho, for making such explicit algorithms in his book 'Compilators'.
* To Barry Brown, for explaining the NFA to DFA algorithm here: https://www.youtube.com/watch?v=taClnxU-nao
* However, the previous video NEEDED to be supplemented with: 
http://web.cecs.pdx.edu/~harry/compilers/slides/LexicalPart3.pdf

* Made in 2017 for Compiler Design Course at [Universidad del Valle de Guatemala](http://www.uvg.edu.gt/index2.html)
