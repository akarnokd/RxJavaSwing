# RxJavaSwing

<a href='https://github.com/akarnokd/RxJavaSwing/actions?query=workflow%3A%22Java+CI+with+Gradle%22'><img src='https://github.com/akarnokd/RxJavaSwing/workflows/Java%20CI%20with%20Gradle/badge.svg'></a>
[![codecov.io](http://codecov.io/github/akarnokd/RxJavaSwing/coverage.svg?branch=3.x)](http://codecov.io/github/akarnokd/RxJavaSwing?branch=3.x)


Bridge between Java 6 Swing (GUI) events and RxJava 2/3 + a scheduler for the swing event dispatch thread.

# Releases

Maven: [http://search.maven.org](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.github.akarnokd%22)

## RxJava 3

RxJavaSwing 3.x: [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.akarnokd/rxjava3-swing/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.akarnokd/rxjava3-swing)
RxJava 3.x: [![RxJava 3.x](https://maven-badges.herokuapp.com/maven-central/io.reactivex.rxjava3/rxjava/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.reactivex.rxjava3/rxjava)


**gradle**

```
dependencies {
    compile "com.github.akarnokd:rxjava3-swing:3.1.1"
}
```



## RxJava 2 (End of Life)

RxJavaSwing 0.x: [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.github.akarnokd/rxjava2-swing/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.akarnokd/rxjava2-swing)
RxJava 2.x: [![RxJava 2.x](https://maven-badges.herokuapp.com/maven-central/io.reactivex.rxjava2/rxjava/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.reactivex.rxjava2/rxjava)

**gradle**

```
dependencies {
    compile "com.github.akarnokd:rxjava2-swing:0.3.7"
}
```

# Usage

```java
import hu.akarnokd.rxjava3.swing.*;
```

## Notable classes

- SwingObservable
- SwingSchedulers
- RxSwingPlugins
