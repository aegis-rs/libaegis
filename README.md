# libaegis - Library for the Aegis Chat application

## Overview
This library currently consists of two main components:

1. **API Communication Layer** – a set of classes for interacting with the **Ory Kratos** API.  
2. **Encryption Layer** – a set of classes implementing the **Double Ratchet algorithm** and providing encryption functionality based on it.

> **Note**: The **Double Ratchet algorithm implementation** currently only works with the **JVM**.

## Installation

Version: `dev.teamnight.aegis:libaegis:1.0-ALPHA-2`

The library is available on GitHub Packages maven repository using the URL `https://maven.pkg.github.com/aegis-rs/libaegis`.

Example (Gradle Kotlin DSL):

```kotlin
repositories {
    mavenCentral()
    maven {
        url = uri("https://maven.pkg.github.com/aegis-rs/libaegis")
        credentials {
            username = System.getenv("GITHUB_USER")
            password = System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    implementation("dev.teamnight.aegis:libaegis:1.0-ALPHA-2")
}
```
## Kratos API Quickstart

The main class to interact with the Ory Kratos API is [KratosApi](https://github.com/aegis-rs/libaegis/blob/main/src/commonMain/kotlin/dev/teamnight/aegis/libaegis/kratos/KratosApi.kt).
[KratosApi](https://github.com/aegis-rs/libaegis/blob/main/src/commonMain/kotlin/dev/teamnight/aegis/libaegis/kratos/KratosApi.kt) provides several methods to call the flows of Ory Kratos:
- [`createRegistrationFlow()`](https://github.com/aegis-rs/libaegis/blob/695f9846c7cad944ddc10144cfe53e4f87ad3c38/src/commonMain/kotlin/dev/teamnight/aegis/libaegis/kratos/KratosApi.kt#L38)
starts the registration flow and provides data to render the UI using `RegistrationFlow#state`
- [`createLoginFlow()`](https://github.com/aegis-rs/libaegis/blob/695f9846c7cad944ddc10144cfe53e4f87ad3c38/src/commonMain/kotlin/dev/teamnight/aegis/libaegis/kratos/KratosApi.kt#L48) starts the login flow like the registration flow
- [`refreshSession()`](https://github.com/aegis-rs/libaegis/blob/695f9846c7cad944ddc10144cfe53e4f87ad3c38/src/commonMain/kotlin/dev/teamnight/aegis/libaegis/kratos/KratosApi.kt#L58) refreshes the user session information
- and [`isReady()`](https://github.com/aegis-rs/libaegis/blob/695f9846c7cad944ddc10144cfe53e4f87ad3c38/src/commonMain/kotlin/dev/teamnight/aegis/libaegis/kratos/KratosApi.kt#L84) checks if the API is healthy to react.

#### Sample
```kotlin
val kratosApi = KratosApi("http://localhost:4433")

runBlocking {
    val flow = kratosApi.createRegistrationFlow()

    val traits = Traits(
        "teamnight",
        "admin@teamnight.dev",
        "teamnight"
    )

    val step1 = flow.update(RegistrationParams.Profile(traits))

    val response = flow.update(
        RegistrationParams.Password(
            traits = traits,
            password = "a5BNzu357S"
        )
    )

    if(response is RegistrationFlowCompleted) {
        println(state.result.sessionToken)
    }
}
```

## Double Ratchet Quickstart

The other part implements the Double Ratchet algorithm. As an user you can use the [DoubleRatchet](https://github.com/aegis-rs/libaegis/blob/main/src/commonMain/kotlin/dev/teamnight/aegis/libaegis/crypto/DoubleRatchet.kt) class to use
the algorithm in your application. Typically you need to initialize the key pairs before and calculate a Root Key using X3DH as provided below.

> **Note**: The sample below should not be used in real world.

#### Sample
```kotlin
import dev.teamnight.aegis.libaegis.crypto.DoubleRatchet
import dev.teamnight.aegis.libaegis.crypto.algorithm.ECDHResult
import dev.teamnight.aegis.libaegis.crypto.key.RatchetKey
import dev.teamnight.aegis.libaegis.crypto.key.RootKey

// Example: derive ECDH results from identity and prekeys (X3DH)
val aliceIk = RatchetKey.generate()      // Alice identity key (private/public)
val aliceEk = RatchetKey.generate()      // Alice ephemeral key
val bobIk   = RatchetKey.generate()      // Bob identity key
val bobSpk  = RatchetKey.generate()      // Bob signed prekey
val bobOpk  = RatchetKey.generate()      // Bob one‑time prekey

// ECDH for both sides (simplified; in practice sign/verify keys)
val aResults = arrayOf(
    ECDHResult(aliceIk.privateKey!!, bobIk.publicKey),
    ECDHResult(aliceEk.privateKey!!, bobSpk.publicKey),
    ECDHResult(aliceIk.privateKey!!, bobSpk.publicKey),
    ECDHResult(aliceEk.privateKey!!, bobOpk.publicKey),
)

val (aliceRoot, aliceChain) = RootKey.generateInitialRootKey(aResults)

// Alice starts without a received ratchet key -> initial chain is the sending chain
val alice = DoubleRatchet(keys = aliceRoot to aliceChain, receivedRatchetKey = null)

// Bob initializes with Alice's current ratchet public key so the first message is decryptable
val bobResults = arrayOf(
    ECDHResult(bobIk.privateKey!!, aliceIk.publicKey),
    ECDHResult(bobSpk.privateKey!!, aliceEk.publicKey),
    ECDHResult(bobSpk.privateKey!!, aliceIk.publicKey),
    ECDHResult(bobOpk.privateKey!!, aliceEk.publicKey),
)
val (bobRoot, bobChain) = RootKey.generateInitialRootKey(bobResults)
val bob = DoubleRatchet(keys = bobRoot to bobChain, receivedRatchetKey = alice.ownRatchetKey)
```

Alternatively, if you have a persisted state, use `DoubleRatchet.fromExisting(...)` to restore it without desynchronizing the peer.

#### Sending and receiving messages

```kotlin
// Alice encrypts and sends
val ciphertext = alice.encrypt("Hello Bob".encodeToByteArray())

// Bob decrypts (may perform a ratchet step based on the header)
val plaintext = bob.decrypt(ciphertext)
println(plaintext.decodeToString()) // → "Hello Bob"

// Bob replies
val reply = bob.encrypt("Hi Alice".encodeToByteArray())
val pt2 = alice.decrypt(reply)
println(pt2.decodeToString())
```

## License

The library is licensed under the MIT License.
