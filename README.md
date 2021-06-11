# NIS Practical 2021
## Pretty Good Privacy (PGP) Cryptosystem

### Group Members
1. Aidan Bailey
2. Insaaf Dhansay
3. Emily Morris
4. Kialan Pillay

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