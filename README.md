# NIS Practical 2021
## Pretty Good Privacy (PGP) Cryptosystem

### Group Members
1. Aidan Bailey (blyadi001)
2. Insaaf Dhansay (dhnins001)
3. Emily Morris (mrremi007)
4. Kialan Pillay (pllkia010)

### Installation and Execution
Gradle, a build automation tool, is used to manage the compilation and execution of the sources. You do not need to 
manually install Gradle, the Gradle Wrapper is used to invoke a declared version of Gradle.

Note that separate terminal sessions are required.

The following command **must** be executed first. It populates a keystore and saves the file to disk.
```
./gradlew run '-PmainClassName=CertificateAuthority' --console=plain
```
The server runs on port 4444 and listens for incoming client requests.
A client that attempts to initiate communication with a server that is not active will result in an error.
```
./gradlew run '-PmainClassName=Server' --console=plain
```
```
./gradlew run '-PmainClassName=Client' --console=plain
```
```
./gradlew run '-PmainClassName=Client' --console=plain
```

### Additional Notes
Note that whilst `Server` and `Client` *can* accept arguments to configure the default hostname ("localhost") and port (4444),
the Gradle `run` task is configured to execute the programs with the default values only. 
This is the recommended configuration; attempting to run the sources manually using `java` is incompatible with Gradle.