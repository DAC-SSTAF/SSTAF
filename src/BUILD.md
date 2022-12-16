# Introduction

This directory includes the source code, resources, test cases and build scripts for the core components of the Soldier
and Squad Trade-space Analysis Framework (SSTAF).

# Project Structure

This project is organized as shown in Table 1. The only artifacts intended for use in production are found in the "
framework" and "features" directories.

Table 1: Subdirectories

| Directory | Description  |
|:----------|:----------|
| build     | Build artifacts (gradle convention)  | 
| buildSrc  | Source code for custom Gradle components   |
| features  | Source, test and resources for plugin feature modules that are <br> included with the core system |
| framework | Source, test and resources for the core SSTAF modules |
| testFeatures | Source for plugin modules used for testing. These features <br> should not be used for production runs.  |

# Building SSTAF

## Java

SSTAF requires Java Development Kit (JDK) version 11 or later. SSTAF uses Java 11 language features and cannot be run on
an earlier version without significant changes.

SSTAF does not use any dangerous or deprecated constructs such as the sun.misc.Unsafe class, so it should compile and
run without issue on newer JDKs. Future work will modernize SSTAF in accordance with new language constructs.

## Gradle

The minimum Gradle version that has been used to build and test SSTAF is Gradle 6.7.1. Gradle 6.4 might be sufficient
since it is advertised to have full support for Java modules (Jigsaw), but this has not been tested.



