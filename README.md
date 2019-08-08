# BUT4Reuse helper to use the ArgoUML SPL Benchmark

This plugin allows to easily work with the ArgoUML SPL Benchmark when using feature location techniques implemented within the BUT4Reuse framework.
Its main features are:
* Automatic generation of BUT4Reuse assets from the benchmark's predefined scenarios. For each scenario, an "artefactmodel" and a "featurelist" will be automatilly created. These are the assets that BUT4Reuse needs to launch feature location techniques.
* After launching a feature location technique in BUT4Reuse, the results will be automatically exported in the Benchmark format. This way you will be able to get the metrics from the benchmark for your feature location technique. BUT4Reuse includes a Java adapter based on JDT elements. Once a feature location technique is finished, the JDT elements associated to each feature (or feature interaction, or feature negation) are then processed to comply with the benchmark format. This can be sometimes tricky so this will hide this complexity so the focus of the users can be on the feature location technique using the BUT4Reuse interfaces.


For information on the ArgoUML SPL Benchmark: https://github.com/but4reuse/argouml-spl-benchmark

For information on BUT4Reuse: https://github.com/but4reuse/but4reuse
## Setting-up
### Setting-up BUT4Reuse and this helper
* Prepare BUT4Reuse. Download and import all projects from the BUT4Reuse repository. Instructions can be found here https://github.com/but4reuse/but4reuse/wiki/Installation
* Import the project source code (org.but4reuse.benchmarks.argoumlspl found in this repository) in the same workspace where you have all the BUT4Reuse source code.
* Now launch a runtime workspace. Instructions for running can be found also at https://github.com/but4reuse/but4reuse/wiki/Installation

### Setting-up the ArgoUML SPL Benchmark
* In this new runtime workspace (see previous section), prepare the ArgoUML SPL Benchmark. You have instructions for downloading and importing the projects here: https://github.com/but4reuse/argouml-spl-benchmark
* Get familiar with the benchmark by building some of the predefined scenarios etc. All info at the previously mentioned website.

## Using the helper
### Generate BUT4Reuse assets
* Right click the "scenarios" folder of the benchmark, and click on the "Create BUT4Reuse scenarios" action. Inside each of the predefined scenarios (for example inside "ScenarioTraditionalVariants") you will see that a folder called BUT4Reuse is created. There you will have an artefactmodel and a featurelist (for example ScenarioTraditionalVariants.artefactmodel and ScenariosTraditionalVariants.featurelist). By opening them you can observe that they actually correspond to the variants of this scenario, to the feature names and descriptions defined in the benchmark, and to the mappings of features to variants of the scenario.
### Generating benchmark results
* In the menu bar Window -> Preferences -> BUT4Reuse -> Visualisations, it is important that "ArgoUML SPL Feature Location results creation" is activated. This will allow to create the results in the benchmark format once the feature location technique has finished.
* Please check BUT4Reuse tutorials and user manual https://github.com/but4reuse/but4reuse/wiki to know how to select your feature location technique and configure the analysis chain (block identification etc.).
* You may also want to perform some featurelist preprocessing by creating feature negations and 2-wise or 3-wise interactions. https://github.com/but4reuse/but4reuse/wiki/UserManual
* Launch the feature location technique from which you want to get the results. https://github.com/but4reuse/but4reuse/wiki Open the featurelist model, select the featurelist, righ click and click on Launch feature location. 
* Once finished, the benchmark folder called "yourResults" will contain the results in the expected format by the benchmark (the txt files for each feature).
* The benchmark has the functionality to automatically create the metrics from the files on "yourResults". Check https://github.com/but4reuse/argouml-spl-benchmark

## Usage outside the ArgoUML SPL Benchmark
* You can activate the mentioned visualisation to create the results even if you are not using the assets of the ArgoUML SPL Benchamrk. That means that if you use the Java JDT adapter for feature location in any other project, the results folder will be created and files will be created inside this folder following the benchmark format.
