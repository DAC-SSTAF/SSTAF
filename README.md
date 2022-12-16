# The Soldier and Squad Trade Space Analysis Framework (SSTAF) 

## Introduction

This project contains the core components of the Soldier and Squad Trade Space Analysis Framework (SSTAF). SSTAF is a software infrastructure system for integrating multiple human performance and other models to provide a unified representation of Soldier state, capability and behavior. SSTAF models the Soldier as a system, where the results of one model can affect the results of other models, and both the positive and negative effects of Soldier equipment can be captured. The ultimate goal of SSTAF is to provide an architecture that enables the development of digital twins for specific Soldiers. These digital twins can be used not only for material trade space analysis but also for interactive training and mission planning.

The key capabilities of SSTAF are the following:
* Model Soldier state and capability, update the state according to simulation events and modify the behavior of integrated models according to the current state.
* Support flexible anthropometric, human performance and equipment configurations to enable modeling at multiple levels of resolution to include modeling specific individual Soldiers.
* Provide an extensible application programming interface (API) usable for both interactive systems and force-on-force models.

SSTAF was developed by the U.S. Army Combat Capabilities Development Command (DEVCOM) Analysis Center (DAC). Development began in January 2019. The SSTAF core and several plugin modules were released to the public in December 2022 through the publication of DEVCOM DAC-TR-2022-112. The report includes the entire unrestricted SSTAF codebase as appendices. This project was created from those appendices.

# Project Structure

This project is organized as shown in Table 1. 


Table 1: Subdirectories

| Directory | Description  |
|:----------|:----------|
| LICENSE.MD | The Apache License, version 2 license file | 
| README.MD | This file |
| doc | The set of formal, public release documentation for SSTAF |
| plugin | The source for the SSTAF Gradle plugin. The plugin is required for building SSTAF |
| src | The SSTAF source tree |

# Building SSTAF

Building SSTAF requires two steps.

1. Compile and install the SSTAF Gradle plugin, In the 'plugin' directory, invoke “gradle build publishToMavenLocal”.
1. Compile and install the SSTAF system. In the 'src' directory, invoke “gradle build”.


# Documents

The *docs* directory contains formal documention that was created for the SSTAF project.

| File | Title | Description  |
|:----------|:-------|:----------|
| DEVCOM_DAC-TR-2021-007.pdf     | The Soldier and Squad Trade Space Analysis Framework (SSTAF) | This technical report describes the motivation, central concepts, and architecture of SSTAF. |
| DEVCOM_DAC-TR-2022-112.pdf     | The Soldier and Squad Trade Space Analysis Framework (SSTAF) Interactive Analyzer| This technical report describes a stand-alone application for running SSTAF-based analyses. It also documents SSTAF input files and configuration options. |
